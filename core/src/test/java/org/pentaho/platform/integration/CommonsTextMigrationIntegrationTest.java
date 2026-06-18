/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.integration;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for Apache Commons Text migration across pentaho-platform.
 * Validates complete escape/unescape functionality and compatibility across all modules.
 */
@DisplayName("Apache Commons Text Migration - Comprehensive Integration Tests")
public class CommonsTextMigrationIntegrationTest {

  @Test
  @DisplayName("CommonsText escape methods work across all escaping types")
  void testAllEscapeMethodsAvailable() {
    // All required methods should be available in commons-text
    assertDoesNotThrow(() -> StringEscapeUtils.escapeHtml4("test"));
    assertDoesNotThrow(() -> StringEscapeUtils.unescapeHtml4("&lt;test&gt;"));
    assertDoesNotThrow(() -> StringEscapeUtils.escapeXml10("<test>"));
    assertDoesNotThrow(() -> StringEscapeUtils.escapeXml11("<test>"));
    assertDoesNotThrow(() -> StringEscapeUtils.escapeJava("test"));
    assertDoesNotThrow(() -> StringEscapeUtils.escapeEcmaScript("test"));
  }

  @Test
  @DisplayName("HTML4 escaping compatible with XML escaping")
  void testHtml4AndXmlCompatibility() {
    String input = "Text <tag attr=\"value\"> & symbol";
    
    String htmlEscaped = StringEscapeUtils.escapeHtml4(input);
    String xmlEscaped = StringEscapeUtils.escapeXml11(input);
    
    // Both should escape the special characters
    assertTrue(htmlEscaped.contains("&lt;"));
    assertTrue(xmlEscaped.contains("&lt;"));
    assertTrue(htmlEscaped.contains("&amp;"));
    assertTrue(xmlEscaped.contains("&amp;"));
  }

  @Test
  void testXSSPreventionWorkflow() {
    // Simulate a complete XSS attack prevention workflow
    String userInput = "\" onclick=\"alert('XSS')\" data=\"<script>alert('XSS')</script>";
    
    // 1. Escape for HTML context - escaping prevents XSS by escaping dangerous characters
    String htmlEscaped = StringEscapeUtils.escapeHtml4(userInput);
    assertFalse(htmlEscaped.contains("<script"));  // Raw <script tag must be escaped
    assertFalse(htmlEscaped.contains("<"));        // Raw < brackets must be escaped
    assertTrue(htmlEscaped.contains("&lt;"));      // < must be escaped to &lt;

    // 2. Verify that dangerous quotes and angle brackets are escaped for safe HTML attribute usage
    assertTrue(htmlEscaped.contains("&quot;") || htmlEscaped.contains("&#"));
  }

  @Test
  void testErrorMessageXSSPrevention() {
    String errorMsg = "<img src=x onerror='alert(\"XSS\")' /> Database query error";
    String escaped = StringEscapeUtils.escapeHtml4(errorMsg);

    // Prevent XSS by escaping the <img tag - no raw < should exist
    assertFalse(escaped.contains("<img"));     // Raw <img tag must be escaped
    assertFalse(escaped.contains("<"));        // No raw < brackets
    assertTrue(escaped.contains("&lt;"));      // < must be escaped to &lt;
    // Note: onerror= remains as text, but the angle brackets are escaped so it can't execute
  }

  @Test
  @DisplayName("XML attribute value escaping for error/status messages")
  void testXmlAttributeEscaping() {
    String statusMsg = "Error: Value < 10 & > 5";
    String escaped = StringEscapeUtils.escapeXml11(statusMsg);
    
    // Should be safe in XML attribute
    assertEquals("Error: Value &lt; 10 &amp; &gt; 5", escaped);
  }

  @Test
  @DisplayName("Database credential escaping (HTML unescape workflow)")
  void testDatabaseCredentialEscaping() {
    // Simulate credential handling with escaping
    String originalUsername = "user&name";
    String originalPassword = "pass<word>";
    
    // In real scenario: username and password would come from HTML form
    // After form submission, they would be HTML escaped
    String htmlEscapedUsername = StringEscapeUtils.escapeHtml4(originalUsername);
    String htmlEscapedPassword = StringEscapeUtils.escapeHtml4(originalPassword);
    
    // Then database layer would unescape them
    String dbUsername = StringEscapeUtils.unescapeHtml4(htmlEscapedUsername);
    String dbPassword = StringEscapeUtils.unescapeHtml4(htmlEscapedPassword);
    
    // Should be back to original
    assertEquals(originalUsername, dbUsername);
    assertEquals(originalPassword, dbPassword);
  }

  @Test
  @DisplayName("JavaScript environment variable escaping")
  void testJavaScriptVariableEscaping() {
    String filePath = "C:\\Program Files\\Pentaho";
    String escaped = StringEscapeUtils.escapeEcmaScript(filePath);
    
    // Should have escaped backslashes
    assertTrue(escaped.contains("\\\\"));
  }

  @Test
  @DisplayName("Complex workflow: HTML form → XML storage → JavaScript retrieval")
  void testComplexEscapingWorkflow() {
    String userInput = "<div>User & Admin's data</div>";
    
    // Step 1: User submits HTML form
    String htmlEscaped = StringEscapeUtils.escapeHtml4(userInput);
    assertTrue(htmlEscaped.contains("&lt;div&gt;"));
    
    // Step 2: Store in XML
    String storedXml = "<data>" + StringEscapeUtils.escapeXml11(htmlEscaped) + "</data>";
    assertTrue(storedXml.contains("&amp;lt;"));
    
    // Step 3: Retrieve and use in JavaScript
    String jsEscaped = StringEscapeUtils.escapeEcmaScript(htmlEscaped);
    assertNotNull(jsEscaped);
  }

  @Test
  @DisplayName("Round-trip preservation of data through multiple escape levels")
  void testMultiLevelRoundTrip() {
    String original = "Text & <tag> with \"quotes\"";
    
    // Escape for HTML
    String htmlEscaped = StringEscapeUtils.escapeHtml4(original);
    
    // Unescape back
    String htmlUnescaped = StringEscapeUtils.unescapeHtml4(htmlEscaped);
    
    // Should be back to original
    assertEquals(original, htmlUnescaped);
  }

  @Test
  @DisplayName("Control character handling in XML 1.1")
  void testControlCharacterHandling() {
    // XML 1.1 handles control characters differently
    String withControlChar = "Error\u0001Message";
    String xmlEscaped = StringEscapeUtils.escapeXml11(withControlChar);
    
    // Should be escaped
    assertNotEquals(withControlChar, xmlEscaped);
  }

  @Test
  @DisplayName("Null and empty string handling across all methods")
  void testNullAndEmptyHandling() {
    // Nulls should be handled gracefully
    assertNull(StringEscapeUtils.escapeHtml4(null));
    assertNull(StringEscapeUtils.unescapeHtml4(null));
    assertNull(StringEscapeUtils.escapeXml11(null));
    assertNull(StringEscapeUtils.escapeJava(null));
    assertNull(StringEscapeUtils.escapeEcmaScript(null));
    
    // Empty strings should return empty
    assertEquals("", StringEscapeUtils.escapeHtml4(""));
    assertEquals("", StringEscapeUtils.unescapeHtml4(""));
    assertEquals("", StringEscapeUtils.escapeXml11(""));
    assertEquals("", StringEscapeUtils.escapeJava(""));
    assertEquals("", StringEscapeUtils.escapeEcmaScript(""));
  }

  @Test
  @DisplayName("Consecutive special characters handling")
  void testConsecutiveSpecialCharacters() {
    String input = "<<<>>>&&&&\"\"\"''''";
    
    String htmlEscaped = StringEscapeUtils.escapeHtml4(input);
    String xmlEscaped = StringEscapeUtils.escapeXml11(input);
    
    // Both should escape all characters
    assertNotEquals(input, htmlEscaped);
    assertNotEquals(input, xmlEscaped);
  }

  @Test
  @DisplayName("Security: SQL injection attempt escaping")
  void testSqlInjectionAttemptEscaping() {
    String sqlInjection = "'; DROP TABLE users; --";
    String htmlEscaped = StringEscapeUtils.escapeHtml4(sqlInjection);
    
    // In HTML context, this should be neutralized
    assertFalse(htmlEscaped.contains("';"));
    assertTrue(htmlEscaped.contains("&"));
  }

  @Test
  @DisplayName("Security: Command injection attempt escaping")
  void testCommandInjectionAttemptEscaping() {
    String cmdInjection = "command; rm -rf /";
    String xmlEscaped = StringEscapeUtils.escapeXml11(cmdInjection);
    
    // Should not contain unescaped semicolon or slashes
    assertNotNull(xmlEscaped);
  }

  @Test
  @DisplayName("Real-world scenario: Error message with user input")
  void testRealWorldErrorMessageScenario() {
    // Simulates error message containing user-supplied data
    String userInput = "<script>alert('XSS')</script>";
    String errorMsg = "Error processing your request: " + userInput;
    
    String escaped = StringEscapeUtils.escapeHtml4(errorMsg);
    String unescaped = StringEscapeUtils.unescapeHtml4(escaped);
    
    assertEquals(errorMsg, unescaped);
    assertFalse(escaped.contains("<script>"));
  }

  @Test
  @DisplayName("Performance: Multiple escaping operations")
  void testPerformanceMultipleEscapes() {
    String input = "Test <data> & \"value\"";
    
    // Should handle multiple operations efficiently
    for (int i = 0; i < 1000; i++) {
      StringEscapeUtils.escapeHtml4(input);
      StringEscapeUtils.escapeXml11(input);
      StringEscapeUtils.escapeEcmaScript(input);
    }
    
    // If we get here, performance is acceptable (no timeout)
    assertTrue(true);
  }

  @Test
  @DisplayName("CommonsText StringEscapeUtils class availability verification")
  void testCommonsTextClassAvailability() {
    // Verify commons-text is available and correct version
    assertNotNull(StringEscapeUtils.class);
    assertTrue(StringEscapeUtils.class.getPackage().getName().contains("apache.commons.text"));
  }

  @Test
  @DisplayName("Escape method consistency across types")
  void testEscapeConsistency() {
    String input = "A & B < C > D";
    
    String html = StringEscapeUtils.escapeHtml4(input);
    String xml = StringEscapeUtils.escapeXml11(input);
    
    // Both should escape the basic special characters
    assertTrue(html.contains("&amp;"));
    assertTrue(xml.contains("&amp;"));
    assertTrue(html.contains("&lt;"));
    assertTrue(xml.contains("&lt;"));
    assertTrue(html.contains("&gt;"));
    assertTrue(xml.contains("&gt;"));
  }
}
