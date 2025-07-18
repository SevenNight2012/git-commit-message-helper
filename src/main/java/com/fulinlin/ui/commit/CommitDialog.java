package com.fulinlin.ui.commit;

import com.fulinlin.localization.PluginBundle;
import com.fulinlin.model.AISettings;
import com.fulinlin.model.CommitTemplate;
import com.fulinlin.storage.GitCommitMessageHelperSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.CheckinProjectPanel;

import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CommitDialog extends DialogWrapper {

    private final CommitPanel commitPanel;
    private final AIGeneratePanel aiGeneratePanel;
    private final JTabbedPane tabbedPane;
    private final GitCommitMessageHelperSettings settings;
    private final Project project;
    private boolean aiTabAvailable = false;

    public CommitDialog(@Nullable Project project, GitCommitMessageHelperSettings settings,
            CommitTemplate commitMessageTemplate,
            CheckinProjectPanel gitCommitPanel) {
        super(project);
        this.project = project;
        this.settings = settings;

        // Initialize panels
        commitPanel = new CommitPanel(project, settings, commitMessageTemplate);
        aiGeneratePanel = new AIGeneratePanel(project, settings.getAISettings(), gitCommitPanel);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manual Build", commitPanel.getMainPanel());

        // Check AI availability and add AI tab conditionally
        aiTabAvailable = isAIAvailable();
        if (aiTabAvailable) {
            tabbedPane.addTab("AI Generate", aiGeneratePanel.getMainPanel());
        }

        // Set default tab based on AI availability
        if (aiTabAvailable && settings.getAISettings() != null && settings.getAISettings().isEnabled()) {
            tabbedPane.setSelectedIndex(1); // AI Generate tab
        } else {
            tabbedPane.setSelectedIndex(0); // Manual Build tab
        }

        // Add tab change listener for user guidance
        tabbedPane.addChangeListener(new TabChangeListener());

        setTitle(PluginBundle.get("commit.panel.title"));
        setOKButtonText(PluginBundle.get("commit.panel.ok.button"));
        setCancelButtonText(PluginBundle.get("commit.panel.cancel.button"));
        init();
    }

    /**
     * 检查AI功能是否可用
     */
    private boolean isAIAvailable() {
        if (settings.getAISettings() == null) {
            return false;
        }

        AISettings aiSettings = settings.getAISettings();

        // 检查基本配置
        if (!aiSettings.isEnabled()) {
            return false;
        }

        if (aiSettings.getApiKey() == null || aiSettings.getApiKey().trim().isEmpty()) {
            return false;
        }

        if (aiSettings.getApiEndpoint() == null || aiSettings.getApiEndpoint().trim().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * 选项卡切换监听器，提供用户引导
     */
    private class TabChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            int selectedIndex = tabbedPane.getSelectedIndex();

            // 只在AI选项卡被选中时显示提示（如果AI选项卡可用）
            if (aiTabAvailable && selectedIndex == 1) {
                showAITabGuidance();
            }
        }
    }

        /**
     * 显示AI选项卡使用指南
     */
    private void showAITabGuidance() {
        // 检查AI选项卡是否有内容，如果没有则显示提示
        if (!aiGeneratePanel.hasContent()) {
            SwingUtilities.invokeLater(() -> {
                // 使用更简洁的提示，避免过多的弹窗干扰
                showAITabTooltip();
            });
        }
    }

    /**
     * 显示AI选项卡提示信息
     */
    private void showAITabTooltip() {
        // 在AI选项卡上显示一个临时的状态提示
        aiGeneratePanel.showGuidanceMessage("点击'AI Generate'按钮开始生成提交信息");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return tabbedPane;
    }

    /**
     * 获取当前选项卡的commit message字符串
     * @return commit message字符串
     */
    public String getCommitMessageString() {
        // Check which tab is currently selected
        int selectedIndex = tabbedPane.getSelectedIndex();

        if (aiTabAvailable && selectedIndex == 1) { // AI Generate tab
            // Return AI generated content directly
            return aiGeneratePanel.getAIContent();
        }

        // Manual build tab or fallback - return the formatted commit message
        return this.commitPanel.getCommitMessage(settings).toString();
    }

    @Nullable
    public CommitTemplate getCommitMessageTemplate() {
        // Check which tab is currently selected
        int selectedIndex = tabbedPane.getSelectedIndex();

        if (aiTabAvailable && selectedIndex == 1) { // AI Generate tab
            return null;
        }
        // Manual build tab or fallback
        return commitPanel.getCommitMessageTemplate();
    }

    /**
     * 获取当前选中的选项卡索引
     */
    public int getSelectedTabIndex() {
        return tabbedPane.getSelectedIndex();
    }

    /**
     * 检查AI选项卡是否可用
     */
    public boolean isAITabAvailable() {
        return aiTabAvailable;
    }

    /**
     * 手动切换到AI选项卡（如果可用）
     */
    public void switchToAITab() {
        if (aiTabAvailable) {
            tabbedPane.setSelectedIndex(1);
        }
    }

    /**
     * 手动切换到手动构建选项卡
     */
    public void switchToManualTab() {
        tabbedPane.setSelectedIndex(0);
    }
}