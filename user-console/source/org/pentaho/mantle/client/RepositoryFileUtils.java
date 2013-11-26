/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileTreeDto;

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
    repositoryFile.setCreatedDate( file.getCreatedDate() );
    repositoryFile.setDeletedDate( file.getDeletedDate() );
    repositoryFile.setDescription( file.getDescription() );
    repositoryFile.setFolder( file.isFolder() );
    repositoryFile.setHidden( file.isHidden() );
    repositoryFile.setId( file.getId() );
    repositoryFile.setLastModifiedDate( file.getLastModifiedDate() );
    repositoryFile.setLocale( file.getLocale() );
    repositoryFile.setLockDate( file.getLockDate() );
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
    repositoryFile.setCreatedDate( file.getCreatedDate() );
    repositoryFile.setDeletedDate( file.getDeletedDate() );
    repositoryFile.setDescription( file.getDescription() );
    repositoryFile.setFolder( file.isFolder() );
    repositoryFile.setHidden( file.isHidden() );
    repositoryFile.setId( file.getId() );
    repositoryFile.setLastModifiedDate( file.getLastModifiedDate() );
    repositoryFile.setLocale( file.getLocale() );
    repositoryFile.setLockDate( file.getLockDate() );
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
