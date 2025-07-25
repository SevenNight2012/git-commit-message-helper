package com.fulinlin.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网络请求日志记录器
 * 记录网络请求异常信息，支持按天分文件存储，最多保存一周的日志
 */
public class NetworkRequestLogger {
    private static final Logger LOG = Logger.getInstance(NetworkRequestLogger.class);
    private static final String LOG_DIR_NAME = "network-request-logs";
    private static final int MAX_LOG_DAYS = 7; // 最多保存7天的日志
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final ExecutorService executorService;
    private final Path logDirectory;

    public NetworkRequestLogger() {
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "NetworkRequestLogger");
            thread.setDaemon(true);
            return thread;
        });
        this.logDirectory = getLogDirectory();
        ensureLogDirectoryExists();
        cleanupOldLogs();
    }

    /**
     * 记录网络请求异常信息
     * @param requestInfo 请求信息
     * @param responseInfo 响应信息
     * @param exception 异常信息
     * @param methodName 发生异常的方法名
     */
    public void logRequestException(@NotNull RequestInfo requestInfo,
                                   @NotNull ResponseInfo responseInfo,
                                   @NotNull Exception exception,
                                   @NotNull String methodName) {
        CompletableFuture.runAsync(() -> {
            try {
                String logEntry = buildLogEntry(requestInfo, responseInfo, exception, methodName);
                writeLogEntry(logEntry);
            } catch (Exception e) {
                LOG.warn("Failed to log network request exception", e);
            }
        }, executorService);
    }

    /**
     * 记录网络请求成功信息
     * @param requestInfo 请求信息
     * @param responseInfo 响应信息
     * @param methodName 方法名
     */
    public void logRequestSuccess(@NotNull RequestInfo requestInfo,
                                  @NotNull ResponseInfo responseInfo,
                                  @NotNull String methodName) {
        CompletableFuture.runAsync(() -> {
            try {
                String logEntry = buildSuccessLogEntry(requestInfo, responseInfo, methodName);
                writeLogEntry(logEntry, true);
            } catch (Exception e) {
                LOG.warn("Failed to log network request success", e);
            }
        }, executorService);
    }

    /**
     * 构建日志条目
     */
    private String buildLogEntry(RequestInfo requestInfo, ResponseInfo responseInfo,
                                Exception exception, String methodName) {
        StringBuilder sb = new StringBuilder();
        String timestamp = java.time.LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        sb.append("=== Network Request Exception Log ===\n");
        sb.append("Timestamp: ").append(timestamp).append("\n");
        sb.append("Method: ").append(methodName).append("\n");
        sb.append("Exception: ").append(exception.getClass().getSimpleName()).append("\n");
        sb.append("Exception Message: ").append(exception.getMessage()).append("\n");

        // 请求信息
        sb.append("\n--- Request Information ---\n");
        sb.append("URL: ").append(requestInfo.getUrl()).append("\n");
        sb.append("Method: ").append(requestInfo.getMethod()).append("\n");
        sb.append("Headers: ").append(requestInfo.getHeaders()).append("\n");
        sb.append("Request Body: ").append(requestInfo.getRequestBody()).append("\n");

        // 响应信息
        sb.append("\n--- Response Information ---\n");
        sb.append("Status Code: ").append(responseInfo.getStatusCode()).append("\n");
        sb.append("Response Headers: ").append(responseInfo.getHeaders()).append("\n");
        sb.append("Response Body: ").append(responseInfo.getResponseBody()).append("\n");

        // 异常堆栈
        sb.append("\n--- Exception Stack Trace ---\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        sb.append(sw.toString());

        sb.append("\n=== End Log Entry ===\n\n");

        return sb.toString();
    }

    /**
     * 构建成功日志条目
     */
    private String buildSuccessLogEntry(RequestInfo requestInfo, ResponseInfo responseInfo, String methodName) {
        StringBuilder sb = new StringBuilder();
        String timestamp = java.time.LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        sb.append("=== Network Request Success Log ===\n");
        sb.append("Timestamp: ").append(timestamp).append("\n");
        sb.append("Method: ").append(methodName).append("\n");
        // 请求信息
        sb.append("\n--- Request Information ---\n");
        sb.append("URL: ").append(requestInfo.getUrl()).append("\n");
        sb.append("Method: ").append(requestInfo.getMethod()).append("\n");
        sb.append("Headers: ").append(requestInfo.getHeaders()).append("\n");
        sb.append("Request Body: ").append(requestInfo.getRequestBody()).append("\n");
        // 响应信息
        sb.append("\n--- Response Information ---\n");
        sb.append("Status Code: ").append(responseInfo.getStatusCode()).append("\n");
        sb.append("Response Headers: ").append(responseInfo.getHeaders()).append("\n");
        sb.append("Response Body: ").append(responseInfo.getResponseBody()).append("\n");
        sb.append("\n=== End Log Entry ===\n\n");
        return sb.toString();
    }

    /**
     * 写入日志条目到文件
     * @param logEntry 日志内容
     * @param isSuccess 是否为成功日志
     */
    private void writeLogEntry(String logEntry, boolean isSuccess) throws IOException {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String fileType = isSuccess ? "success" : "fail";
        Path logFile = logDirectory.resolve("network-request-" + fileType + "-" + today + ".log");
        if (!Files.exists(logFile)) {
            Files.createFile(logFile);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND)) {
            writer.write(logEntry);
            writer.flush();
        }
    }

    // 修改原有失败日志写入调用
    private void writeLogEntry(String logEntry) throws IOException {
        writeLogEntry(logEntry, false);
    }

    /**
     * 获取日志目录
     */
    public static Path getLogDirectory() {
        // 使用IntelliJ IDEA的缓存目录
        String cachePath = System.getProperty("idea.system.path");
        if (cachePath == null) {
            // 备用方案：使用用户目录
            cachePath = System.getProperty("user.home");
        }

        Path basePath = Paths.get(cachePath);
        if (SystemInfo.isWindows) {
            // Windows: 在缓存目录下创建插件日志目录
            return basePath.resolve("GitCommitMessageHelper").resolve(LOG_DIR_NAME);
        } else {
            // Unix/Linux/macOS: 在缓存目录下创建插件日志目录
            return basePath.resolve(".GitCommitMessageHelper").resolve(LOG_DIR_NAME);
        }
    }

    /**
     * 确保日志目录存在
     */
    private void ensureLogDirectoryExists() {
        try {
            if (!Files.exists(logDirectory)) {
                Files.createDirectories(logDirectory);
            }
        } catch (IOException e) {
            LOG.warn("Failed to create log directory: " + logDirectory, e);
        }
    }

    /**
     * 清理旧日志文件（保留最近7天）
     */
    private void cleanupOldLogs() {
        CompletableFuture.runAsync(() -> {
            try {
                LocalDate cutoffDate = LocalDate.now().minusDays(MAX_LOG_DAYS);

                if (Files.exists(logDirectory)) {
                    Files.list(logDirectory)
                        .filter(path -> path.toString().endsWith(".log"))
                        .forEach(path -> {
                            try {
                                String fileName = path.getFileName().toString();
                                // 提取日期部分：network-request-2024-01-01.log -> 2024-01-01
                                String dateStr = fileName.replace("network-request-", "").replace(".log", "");
                                LocalDate fileDate = LocalDate.parse(dateStr, DATE_FORMATTER);

                                if (fileDate.isBefore(cutoffDate)) {
                                    Files.deleteIfExists(path);
                                    LOG.info("Deleted old log file: " + fileName);
                                }
                            } catch (Exception e) {
                                LOG.warn("Failed to process log file: " + path, e);
                            }
                        });
                }
            } catch (Exception e) {
                LOG.warn("Failed to cleanup old logs", e);
            }
        }, executorService);
    }

    /**
     * 关闭日志记录器
     */
    public void shutdown() {
        try {
            executorService.shutdown();
        } catch (Exception ignore) {
        }
    }

    /**
     * 请求信息数据类
     */
    public static class RequestInfo {
        private final String url;
        private final String method;
        private final String headers;
        private final String requestBody;

        public RequestInfo(String url, String method, String headers, String requestBody) {
            this.url = url != null ? url : "";
            this.method = method != null ? method : "";
            this.headers = headers != null ? headers : "";
            this.requestBody = requestBody != null ? requestBody : "";
        }

        public String getUrl() { return url; }
        public String getMethod() { return method; }
        public String getHeaders() { return headers; }
        public String getRequestBody() { return requestBody; }
    }

    /**
     * 响应信息数据类
     */
    public static class ResponseInfo {
        private final int statusCode;
        private final String headers;
        private final String responseBody;

        public ResponseInfo(int statusCode, String headers, String responseBody) {
            this.statusCode = statusCode;
            this.headers = headers != null ? headers : "";
            this.responseBody = responseBody != null ? responseBody : "";
        }

        public int getStatusCode() { return statusCode; }
        public String getHeaders() { return headers; }
        public String getResponseBody() { return responseBody; }
    }
}