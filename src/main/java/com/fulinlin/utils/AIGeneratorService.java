package com.fulinlin.utils;

import com.fulinlin.model.AISettings;
import com.fulinlin.model.CommitTemplate;
import com.intellij.openapi.project.Project;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * AI生成主流程服务
 */
public class AIGeneratorService {
    private final CodeChangeAnalyzer analyzer;
    private final PromptBuilder promptBuilder;
    private final DeepSeekAPIClient apiClient;

    public AIGeneratorService(AISettings settings) {
        this.analyzer = new CodeChangeAnalyzer();
        this.promptBuilder = new PromptBuilder();
        this.apiClient = new DeepSeekAPIClient(settings);
    }

    /**
     * 串联分析、提示词、API调用、响应解析，异步生成CommitTemplate
     *
     * @param project     当前项目
     * @param templateKey 模板key（如conventional_en, conventional_zh）
     * @param locale      语言
     * @return CompletableFuture<CommitTemplate>
     */
    public CompletableFuture<CommitTemplate> generateCommitMessage(Project project,
            String templateKey, Locale locale) {
        return CompletableFuture.supplyAsync(() -> analyzer.analyzeChanges(project))
                .thenCompose(changeInfo -> {
                    String prompt = promptBuilder.buildPrompt(changeInfo, templateKey, locale);
                    return apiClient.generateMessage(prompt)
                            .thenApply(this::parseToCommitTemplate);
                })
                .exceptionally(e -> {
                    IDENotificationUtil.notifyError(project, "AI生成失败", e.getMessage());
                    return new CommitTemplate();
                });
    }

    /**
     * 解析AI响应为CommitTemplate对象
     * 支持多种AI响应格式：
     * 1. 完整的commit message格式：type(scope): subject
     * 2 解释部分包含详细说明
     * 3异常数据处理
     */
    private CommitTemplate parseToCommitTemplate(String aiResponse) {
        CommitTemplate template = new CommitTemplate();

        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return template;
        }

        try {
            // 按行分割响应
            String[] lines = aiResponse.split("\n");
            StringBuilder bodyBuilder = new StringBuilder();
            boolean inCodeBlock = false;
            boolean inBreakdownSection = false;
            boolean foundCommitMessage = false;

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                String trimmedLine = line.trim();

                if (trimmedLine.isEmpty()) {
                    continue;
                }

                // 检查是否进入代码块
                if (trimmedLine.startsWith("```")) {
                    inCodeBlock = !inCodeBlock;
                    continue;
                }

                // 如果在代码块内，解析commit message
                if (inCodeBlock) {
                    if (isCommitMessageFormat(trimmedLine)) {
                        parseCommitMessageLine(trimmedLine, template);
                        foundCommitMessage = true;
                        continue;
                    }

                    // 代码块内的其他内容作为body
                    if (!trimmedLine.startsWith("```")) {
                        bodyBuilder.append(trimmedLine).append("\n");
                    }
                    continue;
                }

                // 检查是否进入Breakdown部分
                if (trimmedLine.toLowerCase().contains("breakdown:") ||
                    trimmedLine.toLowerCase().contains("explanation:") ||
                    trimmedLine.toLowerCase().contains("说明：") ||
                    trimmedLine.toLowerCase().contains("解释：")) {
                    inBreakdownSection = true;
                    continue;
                }

                // 如果在Breakdown部分，解析各个字段
                if (inBreakdownSection) {
                    parseBreakdownLine(trimmedLine, template);
                    continue;
                }

                // 检查是否是commit message格式（不在代码块内的情况）
                if (!foundCommitMessage && isCommitMessageFormat(trimmedLine)) {
                    parseCommitMessageLine(trimmedLine, template);
                    foundCommitMessage = true;
                    continue;
                }

                // 其他内容：如果还没有找到commit message，可能是body内容
                if (!foundCommitMessage && !trimmedLine.startsWith("Here's") &&
                    !trimmedLine.toLowerCase().contains("commit message")) {
                    bodyBuilder.append(trimmedLine).append("\n");
                }
            }

            // 设置body内容，清理多余内容
            String body = cleanBodyContent(bodyBuilder.toString().trim());
            if (!body.isEmpty()) {
                template.setBody(body);
            }

            // 如果没有解析到type和subject，尝试从第一行提取
            if ((template.getType() == null || template.getType().isEmpty()) &&
                (template.getSubject() == null || template.getSubject().isEmpty())) {
                extractFromFirstLine(lines, template);
            }

        } catch (Exception e) {
            // 记录解析异常，但不抛出，返回空的template
            System.err.println("解析AI响应时发生异常: " + e.getMessage());
        }

        return template;
    }

    /**
     * 清理body内容，移除不需要的部分
     */
    private String cleanBodyContent(String body) {
        if (body == null || body.isEmpty()) {
            return "";
        }

        // 移除Breakdown部分及其后续内容
        String[] parts = body.split("(?i)breakdown:");
        if (parts.length > 1) {
            body = parts[0].trim();
        }

        // 移除Explanation部分及其后续内容
        parts = body.split("(?i)explanation:");
        if (parts.length > 1) {
            body = parts[0].trim();
        }

        // 移除说明部分及其后续内容
        parts = body.split("说明：");
        if (parts.length > 1) {
            body = parts[0].trim();
        }

        // 移除解释部分及其后续内容
        parts = body.split("解释：");
        if (parts.length > 1) {
            body = parts[0].trim();
        }

        return body;
    }

    /**
     * 解析Breakdown部分的每一行
     */
    private void parseBreakdownLine(String line, CommitTemplate template) {
        String lowerLine = line.toLowerCase();

        // 解析各个字段
        if (lowerLine.startsWith("type:") || lowerLine.startsWith("类型：")) {
            String value = extractFieldValue(line, "type:", "类型：");
            if (value != null) template.setType(value);
        } else if (lowerLine.startsWith("scope:") || lowerLine.startsWith("范围：")) {
            String value = extractFieldValue(line, "scope:", "范围：");
            if (value != null) template.setScope(value);
        } else if (lowerLine.startsWith("subject:") || lowerLine.startsWith("主题：")) {
            String value = extractFieldValue(line, "subject:", "主题：");
            if (value != null) template.setSubject(value);
        } else if (lowerLine.startsWith("body:") || lowerLine.startsWith("正文：")) {
            String value = extractFieldValue(line, "body:", "正文：");
            if (value != null) template.setBody(value);
        } else if (lowerLine.startsWith("breaking changes:") || lowerLine.startsWith("破坏性变更：")) {
            String value = extractFieldValue(line, "breaking changes:", "破坏性变更：");
            if (value != null && !value.equalsIgnoreCase("none")) {
                template.setChanges(value);
            }
        } else if (lowerLine.startsWith("closes:") || lowerLine.startsWith("关闭：")) {
            String value = extractFieldValue(line, "closes:", "关闭：");
            if (value != null && !value.equalsIgnoreCase("none")) {
                template.setCloses(value);
            }
        } else if (lowerLine.startsWith("skip ci:") || lowerLine.startsWith("跳过ci：")) {
            String value = extractFieldValue(line, "skip ci:", "跳过ci：");
            if (value != null) template.setSkipCi(value);
        }
    }

    /**
     * 判断是否是commit message格式 (type(scope): subject)
     */
    private boolean isCommitMessageFormat(String line) {
        // 匹配格式：type(scope): subject 或 type: subject
        return line.matches("^[a-zA-Z]+(?:\\([^)]+\\))?:\\s+.+$");
    }

    /**
     * 解析commit message行
     */
    private void parseCommitMessageLine(String line, CommitTemplate template) {
        try {
            // 移除可能的markdown代码块标记
            line = line.replaceAll("^```.*$", "").trim();

            // 匹配 type(scope): subject 格式
            String pattern = "^([a-zA-Z]+)(?:\\(([^)]+)\\))?:\\s+(.+)$";
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(line);

            if (matcher.find()) {
                template.setType(matcher.group(1));
                if (matcher.group(2) != null) {
                    template.setScope(matcher.group(2));
                }
                template.setSubject(matcher.group(3));
            }
        } catch (Exception e) {
            System.err.println("解析commit message行时发生异常: " + e.getMessage());
        }
    }

    /**
     * 提取字段值
     */
    private String extractFieldValue(String line, String... prefixes) {
        for (String prefix : prefixes) {
            if (line.toLowerCase().startsWith(prefix.toLowerCase())) {
                String value = line.substring(prefix.length()).trim();
                // 移除可能的引号
                value = value.replaceAll("^['\"]|['\"]$", "");
                return value;
            }
        }
        return null;
    }

    /**
     * 从第一行提取信息（备用方案）
     */
    private void extractFromFirstLine(String[] lines, CommitTemplate template) {
        if (lines.length == 0) return;

        String firstLine = lines[0].trim();

        // 尝试从第一行提取commit message格式
        if (isCommitMessageFormat(firstLine)) {
            parseCommitMessageLine(firstLine, template);
        } else {
            // 如果不是标准格式，尝试提取有意义的内容作为subject
            String content = firstLine.replaceAll("^```.*$", "").trim();
            if (content.length() > 0 && content.length() <= 100) {
                template.setSubject(content);
            }
        }
    }
}