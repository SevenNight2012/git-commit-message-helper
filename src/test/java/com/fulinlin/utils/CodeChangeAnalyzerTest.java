package com.fulinlin.utils;

import com.fulinlin.model.ChangeType;
import com.fulinlin.model.CodeChangeInfo;
import com.fulinlin.model.FileChange;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CodeChangeAnalyzerTest {

    @Mock
    private Project project;

    @Mock
    private ChangeListManager changeListManager;

    @Mock
    private Change change;

    @Mock
    private VirtualFile virtualFile;

    @Mock
    private ContentRevision beforeRevision;

    @Mock
    private ContentRevision afterRevision;

    private CodeChangeAnalyzer analyzer;

    @Before
    public void setUp() {
        analyzer = new CodeChangeAnalyzer();

        // Mock ChangeListManager
        when(ChangeListManager.getInstance(project)).thenReturn(changeListManager);
    }

    @Test
    public void testAnalyzeChanges_EmptyChanges() {
        // Given
        Collection<Change> changes = new ArrayList<>();
        when(changeListManager.getAllChanges()).thenReturn(changes);

        // When
        CodeChangeInfo result = analyzer.analyzeChanges(project);

        // Then
        assertNotNull(result);
        assertTrue(result.getChangedFiles().isEmpty());
        assertEquals("", result.getDiffContent());
        assertEquals(ChangeType.OTHER, result.getChangeType());
        assertEquals("", result.getScope());
    }

    @Test
    public void testAnalyzeChanges_NewFile() {
        // Given
        Collection<Change> changes = new ArrayList<>();
        changes.add(change);

        when(changeListManager.getAllChanges()).thenReturn(changes);
        when(change.getType()).thenReturn(Change.Type.NEW);
        when(change.getVirtualFile()).thenReturn(virtualFile);
        when(virtualFile.getPath()).thenReturn("src/main/java/com/example/NewClass.java");
        when(change.getBeforeRevision()).thenReturn(null);
        when(change.getAfterRevision()).thenReturn(afterRevision);
        when(afterRevision.getContent()).thenReturn("public class NewClass {}");

        // When
        CodeChangeInfo result = analyzer.analyzeChanges(project);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getChangedFiles().size());
        assertEquals(ChangeType.FEATURE, result.getChangeType());

        FileChange fileChange = result.getChangedFiles().get(0);
        assertEquals("src/main/java/com/example/NewClass.java", fileChange.getFilePath());
        assertEquals(ChangeType.FEATURE, fileChange.getChangeType());
    }

    @Test
    public void testAnalyzeChanges_ModifiedFile() {
        // Given
        Collection<Change> changes = new ArrayList<>();
        changes.add(change);

        when(changeListManager.getAllChanges()).thenReturn(changes);
        when(change.getType()).thenReturn(Change.Type.MODIFICATION);
        when(change.getVirtualFile()).thenReturn(virtualFile);
        when(virtualFile.getPath()).thenReturn("src/main/java/com/example/ExistingClass.java");
        when(change.getBeforeRevision()).thenReturn(beforeRevision);
        when(change.getAfterRevision()).thenReturn(afterRevision);
        when(beforeRevision.getContent()).thenReturn("public class ExistingClass {}");
        when(afterRevision.getContent()).thenReturn("public class ExistingClass { private String field; }");

        // When
        CodeChangeInfo result = analyzer.analyzeChanges(project);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getChangedFiles().size());
        assertEquals(ChangeType.FIX, result.getChangeType());

        FileChange fileChange = result.getChangedFiles().get(0);
        assertEquals("src/main/java/com/example/ExistingClass.java", fileChange.getFilePath());
        assertEquals(ChangeType.FIX, fileChange.getChangeType());
        assertTrue(fileChange.getDiffContent().contains("--- Before"));
        assertTrue(fileChange.getDiffContent().contains("+++ After"));
    }

    @Test
    public void testAnalyzeChanges_DeletedFile() {
        // Given
        Collection<Change> changes = new ArrayList<>();
        changes.add(change);

        when(changeListManager.getAllChanges()).thenReturn(changes);
        when(change.getType()).thenReturn(Change.Type.DELETED);
        when(change.getVirtualFile()).thenReturn(virtualFile);
        when(virtualFile.getPath()).thenReturn("src/main/java/com/example/OldClass.java");
        when(change.getBeforeRevision()).thenReturn(beforeRevision);
        when(change.getAfterRevision()).thenReturn(null);
        when(beforeRevision.getContent()).thenReturn("public class OldClass {}");

        // When
        CodeChangeInfo result = analyzer.analyzeChanges(project);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getChangedFiles().size());
        assertEquals(ChangeType.CHORE, result.getChangeType());

        FileChange fileChange = result.getChangedFiles().get(0);
        assertEquals("src/main/java/com/example/OldClass.java", fileChange.getFilePath());
        assertEquals(ChangeType.CHORE, fileChange.getChangeType());
    }

    @Test
    public void testAnalyzeChanges_MultipleFiles() {
        // Given
        Collection<Change> changes = new ArrayList<>();

        Change newChange = mock(Change.class);
        Change modifiedChange = mock(Change.class);
        VirtualFile newFile = mock(VirtualFile.class);
        VirtualFile modifiedFile = mock(VirtualFile.class);

        changes.add(newChange);
        changes.add(modifiedChange);

        when(changeListManager.getAllChanges()).thenReturn(changes);

        // New file
        when(newChange.getType()).thenReturn(Change.Type.NEW);
        when(newChange.getVirtualFile()).thenReturn(newFile);
        when(newFile.getPath()).thenReturn("src/main/java/com/example/NewClass.java");
        when(newChange.getBeforeRevision()).thenReturn(null);
        when(newChange.getAfterRevision()).thenReturn(afterRevision);
        when(afterRevision.getContent()).thenReturn("public class NewClass {}");

        // Modified file
        when(modifiedChange.getType()).thenReturn(Change.Type.MODIFICATION);
        when(modifiedChange.getVirtualFile()).thenReturn(modifiedFile);
        when(modifiedFile.getPath()).thenReturn("src/main/java/com/example/ExistingClass.java");
        when(modifiedChange.getBeforeRevision()).thenReturn(beforeRevision);
        when(modifiedChange.getAfterRevision()).thenReturn(afterRevision);
        when(beforeRevision.getContent()).thenReturn("public class ExistingClass {}");
        when(afterRevision.getContent()).thenReturn("public class ExistingClass { private String field; }");

        // When
        CodeChangeInfo result = analyzer.analyzeChanges(project);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getChangedFiles().size());
        assertEquals(ChangeType.FEATURE, result.getChangeType()); // 优先显示FEATURE类型
        assertEquals("main", result.getScope()); // 从路径提取scope
    }

    @Test
    public void testAnalyzeChanges_ExceptionHandling() {
        // Given
        when(changeListManager.getAllChanges()).thenThrow(new RuntimeException("Test exception"));

        // When
        CodeChangeInfo result = analyzer.analyzeChanges(project);

        // Then
        assertNotNull(result);
        assertTrue(result.getChangedFiles().isEmpty());
        assertEquals("", result.getDiffContent());
        assertEquals(ChangeType.OTHER, result.getChangeType());
        assertEquals("", result.getScope());
    }
}