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
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.util.RepositoryPathEncoder;
import org.pentaho.platform.web.http.messages.Messages;

public class FileUtils {
  public static final String PATH_SEPARATOR = "/";
  public static final String ENCODED_PATH_SEPARATOR = ":";
  private static final Log logger = LogFactory.getLog( FileUtils.class );

  public static String idToPath( String pathId ) {
    // slashes in pathId are illegal.. we scrub them out so the file will not be found
    // if the pathId was given in slash separated format
    if ( pathId.contains( PATH_SEPARATOR ) ) {
      logger.warn( Messages.getInstance().getString( "FileResource.ILLEGAL_PATHID", pathId ) );
    }
    String path = pathId.replaceAll( PATH_SEPARATOR, ENCODED_PATH_SEPARATOR );
    path = RepositoryPathEncoder.decodeRepositoryPath( path );
    if ( !path.startsWith( PATH_SEPARATOR ) ) {
      path = PATH_SEPARATOR + path;
    }
    return path;
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

  public static String[] convertCommaSeparatedStringToArray( String stringToConvert ) {
    if ( stringToConvert == null || stringToConvert.isEmpty() ) {
      throw new IllegalArgumentException( "String cannot be null or empty" );
    }

    return stringToConvert.split( "[,]" );
  }
}
