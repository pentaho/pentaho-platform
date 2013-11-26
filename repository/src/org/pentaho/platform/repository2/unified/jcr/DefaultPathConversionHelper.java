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
  public String absToRel( final String absPath ) {
    Assert.hasLength( absPath );
    Assert.isTrue( absPath.startsWith( RepositoryFile.SEPARATOR ) );
    if ( ( ServerRepositoryPaths.getTenantRootFolderPath() != null )
        && absPath.startsWith( ServerRepositoryPaths.getTenantRootFolderPath() ) ) {
      String tmpPath = absPath.substring( ServerRepositoryPaths.getTenantRootFolderPath().length() );
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
  public String relToAbs( final String relPath ) {
    Assert.hasLength( relPath );
    Assert.isTrue( relPath.startsWith( RepositoryFile.SEPARATOR ) );
    return ServerRepositoryPaths.getTenantRootFolderPath()
        + ( RepositoryFile.SEPARATOR.equals( relPath ) ? "" : relPath ); //$NON-NLS-1$
  }

}
