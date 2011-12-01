package org.pentaho.platform.repository2.unified.fs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FileSystemRepositoryFileDao implements IRepositoryFileDao {
  private File rootDir = new File(System.getProperty("solution.root.dir", System.getProperty("user.dir")));

  public FileSystemRepositoryFileDao() {
    this(new File(System.getProperty("solution.root.dir", System.getProperty("user.dir"))));
  }

  public FileSystemRepositoryFileDao(final String baseDir) {
    this(new File(baseDir));
  }

  public FileSystemRepositoryFileDao(final File baseDir) {
    assert (baseDir.exists() && baseDir.isDirectory());
    this.rootDir = baseDir;
  }

  public boolean canUnlockFile(Serializable fileId) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public File getRootDir() {
    return new File(rootDir.getAbsolutePath());
  }

  private byte[] inputStreamToBytes(InputStream in) throws IOException {

    ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
    byte[] buffer = new byte[4096];
    int len;

    while ((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);

    in.close();
    out.close();
    return out.toByteArray();
  }

  public RepositoryFile createFile(Serializable parentFolderId, RepositoryFile file, IRepositoryFileData data,
                                   RepositoryFileAcl acl, String versionMessage) {
    String fileNameWithPath = RepositoryFilenameUtils.concat(parentFolderId.toString(), file.getName());
    FileOutputStream fos = null;
    try {
      File f = new File(fileNameWithPath);
      f.createNewFile();
      fos = new FileOutputStream(f);
      if (data instanceof SimpleRepositoryFileData) {
        fos.write(inputStreamToBytes(((SimpleRepositoryFileData) data).getStream()));
      }

    } catch (FileNotFoundException e) {
      throw new UnifiedRepositoryException();
    } catch (IOException e) {
      throw new UnifiedRepositoryException();
    } finally {
      IOUtils.closeQuietly(fos);
    }
    return file;
  }

  public RepositoryFile createFolder(Serializable parentFolderId, RepositoryFile file, RepositoryFileAcl acl,
                                     String versionMessage) {
    try {
      String folderNameWithPath = parentFolderId + "/" + file.getName();
      File newFolder = new File(folderNameWithPath);
      newFolder.mkdir();
      final RepositoryFile repositoryFolder= internalGetFile(newFolder);
      return repositoryFolder;
    } catch (Throwable th) {
      throw new UnifiedRepositoryException();
    }
  }

  public void deleteFile(Serializable fileId, String versionMessage) {
    try {
      File f = new File(fileId.toString());
      f.delete();
    } catch (Exception e) {

    }

  }

  public void deleteFileAtVersion(Serializable fileId, Serializable versionId) {
    deleteFile(fileId, null);
  }

  public List<RepositoryFile> getChildren(Serializable folderId) {
    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    File folder = new File(folderId.toString());
    for (Iterator iterator = FileUtils.listFiles(folder, null, false).iterator(); iterator.hasNext(); ) {
      children.add(internalGetFile((File) iterator.next()));
    }
    return children;
  }

  public List<RepositoryFile> getChildren(Serializable folderId, String filter) {
    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    File folder = new File(folderId.toString());
    for (Iterator iterator = FileUtils.listFiles(folder, new WildcardFileFilter(filter), null).iterator(); iterator.hasNext(); ) {
      children.add(internalGetFile((File) iterator.next()));
    }
    return children;
  }

  @SuppressWarnings("unchecked")
  public <T extends IRepositoryFileData> T getData(Serializable fileId, Serializable versionId, Class<T> dataClass) {
    File f = new File(fileId.toString());
    T data = null;
    try {
      data = (T) new SimpleRepositoryFileData(new FileInputStream(f), "UTF-8", "text/plain");
    } catch (FileNotFoundException e) {
      throw new UnifiedRepositoryException(e);
    }
    return data;
  }

  public List<RepositoryFile> getDeletedFiles(Serializable folderId, String filter) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public List<RepositoryFile> getDeletedFiles() {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public RepositoryFile internalGetFile(File f) {
    RepositoryFile file = null;
    if (f.exists()) {
      String jcrPath = f.getAbsolutePath().substring(rootDir.getAbsolutePath().length());
      if (jcrPath.length() == 0) {
        jcrPath = "/";
      }
      file = new RepositoryFile.Builder(f.getAbsolutePath(), f.getName())
          .createdDate(new Date(f.lastModified()))
          .lastModificationDate(new Date(f.lastModified()))
          .folder(f.isDirectory())
          .versioned(false)
          .path(jcrPath)
          .versionId(f.getName())
          .locked(false)
          .lockDate(null)
          .lockMessage(null)
          .lockOwner(null)
          .owner(null)
          .title(f.getName()).description(f.getName())
          .titleMap(null)
          .descriptionMap(null)
          .locale(null)
          .fileSize(f.length())
          .build();
    }
    return file;

  }

  public RepositoryFile getFile(String relPath) {
    String physicalFileLocation = relPath.equals("/") ? rootDir.getAbsolutePath() : RepositoryFilenameUtils.concat
        (rootDir.getAbsolutePath(), relPath.substring(RepositoryFilenameUtils.getPrefixLength(relPath)));
    return internalGetFile(new File(physicalFileLocation));
  }

  public RepositoryFile getFile(String relPath, boolean loadLocaleMaps) {
    return getFile(relPath);
  }

  public RepositoryFile getFile(Serializable fileId, Serializable versionId) {
    return getFile(fileId.toString());
  }

  public RepositoryFile getFileByAbsolutePath(String absPath) {
    return getFile(absPath);
  }

  public RepositoryFile getFileById(Serializable fileId) {
    return getFile(fileId.toString());
  }

  public RepositoryFile getFileById(Serializable fileId, boolean loadLocaleMaps) {
    return getFile(fileId.toString());
  }

  public RepositoryFileTree getTree(String relPath, int depth, String filter, boolean showHidden) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public List<VersionSummary> getVersionSummaries(Serializable fileId) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public VersionSummary getVersionSummary(Serializable fileId, Serializable versionId) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public void lockFile(Serializable fileId, String message) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public void moveFile(Serializable fileId, String destRelPath, String versionMessage) {
    RepositoryFile file = getFileById(fileId);
    SimpleRepositoryFileData data = getData(fileId, null, SimpleRepositoryFileData.class);
    deleteFile(fileId, versionMessage);
    createFile(null, file, data, createDefaultAcl(), versionMessage);
  }

  public void permanentlyDeleteFile(Serializable fileId, String versionMessage) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public void restoreFileAtVersion(Serializable fileId, Serializable versionId, String versionMessage) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public void undeleteFile(Serializable fileId, String versionMessage) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public void unlockFile(Serializable fileId) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public RepositoryFile updateFile(RepositoryFile file, IRepositoryFileData data, String versionMessage) {
    File f = new File(file.getId().toString());
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(f, false);
      if (data instanceof SimpleRepositoryFileData) {
        fos.write(inputStreamToBytes(((SimpleRepositoryFileData) data).getStream()));
      }
    } catch (FileNotFoundException e) {
      throw new UnifiedRepositoryException();
    } catch (IOException e) {
      throw new UnifiedRepositoryException();
    } finally {
      IOUtils.closeQuietly(fos);
    }

    return createFile(file.getPath(), file, data, createDefaultAcl(), null);
  }

  protected RepositoryFileAcl createDefaultAcl() {
    return new RepositoryFileAcl.Builder(new RepositoryFileSid("joe")).entriesInheriting(true).build();
  }

  public List<RepositoryFile> getReferrers(Serializable fileId) {
    throw new UnsupportedOperationException();
  }

  public void setRootDir(File rootDir) {
    this.rootDir = rootDir;
  }

  public void setFileMetadata(final Serializable fileId, Map<String, Serializable> metadataMap) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public Map<String, Serializable> getFileMetadata(final Serializable fileId) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public void copyFile(Serializable fileId, String destAbsPath, String versionMessage) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }

  public List<RepositoryFile> getDeletedFiles(String origParentFolderPath, String filter) {
    throw new UnsupportedOperationException("This operation is not support by this repository");
  }
}
