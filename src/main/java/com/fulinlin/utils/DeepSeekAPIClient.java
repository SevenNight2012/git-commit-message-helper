package com.fulinlin.utils;

import com.fulinlin.model.AISettings;
import com.fulinlin.model.ConnectionTestResult;
import com.fulinlin.utils.IDENotificationUtil;
import com.intellij.openapi.project.Project;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek API 客户端，兼容OpenAI风格
 */
public class DeepSeekAPIClient {
    private final AISettings settings;
    private final OkHttpClient httpClient;

    public DeepSeekAPIClient(AISettings settings) {
        this.settings = settings;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true) // 启用连接失败重试
                .build();
    }

    /**
     * 生成commit message（异步），带IDEA通知
     * @param prompt AI提示词
     * @param project 当前项目
     * @return CompletableFuture<String> AI生成内容
     */
    public CompletableFuture<String> generateMessage(String prompt, Project project) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String requestBody = buildRequestBody(prompt);
                Request request = new Request.Builder()
                        .url(settings.getApiEndpoint())
                        .addHeader("Authorization", "Bearer " + settings.getApiKey())
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorDetails = getErrorDetails(response);
                        String errorMessage = getErrorMessage(response.code(), errorDetails);
                        throw new IOException(errorMessage);
                    }
                    String responseBody = response.body().string();
                    return parseResponse(responseBody);
                }
            } catch (Exception e) {
                String errorMessage = getErrorMessage(e);
                IDENotificationUtil.notifyError(project, "DeepSeek API调用失败", errorMessage);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * 生成commit message（异步）
     * @param prompt AI提示词
     * @return CompletableFuture<String> AI生成内容
     */
    public CompletableFuture<String> generateMessage(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String requestBody = buildRequestBody(prompt);
                Request request = new Request.Builder()
                        .url(settings.getApiEndpoint())
                        .addHeader("Authorization", "Bearer " + settings.getApiKey())
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorDetails = getErrorDetails(response);
                        String errorMessage = getErrorMessage(response.code(), errorDetails);
                        throw new IOException(errorMessage);
                    }
                    String responseBody = response.body().string();
                    return parseResponse(responseBody);
                }
            } catch (Exception e) {
                String errorMessage = getErrorMessage(e);
                throw new RuntimeException("DeepSeek API调用异常: " + errorMessage, e);
            }
        });
    }

    /**
     * 获取错误详情
     */
    private String getErrorDetails(Response response) {
        try {
            if (response.body() != null) {
                return response.body().string();
            }
        } catch (Exception e) {
            // 忽略读取错误体的异常
        }
        return "";
    }

    /**
     * 根据HTTP状态码获取错误消息
     */
    private String getErrorMessage(int statusCode, String errorDetails) {
        switch (statusCode) {
            case 400:
                return "请求参数错误 (400)";
            case 401:
                return "API密钥无效或未授权 (401)";
            case 403:
                return "访问被拒绝，请检查API密钥权限 (403)";
            case 404:
                return "API端点不存在 (404)";
            case 429:
                return "请求频率过高，请稍后重试 (429)";
            case 500:
                return "服务器内部错误 (500)";
            case 502:
                return "网关错误 (502)";
            case 503:
                return "服务不可用 (503)";
            case 504:
                return "网关超时 (504)";
            default:
                return "HTTP错误 " + statusCode + ": " + errorDetails;
        }
    }

    /**
     * 根据异常类型获取错误消息
     */
    private String getErrorMessage(Exception e) {
        if (e instanceof java.net.ConnectException) {
            return "无法连接到服务器，请检查网络连接";
        } else if (e instanceof java.net.SocketTimeoutException) {
            return "连接超时，请稍后重试";
        } else if (e instanceof java.net.UnknownHostException) {
            return "无法解析服务器地址，请检查API端点配置";
        } else if (e instanceof javax.net.ssl.SSLException) {
            return "SSL连接失败，请检查网络设置";
        } else if (e instanceof java.net.NoRouteToHostException) {
            return "无法路由到主机，请检查网络连接";
        } else if (e.getMessage() != null) {
            return e.getMessage();
        } else {
            return "未知网络错误";
        }
    }

    /**
     * 构建请求体
     */
    private String buildRequestBody(String prompt) {
        JSONObject request = new JSONObject();
        request.put("model", settings.getModel());
        JSONArray messages = new JSONArray();
        // 可扩展：如有system prompt可加在前面
        messages.put(new JSONObject().put("role", "user").put("content", prompt));
        request.put("messages", messages);
        request.put("max_tokens", settings.getMaxTokens());
        request.put("temperature", settings.getTemperature());
        request.put("stream", false);
        return request.toString();
    }

    /**
     * 解析API响应，提取AI生成内容
     */
    private String parseResponse(String responseBody) {
        try {
            JSONObject obj = new JSONObject(responseBody);
            JSONArray choices = obj.optJSONArray("choices");
            if (choices != null && choices.length() > 0) {
                JSONObject message = choices.getJSONObject(0).optJSONObject("message");
                if (message != null) {
                    return message.optString("content", "");
                }
            }

            // 检查是否有错误信息
            if (obj.has("error")) {
                JSONObject error = obj.getJSONObject("error");
                String errorMessage = error.optString("message", "Unknown error");
                throw new RuntimeException("API返回错误: " + errorMessage);
            }

            return "";
        } catch (Exception e) {
            throw new RuntimeException("解析API响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 测试API Key和连通性
     * @return CompletableFuture<ConnectionTestResult> 详细的连接测试结果
     */
    public CompletableFuture<ConnectionTestResult> testConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 验证基本配置
                if (settings.getApiKey() == null || settings.getApiKey().trim().isEmpty()) {
                    return ConnectionTestResult.failure("API Key 未配置", "请检查API Key设置", null);
                }

                if (settings.getApiEndpoint() == null || settings.getApiEndpoint().trim().isEmpty()) {
                    return ConnectionTestResult.failure("API 端点未配置", "请检查API端点设置", null);
                }

                // 构建测试请求
                String requestBody = buildRequestBody("ping");
                Request request = new Request.Builder()
                        .url(settings.getApiEndpoint())
                        .addHeader("Authorization", "Bearer " + settings.getApiKey())
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = getErrorDetails(response);
                        String details = String.format("HTTP状态码: %d, 响应: %s", response.code(), errorBody);
                        String errorMessage = getErrorMessage(response.code(), errorBody);
                        return ConnectionTestResult.failure(errorMessage, details, null);
                    }

                    String responseBody = response.body().string();
                    // 连接测试成功：HTTP状态码为2xx表示请求成功
                    return ConnectionTestResult.success("连接测试成功 (HTTP " + response.code() + ")\n响应内容: " + responseBody);

                }
            } catch (IOException e) {
                String details = "网络连接异常: " + e.getMessage();
                String errorMessage = getErrorMessage(e);
                return ConnectionTestResult.failure(errorMessage, details, e);
            } catch (Exception e) {
                return ConnectionTestResult.failure("未知错误", e.getMessage(), e);
            }
        });
    }

    /**
     * 带重试的API调用
     */
    public CompletableFuture<String> generateMessageWithRetry(String prompt, int maxRetries) {
        return generateMessageWithRetry(prompt, maxRetries, 0);
    }

    private CompletableFuture<String> generateMessageWithRetry(String prompt, int maxRetries, int currentRetry) {
        return generateMessage(prompt)
                .exceptionally(error -> {
                    if (currentRetry < maxRetries && isRetryableError(error)) {
                        // 延迟重试
                        try {
                            Thread.sleep(1000 * (currentRetry + 1)); // 递增延迟
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("重试被中断", e);
                        }

                        return generateMessageWithRetry(prompt, maxRetries, currentRetry + 1).join();
                    } else {
                        throw new RuntimeException("API调用失败，已重试 " + currentRetry + " 次: " + error.getMessage(), error);
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
            message.contains("SocketTimeoutException") ||
            message.contains("UnknownHostException")) {
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
}