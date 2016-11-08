/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

  private StringUtil() {
    super();
    // No need for a constructor since it should be all static utils
  }

  // match "../stuff", "stuff/../stuff", "stuff/..", and same string with "\" instead of "/"
  // Also, fail if it contains a %00 which gets interpreted as a null when thrown at the
  // file system.
  private static final String RE_CONTAINS_PARENT_PATH = "(^.*[/\\\\]|^)\\.\\.([/\\\\].*$|$)|(^.*\0.*$)"; //$NON-NLS-1$

  private static final Pattern CONTAINS_PARENT_PATH_PATTERN = Pattern.compile( StringUtil.RE_CONTAINS_PARENT_PATH );

  /**
   * Tokenize a string and return a String Array of the values seperated by the passed in token.
   * 
   * @param tokenizedString
   *          The string to parse. If this is null, null will be returned.
   * @param token
   *          The token used as a seperator. if this is null, the fill string will be returned in a 1 element array
   * @return Array of Strings that were seperated by the token.
   */
  public static String[] tokenStringToArray( final String tokenizedString, final String token ) {
    if ( tokenizedString == null ) {
      return ( null );
    }
    if ( token == null ) {
      return ( new String[] { tokenizedString } );
    }

    StringTokenizer st = new StringTokenizer( tokenizedString, token );
    List<String> strList = new ArrayList<String>();
    while ( st.hasMoreTokens() ) {
      String tok = st.nextToken();
      strList.add( tok );
    }

    String[] rtnArray = new String[strList.size()];
    return strList.toArray( rtnArray );
  }

  /**
   * Does the path contain a path-segment that is "..".
   * 
   * @param path
   *          String
   * @return boolean return true if path contains "..", else false.
   */
  public static boolean doesPathContainParentPathSegment( final String path ) {
    Matcher m = StringUtil.CONTAINS_PARENT_PATH_PATTERN.matcher( path );
    return m.matches();
  }

  public static void main( final String[] args ) {
    String[] testStrings = { "../bart/maggie.xml", "/bart/..", "/bart/../maggie/homer.xml", "..//bart/maggie.xml", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      "/bart//..", "/bart//../maggie/homer.xml", "/../", "/..", "../", "..\\bart\\maggie.xml", "\\bart\\..", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
      "\\bart\\..\\maggie\\homer.xml", "I am clean", "/go/not/parent.xml", "\\go\\not\\parent.xml", "..", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
      "should/not..not/match", "..should/not/match.xml", "should/not..", "..." }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    for ( String testString : testStrings ) {
      boolean matches = StringUtil.doesPathContainParentPathSegment( testString );
      if ( matches ) {
        System.out.println( testString + " matches." ); //$NON-NLS-1$
      } else {
        System.out.println( "--------" + testString + " DOES NOT MATCH." ); //$NON-NLS-1$//$NON-NLS-2$
      }
    }
  }

  public static boolean isEmpty( final String str ) {
    return ( str == null ) || ( str.length() == 0 );
  }

}
