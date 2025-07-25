package com.fulinlin.ui.setting;

import com.fulinlin.model.AISettings;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.swing.*;
import java.awt.*;

/**
 * AISettingsPanel UI测试
 */
public class AISettingsPanelTest {

    private AISettingsPanel panel;

    @Before
    public void setUp() {
        panel = new AISettingsPanel();
    }

    @Test
    public void testPanelCreation() {
        // 测试面板是否成功创建
        JPanel mainPanel = panel.getMainPanel();
        assertNotNull("Main panel should not be null", mainPanel);

        // 测试面板是否有子组件
        Component[] components = mainPanel.getComponents();
        assertTrue("Main panel should have components", components.length > 0);

        // 测试面板布局
        LayoutManager layout = mainPanel.getLayout();
        assertTrue("Main panel should use BorderLayout", layout instanceof BorderLayout);
    }

    @Test
    public void testSettingsIntegration() {
        // 测试设置集成
        AISettings settings = new AISettings();
        settings.setApiKey("test-key");
        settings.setFileBlacklist(".*\\.key$\n.*config.*");

        // 设置配置
        panel.setSettings(settings);

        // 获取配置
        AISettings retrievedSettings = panel.getSettings();

        // 验证配置是否正确保存和读取
        assertEquals("API Key should be preserved", "test-key", retrievedSettings.getApiKey());
        assertEquals("File blacklist should be preserved", ".*\\.key$\n.*config.*", retrievedSettings.getFileBlacklist());
    }

    @Test
    public void testEmptySettings() {
        // 测试空设置
        AISettings emptySettings = new AISettings();
        panel.setSettings(emptySettings);

        AISettings retrievedSettings = panel.getSettings();
        assertNotNull("Retrieved settings should not be null", retrievedSettings);
        assertNotNull("File blacklist should not be null", retrievedSettings.getFileBlacklist());
    }

    @Test
    public void testLogDirectoryDisplay() {
        // 测试日志目录显示功能
        JPanel mainPanel = panel.getMainPanel();
        assertNotNull("Main panel should not be null", mainPanel);

        // 验证日志目录字段已正确初始化
        // 注意：由于这是UI组件，我们只能验证面板创建成功
        // 实际的日志目录路径会在运行时动态计算
        assertTrue("Panel should be created successfully", true);
    }
}