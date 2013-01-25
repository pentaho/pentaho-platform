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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class DefaultExportHandler implements ExportHandler {
  private static final Log log = LogFactory.getLog(SimpleImportProcessor.class);

  private Map<String, Converter> converters;

  private IUnifiedRepository repository;

  /**
   * Perform export with registered handlers
   */
  @Override
  public InputStream doExport(RepositoryFile repositoryFile, String filePath) throws ExportException, IOException {
    InputStream is = null;

    // Compute the file extension
    final String name = repositoryFile.getName();
    final String ext = RepositoryFilenameUtils.getExtension(name);
    if (StringUtils.isEmpty(ext)) {
      log.debug("Skipping file without extension: " + name);
    }

    // Find the converter - defined in spring xml
    final Converter converter = converters.get(ext);
    if (converter == null) {
      log.debug("Skipping file without converter: " + name);
      return null;
   }

    // just send the converter the file id and let it decide which type to get
    // since it is already based on the file extension
    return converter.convert(repositoryFile.getId());
  }

  public void setConverters(Map<String, Converter> converters) {
    this.converters = converters;
  }

  public void setRepository(IUnifiedRepository repository) {
    this.repository = repository;
  }
}
