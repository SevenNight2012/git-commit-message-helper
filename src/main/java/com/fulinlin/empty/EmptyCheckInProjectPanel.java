package com.fulinlin.empty;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.changes.LocalChangeListImpl;
import com.intellij.openapi.vcs.changes.ui.DefaultCommitChangeListDialog;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.commit.CommitWorkflowHandler;
import com.intellij.vcs.commit.SingleChangeListCommitWorkflow;
import com.intellij.vcs.commit.SingleChangeListCommitWorkflowHandler;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class EmptyCheckInProjectPanel implements CheckinProjectPanel {
    @Override
    public JComponent getComponent() {
        return new JPanel();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return new JPanel();
    }

    @Override
    public @NotNull CommitWorkflowHandler getCommitWorkflowHandler() {
        Project project = ProjectManager.getInstance().getDefaultProject();
        LocalChangeList changeList = LocalChangeListImpl.createEmptyChangeList(project, "EmptyChangeList");
        SingleChangeListCommitWorkflow singleChangeListCommitWorkflow = new SingleChangeListCommitWorkflow(project, new HashSet<>(), new ArrayList<>(), changeList, new ArrayList<>(), true, true, "empty message", null);

        DefaultCommitChangeListDialog dialog = new DefaultCommitChangeListDialog(singleChangeListCommitWorkflow);
        return new SingleChangeListCommitWorkflowHandler(singleChangeListCommitWorkflow, dialog);
    }

    @Override
    public boolean hasDiffs() {
        return false;
    }

    @Override
    public Collection<VirtualFile> getVirtualFiles() {
        return new ArrayList<>();
    }

    @Override
    public Collection<Change> getSelectedChanges() {
        return new ArrayList<>();
    }

    @Override
    public Collection<File> getFiles() {
        return new ArrayList<>();
    }

    @Override
    public @NotNull Project getProject() {
        return ProjectManager.getInstance().getDefaultProject();
    }

    @Override
    public boolean vcsIsAffected(String name) {
        return false;
    }

    @Override
    public Collection<VirtualFile> getRoots() {
        return new ArrayList<>();
    }

    @Override
    public @NotNull @Nls String getCommitMessage() {
        return "";
    }

    @Override
    public @NlsContexts.Button String getCommitActionName() {
        return "";
    }

    @Override
    public void setCommitMessage(String currentDescription) {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void saveState() {

    }

    @Override
    public void restoreState() {

    }
}
