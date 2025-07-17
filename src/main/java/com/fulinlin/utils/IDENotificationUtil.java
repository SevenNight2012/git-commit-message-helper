package com.fulinlin.utils;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

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
        // 方案3: 使用可复制的对话框（最后备选）
        try {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    showCopyableDialog(title, content, Messages.getErrorIcon());
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
        // 方案3: 用可复制的信息对话框
        try {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    showCopyableDialog(title, content, Messages.getInformationIcon());
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
        // 方案3: 用可复制的警告对话框
        try {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    showCopyableDialog(title, content, Messages.getWarningIcon());
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
        // 方案3: 用可复制的信息对话框
        try {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    showCopyableDialog(title, content, Messages.getInformationIcon());
                } catch (Exception e) {
                    LOG.error("All notification methods failed", e);
                }
            });
        } catch (Exception e) {
            LOG.error("All notification methods failed", e);
        }
    }

    /**
     * 显示可复制的对话框
     */
    private static void showCopyableDialog(String title, String content, Icon icon) {
        CopyableMessageDialog dialog = new CopyableMessageDialog(title, content, icon);
        dialog.show();
    }

    /**
     * 可复制的消息对话框
     */
    private static class CopyableMessageDialog extends DialogWrapper {
        private final String content;
        private final Icon icon;

        public CopyableMessageDialog(String title, String content, Icon icon) {
            super(true);
            this.content = content;
            this.icon = icon;
            setTitle(title);
            setResizable(true);
            init();
        }

        @Override
        protected JComponent createCenterPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setPreferredSize(new Dimension(500, 300));

            // 创建可选择的文本区域
            JBTextArea textArea = new JBTextArea(content);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(JBUI.Fonts.label());
            textArea.setBackground(UIManager.getColor("Panel.background"));
            textArea.setBorder(JBUI.Borders.empty(10));

            // 添加滚动面板
            JBScrollPane scrollPane = new JBScrollPane(textArea);
            scrollPane.setBorder(JBUI.Borders.compound(
                JBUI.Borders.customLine(JBUI.CurrentTheme.DefaultTabs.borderColor()),
                JBUI.Borders.empty(5)
            ));

            // 创建按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            // 复制按钮
            JButton copyButton = new JButton("Copy to Clipboard");
            copyButton.addActionListener(e -> {
                StringSelection selection = new StringSelection(content);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            });

            // 确定按钮
            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> close(OK_EXIT_CODE));

            buttonPanel.add(copyButton);
            buttonPanel.add(okButton);

            // 组装面板
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            return panel;
        }

        @Override
        protected Action[] createActions() {
            return new Action[0]; // 不使用默认按钮，使用自定义按钮
        }
    }
}