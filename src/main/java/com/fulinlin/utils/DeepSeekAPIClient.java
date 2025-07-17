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
                        String msg = "API调用失败: " + response.code() + " - " + response.message();
                        IDENotificationUtil.notifyError(project, "DeepSeek API调用失败", msg);
                        throw new IOException(msg);
                    }
                    String responseBody = response.body().string();
                    return parseResponse(responseBody);
                }
            } catch (Exception e) {
                IDENotificationUtil.notifyError(project, "DeepSeek API调用异常", e.getMessage());
            }
            return "Error";
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
                        throw new IOException("API调用失败: " + response.code() + " - " + response.message());
                    }
                    String responseBody = response.body().string();
                    return parseResponse(responseBody);
                }
            } catch (Exception e) {
                throw new RuntimeException("DeepSeek API调用异常: " + e.getMessage(), e);
            }
        });
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
        JSONObject obj = new JSONObject(responseBody);
        JSONArray choices = obj.optJSONArray("choices");
        if (choices != null && choices.length() > 0) {
            JSONObject message = choices.getJSONObject(0).optJSONObject("message");
            if (message != null) {
                return message.optString("content", "");
            }
        }
        return "";
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
                    return ConnectionTestResult.failure("API Key 未配置, 检查API Key设置", null);
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
                        String errorBody = "";
                        if (response.body() != null) {
                            errorBody = response.body().string();
                        }

                        String details = String.format("HTTP状态码: %d, 响应: %s", response.code(), errorBody);

                        if (response.code() == 401) {
                            return ConnectionTestResult.failure("API Key 无效", details, null);
                        } else if (response.code() == 404) {
                            return ConnectionTestResult.failure("API 端点不存在", details, null);
                        } else if (response.code() >= 500) {
                            return ConnectionTestResult.failure("服务器内部错误", details, null);
                        } else {
                            return ConnectionTestResult.failure("API调用失败", details, null);
                        }
                    }

                    String responseBody = response.body().string();
                    // 连接测试成功：HTTP状态码为2xx表示请求成功
                    return ConnectionTestResult.success("连接测试成功 (HTTP " + response.code() + ")\n响应内容: " + responseBody);

                }
            } catch (IOException e) {
                String details = "网络连接异常: " + e.getMessage();
                if (e.getMessage().contains("connect")) {
                    return ConnectionTestResult.failure("无法连接到服务器", details, e);
                } else if (e.getMessage().contains("timeout")) {
                    return ConnectionTestResult.failure("连接超时", details, e);
                } else {
                    return ConnectionTestResult.failure("网络错误", details, e);
                }
            } catch (Exception e) {
                return ConnectionTestResult.failure("未知错误", e.getMessage(), e);
            }
        });
    }
}