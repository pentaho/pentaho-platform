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

package org.pentaho.platform.engine.services;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.api.engine.ActionSequenceException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for MessageFormatter with Apache Commons Text migration.
 * Validates that StringEscapeUtils.escapeHtml4() properly escapes HTML content for XSS prevention.
 */
public class MessageFormatterCommonsTextMigrationTest {

  private MessageFormatter messageFormatter;

  @Before
  public void setUp() {
    messageFormatter = new MessageFormatter();
  }

  @Test
  public void testGetFirstErrorEscapesHtml() {
    List<String> messages = new ArrayList<>();
    messages.add("Error: <script>alert('XSS')</script>");
    messages.add("Another error message");

    String result = messageFormatter.getFirstError(messages);
    
    assertNotNull(result);
    assertFalse(result.contains("<script>"));
    assertFalse(result.contains("</script>"));
    assertTrue(result.contains("&lt;") || result.contains("&#"));
  }

  @Test
  public void testGetFirstErrorPreventsXssEventHandlers() {
    List<String> messages = new ArrayList<>();
    messages.add("Error: \" onmouseover=\"alert('XSS')");
    
    String result = messageFormatter.getFirstError(messages);
    
    assertNotNull(result);
    // escapeHtml4() escapes quotes in attributes, preventing XSS
    assertFalse(result.contains("\""));  // raw quotes must be escaped
    assertTrue(result.contains("&quot;") || result.contains("&#"));
  }

  @Test
  public void testGetFirstErrorEscapesAmpersands() {
    List<String> messages = new ArrayList<>();
    messages.add("Error: Parameters A & B & C");
    
    String result = messageFormatter.getFirstError(messages);
    
    assertNotNull(result);
    assertTrue(result.contains("&amp;"));
  }

  @Test
  public void testGetFirstErrorNullMessages() {
    String result = messageFormatter.getFirstError(null);
    assertNull(result);
  }

  @Test
  public void testGetFirstErrorNotFound() {
    List<String> messages = new ArrayList<>();
    messages.add("Warning: This is not an error");
    
    String result = messageFormatter.getFirstError(messages);
    assertNull(result);
  }

  @Test
  public void testGetFirstErrorNormalText() {
    List<String> messages = new ArrayList<>();
    messages.add("Error: Normal error message without special characters");
    
    String result = messageFormatter.getFirstError(messages);
    
    assertNotNull(result);
    assertTrue(result.contains("Error:"));
    assertTrue(result.contains("Normal error message"));
  }

  @Test
  public void testGetFirstErrorEmptyMessages() {
    List<String> messages = new ArrayList<>();
    
    String result = messageFormatter.getFirstError(messages);
    assertNull(result);
  }

  @Test
  public void testFormatErrorMessageEscapesHtml() {
    StringBuffer messageBuffer = new StringBuffer();
    List<String> messages = new ArrayList<>();
    messages.add("<img src=x onerror='alert(\"XSS\")'>");
    messages.add("Normal error message");

    messageFormatter.formatErrorMessage("text/html", "Test Error", messages, messageBuffer);
    
    String result = messageBuffer.toString();
    // HTML escaping prevents XSS by escaping <img tag, not by removing onerror= string
    assertFalse(result.contains("<img"));  // Raw <img tag should be escaped
    assertTrue(result.contains("&lt;"));   // Opening < should be escaped
    assertTrue(result.contains("&") || result.contains("&#"));
  }

  @Test
  public void testFormatErrorMessageUsesHtml4Escaping() {
    StringBuffer messageBuffer = new StringBuffer();
    List<String> messages = new ArrayList<>();
    messages.add("Error with special chars: <>&\"'");

    messageFormatter.formatErrorMessage("text/html", "Title", messages, messageBuffer);
    
    String result = messageBuffer.toString();
    // HTML4 escaping should handle all these characters
    assertTrue(result.contains("&lt;"));
    assertTrue(result.contains("&gt;"));
    assertTrue(result.contains("&amp;"));
  }

  @Test
  public void testFormatErrorMessagePlainText() {
    StringBuffer messageBuffer = new StringBuffer();
    List<String> messages = new ArrayList<>();
    messages.add("Error message");

    messageFormatter.formatErrorMessage("text/plain", "Title", messages, messageBuffer);
    
    String result = messageBuffer.toString();
    assertNotNull(result);
    // Plain text should not contain HTML tags
    assertFalse(result.contains("<html>"));
  }

  @Ignore("Requires PentahoSystem initialization")
  @Test
  public void testFormatExceptionMessageEscapesException() {
    StringBuffer messageBuffer = new StringBuffer();
    ActionSequenceException exception = new ActionSequenceException("Test <error> & \"message\"");

    messageFormatter.formatExceptionMessage("text/html", exception, messageBuffer);
    
    String result = messageBuffer.toString();
    assertFalse(result.contains("Test <error>"));
    assertTrue(result.contains("&lt;") || result.contains("&#"));
  }

  @Ignore("Requires PentahoSystem initialization")
  @Test
  public void testFormatExceptionMessagePreventsXss() {
    StringBuffer messageBuffer = new StringBuffer();
    ActionSequenceException exception = new ActionSequenceException(
        "Exception with <script>alert('XSS')</script>"
    );

    messageFormatter.formatExceptionMessage("text/html", exception, messageBuffer);
    
    String result = messageBuffer.toString();
    assertFalse(result.contains("<script>"));
    assertFalse(result.contains("</script>"));
  }

  @Test
  public void testHtmlEscapingRoundtrip() {
    String original = "Test message with <html> & \"special\" chars";
    String escaped = StringEscapeUtils.escapeHtml4(original);
    String unescaped = StringEscapeUtils.unescapeHtml4(escaped);
    
    assertEquals(original, unescaped);
  }

  @Test
  public void testEscapeHtml4SpecialCharacters() {
    String input = "< > & \" '";
    String result = StringEscapeUtils.escapeHtml4(input);
    
    // At least some characters should be escaped
    assertTrue(result.contains("&"));
  }

  @Test
  public void testMultipleConsecutiveSpecialChars() {
    String input = "<<<>>>&&&&\"\"\"";
    String result = StringEscapeUtils.escapeHtml4(input);
    
    // Each character should be escaped: < > & "
    String expected = "&lt;&lt;&lt;&gt;&gt;&gt;&amp;&amp;&amp;&amp;&quot;&quot;&quot;";
    assertEquals(expected, result);
  }
}
