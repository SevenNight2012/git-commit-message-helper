package com.fulinlin.configurable;

import com.fulinlin.storage.GitCommitMessageHelperSettings;
import com.fulinlin.ui.central.CentralSettingPanel;
import com.fulinlin.ui.setting.AISettingsPanel;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CentralSettingConfigurable implements SearchableConfigurable {

    private CentralSettingPanel centralSettingPanel;
    private AISettingsPanel aiSettingsPanel;
    private JTabbedPane tabbedPane;

    private GitCommitMessageHelperSettings settings;

    public CentralSettingConfigurable() {
        settings = ServiceManager.getService(GitCommitMessageHelperSettings.class);
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "plugins.gitcommitmessagehelper";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (tabbedPane == null) {
            tabbedPane = new JTabbedPane();
            if (centralSettingPanel == null) {
                centralSettingPanel = new CentralSettingPanel(settings);
            }
            tabbedPane.addTab("General", centralSettingPanel.getMainPanel());
            if (aiSettingsPanel == null) {
                aiSettingsPanel = new AISettingsPanel();
                aiSettingsPanel.setSettings(settings.getAISettings());
            }
            tabbedPane.addTab("AI Settings", aiSettingsPanel.getMainPanel());
        }
        return tabbedPane;
    }

    @Override
    public void reset() {
        centralSettingPanel.reset(settings);
        if (aiSettingsPanel != null) {
            aiSettingsPanel.setSettings(settings.getAISettings());
        }
    }

    @Override
    public boolean isModified() {
        boolean modified = centralSettingPanel.isModified(settings);
        if (aiSettingsPanel != null) {
            modified = modified || !aiSettingsPanel.getSettings().equals(settings.getAISettings());
        }
        return modified;
    }

    @Override
    public void apply() {
        settings.setCentralSettings(centralSettingPanel.getSettings().getCentralSettings());
        if (aiSettingsPanel != null) {
            settings.setAISettings(aiSettingsPanel.getSettings());
        }
        settings = centralSettingPanel.getSettings().clone();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "GitCommitMessageHelper";
    }
}
