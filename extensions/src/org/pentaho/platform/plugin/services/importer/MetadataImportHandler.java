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
package org.pentaho.platform.plugin.services.importer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.plugin.services.importexport.PentahoMetadataFileInfo;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryImporter;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>, nbaker
 */
public class MetadataImportHandler implements IPlatformImportHandler {
  private static final Log log = LogFactory.getLog(MetadataImportHandler.class);

  private static final Messages messages = Messages.getInstance();

  IPentahoMetadataDomainRepositoryImporter metadataRepositoryImporter;

  public MetadataImportHandler(final IPentahoMetadataDomainRepositoryImporter metadataImporter) {
    if (metadataImporter == null) {
      throw new IllegalArgumentException();
    }
    this.metadataRepositoryImporter = metadataImporter;
  }

  @Override
  public void importFile(IPlatformImportBundle file) throws PlatformImportException {

    String domainId = processMetadataFile(file);

    // bundle may have language files supplied with it.
    if (file.getChildBundles() != null) {
      for (IPlatformImportBundle child : file.getChildBundles()) {
        processLocaleFile(child, domainId);
      }
    }

  }

  /**
   * Processes the file as a metadata file and returns the domain name. It will import the file into the
   * Pentaho Metadata Domain Repository.
   *
   * @param bundle
   * @return
   */
  protected String processMetadataFile(final IPlatformImportBundle bundle) throws PlatformImportException {
    final String domainId = (String) bundle.getProperty("domain-id");

    if (domainId == null) {
      throw new PlatformImportException("Bundle missing required domain-id property");
    }
    try {
      log.debug("Importing as metadata - [domain=" + domainId + "]");
      metadataRepositoryImporter.storeDomain(bundle.getInputStream(), domainId, true);
      return domainId;
    } catch (Exception e) {
      final String errorMessage = messages.getErrorString("MetadataImportHandler.ERROR_0001_IMPORTING_METADATA",
          domainId, e.getLocalizedMessage());
      log.error(errorMessage, e);
    }
    return null;
  }

  private void processLocaleFile(final IPlatformImportBundle bundle, String domainId) throws PlatformImportException {
    final String fullFilename = RepositoryFilenameUtils.concat("/", bundle.getName());
    final PentahoMetadataFileInfo info = new PentahoMetadataFileInfo(fullFilename);

    if (domainId == null) {
      // try to resolve domainId from bundle
      domainId = (String) bundle.getProperty("domain-id");
    }
    if (domainId == null) {
      throw new PlatformImportException("Bundle missing required domain-id property");
    }
    try {
      log.debug("Importing [" + info.getPath() + "] as properties - [domain=" + domainId + " : locale="
          + info.getLocale() + "]");
      metadataRepositoryImporter.addLocalizationFile(domainId, info.getLocale(), bundle.getInputStream(), true);

    } catch (Exception e) {
      final String errorMessage = messages.getErrorString("MetadataImportHandler.ERROR_0002_IMPORTING_LOCALE_FILE",
          info.getPath(), domainId, info.getLocale(), e.getLocalizedMessage());
      log.error(errorMessage, e);
    }
  }


}
