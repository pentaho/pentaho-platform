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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
