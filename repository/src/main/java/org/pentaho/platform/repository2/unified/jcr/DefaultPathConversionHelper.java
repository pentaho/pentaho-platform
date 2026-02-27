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
    Assert.hasLength( absPath, "Absolute path must not be null or empty" );
    Assert.isTrue( absPath.startsWith( RepositoryFile.SEPARATOR ), "Absolute path must start with the repository separator" );
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
    Assert.hasLength( relPath, "Relative path must not be null or empty" );
    Assert.isTrue( relPath.startsWith( RepositoryFile.SEPARATOR ), "Relative path must start with the repository separator" );
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
