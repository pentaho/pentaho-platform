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
package org.pentaho.platform.repository2.unified.importexport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository.pmd.IPentahoMetadataDomainRepositoryImporter;
import org.pentaho.platform.repository.pmd.PentahoMetadataDomainRepository;
import org.pentaho.platform.repository.pmd.PentahoMetadataFileInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class MetadataImportHandler implements ImportHandler {
  private static final Log log = LogFactory.getLog(MetadataImportHandler.class);
  private static final Messages messages = Messages.getInstance();

  private IUnifiedRepository repository;
  private IPentahoMetadataDomainRepositoryImporter metadataImporter;

  public MetadataImportHandler(final IUnifiedRepository repository) {
    if (null == repository) {
      throw new IllegalArgumentException();
    }
    this.repository = repository;
  }

  protected IPentahoMetadataDomainRepositoryImporter getMetadataImporter() {
    if (metadataImporter == null) {
      metadataImporter = new PentahoMetadataDomainRepository(repository);
    }
    return metadataImporter;
  }

  /**
   * Processes the list of files and performs any processing required to import that data into the repository. If
   * during processing it handles file(s) which should not be handled by downstream import handlers, then it
   * should remove them from the set of files provided.
   *
   * @param importFileSet   the set of files to be imported - any files handled to completion by this Import Handler
   *                        should remove this files from this list
   * @param destinationPath the requested destination location in the repository
   * @param comment         the import comment provided
   * @param overwrite       indicates if the process is authorized to overwrite existing content in the repository
   * @throws ImportException indicates a significant error during import processing
   */
  @Override
  public void doImport(final Iterable<ImportSource.IRepositoryFileBundle> importFileSet, final String destinationPath,
                       final String comment, final boolean overwrite) throws ImportException {
    if (null == importFileSet) {
      throw new IllegalArgumentException();
    }

    // Create a list of processed metadata files to save
    final Map<String, String> processedDomains = new HashMap<String, String>();
    boolean potentialMissedLocaleFiles = false;

    // Find the metadata files
    for (Iterator iterator = importFileSet.iterator(); iterator.hasNext(); ) {
      final ImportSource.IRepositoryFileBundle file = (ImportSource.IRepositoryFileBundle) iterator.next();
      final String name = file.getFile().getName();
      final String extension = RepositoryFilenameUtils.getExtension(name);
      final String fullFilename = RepositoryFilenameUtils.concat(file.getPath(), name);
      if (StringUtils.equals("xmi", extension)) {
        final String domainId = processMetadataFile(file, fullFilename, overwrite);
        if (!StringUtils.isEmpty(domainId)) {
          processedDomains.put(domainId, fullFilename);
          iterator.remove();
        }
      } else if (StringUtils.equals("properties", extension)) {
        final PentahoMetadataFileInfo info = new PentahoMetadataFileInfo(fullFilename);
        if (info.getLocale() != null && processedDomains.containsKey(info.getDomainId())) {
          processLocaleFile(file, info, overwrite);
          iterator.remove();
        } else {
          potentialMissedLocaleFiles = potentialMissedLocaleFiles || (info.getLocale() != null);

        }
      }
    }

    // If there are potentially missed locale files, we need to make a 2nd pass
    if (potentialMissedLocaleFiles) {
      for (Iterator iterator = importFileSet.iterator(); iterator.hasNext(); ) {
        final ImportSource.IRepositoryFileBundle file = (ImportSource.IRepositoryFileBundle) iterator.next();
        final String name = file.getFile().getName();
        final String extension = RepositoryFilenameUtils.getExtension(name);
        if (StringUtils.equals("properties", extension)) {
          final String fullFilename = RepositoryFilenameUtils.concat(file.getPath(), name);
          final PentahoMetadataFileInfo info = new PentahoMetadataFileInfo(fullFilename);
          if (info.getLocale() != null && processedDomains.containsKey(info.getDomainId())) {
            processLocaleFile(file, info, overwrite);
            iterator.remove();
          }
        }
      }
    }
  }

  /**
   * Processes the file as a metadata file and returns the domain name. It will import the file into the
   * Pentaho Metadata Domain Repository.
   *
   * @param file
   * @param filename
   * @return
   */
  protected String processMetadataFile(final ImportSource.IRepositoryFileBundle bundle, final String filename,
                                       final boolean overwrite) throws ImportException {
    try {
      final PentahoMetadataFileInfo info = new PentahoMetadataFileInfo(filename);
      log.debug("Importing [" + info.getPath() + "] as metadata - [domain=" + info.getDomainId() + "]");
      if (!StringUtils.isEmpty(info.getDomainId())) {
        log.debug("importing [" + filename + "] as metadata [domain=" + info.getDomainId() + " : overwrite=" + overwrite + "]");
        getMetadataImporter().storeDomain(bundle.getInputStream(), info.getDomainId(), overwrite);
      }
      return info.getDomainId();
    } catch (Exception e) {
      final String errorMessage = messages.getErrorString("", e.getLocalizedMessage()); // TODO I18N
      log.error(errorMessage);
    }
    return null;
  }

  private void processLocaleFile(final ImportSource.IRepositoryFileBundle file, final PentahoMetadataFileInfo info, final boolean overwrite) {
    try {
      log.debug("Importing [" + info.getPath() + "] as properties - [domain=" + info.getDomainId() + " : locale=" + info.getLocale() + "]");
      getMetadataImporter().addLocalizationFile(info.getDomainId(), info.getLocale(), file.getInputStream(), overwrite);
    } catch (Exception e) {
      final String errorMessage = messages.getErrorString("", e.getLocalizedMessage()); // TODO I18N
      log.error(errorMessage);
    }
  }

  /**
   * Returns the name of this Import Handler
   */
  @Override
  public String getName() {
    return "PentahoMetadataImportHandler";
  }
}
