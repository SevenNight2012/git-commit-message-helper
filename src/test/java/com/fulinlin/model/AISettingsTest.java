package com.fulinlin.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class AISettingsTest {

    @Test
    public void testDefaultPromptTemplateIsChinese() {
        AISettings settings = new AISettings();
        assertEquals("Default prompt template should be Chinese", "conventional_zh", settings.getPromptTemplate());
    }

    @Test
    public void testSetAndGetPromptTemplate() {
        AISettings settings = new AISettings();

        // Test setting English template
        settings.setPromptTemplate("conventional_en");
        assertEquals("Should return English template", "conventional_en", settings.getPromptTemplate());

        // Test setting Chinese template
        settings.setPromptTemplate("conventional_zh");
        assertEquals("Should return Chinese template", "conventional_zh", settings.getPromptTemplate());
    }

    @Test
    public void testPromptTemplateWithOtherSettings() {
        AISettings settings = new AISettings();
        settings.setApiKey("test-key");
        settings.setEnabled(true);
        settings.setPromptTemplate("conventional_en");

        assertEquals("API key should be preserved", "test-key", settings.getApiKey());
        assertTrue("Enabled should be preserved", settings.isEnabled());
        assertEquals("Prompt template should be English", "conventional_en", settings.getPromptTemplate());
    }
}