package com.fulinlin.utils;

import com.fulinlin.model.CodeChangeInfo;
import org.junit.Test;
import static org.junit.Assert.*;

public class PromptBuilderTest {

    @Test
    public void testBuildPromptWithChineseTemplate() {
        PromptBuilder builder = new PromptBuilder();
        CodeChangeInfo info = new CodeChangeInfo();

        String prompt = builder.buildPrompt(info, "conventional_zh", java.util.Locale.CHINESE);

        assertNotNull("Prompt should not be null", prompt);
        assertTrue("Prompt should contain Chinese text", prompt.contains("你是一个专业的Git commit message生成助手"));
        assertTrue("Prompt should contain Chinese instructions", prompt.contains("代码变更信息"));
    }

    @Test
    public void testBuildPromptWithEnglishTemplate() {
        PromptBuilder builder = new PromptBuilder();
        CodeChangeInfo info = new CodeChangeInfo();

        String prompt = builder.buildPrompt(info, "conventional_en", java.util.Locale.ENGLISH);

        assertNotNull("Prompt should not be null", prompt);
        assertTrue("Prompt should contain English text", prompt.contains("You are a professional Git commit message assistant"));
        assertTrue("Prompt should contain English instructions", prompt.contains("Code changes"));
    }

    @Test
    public void testDefaultPromptIsChinese() {
        PromptBuilder builder = new PromptBuilder();
        CodeChangeInfo info = new CodeChangeInfo();

        String prompt = builder.buildPrompt(info);

        assertNotNull("Prompt should not be null", prompt);
        assertTrue("Default prompt should be Chinese", prompt.contains("你是一个专业的Git commit message生成助手"));
    }
}