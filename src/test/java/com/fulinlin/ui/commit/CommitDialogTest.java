package com.fulinlin.ui.commit;

import com.fulinlin.empty.EmptyCheckInProjectPanel;
import com.fulinlin.model.AISettings;
import com.fulinlin.model.CommitTemplate;
import com.fulinlin.storage.GitCommitMessageHelperSettings;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

import javax.swing.*;

/**
 * CommitDialog选项卡状态管理测试
 */
public class CommitDialogTest extends BasePlatformTestCase {

    @Test
    public void testAITabAvailability() {
        // 测试AI功能可用时的情况
        GitCommitMessageHelperSettings settings = new GitCommitMessageHelperSettings();
        AISettings aiSettings = new AISettings();
        aiSettings.setEnabled(true);
        aiSettings.setApiKey("test-api-key");
        aiSettings.setApiEndpoint("https://api.test.com");
        settings.setAISettings(aiSettings);

        CommitTemplate template = new CommitTemplate();
        CheckinProjectPanel panel = new EmptyCheckInProjectPanel();
        CommitDialog dialog = new CommitDialog(getProject(), settings, template, panel);

        // 验证AI选项卡可用
        assertTrue("AI选项卡应该可用", dialog.isAITabAvailable());
        assertEquals("应该有两个选项卡", 2, ((JTabbedPane) dialog.getContentPane()).getTabCount());
    }

    @Test
    public void testAITabUnavailable() {
        // 测试AI功能不可用时的情况
        GitCommitMessageHelperSettings settings = new GitCommitMessageHelperSettings();
        AISettings aiSettings = new AISettings();
        aiSettings.setEnabled(false); // AI功能禁用
        settings.setAISettings(aiSettings);

        CommitTemplate template = new CommitTemplate();
        CheckinProjectPanel panel = new EmptyCheckInProjectPanel();
        CommitDialog dialog = new CommitDialog(getProject(), settings, template, panel);

        // 验证AI选项卡不可用
        assertFalse("AI选项卡应该不可用", dialog.isAITabAvailable());
        assertEquals("应该只有一个选项卡", 1, ((JTabbedPane) dialog.getContentPane()).getTabCount());
    }

    @Test
    public void testAITabUnavailableNoApiKey() {
        // 测试没有API Key时的情况
        GitCommitMessageHelperSettings settings = new GitCommitMessageHelperSettings();
        AISettings aiSettings = new AISettings();
        aiSettings.setEnabled(true);
        aiSettings.setApiKey(""); // 空的API Key
        aiSettings.setApiEndpoint("https://api.test.com");
        settings.setAISettings(aiSettings);

        CommitTemplate template = new CommitTemplate();
        CheckinProjectPanel panel = new EmptyCheckInProjectPanel();
        CommitDialog dialog = new CommitDialog(getProject(), settings, template, panel);

        // 验证AI选项卡不可用
        assertFalse("AI选项卡应该不可用", dialog.isAITabAvailable());
        assertEquals("应该只有一个选项卡", 1, ((JTabbedPane) dialog.getContentPane()).getTabCount());
    }

    @Test
    public void testDefaultTabSelection() {
        // 测试默认选项卡选择
        GitCommitMessageHelperSettings settings = new GitCommitMessageHelperSettings();
        AISettings aiSettings = new AISettings();
        aiSettings.setEnabled(true);
        aiSettings.setApiKey("test-api-key");
        aiSettings.setApiEndpoint("https://api.test.com");
        settings.setAISettings(aiSettings);

        CommitTemplate template = new CommitTemplate();
        CheckinProjectPanel panel = new EmptyCheckInProjectPanel();
        CommitDialog dialog = new CommitDialog(getProject(), settings, template, panel);

        // 验证默认选中AI选项卡
        assertEquals("应该默认选中AI选项卡", 1, dialog.getSelectedTabIndex());
    }

    @Test
    public void testManualTabSelectionWhenAIDisabled() {
        // 测试AI禁用时默认选中手动选项卡
        GitCommitMessageHelperSettings settings = new GitCommitMessageHelperSettings();
        AISettings aiSettings = new AISettings();
        aiSettings.setEnabled(false);
        settings.setAISettings(aiSettings);

        CommitTemplate template = new CommitTemplate();
        CheckinProjectPanel panel = new EmptyCheckInProjectPanel();
        CommitDialog dialog = new CommitDialog(getProject(), settings, template, panel);

        // 验证默认选中手动选项卡
        assertEquals("应该默认选中手动选项卡", 0, dialog.getSelectedTabIndex());
    }

    @Test
    public void testTabSwitching() {
        // 测试选项卡切换功能
        GitCommitMessageHelperSettings settings = new GitCommitMessageHelperSettings();
        AISettings aiSettings = new AISettings();
        aiSettings.setEnabled(true);
        aiSettings.setApiKey("test-api-key");
        aiSettings.setApiEndpoint("https://api.test.com");
        settings.setAISettings(aiSettings);

        CommitTemplate template = new CommitTemplate();
        CheckinProjectPanel panel = new EmptyCheckInProjectPanel();
        CommitDialog dialog = new CommitDialog(getProject(), settings, template, panel);

        // 测试切换到手动选项卡
        dialog.switchToManualTab();
        assertEquals("应该切换到手动选项卡", 0, dialog.getSelectedTabIndex());

        // 测试切换到AI选项卡
        dialog.switchToAITab();
        assertEquals("应该切换到AI选项卡", 1, dialog.getSelectedTabIndex());
    }
}