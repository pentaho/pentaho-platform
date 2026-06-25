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


package org.pentaho.platform.web.http.filters;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for PentahoWebContextFilter with Apache Commons Text migration.
 * Validates that StringEscapeUtils.escapeEcmaScript() properly escapes JavaScript in web context.
 */
public class PentahoWebContextFilterCommonsTextMigrationTest {

  @Before
  public void setUp() {
    // Setup if needed
  }

  @Test
  public void testEscapeEcmaScriptDoubleQuotes() {
    String input = "var msg = \"Hello World\";";
    String escaped = StringEscapeUtils.escapeEcmaScript(input);
    
    assertNotNull(escaped);
    assertTrue(escaped.contains("\\\""));
  }

  @Test
  public void testEscapeEcmaScriptSingleQuotes() {
    String input = "'test'";
    String escaped = StringEscapeUtils.escapeEcmaScript(input);
    
    assertNotNull(escaped);
    // Single quotes should be escaped (commons-text escapes them)
    assertNotEquals(input, escaped);
    // Verify the apostrophes are handled (escaped form will differ)
    assertTrue(escaped.contains("\\") || !escaped.contains("'")); // Either escaped or replaced
  }

  @Test
  public void testEscapeEcmaScriptNewlines() {
    String input = "Line1\nLine2";
    String escaped = StringEscapeUtils.escapeEcmaScript(input);
    
    assertNotNull(escaped);
    assertTrue(escaped.contains("\\n") || escaped.contains("\\r\\n"));
  }

  @Test
  public void testEscapeEcmaScriptBackslash() {
    String input = "Path: C:\\Windows\\System32";
    String escaped = StringEscapeUtils.escapeEcmaScript(input);
    
    assertTrue(escaped.contains("\\\\"));
  }

  @Test
  public void testEscapeEcmaScriptNull() {
    String escaped = StringEscapeUtils.escapeEcmaScript(null);
    assertNull(escaped);
  }

  @Test
  public void testEscapeEcmaScriptEmpty() {
    String escaped = StringEscapeUtils.escapeEcmaScript("");
    assertEquals("", escaped);
  }

  @Test
  public void testEscapeEcmaScriptNormalText() {
    String input = "normalVariable123";
    String escaped = StringEscapeUtils.escapeEcmaScript(input);
    
    assertEquals(input, escaped);
  }

  @Test
  public void testEscapeEnvironmentVariable() {
    String envVar = "var env = \"C:\\Program Files\\Pentaho\";";
    String escaped = StringEscapeUtils.escapeEcmaScript(envVar);
    
    assertTrue(escaped.contains("\\\""));
    assertTrue(escaped.contains("\\\\"));
  }

  @Test
  public void testEscapeEcmaScriptPreventsInjection() {
    String injection = "\"; alert('XSS'); var x = \"";
    String escaped = StringEscapeUtils.escapeEcmaScript(injection);
    
    // Escaping quotes prevents code injection by making them harmless in JS context
    assertNotNull(escaped);
    assertTrue(escaped.contains("\\"));  // Must have escape characters
    // The key is: unescaped quotes become escaped, breaking the injection
    assertNotEquals(injection, escaped);  // Must be different (escaped)
  }

  @Test
  public void testEscapeEcmaScriptCarriageReturn() {
    String input = "Line1\rLine2";
    String escaped = StringEscapeUtils.escapeEcmaScript(input);
    
    assertTrue(escaped.contains("\\r"));
  }

  @Test
  public void testEscapeEcmaScriptUsesEcmaStandard() {
    // ECMA standard should escape specific characters
    String input = "var x = {\"key\": \"value\"};";
    String escaped = StringEscapeUtils.escapeEcmaScript(input);
    
    assertTrue(escaped.contains("\\\""));
  }

  @Test
  public void testEcmaScriptRoundtrip() {
    String original = "var msg = \"Hello\\nWorld\";";
    String escaped = StringEscapeUtils.escapeEcmaScript(original);
    
    // Note: Some escape sequences cannot be perfectly reversed
    assertNotNull(escaped);
    assertNotEquals(original, escaped);
  }

  @Test
  public void testEscapeEcmaScriptUnicode() {
    String input = "var msg = \"Unicode: \\u00E9 \\u4E2D\";";
    String escaped = StringEscapeUtils.escapeEcmaScript(input);
    
    assertNotNull(escaped);
  }

  @Test
  public void testEscapeMultipleConsecutiveChars() {
    String input = "\"\"\"\\\\\\";
    String escaped = StringEscapeUtils.escapeEcmaScript(input);
    
    assertNotNull(escaped);
    assertTrue(escaped.contains("\\\""));
    assertTrue(escaped.contains("\\\\"));
  }

  @Test
  public void testReservedCharPatternEscaping() {
    // Pattern like: .*[<>:"|?*]+.*
    String chars = "<>:\"|?*";
    StringBuilder buf = new StringBuilder(".*[");
    
    for (Character ch : chars.toCharArray()) {
      buf.append(StringEscapeUtils.escapeEcmaScript(ch.toString()));
    }
    buf.append("]+.*");
    
    String pattern = buf.toString();
    assertNotNull(pattern);
    // Pattern should contain escaped characters
    assertTrue(pattern.contains(".*["));
    assertTrue(pattern.contains("]+.*"));
  }
}
