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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * General filename and filepath manipulation utilities for the Pentaho Repository. NOTE: these methods will work
 * independently of the underlying operating system. Most methods will translate a backslash (\) to a forward slash
 * (/) but should be be depended upon to make that translation.
 * <p/>
 * This class defines six components within a filename (example /dev/project/file.txt):
 * <ul>
 * <li>the prefix - /</li>
 * <li>the path - dev/project/</li>
 * <li>the full path - /dev/project/</li>
 * <li>the name - file.txt</li>
 * <li>the base name - file</li>
 * <li>the extension - txt</li>
 * </ul>
 * Note that this class works best if directory filenames end with a separator. If you omit the last separator, it
 * is impossible to determine if the filename corresponds to a file or a directory. As a result, we have chosen to
 * say it corresponds to a file.
 * <p/>
 * This class only supports Pentaho Repository (Unix) style names. Prefixes are matched as follows:
 * 
 * <pre>
 * a/b/c.txt           --> ""          --> relative
 * /a/b/c.txt          --> "/"         --> absolute
 * </pre>
 * <p/>
 * Origin of code: Apache Commons IO 2.1
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 * @since Pentaho 5.0
 */
public class RepositoryFilenameUtils {
  private static final char SEPARATOR = RepositoryFile.SEPARATOR.charAt( 0 );

  private static final Character DEFAULT_ESCAPE_CHAR = '%';

  private static final String ESCAPE_CHAR_SYSTEM_PROPERTY = "pentaho.repository.client.escapeChar"; //$NON-NLS-1$

  private static Character escapeChar;

  static {
    String escapeCharStr = System.getProperty( ESCAPE_CHAR_SYSTEM_PROPERTY );
    if ( escapeCharStr != null && escapeCharStr.trim().length() != 1 ) {
      escapeChar = new Character( escapeCharStr.charAt( 0 ) );
    } else {
      escapeChar = DEFAULT_ESCAPE_CHAR;
    }
  }

  /**
   * Instances should NOT be constructed in standard programming.
   */
  private RepositoryFilenameUtils() {
  }

  // -----------------------------------------------------------------------

  /**
   * Normalizes a path, removing double and single dot path steps.
   * <p/>
   * This method normalizes a path to a standard format.
   * <p/>
   * A trailing slash will be retained. A double slash will be merged to a single slash (but UNC names are
   * handled). A single dot path segment will be removed. A double dot will cause that path segment and the one
   * before to be removed. If the double dot has no parent path segment to work with, <code>null</code> is
   * returned.
   * <p/>
   * The output will be the same on both Unix and Windows except for the separator character.
   * 
   * <pre>
   * /foo//               -->   /foo/
   * /foo/./              -->   /foo/
   * /foo/../bar          -->   /bar
   * /foo/../bar/         -->   /bar/
   * /foo/../bar/../baz   -->   /baz
   * //foo//./bar         -->   /foo/bar
   * /../                 -->   null
   * ../foo               -->   null
   * foo/bar/..           -->   foo/
   * foo/../../bar        -->   null
   * foo/../bar           -->   bar
   * </pre>
   * 
   * @param filename
   *          the filename to normalize, null returns null
   * @return the normalized filename, or null if invalid
   */
  public static String normalize( final String filename ) {
    return FilenameUtils.normalize( filename, true );
  }

  /**
   * Normalizes a path, removing double and single dot path steps.
   * <p/>
   * This method normalizes a path to a standard format.
   * <p/>
   * A trailing slash will be retained. A double slash will be merged to a single slash (but UNC names are
   * handled). A single dot path segment will be removed. A double dot will cause that path segment and the one
   * before to be removed. If the double dot has no parent path segment to work with, <code>null</code> is
   * returned.
   * <p/>
   * The output will be the same on both Unix and Windows except for the separator character.
   * 
   * <pre>
   * /foo//               -->   /foo/
   * /foo/./              -->   /foo/
   * /foo/../bar          -->   /bar
   * /foo/../bar/         -->   /bar/
   * /foo/../bar/../baz   -->   /baz
   * //foo//./bar         -->   /foo/bar
   * /../                 -->   null
   * ../foo               -->   null
   * foo/bar/..           -->   foo/
   * foo/../../bar        -->   null
   * foo/../bar           -->   bar
   * </pre>
   * 
   * @param filename
   *          the filename to normalize, null returns null
   * @param leadingSlash
   *          will ensue there is a leading slash on the result if {@code true}
   * @return the normalized filename, or null if invalid
   */
  public static String normalize( final String filename, final boolean leadingSlash ) {
    String normalizedFilename = null;
    if ( filename != null ) {
      normalizedFilename = normalize( filename.trim() );
      if ( leadingSlash && normalizedFilename != null && normalizedFilename.indexOf( RepositoryFile.SEPARATOR ) != 0 ) {
        normalizedFilename = RepositoryFile.SEPARATOR + normalizedFilename;
      }
    }
    return normalizedFilename;
  }

  // -----------------------------------------------------------------------

  /**
   * Normalizes a path, removing double and single dot path steps, and removing any final directory separator.
   * <p/>
   * This method normalizes a path to a standard format.
   * <p/>
   * A trailing slash will be removed. A double slash will be merged to a single slash (but UNC names are handled).
   * A single dot path segment will be removed. A double dot will cause that path segment and the one before to be
   * removed. If the double dot has no parent path segment to work with, <code>null</code> is returned.
   * <p/>
   * The output will be the same on both Unix and Windows except for the separator character.
   * 
   * <pre>
   * /foo//               -->   /foo
   * /foo/./              -->   /foo
   * /foo/../bar          -->   /bar
   * /foo/../bar/         -->   /bar
   * /foo/../bar/../baz   -->   /baz
   * //foo//./bar         -->   /foo/bar
   * /../                 -->   null
   * ../foo               -->   null
   * foo/bar/..           -->   foo
   * foo/../../bar        -->   null
   * foo/../bar           -->   bar
   * </pre>
   * 
   * @param filename
   *          the filename to normalize, null returns null
   * @return the normalized filename, or null if invalid
   */
  public static String normalizeNoEndSeparator( final String filename ) {
    return FilenameUtils.normalizeNoEndSeparator( filename, true );
  }

  // -----------------------------------------------------------------------

  /**
   * Concatenates a filename to a base path using normal command line style rules.
   * <p/>
   * The effect is equivalent to resultant directory after changing directory to the first argument, followed by
   * changing directory to the second argument.
   * <p/>
   * The first argument is the base path, the second is the path to concatenate. The returned path is always
   * normalized via {@link #normalize(String)}, thus <code>..</code> is handled.
   * <p/>
   * If <code>pathToAdd</code> is absolute (has an absolute prefix), then it will be normalized and returned.
   * Otherwise, the paths will be joined, normalized and returned.
   * <p/>
   * 
   * <pre>
   * /foo/ + bar          -->   /foo/bar
   * /foo + bar           -->   /foo/bar
   * /foo + /bar          -->   /bar
   * /foo/a/ + ../bar     -->   foo/bar
   * /foo/ + ../../bar    -->   null
   * /foo/ + /bar         -->   /bar
   * /foo/.. + /bar       -->   /bar
   * /foo + bar/c.txt     -->   /foo/bar/c.txt
   * /foo/c.txt + bar     -->   /foo/c.txt/bar (!)
   * </pre>
   * 
   * (!) Note that the first parameter must be a path. If it ends with a name, then the name will be built into the
   * concatenated path. If this might be a problem, use {@link #getFullPath(String)} on the base path argument.
   * 
   * @param basePath
   *          the base path to attach to, always treated as a path
   * @param fullFilenameToAdd
   *          the filename (or path) to attach to the base
   * @return the concatenated path, or null if invalid
   */
  public static String concat( final String basePath, final String fullFilenameToAdd ) {
    int prefix = 0;
    if(StringUtils.hasLength( fullFilenameToAdd)) {
      prefix = getPrefixLength( fullFilenameToAdd.replace( ":", "_" ) );  
    }
    if ( prefix < 0 ) {
      return null;
    }
    if ( prefix > 0 ) {
      return RepositoryFilenameUtils.normalize( fullFilenameToAdd );
    }
    if ( basePath == null ) {
      return null;
    }
    int len = basePath.length();
    if ( len == 0 ) {
      return RepositoryFilenameUtils.normalize( fullFilenameToAdd );
    }
    char ch = basePath.charAt( len - 1 );
    if ( SEPARATOR == ch ) {
      return RepositoryFilenameUtils.normalize( basePath + fullFilenameToAdd );
    } else {
      return RepositoryFilenameUtils.normalize( basePath + SEPARATOR + fullFilenameToAdd );
    }
  }

  // -----------------------------------------------------------------------

  /**
   * Converts all separators to the Repository (Unix) separator of forward slash.
   * 
   * @param path
   *          the path to be changed, null ignored
   * @return the updated path
   */
  public static String separatorsToRepository( final String path ) {
    return FilenameUtils.separatorsToUnix( path );
  }

  // -----------------------------------------------------------------------

  /**
   * Returns the length of the filename prefix,
   * <p/>
   * The prefix length includes the first slash in the full filename if applicable. Thus, it is possible that the
   * length returned is greater than the length of the input string.
   * 
   * <pre>
   * a/b/c.txt           --> ""          --> relative
   * /a/b/c.txt          --> "/"         --> absolute
   * </pre>
   * <p/>
   * 
   * @param filename
   *          the filename to find the prefix in, null returns -1
   * @return the length of the prefix, -1 if invalid or null
   */
  public static int getPrefixLength( final String filename ) {
    return FilenameUtils.getPrefixLength( filename );
  }

  /**
   * Returns the index of the last directory separator character.
   * <p/>
   * The position of the last forward or backslash is returned.
   * <p/>
   * 
   * @param filename
   *          the filename to find the last path separator in, null returns -1
   * @return the index of the last separator character, or -1 if there is no such character
   */
  public static int indexOfLastSeparator( final String filename ) {
    return FilenameUtils.indexOfLastSeparator( filename );
  }

  /**
   * Returns the index of the last extension separator character, which is a dot.
   * <p/>
   * This method also checks that there is no directory separator after the last dot. To do this it uses
   * {@link #indexOfLastSeparator(String)}
   * <p/>
   * 
   * @param filename
   *          the filename to find the last path separator in, null returns -1
   * @return the index of the last separator character, or -1 if there is no such character
   */
  public static int indexOfExtension( final String filename ) {
    return FilenameUtils.indexOfExtension( filename );
  }

  // -----------------------------------------------------------------------

  /**
   * Gets the prefix from a full filename.
   * <p/>
   * The prefix includes the first slash in the full filename where applicable.
   * 
   * <pre>
   * a/b/c.txt           --> ""          --> relative
   * /a/b/c.txt          --> "/"         --> absolute
   * </pre>
   * <p/>
   * 
   * @param filename
   *          the filename to query, null returns null
   * @return the prefix of the file, null if invalid
   */
  public static String getPrefix( final String filename ) {
    return FilenameUtils.getPrefix( filename );
  }

  /**
   * Gets the path from a full filename, which excludes the prefix.
   * <p/>
   * The method is entirely text based, and returns the text before and including the last forward or backslash.
   * 
   * <pre>
   * a.txt        --> ""
   * a/b/c        --> a/b/
   * a/b/c/       --> a/b/c/
   * /a.txt       --> ""
   * /a/b/c       --> a/b/
   * /a/b/c/      --> a/b/c/
   * </pre>
   * <p/>
   * This method drops the prefix from the result. See {@link #getFullPath(String)} for the method that retains the
   * prefix.
   * 
   * @param filename
   *          the filename to query, null returns null
   * @return the path of the file, an empty string if none exists, null if invalid
   */
  public static String getPath( final String filename ) {
    return FilenameUtils.getPath( filename );
  }

  /**
   * Gets the path from a full filename, which excludes the prefix, and also excluding the final directory
   * separator.
   * <p/>
   * The method is entirely text based, and returns the text before the last forward or backslash.
   * 
   * <pre>
   * a.txt        --> ""
   * a/b/c        --> a/b
   * a/b/c/       --> a/b/c
   * /a.txt       --> ""
   * /a/b/c       --> a/b
   * /a/b/c/      --> a/b/c
   * </pre>
   * <p/>
   * This method drops the prefix from the result. See {@link #getFullPathNoEndSeparator(String)} for the method
   * that retains the prefix.
   * 
   * @param filename
   *          the filename to query, null returns null
   * @return the path of the file, an empty string if none exists, null if invalid
   */
  public static String getPathNoEndSeparator( final String filename ) {
    return FilenameUtils.getPathNoEndSeparator( filename );
  }

  /**
   * Gets the full path from a full filename, which is the prefix + path.
   * <p/>
   * The method is entirely text based, and returns the text before and including the last forward or backslash.
   * 
   * <pre>
   * a.txt        --> ""
   * a/b/c        --> a/b/
   * a/b/c/       --> a/b/c/
   * /a.txt       --> /
   * /a/b/c       --> /a/b/
   * /a/b/c/      --> /a/b/c/
   * </pre>
   * <p/>
   * The output will be the same irrespective of the machine that the code is running on.
   * 
   * @param filename
   *          the filename to query, null returns null
   * @return the path of the file, an empty string if none exists, null if invalid
   */
  public static String getFullPath( final String filename ) {
    return FilenameUtils.getFullPath( filename );
  }

  /**
   * Gets the full path from a full filename, which is the prefix + path, and also excluding the final directory
   * separator.
   * <p/>
   * This method will handle a file in either Unix or Windows format. The method is entirely text based, and
   * returns the text before the last forward or backslash.
   * 
   * <pre>
   * a.txt        --> ""
   * a/b/c        --> a/b
   * a/b/c/       --> a/b/c
   * /a.txt       --> /
   * /a/b/c       --> /a/b
   * /a/b/c/      --> /a/b/c
   * </pre>
   * <p/>
   * The output will be the same irrespective of the machine that the code is running on.
   * 
   * @param filename
   *          the filename to query, null returns null
   * @return the path of the file, an empty string if none exists, null if invalid
   */
  public static String getFullPathNoEndSeparator( final String filename ) {
    return FilenameUtils.getFullPathNoEndSeparator( filename );
  }

  /**
   * Gets the name minus the path from a full filename.
   * <p/>
   * The text after the last forward or backslash is returned.
   * 
   * <pre>
   * a/b/c.txt --> c.txt
   * a.txt     --> a.txt
   * a/b/c     --> c
   * a/b/c/    --> ""
   * </pre>
   * <p/>
   * 
   * @param filename
   *          the filename to query, null returns null
   * @return the name of the file without the path, or an empty string if none exists
   */
  public static String getName( final String filename ) {
    return FilenameUtils.getName( filename );
  }

  /**
   * Gets the base name, minus the full path and extension, from a full filename.
   * <p/>
   * The text after the last forward or backslash and before the last dot is returned.
   * 
   * <pre>
   * a/b/c.txt --> c
   * a.txt     --> a
   * a/b/c     --> c
   * a/b/c/    --> ""
   * </pre>
   * <p/>
   * 
   * @param filename
   *          the filename to query, null returns null
   * @return the name of the file without the path, or an empty string if none exists
   */
  public static String getBaseName( final String filename ) {
    return FilenameUtils.getBaseName( filename );
  }

  /**
   * Gets the extension of a filename.
   * <p/>
   * This method returns the textual part of the filename after the last dot. There must be no directory separator
   * after the dot.
   * 
   * <pre>
   * foo.txt      --> "txt"
   * a/b/c.jpg    --> "jpg"
   * a/b.txt/c    --> ""
   * a/b/c        --> ""
   * </pre>
   * <p/>
   * 
   * @param filename
   *          the filename to retrieve the extension of.
   * @return the extension of the file or an empty string if none exists.
   */
  public static String getExtension( final String filename ) {
    return FilenameUtils.getExtension( filename );
  }

  // -----------------------------------------------------------------------

  /**
   * Removes the extension from a filename.
   * <p/>
   * This method returns the textual part of the filename before the last dot. There must be no directory separator
   * after the dot.
   * 
   * <pre>
   * foo.txt    --> foo
   * a/b/c.jpg  --> a/b/c
   * a/b/c      --> a/b/c
   * a.b/c      --> a.b/c
   * </pre>
   * <p/>
   * 
   * @param filename
   *          the filename to query, null returns null
   * @return the filename minus the extension
   */
  public static String removeExtension( final String filename ) {
    return FilenameUtils.removeExtension( filename );
  }

  // -----------------------------------------------------------------------

  /**
   * Checks whether two filenames are equal exactly.
   * <p/>
   * No processing is performed on the filenames other than comparison, thus this is merely a null-safe
   * case-sensitive equals.
   * 
   * @param filename1
   *          the first filename to query, may be null
   * @param filename2
   *          the second filename to query, may be null
   * @return true if the filenames are equal, null equals null
   * @see org.apache.commons.io.IOCase#SENSITIVE
   */
  public static boolean equals( final String filename1, final String filename2 ) {
    return FilenameUtils.equals( filename1, filename2, false, IOCase.SENSITIVE );
  }

  // -----------------------------------------------------------------------

  /**
   * Checks whether two filenames are equal after both have been normalized.
   * <p/>
   * Both filenames are first passed to {@link #normalize(String)}. The check is then performed in a case-sensitive
   * manner.
   * 
   * @param filename1
   *          the first filename to query, may be null
   * @param filename2
   *          the second filename to query, may be null
   * @return true if the filenames are equal, null equals null
   * @see IOCase#SENSITIVE
   */
  public static boolean equalsNormalized( String filename1, String filename2 ) {
    return FilenameUtils.equals( filename1, filename2, true, IOCase.SENSITIVE );
  }

  // -----------------------------------------------------------------------

  /**
   * Checks whether the extension of the filename is that specified.
   * <p/>
   * This method obtains the extension as the textual part of the filename after the last dot. There must be no
   * directory separator after the dot. The extension check is case-sensitive on all platforms.
   * 
   * @param filename
   *          the filename to query, null returns false
   * @param extension
   *          the extension to check for, null or empty checks for no extension
   * @return true if the filename has the specified extension
   */
  public static boolean isExtension( final String filename, final String extension ) {
    return FilenameUtils.isExtension( filename, extension );
  }

  /**
   * Checks whether the extension of the filename is one of those specified.
   * <p/>
   * This method obtains the extension as the textual part of the filename after the last dot. There must be no
   * directory separator after the dot. The extension check is case-sensitive on all platforms.
   * 
   * @param filename
   *          the filename to query, null returns false
   * @param extensions
   *          the extensions to check for, null checks for no extension
   * @return true if the filename is one of the extensions
   */
  public static boolean isExtension( final String filename, final String[] extensions ) {
    return FilenameUtils.isExtension( filename, extensions );
  }

  /**
   * Checks whether the extension of the filename is one of those specified.
   * <p/>
   * This method obtains the extension as the textual part of the filename after the last dot. There must be no
   * directory separator after the dot. The extension check is case-sensitive on all platforms.
   * 
   * @param filename
   *          the filename to query, null returns false
   * @param extensions
   *          the extensions to check for, null checks for no extension
   * @return true if the filename is one of the extensions
   */
  public static boolean isExtension( final String filename, final Collection extensions ) {
    return FilenameUtils.isExtension( filename, extensions );
  }

  // -----------------------------------------------------------------------

  /**
   * Checks a filename to see if it matches the specified wildcard matcher, always testing case-sensitive.
   * <p/>
   * The wildcard matcher uses the characters '?' and '*' to represent a single or multiple wildcard characters.
   * This is the same as often found on Dos/Unix command lines. The check is case-sensitive always.
   * 
   * <pre>
   * wildcardMatch("c.txt", "*.txt")      --> true
   * wildcardMatch("c.txt", "*.jpg")      --> false
   * wildcardMatch("a/b/c.txt", "a/b/*")  --> true
   * wildcardMatch("c.txt", "*.???")      --> true
   * wildcardMatch("c.txt", "*.????")     --> false
   * </pre>
   * 
   * @param filename
   *          the filename to match on
   * @param wildcardMatcher
   *          the wildcard string to match against
   * @return true if the filename matches the wildcard string
   * @see IOCase#SENSITIVE
   */
  public static boolean wildcardMatch( final String filename, final String wildcardMatcher ) {
    return FilenameUtils.wildcardMatch( filename, wildcardMatcher, IOCase.SENSITIVE );
  }

  /**
   * Performs percent-encoding (as specified in {@code IUnifiedRepository}) on given {@code name}, only encoding
   * the characters given in {@code reservedChars}. Assumes only ASCII characters in reservedChars.
   * 
   * @param name
   *          name to escape
   * @param reservedChars
   *          chars within name to escape
   * @return escaped name
   */
  public static String escape( final String name, final List<Character> reservedChars ) {
    if ( name == null || reservedChars == null ) {
      throw new IllegalArgumentException();
    }
    if ( reservedChars.contains( escapeChar ) ) { // we can't use % as escape char if it is illegal
      throw new IllegalArgumentException();
    }
    List<Character> mergedReservedChars = new ArrayList<Character>( reservedChars );
    mergedReservedChars.add( escapeChar ); // have to have this one
    StringBuilder buffer = new StringBuilder( name.length() * 2 );
    for ( int i = 0; i < name.length(); i++ ) {
      char ch = name.charAt( i );
      if ( mergedReservedChars.contains( ch ) ) {
        buffer.append( escapeChar );
        buffer.append( Character.toUpperCase( Character.forDigit( ch / 16, 16 ) ) );
        buffer.append( Character.toUpperCase( Character.forDigit( ch % 16, 16 ) ) );
      } else {
        buffer.append( ch );
      }
    }
    return buffer.toString();
  }

  /**
   * Reverts modifications of {@link #escape(String)} such that for all {@code String}s {@code t},
   * {@code t.equals(unescape(escape(t)))}. Assumes only ASCII characters have been escaped.
   * 
   * @param name
   *          name to unescape
   * @return unescaped name
   */
  public static String unescape( final String name ) {
    if ( name == null ) {
      throw new IllegalArgumentException();
    }
    StringBuilder buffer = new StringBuilder( name.length() );
    String str = name;
    int i = str.indexOf( escapeChar );
    while ( i > -1 && i + 2 < str.length() ) {
      buffer.append( str.toCharArray(), 0, i );
      int a = Character.digit( str.charAt( i + 1 ), 16 );
      int b = Character.digit( str.charAt( i + 2 ), 16 );
      if ( a > -1 && b > -1 ) {
        buffer.append( (char) ( a * 16 + b ) );
        str = str.substring( i + 3 );
      } else {
        buffer.append( escapeChar );
        str = str.substring( i + 1 );
      }
      i = str.indexOf( escapeChar );
    }
    buffer.append( str );
    return buffer.toString();
  }
}
