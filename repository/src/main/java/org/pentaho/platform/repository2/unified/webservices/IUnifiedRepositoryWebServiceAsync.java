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

package org.pentaho.platform.repository2.unified.webservices;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface IUnifiedRepositoryWebServiceAsync {

  void canUnlockFile( String fileId, AsyncCallback<Boolean> arg2 );

  void copyFile( String string, String destAbsPath, String versionMessage, AsyncCallback<Void> arg4 );

  void createFile( String parentFolderId, RepositoryFileDto file, NodeRepositoryFileDataDto data,
      String versionMessage, AsyncCallback<RepositoryFileDto> arg5 );

  void createFileWithAcl( String parentFolderId, RepositoryFileDto file, NodeRepositoryFileDataDto data,
      RepositoryFileAclDto acl, String versionMessage, AsyncCallback<RepositoryFileDto> arg6 );

  void createFolder( String parentFolderId, RepositoryFileDto file, String versionMessage,
      AsyncCallback<RepositoryFileDto> arg4 );

  void createFolderWithAcl( String parentFolderId, RepositoryFileDto file, RepositoryFileAclDto acl,
      String versionMessage, AsyncCallback<RepositoryFileDto> arg5 );

  void deleteFile( String fileId, String versionMessage, AsyncCallback<Void> arg3 );

  void deleteFileAtVersion( String fileId, String versionId, AsyncCallback<Void> arg3 );

  void deleteFileWithPermanentFlag( String fileId, boolean permanent, String versionMessage, AsyncCallback<Void> arg4 );

  void getAcl( String fileId, AsyncCallback<RepositoryFileAclDto> arg2 );

  void getChildren( String folderId, AsyncCallback<List<RepositoryFileDto>> arg2 );

  void getChildrenWithFilter( String folderId, String filter, AsyncCallback<List<RepositoryFileDto>> arg3 );

  void getDataAsNodeForRead( String fileId, AsyncCallback<NodeRepositoryFileDataDto> arg2 );

  void getDataAsNodeForReadAtVersion( String fileId, String versionId, AsyncCallback<NodeRepositoryFileDataDto> arg3 );

  void getDataAsNodeForReadInBatch( List<RepositoryFileDto> files,
                                    AsyncCallback<List<NodeRepositoryFileDataDto>> arg2 );

  void getDeletedFiles( AsyncCallback<List<RepositoryFileDto>> arg1 );

  void getDeletedFilesInFolder( String folderPath, AsyncCallback<List<RepositoryFileDto>> arg2 );

  void
  getDeletedFilesInFolderWithFilter( String folderPath, String filter, AsyncCallback<List<RepositoryFileDto>> arg3 );

  void getEffectiveAces( String fileId, AsyncCallback<List<RepositoryFileAclAceDto>> arg2 );

  void getEffectiveAcesWithForceFlag( String fileId, boolean forceEntriesInheriting,
      AsyncCallback<List<RepositoryFileAclAceDto>> arg3 );

  void getFile( String path, AsyncCallback<RepositoryFileDto> arg2 );

  void getFileAtVersion( String fileId, String versionId, AsyncCallback<RepositoryFileDto> arg3 );

  void getFileById( String fileId, AsyncCallback<RepositoryFileDto> arg2 );

  void getFileMetadata( String fileId, AsyncCallback<List<StringKeyStringValueDto>> arg2 );

  void getReferrers( String fileId, AsyncCallback<List<RepositoryFileDto>> arg2 );

  void getReservedChars( AsyncCallback<List<Character>> arg1 );

  void getTree( String path, int depth, String filter, boolean showHidden, AsyncCallback<RepositoryFileTreeDto> arg5 );

  void getVersionSummaries( String fileId, AsyncCallback<List<VersionSummaryDto>> arg2 );

  void getVersionSummary( String fileId, String versionId, AsyncCallback<VersionSummaryDto> arg3 );

  void getVersionSummaryInBatch( List<RepositoryFileDto> files, AsyncCallback<List<VersionSummaryDto>> arg2 );

  void hasAccess( String path, List<Integer> permissions, AsyncCallback<Boolean> arg3 );

  void lockFile( String fileId, String message, AsyncCallback<Void> arg3 );

  void moveFile( String fileId, String destAbsPath, String versionMessage, AsyncCallback<Void> arg4 );

  void restoreFileAtVersion( String fileId, String versionId, String versionMessage, AsyncCallback<Void> arg4 );

  void setFileMetadata( String fileId, List<StringKeyStringValueDto> fileMetadataMap, AsyncCallback<Void> arg3 );

  void undeleteFile( String fileId, String versionMessage, AsyncCallback<Void> arg3 );

  void unlockFile( String fileId, AsyncCallback<Void> arg2 );

  void updateAcl( RepositoryFileAclDto acl, AsyncCallback<RepositoryFileAclDto> arg2 );

  void updateFile( RepositoryFileDto file, NodeRepositoryFileDataDto data, String versionMessage,
      AsyncCallback<RepositoryFileDto> arg4 );
}
