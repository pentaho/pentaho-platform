/*
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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 * @author dkincade
 */

package org.pentaho.test.platform.repository2.unified;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Mock implementation of the {@link IUnifiedRepositoryRepository} for unit testing. Please edit / add as needed</p>
 *
 * Currently, this implementation just creates a memory version of a repository filesystem that does not support much
 */
public class MockUnifiedRepository implements IUnifiedRepository {
  protected Map<String, Object> fileTable = new HashMap<String, Object>();

  public RepositoryFile getFile(final String path) {
    if (fileTable.containsKey(path)) {
      final String filename = FilenameUtils.getName(path);
      final RepositoryFile file = new RepositoryFile.Builder(path, filename).path(path).build();
      return file;
    } else {
      return null;
    }
  }

  public RepositoryFileTree getTree(final String path, final int depth, final String filter, final boolean showHidden) {
    return null;
  }

  public RepositoryFile getFileAtVersion(final Serializable fileId, final Serializable versionId) {
    return null;
  }

  public RepositoryFile getFileById(final Serializable fileId) {
    return null;
  }

  public RepositoryFile getFile(final String path, final boolean loadLocaleMaps) {
    return null;
  }

  public RepositoryFile getFileById(final Serializable fileId, final boolean loadLocaleMaps) {
    return null;
  }

  public <T extends IRepositoryFileData> T getDataForRead(final Serializable fileId, final Class<T> dataClass) {
    assert (SimpleRepositoryFileData.class.equals(dataClass));
    try {
      final Properties mappingFile = (Properties) fileTable.get(fileId);

      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      mappingFile.store(baos, "");

      final T output = (T) new SimpleRepositoryFileData(new ByteArrayInputStream(baos.toByteArray()), "UTF-8", "text/plain");
      return output;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public <T extends IRepositoryFileData> T getDataAtVersionForRead(final Serializable fileId, final Serializable versionId, final Class<T> dataClass) {
    return null;
  }

  public <T extends IRepositoryFileData> T getDataForExecute(final Serializable fileId, final Class<T> dataClass) {
    return null;
  }

  public <T extends IRepositoryFileData> T getDataAtVersionForExecute(final Serializable fileId, final Serializable versionId, final Class<T> dataClass) {
    return null;
  }

  public <T extends IRepositoryFileData> List<T> getDataForReadInBatch(final List<RepositoryFile> files, final Class<T> dataClass) {
    return null;
  }

  public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch(final List<RepositoryFile> files, final Class<T> dataClass) {
    return null;
  }

  public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file, final IRepositoryFileData data, final String versionMessage) {
    final String filePath = parentFolderId.toString() + "/" + file.getName();
    final RepositoryFile newFile = new RepositoryFile.Builder(parentFolderId.toString(),
        file.getName()).folder(false).path(filePath).build();
    fileTable.put(filePath, data);
    return newFile;
  }

  public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file, final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage) {
    return null;
  }

  public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file, final String versionMessage) {
    String parentPath = parentFolderId.toString();
    if (parentPath.equals("/")) {
      parentPath = "";
    }
    return createFolder(parentPath + "/" + file.getName());
  }

  public RepositoryFile createFolder(final String path) {
    final String folderName = FilenameUtils.getName(path);
    final RepositoryFile newFolder = new RepositoryFile.Builder(path, folderName).folder(true).path(path).build();
    fileTable.put(path, newFolder);
    return newFolder;
  }

  public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file, final RepositoryFileAcl acl, final String versionMessage) {
    return createFolder(parentFolderId, file, versionMessage);
  }

  public List<RepositoryFile> getChildren(final Serializable folderId) {
    return null;
  }

  public List<RepositoryFile> getChildren(final Serializable folderId, final String filter) {
    return null;
  }

  public RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData data, final String versionMessage) {
    assert (file != null && data != null);
    if (!fileTable.containsKey(file.getPath())) {
      return null;
    }

    try {
      SimpleRepositoryFileData fileData = (SimpleRepositoryFileData) data;
      InputStream in = fileData.getStream();
      Properties newMappingFile = new Properties();
      newMappingFile.load(in);
      fileTable.put(file.getPath(), newMappingFile);
    } catch (IOException e) {
      return null;
    }
    return file;
  }

  public void deleteFile(final Serializable fileId, final boolean permanent, final String versionMessage) {
    deleteFile(fileId, versionMessage);
  }

  public void deleteFile(final Serializable fileId, final String versionMessage) {
    assert (null != fileId);
    final String path = fileId.toString();
    for (Iterator<String> it = fileTable.keySet().iterator(); it.hasNext(); ) {
      final String repoPath = it.next();
      if (StringUtils.startsWith(repoPath, path)) {
        it.remove();
      }
    }
  }

  public void moveFile(final Serializable fileId, final String destAbsPath, final String versionMessage) {
  }

  public void copyFile(final Serializable fileId, final String destAbsPath, final String versionMessage) {
  }

  public void undeleteFile(final Serializable fileId, final String versionMessage) {
  }

  public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath) {
    return null;
  }

  public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath, final String filter) {
    return null;
  }

  public List<RepositoryFile> getDeletedFiles() {
    return null;
  }

  public boolean canUnlockFile(final Serializable fileId) {
    return false;
  }

  public void lockFile(final Serializable fileId, final String message) {
  }

  public void unlockFile(final Serializable fileId) {
  }

  public RepositoryFileAcl getAcl(final Serializable fileId) {
    return null;
  }

  public RepositoryFileAcl updateAcl(final RepositoryFileAcl acl) {
    return null;
  }

  public boolean hasAccess(final String path, final EnumSet<RepositoryFilePermission> permissions) {
    return false;
  }

  public List<RepositoryFileAce> getEffectiveAces(final Serializable fileId) {
    return null;
  }

  public List<RepositoryFileAce> getEffectiveAces(final Serializable fileId, final boolean forceEntriesInheriting) {
    return null;
  }

  public VersionSummary getVersionSummary(final Serializable fileId, final Serializable versionId) {
    return null;
  }

  public List<VersionSummary> getVersionSummaryInBatch(final List<RepositoryFile> files) {
    return null;
  }

  public List<VersionSummary> getVersionSummaries(final Serializable fileId) {
    return null;
  }

  public void deleteFileAtVersion(final Serializable fileId, final Serializable versionId) {
  }

  public void restoreFileAtVersion(final Serializable fileId, final Serializable versionId, final String versionMessage) {
  }

  public List<RepositoryFile> getReferrers(final Serializable fileId) {
    return null;
  }

  public void setFileMetadata(final Serializable fileId, final Map<String, Serializable> metadataMap) {
  }

  public Map<String, Serializable> getFileMetadata(final Serializable fileId) {
    return null;
  }

  /**
   * Returns information about the files currently in the mock repository
   */
  public Map<String, Object> getFileTable() {
    return fileTable;
  }

}