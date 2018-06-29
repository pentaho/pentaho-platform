/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002 - 2018 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.platform.web.http.api.resources.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.jetty.util.ajax.JSON;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.utils.EscapeUtils.HTMLCharacterEscapes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EscapeUtilsTest {

  private static final String JSON_OBJECT_INVALID = "{\"foo\": \"value's\",";
  private static final String JSON_OBJECT_INVALID_RAW_ESCAPE = "{\\u0022foo\\u0022: \\u0022value\\u0027s\\u0022,";

  private static final String JSON_OBJECT = "{" +
    "\"foo\": \"value's\", " +
    "\"bar\": \"<x&x>\", " +
    "\"baz\": [123, \"\", \">\\\\<\"]" +
    "}";
  private static final String JSON_OBJECT_RAW_ESCAPE = "{" +
    "\\u0022foo\\u0022: \\u0022value\\u0027s\\u0022, " +
    "\\u0022bar\\u0022: \\u0022\\u003Cx\\u0026x\\u003E\\u0022, " +
    "\\u0022baz\\u0022: [123, \\u0022\\u0022, \\u0022\\u003E\\\\\\\\\\u003C\\u0022]" +
    "}";
  private static final String JSON_OBJECT_JSON_ESCAPE = "{" +
    "\"foo\":\"value\\u0027s\"," +
    "\"bar\":\"\\u003Cx\\u0026x\\u003E\"," +
    "\"baz\":[123,\"\",\"\\u003E\\\\\\u003C\"]" +
    "}";

  private static final String JSON_ARRAY = "[\"asdf\", \"as<>df\", \"<html>\", \"f&g\", null, false, 123]";
  private static final String JSON_ARRAY_RAW_ESCAPE = "[" +
    "\\u0022asdf\\u0022, \\u0022as\\u003C\\u003Edf\\u0022, \\u0022\\u003Chtml\\u003E\\u0022, " +
    "\\u0022f\\u0026g\\u0022, null, false, 123" +
    "]";
  private static final String JSON_ARRAY_JSON_ESCAPE = "[" +
    "\"asdf\",\"as\\u003C\\u003Edf\",\"\\u003Chtml\\u003E\",\"f\\u0026g\",null,false,123" +
    "]";

  // region escapeJson
  @Test( expected = JsonParseException.class )
  public void testEscapeJson_invalidJsonValue() throws Exception {
    EscapeUtils.escapeJson( JSON_OBJECT_INVALID );
  }

  @Test
  public void testEscapeJson_null() throws Exception {
    assertNull( EscapeUtils.escapeJson( null ) );
  }

  @Test
  public void testEscapeJson_simpleValue() throws Exception {
    final String text = "\"text\"";
    final String expected = "\"text\"";

    assertTestData( expected, text );

    String actual = EscapeUtils.escapeJson( text );

    assertEquals( expected, actual );
  }

  @Test
  public void testEscapeJson_jsonArray() throws Exception {
    assertTestData( JSON_ARRAY_JSON_ESCAPE, JSON_ARRAY );

    String actual = EscapeUtils.escapeJson( JSON_ARRAY );

    assertEquals( JSON_ARRAY_JSON_ESCAPE, actual );

  }

  @Test
  public void testEscapeJson_jsonObject() throws Exception {
    assertTestData( JSON_OBJECT_JSON_ESCAPE, JSON_OBJECT );

    String actual = EscapeUtils.escapeJson( JSON_OBJECT );

    assertEquals( JSON_OBJECT_JSON_ESCAPE, actual );
  }
  // endregion

  // region escapeRaw
  @Test
  public void testEscapeRaw_invalidJsonValue() {
    assertRawTestData( JSON_OBJECT_INVALID_RAW_ESCAPE, JSON_OBJECT_INVALID );

    String actual = EscapeUtils.escapeRaw( JSON_OBJECT_INVALID );

    assertEquals( JSON_OBJECT_INVALID_RAW_ESCAPE, actual );
  }

  @Test
  public void testEscapeRaw_null() {
    assertNull( EscapeUtils.escapeRaw( null ) );
  }

  @Test
  public void testEscapeRaw_simpleValue() {
    final String text = "\"text\"";
    final String expected = "\\u0022text\\u0022";

    assertRawTestData( expected, text );

    String actual = EscapeUtils.escapeRaw( text );

    assertEquals( expected, actual );
  }

  @Test
  public void testEscapeRaw_jsonArray() {
    assertRawTestData( JSON_ARRAY_RAW_ESCAPE, JSON_ARRAY );

    String actual = EscapeUtils.escapeRaw( JSON_ARRAY );

    assertEquals( JSON_ARRAY_RAW_ESCAPE, actual );
  }

  @Test
  public void testEscapeRaw_jsonObject() {
    assertRawTestData( JSON_OBJECT_RAW_ESCAPE, JSON_OBJECT );

    String actual = EscapeUtils.escapeRaw( JSON_OBJECT );

    assertEquals( JSON_OBJECT_RAW_ESCAPE, actual );
  }
  // endregion

  // region escapeJsonOrRaw
  @Test
  public void testEscapeJsonOrRaw_invalidJsonValue() {
    assertRawTestData( JSON_OBJECT_INVALID_RAW_ESCAPE, JSON_OBJECT_INVALID );

    String actual = EscapeUtils.escapeRaw( JSON_OBJECT_INVALID );

    assertEquals( JSON_OBJECT_INVALID_RAW_ESCAPE, actual );
  }

  @Test
  public void testEscapeJsonOrRaw_null() {
    assertNull( EscapeUtils.escapeJsonOrRaw( null ) );
  }

  @Test
  public void testEscapeJsonOrRaw_simpleValue() {
    final String text = "\"text\"";
    final String expected = "\"text\"";

    assertTestData( expected, text );

    String actual = EscapeUtils.escapeJsonOrRaw( text );

    assertEquals( expected, actual );
  }

  @Test
  public void testEscapeJsonOrRaw_jsonArray() {
    assertTestData( JSON_ARRAY_JSON_ESCAPE, JSON_ARRAY );

    String actual = EscapeUtils.escapeJsonOrRaw( JSON_ARRAY );

    assertEquals( JSON_ARRAY_JSON_ESCAPE, actual );

  }

  @Test
  public void testEscapeJsonOrRaw_jsonObject() {
    assertTestData( JSON_OBJECT_JSON_ESCAPE, JSON_OBJECT );

    String actual = EscapeUtils.escapeJsonOrRaw( JSON_OBJECT );

    assertEquals( JSON_OBJECT_JSON_ESCAPE, actual );
  }
  // endregion

  @Test
  public void testHTMLCharacterEscapes() {
    HTMLCharacterEscapes ce = new EscapeUtils.HTMLCharacterEscapes();

    int[] actualEscapes = ce.getEscapeCodesForAscii();
    assertEquals( "<", CharacterEscapes.ESCAPE_STANDARD, actualEscapes[(int) '<'] );
    assertEquals( ">", CharacterEscapes.ESCAPE_STANDARD, actualEscapes[(int) '>'] );
    assertEquals( "&", CharacterEscapes.ESCAPE_STANDARD, actualEscapes[(int) '&'] );
    assertEquals( "\'", CharacterEscapes.ESCAPE_STANDARD, actualEscapes[(int) '\''] );
    assertEquals( "\"", CharacterEscapes.ESCAPE_STANDARD, actualEscapes[(int) '\"'] );

    int[] standardEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
    for ( int i = 0; i < standardEscapes.length; i++ ) {
      if ( "<>&\'\"".indexOf( (char) i ) < 0 ) {
        assertEquals( "(char)" + i, standardEscapes[i], actualEscapes[i] );
      }
    }

    assertNull( ce.getEscapeSequence( 1 ) );
    assertNull( ce.getEscapeSequence( (int) '%' ) );
  }

  // region aux methods
  private void assertTestData( String testData, String actual ) {
    final String expected = JSON.toString( JSON.parse( testData ) );
    final String parsedActual = JSON.toString( JSON.parse( actual ) );

    assertEquals( expected, parsedActual );
  }

  private void assertRawTestData( String testData, String actual ) {
    final String expected = StringEscapeUtils.unescapeJava( testData );

    assertEquals( expected, actual );
  }
  // endregion
}
