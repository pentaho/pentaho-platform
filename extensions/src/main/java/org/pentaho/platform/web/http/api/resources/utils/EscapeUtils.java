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

import java.io.IOException;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class EscapeUtils {

  static class HTMLCharacterEscapes extends CharacterEscapes {
    private final int[] asciiEscapes;

    public HTMLCharacterEscapes() {
      // start with set of characters known to require escaping (double-quote, backslash etc)
      int[] esc = CharacterEscapes.standardAsciiEscapesForJSON();

      // and force escaping of a few others:
      esc['<'] = CharacterEscapes.ESCAPE_STANDARD;
      esc['>'] = CharacterEscapes.ESCAPE_STANDARD;
      esc['&'] = CharacterEscapes.ESCAPE_STANDARD;
      esc['\''] = CharacterEscapes.ESCAPE_STANDARD;
      esc['\"'] = CharacterEscapes.ESCAPE_STANDARD;

      asciiEscapes = esc;
    }

    // this method gets called for character codes 0 - 127
    @Override
    public int[] getEscapeCodesForAscii() {
      return asciiEscapes;
    }

    // and this for others; we don't need anything special here
    @Override
    public SerializableString getEscapeSequence( int ch ) {
      // no further escaping (beyond ASCII chars) needed:
      return null;
    }
  }

  /**
   * Escapes Strings in a JSON structure
   * @param text
   * @return
   * @throws IOException if failed (when text is not not JSON)
   */
  public static String escapeJson( String text ) throws IOException {
    return escapeJson( text, null );
  }

  public static String escapeJson( String text, DefaultPrettyPrinter prettyPrinter ) throws IOException {
    if ( text == null ) {
      return null;
    }

    JsonNode parsedJson = ( new ObjectMapper() ).readTree( text );

    return getObjectWriter( prettyPrinter ).writeValueAsString( parsedJson );
  }

  /**
   * Escapes any text using the same rules as <code>escapeJson() does</code>
   * @param text
   * @return
   */
  public static String escapeRaw( String text ) {
    return escapeRaw( text, null );
  }

  public static String escapeRaw( String text, DefaultPrettyPrinter prettyPrinter ) {
    if ( text == null ) {
      return null;
    }

    String result = null;
    try {
      String escapedValue = getObjectWriter( prettyPrinter ).writeValueAsString( text );

      result = escapedValue.substring( 1, escapedValue.length() - 1 ); // unquote
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    return result;
  }

  public static String escapeJsonOrRaw( String text ) {
    return escapeJsonOrRaw( text, null );
  }

  public static String escapeJsonOrRaw( String text, DefaultPrettyPrinter prettyPrinter ) {
    if ( text == null ) {
      return null;
    }

    try {
      return escapeJson( text, prettyPrinter );
    } catch ( Exception e ) {
      // logger.debug ?
      return escapeRaw( text, prettyPrinter );
    }
  }

  private static ObjectWriter getObjectWriter( DefaultPrettyPrinter prettyPrinter ) {
    ObjectMapper mapper = new ObjectMapper();

    mapper.getFactory().setCharacterEscapes( new HTMLCharacterEscapes() );

    return mapper.writer( prettyPrinter );
  }
}
