package com.fulinlin.utils;

import com.fulinlin.model.ChangeType;
import com.fulinlin.model.CodeChangeInfo;
import com.fulinlin.model.FileChange;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.*;

public class PromptBuilderTest {

    @Test
    public void testBuildPrompt_English() {
        CodeChangeInfo info = new CodeChangeInfo();
        FileChange fileChange = new FileChange("src/main/java/com/example/Test.java", ChangeType.FEATURE, "--- Before\n\n+++ After\npublic class Test {}\n");
        info.setChangedFiles(Collections.singletonList(fileChange));
        info.setChangeType(ChangeType.FEATURE);
        info.setDiffContent(fileChange.getDiffContent());

        PromptBuilder builder = new PromptBuilder();
        String prompt = builder.buildPrompt(info, "conventional_en", Locale.ENGLISH);
        assertTrue(prompt.contains("You are a professional Git commit message assistant"));
        assertTrue(prompt.contains("src/main/java/com/example/Test.java"));
        assertTrue(prompt.contains("new feature"));
    }

    @Test
    public void testBuildPrompt_Chinese() {
        CodeChangeInfo info = new CodeChangeInfo();
        FileChange fileChange = new FileChange("src/main/java/com/example/Test.java", ChangeType.FIX, "--- Before\npublic class Test {}\n+++ After\npublic class Test { private int a; }\n");
        info.setChangedFiles(Collections.singletonList(fileChange));
        info.setChangeType(ChangeType.FIX);
        info.setDiffContent(fileChange.getDiffContent());

        PromptBuilder builder = new PromptBuilder();
        String prompt = builder.buildPrompt(info, "conventional_zh", Locale.CHINESE);
        assertTrue(prompt.contains("你是一个专业的Git commit message生成助手"));
        assertTrue(prompt.contains("src/main/java/com/example/Test.java"));
        assertTrue(prompt.contains("Bug修复"));
    }

    @Test
    public void testBuildPrompt_Default() {
        CodeChangeInfo info = new CodeChangeInfo();
        info.setChangedFiles(Collections.emptyList());
        info.setChangeType(ChangeType.OTHER);
        info.setDiffContent("");

        PromptBuilder builder = new PromptBuilder();
        String prompt = builder.buildPrompt(info);
        assertTrue(prompt.contains("You are a professional Git commit message assistant"));
    }

    @Test
    public void testBuildPrompt_MultipleFiles() {
        CodeChangeInfo info = new CodeChangeInfo();
        FileChange file1 = new FileChange("src/main/java/com/example/A.java", ChangeType.FEATURE, "diff1");
        FileChange file2 = new FileChange("src/main/java/com/example/B.java", ChangeType.REFACTOR, "diff2");
        info.setChangedFiles(Arrays.asList(file1, file2));
        info.setChangeType(ChangeType.REFACTOR);
        info.setDiffContent("diff1\ndiff2");

        PromptBuilder builder = new PromptBuilder();
        String prompt = builder.buildPrompt(info, "conventional_en", Locale.ENGLISH);
        assertTrue(prompt.contains("A.java"));
        assertTrue(prompt.contains("B.java"));
        assertTrue(prompt.contains("refactor"));
    }
}