/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RepositoryPathEncoder {

  /**
   * Use this function to escape the "colonized" path for addition to a url.
   * 
   * @param value
   *          The colonized string (eg: ":public:Steel Wheels:File::WithColon")
   * @return Escaped version of value (eg: "%3Apublic%3ASteel%20Wheels%3AFile%3A%3AWithColon")
   */
  public static String encode( String value ) {
    String encoded = encodeURIComponent( value );
    return encoded.replace( "%5C", "%255C" ).replace( "%2F", "%252F" );
  }

  /**
   * Encodes a string using the equivalent of a javascript's <code>encodeURIComponent</code>
   * 
   * @param value
   *          The String to be encoded
   * @return the encoded String
   */
  public static String encodeURIComponent( String value ) {
    String encoded = null;
    try {
      encoded =
          URLEncoder.encode( value, "UTF-8" ).replaceAll( "\\+", "%20" ).replaceAll( "\\%21", "!" ).replaceAll(
              "\\%27", "'" ).replaceAll( "\\%28", "(" ).replaceAll( "\\%29", ")" ).replaceAll( "\\%7E", "~" );
    }
    catch ( UnsupportedEncodingException e ) {
      //Should not happen
      encoded = value;
    }
    return encoded;
  }

  public static String encodeRepositoryPath( String path ) {
    return path.replace( ":", "\t" ).replace( "/", ":" );
  }

  public static String decodeRepositoryPath( String path ) {
    return path.replace( ":", "/" ).replace( "\t", ":" );
  }
}
