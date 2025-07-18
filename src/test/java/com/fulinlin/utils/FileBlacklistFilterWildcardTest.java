package com.fulinlin.utils;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * FileBlacklistFilter 通配符转换测试
 */
public class FileBlacklistFilterWildcardTest {

    private FileBlacklistFilter filter;

    @Before
    public void setUp() {
        filter = new FileBlacklistFilter();
    }

    @Test
    public void testBasicWildcardConversion() {
        // 测试基本的通配符转换
        String result1 = filter.convertWildcardToRegex("*.md");
        System.out.println("*.md -> " + result1);
        assertEquals(".*\\.md$", result1);

        String result2 = filter.convertWildcardToRegex("*.txt");
        System.out.println("*.txt -> " + result2);
        assertEquals(".*\\.txt$", result2);

        String result3 = filter.convertWildcardToRegex("config/*.json");
        System.out.println("config/*.json -> " + result3);
        assertEquals("config/.*\\.json$", result3);

        String result4 = filter.convertWildcardToRegex("src/*/*.java");
        System.out.println("src/*/*.java -> " + result4);
        assertEquals("src/.*/.*\\.java$", result4);
    }

    @Test
    public void testWildcardPatterns() {
        filter.initializeFromConfig("*.md\n*.txt\nconfig/*.json");

        // 测试通配符模式匹配
        assertTrue(filter.isFileBlacklisted("README.md"));
        assertTrue(filter.isFileBlacklisted("documentation.md"));
        assertTrue(filter.isFileBlacklisted("notes.txt"));
        assertTrue(filter.isFileBlacklisted("config/settings.json"));
        assertTrue(filter.isFileBlacklisted("config/database.json"));

        // 测试不匹配的情况
        assertFalse(filter.isFileBlacklisted("README.md.bak"));
        assertFalse(filter.isFileBlacklisted("src/main.java"));
        assertFalse(filter.isFileBlacklisted("config/settings.xml"));
    }

    @Test
    public void testMixedPatterns() {
        // 混合通配符和正则表达式模式
        filter.initializeFromConfig("*.md\n.*\\.key$\nconfig/*\n.*password.*");

        assertTrue(filter.isFileBlacklisted("README.md"));
        assertTrue(filter.isFileBlacklisted("private.key"));
        assertTrue(filter.isFileBlacklisted("config/settings.json"));
        assertTrue(filter.isFileBlacklisted("user-password.conf"));

        assertFalse(filter.isFileBlacklisted("README.md.bak"));
        assertFalse(filter.isFileBlacklisted("public.key.bak"));
        assertFalse(filter.isFileBlacklisted("src/main.java"));
    }

    @Test
    public void testQuestionMarkWildcard() {
        filter.initializeFromConfig("test?.txt\nconfig.???.json");

        assertTrue(filter.isFileBlacklisted("test1.txt"));
        assertTrue(filter.isFileBlacklisted("testA.txt"));
        assertTrue(filter.isFileBlacklisted("config.123.json"));
        assertTrue(filter.isFileBlacklisted("config.abc.json"));

        assertFalse(filter.isFileBlacklisted("test.txt"));
        assertFalse(filter.isFileBlacklisted("test12.txt"));
        assertFalse(filter.isFileBlacklisted("config.12.json"));
        assertFalse(filter.isFileBlacklisted("config.1234.json"));
    }

    @Test
    public void testValidationWithWildcards() {
        // 测试通配符模式的验证
        FileBlacklistFilter.ValidationResult result1 = FileBlacklistFilter.validateBlacklistConfig("*.md\n*.txt");
        assertTrue("Valid wildcard patterns should pass validation", result1.isValid());

        FileBlacklistFilter.ValidationResult result2 = FileBlacklistFilter.validateBlacklistConfig("*.md\n[invalid\n*.txt");
        assertFalse("Invalid patterns should fail validation", result2.isValid());
        assertTrue("Error message should contain line number", result2.getErrorMessage().contains("Line 2"));
    }

    @Test
    public void testRegexPatternsRemainUnchanged() {
        // 测试正则表达式模式保持不变
        String result1 = filter.convertWildcardToRegex(".*\\.key$");
        System.out.println(".*\\.key$ -> " + result1);
        assertEquals(".*\\.key$", result1);

        String result2 = filter.convertWildcardToRegex(".*config.*");
        System.out.println(".*config.* -> " + result2);
        assertEquals(".*config.*", result2);
    }
}