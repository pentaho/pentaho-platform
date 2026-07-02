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


package org.pentaho.mantle.client;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAdapter;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;

import java.util.ArrayList;
import java.util.List;

public class RepositoryFileUtils {

  public static RepositoryFileTree convertToRepositoryFileTree( RepositoryFileTreeDto tree ) {
    RepositoryFileTree fileTree = new RepositoryFileTree();
    List<RepositoryFileTree> fileList = new ArrayList<RepositoryFileTree>();
    RepositoryFile file = convertToRepositoryFile( tree.getFile() );
    fileTree.setFile( file );
    for ( RepositoryFileTreeDto treeItem : tree.getChildren() ) {
      fileList.add( convertToRepositoryFileTree( treeItem ) );
    }
    fileTree.setChildren( fileList );
    return fileTree;
  }

  public static RepositoryFile convertToRepositoryFile( RepositoryFileDto file ) {
    RepositoryFile repositoryFile = new RepositoryFile();
    repositoryFile.setCreatedDate( RepositoryFileAdapter.unmarshalDate( file.getCreatedDate() ) );
    repositoryFile.setDeletedDate( RepositoryFileAdapter.unmarshalDate( file.getDeletedDate() ) );
    repositoryFile.setDescription( file.getDescription() );
    repositoryFile.setFolder( file.isFolder() );
    repositoryFile.setHidden( file.isHidden() );
    repositoryFile.setId( file.getId() );
    repositoryFile.setLastModifiedDate( RepositoryFileAdapter.unmarshalDate( file.getLastModifiedDate() ) );
    repositoryFile.setLocale( file.getLocale() );
    repositoryFile.setLockDate( RepositoryFileAdapter.unmarshalDate( file.getLockDate() ) );
    repositoryFile.setLocked( file.isLocked() );
    repositoryFile.setLockMessage( file.getLockMessage() );
    repositoryFile.setLockOwner( file.getLockOwner() );
    repositoryFile.setName( file.getName() );
    repositoryFile.setOriginalParentFolderId( file.getOriginalParentFolderPath() );
    repositoryFile.setOriginalParentFolderPath( file.getOriginalParentFolderPath() );
    repositoryFile.setOwner( file.getOwner() );
    repositoryFile.setPath( file.getPath() );
    repositoryFile.setTitle( file.getTitle() );
    repositoryFile.setVersionId( file.getVersionId() );
    return repositoryFile;
  }

  public static RepositoryFileTreeDto convertFromRepositoryFileTree( RepositoryFileTree tree ) {
    RepositoryFileTreeDto fileTreeDto = new RepositoryFileTreeDto();
    List<RepositoryFileTreeDto> fileList = new ArrayList<RepositoryFileTreeDto>();
    RepositoryFileDto file = convertFromRepositoryFile( tree.getFile() );
    fileTreeDto.setFile( file );
    for ( RepositoryFileTree treeItem : tree.getChildren() ) {
      fileList.add( convertFromRepositoryFileTree( treeItem ) );
    }
    fileTreeDto.setChildren( fileList );
    return fileTreeDto;
  }

  public static RepositoryFileDto convertFromRepositoryFile( RepositoryFile file ) {
    RepositoryFileDto repositoryFile = new RepositoryFileDto();
    repositoryFile.setCreatedDate( RepositoryFileAdapter.marshalDate( file.getCreatedDate() ) );
    repositoryFile.setDeletedDate( RepositoryFileAdapter.marshalDate( file.getDeletedDate() ) );
    repositoryFile.setDescription( file.getDescription() );
    repositoryFile.setFolder( file.isFolder() );
    repositoryFile.setHidden( file.isHidden() );
    repositoryFile.setAclNode( false );
    repositoryFile.setId( file.getId() );
    repositoryFile.setLastModifiedDate( RepositoryFileAdapter.marshalDate( file.getLastModifiedDate() ) );
    repositoryFile.setLocale( file.getLocale() );
    repositoryFile.setLockDate( RepositoryFileAdapter.marshalDate( file.getLockDate() ) );
    repositoryFile.setLocked( file.isLocked() );
    repositoryFile.setLockMessage( file.getLockMessage() );
    repositoryFile.setLockOwner( file.getLockOwner() );
    repositoryFile.setName( file.getName() );
    repositoryFile.setOriginalParentFolderPath( file.getOriginalParentFolderPath() );
    repositoryFile.setOriginalParentFolderPath( file.getOriginalParentFolderPath() );
    repositoryFile.setOwner( file.getOwner() );
    repositoryFile.setPath( file.getPath() );
    repositoryFile.setTitle( file.getTitle() );
    repositoryFile.setVersionId( file.getVersionId() );
    return repositoryFile;
  }
}
