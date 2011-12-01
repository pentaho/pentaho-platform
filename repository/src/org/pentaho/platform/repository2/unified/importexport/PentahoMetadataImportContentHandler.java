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
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.metadata.PentahoMetadataDomainRepository;

import java.util.Map;

/**
 * Performs the import process on for Pentaho Metadata files (detected with an extension of {@code xmi}
 * User: dkincade
 */
public class PentahoMetadataImportContentHandler extends BaseImportContentHandler {
  private static final Log logger = LogFactory.getLog(PentahoMetadataImportContentHandler.class);
  private IMetadataDomainRepository domainRepository;
  private XmiParser xmiParser;

  /**
   * Returns a simple name to describe this ImportContentHandler
   */
  @Override
  public String getName() {
    return "PentahoMetadataImportContentHandler";
  }

  /**
   * Performs any initialization required prior to handling any import processing
   *
   * @param repository            the {@link org.pentaho.platform.api.repository2.unified.IUnifiedRepository} into which content is being imported
   * @param converters            (ignored)
   * @param destinationFolderPath (ignored)
   * @param versionMessage        (ignored)
   */
  @Override
  public void initialize(final IUnifiedRepository repository,
                         final Map<String, Converter> converters,
                         final String destinationFolderPath,
                         final String versionMessage) throws InitializationException {
    super.initialize(repository, converters, destinationFolderPath, versionMessage);

    if (null == domainRepository) {
      domainRepository = new PentahoMetadataDomainRepository(repository);
    }
    if (null == xmiParser) {
      xmiParser = new XmiParser();
    }
  }

  /**
   * Attempts to perform the import process on the bundle. The handler will determine if it should handle the import
   * process for this content and will perform the processing specific to this content and type. If this handler
   * completes the import process and knows that all other handlers should be skipped, it will return {@code true}.
   * Otherwise it will return {@code false}.
   *
   * @param bundle    the information being imported
   * @param overwrite indicates if this content handler should overwrite existing content with this new content
   * @return {@code true} if processing on this bundle should continue by other handlers, {@code false} otherwise.
   * @throws ImportException indicates an error trying to perform the import process on this content
   */
  @Override
  public Result performImport(final ImportSource.IRepositoryFileBundle bundle, final boolean overwrite) throws ImportException {
    if (null == bundle || null == bundle.getPath() || null == bundle.getFile() || null == bundle.getFile().getName()) {
      logger.debug("skipping import with null bundle or bundle path");
      return Result.SKIPPED;
    }

    // See if it is an xmi file
    final String bundlePathName = RepositoryFilenameUtils.concat(bundle.getPath(), bundle.getFile().getName());
    logger.debug("Checking bundle with path [" + bundlePathName + "]");
    final String extension = RepositoryFilenameUtils.getExtension(bundlePathName);
    if (!StringUtils.equals("xmi", extension)) {
      // We need to skip it
      logger.debug("\tskipping bundle with extension [" + extension + "]");
      return Result.SKIPPED;
    }

    try {
      // Compute the domain ID from the path name
      final String domainId = computeDomainId(bundlePathName);
      logger.debug("Computed domainID to be ["+domainId+"]");

      // Load the XMI file into a Domain object
      logger.debug("Loading Pentaho Metadata bundle with path [" + bundlePathName + "]");
      final Domain domain = xmiParser.parseXmi(bundle.getInputStream());
      domain.setId(domainId);

      logger.debug("Storing Pentaho Metadata bundle with domain id [" + domainId + "]");
      domainRepository.storeDomain(domain, overwrite);
    } catch (Exception e) {
      final String errorMessage =
          messages.getErrorString("PentahoMetadataImportContentHandler.ERROR_0001_FAILED_IMPORTING_PENTAHO_METADATA",
           bundlePathName, e.getLocalizedMessage());
      logger.error(errorMessage);
      throw new ImportException(errorMessage, e);
    }

    // We need to process this
    logger.debug("Completed import of bundle " + bundlePathName);
    return Result.SUCCESS;
  }

  public IMetadataDomainRepository getDomainRepository() {
    return domainRepository;
  }

  public void setDomainRepository(final IMetadataDomainRepository domainRepository) {
    if (null == domainRepository) {
      throw new NullPointerException();
    }
    this.domainRepository = domainRepository;
  }

  public XmiParser getXmiParser() {
    return xmiParser;
  }

  public void setXmiParser(final XmiParser xmiParser) {
    if (null == xmiParser) {
      throw new NullPointerException();
    }
    this.xmiParser = xmiParser;
  }

  protected String computeDomainId(final String bundlePathName) {
    return RepositoryFilenameUtils.getBaseName(bundlePathName);
  }

}
