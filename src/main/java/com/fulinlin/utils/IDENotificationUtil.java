package com.fulinlin.utils;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class IDENotificationUtil {
    private static final String GROUP_ID = "AI Commit Message Helper";

    public static void notifyError(Project project, String title, String content) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(title, content, NotificationType.ERROR)
            .notify(project);
    }

    public static void notifyInfo(Project project, String title, String content) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(title, content, NotificationType.INFORMATION)
            .notify(project);
    }

    public static void notifyWarning(Project project, String title, String content) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(title, content, NotificationType.WARNING)
            .notify(project);
    }
}