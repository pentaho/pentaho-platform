/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.utils;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.SerializableString;
import org.codehaus.jackson.io.CharacterEscapes;
import org.codehaus.jackson.map.ObjectMapper;

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
    @Override public int[] getEscapeCodesForAscii() {
      return asciiEscapes;
    }
    // and this for others; we don't need anything special here
    @Override public SerializableString getEscapeSequence( int ch ) {
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
    if ( text == null ) {
      return null;
    }
    ObjectMapper escapingMapper = new ObjectMapper();
    escapingMapper.getJsonFactory().setCharacterEscapes( new HTMLCharacterEscapes() );

    JsonNode parsedJson = ( new ObjectMapper() ).readTree( text );
    String result = escapingMapper.writeValueAsString( parsedJson );
    return result;
  }

  /**
   * Escapes any text using the same rules as <code>escapeJson() does</code>
   * @param text
   * @return
   */
  public static String escapeRaw( String text ) {
    if ( text == null ) {
      return null;
    }
    ObjectMapper escapingMapper = new ObjectMapper();
    escapingMapper.getJsonFactory().setCharacterEscapes( new HTMLCharacterEscapes() );

    String result = null;
    try {
      result = escapingMapper.writeValueAsString( text );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return result.substring( 1, result.length() - 1 ); //unquote
  }

  public static String escapeJsonOrRaw( String text ) {
    if ( text == null ) {
      return null;
    }
    try {
      return escapeJson( text );
    } catch ( Exception e ) {
      //logger.debug ?
      return escapeRaw( text );
    }
  }

}
