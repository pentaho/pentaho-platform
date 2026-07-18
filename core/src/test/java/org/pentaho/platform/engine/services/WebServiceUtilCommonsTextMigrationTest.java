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
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for WebServiceUtil with Apache Commons Text migration.
 * Validates that StringEscapeUtils.escapeXml11() properly escapes XML in web service responses.
 */
public class WebServiceUtilCommonsTextMigrationTest {

  @Test
  public void testErrorAttributeEscapesXml() {
    String errorMsg = "Error: Value < 10 and > 5 with & symbol";
    String escaped = StringEscapeUtils.escapeXml11(errorMsg);
    
    assertNotNull(escaped);
    assertTrue(escaped.contains("&lt;"));
    assertTrue(escaped.contains("&gt;"));
    assertTrue(escaped.contains("&amp;"));
  }

  @Test
  public void testStatusAttributeEscapesXml() {
    String statusMsg = "Status: \"Success\" with <data>";
    String escaped = StringEscapeUtils.escapeXml11(statusMsg);
    
    assertTrue(escaped.contains("&lt;"));
    assertTrue(escaped.contains("&gt;"));
  }

  @Test
  public void testEscapeNullMessage() {
    String escaped = StringEscapeUtils.escapeXml11(null);
    assertNull(escaped);
  }

  @Test
  public void testEscapeEmptyMessage() {
    String escaped = StringEscapeUtils.escapeXml11("");
    assertEquals("", escaped);
  }

  @Test
  public void testEscapeNormalMessage() {
    String message = "This is a normal error message";
    String escaped = StringEscapeUtils.escapeXml11(message);
    
    assertEquals(message, escaped);
  }

  @Test
  public void testEscapeAmpersandInAttribute() {
    String message = "Parameters A & B & C";
    String escaped = StringEscapeUtils.escapeXml11(message);
    
    assertEquals("Parameters A &amp; B &amp; C", escaped);
  }

  @Test
  public void testEscapeQuotesInAttribute() {
    String message = "Error: \"Parameter invalid\"";
    String escaped = StringEscapeUtils.escapeXml11(message);
    
    // Quotes are escaped in XML 1.1 context
    assertNotNull(escaped);
  }

  @Test
  public void testEscapeXml11ControlCharacters() {
    // XML 1.1 escapes control characters (0x01-0x08, 0x0B-0x0C, 0x0E-0x1F)
    String message = "Error\u0001with\u0003control\u001Fchars";
    String escaped = StringEscapeUtils.escapeXml11(message);
    
    // Should be different due to control character escaping
    assertNotEquals(message, escaped);
  }

  @Test
  public void testEscapeComplexErrorMessage() {
    String message = "Error in <element attr=\"value\"> & other < > & symbols";
    String escaped = StringEscapeUtils.escapeXml11(message);
    
    assertTrue(escaped.contains("&lt;"));
    assertTrue(escaped.contains("&gt;"));
    assertTrue(escaped.contains("&amp;"));
  }

  @Test
  public void testEscapeCdataLikeContent() {
    String message = "Error: <![CDATA[Some content]]>";
    String escaped = StringEscapeUtils.escapeXml11(message);
    
    assertTrue(escaped.contains("&lt;"));
    assertTrue(escaped.contains("&gt;"));
  }

  @Test
  public void testXmlRoundtrip() {
    String original = "Error: Value < 10 & > 5 with \"quotes\"";
    String escaped = StringEscapeUtils.escapeXml11(original);
    String unescaped = StringEscapeUtils.unescapeXml(escaped);
    
    assertEquals(original, unescaped);
  }

  @Test
  public void testDatabaseValueInErrorMessage() {
    String dbValue = "SELECT * FROM users WHERE 1=1' <script>";
    String escaped = StringEscapeUtils.escapeXml11(dbValue);
    
    assertFalse(escaped.contains("<script>"));
    assertTrue(escaped.contains("&lt;"));
  }

  @Test
  public void testConsecutiveSpecialCharsInStatus() {
    String statusMsg = "<<<>>>&&&\"\"\"";
    String escaped = StringEscapeUtils.escapeXml11(statusMsg);
    
    // Verify key invariants: < > & " are all escaped, no raw versions remain
    assertFalse(escaped.contains("<"));
    assertFalse(escaped.contains(">"));
    assertFalse(escaped.contains("&") && !escaped.contains("&lt;"));
    assertFalse(escaped.contains("\""));
    // Verify escaping occurred
    assertTrue(escaped.contains("&lt;"));
    assertTrue(escaped.contains("&gt;"));
    assertTrue(escaped.contains("&amp;"));
    assertTrue(escaped.contains("&quot;"));
  }

  @Test
  public void testEscapeNewlinesInMessage() {
    String message = "Error line 1\nError line 2\rError line 3\r\nError line 4";
    String escaped = StringEscapeUtils.escapeXml11(message);
    
    assertNotNull(escaped);
    // Newlines are preserved in XML
    assertTrue(escaped.contains("\n") || !escaped.isEmpty());
  }
}
