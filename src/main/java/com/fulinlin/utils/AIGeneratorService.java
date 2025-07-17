package com.fulinlin.utils;

import com.fulinlin.model.AISettings;
import com.fulinlin.model.CommitTemplate;
import com.intellij.openapi.project.Project;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * 按照PromptBuilder中的提示语格式进行解析，兼容CommitPanel.form中的UI组件
     */
    private CommitTemplate parseToCommitTemplate(String aiResponse) {
        CommitTemplate template = new CommitTemplate();

        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return template;
        }

        try {
            // 按行分割响应
            String[] lines = aiResponse.split("\n");

            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue;
                }

                // 移除代码块标记
                if (trimmedLine.startsWith("```")) {
                    continue;
                }

                // 解析commit message格式: type(scope): subject
                if (isCommitMessageFormat(trimmedLine)) {
                    parseCommitMessageLine(trimmedLine, template);
                    continue;
                }

                // 解析各个字段（按照PromptBuilder中的格式）
                parseFieldLine(trimmedLine, template);
            }

            // 如果没有解析到type和subject，尝试从第一行提取
            if ((template.getType() == null || template.getType().isEmpty()) &&
                (template.getSubject() == null || template.getSubject().isEmpty())) {
                extractFromFirstLine(lines, template);
            }

        } catch (Exception e) {
            System.err.println("解析AI响应时发生异常: " + e.getMessage());
        }

        return template;
    }

    /**
     * 解析各个字段行
     */
    private void parseFieldLine(String line, CommitTemplate template) {
        String lowerLine = line.toLowerCase();

        // 按照PromptBuilder中的字段顺序解析
        if (lowerLine.startsWith("type:")) {
            String value = extractFieldValue(line, "type:");
            if (value != null) template.setType(value);
        } else if (lowerLine.startsWith("scope:")) {
            String value = extractFieldValue(line, "scope:");
            if (value != null) template.setScope(value);
        } else if (lowerLine.startsWith("subject:")) {
            String value = extractFieldValue(line, "subject:");
            if (value != null) template.setSubject(value);
        } else if (lowerLine.startsWith("body:")|| lowerLine.startsWith("-")) {
            String value = extractFieldValue(line, "body:");
            if (value != null) template.setBody(value);
        } else if (lowerLine.startsWith("breaking changes:")) {
            String value = extractFieldValue(line, "breaking changes:");
            if (value != null && !value.isEmpty()) {
                template.setChanges(value);
            }
        } else if (lowerLine.startsWith("closes:")) {
            String value = extractFieldValue(line, "closes:");
            if (value != null && !value.isEmpty()) {
                template.setCloses(value);
            }
        } else if (lowerLine.startsWith("skip ci:")) {
            String value = extractFieldValue(line, "skip ci:");
            if (value != null) {
                template.setSkipCi(value);
            }
        }
    }

    /**
     * 判断是否是commit message格式 (type(scope): subject)
     */
    private boolean isCommitMessageFormat(String line) {
        // 匹配格式：type(scope): subject 或 type: subject
        return line.matches("^[a-zA-Z]+(?:\\([^)]+\\))?:\\s+.+$") || line.startsWith("-");
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