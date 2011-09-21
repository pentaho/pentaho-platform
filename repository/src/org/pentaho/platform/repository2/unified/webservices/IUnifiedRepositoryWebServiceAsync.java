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
 */
package org.pentaho.platform.repository2.unified.webservices;



import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IUnifiedRepositoryWebServiceAsync {

  void getFile(java.lang.String path, AsyncCallback<RepositoryFileDto> callback);

  void getFileById(java.lang.String fileId, AsyncCallback<RepositoryFileDto> callback);

  void getDataAsNodeForRead(java.lang.String fileId, AsyncCallback<NodeRepositoryFileDataDto> callback);

  void getDataAsNodeForReadInBatch(final java.util.List<RepositoryFileDto> files, AsyncCallback<java.util.List<NodeRepositoryFileDataDto>> callback);
  
  void createFile(java.lang.String parentFolderId, RepositoryFileDto file, NodeRepositoryFileDataDto data, java.lang.String versionMessage, AsyncCallback<RepositoryFileDto> callback);

  void createFileWithAcl(java.lang.String parentFolderId, RepositoryFileDto file, NodeRepositoryFileDataDto data, RepositoryFileAclDto acl, java.lang.String versionMessage, AsyncCallback<RepositoryFileDto> callback);

  void createFolder(java.lang.String parentFolderId, RepositoryFileDto file, java.lang.String versionMessage, AsyncCallback<RepositoryFileDto> callback);

  void createFolderWithAcl(java.lang.String parentFolderId, RepositoryFileDto file, RepositoryFileAclDto acl, java.lang.String versionMessage, AsyncCallback<RepositoryFileDto> callback);

  void getChildren(java.lang.String folderId, AsyncCallback<java.util.List<RepositoryFileDto>> callback);

  void getChildrenWithFilter(java.lang.String folderId, java.lang.String filter, AsyncCallback<java.util.List<RepositoryFileDto>> callback);

  void updateFile(RepositoryFileDto file, NodeRepositoryFileDataDto data, java.lang.String versionMessage, AsyncCallback<RepositoryFileDto> callback);

  void deleteFileWithPermanentFlag(java.lang.String fileId, boolean permanent, java.lang.String versionMessage, AsyncCallback<Void> callback);

  void deleteFile(java.lang.String fileId, java.lang.String versionMessage, AsyncCallback<Void> callback);

  void deleteFileAtVersion(java.lang.String fileId, java.lang.String versionId, AsyncCallback<Void> callback);

  void undeleteFile(java.lang.String fileId, java.lang.String versionMessage, AsyncCallback<Void> callback);

  void getDeletedFilesInFolder(java.lang.String folderPath, AsyncCallback<java.util.List<RepositoryFileDto>> callback);

  void getDeletedFilesInFolderWithFilter(java.lang.String folderPath, java.lang.String filter, AsyncCallback<java.util.List<RepositoryFileDto>> callback);

  void getDeletedFiles(AsyncCallback<java.util.List<RepositoryFileDto>> callback);

  void moveFile(java.lang.String fileId, java.lang.String destAbsPath, java.lang.String versionMessage, AsyncCallback<Void> callback);
  
  void copyFile(java.lang.String fileId, java.lang.String destAbsPath, java.lang.String versionMessage, AsyncCallback<Void> callback);

  void lockFile(java.lang.String fileId, java.lang.String message, AsyncCallback<Void> callback);

  void unlockFile(java.lang.String fileId, AsyncCallback<Void> callback);

  void getTree(java.lang.String path, int depth, java.lang.String filter, AsyncCallback<RepositoryFileTreeDto> callback);

  void getAcl(java.lang.String fileId, AsyncCallback<RepositoryFileAclDto> callback);

  void updateAcl(RepositoryFileAclDto acl, AsyncCallback<RepositoryFileAclDto> callback);

  void hasAccess(java.lang.String path, java.util.List<java.lang.Integer> permissions, AsyncCallback<Boolean> callback);

  void getEffectiveAces(java.lang.String fileId, AsyncCallback<java.util.List<RepositoryFileAclAceDto>> callback);

  void getEffectiveAcesWithForceFlag(java.lang.String fileId, boolean forceEntriesInheriting, AsyncCallback<java.util.List<RepositoryFileAclAceDto>> callback);

  void getDataAsNodeForReadAtVersion(java.lang.String fileId, java.lang.String versionId, AsyncCallback<NodeRepositoryFileDataDto> callback);

  void getVersionSummary(java.lang.String fileId, java.lang.String versionId, AsyncCallback<VersionSummaryDto> callback);

  void getVersionSummaryInBatch(final java.util.List<RepositoryFileDto> files, AsyncCallback<java.util.List<VersionSummaryDto>> callback);
  
  void getVersionSummaries(java.lang.String fileId, AsyncCallback<java.util.List<VersionSummaryDto>> callback);

  void getFileAtVersion(java.lang.String fileId, java.lang.String versionId, AsyncCallback<RepositoryFileDto> callback);

  void restoreFileAtVersion(java.lang.String fileId, java.lang.String versionId, java.lang.String versionMessage, AsyncCallback<Void> callback);

  void canUnlockFile(java.lang.String fileId, AsyncCallback<Boolean> callback);

  void getReferrers(java.lang.String fileId, AsyncCallback<java.util.List<RepositoryFileDto>> fileList);
}
