package com.fulinlin.utils;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import javax.swing.Timer;

/**
 * IntelliJ IDEA 多种通知方式工具类
 * 提供多种备选通知方案，确保用户能看到重要信息
 */
public class IDENotificationUtil {
    private static final Logger LOG = Logger.getInstance(IDENotificationUtil.class);
    private static final String GROUP_ID = "AI Commit Message Helper";

    /**
     * 显示错误信息（多种方式）
     */
    public static void notifyError(Project project, String title, String content) {
        LOG.warn("AI Error: " + title + " - " + content);
        // 方案1: 尝试使用通知系统
        try {
            if (project != null && !project.isDisposed()) {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup(GROUP_ID)
                    .createNotification(title, content, NotificationType.ERROR)
                    .notify(project);
                return;
            }
        } catch (Exception e) {
            LOG.warn("Notification system failed, trying alternative methods", e);
        }
        // 方案2: 用状态栏显示
        try {
            showStatusBarMessage(project, "❌ " + title + ": " + content);
        } catch (Exception e) {
            LOG.warn("Status bar failed", e);
        }
        // 方案3: 使用对话框（最后备选）
        try {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    Messages.showErrorDialog(content, title);
                } catch (Exception e) {
                    LOG.error("All notification methods failed", e);
                }
            });
        } catch (Exception e) {
            LOG.error("All notification methods failed", e);
        }
    }

    /**
     * 显示信息（多种方式）
     */
    public static void notifyInfo(Project project, String title, String content) {
        LOG.info("AI Info: " + title + " - " + content);
        // 方案1: 尝试使用通知系统
        try {
            if (project != null && !project.isDisposed()) {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup(GROUP_ID)
                    .createNotification(title, content, NotificationType.INFORMATION)
                    .notify(project);
                return;
            }
        } catch (Exception e) {
            LOG.warn("Notification system failed, trying alternative methods", e);
        }
        // 方案2: 用状态栏显示
        try {
            showStatusBarMessage(project, "ℹ️ " + title + ": " + content);
        } catch (Exception e) {
            LOG.warn("Status bar failed", e);
        }
        // 方案3: 用信息对话框
        try {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    Messages.showInfoMessage(content, title);
                } catch (Exception e) {
                    LOG.error("All notification methods failed", e);
                }
            });
        } catch (Exception e) {
            LOG.error("All notification methods failed", e);
        }
    }

    /**
     * 显示警告信息（多种方式）
     */
    public static void notifyWarning(Project project, String title, String content) {
        LOG.warn("AI Warning: " + title + " - " + content);
        // 方案1: 尝试使用通知系统
        try {
            if (project != null && !project.isDisposed()) {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup(GROUP_ID)
                    .createNotification(title, content, NotificationType.WARNING)
                    .notify(project);
                return;
            }
        } catch (Exception e) {
            LOG.warn("Notification system failed, trying alternative methods", e);
        }
        // 方案2: 用状态栏显示
        try {
            showStatusBarMessage(project, "⚠️ " + title + ": " + content);
        } catch (Exception e) {
            LOG.warn("Status bar failed", e);
        }
        // 方案3: 用警告对话框
        try {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    Messages.showWarningDialog(content, title);
                } catch (Exception e) {
                    LOG.error("All notification methods failed", e);
                }
            });
        } catch (Exception e) {
            LOG.error("All notification methods failed", e);
        }
    }

    /**
     * 在状态栏显示消息
     */
    private static void showStatusBarMessage(Project project, String message) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                if (project != null && !project.isDisposed()) {
                    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                    if (statusBar != null) {
                        statusBar.setInfo(message);
                        // 3秒后清除状态栏消息
                        new Timer(3000, e -> statusBar.setInfo("")).start();
                    }
                }
            } catch (Exception e) {
                LOG.warn("Status bar message failed", e);
            }
        });
    }

    /**
     * 显示成功消息（专门用于AI操作成功）
     */
    public static void notifySuccess(Project project, String title, String content) {
        LOG.info("AI Success: " + title + " - " + content);
        // 方案1: 尝试使用通知系统
        try {
            if (project != null && !project.isDisposed()) {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup(GROUP_ID)
                    .createNotification(title, content, NotificationType.INFORMATION)
                    .notify(project);
                return;
            }
        } catch (Exception e) {
            LOG.warn("Notification system failed, trying alternative methods", e);
        }
        // 方案2: 使用状态栏显示成功消息
        try {
            showStatusBarMessage(project, "✅ " + title + ": " + content);
        } catch (Exception e) {
            LOG.warn("Status bar failed", e);
        }
        // 方案3: 用信息对话框
        try {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    Messages.showInfoMessage(content, title);
                } catch (Exception e) {
                    LOG.error("All notification methods failed", e);
                }
            });
        } catch (Exception e) {
            LOG.error("All notification methods failed", e);
        }
    }
}