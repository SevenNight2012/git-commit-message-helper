package com.fulinlin.ui;

import com.fulinlin.model.CodeChangeInfo;
import com.fulinlin.model.FileChange;
import com.fulinlin.storage.GitCommitMessageHelperSettings;
import com.fulinlin.utils.CodeChangeAnalyzer;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 用于测试CodeChangeAnalyzer的简单UI
 */
public class CodeChangeAnalyzerDialog extends JFrame {

    private final GitCommitMessageHelperSettings settings;

    private JButton analyzeButton;
    private JList<String> fileList;
    private JTextArea diffTextArea;
    private DefaultListModel<String> fileListModel;
    private CodeChangeInfo lastChangeInfo;

    public CodeChangeAnalyzerDialog(Project project) {
        this.settings = ServiceManager.getService(GitCommitMessageHelperSettings.class);

        setTitle("CodeChangeAnalyzer 测试UI");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        analyzeButton = new JButton("分析变更");
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        diffTextArea = new JTextArea();
        diffTextArea.setEditable(false);
        diffTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(fileList), new JScrollPane(diffTextArea));
        splitPane.setDividerLocation(200);

        add(analyzeButton, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // 按钮点击事件
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyzeChanges(project);
            }
        });

        // 文件列表点击事件
        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && lastChangeInfo != null) {
                int idx = fileList.getSelectedIndex();
                if (idx >= 0 && idx < lastChangeInfo.getChangedFiles().size()) {
                    FileChange fc = lastChangeInfo.getChangedFiles().get(idx);
                    diffTextArea.setText(fc.getDiffContent());
                }
            }
        });
    }

    protected void analyzeChanges(Project project) {
        CodeChangeAnalyzer analyzer = new CodeChangeAnalyzer(null, this.settings.getAISettings());
        CodeChangeInfo changeInfo = analyzer.analyzeChanges(project);
        this.lastChangeInfo = changeInfo;
        fileListModel.clear();
        List<FileChange> files = changeInfo.getChangedFiles();
        for (FileChange fc : files) {
            fileListModel.addElement(fc.getFilePath() + " [" + fc.getChangeType() + "]");
        }
        diffTextArea.setText(changeInfo.getDiffContent());
    }
}