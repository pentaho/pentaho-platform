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


package org.pentaho.platform.repository.solution.filebased;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileProvider;

import java.util.Collection;

public class SolutionRepositoryVfs extends AbstractFileProvider {

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
