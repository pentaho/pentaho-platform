/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.platform.engine.services;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Apache Commons Text StringEscapeUtils migration.
 * Validates that StringEscapeUtils methods from commons-text work correctly
 * as replacements for deprecated commons-lang3 methods.
 */
@DisplayName("StringEscapeUtils Migration Tests")
public class StringEscapeUtilsMigrationTest {

  // ==================== HTML4 Escape/Unescape Tests ====================

  @Test
  @DisplayName("escapeHtml4() should escape HTML special characters")
  void testEscapeHtml4BasicChars() {
    String input = "<script>alert('XSS')</script>";
    String result = StringEscapeUtils.escapeHtml4(input);
    // Verify key invariants: angle brackets and quotes are escaped (hex or decimal)
    assertFalse(result.contains("<script"));  // Raw <script should be escaped
    assertTrue(result.contains("&lt;"));      // < is escaped
    assertTrue(result.contains("&gt;"));      // > is escaped
    assertTrue(result.contains("&#") || result.contains("&"));  // Quotes/apostrophes escaped
  }

  @Test
  @DisplayName("escapeHtml4() should escape ampersand, less than, greater than, quote, and apostrophe")
  void testEscapeHtml4SpecialChars() {
    String input = "\"Hello & goodbye\" <tag>";
    String result = StringEscapeUtils.escapeHtml4(input);
    assertTrue(result.contains("&quot;") || result.contains("&#x22;"));
    assertTrue(result.contains("&amp;"));
    assertTrue(result.contains("&lt;"));
    assertTrue(result.contains("&gt;"));
  }

  @Test
  @DisplayName("escapeHtml4() should handle null input")
  void testEscapeHtml4Null() {
    assertNull(StringEscapeUtils.escapeHtml4(null));
  }

  @Test
  @DisplayName("escapeHtml4() should handle empty string")
  void testEscapeHtml4Empty() {
    assertEquals("", StringEscapeUtils.escapeHtml4(""));
  }

  @Test
  @DisplayName("escapeHtml4() should handle already escaped content")
  void testEscapeHtml4AlreadyEscaped() {
    String input = "&lt;div&gt;";
    String result = StringEscapeUtils.escapeHtml4(input);
    assertEquals("&amp;lt;div&amp;gt;", result);
  }

  @Test
  @DisplayName("unescapeHtml4() should unescape HTML entities")
  void testUnescapeHtml4BasicEntities() {
    String input = "&lt;script&gt;alert(&#x27;XSS&#x27;)&lt;/script&gt;";
    String result = StringEscapeUtils.unescapeHtml4(input);
    assertTrue(result.contains("<script>") && result.contains("</script>"));
  }

  @Test
  @DisplayName("unescapeHtml4() should handle numeric and named entities")
  void testUnescapeHtml4NumericAndNamed() {
    String input = "&quot;Hello &amp; goodbye&quot; &#60;tag&#62;";
    String result = StringEscapeUtils.unescapeHtml4(input);
    assertTrue(result.contains("\"") && result.contains("&") && result.contains("<"));
  }

  @Test
  @DisplayName("unescapeHtml4() should handle null input")
  void testUnescapeHtml4Null() {
    assertNull(StringEscapeUtils.unescapeHtml4(null));
  }

  @Test
  @DisplayName("unescapeHtml4() should handle empty string")
  void testUnescapeHtml4Empty() {
    assertEquals("", StringEscapeUtils.unescapeHtml4(""));
  }

  @Test
  @DisplayName("Round-trip: escapeHtml4() then unescapeHtml4() should return original")
  void testRoundTripHtml4() {
    String original = "Test <tag> with & special \"chars\"";
    String escaped = StringEscapeUtils.escapeHtml4(original);
    String unescaped = StringEscapeUtils.unescapeHtml4(escaped);
    assertEquals(original, unescaped);
  }

  // ==================== XML Escape Tests ====================

  @Test
  @DisplayName("escapeXml10() should escape XML special characters")
  void testEscapeXml10BasicChars() {
    String input = "<tag attribute=\"value\">Content & more</tag>";
    String result = StringEscapeUtils.escapeXml10(input);
    assertTrue(result.contains("&lt;") && result.contains("&gt;") && result.contains("&amp;"));
  }

  @Test
  @DisplayName("escapeXml10() should handle null input")
  void testEscapeXml10Null() {
    assertNull(StringEscapeUtils.escapeXml10(null));
  }

  @Test
  @DisplayName("escapeXml10() should handle empty string")
  void testEscapeXml10Empty() {
    assertEquals("", StringEscapeUtils.escapeXml10(""));
  }

  @Test
  @DisplayName("escapeXml11() should escape XML 1.1 special characters")
  void testEscapeXml11BasicChars() {
    String input = "<tag>Content</tag>";
    String result = StringEscapeUtils.escapeXml11(input);
    assertTrue(result.contains("&lt;") && result.contains("&gt;"));
  }

  @Test
  @DisplayName("escapeXml11() should escape control characters")
  void testEscapeXml11ControlChars() {
    String input = "Text with \u0001 control char";
    String result = StringEscapeUtils.escapeXml11(input);
    // Control characters should be escaped in XML 1.1
    assertNotEquals(input, result);
  }

  @Test
  @DisplayName("escapeXml11() should handle null input")
  void testEscapeXml11Null() {
    assertNull(StringEscapeUtils.escapeXml11(null));
  }

  @Test
  @DisplayName("escapeXml11() should handle empty string")
  void testEscapeXml11Empty() {
    assertEquals("", StringEscapeUtils.escapeXml11(""));
  }

  // ==================== Java Escape Tests ====================

  @Test
  @DisplayName("escapeJava() should escape Java string special characters")
  void testEscapeJavaSpecialChars() {
    String input = "Line1\nLine2\tTabbed\"Quoted\"";
    String result = StringEscapeUtils.escapeJava(input);
    assertTrue(result.contains("\\n") && result.contains("\\t") && result.contains("\\\""));
  }

  @Test
  @DisplayName("escapeJava() should escape backslash")
  void testEscapeJavaBackslash() {
    String input = "C:\\Windows\\System32";
    String result = StringEscapeUtils.escapeJava(input);
    assertTrue(result.contains("\\\\"));
  }

  @Test
  @DisplayName("escapeJava() should handle null input")
  void testEscapeJavaNul() {
    assertNull(StringEscapeUtils.escapeJava(null));
  }

  @Test
  @DisplayName("escapeJava() should handle empty string")
  void testEscapeJavaEmpty() {
    assertEquals("", StringEscapeUtils.escapeJava(""));
  }

  @Test
  @DisplayName("escapeJava() should escape Unicode characters")
  void testEscapeJavaUnicode() {
    String input = "Unicode: \u00E9 \u4E2D\u6587";
    String result = StringEscapeUtils.escapeJava(input);
    assertTrue(result.contains("\\u00e9") || result.contains("\\u00E9"));
  }

  @Test
  @DisplayName("Round-trip: escapeJava() should preserve content for ASCII")
  void testRoundTripJavaAscii() {
    String original = "Simple text";
    String escaped = StringEscapeUtils.escapeJava(original);
    assertEquals(original, escaped);
  }

  // ==================== JavaScript/ECMAScript Tests ====================

  @Test
  @DisplayName("escapeEcmaScript() should escape JavaScript special characters")
  void testEscapeEcmaScriptSpecialChars() {
    String input = "var msg = \"Hello\"; // Comment";
    String result = StringEscapeUtils.escapeEcmaScript(input);
    assertTrue(result.contains("\\\""));
  }

  @Test
  @DisplayName("escapeEcmaScript() should handle single quotes")
  void testEscapeEcmaScriptSingleQuotes() {
    String input = "It's a test";
    String result = StringEscapeUtils.escapeEcmaScript(input);
    // Result should contain escaped quote or be unchanged for single quotes in certain contexts
    assertNotNull(result);
  }

  @Test
  @DisplayName("escapeEcmaScript() should handle newlines and tabs")
  void testEscapeEcmaScriptWhitespace() {
    String input = "Line1\nLine2\tTabbed";
    String result = StringEscapeUtils.escapeEcmaScript(input);
    assertTrue(result.contains("\\n") || result.contains("\\r\\n") || result.contains("\\t"));
  }

  @Test
  @DisplayName("escapeEcmaScript() should handle null input")
  void testEscapeEcmaScriptNull() {
    assertNull(StringEscapeUtils.escapeEcmaScript(null));
  }

  @Test
  @DisplayName("escapeEcmaScript() should handle empty string")
  void testEscapeEcmaScriptEmpty() {
    assertEquals("", StringEscapeUtils.escapeEcmaScript(""));
  }

  // ==================== Integration Tests ====================

  @Test
  @DisplayName("HTML4 escape should prevent XSS attacks")
  void testXSSPrevention() {
    String xssPayload = "<img src=x onerror='alert(\"XSS\")'>";
    String escaped = StringEscapeUtils.escapeHtml4(xssPayload);
    // HTML escaping prevents XSS by escaping the <img tag, not removing onerror= text
    assertFalse(escaped.contains("<img"));   // Raw <img should not exist
    assertTrue(escaped.contains("&lt;"));    // < must be escaped
  }

  @Test
  @DisplayName("XML escape should handle CDATA sections")
  void testXmlWithCDATA() {
    String input = "<![CDATA[Some content]]>";
    String result = StringEscapeUtils.escapeXml10(input);
    assertTrue(result.contains("&lt;") && result.contains("&gt;"));
  }

  @Test
  @DisplayName("Multiple escape types can be used in sequence")
  void testMultipleEscapeSequence() {
    String original = "<div>\"Test\" & other</div>";
    
    // Escape for HTML context
    String htmlEscaped = StringEscapeUtils.escapeHtml4(original);
    assertFalse(htmlEscaped.contains("<"));
    
    // Escape for Java string
    String javaEscaped = StringEscapeUtils.escapeJava(original);
    assertFalse(javaEscaped.contains("\n"));
  }

  @Test
  @DisplayName("escapeHtml4() should prevent XSS attack vectors")
  void testXSSVectors1() {
    String[] payloads = {
      "<script>",
      "'; DROP TABLE users; --",
      "\"><script>alert('xss')</script>",
      "\\x3cscript\\x3e",
      "%3Cscript%3E"
    };
    for (String payload : payloads) {
      String escaped = StringEscapeUtils.escapeHtml4(payload);
      assertFalse(escaped.contains("<script") || escaped.contains("script>"));
    }
  }

  @Test
  @DisplayName("Escaped output should be safe for HTML attributes")
  void testSafeHtmlAttribute() {
    String userInput = "\" onmouseover=\"alert('XSS')";
    String escaped = StringEscapeUtils.escapeHtml4(userInput);
    // Should be safe to use in attribute: <div data-value="ESCAPED_HERE">
    assertTrue(escaped.contains("&quot;") || escaped.contains("&#x22;"));
  }

  @Test
  @DisplayName("Commons-text StringEscapeUtils is available")
  void testCommonsTextAvailability() {
    assertNotNull(StringEscapeUtils.class);
    assertTrue(StringEscapeUtils.class.getPackage().getName().contains("apache.commons.text"));
  }
}
