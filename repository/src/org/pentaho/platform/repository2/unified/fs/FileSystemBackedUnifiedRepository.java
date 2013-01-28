package org.pentaho.platform.repository2.unified.fs;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;

public class FileSystemBackedUnifiedRepository implements IUnifiedRepository {
  private FileSystemRepositoryFileDao repositoryFileDao;

  public FileSystemBackedUnifiedRepository(final String baseDir) {
    this(new FileSystemRepositoryFileDao(baseDir));
  }

  public FileSystemBackedUnifiedRepository(final File baseDir) {
    this(new FileSystemRepositoryFileDao(baseDir));
  }

  public FileSystemBackedUnifiedRepository() {
    this(new FileSystemRepositoryFileDao());
  }

  public FileSystemBackedUnifiedRepository(final FileSystemRepositoryFileDao repositoryFileDao) {
    this.repositoryFileDao = repositoryFileDao;
  }

  public boolean canUnlockFile(Serializable fileId) {
    return repositoryFileDao.canUnlockFile(fileId);
  }

  public RepositoryFile createFile(Serializable parentFolderId, RepositoryFile file, IRepositoryFileData data,
                                   String versionMessage) {
    return repositoryFileDao.createFile(parentFolderId, file, data, null, versionMessage);
  }

  public RepositoryFile createFile(Serializable parentFolderId, RepositoryFile file, IRepositoryFileData data,
                                   RepositoryFileAcl acl, String versionMessage) {
    return repositoryFileDao.createFile(parentFolderId, file, data, acl, versionMessage);
  }

  public RepositoryFile createFolder(Serializable parentFolderId, RepositoryFile file, String versionMessage) {
    return repositoryFileDao.createFolder(parentFolderId, file, null, versionMessage);
  }

  public RepositoryFile createFolder(Serializable parentFolderId, RepositoryFile file, RepositoryFileAcl acl,
                                     String versionMessage) {
    return repositoryFileDao.createFolder(parentFolderId, file, acl, versionMessage);
  }

  public void deleteFile(Serializable fileId, boolean permanent, String versionMessage) {
    repositoryFileDao.deleteFile(fileId, versionMessage);

  }

  public void deleteFile(Serializable fileId, String versionMessage) {
    repositoryFileDao.deleteFile(fileId, versionMessage);

  }

  public void deleteFileAtVersion(Serializable fileId, Serializable versionId) {
    repositoryFileDao.deleteFileAtVersion(fileId, versionId);
  }

  public RepositoryFileAcl getAcl(Serializable fileId) {
    throw new UnsupportedOperationException();
  }

  public List<RepositoryFile> getChildren(Serializable folderId) {
    return repositoryFileDao.getChildren(folderId);
  }

  public List<RepositoryFile> getChildren(Serializable folderId, String filter) {
    return repositoryFileDao.getChildren(folderId, filter);
  }

  public <T extends IRepositoryFileData> T getDataAtVersionForExecute(Serializable fileId, Serializable versionId,
                                                                      Class<T> dataClass) {
    return repositoryFileDao.getData(fileId, versionId, dataClass);
  }

  public <T extends IRepositoryFileData> T getDataAtVersionForRead(Serializable fileId, Serializable versionId,
                                                                   Class<T> dataClass) {
    return repositoryFileDao.getData(fileId, versionId, dataClass);
  }

  public <T extends IRepositoryFileData> T getDataForExecute(Serializable fileId, Class<T> dataClass) {
    return repositoryFileDao.getData(fileId, null, dataClass);
  }

  public <T extends IRepositoryFileData> T getDataForRead(Serializable fileId, Class<T> dataClass) {
    return repositoryFileDao.getData(fileId, null, dataClass);
  }

  public List<RepositoryFile> getDeletedFiles(Serializable folderId) {
    // TODO Auto-generated method stub
    return repositoryFileDao.getDeletedFiles(folderId, null);
  }

  public List<RepositoryFile> getDeletedFiles(Serializable folderId, String filter) {
    return repositoryFileDao.getDeletedFiles(folderId, filter);
  }

  public List<RepositoryFile> getDeletedFiles() {
    return repositoryFileDao.getDeletedFiles();
  }

  public List<RepositoryFileAce> getEffectiveAces(Serializable fileId) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<RepositoryFileAce> getEffectiveAces(Serializable fileId, boolean forceEntriesInheriting) {
    // TODO Auto-generated method stub
    return null;
  }

  public RepositoryFile getFile(String path) {
    return repositoryFileDao.getFile(path);
  }

  public RepositoryFile getFile(String path, boolean loadLocaleMaps) {
    return repositoryFileDao.getFile(path, loadLocaleMaps);
  }

  public RepositoryFile getFileAtVersion(Serializable fileId, Serializable versionId) {
    return repositoryFileDao.getFile(fileId, versionId);
  }

  public RepositoryFile getFileById(Serializable fileId) {
    return repositoryFileDao.getFile(fileId, null);
  }

  public RepositoryFile getFileById(Serializable fileId, boolean loadLocaleMaps) {
    return repositoryFileDao.getFileById(fileId, loadLocaleMaps);
  }

  public RepositoryFileTree getTree(String path, int depth, String filter, boolean showHidden) {
    return repositoryFileDao.getTree(path, depth, filter, showHidden);
  }

  public List<VersionSummary> getVersionSummaries(Serializable fileId) {
    return repositoryFileDao.getVersionSummaries(fileId);
  }

  public VersionSummary getVersionSummary(Serializable fileId, Serializable versionId) {
    return repositoryFileDao.getVersionSummary(fileId, versionId);
  }

  public void setRootDir(File rootDir) {
    repositoryFileDao.setRootDir(rootDir);
  }

  public boolean hasAccess(String path, EnumSet<RepositoryFilePermission> permissions) {
    throw new UnsupportedOperationException();
  }

  public void lockFile(Serializable fileId, String message) {
    repositoryFileDao.lockFile(fileId, message);
  }

  public void moveFile(Serializable fileId, String destAbsPath, String versionMessage) {
    repositoryFileDao.moveFile(fileId, destAbsPath, versionMessage);
  }

  public void restoreFileAtVersion(Serializable fileId, Serializable versionId, String versionMessage) {
    repositoryFileDao.restoreFileAtVersion(fileId, versionId, versionMessage);
  }

  public void undeleteFile(Serializable fileId, String versionMessage) {
    repositoryFileDao.undeleteFile(fileId, versionMessage);
  }

  public void unlockFile(Serializable fileId) {
    repositoryFileDao.unlockFile(fileId);
  }

  public RepositoryFileAcl updateAcl(RepositoryFileAcl acl) {
    throw new UnsupportedOperationException();
  }

  public RepositoryFile updateFile(RepositoryFile file, IRepositoryFileData data, String versionMessage) {
    return repositoryFileDao.updateFile(file, data, versionMessage);
  }

  public List<RepositoryFile> getReferrers(Serializable arg0) {
    throw new UnsupportedOperationException();
  }

  public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch(List<RepositoryFile> files, Class<T> dataClass) {
    throw new UnsupportedOperationException();
  }

  public <T extends IRepositoryFileData> List<T> getDataForReadInBatch(List<RepositoryFile> files, Class<T> dataClass) {
    throw new UnsupportedOperationException();
  }

  public List<VersionSummary> getVersionSummaryInBatch(List<RepositoryFile> files) {
    throw new UnsupportedOperationException();
  }

  public void setFileMetadata(final Serializable fileId, Map<String, Serializable> metadataMap) {
    repositoryFileDao.setFileMetadata(fileId, metadataMap);
  }

  public Map<String, Serializable> getFileMetadata(final Serializable fileId) {
    return repositoryFileDao.getFileMetadata(fileId);
  }

  public void copyFile(Serializable fileId, String destAbsPath, String versionMessage) {
    throw new UnsupportedOperationException();
  }

  public List<RepositoryFile> getDeletedFiles(String origParentFolderPath, String filter) {
    throw new UnsupportedOperationException();
  }

  public List<RepositoryFile> getDeletedFiles(String origParentFolderPath) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Character> getReservedChars() {
    return Collections.emptyList();
  }


}
