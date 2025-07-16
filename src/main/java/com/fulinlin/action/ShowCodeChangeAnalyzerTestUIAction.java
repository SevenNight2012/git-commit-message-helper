package com.fulinlin.action;

import com.fulinlin.ui.CodeChangeAnalyzerDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ShowCodeChangeAnalyzerTestUIAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        CodeChangeAnalyzerDialog ui = new CodeChangeAnalyzerDialog(project);
        ui.setVisible(true);
    }
}