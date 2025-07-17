package com.fulinlin.utils;

import com.fulinlin.model.AISettings;
import com.fulinlin.model.CommitTemplate;
import com.intellij.openapi.project.Project;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI生成主流程服务
 */
public class AIGeneratorService {
    private final CodeChangeAnalyzer analyzer;
    private final PromptBuilder promptBuilder;
    private final DeepSeekAPIClient apiClient;
    private final AISettings settings;

    public AIGeneratorService(AISettings settings) {
        this.settings = settings;
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
        return CompletableFuture.supplyAsync(() -> {
            try {
                return analyzer.analyzeChanges(project);
            } catch (Exception e) {
                throw new RuntimeException("代码变更分析失败: " + e.getMessage(), e);
            }
        })
        .thenCompose(changeInfo -> {
            try {
                String prompt = promptBuilder.buildPrompt(changeInfo, templateKey, locale);
                return apiClient.generateMessage(prompt)
                        .thenApply(this::parseToCommitTemplate)
                        .exceptionally(error -> {
                            throw new RuntimeException("AI生成失败: " + getErrorMessage(error), error);
                        });
            } catch (Exception e) {
                throw new RuntimeException("提示词构建失败: " + e.getMessage(), e);
            }
        })
        .exceptionally(error -> {
            handleError(project, error);
            return new CommitTemplate();
        });
    }

    /**
     * 带重试的AI生成
     */
    public CompletableFuture<CommitTemplate> generateCommitMessageWithRetry(Project project,
            String templateKey, Locale locale, int maxRetries) {
        return generateCommitMessageWithRetry(project, templateKey, locale, maxRetries, 0);
    }

    private CompletableFuture<CommitTemplate> generateCommitMessageWithRetry(Project project,
            String templateKey, Locale locale, int maxRetries, int currentRetry) {
        return generateCommitMessage(project, templateKey, locale)
                .exceptionally(error -> {
                    if (currentRetry < maxRetries && isRetryableError(error)) {
                        // 延迟重试
                        try {
                            Thread.sleep(1000 * (currentRetry + 1)); // 递增延迟
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("重试被中断", e);
                        }

                        IDENotificationUtil.notifyInfo(project, "AI生成重试",
                            "第 " + (currentRetry + 1) + " 次重试...");

                        return generateCommitMessageWithRetry(project, templateKey, locale, maxRetries, currentRetry + 1)
                                .join();
                    } else {
                        throw new RuntimeException("AI生成失败，已重试 " + currentRetry + " 次: " + error.getMessage(), error);
                    }
                });
    }

    /**
     * 判断错误是否可重试
     */
    private boolean isRetryableError(Throwable error) {
        String message = error.getMessage();
        if (message == null) return false;

        // 网络相关错误可以重试
        if (message.contains("connect") ||
            message.contains("Connection refused") ||
            message.contains("timeout") ||
            message.contains("SocketTimeoutException")) {
            return true;
        }

        // 服务器错误可以重试
        if (message.contains("500") ||
            message.contains("Internal Server Error") ||
            message.contains("502") ||
            message.contains("503") ||
            message.contains("504")) {
            return true;
        }

        // 频率限制可以重试
        if (message.contains("429") || message.contains("Too Many Requests")) {
            return true;
        }

        return false;
    }

    /**
     * 处理错误
     */
    private void handleError(Project project, Throwable error) {
        String errorMessage = getErrorMessage(error);
        String userFriendlyMessage = getUserFriendlyErrorMessage(error);

        // 记录错误
        System.err.println("AI Generation Error: " + errorMessage);
        error.printStackTrace();

        // 发送通知
        IDENotificationUtil.notifyError(project, "AI生成失败", userFriendlyMessage);
    }

    /**
     * 获取错误消息
     */
    private String getErrorMessage(Throwable error) {
        if (error instanceof java.net.ConnectException ||
            error.getMessage().contains("connect") ||
            error.getMessage().contains("Connection refused")) {
            return "网络连接失败";
        } else if (error instanceof java.net.SocketTimeoutException ||
                   error.getMessage().contains("timeout")) {
            return "请求超时";
        } else if (error.getMessage().contains("401") ||
                   error.getMessage().contains("Unauthorized")) {
            return "API密钥无效";
        } else if (error.getMessage().contains("429") ||
                   error.getMessage().contains("Too Many Requests")) {
            return "请求频率过高";
        } else if (error.getMessage().contains("500") ||
                   error.getMessage().contains("Internal Server Error")) {
            return "服务器内部错误";
        } else if (error.getMessage().contains("403") ||
                   error.getMessage().contains("Forbidden")) {
            return "访问被拒绝";
        } else if (error.getMessage().contains("404") ||
                   error.getMessage().contains("Not Found")) {
            return "API端点不存在";
        } else {
            return error.getMessage();
        }
    }

    /**
     * 获取用户友好的错误消息
     */
    private String getUserFriendlyErrorMessage(Throwable error) {
        String message = error.getMessage();

        if (message.contains("connect") || message.contains("Connection refused")) {
            return "无法连接到AI服务器。请检查网络连接或稍后重试。";
        } else if (message.contains("timeout")) {
            return "请求超时。服务器响应时间过长，请稍后重试。";
        } else if (message.contains("401") || message.contains("Unauthorized")) {
            return "API密钥无效。请在设置中检查并更新您的API密钥。";
        } else if (message.contains("429") || message.contains("Too Many Requests")) {
            return "请求频率过高。请稍等片刻后重试。";
        } else if (message.contains("500") || message.contains("Internal Server Error")) {
            return "服务器内部错误。请稍后重试或联系技术支持。";
        } else if (message.contains("403") || message.contains("Forbidden")) {
            return "访问被拒绝。请检查您的API密钥权限。";
        } else if (message.contains("404") || message.contains("Not Found")) {
            return "API端点不存在。请检查API端点配置。";
        } else if (message.contains("代码变更分析失败")) {
            return "无法分析代码变更。请确保项目中有未提交的更改。";
        } else if (message.contains("提示词构建失败")) {
            return "无法构建AI提示词。请检查模板配置。";
        } else {
            return "AI生成失败：" + message;
        }
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