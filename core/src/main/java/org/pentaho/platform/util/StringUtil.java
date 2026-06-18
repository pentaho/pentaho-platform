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


package org.pentaho.platform.util;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
   * @param tokenizedString The string to parse. If this is null, null will be returned.
   * @param token           The token used as a seperator. if this is null, the fill string will be returned in a 1
   *                        element array
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

    String[] rtnArray = new String[ strList.size() ];
    return strList.toArray( rtnArray );
  }

  /**
   * Does the path contain a path-segment that is "..".
   *
   * @param path String
   * @return boolean return true if path contains "..", else false.
   */
  public static boolean doesPathContainParentPathSegment( final String path ) {
    Matcher m = StringUtil.CONTAINS_PARENT_PATH_PATTERN.matcher( path );
    return m.matches();
  }

  public static void main( final String[] args ) {
    String[] testStrings = { "../bart/maggie.xml", "/bart/..", "/bart/../maggie/homer.xml", "..//bart/maggie.xml",
      //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      "/bart//..", "/bart//../maggie/homer.xml", "/../", "/..", "../", "..\\bart\\maggie.xml", "\\bart\\..",
      //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
      "\\bart\\..\\maggie\\homer.xml", "I am clean", "/go/not/parent.xml", "\\go\\not\\parent.xml", "..",
      //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
      "should/not..not/match", "..should/not/match.xml", "should/not..",
      "..." }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

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

  /**
   * Returns the provided {@link Map} as a pretty string, using the system dependent new like character as the
   * key-value pair separator, and ' -> ' as the individual key and value separator.
   *
   * @param map the {@link Map} to be converted to a string
   * @return a pretty {@link String} representation of the {@link Map}
   */
  public static String getMapAsPrettyString( final Map map ) {
    final ByteArrayOutputStream writtenBytes = new ByteArrayOutputStream();
    MapUtils.debugPrint( new PrintStream( writtenBytes ), "Map", map );
    return writtenBytes.toString();
  }

  /**
   * Returns a map of String key-value pairs, in which each key and corresponding value has been trimmed of whitespace.
   * @param map the {@link Map} of String key-value pairs to be trimmed
   * @return a trimmed String {@link Map}
   */
  public static Map<String, String> trimStringMap( Map<String, String> map ) {
    return map == null ? map : map.entrySet().stream().collect( Collectors.toMap( e -> StringUtils.trimToEmpty( e.getKey() ), e -> StringUtils.trimToEmpty( e.getValue() ) ) );
  }

  /**
   * Returns a List of Strings in which each String has been trimmed of whitespace.
   * @param list the {@link List} of Strings to be trimmed
   * @return a trimmed {@link List} of Strings
   */
  public static List<String> trimStringList( List<String> list ) {
    return list == null ? list : list.stream().map( StringUtils::trimToEmpty ).collect( Collectors.toList() );
  }

  /**
   * Wrapper method around StringUtils.trim
   * @param string the {@link String} to be trimmed
   * @return the trimmed {@link String}
   */
  public static String trimToEmpty( String string ) {
    return StringUtils.trimToEmpty( string );
  }
}
