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
 * Copyright 2011 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.plugin.services.importexport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class SimpleImportProcessor implements ImportProcessor {
  private static final Log log = LogFactory.getLog(SimpleImportProcessor.class);

  private final String destinationPath;
  private final String comment;
  private ImportSource importSource;
  private List<ImportHandler> importHandlerList;

  public SimpleImportProcessor(final String destinationPath, final String comment) {
    if (StringUtils.isEmpty(destinationPath)) {
      throw new IllegalArgumentException();
    }
    this.destinationPath = RepositoryFilenameUtils.separatorsToRepository(destinationPath);
    this.comment = comment;
    this.importHandlerList = new ArrayList<ImportHandler>();
  }

  /**
   * Sets the {@link ImportSource} for this import processor
   *
   * @param importSource
   */
  @Override
  public void setImportSource(final ImportSource importSource) {
    if (importSource == null) {
      throw new IllegalArgumentException();
    }
    this.importSource = importSource;
  }

  /**
   * Adds an {@link ImportHandler} to the end of the list of Import Handlers.
   *
   * @param importHandler
   */
  @Override
  public void addImportHandler(final ImportHandler importHandler) {
    importHandlerList.add(importHandler);
  }

  /**
   * Performs the import process
   *
   * @throws ImportException indicates an error in import processing
   */
  @Override
  public void performImport() throws ImportException {
    try {
      final Iterable<ImportSource.IRepositoryFileBundle> files = importSource.getFiles();
      for (final ImportHandler importHandler : importHandlerList) {
        try {
          log.info("Trying import handler [" + importHandler.getName() + "] with a file set of size " + importSource.getCount() + "...");
          importHandler.doImport(files, destinationPath, comment, true);
        } catch (ImportException e) {
          log.error("Error using import handler [" + importHandler.getName() + "] (skipping) - " + e.getLocalizedMessage()); // TODO I18N
        }
      }
    } catch (IOException e) {
      e.printStackTrace(); // TODO I18N
    }
    log.info("Completed set of import handlers - files not imported = " + importSource.getCount());
  }
}
