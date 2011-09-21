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
 * Copyright 2007-2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.repository.solution.filebased;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.operations.FileOperations;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class SolutionRepositoryVfsFileObject implements FileObject {

  private String fileRef;

  private static final IUnifiedRepository REPOSITORY = PentahoSystem.get(IUnifiedRepository.class, null);

  private FileContent content = null;

  private boolean fileInitialized;
  
  private RepositoryFile repositoryFile = null;

  public SolutionRepositoryVfsFileObject(final String fileRef) {
    super();
    this.fileRef = fileRef;
  }

  public IUnifiedRepository getRepository() {
    return REPOSITORY;
  }

  public String getFileRef() {
    return fileRef;
  }

  public FileName getName() {
    initFile();
    FileType fileType = null;
    try {
      fileType = getType();
    } catch (Exception ex) {
      fileType = FileType.FOLDER;
    }
    return new SolutionRepositoryFileName(fileRef, fileType);
  }

  public URL getURL() throws FileSystemException {
    URL url = null;
    try {
      url = new URL("solution:" + fileRef); //$NON-NLS-1$
    } catch (Exception e) {

    }
    return url;
  }

  private void initFile() {
    if (!fileInitialized) {
      repositoryFile = REPOSITORY.getFile(fileRef);
      fileInitialized = true;
    }
  }
  public boolean exists() throws FileSystemException {
    initFile();
    return repositoryFile != null;
  }

  public boolean isHidden() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public boolean isReadable() throws FileSystemException {
    // not needed for our usage
    return exists();
  }

  public boolean isWriteable() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public FileType getType() throws FileSystemException {
    return ((repositoryFile != null) && !repositoryFile.isFolder()) ? FileType.FILE : FileType.FOLDER;
  }

  public FileObject getParent() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileSystem getFileSystem() {
    // not needed for our usage
    return null;
  }

  public FileObject[] getChildren() throws FileSystemException {

    List<FileObject> fileList = new ArrayList<FileObject>();
    if (exists()) {
      for (RepositoryFile child : REPOSITORY.getChildren(repositoryFile.getId())) {
          SolutionRepositoryVfsFileObject fileInfo = new SolutionRepositoryVfsFileObject(child.getPath());
          fileList.add(fileInfo);
      }
    }
    return fileList.toArray(new FileObject[0]);
  }

  public FileObject getChild(final String arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject resolveFile(final String arg0, final NameScope arg1) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject resolveFile(final String arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject[] findFiles(final FileSelector arg0) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public void findFiles(final FileSelector arg0, final boolean arg1, final List arg2) throws FileSystemException {
    // not needed for our usage
  }

  public boolean delete() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public int delete(final FileSelector arg0) throws FileSystemException {
    // not needed for our usage
    return 0;
  }

  public void createFolder() throws FileSystemException {
    // not needed for our usage

  }

  public void createFile() throws FileSystemException {
    // not needed for our usage

  }

  public void copyFrom(final FileObject arg0, final FileSelector arg1) throws FileSystemException {
    // not needed for our usage

  }

  public void moveTo(final FileObject arg0) throws FileSystemException {
    // not needed for our usage
  }

  public boolean canRenameTo(final FileObject arg0) {
    // not needed for our usage
    return false;
  }

  public FileContent getContent() throws FileSystemException {
    content = new SolutionRepositoryVfsFileContent(this);
    return content;
  }

  public void close() throws FileSystemException {
    if (content != null) {
      content.close();
      content = null;
    }
  }

  public void refresh() throws FileSystemException {
    // not needed for our usage
  }

  public boolean isAttached() {
    // not needed for our usage
    return false;
  }

  public boolean isContentOpen() {
    return (content != null) && content.isOpen();
  }

  public FileOperations getFileOperations() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public InputStream getInputStream() throws FileSystemException {
    InputStream inputStream = null;
    if (exists()) {
      inputStream = REPOSITORY.getDataForRead(repositoryFile.getId(), SimpleRepositoryFileData.class).getStream();
    }
    return inputStream;
  }
}
