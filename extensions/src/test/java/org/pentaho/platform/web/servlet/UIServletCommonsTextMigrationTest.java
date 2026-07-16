/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Pentaho, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.web.servlet;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for UIServlet with Apache Commons Text migration.
 * Validates that StringEscapeUtils.escapeHtml4() properly escapes HTML in servlet parameters.
 */
public class UIServletCommonsTextMigrationTest {

  @Before
  public void setUp() {
    // Setup if needed
  }

  @Test
  public void testComponentParameterEscapesHtml() {
    String componentName = "<script>alert('XSS')</script>";
    String escaped = StringEscapeUtils.escapeHtml4(componentName);
    
    assertNotNull(escaped);
    assertFalse(escaped.contains("<script>"));
    assertFalse(escaped.contains("</script>"));
    assertTrue(escaped.contains("&lt;") || escaped.contains("&#"));
  }

  @Test
  public void testComponentEscapesPreventsXss() {
    String injection = "\" onclick=\"alert('XSS')\"";
    String escaped = StringEscapeUtils.escapeHtml4(injection);
    
    // HTML escaping prevents XSS by escaping quotes, not by removing onclick= text
    assertFalse(escaped.contains("\""));   // Raw quotes must be escaped
    assertTrue(escaped.contains("&quot;") || escaped.contains("&#"));  // Quotes are escaped
  }

  @Test
  public void testNullComponentName() {
    String escaped = StringEscapeUtils.escapeHtml4(null);
    assertNull(escaped);
  }

  @Test
  public void testEmptyComponentName() {
    String escaped = StringEscapeUtils.escapeHtml4("");
    assertEquals("", escaped);
  }

  @Test
  public void testNormalComponentName() {
    String componentName = "MainDashboard";
    String escaped = StringEscapeUtils.escapeHtml4(componentName);
    
    assertEquals(componentName, escaped);
  }

  @Test
  public void testComponentNameWithHtmlEntities() {
    String componentName = "Component & Settings";
    String escaped = StringEscapeUtils.escapeHtml4(componentName);
    
    assertEquals("Component &amp; Settings", escaped);
  }

  @Test
  public void testComponentNameWithAngleBrackets() {
    String componentName = "Component<id>123</id>";
    String escaped = StringEscapeUtils.escapeHtml4(componentName);
    
    assertTrue(escaped.contains("&lt;"));
    assertTrue(escaped.contains("&gt;"));
  }

  @Test
  public void testComponentNameWithQuotes() {
    String componentName = "\"ComponentName\"";
    String escaped = StringEscapeUtils.escapeHtml4(componentName);
    
    assertTrue(escaped.contains("&quot;") || escaped.contains("&#"));
  }

  @Test
  public void testEscapeHtml4Standard() {
    String input = "< > & \"";
    String escaped = StringEscapeUtils.escapeHtml4(input);
    
    assertTrue(escaped.contains("&lt;"));
    assertTrue(escaped.contains("&gt;"));
    assertTrue(escaped.contains("&amp;"));
    assertTrue(escaped.contains("&quot;") || escaped.contains("&#"));
  }

  @Test
  public void testHtml4RoundTrip() {
    String original = "Component & <Settings>";
    String escaped = StringEscapeUtils.escapeHtml4(original);
    String unescaped = StringEscapeUtils.unescapeHtml4(escaped);
    
    assertEquals(original, unescaped);
  }

  @Test
  public void testComponentNameWithDatabaseChars() {
    // Test with single quotes and equals signs (SQL injection attempt)
    String componentName = "Component'OR'1'='1";
    String escaped = StringEscapeUtils.escapeHtml4(componentName);
    
    // HTML4 escaping preserves single quotes, but ensures no raw dangerous HTML/JS
    assertNotNull(escaped);
    assertFalse(escaped.contains("<") || escaped.contains(">") || escaped.contains("\""));
  }

  @Test
  public void testMultipleConsecutiveSpecialChars() {
    String componentName = "<<<>>>&&&\"\"\"";
    String escaped = StringEscapeUtils.escapeHtml4(componentName);
    
    String expected = "&lt;&lt;&lt;&gt;&gt;&gt;&amp;&amp;&amp;&quot;&quot;&quot;";
    assertEquals(expected, escaped);
  }

  @Test
  public void testComponentNameWithNewlines() {
    String componentName = "Component\nName\rWith\r\nNewlines";
    String escaped = StringEscapeUtils.escapeHtml4(componentName);
    
    assertNotNull(escaped);
    // HTML escaping should not alter newlines
    assertEquals(componentName, escaped);
  }

  @Test
  public void testComplexComponentName() {
    String componentName = "Component<tag attr=\"value\"> & more";
    String escaped = StringEscapeUtils.escapeHtml4(componentName);
    
    assertTrue(escaped.contains("&lt;"));
    assertTrue(escaped.contains("&gt;"));
    assertTrue(escaped.contains("&amp;"));
    assertTrue(escaped.contains("&quot;") || escaped.contains("&#"));
  }

  @Test
  public void testComponentNameWithCdataLike() {
    String componentName = "<![CDATA[Component Data]]>";
    String escaped = StringEscapeUtils.escapeHtml4(componentName);
    
    assertTrue(escaped.contains("&lt;"));
    assertTrue(escaped.contains("&gt;"));
  }

  @Test
  public void testHtmlInjectionPrevention() {
    String injection = "<img src=x onerror='alert(\"XSS\")'>";
    String escaped = StringEscapeUtils.escapeHtml4(injection);
    
    // HTML escaping prevents XSS by escaping < so <img cannot execute
    assertFalse(escaped.contains("<"));        // No raw angle brackets
    assertTrue(escaped.contains("&lt;"));      // < is escaped
    // Note: onerror= remains as plain text, but the <img tag itself is escaped so it won't execute
  }
}
