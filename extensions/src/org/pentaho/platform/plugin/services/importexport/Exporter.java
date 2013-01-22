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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifest;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifestEntity;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author wseyler
 *
 */
public class Exporter {
  IUnifiedRepository unifiedRepository;
  private String repoPath;
  private String filePath;
  
  private File exportDir;

  private boolean withManifest;

  private ExportManifest exportManifest;
  protected DefaultUnifiedRepositoryWebService repoWs;

  private static String EXPORT_MANIFEST_FILENAME = "exportManifest.xml";
  private static String EXPORT_INFO_DATE_FORMAT = "dd-MM-yyyy";
  private static String EXPORT_INFO_TIME_FORMAT = "hh:mm:ss z";

  /**
   * @param unifiedRepository
   */
  public Exporter(IUnifiedRepository unifiedRepository) {
    this(unifiedRepository, null, null, false);
  }

  /**
   * @param unifiedRepository
   */
  public Exporter(IUnifiedRepository unifiedRepository, String path) {
    this(unifiedRepository, path, null, false);
  }


  /**
   * @param unifiedRepository
   * @param path (repo)
   * @param filePath
   */
  public Exporter(IUnifiedRepository unifiedRepository, String path, String filePath) {
    this(unifiedRepository, path, filePath, false);
  }

  /**
   * @param unifiedRepository
   * @param path (repo)
   * @param filePath
   */
  public Exporter(IUnifiedRepository unifiedRepository, String path, String filePath, boolean withManifest) {
    this.unifiedRepository = unifiedRepository;
    this.repoPath = path;
    this.filePath = filePath;
    this.withManifest = withManifest;

    if(withManifest){
      this.exportManifest = new ExportManifest();

      // set created by and create date in manifest information
      IPentahoSession session = PentahoSessionHolder.getSession();

      Date todaysDate = new Date();
      SimpleDateFormat dateFormat = new SimpleDateFormat(EXPORT_INFO_DATE_FORMAT);
      SimpleDateFormat timeFormat = new SimpleDateFormat(EXPORT_INFO_TIME_FORMAT);

      exportManifest.getManifestInformation().setExportBy(session.getName());
      exportManifest.getManifestInformation()
          .setExportDate(dateFormat.format(todaysDate) + " " + timeFormat.format(todaysDate));
    }
  }

  /**
   *
   * @param unifiedRepository
   * @param withManifest
   */
  public Exporter(IUnifiedRepository unifiedRepository, boolean withManifest){
    this(unifiedRepository, null, null, withManifest);
  }

  /**
   *
   * @throws IOException
   */
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

  /**
   *
   * @return
   * @throws IOException
   */
  public File doExportAsZip() throws IOException {
    RepositoryFile exportRepositoryFile = unifiedRepository.getFile(repoPath);
    return doExportAsZip(exportRepositoryFile, false);
  }

  /**
   * Create manifest and populate it while building zip
   * @param withManifest
   * @return
   */
  public File doExportAsZip(RepositoryFile exportRepositoryFile, boolean withManifest) throws IOException{
    File zipFile = File.createTempFile("repoExport", ".zip");
    zipFile.deleteOnExit();

    filePath = new File(repoPath).getParent();
    if (exportRepositoryFile == null) {
      throw new FileNotFoundException("JCR file not found: " + repoPath);
    }

    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
    if (exportRepositoryFile.isFolder()) {  // Handle recursive export
      ZipEntry entry = new ZipEntry(exportRepositoryFile.getPath().substring(filePath.length() + 1) + File.separator);
      zos.putNextEntry(entry);  	
      exportDirectoryAsZip(exportRepositoryFile, zos);
    } else {
      exportFileAsZip(exportRepositoryFile, zos);
    }

    // write manifest to zip output stream if requested
    if(withManifest){
      ZipEntry entry = new ZipEntry(EXPORT_MANIFEST_FILENAME);
      zos.putNextEntry(entry);

      // pass output stream to manifest class for writing
      try{
        exportManifest.toXml(zos);
      }
      catch(Exception e){

      }

      zos.closeEntry();
    }
    
    zos.close();
    return zipFile;
  }
  
  /**
   * @param repositoryDir
   * @param zos
   */
  private void exportDirectoryAsZip(RepositoryFile repositoryDir, ZipOutputStream zos) throws IOException {
    List<RepositoryFile> children = unifiedRepository.getChildren(repositoryDir.getId());
    for (RepositoryFile repoFile : children) {
      if (repoFile.isFolder()) {
    	ZipEntry entry = new ZipEntry(repoFile.getPath().substring(filePath.length() + 1) + File.separator);
    	zos.putNextEntry(entry);  
        exportDirectoryAsZip(repoFile, zos);
      } else {
        exportFileAsZip(repoFile, zos);
      }
    }
  }

  /**
   * @param exportRepositoryFile
   * @param zos
   */
  private void exportFileAsZip(RepositoryFile exportRepositoryFile, ZipOutputStream zos) throws IOException {

    // get entity instance for this file and add to manifest
    if(this.withManifest){
      RepositoryFileAcl fileAcl = unifiedRepository.getAcl(exportRepositoryFile.getId());
      this.exportManifest.add(new ExportManifestEntity(exportRepositoryFile, fileAcl));
    }

    ZipEntry entry = new ZipEntry(exportRepositoryFile.getPath().substring(filePath.length() + 1));
    zos.putNextEntry(entry);
    SimpleRepositoryFileData repoFileData = unifiedRepository.getDataForRead(exportRepositoryFile.getId(), SimpleRepositoryFileData.class);
    InputStream is = repoFileData.getStream();
    IOUtils.copy(is, zos);
    zos.closeEntry();
    is.close();
  }

  /**
   * @param repositoryDir
   * @param parentDir
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

  /**
   *
   * @param exportRepositoryFile
   * @param exportDirectory
   * @throws IOException
   */
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

  /**
   *
   * @return
   */
  public IUnifiedRepository getUnifiedRepository() {
    return unifiedRepository;
  }

  /**
   *
   * @param unifiedRepository
   */
  public void setUnifiedRepository(IUnifiedRepository unifiedRepository) {
    this.unifiedRepository = unifiedRepository;
  }

  /**
   *
   * @return
   */
  public String getRepoPath() {
    return repoPath;
  }

  /**
   *
   * @param repoPath
   */
  public void setRepoPath(String repoPath) {
    this.repoPath = repoPath;
  }

  /**
   *
   * @return
   */
  public String getFilePath() {
    return filePath;
  }

  /**
   *
   * @param filePath
   */
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  /**
   *
   * @return
   */
  public DefaultUnifiedRepositoryWebService getRepoWs() {
    return repoWs;
  }

  /**
   *
   * @param repoWs
   */
  public void setRepoWs(DefaultUnifiedRepositoryWebService repoWs) {
    this.repoWs = repoWs;
  }
}
