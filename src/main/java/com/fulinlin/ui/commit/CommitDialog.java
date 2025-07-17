package com.fulinlin.ui.commit;

import com.fulinlin.localization.PluginBundle;
import com.fulinlin.model.CommitTemplate;
import com.fulinlin.storage.GitCommitMessageHelperSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CommitDialog extends DialogWrapper {

    private final CommitPanel commitPanel;
    private final AIGeneratePanel aiGeneratePanel;
    private final JTabbedPane tabbedPane;
    private final GitCommitMessageHelperSettings settings;
    private final Project project;

    public CommitDialog(@Nullable Project project, GitCommitMessageHelperSettings settings, CommitTemplate commitMessageTemplate) {
        super(project);
        this.project = project;
        this.settings = settings;

        // Initialize panels
        commitPanel = new CommitPanel(project, settings, commitMessageTemplate);
        aiGeneratePanel = new AIGeneratePanel(project, settings.getAISettings());

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manual Build", commitPanel.getMainPanel());
        tabbedPane.addTab("AI Generate", aiGeneratePanel.getMainPanel());

        // Set default tab based on AI availability
        if (settings.getAISettings() != null && settings.getAISettings().isEnabled()) {
            tabbedPane.setSelectedIndex(1); // AI Generate tab
        } else {
            tabbedPane.setSelectedIndex(0); // Manual Build tab
        }

        setTitle(PluginBundle.get("commit.panel.title"));
        setOKButtonText(PluginBundle.get("commit.panel.ok.button"));
        setCancelButtonText(PluginBundle.get("commit.panel.cancel.button"));
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return tabbedPane;
    }

    public CommitMessage getCommitMessage(GitCommitMessageHelperSettings settings) {
        // Check which tab is currently selected
        int selectedIndex = tabbedPane.getSelectedIndex();

        if (selectedIndex == 1) { // AI Generate tab
            // Return AI generated content as a simple commit message
            String aiContent = aiGeneratePanel.getAIContent();
            if (!aiContent.isEmpty()) {
                // Create a simple commit message from AI content
                return new CommitMessage(
                        settings,
                        new com.fulinlin.model.TypeAlias("feat", ""), // Default type for AI generated
                        "", // No scope
                        aiContent, // Use AI content as subject
                        "", // No body
                        "", // No closed issues
                        "", // No breaking changes
                        "" // No skip CI
                );
            }
        }

        // Manual build tab or fallback
        return commitPanel.getCommitMessage(settings);
    }

    public CommitTemplate getCommitMessageTemplate() {
        // Check which tab is currently selected
        int selectedIndex = tabbedPane.getSelectedIndex();

        if (selectedIndex == 1) { // AI Generate tab
            // Return AI content as template
            CommitTemplate commitTemplate = new CommitTemplate();
            String aiContent = aiGeneratePanel.getAIContent();
            if (!aiContent.isEmpty()) {
                commitTemplate.setSubject(aiContent);
                // Try to parse AI content for other fields if possible
                parseAIContentToTemplate(aiContent, commitTemplate);
            }
            return commitTemplate;
        }

        // Manual build tab or fallback
        return commitPanel.getCommitMessageTemplate();
    }

    private void parseAIContentToTemplate(String aiContent, CommitTemplate template) {
        // Simple parsing of AI content to extract structured information
        String[] lines = aiContent.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            // Try to parse conventional commit format: type(scope): subject
            if (firstLine.matches("^[a-zA-Z]+\\([^)]+\\):\\s+.+$")) {
                // Has scope
                String[] parts = firstLine.split(":", 2);
                if (parts.length == 2) {
                    String typeScope = parts[0].trim();
                    String subject = parts[1].trim();

                    int scopeStart = typeScope.indexOf("(");
                    int scopeEnd = typeScope.indexOf(")");
                    if (scopeStart > 0 && scopeEnd > scopeStart) {
                        String type = typeScope.substring(0, scopeStart);
                        String scope = typeScope.substring(scopeStart + 1, scopeEnd);
                        template.setType(type);
                        template.setScope(scope);
                        template.setSubject(subject);
                    }
                }
            } else if (firstLine.matches("^[a-zA-Z]+:\\s+.+$")) {
                // No scope
                String[] parts = firstLine.split(":", 2);
                if (parts.length == 2) {
                    String type = parts[0].trim();
                    String subject = parts[1].trim();
                    template.setType(type);
                    template.setSubject(subject);
                }
            } else {
                // Just use the first line as subject
                template.setSubject(firstLine);
            }

            // Parse body and other sections
            StringBuilder body = new StringBuilder();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.startsWith("BREAKING CHANGE:")) {
                    template.setChanges(line.substring("BREAKING CHANGE:".length()).trim());
                } else if (line.startsWith("Closes:")) {
                    template.setCloses(line.substring("Closes:".length()).trim());
                } else if (!line.isEmpty()) {
                    if (body.length() > 0) {
                        body.append("\n");
                    }
                    body.append(line);
                }
            }
            if (body.length() > 0) {
                template.setBody(body.toString());
            }
        }
    }
}