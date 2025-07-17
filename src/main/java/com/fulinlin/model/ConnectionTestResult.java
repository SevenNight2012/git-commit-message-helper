package com.fulinlin.model;

/**
 * 连接测试结果
 */
public class ConnectionTestResult {
    private final boolean success;
    private final String message;
    private final String details;
    private final Exception exception;

    public ConnectionTestResult(boolean success, String message, String details, Exception exception) {
        this.success = success;
        this.message = message;
        this.details = details;
        this.exception = exception;
    }

    public static ConnectionTestResult success(String message) {
        return new ConnectionTestResult(true, message, null, null);
    }

    public static ConnectionTestResult failure(String message, String details, Exception exception) {
        return new ConnectionTestResult(false, message, details, exception);
    }

    public static ConnectionTestResult failure(String message, Exception exception) {
        return new ConnectionTestResult(false, message, exception.getMessage(), exception);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public Exception getException() {
        return exception;
    }
}