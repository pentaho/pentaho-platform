/*
 * Copyright 2011 Pentaho Corporation.  All rights reserved. 
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
 * @created Jan 28, 2011 
 * @author wseyler
 */


package org.pentaho.platform.repository2.unified.importexport.legacy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.importexport.ImportSource;
import org.pentaho.platform.repository2.unified.importexport.RepositoryFileBundle;

/**
 * @author wseyler
 *
 */
public class ZipSolutionRepositoryImportSource extends AbstractImportSource {
  private static final int BUFFER_SIZE = 2048;

  private String charSet;
  private String ownerName;
  private ZipInputStream zipInputStream;
  private String[] filters;

  public ZipSolutionRepositoryImportSource(ZipInputStream zipInputStream, String charSet, String[] filtersParam) {
    super();
    this.zipInputStream = zipInputStream;
    this.charSet = charSet;
    this.filters = (filtersParam == null) ? new String[] {} : filtersParam;
  }
  
  public ZipSolutionRepositoryImportSource(ZipInputStream zipInputStream, String charSet) {
	this(zipInputStream, charSet, null);
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#getFile(java.lang.String)
   */
  @Override
  public IRepositoryFileBundle getFile(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#getFiles()
   */
  @Override
  public Iterable<IRepositoryFileBundle> getFiles() throws IOException {
    List<IRepositoryFileBundle> files = new ArrayList<IRepositoryFileBundle>();
//    try {
      ZipEntry entry = zipInputStream.getNextEntry();
      File tempFile = null;
      while (entry != null) {
        String entryName = FilenameUtils.separatorsToUnix(entry.getName());
        String extension = "";
       
        boolean includeFile = true;
        ImportSource importSource = null;
        for(String filter : this.filters) {
        	if (entryName.contains(filter)) {
        		importSource = resolveDependentImportSource(filter);
        		includeFile = importSource != null;
        		if(includeFile) {
        			break;
        		}
        	}
        }
        if (includeFile) {
          boolean isDir = entry.getSize() == 0;
          if (!isDir) {
            extension = entryName.substring(entryName.lastIndexOf('.') + 1);
            tempFile = File.createTempFile("zip", null);
            tempFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempFile);
            if(WAQRFilesMigrationHelper.isOldXWAQRFile(entryName)) {
              WAQRFilesMigrationHelper.convertToNewXWAQR(zipInputStream, fos) ;
            } else if (WAQRFilesMigrationHelper.isOldXreportSpecFile(entryName)) {
              WAQRFilesMigrationHelper.convertToNewXreportSpec(zipInputStream,fos) ;
            } else {
              IOUtils.copy(zipInputStream, fos);  
            }
            
            fos.close();
          }
          File file = new File(entryName);
          RepositoryFile repoFile = new RepositoryFile.Builder(WAQRFilesMigrationHelper.convertToNewExtension(file.getName()))
            .folder(isDir).hidden(WAQRFilesMigrationHelper.hideFileCheck(file.getName())).build();
          
          String parentDir =  new File(entryName).getParent() == null ? "/" : new File(entryName).getParent() + "/";    
          RepositoryFileBundle repoFileBundle = new RepositoryFileBundle(repoFile, null, parentDir, tempFile, charSet, mimeTypes.get(extension.toLowerCase()));
          if(importSource != null) {
        	  importSource.addFile(repoFileBundle);
          } else {
        	  files.add(repoFileBundle);
          }
        }
        zipInputStream.closeEntry();
        entry = zipInputStream.getNextEntry();
      }
      zipInputStream.close();
//    } catch (IllegalStateException e) {
//      return files;
//    } catch (IOException e) {
//      return files;
//    }
    return files;
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
    this.ownerName = ownerName;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.importexport.ImportSource#setRequiredCharset(java.lang.String)
   */
  @Override
  public void setRequiredCharset(String charset) {
    // TODO Auto-generated method stub

  }
}
