package com.fulinlin.utils;

import com.fulinlin.model.AISettings;
import com.fulinlin.model.CommitTemplate;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 错误处理机制测试
 */
public class ErrorHandlingTest extends BasePlatformTestCase {

    @Test
    public void testNetworkConnectionError() {
        // 测试网络连接错误
        AISettings settings = createInvalidEndpointSettings();
        AIGeneratorService service = new AIGeneratorService(settings);

        CompletableFuture<CommitTemplate> future = service.generateCommitMessage(getProject(), "conventional_en", java.util.Locale.ENGLISH);

        try {
            CommitTemplate result = future.get();
            // 应该返回空的CommitTemplate而不是抛出异常
            assertNotNull("应该返回CommitTemplate对象", result);
        } catch (ExecutionException | InterruptedException e) {
            fail("不应该抛出异常，应该返回空的CommitTemplate");
        }
    }

    @Test
    public void testInvalidApiKeyError() {
        // 测试无效API Key错误
        AISettings settings = createInvalidApiKeySettings();
        AIGeneratorService service = new AIGeneratorService(settings);

        CompletableFuture<CommitTemplate> future = service.generateCommitMessage(getProject(), "conventional_en", java.util.Locale.ENGLISH);

        try {
            CommitTemplate result = future.get();
            // 应该返回空的CommitTemplate而不是抛出异常
            assertNotNull("应该返回CommitTemplate对象", result);
        } catch (ExecutionException | InterruptedException e) {
            fail("不应该抛出异常，应该返回空的CommitTemplate");
        }
    }

    @Test
    public void testRetryableErrorDetection() {
        // 测试可重试错误检测
        AIGeneratorService service = new AIGeneratorService(createValidSettings());

        // 模拟网络错误
        RuntimeException networkError = new RuntimeException("connect failed");
        assertTrue("网络连接错误应该是可重试的", isRetryableError(service, networkError));

        // 模拟超时错误
        RuntimeException timeoutError = new RuntimeException("timeout");
        assertTrue("超时错误应该是可重试的", isRetryableError(service, timeoutError));

        // 模拟服务器错误
        RuntimeException serverError = new RuntimeException("500 Internal Server Error");
        assertTrue("服务器错误应该是可重试的", isRetryableError(service, serverError));

        // 模拟频率限制错误
        RuntimeException rateLimitError = new RuntimeException("429 Too Many Requests");
        assertTrue("频率限制错误应该是可重试的", isRetryableError(service, rateLimitError));

        // 模拟认证错误
        RuntimeException authError = new RuntimeException("401 Unauthorized");
        assertFalse("认证错误不应该重试", isRetryableError(service, authError));
    }

    @Test
    public void testErrorMessageClassification() {
        // 测试错误消息分类
        AIGeneratorService service = new AIGeneratorService(createValidSettings());

        // 测试网络连接错误
        String networkError = getErrorMessage(service, new RuntimeException("connect failed"));
        assertTrue("网络连接错误消息应该包含连接信息", networkError.contains("网络连接失败"));

        // 测试超时错误
        String timeoutError = getErrorMessage(service, new RuntimeException("timeout"));
        assertTrue("超时错误消息应该包含超时信息", timeoutError.contains("请求超时"));

        // 测试API Key错误
        String apiKeyError = getErrorMessage(service, new RuntimeException("401 Unauthorized"));
        assertTrue("API Key错误消息应该包含认证信息", apiKeyError.contains("API密钥无效"));

        // 测试频率限制错误
        String rateLimitError = getErrorMessage(service, new RuntimeException("429 Too Many Requests"));
        assertTrue("频率限制错误消息应该包含频率信息", rateLimitError.contains("请求频率过高"));
    }

    @Test
    public void testUserFriendlyErrorMessage() {
        // 测试用户友好的错误消息
        AIGeneratorService service = new AIGeneratorService(createValidSettings());

        // 测试网络连接错误
        String networkError = getUserFriendlyErrorMessage(service, new RuntimeException("connect failed"));
        assertTrue("用户友好的网络错误消息应该包含中文", networkError.contains("无法连接到AI服务器"));

        // 测试API Key错误
        String apiKeyError = getUserFriendlyErrorMessage(service, new RuntimeException("401 Unauthorized"));
        assertTrue("用户友好的API Key错误消息应该包含中文", apiKeyError.contains("API密钥无效"));

        // 测试服务器错误
        String serverError = getUserFriendlyErrorMessage(service, new RuntimeException("500 Internal Server Error"));
        assertTrue("用户友好的服务器错误消息应该包含中文", serverError.contains("服务器内部错误"));
    }

    @Test
    public void testRetryWithMaxAttempts() {
        // 测试重试机制的最大尝试次数
        AISettings settings = createValidSettings();
        AIGeneratorService service = new AIGeneratorService(settings);

        CompletableFuture<CommitTemplate> future = service.generateCommitMessageWithRetry(
            getProject(), "conventional_en", java.util.Locale.ENGLISH, 3);

        try {
            CommitTemplate result = future.get();
            // 应该返回CommitTemplate对象
            assertNotNull("应该返回CommitTemplate对象", result);
        } catch (ExecutionException | InterruptedException e) {
            // 如果所有重试都失败，应该抛出异常
            assertTrue("重试失败后应该抛出异常", e.getCause() instanceof RuntimeException);
        }
    }

    // 辅助方法
    private AISettings createValidSettings() {
        AISettings settings = new AISettings();
        settings.setEnabled(true);
        settings.setApiKey("valid-api-key");
        settings.setApiEndpoint("https://api.deepseek.com/v1/chat/completions");
        return settings;
    }

    private AISettings createInvalidEndpointSettings() {
        AISettings settings = new AISettings();
        settings.setEnabled(true);
        settings.setApiKey("valid-api-key");
        settings.setApiEndpoint("https://invalid-endpoint.com");
        return settings;
    }

    private AISettings createInvalidApiKeySettings() {
        AISettings settings = new AISettings();
        settings.setEnabled(true);
        settings.setApiKey("invalid-api-key");
        settings.setApiEndpoint("https://api.deepseek.com/v1/chat/completions");
        return settings;
    }

    // 使用反射访问私有方法进行测试
    private boolean isRetryableError(AIGeneratorService service, RuntimeException error) {
        try {
            java.lang.reflect.Method method = AIGeneratorService.class.getDeclaredMethod("isRetryableError", Throwable.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(service, error);
        } catch (Exception e) {
            fail("无法访问isRetryableError方法: " + e.getMessage());
            return false;
        }
    }

    private String getErrorMessage(AIGeneratorService service, RuntimeException error) {
        try {
            java.lang.reflect.Method method = AIGeneratorService.class.getDeclaredMethod("getErrorMessage", Throwable.class);
            method.setAccessible(true);
            return (String) method.invoke(service, error);
        } catch (Exception e) {
            fail("无法访问getErrorMessage方法: " + e.getMessage());
            return "";
        }
    }

    private String getUserFriendlyErrorMessage(AIGeneratorService service, RuntimeException error) {
        try {
            java.lang.reflect.Method method = AIGeneratorService.class.getDeclaredMethod("getUserFriendlyErrorMessage", Throwable.class);
            method.setAccessible(true);
            return (String) method.invoke(service, error);
        } catch (Exception e) {
            fail("无法访问getUserFriendlyErrorMessage方法: " + e.getMessage());
            return "";
        }
    }
}