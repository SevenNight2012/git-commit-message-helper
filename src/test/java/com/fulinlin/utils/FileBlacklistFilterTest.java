package com.fulinlin.utils;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * FileBlacklistFilter 单元测试
 */
public class FileBlacklistFilterTest {

    private FileBlacklistFilter filter;

    @Before
    public void setUp() {
        filter = new FileBlacklistFilter();
    }

    @Test
    public void testEmptyBlacklist() {
        filter.initializeFromConfig("");

        assertFalse(filter.isFileBlacklisted("any/file/path"));
        assertFalse(filter.isFileBlacklisted("config/settings.json"));
        assertFalse(filter.isFileBlacklisted("src/main/java/Test.java"));
    }

    @Test
    public void testKeyFilePattern() {
        filter.initializeFromConfig(".*\\.key$");

        assertTrue(filter.isFileBlacklisted("config/private.key"));
        assertTrue(filter.isFileBlacklisted("keys/server.key"));
        assertFalse(filter.isFileBlacklisted("config/private.key.bak"));
        assertFalse(filter.isFileBlacklisted("src/main/java/KeyManager.java"));
    }

    @Test
    public void testConfigDirectoryPattern() {
        filter.initializeFromConfig(".*config.*");

        assertTrue(filter.isFileBlacklisted("config/settings.json"));
        assertTrue(filter.isFileBlacklisted("app/config/database.yml"));
        assertTrue(filter.isFileBlacklisted("src/config/application.properties"));
        assertFalse(filter.isFileBlacklisted("src/main/java/ConfigManager.java"));
    }

    @Test
    public void testMultiplePatterns() {
        filter.initializeFromConfig(".*\\.key$\n.*\\.pem$\n.*config.*");

        assertTrue(filter.isFileBlacklisted("config/private.key"));
        assertTrue(filter.isFileBlacklisted("certs/certificate.pem"));
        assertTrue(filter.isFileBlacklisted("config/settings.json"));
        assertFalse(filter.isFileBlacklisted("src/main/java/Test.java"));
    }

    @Test
    public void testFilterFilesList() {
        filter.initializeFromConfig(".*\\.key$\n.*config.*");

        List<String> files = Arrays.asList(
            "src/main/java/Test.java",
            "config/settings.json",
            "keys/private.key",
            "src/main/resources/application.yml"
        );

        List<String> filtered = filter.filterFiles(files);

        assertEquals(2, filtered.size());
        assertTrue(filtered.contains("src/main/java/Test.java"));
        assertTrue(filtered.contains("src/main/resources/application.yml"));
        assertFalse(filtered.contains("config/settings.json"));
        assertFalse(filtered.contains("keys/private.key"));
    }

    @Test
    public void testInvalidRegexPattern() {
        // 测试包含无效正则表达式的配置
        filter.initializeFromConfig(".*\\.key$\n[invalid regex\n.*config.*");

        // 应该忽略无效的正则表达式，只应用有效的
        assertTrue(filter.isFileBlacklisted("config/private.key"));
        assertTrue(filter.isFileBlacklisted("config/settings.json"));
        assertFalse(filter.isFileBlacklisted("src/main/java/Test.java"));
    }

    @Test
    public void testValidationValidPatterns() {
        String validConfig = ".*\\.key$\n.*\\.pem$\n.*config.*";
        FileBlacklistFilter.ValidationResult result = FileBlacklistFilter.validateBlacklistConfig(validConfig);

        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testValidationInvalidPatterns() {
        String invalidConfig = ".*\\.key$\n[invalid regex\n.*config.*";
        FileBlacklistFilter.ValidationResult result = FileBlacklistFilter.validateBlacklistConfig(invalidConfig);

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Line 2"));
    }

    @Test
    public void testValidationEmptyConfig() {
        FileBlacklistFilter.ValidationResult result = FileBlacklistFilter.validateBlacklistConfig("");

        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testValidationNullConfig() {
        FileBlacklistFilter.ValidationResult result = FileBlacklistFilter.validateBlacklistConfig(null);

        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testPathNormalization() {
        filter.initializeFromConfig(".*config.*");

        // 测试Windows路径分隔符
        assertTrue(filter.isFileBlacklisted("config\\settings.json"));
        assertTrue(filter.isFileBlacklisted("app\\config\\database.yml"));

        // 测试Unix路径分隔符
        assertTrue(filter.isFileBlacklisted("config/settings.json"));
        assertTrue(filter.isFileBlacklisted("app/config/database.yml"));
    }
}