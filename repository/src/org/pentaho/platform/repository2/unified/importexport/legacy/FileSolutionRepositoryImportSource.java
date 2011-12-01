/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Dec 2, 2010 
 * @author wseyler
 */

package org.pentaho.platform.repository2.unified.importexport.legacy;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.importexport.RepositoryFileBundle;
import org.springframework.util.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author wseyler
 *
 */
public class FileSolutionRepositoryImportSource extends AbstractImportSource {

  private File sourceFile;

  private String charSet;

  private String ownerName;

  private boolean recursive;

  public FileSolutionRepositoryImportSource(final File sourceFile, final String charSet) {
    super();
    Assert.notNull(sourceFile);
    recursive = sourceFile.isDirectory();
    this.sourceFile = sourceFile;
    Assert.notNull(charSet);
    this.charSet = charSet;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#getFile(java.lang.String)
   */
  public IRepositoryFileBundle getFile(final String path) {
    File file = new File(path);
    file = file.getAbsoluteFile();
    if(WAQRFilesMigrationHelper.isOldXWAQRFile(file.getName())) {
      WAQRFilesMigrationHelper.convertToNewXWAQR(file) ;
    } else if (WAQRFilesMigrationHelper.isOldXreportSpecFile(file.getName())) {
      WAQRFilesMigrationHelper.convertToNewXreportSpec(file) ;
    }
    RepositoryFile repoFile = new RepositoryFile.Builder(WAQRFilesMigrationHelper.convertToNewExtension(file.getName()))
        .folder(file.isDirectory()).hidden(WAQRFilesMigrationHelper.hideFileCheck(file.getName())).lastModificationDate(new Date(file.lastModified())).build();

    String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
    String repoPath = "";
    if (recursive) {
      String parentFilePath = file.getParentFile().getAbsolutePath();
      String sourceParentFilePath = sourceFile.getParentFile().getAbsolutePath();
      repoPath = parentFilePath.substring(sourceParentFilePath.length()) + File.separator;
    }

    repoPath = RepositoryFilenameUtils.separatorsToRepository(repoPath);
    RepositoryFileBundle repoFileBundle = new RepositoryFileBundle(repoFile, null, repoPath, file, charSet,
        mimeTypes.get(extension.toLowerCase()));

    return repoFileBundle;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#getFiles()
   */
  public Iterable<IRepositoryFileBundle> getFiles() {
    return new Iterable<IRepositoryFileBundle>() {
      @SuppressWarnings("synthetic-access")
      public Iterator<IRepositoryFileBundle> iterator() {
        return new RepositoryFileBundleIterator(sourceFile);
      }
    };
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#getRequiredCharset()
   */
  public String getRequiredCharset() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#setOwnerName(java.lang.String)
   */
  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#setRequiredCharset(java.lang.String)
   */
  public void setRequiredCharset(String charset) {
    // TODO Auto-generated method stub

  }

  private class RepositoryFileBundleIterator implements Iterator<IRepositoryFileBundle> {

    private File baseFile;

    Iterator<File> iter;

    List<File> files = new ArrayList<File>();

    public RepositoryFileBundleIterator(File baseFile) {
      super();
      this.baseFile = baseFile;
      buildFileList(baseFile);
      iter = files.listIterator();
    }

    /**
     * 
     */
    private void buildFileList(File currentFile) {
      // Weed out the system folder
      if (currentFile.isDirectory() && currentFile.getName().equals("system")) {
        return;
      }
      // Weed out .svn folders
      if (currentFile.isDirectory() && currentFile.getName().equals(".svn")) {
        return;
      }
      files.add(currentFile);
      if (currentFile.isDirectory()) {
        for (File child : currentFile.listFiles()) {
          buildFileList(child);
        }
      }
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      return iter.hasNext();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public IRepositoryFileBundle next() {
      File nextFile = iter.next();
      return FileSolutionRepositoryImportSource.this.getFile(nextFile.getAbsolutePath());
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      iter.remove();
    }

  }


}
