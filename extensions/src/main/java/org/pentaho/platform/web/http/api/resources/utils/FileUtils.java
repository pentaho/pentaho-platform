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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources.utils;

import com.google.gwt.regexp.shared.RegExp;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.util.RepositoryPathEncoder;
import org.pentaho.platform.web.http.messages.Messages;

import java.io.FileInputStream;

public class FileUtils {
  public static final String PATH_SEPARATOR = "/";
  public static final String ENCODED_PATH_SEPARATOR = ":";
  private static final Log logger = LogFactory.getLog( FileUtils.class );
  private static final RegExp containsControlCharactersPattern = RegExp.compile( "[\\x00-\\x1F\\x7F]" );

  public static String idToPath( String pathId ) {
    // slashes in pathId are illegal.. we scrub them out so the file will not be found
    // if the pathId was given in slash separated format
    if ( pathId.contains( PATH_SEPARATOR ) ) {
      logger.warn( Messages.getInstance().getString( "FileResource.ILLEGAL_PATHID", pathId ) );
    }
    String path = pathId.replace( PATH_SEPARATOR, ENCODED_PATH_SEPARATOR );
    path = RepositoryPathEncoder.decodeRepositoryPath( path );
    if ( !path.startsWith( PATH_SEPARATOR ) ) {
      path = PATH_SEPARATOR + path;
    }
    return path;
  }

  /**
   * Gracefully closes a fileInputStream
   * @param fileInputStream The {@link FileInputStream}
   * @return returns true if the file input stream is successfully closed, false otherwise
   */
  public static boolean closeQuietly( FileInputStream fileInputStream ) {
    if ( fileInputStream != null ) {
      try {
        fileInputStream.close();
      } catch ( Exception e ) {
        logger.error( e.getMessage() );
        return false;
      }
      return true;
    }
    return false;
  }

  /**
   * Checks whether {@code path} contains any of {@code reserved}.
   *
   * @param path unix-style path
   * @param reserved array of reserved characters
   * @return {@code true} if any of {@code reserved} is contained by {@code path}
   */
  public static boolean containsReservedCharacter( String path, char[] reserved ) {
    while ( !path.isEmpty() ) {
      String name = FilenameUtils.getName( path );
      if ( StringUtils.containsAny( name, reserved ) ) {
        return true;
      }
      path = FilenameUtils.getPathNoEndSeparator( path );
    }
    return false;
  }

  /**
   * Checks whether {@code path} contains any of Control Characters
   *
   * @param path to be validated
   * @return {@code true} if any of {@code Control Characters} is contained in {@code path}
   */
  public static boolean containsControlCharacters( String path ) {
    return containsControlCharactersPattern.test( path );
  }

  public static String[] convertCommaSeparatedStringToArray( String stringToConvert ) {
    if ( stringToConvert == null || stringToConvert.isEmpty() ) {
      throw new IllegalArgumentException( "String cannot be null or empty" );
    }

    return stringToConvert.split( "[,]" );
  }

  public static String getParentPath( final String path ) {
    if ( path == null || path.length() == 0 || path.indexOf( '/' ) == -1 ) {
      throw new IllegalArgumentException();
    } else if ( path.equals( PATH_SEPARATOR ) ) {
      return null;
    }

    int index = trimTrailingSlash( path ).lastIndexOf( PATH_SEPARATOR );

    if ( index == -1 ) {
      return null;
    } else if ( index == 0 ) {
      return PATH_SEPARATOR;
    } else {
      return path.substring( 0, index );
    }
  }

  public static boolean isRootLevelPath( final String path ) {
    if ( path == null || path.length() == 0 || !path.startsWith( PATH_SEPARATOR ) ) {
      // the path must be an absolute path like stated in the documentation for the REST API
      throw new IllegalArgumentException();
    } else if ( path.equals( PATH_SEPARATOR ) ) {
      return true;
    } else if ( trimTrailingSlash( path ).startsWith( ClientRepositoryPaths.getPublicFolderPath() + PATH_SEPARATOR )
      || trimTrailingSlash( path ).startsWith( ClientRepositoryPaths.getHomeFolderPath() + PATH_SEPARATOR )
      || trimTrailingSlash( path ).startsWith( ClientRepositoryPaths.getEtcFolderPath() + PATH_SEPARATOR ) ) {
      return false;
    }

    return true;
  }

  private static String trimTrailingSlash( String path ) {
    return path.endsWith( PATH_SEPARATOR ) ? path.substring( 0, path.length() - 2 ) : path;
  }
}
