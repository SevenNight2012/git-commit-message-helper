package com.fulinlin.utils;

import com.fulinlin.model.AISettings;
import com.intellij.openapi.project.Project;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
     * 串联分析、提示词、API调用，异步生成commit message字符串
     *
     * @param project     当前项目
     * @param templateKey 模板key（如conventional_en, conventional_zh）
     * @param locale      语言
     * @return CompletableFuture<String>
     */
    public CompletableFuture<String> generateCommitMessage(Project project,
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
                        .exceptionally(error -> {
                            throw new RuntimeException("AI生成失败: " + getErrorMessage(error), error);
                        });
            } catch (Exception e) {
                throw new RuntimeException("提示词构建失败: " + e.getMessage(), e);
            }
        })
        .exceptionally(error -> {
            handleError(project, error);
            return "";
        });
    }

    /**
     * 带重试的AI生成
     */
    public CompletableFuture<String> generateCommitMessageWithRetry(Project project,
            String templateKey, Locale locale, int maxRetries) {
        return generateCommitMessageWithRetry(project, templateKey, locale, maxRetries, 0);
    }

    private CompletableFuture<String> generateCommitMessageWithRetry(Project project,
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


}