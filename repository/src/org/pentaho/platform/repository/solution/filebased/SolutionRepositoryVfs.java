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

package org.pentaho.platform.repository.solution.filebased;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.FileProvider;

import java.util.Collection;

public class SolutionRepositoryVfs implements FileProvider {

  public SolutionRepositoryVfs() {
    super();
  }

  public FileObject findFile( final FileObject baseFile, final String uri, final FileSystemOptions arg2 )
    throws FileSystemException {

    SolutionRepositoryVfsFileObject fileInfo = null;
    // for now assume that all URIs are absolute and we don't handle compound URIs
    if ( uri != null ) {
      // this is a fully qualified file path
      int pos = uri.indexOf( ':' );
      String solutionPath = uri.substring( pos + 1 );
      fileInfo = new SolutionRepositoryVfsFileObject( solutionPath );
    }
    return fileInfo;
  }

  public FileObject createFileSystem( final String arg0, final FileObject arg1, final FileSystemOptions arg2 )
    throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileSystemConfigBuilder getConfigBuilder() {
    // not needed for our usage
    return null;
  }

  public Collection getCapabilities() {
    // not needed for our usage
    return null;
  }

  public FileName parseUri( final FileName arg0, final String arg1 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

}
