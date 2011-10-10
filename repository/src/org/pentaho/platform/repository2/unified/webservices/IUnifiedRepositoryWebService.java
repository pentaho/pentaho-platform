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

import javax.jws.WebService;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * JAX-WS-safe version of {@code IUnifiedRepositoryService}.
 * 
 * <ul>
 * <li>No method overloading (e.g. getFile(id, versionId) becomes getFileAtVersion(id, versionId).</li>
 * <li>No interfaces (e.g. Serializable becomes String).</li>
 * <li>No references to public static inner classes.</li>
 * </ul>
 * 
 * @author mlowery
 */
@WebService
public interface IUnifiedRepositoryWebService extends RemoteService {

  RepositoryFileDto getFile(final String path);

  RepositoryFileDto getFileById(final String fileId);

  NodeRepositoryFileDataDto getDataAsNodeForRead(final String fileId);

  List<NodeRepositoryFileDataDto> getDataAsNodeForReadInBatch(final List<RepositoryFileDto> files);

  RepositoryFileDto createFile(final String parentFolderId, final RepositoryFileDto file,
      final NodeRepositoryFileDataDto data, final String versionMessage);

  RepositoryFileDto createFileWithAcl(final String parentFolderId, final RepositoryFileDto file,
      final NodeRepositoryFileDataDto data, final RepositoryFileAclDto acl, final String versionMessage);

  RepositoryFileDto createFolder(final String parentFolderId, final RepositoryFileDto file, final String versionMessage);

  RepositoryFileDto createFolderWithAcl(final String parentFolderId, final RepositoryFileDto file,
      final RepositoryFileAclDto acl, final String versionMessage);

  List<RepositoryFileDto> getChildren(final String folderId);

  List<RepositoryFileDto> getChildrenWithFilter(final String folderId, final String filter);

  RepositoryFileDto updateFile(final RepositoryFileDto file, final NodeRepositoryFileDataDto data,
      final String versionMessage);

  void deleteFileWithPermanentFlag(final String fileId, final boolean permanent, final String versionMessage);

  void deleteFile(final String fileId, final String versionMessage);

  void deleteFileAtVersion(final String fileId, final String versionId);

  void undeleteFile(final String fileId, final String versionMessage);

  List<RepositoryFileDto> getDeletedFilesInFolder(final String folderPath);

  List<RepositoryFileDto> getDeletedFilesInFolderWithFilter(final String folderPath, final String filter);

  List<RepositoryFileDto> getDeletedFiles();

  void moveFile(final String fileId, final String destAbsPath, final String versionMessage);

  void copyFile(final String string, final String destAbsPath, final String versionMessage);
  
  void lockFile(final String fileId, final String message);

  void unlockFile(final String fileId);
 
  RepositoryFileTreeDto getTree(final String path, final int depth, final String filter, final boolean showHidden);

  RepositoryFileAclDto getAcl(final String fileId);

  RepositoryFileAclDto updateAcl(final RepositoryFileAclDto acl);

  boolean hasAccess(final String path, final List<Integer> permissions);

  List<RepositoryFileAclAceDto> getEffectiveAces(final String fileId);

  List<RepositoryFileAclAceDto> getEffectiveAcesWithForceFlag(final String fileId, final boolean forceEntriesInheriting);

  NodeRepositoryFileDataDto getDataAsNodeForReadAtVersion(final String fileId, final String versionId);

  VersionSummaryDto getVersionSummary(String fileId, String versionId);
  
  List<VersionSummaryDto> getVersionSummaryInBatch(final List<RepositoryFileDto> files);

  List<VersionSummaryDto> getVersionSummaries(final String fileId);

  RepositoryFileDto getFileAtVersion(final String fileId, final String versionId);

  void restoreFileAtVersion(final String fileId, final String versionId, final String versionMessage);

  boolean canUnlockFile(final String fileId);

  List<RepositoryFileDto> getReferrers(final String fileId);
  
//  void setFileMetadata(final Serializable fileId, FileMetadataMap fileMetadataMap);
//  
//  FileMetadataMap getFileMetadata(final Serializable fileId);
}
