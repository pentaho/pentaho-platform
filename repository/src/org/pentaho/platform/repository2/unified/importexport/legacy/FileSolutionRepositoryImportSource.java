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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.importexport.RepositoryFileBundle;
import org.springframework.util.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wseyler
 */
public class FileSolutionRepositoryImportSource extends AbstractImportSource {
  private static final Log log = LogFactory.getLog(FileSolutionRepositoryImportSource.class);

  private File sourceFile;
  private String charSet;
  private String filename;
  private final List<IRepositoryFileBundle> files = new ArrayList<IRepositoryFileBundle>();

  public FileSolutionRepositoryImportSource(final File sourceFile, final String charSet) {
    this(sourceFile, sourceFile.getName(), charSet);
  }

  public FileSolutionRepositoryImportSource(final File sourceFile, final String filename, final String charSet) {
    Assert.notNull(sourceFile);
    Assert.hasText(filename);
    Assert.hasText(charSet);
    this.sourceFile = sourceFile;
    this.filename = filename;
    this.charSet = charSet;
  }

  /**
   * builds the list of files
   *
   * @throws Exception indicates an error initializing this ImportSource
   */
  @Override
  public void initialize() throws Exception {
    addFileToList(sourceFile);
    log.debug("File list built - size=" + files.size());
  }

  /* (non-Javadoc)
  * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#getFiles()
  */
  public Iterable<IRepositoryFileBundle> getFiles() {
    return files;
  }

  /**
   * Returns the number of files to process (or -1 if that is not known)
   */
  @Override
  public int getCount() {
    return files.size();
  }

  protected void addFileToList(final File currentFile) {
    // Weed out .svn folders
    if (currentFile == null || (currentFile.isDirectory() && currentFile.getName().equals(".svn"))) {
      return;
    }
    files.add(getFile(currentFile, filename));
    if (currentFile.isDirectory()) {
      for (File child : currentFile.listFiles()) {
        addFileToList(child);
      }
    }
  }

  protected IRepositoryFileBundle getFile(final File currentFile, final String filename) {
    if (WAQRFilesMigrationHelper.isOldXWAQRFile(filename)) {
      WAQRFilesMigrationHelper.convertToNewXWAQR(currentFile);
    } else if (WAQRFilesMigrationHelper.isOldXreportSpecFile(filename)) {
      WAQRFilesMigrationHelper.convertToNewXreportSpec(currentFile);
    }

    final RepositoryFile repoFile = new RepositoryFile.Builder(WAQRFilesMigrationHelper.convertToNewExtension(filename))
        .folder(currentFile.isDirectory()).hidden(WAQRFilesMigrationHelper.hideFileCheck(filename))
        .lastModificationDate(new Date(currentFile.lastModified())).build();

    final String extension = RepositoryFilenameUtils.getExtension(filename);
    String repoPath = "";
    if (currentFile.isDirectory()) {
      final String parentFilePath = currentFile.getParentFile().getAbsolutePath();
      final String sourceParentFilePath = sourceFile.getParentFile().getAbsolutePath();
      repoPath = RepositoryFilenameUtils.getFullPath(filename);
    }

    return new RepositoryFileBundle(repoFile, null, repoPath, currentFile, charSet, getMimeType(extension.toLowerCase()));
  }
}
