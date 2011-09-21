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
 * @created Feb 7, 2011 
 * @author wseyler
 */


package org.pentaho.platform.repository2.unified.importexport.legacy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.importexport.RepositoryFileBundle;

/**
 * @author wseyler
 *
 */
public class SingleFileStreamImportSource extends AbstractImportSource {
  private String fileName;
  private String charSet;
  private File outFile;

  public SingleFileStreamImportSource(InputStream is, String fileName, String charSet) throws IOException {
    super();
    this.fileName = fileName;
    this.charSet = charSet;
    outFile = File.createTempFile("JCR", null);
    outFile.deleteOnExit();
    
    OutputStream os = new FileOutputStream(outFile);

    if(WAQRFilesMigrationHelper.isOldXWAQRFile(outFile.getName())) {
      WAQRFilesMigrationHelper.convertToNewXWAQR(is, os) ;
    } else if (WAQRFilesMigrationHelper.isOldXreportSpecFile(outFile.getName())) {
      WAQRFilesMigrationHelper.convertToNewXreportSpec(is, os) ;
    }
    IOUtils.copy(is, os);
    is.close();  
    os.close();  
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#getFile(java.lang.String)
   */
  @Override
  public IRepositoryFileBundle getFile(String path) {
    RepositoryFile repoFile = new RepositoryFile.Builder(WAQRFilesMigrationHelper.convertToNewExtension(fileName)).
      folder(outFile.isDirectory()).hidden(WAQRFilesMigrationHelper.hideFileCheck(fileName)).lastModificationDate(new Date(outFile.lastModified())).build();

    String extension = fileName.substring(fileName.indexOf('.') + 1);
    String repoPath = "/";
    repoPath = FilenameUtils.separatorsToUnix(repoPath);
    RepositoryFileBundle repoFileBundle = new RepositoryFileBundle(repoFile, null, repoPath, outFile, charSet, mimeTypes.get(extension.toLowerCase()));
    
    return repoFileBundle;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#getFiles()
   */
  @Override
  public Iterable<IRepositoryFileBundle> getFiles() {
    List<IRepositoryFileBundle> iterable = new ArrayList<IRepositoryFileBundle>();
    iterable.add(getFile(null));
    return iterable;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#getRequiredCharset()
   */
  @Override
  public String getRequiredCharset() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#setOwnerName(java.lang.String)
   */
  @Override
  public void setOwnerName(String ownerName) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#setRequiredCharset(java.lang.String)
   */
  @Override
  public void setRequiredCharset(String charset) {
    // TODO Auto-generated method stub

  }

}
