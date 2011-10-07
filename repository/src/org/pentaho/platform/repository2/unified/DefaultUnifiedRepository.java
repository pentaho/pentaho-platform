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
package org.pentaho.platform.repository2.unified;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link IUnifiedRepository}. Delegates to {@link IRepositoryFileDao} and 
 * {@link IRepositoryFileAclDao}.
 * 
 * @author mlowery
 */
public class DefaultUnifiedRepository implements IUnifiedRepository {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private IRepositoryFileDao repositoryFileDao;

  private IRepositoryFileAclDao repositoryFileAclDao;

  // ~ Constructors ====================================================================================================

  public DefaultUnifiedRepository(final IRepositoryFileDao contentDao, final IRepositoryFileAclDao aclDao) {
    super();
    Assert.notNull(contentDao);
    Assert.notNull(aclDao);
    this.repositoryFileDao = contentDao;
    this.repositoryFileAclDao = aclDao;
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFileAce> getEffectiveAces(final Serializable fileId) {
    return getEffectiveAces(fileId, false);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFileAce> getEffectiveAces(final Serializable fileId, final boolean forceEntriesInheriting) {
    return repositoryFileAclDao.getEffectiveAces(fileId, forceEntriesInheriting);
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasAccess(final String path, final EnumSet<RepositoryFilePermission> permissions) {
    return repositoryFileAclDao.hasAccess(path, permissions);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final String path) {
    Assert.hasText(path);
    return repositoryFileDao.getFile(path, false);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFileById(final Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileDao.getFileById(fileId, false);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFile(final String path, final boolean loadMaps) {
    Assert.hasText(path);
    return repositoryFileDao.getFile(path, loadMaps);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFileById(final Serializable fileId, final boolean loadMaps) {
    Assert.notNull(fileId);
    return repositoryFileDao.getFileById(fileId, loadMaps);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final String versionMessage) {
    return createFile(parentFolderId, file, data, repositoryFileAclDao.createDefaultAcl(), versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file,
      final String versionMessage) {
    return createFolder(parentFolderId, file, repositoryFileAclDao.createDefaultAcl(), versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    Assert.notNull(data);
    Assert.notNull(acl);
    // external callers never allowed to create files at repo root
    Assert.notNull(parentFolderId);
    return internalCreateFile(parentFolderId, file, data, acl, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file,
      final RepositoryFileAcl acl, final String versionMessage) {
    Assert.notNull(file);
    Assert.notNull(acl);
    Assert.isTrue(file.isFolder());
    Assert.hasText(file.getName());
    // external callers never allowed to create folders at repo root
    Assert.notNull(parentFolderId);
    return internalCreateFolder(parentFolderId, file, acl, versionMessage);
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Delegates to {@link #getStreamForRead(RepositoryFile)} but assumes that some external system (e.g. Spring Security)
   * is protecting this method with different authorization rules than {@link #getStreamForRead(RepositoryFile)}.
   * </p>
   * 
   * <p>
   * In a direct contradiction of the previous paragraph, this implementation is not currently protected by Spring
   * Security.
   * </p>
   * 
   * @see #getDataForRead(RepositoryFile, Class)
   */
  public <T extends IRepositoryFileData> T getDataForExecute(final Serializable fileId, final Class<T> dataClass) {
    return getDataAtVersionForExecute(fileId, null, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends IRepositoryFileData> T getDataAtVersionForExecute(final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass) {
    return getDataAtVersionForRead(fileId, versionId, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends IRepositoryFileData> T getDataForRead(final Serializable fileId, final Class<T> dataClass) {
    return getDataAtVersionForRead(fileId, null, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends IRepositoryFileData> T getDataAtVersionForRead(final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass) {
    Assert.notNull(fileId);
    return repositoryFileDao.getData(fileId, versionId, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends IRepositoryFileData> List<T> getDataForReadInBatch(
      final List<RepositoryFile> files, final Class<T> dataClass) {
    Assert.notNull(files);
    List<T> data = new ArrayList<T>(files.size());
    for (RepositoryFile f : files) {
      Assert.notNull(f);
      data.add(getDataAtVersionForRead(f.getId(), f.getVersionId(), dataClass));
    }
    return data;
  }

  /**
   * {@inheritDoc}
   */
  public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch(
      final List<RepositoryFile> files, final Class<T> dataClass) {
    return getDataForReadInBatch(files, dataClass);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getChildren(final Serializable folderId) {
    return getChildren(folderId, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getChildren(final Serializable folderId, final String filter) {
    Assert.notNull(folderId);
    return repositoryFileDao.getChildren(folderId, filter);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData data,
      final String versionMessage) {
    Assert.notNull(file);
    Assert.notNull(data);

    return internalUpdateFile(file, data, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteFile(final Serializable fileId, final boolean permanent, final String versionMessage) {
    Assert.notNull(fileId);
    if (permanent) {
      // fyi: acl deleted when file node is deleted
      repositoryFileDao.permanentlyDeleteFile(fileId, versionMessage);
    } else {
      repositoryFileDao.deleteFile(fileId, versionMessage);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteFile(final Serializable fileId, final String versionMessage) {
    deleteFile(fileId, false, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteFileAtVersion(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    repositoryFileDao.deleteFileAtVersion(fileId, versionId);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath) {
    return getDeletedFiles(origParentFolderPath, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath, final String filter) {
    Assert.hasLength(origParentFolderPath);
    return repositoryFileDao.getDeletedFiles(origParentFolderPath, filter);
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getDeletedFiles() {
    return repositoryFileDao.getDeletedFiles();
  }

  /**
   * {@inheritDoc}
   */
  public void undeleteFile(final Serializable fileId, final String versionMessage) {
    Assert.notNull(fileId);
    repositoryFileDao.undeleteFile(fileId, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFileAcl getAcl(final Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileAclDao.getAcl(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public void lockFile(final Serializable fileId, final String message) {
    Assert.notNull(fileId);
    repositoryFileDao.lockFile(fileId, message);
  }

  /**
   * {@inheritDoc}
   */
  public void unlockFile(final Serializable fileId) {
    Assert.notNull(fileId);
    repositoryFileDao.unlockFile(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public VersionSummary getVersionSummary(Serializable fileId, Serializable versionId) {
    Assert.notNull(fileId);
    return repositoryFileDao.getVersionSummary(fileId, versionId);
  }

  /**
   * {@inheritDoc}
   */
  public List<VersionSummary> getVersionSummaryInBatch(final List<RepositoryFile> files) {
    Assert.notNull(files);
    List<VersionSummary> summaries = new ArrayList<VersionSummary>(files.size());
    for (RepositoryFile file : files) {
      Assert.notNull(file);
      summaries.add(getVersionSummary(file.getId(), file.getVersionId()));
    }
    return summaries;
  }

  /**
   * {@inheritDoc}
   */
  public List<VersionSummary> getVersionSummaries(final Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileDao.getVersionSummaries(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFile getFileAtVersion(final Serializable fileId, final Serializable versionId) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    return repositoryFileDao.getFile(fileId, versionId);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFileAcl updateAcl(final RepositoryFileAcl acl) {
    Assert.notNull(acl);
    return repositoryFileAclDao.updateAcl(acl);
  }

  /**
   * {@inheritDoc}
   */
  public void moveFile(final Serializable fileId, final String destAbsPath, final String versionMessage) {
    Assert.notNull(fileId);
    Assert.hasText(destAbsPath);
    repositoryFileDao.moveFile(fileId, destAbsPath, versionMessage);
  }
  
  /**
   * {@inheritDoc}
   */
  public void copyFile(final Serializable fileId, final String destAbsPath, final String versionMessage) {
    Assert.notNull(fileId);
    Assert.hasText(destAbsPath);
    repositoryFileDao.copyFile(fileId, destAbsPath, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public void restoreFileAtVersion(final Serializable fileId, final Serializable versionId, final String versionMessage) {
    Assert.notNull(fileId);
    Assert.notNull(versionId);
    repositoryFileDao.restoreFileAtVersion(fileId, versionId, versionMessage);
  }

  /**
   * {@inheritDoc}
   */
  public boolean canUnlockFile(final Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileDao.canUnlockFile(fileId);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFileTree getTree(final String path, final int depth, final String filter) {
    Assert.hasText(path);
    return getTree(path, depth, filter, true);
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryFileTree getTree(final String path, final int depth, final String filter, final boolean showHidden) {
    Assert.hasText(path);
    return repositoryFileDao.getTree(path, depth, filter, showHidden);
  }
  
  private RepositoryFile internalCreateFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage) {
    Assert.notNull(file);
    Assert.notNull(data);
    Assert.notNull(acl);
    return repositoryFileDao.createFile(parentFolderId, file, data, acl, versionMessage);
  }

  private RepositoryFile internalCreateFolder(final Serializable parentFolderId, final RepositoryFile file,
      final RepositoryFileAcl acl, final String versionMessage) {
    Assert.notNull(file);
    Assert.notNull(acl);
    return repositoryFileDao.createFolder(parentFolderId, file, acl, versionMessage);
  }

  private RepositoryFile internalUpdateFile(final RepositoryFile file, final IRepositoryFileData data,
      final String versionMessage) {
    Assert.notNull(file);
    Assert.notNull(data);
    return repositoryFileDao.updateFile(file, data, versionMessage);
  }

  public List<RepositoryFile> getReferrers(Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileDao.getReferrers(fileId);
  }

  public void setFileMetadata(final Serializable fileId, Map<String, Serializable> metadataMap) {
    Assert.notNull(fileId);
    repositoryFileDao.setFileMetadata(fileId, metadataMap);
  }
  
  public Map<String, Serializable> getFileMetadata(final Serializable fileId) {
    Assert.notNull(fileId);
    return repositoryFileDao.getFileMetadata(fileId);
  }
}
