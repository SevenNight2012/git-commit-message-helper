package com.fulinlin.ui.setting;

import com.fulinlin.model.AISettings;
import com.fulinlin.utils.DeepSeekAPIClient;
import com.fulinlin.utils.IDENotificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class AISettingsPanel {
    private JPanel mainPanel;
    private JTextField apiKeyField;
    private JTextField endpointField;
    private JComboBox<String> modelComboBox;
    private JSpinner maxTokensSpinner;
    private JSpinner temperatureSpinner;
    private JCheckBox enabledCheckBox;
    private JCheckBox autoGenerateCheckBox;
    private JButton testConnectionButton;
    private Project project;

    public AISettingsPanel() {
        this(null);
    }

    public AISettingsPanel(Project project) {
        this.project = project;
        initComponents();
        setupEventHandlers();
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // API配置面板
        JPanel apiPanel = createAPIPanel();

        // 生成配置面板
        JPanel generationPanel = createGenerationPanel();

        // 按钮面板
        JPanel buttonPanel = createButtonPanel();

        // 组装主面板
        mainPanel.add(apiPanel, BorderLayout.NORTH);
        mainPanel.add(generationPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createAPIPanel() {
        JPanel apiPanel = new JPanel(new GridBagLayout());
        apiPanel.setBorder(BorderFactory.createTitledBorder("API Configuration"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // API Key
        gbc.gridx = 0;
        gbc.gridy = 0;
        apiPanel.add(new JLabel("API Key:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        apiKeyField = new JPasswordField(40);
        apiPanel.add(apiKeyField, gbc);

        // Endpoint
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        apiPanel.add(new JLabel("Endpoint:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        endpointField = new JTextField("https://api.deepseek.com/v1/chat/completions", 40);
        apiPanel.add(endpointField, gbc);

        // Model
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        apiPanel.add(new JLabel("Model:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        modelComboBox = new ComboBox<>(new String[]{"deepseek-chat", "deepseek-reasoner"});
        apiPanel.add(modelComboBox, gbc);

        return apiPanel;
    }

    private JPanel createGenerationPanel() {
        JPanel generationPanel = new JPanel(new GridBagLayout());
        generationPanel.setBorder(BorderFactory.createTitledBorder("Generation Settings"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Max Tokens
        gbc.gridx = 0;
        gbc.gridy = 0;
        generationPanel.add(new JLabel("Max Tokens:"), gbc);

        gbc.gridx = 1;
        maxTokensSpinner = new JSpinner(new SpinnerNumberModel(500, 100, 2000, 50));
        generationPanel.add(maxTokensSpinner, gbc);

        // Temperature
        gbc.gridx = 0;
        gbc.gridy = 1;
        generationPanel.add(new JLabel("Temperature:"), gbc);

        gbc.gridx = 1;
        temperatureSpinner = new JSpinner(new SpinnerNumberModel(0.7, 0.0, 2.0, 0.1));
        generationPanel.add(temperatureSpinner, gbc);

        // Enable AI
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        enabledCheckBox = new JCheckBox("Enable AI Generation");
        generationPanel.add(enabledCheckBox, gbc);

        // Auto Generate
        gbc.gridx = 0;
        gbc.gridy = 3;
        autoGenerateCheckBox = new JCheckBox("Auto-generate on commit dialog open");
        generationPanel.add(autoGenerateCheckBox, gbc);

        return generationPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        testConnectionButton = new JButton("Test Connection");
        buttonPanel.add(testConnectionButton);
        return buttonPanel;
    }

    private void setupEventHandlers() {
        testConnectionButton.addActionListener(e -> testConnection());
        enabledCheckBox.addActionListener(e -> updateUIState());
    }

    private void updateUIState() {
        boolean enabled = enabledCheckBox.isSelected();
        apiKeyField.setEnabled(enabled);
        endpointField.setEnabled(enabled);
        modelComboBox.setEnabled(enabled);
        maxTokensSpinner.setEnabled(enabled);
        temperatureSpinner.setEnabled(enabled);
        autoGenerateCheckBox.setEnabled(enabled);
        testConnectionButton.setEnabled(enabled);
    }

    private void testConnection() {
        // 尝试获取当前活动的项目
        final Project currentProject = getCurrentProject();

        if (currentProject == null) {
            // 如果没有项目，显示警告但继续测试
            int result = Messages.showYesNoDialog(
                    "No active project found. Test connection without project context?",
                    "Test Connection Warning",
                    Messages.getQuestionIcon()
            );
            if (result != Messages.YES) {
                return;
            }
        }

        // 禁用按钮，显示加载状态
        testConnectionButton.setEnabled(false);
        testConnectionButton.setText("Testing...");

        AISettings testSettings = getSettings();
        DeepSeekAPIClient client = new DeepSeekAPIClient(testSettings);

        // 异步测试连接
        client.testConnection().thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
                testConnectionButton.setEnabled(true);
                testConnectionButton.setText("Test Connection");

                if (success) {
                    if (currentProject != null && currentProject.isDisposed() == false) {
                        IDENotificationUtil.notifyInfo(currentProject, "AI Connection Test", "Connection successful! AI service is ready to use.");
                    } else {
                        Messages.showInfoMessage("Connection successful! AI service is ready to use.", "AI Connection Test");
                    }
                } else {
                    if (currentProject != null && currentProject.isDisposed() == false) {
                        IDENotificationUtil.notifyError(currentProject, "AI Connection Test", "Connection failed. Please check your API key and endpoint.");
                    } else {
                        Messages.showErrorDialog("Connection failed. Please check your API key and endpoint.", "AI Connection Test");
                    }
                }
            });
        }).exceptionally(error -> {
            SwingUtilities.invokeLater(() -> {
                testConnectionButton.setEnabled(true);
                testConnectionButton.setText("Test Connection");
                if (currentProject != null && currentProject.isDisposed() == false) {
                    IDENotificationUtil.notifyError(currentProject, "AI Connection Test", "Connection failed: " + error.getMessage());
                } else {
                    Messages.showErrorDialog("Connection failed: " + error.getMessage(), "AI Connection Test");
                }
            });
            return null;
        });
    }

    private Project getCurrentProject() {
        try {
            ProjectManager projectManager = ProjectManager.getInstance();
            Project[] openProjects = projectManager.getOpenProjects();
            if (openProjects.length > 0) {
                return openProjects[0];
            }
        } catch (Exception e) {
            // 忽略获取项目时的异常
        }
        return null;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setSettings(AISettings settings) {
        apiKeyField.setText(settings.getApiKey() != null ? settings.getApiKey() : "");
        endpointField.setText(settings.getApiEndpoint());
        modelComboBox.setSelectedItem(settings.getModel());
        maxTokensSpinner.setValue(settings.getMaxTokens());
        temperatureSpinner.setValue(settings.getTemperature());
        enabledCheckBox.setSelected(settings.isEnabled());
        autoGenerateCheckBox.setSelected(settings.isAutoGenerate());
        updateUIState();
    }

    public AISettings getSettings() {
        AISettings settings = new AISettings();
        settings.setApiKey(apiKeyField.getText());
        settings.setApiEndpoint(endpointField.getText());
        settings.setModel((String) modelComboBox.getSelectedItem());
        settings.setMaxTokens((Integer) maxTokensSpinner.getValue());
        settings.setTemperature((Double) temperatureSpinner.getValue());
        settings.setEnabled(enabledCheckBox.isSelected());
        settings.setAutoGenerate(autoGenerateCheckBox.isSelected());
        return settings;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}