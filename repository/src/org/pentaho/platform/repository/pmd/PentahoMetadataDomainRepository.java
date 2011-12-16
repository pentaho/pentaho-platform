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
package org.pentaho.platform.repository.pmd;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.LocalizationUtil;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository2.unified.RepositoryUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class PentahoMetadataDomainRepository implements IMetadataDomainRepository,
    IPentahoMetadataDomainRepositoryImporter, IPentahoMetadataDomainRepositoryExporter {
  private static final Log logger = LogFactory.getLog(PentahoMetadataDomainRepository.class);

  private static final String DOMAIN_ID = "domain-id";
  private static final String LOCALE = "locale";
  private static final String ENCODING = "UTF-8";
  private static final String MIME_TYPE = "text/xml";
  private static final String PROPERTIES_EXTENSION = "properties";

  private static final Messages messages = Messages.getInstance();

  private IUnifiedRepository repository;
  private XmiParser xmiParser;
  private RepositoryUtils repositoryUtils;
  private LocalizationUtil localizationUtil;


  public PentahoMetadataDomainRepository(final IUnifiedRepository repository) {
    this(repository, null, null, null);
  }

  public PentahoMetadataDomainRepository(final IUnifiedRepository repository,
                                         final RepositoryUtils repositoryUtils,
                                         final XmiParser xmiParser,
                                         final LocalizationUtil localizationUtil) {
    if (null == repository) {
      throw new IllegalArgumentException();
    }
    setRepository(repository);
    setRepositoryUtils(repositoryUtils);
    setLocalizationUtil(localizationUtil);
    setXmiParser(xmiParser);
  }

  /**
   * Store a domain to the repository.  The domain should persist between JVM restarts.
   *
   * @param domain    domain object to store
   * @param overwrite if true, overwrite existing domain
   * @throws org.pentaho.metadata.repository.DomainIdNullException
   *          if domain id is null
   * @throws org.pentaho.metadata.repository.DomainAlreadyExistsException
   *          if domain exists and overwrite = false
   * @throws org.pentaho.metadata.repository.DomainStorageException
   *          if there is a problem storing the domain
   */
  @Override
  public void storeDomain(final Domain domain, final boolean overwrite)
      throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
    logger.debug("storeDomain(domain(id=" + (domain != null ? domain.getId() : "") + ", " + overwrite + ")");
    if (null == domain || StringUtils.isEmpty(domain.getId())) {
      throw new DomainIdNullException(messages.getErrorString("PentahoMetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL"));
    }

    try {
      // NOTE - a ByteArrayInputStream doesn't need to be closed ...
      //  ... so this is safe AS LONG AS we use a ByteArrayInputStream
      final String xmi = xmiParser.generateXmi(domain);
      final InputStream inputStream = new ByteArrayInputStream(xmi.getBytes());
      storeDomain(inputStream, domain.getId(), overwrite);
    } catch (DomainStorageException dse) {
      throw dse;
    } catch (DomainAlreadyExistsException dae) {
      throw dae;
    } catch (Exception e) {
      final String errorMessage =
          messages.getErrorString("PentahoDomainMetadataRepository.ERROR_0003_ERROR_STORING_DOMAIN",
              domain.getId(), e.getLocalizedMessage());
      logger.error(errorMessage, e);
      throw new DomainStorageException(errorMessage, e);
    }
  }

  /**
   * Stores a domain to the repository directly as an Input Stream
   *
   * @param inputStream
   * @param domainId
   * @param overwrite
   */
  @Override
  public void storeDomain(final InputStream inputStream, final String domainId, final boolean overwrite)
      throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
    logger.debug("storeDomain(inputStream, " + domainId + ", " + overwrite + ")");
    if (null == inputStream) {
      throw new IllegalArgumentException();
    }
    if (StringUtils.isEmpty(domainId)) {
      throw new DomainIdNullException(messages.getErrorString("PentahoMetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL"));
    }

    // Check to see if the domain already exists
    final String domainPathName = computeDomainFilename(domainId);
    if (!overwrite) {
      final RepositoryFile existingDomain = repository.getFile(domainPathName);
      if (existingDomain != null) {
        final String errorString =
            messages.getErrorString("PentahoMetadataDomainRepository.ERROR_0002_DOMAIN_ALREADY_EXISTS", domainId);
        logger.error(errorString);
        logger.debug("\t" + existingDomain.getId() + " == " + domainId);
        throw new DomainAlreadyExistsException(errorString);
      }
    }

    // Store the domain in the repository
    final IRepositoryFileData data = new SimpleRepositoryFileData(inputStream, ENCODING, MIME_TYPE);
    final RepositoryFile repositoryFile = repositoryUtils.saveFile(domainPathName, data, true, overwrite, true, true, null);

    // This invalidates any caching
    flushDomains();

    // Store the metadata about this domain
    storeDomainMetadata(repositoryFile, domainId);
  }

  /**
   * retrieve a domain from the repo.  This does lazy loading of the repo, so it calls reloadDomains()
   * if not already loaded.
   *
   * @param domainId domain to get from the repository
   * @return domain object
   */
  @Override
  public Domain getDomain(final String domainId) {
    logger.debug("getDomain(" + domainId + ")");
    if (StringUtils.isEmpty(domainId)) {
      throw new IllegalArgumentException(
          messages.getErrorString("PentahoDomainMetadataRepository.ERROR_0004_DOMAIN_ID_INVALID", domainId));
    }

    final String filePath = computeDomainFilename(domainId);
    Domain domain = null;
    try {
      // Load the domain file
      final RepositoryFile file = repository.getFile(filePath);
      if (null != file) {
        SimpleRepositoryFileData data = repository.getDataForRead(file.getId(), SimpleRepositoryFileData.class);
        domain = xmiParser.parseXmi(data.getStream());
        domain.setId(domainId);
        logger.debug("loaded domain");

        // Load any I18N bundles
        final RepositoryFile dir = getMetadataDir();
        loadLocaleStrings(domainId, domain, dir);
        logger.debug("loaded I18N bundles");
      }
    } catch (Exception e) {
      throw new UnifiedRepositoryException(
          messages.getErrorString("PentahoDomainMetadataRepository.ERROR_0005_ERROR_RETRIEVING_DOMAIN",
              domainId, e.getLocalizedMessage()), e);
    }

    // Return
    return domain;
  }

  /**
   * return a list of all the domain ids in the repository.  triggers a call to reloadDomains if necessary.
   *
   * @return the domain Ids.
   */
  @Override
  public Set<String> getDomainIds() {
    logger.debug("getDomainIds()");
    Set<String> domainIds = new HashSet<String>();
    final RepositoryFile metadataFolder = repository.getFile(RepositoryMetadataInfo.getMetadataFolderPath());
    final List<RepositoryFile> children = repository.getChildren(metadataFolder.getId(),
        '*' + RepositoryMetadataInfo.getFileExtension());
    for (final RepositoryFile child : children) {
      final Map<String, Serializable> fileMetadata = repository.getFileMetadata(child.getId());
      if (null != fileMetadata && fileMetadata.containsKey(DOMAIN_ID)) {
        domainIds.add(fileMetadata.get(DOMAIN_ID).toString());
      }
    }
    return domainIds;
  }

  /**
   * remove a domain from disk and memory.
   *
   * @param domainId
   */
  @Override
  public void removeDomain(final String domainId) {
    logger.debug("removeDomain(" + domainId + ")");
    if (StringUtils.isEmpty(domainId)) {
      throw new IllegalArgumentException(
          messages.getErrorString("PentahoDomainMetadataRepository.ERROR_0004_DOMAIN_ID_INVALID", domainId));
    }

    // Get the metadata domain file
    final String domainFilename = computeDomainFilename(domainId);
    final RepositoryFile domainFile = repository.getFile(domainFilename);
    if (domainFile != null) {
      // This invalidates any caching
      flushDomains();

      // Delete the metadata domain file
      repository.deleteFile(domainFile.getId(), null);

      // Get and delete any I18N properties files
      final String localeBaseName = computeRepositorySafeName(domainId);
      final RepositoryFile metadataDir = getMetadataDir();
      final List<RepositoryFile> children = repository.getChildren(metadataDir.getId(), localeBaseName + "_*.properties");
      for (final RepositoryFile child : children) {
        final Map<String, Serializable> fileMetadata = repository.getFileMetadata(child.getId());
        if (fileMetadata != null && fileMetadata.containsKey(DOMAIN_ID) &&
            StringUtils.equals(domainId, fileMetadata.get(DOMAIN_ID).toString())) {
          repository.deleteFile(child.getId(), null);
        }
      }
    }
  }

  /**
   * remove a model from a domain which is stored either on a disk or memory.
   *
   * @param domainId
   * @param modelId
   */
  @Override
  public void removeModel(final String domainId, final String modelId) throws DomainIdNullException, DomainStorageException {
    logger.debug("removeModel(" + domainId + ", " + modelId + ")");
    if (StringUtils.isEmpty(domainId)) {
      throw new IllegalArgumentException(
          messages.getErrorString("PentahoDomainMetadataRepository.ERROR_0004_DOMAIN_ID_INVALID", domainId));
    }
    if (StringUtils.isEmpty(modelId)) {
      throw new IllegalArgumentException(
          messages.getErrorString("PentahoDomainMetadataRepository.ERROR_0006_MODEL_ID_INVALID"));
    }

    // Get the domain and remove the model
    final Domain domain = getDomain(domainId);
    if (null != domain) {
      boolean found = false;
      final Iterator<LogicalModel> iter = domain.getLogicalModels().iterator();
      while (iter.hasNext()) {
        LogicalModel model = iter.next();
        if (modelId.equals(model.getId())) {
          iter.remove();
          found = true;
          break;
        }
      }

      // Update the domain if we change it
      if (found) {
        try {
          storeDomain(domain, true);
        } catch (DomainAlreadyExistsException ignored) {
          // This can't happen since we have setup overwrite to true
        }
      }
    }
  }

  /**
   * reload domains from disk
   */
  @Override
  public void reloadDomains() {
    // This method does nothing since this implementation does not cache items
  }

  /**
   * flush the domains from memory
   */
  @Override
  public void flushDomains() {
    // This method does nothing since this implementation does not cache items
  }

  @Override
  public String generateRowLevelSecurityConstraint(final LogicalModel model) {
    // We will let subclasses handle this issue
    return null;
  }

  /**
   * The aclHolder cannot be null unless the access type requested is ACCESS_TYPE_SCHEMA_ADMIN.
   */
  @Override
  public boolean hasAccess(final int accessType, final IConcept aclHolder) {
    // We will let subclasses handle this computation
    return true;
  }

  /**
   * Adds a set of properties as a locale properties file for the specified Domain ID
   *
   * @param domainId   the domain ID for which this properties file will be added
   * @param locale     the locale for which this properties file will be added
   * @param properties the properties to be added
   */
  public void addLocalizationFile(final String domainId, final String locale, final Properties properties) throws DomainStorageException {
    // This is safe since ByteArray streams don't have to be closed
    if (null != properties) {
      try {
        final OutputStream out = new ByteArrayOutputStream();
        properties.store(out, null);
        addLocalizationFile(domainId, locale, new ByteArrayInputStream(out.toString().getBytes()), true);
      } catch (IOException e) {
        throw new DomainStorageException(
            messages.getErrorString("PentahoDomainMetadataRepository.ERROR_0008_ERROR_IN_REPOSITORY",
                e.getLocalizedMessage()), e);
      }
    }
  }

  @Override
  public void addLocalizationFile(final String domainId, final String locale, final InputStream inputStream,
                                  final boolean overwrite) throws DomainStorageException {
    logger.debug("addLocalizationFile(" + domainId + ", " + locale + ", inputStream)");
    if (null != inputStream) {
      if (StringUtils.isEmpty(domainId) || StringUtils.isEmpty(locale)) {
        throw new IllegalArgumentException(
            messages.getErrorString("PentahoDomainMetadataRepository.ERROR_0004_DOMAIN_ID_INVALID", domainId));
      }

      // This invalidates any cached information
      flushDomains();

      // Check for duplicates
      final String filename = computeRepositorySafeName(domainId) + '_' + locale + ".properties";
      if (!overwrite && !repository.getChildren(getMetadataDir().getId(), filename).isEmpty()) {
        throw new DomainStorageException(
            messages.getErrorString("PentahoDomainMetadataRepository.ERROR_0009_PROPERTIES_ALREADY_EXISTS", filename), null);
      }

      final RepositoryFile bundleFile = repository.createFile(getMetadataDir().getId(),
          new RepositoryFile.Builder(filename).build(),
          new SimpleRepositoryFileData(inputStream, ENCODING, MIME_TYPE), null);

      final Map<String, Serializable> bundleMetadata = new HashMap<String, Serializable>();
      bundleMetadata.put(DOMAIN_ID, domainId);
      bundleMetadata.put(LOCALE, locale);
      repository.setFileMetadata(bundleFile.getId(), bundleMetadata);
    }
  }

  protected RepositoryFile getMetadataDir() {
    final String metadataDirName = RepositoryMetadataInfo.getMetadataFolderPath();
    return repository.getFile(metadataDirName);
  }

  protected void storeDomainMetadata(final RepositoryFile domainFile, final String domainId) {
    // Store the information as metadata on the original metadata file
    final Map<String, Serializable> metadataMap = new HashMap<String, Serializable>();
    metadataMap.put(DOMAIN_ID, domainId);
    repository.setFileMetadata(domainFile.getId(), metadataMap);
  }

  protected void loadLocaleStrings(final String domainId, final Domain domain, final RepositoryFile dir) {
    if (null != dir) {
      final List<RepositoryFile> bundles = repository.getChildren(dir.getId(), "*." + PROPERTIES_EXTENSION);
      for (final RepositoryFile bundle : bundles) {
        final String locale = computeLocale(domainId, bundle.getName());
        final Properties properties = loadProperties(locale, bundle);
        localizationUtil.importLocalizedProperties(domain, properties, locale);
      }
    }
  }

  protected Properties loadProperties(final String locale, final RepositoryFile bundle) {
    try {
      Properties properties = null;
      if (!StringUtils.isEmpty(locale)) {
        final SimpleRepositoryFileData bundleData =
            repository.getDataForRead(bundle.getId(), SimpleRepositoryFileData.class);
        if (bundleData != null) {
          properties = new Properties();
          properties.load(bundleData.getStream());
        }
      }
      return properties;
    } catch (IOException e) {
      throw new UnifiedRepositoryException(
          messages.getErrorString("PentahoDomainMetadataRepository.ERROR_0008_ERROR_IN_REPOSITORY",
              e.getLocalizedMessage()), e);
    }
  }

  protected static String computeLocale(final String domainId, final String filename) {
    String locale = null;
    if (null != domainId && null != filename) {
      final String repoSafeDomainName = computeRepositorySafeName(domainId);
      if (filename.indexOf(repoSafeDomainName) == 0 && filename.length() > repoSafeDomainName.length()) {
        locale = RepositoryFilenameUtils.getBaseName(filename).substring(repoSafeDomainName.length() + 1);
      }
    }
    return locale;
  }

  protected static String computeRepositorySafeName(final String domainId) {
    return RepositoryUtils.generateRepositorySafeName(domainId);
  }

  protected static String computeDomainFilename(final String domainId) {
    return RepositoryFilenameUtils.concat(RepositoryMetadataInfo.getMetadataFolderPath(),
        computeRepositorySafeName(domainId) + RepositoryMetadataInfo.getFileExtension());
  }

  protected IUnifiedRepository getRepository() {
    return repository;
  }

  protected XmiParser getXmiParser() {
    return xmiParser;
  }

  protected RepositoryUtils getRepositoryUtils() {
    return repositoryUtils;
  }

  protected LocalizationUtil getLocalizationUtil() {
    return localizationUtil;
  }

  protected void setRepository(final IUnifiedRepository repository) {
    this.repository = repository;
  }

  protected void setXmiParser(final XmiParser xmiParser) {
    this.xmiParser = (xmiParser != null ? xmiParser : new XmiParser());
  }

  protected void setRepositoryUtils(final RepositoryUtils repositoryUtils) {
    this.repositoryUtils = (repositoryUtils != null ? repositoryUtils : new RepositoryUtils(repository));
  }

  protected void setLocalizationUtil(final LocalizationUtil localizationUtil) {
    this.localizationUtil = (localizationUtil != null ? localizationUtil : new LocalizationUtil());
  }
}
