package com.fulinlin.utils;

import com.fulinlin.model.AISettings;
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
     * @return true=连通，false=失败
     */
    public CompletableFuture<Boolean> testConnection() {
        // 用一个简单的prompt测试
        return generateMessage("ping").thenApply(result -> result != null && !result.isEmpty())
                .exceptionally(e -> false);
    }
}