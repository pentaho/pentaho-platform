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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.springframework.util.Assert;

/**
 * Default {@link IPathConversionHelper} implementation. Uses {@link ServerRepositoryPaths}.
 * 
 * @author mlowery
 */
public class DefaultPathConversionHelper implements IPathConversionHelper {

  /**
   * Returns null if path is not at or under the tenant root.
   */
  @Override
  public String absToRel( final String absPath ) {
    Assert.hasLength( absPath );
    Assert.isTrue( absPath.startsWith( RepositoryFile.SEPARATOR ) );
    String convertedAbsPath = convertPathSlashes( absPath );
    if ( ( ServerRepositoryPaths.getTenantRootFolderPath() != null )
        && convertedAbsPath.startsWith( ServerRepositoryPaths.getTenantRootFolderPath() ) ) {
      String tmpPath = convertedAbsPath.substring( ServerRepositoryPaths.getTenantRootFolderPath().length() );
      if ( "".equals( tmpPath ) ) { //$NON-NLS-1$
        return RepositoryFile.SEPARATOR;
      } else {
        return tmpPath;
      }
    } else {
      // might be running as repository admin user so tenant is not applicable
      return null;
    }
  }

  /**
   * Unconditionally adds tenant root path to given path.
   */
  @Override
  public String relToAbs( final String relPath ) {
    Assert.hasLength( relPath );
    Assert.isTrue( relPath.startsWith( RepositoryFile.SEPARATOR ) );
    String convertedRelPath = convertPathSlashes( relPath );
    return ServerRepositoryPaths.getTenantRootFolderPath()
        + ( RepositoryFile.SEPARATOR.equals( convertedRelPath ) ? "" : convertedRelPath ); //$NON-NLS-1$
  }

  /**
   * Converts a path string with backslashes into a path string with the repository separator
   * @param path the path string
   * @return a new path string with backslashes converted to repository separators
   */
  private String convertPathSlashes( final String path ) {
    return path.replace( "\\", RepositoryFile.SEPARATOR );
  }

}
