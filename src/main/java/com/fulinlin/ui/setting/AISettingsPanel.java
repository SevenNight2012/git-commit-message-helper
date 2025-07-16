package com.fulinlin.ui.setting;

import com.fulinlin.model.AISettings;
import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.*;

public class AISettingsPanel {
    private JPanel mainPanel;
    private JTextField apiKeyField;
    private JTextField endpointField;
    private JComboBox<String> modelComboBox;
    private JSpinner maxTokensSpinner;
    private JSpinner temperatureSpinner;
    private JCheckBox enabledCheckBox;
    private JCheckBox autoGenerateCheckBox;

    public AISettingsPanel() {
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // API Key
        mainPanel.add(new JLabel("API Key:"), gbc);
        gbc.gridx = 1;
        apiKeyField = new JTextField(30);
        mainPanel.add(apiKeyField, gbc);

        // Endpoint
        gbc.gridx = 0;
        gbc.gridy++;
        mainPanel.add(new JLabel("Endpoint:"), gbc);
        gbc.gridx = 1;
        endpointField = new JTextField("https://api.deepseek.com/v1/chat/completions", 30);
        mainPanel.add(endpointField, gbc);

        // Model
        gbc.gridx = 0;
        gbc.gridy++;
        mainPanel.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1;
        modelComboBox = new ComboBox<>(new String[]{"deepseek-chat"});
        mainPanel.add(modelComboBox, gbc);

        // Max Tokens
        gbc.gridx = 0;
        gbc.gridy++;
        mainPanel.add(new JLabel("Max Tokens:"), gbc);
        gbc.gridx = 1;
        maxTokensSpinner = new JSpinner(new SpinnerNumberModel(500, 100, 2000, 50));
        mainPanel.add(maxTokensSpinner, gbc);

        // Temperature
        gbc.gridx = 0;
        gbc.gridy++;
        mainPanel.add(new JLabel("Temperature:"), gbc);
        gbc.gridx = 1;
        temperatureSpinner = new JSpinner(new SpinnerNumberModel(0.7, 0.0, 2.0, 0.1));
        mainPanel.add(temperatureSpinner, gbc);

        // Enable AI
        gbc.gridx = 0;
        gbc.gridy++;
        enabledCheckBox = new JCheckBox("Enable AI Generation");
        mainPanel.add(enabledCheckBox, gbc);

        // Auto Generate
        gbc.gridx = 1;
        autoGenerateCheckBox = new JCheckBox("Auto-generate on commit dialog open");
        mainPanel.add(autoGenerateCheckBox, gbc);
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
}