package org.pentaho.platform.plugin.services.importexport;/*
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
 * User: pminutillo
 * Date: 1/16/13
 * Time: 4:41 PM
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifest;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.reporting.libraries.libsparklines.util.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SimpleExportProcessor implements ExportProcessor {
  private static final Log log = LogFactory.getLog(SimpleImportProcessor.class);

  private String path;

  List<ExportHandler> exportHandlerList;

  private ExportManifest exportManifest;
  protected DefaultUnifiedRepositoryWebService repoWs;
  IUnifiedRepository unifiedRepository;

  private static String EXPORT_MANIFEST_FILENAME = "exportManifest.xml";
  private static String EXPORT_INFO_DATE_FORMAT = "dd-MM-yyyy";
  private static String EXPORT_INFO_TIME_FORMAT = "hh:mm:ss z";
  private static String EXPORT_TEMP_FILENAME_PREFIX = "repoExport";
  private static String EXPORT_TEMP_FILENAME_EXT = ".zip";

   /**
   * Encapsulates the logic of registering import handlers, generating the manifest,
    * and performing the export
   */
  public SimpleExportProcessor(String path, IUnifiedRepository repository){
    // set a default path at root if missing
    if(StringUtils.isEmpty(path)){
      this.path = "/";
    }
    else{
      this.path = path;
    }

    this.unifiedRepository = repository;

    this.exportHandlerList = new ArrayList<ExportHandler>();

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

  /**
   * Adds an {@link org.pentaho.platform.plugin.services.importexport.ExportHandler} to the end of the list of Export Handlers. The first ExportHandler added
   * will be the first to get a chance to process the data
   *
   * @param exportHandler
   */
  @Override
  public void addExportHandler(ExportHandler exportHandler) {
    exportHandlerList.add(exportHandler);
  }

  /**
   * Performs the export process, returns a zip File object
   *
   * @throws org.pentaho.platform.plugin.services.importexport.ExportException
   *          indicates an error in import processing
   */
  @Override
  public File performExport(RepositoryFile exportRepositoryFile) throws ExportException, IOException {
    File exportFile = null;

    // create temp file
    exportFile = File.createTempFile(EXPORT_TEMP_FILENAME_PREFIX, EXPORT_TEMP_FILENAME_EXT);
    exportFile.deleteOnExit();

    // get the file path
    String filePath = new File(this.path).getParent();

    // send a response right away if not found
    if (exportRepositoryFile == null) {
      // todo: add to messages.properties
      throw new FileNotFoundException("JCR file not found: " + this.path);
    }

    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(exportFile));

    if (exportRepositoryFile.isFolder()) {  // Handle recursive export
      ZipEntry entry = new ZipEntry(exportRepositoryFile.getPath().substring(filePath.length() + 1) + File.separator);
      zos.putNextEntry(entry);
      exportDirectory(exportRepositoryFile, zos, filePath);
    } else {
      exportFile(exportRepositoryFile, zos, filePath);
    }

    // write manifest to zip output stream
    ZipEntry entry = new ZipEntry(EXPORT_MANIFEST_FILENAME);
    zos.putNextEntry(entry);

    // pass output stream to manifest class for writing
    try{
      exportManifest.toXml(zos);
    }
    catch(Exception e){
      // todo: add to messages.properties
      log.error("Error generating export XML");
    }

    zos.closeEntry();

    zos.close();

    // clean up
    exportManifest = null;
    zos = null;

    return exportFile;
  }

  /**
   *
   * @param repositoryFile
   * @param outputStream
   */
  @Override
  public void exportFile(RepositoryFile repositoryFile, ZipOutputStream outputStream, String filePath) throws ExportException, IOException{
    // iterate through handlers to perform export
    for(ExportHandler exportHandler : exportHandlerList){
      exportHandler.doExport(repositoryFile, outputStream, filePath, this.exportManifest);
    }
  }

  /**
   *
   * @param repositoryDir
   * @param outputStream
   */
  @Override
  public void exportDirectory(RepositoryFile repositoryDir, ZipOutputStream outputStream, String filePath) throws ExportException, IOException{
    List<RepositoryFile> children = this.unifiedRepository.getChildren(repositoryDir.getId());
    for (RepositoryFile repositoryFile : children) {
      if (repositoryFile.isFolder()) {
        ZipEntry entry = new ZipEntry(repositoryFile.getPath().substring(filePath.length() + 1) + File.separator);
        outputStream.putNextEntry(entry);
        exportDirectory(repositoryFile, outputStream, filePath);
      } else {
        exportFile(repositoryFile, outputStream, filePath);
      }
    }

  }

  /**
   *
   * @return
   */
  public String getPath() {
    return path;
  }

  /**
   *
   * @param path
   */
  public void setPath(String path) {
    this.path = path;
  }

  public IUnifiedRepository getUnifiedRepository() {
    return unifiedRepository;
  }

  public void setUnifiedRepository(IUnifiedRepository unifiedRepository) {
    this.unifiedRepository = unifiedRepository;
  }
}
