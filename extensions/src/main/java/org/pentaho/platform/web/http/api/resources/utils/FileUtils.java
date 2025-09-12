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
   * @param path     unix-style path
   * @param reserved array of reserved characters
   * @return {@code true} if any of {@code reserved} is contained by {@code path}
   */
  public static boolean containsReservedCharacter( String path, char[] reserved ) {
    return containsReservedCharacter( path, reserved, false );
  }

  /**
   * Checks whether {@code nameOrPath} contains any of {@code reserved}.
   *
   * @param nameOrPath unix-style path or name
   * @param reserved   array of reserved characters
   * @param strict     if true, only the last segment of the path (the name) is checked
   * @return {@code true} if any of {@code reserved} is contained by {@code path}
   */
  public static boolean containsReservedCharacter( String nameOrPath, char[] reserved, boolean strict ) {
    if ( strict ) {
      return nameContainsReservedCharacter( nameOrPath, reserved );
    }

    while ( !nameOrPath.isEmpty() ) {
      final String name = FilenameUtils.getName( nameOrPath );

      if ( nameContainsReservedCharacter( name, reserved ) ) {
        return true;
      }

      nameOrPath = FilenameUtils.getPathNoEndSeparator( nameOrPath );
    }

    return false;
  }

  public static boolean nameContainsReservedCharacter( String name, char[] reserved ) {
    return StringUtils.containsAny( name, reserved );
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
