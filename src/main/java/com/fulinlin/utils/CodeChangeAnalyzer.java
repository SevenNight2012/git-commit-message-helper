package com.fulinlin.utils;

import com.fulinlin.model.ChangeType;
import com.fulinlin.model.CodeChangeInfo;
import com.fulinlin.model.FileChange;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 代码变更分析器
 */
public class CodeChangeAnalyzer {

    private final Collection<Change> mSelectedChanges;
    private FileBlacklistFilter fileBlacklistFilter;

    public CodeChangeAnalyzer() {
        this(null);
    }

    public CodeChangeAnalyzer(@Nullable CheckinProjectPanel gitPanel) {
        this(gitPanel, null);
    }

    public CodeChangeAnalyzer(@Nullable CheckinProjectPanel gitPanel, @Nullable String fileBlacklistConfig) {
        mSelectedChanges = null == gitPanel ? new ArrayList<>() : gitPanel.getSelectedChanges();

        // 初始化文件黑名单过滤器
        fileBlacklistFilter = new FileBlacklistFilter();
        if (fileBlacklistConfig != null) {
            fileBlacklistFilter.initializeFromConfig(fileBlacklistConfig);
        }
    }

    /**
     * 设置文件黑名单配置
     * @param fileBlacklistConfig 文件黑名单配置字符串
     */
    public void setFileBlacklistConfig(String fileBlacklistConfig) {
        if (fileBlacklistFilter == null) {
            fileBlacklistFilter = new FileBlacklistFilter();
        }
        fileBlacklistFilter.initializeFromConfig(fileBlacklistConfig);
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
                    // 应用文件黑名单过滤
                    if (fileBlacklistFilter != null && fileBlacklistFilter.isFileBlacklisted(fileChange.getFilePath())) {
                        // 跳过黑名单中的文件
                        continue;
                    }

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

        } catch (Exception e) {
            // 错误处理：返回空的变更信息
            changeInfo.setChangedFiles(new ArrayList<>());
            changeInfo.setDiffContent("");
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

    // 支持的文本文件扩展名
    private static final String[] TEXT_FILE_EXTENSIONS = {".java", ".kt", ".xml", ".groovy", ".md", ".txt", ".properties"};

    // 判断是否为文本文件
    private boolean isTextFile(String filePath) {
        String lower = filePath.toLowerCase();
        for (String ext : TEXT_FILE_EXTENSIONS) {
            if (lower.endsWith(ext)) return true;
        }
        return false;
    }

    private String getDiffContent(Change change) {
        try {
            String filePath = getFilePath(change);
            if (filePath == null) return "";

            // 判断是否为文本文件
            if (!isTextFile(filePath)) {
                // 二进制文件或不支持的类型，仅返回变更类型和文件名
                return String.format("[Binary or unsupported file] %s", filePath);
            }

            ContentRevision beforeRevision = change.getBeforeRevision();
            ContentRevision afterRevision = change.getAfterRevision();

            String beforeContent = beforeRevision != null ? safeGetContent(beforeRevision) : "";
            String afterContent = afterRevision != null ? safeGetContent(afterRevision) : "";

            // 生成unified diff，仅输出变更内容
            if (!beforeContent.equals(afterContent)) {
                // 按行分割
                List<String> original = Arrays.asList(beforeContent.split("\\r?\\n"));
                List<String> revised = Arrays.asList(afterContent.split("\\r?\\n"));
                Patch<String> patch = DiffUtils.diff(original, revised);
                List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                        filePath, filePath, original, patch, 3);
                return String.join("\n", unifiedDiff);
            }
            return "";
        } catch (Exception e) {
            return "[Diff error] " + e.getMessage();
        }
    }

    // 安全获取内容，避免null
    private String safeGetContent(ContentRevision revision) {
        try {
            String content = revision.getContent();
            return content == null ? "" : content;
        } catch (Exception e) {
            return "";
        }
    }
}