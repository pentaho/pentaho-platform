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
 * Time: 4:52 PM
 */

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.plugin.services.importexport.pdi.PDIImportUtil;
import org.pentaho.platform.plugin.services.importexport.pdi.StreamToJobNodeConverter;
import org.pentaho.platform.plugin.services.importexport.pdi.StreamToTransNodeConverter;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifest;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifestEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DefaultExportHandler implements ExportHandler {
  private static final Log log = LogFactory.getLog(SimpleImportProcessor.class);
  private static final Map<String, Converter> converters = new HashMap<String, Converter>();

  private final IUnifiedRepository unifiedRepository;
  private ExportManifest exportManifest;

  /**
   *
   * @param repository
   */
  public DefaultExportHandler(final IUnifiedRepository repository) {
    // Validate and save the repository

    // todo - check ezequiel's changes for checking file types using mime-type during import and use similar method
    if (null == repository) {
      throw new IllegalArgumentException();
    }

    this.unifiedRepository = repository;

    final StreamConverter streamConverter = new StreamConverter(repository);
    converters.put("prpt", streamConverter);
    converters.put("prpti", streamConverter);
    converters.put("mondrian.xml", streamConverter);
    converters.put("report", streamConverter);
    converters.put("rptdesign", streamConverter);
    converters.put("svg", streamConverter);
    converters.put("url", streamConverter);
    converters.put("xaction", streamConverter);
    converters.put("xanalyzer", streamConverter);
    converters.put("xcdf", streamConverter);
    converters.put("xdash", streamConverter);
    converters.put("xreportspec", streamConverter);
    converters.put("waqr.xaction", streamConverter);
    converters.put("xwaqr", streamConverter);
    converters.put("gif", streamConverter);
    converters.put("css", streamConverter);
    converters.put("html", streamConverter);
    converters.put("htm", streamConverter);
    converters.put("jpg", streamConverter);
    converters.put("jpeg", streamConverter);
    converters.put("js", streamConverter);
    converters.put("cfg.xml", streamConverter);
    converters.put("jrxml", streamConverter);
    converters.put("png", streamConverter);
    converters.put("properties", streamConverter);
    converters.put("sql", streamConverter);
    converters.put("xmi", streamConverter);
    converters.put("xml", streamConverter);
    converters.put("cda", streamConverter); //$NON-NLS-1$

    try {
      PDIImportUtil.connectToRepository(null);
      final StreamToJobNodeConverter jobConverter = new StreamToJobNodeConverter(repository);
      final StreamToTransNodeConverter transConverter = new StreamToTransNodeConverter(repository);
      converters.put("kjb", jobConverter);
      converters.put("ktr", transConverter);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("No PDI Repository found, switching to standard converter.");
      converters.put("kjb", streamConverter);
      converters.put("ktr", streamConverter);
    }
  }

  /**
   * Perform export with registered handlers
   */
  @Override
  public void doExport(RepositoryFile repositoryFile, ZipOutputStream outputStream, String filePath, ExportManifest exportManifest) throws ExportException, IOException {
    RepositoryFileAcl fileAcl = unifiedRepository.getAcl(repositoryFile.getId());
    exportManifest.add(new ExportManifestEntity(repositoryFile, fileAcl));
    ZipEntry entry = new ZipEntry(repositoryFile.getPath().substring(filePath.length() + 1));
    outputStream.putNextEntry(entry);

    // Compute the file extension
    final String name = repositoryFile.getName();
    final String ext = RepositoryFilenameUtils.getExtension(name);
    if (StringUtils.isEmpty(ext)) {
      log.debug("Skipping file without extension: " + name);
    }

    // Find the converter
    final Converter converter = converters.get(ext);
    if (converter == null) {
      log.debug("Skipping file without converter: " + name);
   }

    // just send the converter the file id and let it decide which type to get
    // since it is already based on the file extension
    InputStream is = converter.convert(repositoryFile.getId());

    IOUtils.copy(is, outputStream);
    outputStream.closeEntry();
    is.close();
  }

    /**
   *
   * @return
   */
  public ExportManifest getExportManifest() {
    return exportManifest;
  }

  /**
   *
   * @param exportManifest
   */
  public void setExportManifest(ExportManifest exportManifest) {
    this.exportManifest = exportManifest;
  }
}
