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


package org.pentaho.platform.plugin.services.importer;

import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

import java.util.List;

public class PDIImportFileHandler extends RepositoryFileImportFileHandler implements IPlatformImportHandler {

  public PDIImportFileHandler( List<IMimeType> mimeTypes ) {
    super( mimeTypes );
  }

  @Override
  protected RepositoryFile createFile( final RepositoryFileImportBundle bundle, final String repositoryPath,
                                       final IRepositoryFileData data ) throws PlatformImportException {

    String originalName = bundle.getName();
    bundle.setName( PentahoPlatformImporter.checkAndSanitize( originalName ) );
    bundle.setTitle( originalName );

    return super.createFile( bundle, repositoryPath, data );
  }

  @Override
  protected RepositoryFile updateFile( final RepositoryFileImportBundle bundle, final RepositoryFile file,
                                       final IRepositoryFileData data ) throws PlatformImportException {
    RepositoryFile updatedFile = null;
    if ( isNodeRepositoryFileData( file ) ) {
      updatedFile = getRepository().updateFile( file, data, bundle.getComment() );
    } else {
      String fileName = bundle.getName();
      getLogger().trace( "The file [" + fileName + "] will be recreated because it content-type was changed." );
      RepositoryFileAcl originFileAcl = getRepository().getAcl( file.getId() );
      getRepository().deleteFile( file.getId(), true, null );

      RepositoryFileAcl newFileAcl = bundle.getAcl();
      bundle.setAcl( originFileAcl );
      bundle.setExtraMetaData( bundle.getExtraMetaData() );
      updatedFile = createFile( bundle, file.getPath(), data );
      bundle.setAcl( newFileAcl );
    }
    return updatedFile;
  }

  private boolean isNodeRepositoryFileData( final RepositoryFile file ) {
    try {
      getRepository().getDataForRead( file.getId(), NodeRepositoryFileData.class );
      return true;
    } catch ( UnifiedRepositoryException e ) {
      return false;
    }
  }
}
