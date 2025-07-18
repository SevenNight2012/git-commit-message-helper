package com.fulinlin.utils;

import com.fulinlin.model.ChangeType;
import com.fulinlin.model.CodeChangeInfo;
import com.fulinlin.model.FileChange;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 代码变更分析器
 */
public class CodeChangeAnalyzer {

    private final Collection<Change> mSelectedChanges;

    public CodeChangeAnalyzer() {
        this(null);
    }

    public CodeChangeAnalyzer(@Nullable CheckinProjectPanel gitPanel) {
        mSelectedChanges = null == gitPanel ? new ArrayList<>() : gitPanel.getSelectedChanges();
    }

    /**
     * 分析变更的代码
     * @param project project对象
     * @return 封装的变更信息
     */
    public CodeChangeInfo analyzeChanges(Project project) {
        CodeChangeInfo changeInfo = new CodeChangeInfo();

        try {
            // 使用ChangeListManager获取变更
            ChangeListManager changeListManager = ChangeListManager.getInstance(project);
            Collection<Change> changes;
            if (null != mSelectedChanges && !mSelectedChanges.isEmpty()) {
                changes = mSelectedChanges;
            } else {
                changes = changeListManager.getAllChanges();
            }

            List<FileChange> fileChanges = new ArrayList<>();
            StringBuilder diffContentBuilder = new StringBuilder();

            // 遍历所有变更
            for (Change change : changes) {
                FileChange fileChange = analyzeChange(change);
                if (fileChange != null) {
                    fileChanges.add(fileChange);

                    // 累积diff内容
                    if (fileChange.getDiffContent() != null) {
                        diffContentBuilder.append("File: ").append(fileChange.getFilePath()).append("\n");
                        diffContentBuilder.append(fileChange.getDiffContent()).append("\n\n");
                    }
                }
            }

            // 设置变更信息
            changeInfo.setChangedFiles(fileChanges);
            changeInfo.setDiffContent(diffContentBuilder.toString());

            // 分析整体变更类型
            ChangeType overallChangeType = analyzeOverallChangeType(fileChanges);
            changeInfo.setChangeType(overallChangeType);

            // 提取scope（基于变更文件路径）
            String scope = extractScope(fileChanges);
            changeInfo.setScope(scope);

        } catch (Exception e) {
            // 错误处理：返回空的变更信息
            changeInfo.setChangedFiles(new ArrayList<>());
            changeInfo.setDiffContent("");
            changeInfo.setChangeType(ChangeType.OTHER);
        }

        return changeInfo;
    }

    private FileChange analyzeChange(Change change) {
        try {
            String filePath = getFilePath(change);
            if (filePath == null) {
                return null;
            }

            // 映射变更类型
            ChangeType changeType = mapChangeType(change.getType());

            // 获取diff内容
            String diffContent = getDiffContent(change);

            return new FileChange(filePath, changeType, diffContent);

        } catch (Exception e) {
            return null;
        }
    }

    private String getFilePath(Change change) {
        VirtualFile file = change.getVirtualFile();
        if (file != null) {
            return file.getPath();
        }

        // 尝试从ContentRevision获取路径
        ContentRevision beforeRevision = change.getBeforeRevision();
        if (beforeRevision != null) {
            return beforeRevision.getFile().getPath();
        }

        ContentRevision afterRevision = change.getAfterRevision();
        if (afterRevision != null) {
            return afterRevision.getFile().getPath();
        }

        return null;
    }

    private ChangeType mapChangeType(Change.Type changeType) {
        switch (changeType) {
            case NEW:
                return ChangeType.FEATURE;
            case MODIFICATION:
                return ChangeType.FIX;
            case DELETED:
                return ChangeType.CHORE;
            case MOVED:
                return ChangeType.REFACTOR;
            default:
                return ChangeType.OTHER;
        }
    }

    private String getDiffContent(Change change) {
        try {
            ContentRevision beforeRevision = change.getBeforeRevision();
            ContentRevision afterRevision = change.getAfterRevision();

            if (beforeRevision == null && afterRevision == null) {
                return "";
            }

            String beforeContent = beforeRevision != null ? beforeRevision.getContent() : "";
            String afterContent = afterRevision != null ? afterRevision.getContent() : "";

            // 简单的diff内容生成（实际项目中可能需要更复杂的diff算法）
            if (!beforeContent.equals(afterContent)) {
                return "--- Before\n" + beforeContent + "\n+++ After\n" + afterContent;
            }

            return "";

        } catch (Exception e) {
            return "";
        }
    }

    private ChangeType analyzeOverallChangeType(List<FileChange> fileChanges) {
        if (fileChanges.isEmpty()) {
            return ChangeType.OTHER;
        }

        // 统计各类型变更数量
        long featureCount = fileChanges.stream()
            .filter(fc -> fc.getChangeType() == ChangeType.FEATURE)
            .count();
        long fixCount = fileChanges.stream()
            .filter(fc -> fc.getChangeType() == ChangeType.FIX)
            .count();
        long refactorCount = fileChanges.stream()
            .filter(fc -> fc.getChangeType() == ChangeType.REFACTOR)
            .count();

        // 根据主要变更类型确定整体类型
        if (featureCount > 0) {
            return ChangeType.FEATURE;
        } else if (fixCount > 0) {
            return ChangeType.FIX;
        } else if (refactorCount > 0) {
            return ChangeType.REFACTOR;
        } else {
            return ChangeType.OTHER;
        }
    }

    private String extractScope(List<FileChange> fileChanges) {
        if (fileChanges.isEmpty()) {
            return "";
        }

        // 基于文件路径提取scope
        // 例如：src/main/java/com/example/core/ -> core
        String firstFilePath = fileChanges.get(0).getFilePath();

        // 简单的scope提取逻辑
        if (firstFilePath.contains("/")) {
            String[] parts = firstFilePath.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].equals("src") && i + 1 < parts.length) {
                    return parts[i + 1]; // 返回src后的第一个目录
                }
            }
        }

        return "";
    }
}