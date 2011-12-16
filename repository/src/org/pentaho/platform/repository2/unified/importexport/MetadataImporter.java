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
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository.pmd.IPentahoMetadataDomainRepositoryImporter;
import org.pentaho.platform.repository.pmd.PentahoMetadataFileInfo;

import java.io.IOException;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class MetadataImporter implements Importer {
  private static final Log log = LogFactory.getLog(MetadataImporter.class);
  private static final Messages message = Messages.getInstance();

  private IUnifiedRepository repository;
  private IPentahoMetadataDomainRepositoryImporter metadataRepositoryImporter;

  public MetadataImporter(final IUnifiedRepository repository) {
    this(repository, null);
  }

  public MetadataImporter(final IUnifiedRepository repository,
                          final IPentahoMetadataDomainRepositoryImporter metadataRepositoryImporter) {
    if (null == repository) {
      throw new IllegalArgumentException();
    }
    this.repository = repository;
    this.metadataRepositoryImporter = metadataRepositoryImporter;
  }

  public IUnifiedRepository getRepository() {
    return repository;
  }

  public IPentahoMetadataDomainRepositoryImporter getMetadataRepositoryImporter() {
    return metadataRepositoryImporter;
  }

  public void setMetadataRepositoryImporter(final IPentahoMetadataDomainRepositoryImporter metadataRepositoryImporter) {
    if (null == metadataRepositoryImporter) {
      throw new IllegalArgumentException();
    }
    this.metadataRepositoryImporter = metadataRepositoryImporter;
  }

  /**
   * @param importSource
   * @param comment
   * @param overwrite
   * @throws java.io.IOException
   */
  @Override
  public void doImport(final ImportSource importSource, final String comment, final boolean overwrite) throws IOException {
    if (null == importSource) {
      throw new IllegalArgumentException();
    }
    if (null == metadataRepositoryImporter) {
      final String error = message.getErrorString("MetadataImporter.ERROR_0004_IMPORTER_NOT_PROVIDED");
      throw new IllegalStateException(error);
    }

    try {
      final Iterable<ImportSource.IRepositoryFileBundle> files = importSource.getFiles();
      for (final ImportSource.IRepositoryFileBundle file : files) {
        final String filename = file.getFile().getName();
        final PentahoMetadataFileInfo fileInfo = new PentahoMetadataFileInfo(filename);
        final String domainId = fileInfo.getFilename();

        if (fileInfo.getFileType() == PentahoMetadataFileInfo.FileType.XMI) {
          log.debug("importing [" + filename + "] as metadata [domain=" + domainId + " : overwrite=" + overwrite + "]");
          metadataRepositoryImporter.storeDomain(file.getInputStream(), domainId, overwrite);
        } else if (fileInfo.getFileType() == PentahoMetadataFileInfo.FileType.PROPERTIES) {
          final String locale = fileInfo.getLocale();
          if (!StringUtils.isEmpty(locale)) {
            log.debug("Importing [" + filename + "] as properties - [domain=" + domainId + " : locale=" + locale + "]");
            metadataRepositoryImporter.addLocalizationFile(domainId, locale, file.getInputStream(), overwrite);
          } else {
            log.warn("Skipping properties file [" + file.getPath() + "] - can't determine locale");
          }
        } else {
          log.debug("Unknown type for filename [" + file.getPath() + "] - skipping");
        }
      }
    } catch (final DomainStorageException e) {
      final String error = message.getErrorString("MetadataImporter.ERROR_0001_DOMAIN_REPOSITORY_ERROR",
          e.getLocalizedMessage());
      log.error(error);
    } catch (final DomainAlreadyExistsException e) {
      log.error(e.getLocalizedMessage());
    } catch (final DomainIdNullException e) {
      log.error(e.getLocalizedMessage());
    }
  }
}
