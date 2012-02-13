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
 * @created Jan 12, 2011 
 * @author wseyler
 */


package org.pentaho.platform.plugin.services.importexport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;

/**
 * @author wseyler
 *
 */
public class Exporter {
  IUnifiedRepository unifiedRepository;
  private String repoPath;
  private String filePath;
  
  private File exportDir;
  /**
   * @param unifiedRepository
   * @param path (repo) 
   * @param filePath
   */
  public Exporter(IUnifiedRepository unifiedRepository, String path, String filePath) {
    this.unifiedRepository = unifiedRepository;
    this.repoPath = path;
    this.filePath = filePath;
  }

  /**
   * @param repo
   */
  public Exporter(IUnifiedRepository unifiedRepository) {
    this.unifiedRepository = unifiedRepository;
  }

  public void doExport() throws IOException {
    exportDir = new File(filePath);
    RepositoryFile exportRepositoryFile = unifiedRepository.getFile(repoPath);
    
    if (exportRepositoryFile == null) {
      throw new FileNotFoundException("JCR file not found: " + repoPath);
    }
    
    if (exportRepositoryFile.isFolder()) {  // Handle recursive export
      exportDirectory(exportRepositoryFile, exportDir);
    } else {  // Handle a single file export
      exportFile(exportRepositoryFile, exportDir);
    }
  }
  
  public File doExportAsZip() throws IOException {
    File zipFile = File.createTempFile("repoExport", ".zip");
    zipFile.deleteOnExit();
    RepositoryFile exportRepositoryFile = unifiedRepository.getFile(repoPath);   
    filePath = new File(repoPath).getParent();
    if (exportRepositoryFile == null) {
      throw new FileNotFoundException("JCR file not found: " + repoPath);
    }

    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
    if (exportRepositoryFile.isFolder()) {  // Handle recursive export
      exportDirectoryAsZip(exportRepositoryFile, zos);
    } else {
      exportFileAsZip(exportRepositoryFile, zos);
    }
    
    zos.close();
    return zipFile;
  }
  
  /**
   * @param exportRepositoryFile
   * @param zipFile
   */
  private void exportDirectoryAsZip(RepositoryFile repositoryDir, ZipOutputStream zos) throws IOException {
    List<RepositoryFile> children = unifiedRepository.getChildren(repositoryDir.getId());
    for (RepositoryFile repoFile : children) {
      if (repoFile.isFolder()) {
        exportDirectoryAsZip(repoFile, zos);
      } else {
        exportFileAsZip(repoFile, zos);
      }
    }
  }

  /**
   * @param repoFile
   * @param zos
   */
  private void exportFileAsZip(RepositoryFile exportRepositoryFile, ZipOutputStream zos) throws IOException {
    ZipEntry entry = new ZipEntry(exportRepositoryFile.getPath().substring(filePath.length()));
    zos.putNextEntry(entry);
    SimpleRepositoryFileData repoFileData = unifiedRepository.getDataForRead(exportRepositoryFile.getId(), SimpleRepositoryFileData.class);
    InputStream is = repoFileData.getStream();
    IOUtils.copy(is, zos);
    zos.closeEntry();
    is.close();
  }

  /**
   * @param repositoryDir
   * @param exportDirectory
   * @throws IOException 
   */
  public void exportDirectory(RepositoryFile repositoryDir, File parentDir) throws IOException {
    if (repositoryDir == null || !repositoryDir.isFolder()) {
      throw new IllegalArgumentException("Source must be a valid directory: " + repositoryDir == null ? "Null" : repositoryDir.getPath());
    }
    if (parentDir == null) {
      throw new FileNotFoundException("Destination must be specified");
    }
    parentDir = new File(parentDir, repositoryDir.getName());
    parentDir.mkdirs();
    List<RepositoryFile> children = unifiedRepository.getChildren(repositoryDir.getId());
    for (RepositoryFile repoFile : children) {
      if (repoFile.isFolder()) {
        exportDirectory(repoFile, parentDir);
      } else {
        exportFile(repoFile, parentDir);
      }
    }
  }

  public void exportFile(RepositoryFile exportRepositoryFile, File exportDirectory) throws IOException {
    if (exportDirectory.exists()) {
      if (!exportDirectory.isDirectory()) {
        throw new IllegalArgumentException("Dest must be a directory: " + exportDirectory.getAbsolutePath());
      }
    } else {  // Directory doesn't exist so create it
      exportDirectory.mkdirs();
    }
    
    if (exportRepositoryFile == null) {
      throw new FileNotFoundException("JCR file not found: " + repoPath);
    }

    SimpleRepositoryFileData repoFileData = unifiedRepository.getDataForRead(exportRepositoryFile.getId(), SimpleRepositoryFileData.class);
    InputStream is = repoFileData.getStream();
    File exportFile = new File(exportDirectory.getAbsolutePath() + File.separator + exportRepositoryFile.getName());
    OutputStream os = new FileOutputStream(exportFile);
    IOUtils.copy(is, os);
    os.close();
    is.close();
  }
  
  public IUnifiedRepository getUnifiedRepository() {
    return unifiedRepository;
  }

  public void setUnifiedRepository(IUnifiedRepository unifiedRepository) {
    this.unifiedRepository = unifiedRepository;
  }

  public String getRepoPath() {
    return repoPath;
  }

  public void setRepoPath(String repoPath) {
    this.repoPath = repoPath;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
  
}
