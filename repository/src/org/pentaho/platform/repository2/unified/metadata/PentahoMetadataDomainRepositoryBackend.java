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
package org.pentaho.platform.repository2.unified.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.RepositoryUtils;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.pms.core.exception.PentahoMetadataException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Backend implementation to store Pentaho Metadata into the Unified repository
 * User: dkincade
 */
class PentahoMetadataDomainRepositoryBackend {
  private static final Log logger = LogFactory.getLog(PentahoMetadataDomainRepositoryBackend.class);
  private static final Messages messages = Messages.getInstance();

  private IUnifiedRepository repository;
  private RepositoryUtils repositoryUtils;
  private JcrMetadataInfo jcrMetadataInfo = new JcrMetadataInfo();
  private XmiParser xmiParser;
  private String characterEncoding = "UTF-8";
  private String mimeType = "text/plain";

  /**
   * Creates a Metadata Domain Repository backend using the specified repository
   *
   * @param repository
   */
  PentahoMetadataDomainRepositoryBackend(final IUnifiedRepository repository) {
    setRepository(repository);
  }

  /**
   * Returns the set of Metadata Domains currently stored in the Unified Repository. The key for the set is the
   * {@code Domain IDs} and the values is a {@link RepositoryFile} object which will be used to load the
   * {@link Domain} object
   *
   * @return a map of Domain IDs
   * @throws Exception indicates an error loading the map from the repository
   */
  public Map<String, RepositoryFile> loadDomainMappingFromRepository() throws Exception {
    logger.debug("loadDomainMappingFromRepository()");
    final Map<String, RepositoryFile> mappings = loadMappingFile();
    logger.debug("loadDomainMappingFromRepository() - returning map with element count " + mappings.size());
    dumpMappings();
    return mappings;
  }

  /**
   * Loads a {@link Domain} from the repository
   *
   * @param domainId       the domain ID of the metadata file to be loaded
   * @param repositoryFile the {@link RepositoryFile} which represents the metadata location
   * @return the loaded metadata {@link Domain} object
   * @throws Exception indicates an error loading the metadata object
   */
  public Domain loadDomain(final String domainId, final RepositoryFile repositoryFile) throws Exception {
    final String methodCall = "loadDomain(\"" + domainId + "\", " + repositoryFile.toString() + ")...";
    logger.debug(methodCall);

    final Domain domain = loadDomainFromRepository(domainId, repositoryFile);
    logger.debug(methodCall + " - returning Domain " + domain);
    dumpMappings();
    return domain;
  }

  /**
   * Removes the specified Domain ID from the internal repository
   *
   * @param domainId the domain to remove
   * @throws Exception indicates an error removing the domain from the repository
   */
  public void removeDomainFromRepository(final String domainId) throws Exception {
    final String methodCall = "removeDomainFromRepository(\"" + domainId + "\")";
    logger.debug(methodCall);

    // Remove domain from the list in the repository
    final Properties mappingProperties = getMetadataMappingFile();
    final String repoLocation = mappingProperties.getProperty(domainId);
    if (null != repoLocation) {
      logger.debug(methodCall + " - repoLocation = [" + repoLocation + "] ... rewriting the mapping file without it");
      mappingProperties.remove(domainId);
      writeMappingPropertiesFiles(mappingProperties);

      logger.debug(methodCall + " - getting the RepositoryFile object");
      final RepositoryFile metadataFolder = repository.getFile(repoLocation);
      if (null != metadataFolder) {
        logger.debug(methodCall + " - deleting the metadata folder");
        repository.deleteFile(metadataFolder.getId(),
            messages.getString("MetadataDomainRepositoryBackend.REMOVE_DOMAIN_COMMENT", domainId));
      } else {
        logger.debug(methodCall + " - could not find the RepositoryFile ... it must be gone");
      }
    } else {
      logger.debug(methodCall + " - could not find the RepositoryFile location ... it must be gone already");
    }
    logger.debug(methodCall + " - done");
    dumpMappings();
  }

  /**
   * Adds the metadata domain to the repository
   *
   * @param domain the domain to add
   * @throws Exception indicates an error saving the metadata domain to the repository
   */
  public void addDomainToRepository(final Domain domain) throws Exception {
    final String domainId = domain.getId();
    final String methodCall = "addDomainToRepository([" + domainId + "])";
    logger.debug(methodCall);

    // Add the new metadata.xml file to the metadata repository
    final String domainPath = jcrMetadataInfo.getMetadataFolderPath() + RepositoryFile.SEPARATOR + domainId;
    InputStream inputStream = null;
    try {
      logger.debug(methodCall + " - parsing the XMI file");
      final String xmi = getXmiParser().generateXmi(domain);
      inputStream = new ByteArrayInputStream(xmi.getBytes(LocaleHelper.getSystemEncoding()));

      // Create the file in the folder
      logger.debug(methodCall + " - creating the metadata file in the domain folder");
      final String domainFile = domainPath + RepositoryFile.SEPARATOR + jcrMetadataInfo.getMetadataFilename();
      IRepositoryFileData repoFileData = new SimpleRepositoryFileData(inputStream, characterEncoding, mimeType);
      final RepositoryFile metadataFile = repositoryUtils.getFile(domainFile, repoFileData, true,
          true, "");
      logger.debug(methodCall + " - completed!");
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }

    // Add the domain id to the mappings
    logger.debug(methodCall + " - rewriting the mapping file after adding domain and path " + domainPath);
    final Properties mappingProperties = getMetadataMappingFile();
    mappingProperties.setProperty(domainId, domainPath);
    writeMappingPropertiesFiles(mappingProperties);

    dumpMappings();
  }

  /**
   * @return the {@link IUnifiedRepository} currently being used by the backend
   */
  public IUnifiedRepository getRepository() {
    return repository;
  }

  /**
   * Sets the {@link IUnifiedRepository} to be used by this backend
   */
  public void setRepository(final IUnifiedRepository repository) {
    if (null == repository) {
      throw new NullPointerException(
          messages.getErrorString("MetadataDomainRepositoryBackend.ERROR_0001_REPOSITORY_NULL"));
    }
    this.repository = repository;
    this.repositoryUtils = new RepositoryUtils(repository);
  }

  public String getCharacterEncoding() {
    return characterEncoding;
  }

  public void setCharacterEncoding(final String characterEncoding) {
    if (null == characterEncoding) {
      throw new NullPointerException();
    }
    this.characterEncoding = characterEncoding;
  }

  public void setJcrMetadataInfo(final JcrMetadataInfo jcrMetadataInfo) {
    this.jcrMetadataInfo = jcrMetadataInfo;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(final String mimeType) {
    if (null == mimeType) {
      throw new NullPointerException();
    }
    this.mimeType = mimeType;
  }

  protected Domain loadDomainFromRepository(final String domainId, final RepositoryFile xmiFile) throws Exception {
    final InputStream xmiInputStream = new RepositoryFileInputStream(xmiFile, repository);
    try {
      final Domain domain = getXmiParser().parseXmi(xmiInputStream);
      domain.setId(domainId);
      return domain;
    } finally {
      xmiInputStream.close();
    }
  }

  protected Properties getMetadataMappingFile() throws IOException, PentahoMetadataException {
    final RepositoryFile metadataMappingFile = getMappingPropertiesFile();
    final SimpleRepositoryFileData metadataMappingFileData =
        repository.getDataForRead(metadataMappingFile.getId(), SimpleRepositoryFileData.class);
    final Properties mappingProperties = new Properties();
    mappingProperties.load(metadataMappingFileData.getStream());
    return mappingProperties;
  }

  protected void writeMappingPropertiesFiles(final Properties mappingProperties) throws IOException, PentahoMetadataException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    mappingProperties.store(out, null);
    final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

    final RepositoryFile mappingsFile = getMappingPropertiesFile();
    final IRepositoryFileData mappingFileData = new SimpleRepositoryFileData(in, characterEncoding, mimeType);
    repository.updateFile(mappingsFile, mappingFileData, "version message");
  }

  protected RepositoryFile getMappingPropertiesFile() throws PentahoMetadataException {
    RepositoryFile mappingFile = repositoryUtils.getFile(jcrMetadataInfo.getMetadataMappingFilePath(), null, false, false, null);
    if (null == mappingFile) {
      final String message =
          messages.getString("PentahoMetadataDomainRepositoryBackend.USER_0002_CREATE_METADATA_FILE_MESSAGE");
      mappingFile = repositoryUtils.getFile(jcrMetadataInfo.getMetadataMappingFilePath(),
          new SimpleRepositoryFileData(new ByteArrayInputStream(new byte[0]), "UTF-8", "text/plain"),
          true, true, message);
    }
    return mappingFile;
  }

  protected void setXmiParser(final XmiParser xmiParser) {
    this.xmiParser = xmiParser;
  }

  protected XmiParser getXmiParser() {
    return (null != xmiParser ? xmiParser : new XmiParser());
  }

  protected Map<String, RepositoryFile> loadMappingFile() throws IOException, PentahoMetadataException {
    final Map<String, RepositoryFile> domainMappings = new TreeMap<String, RepositoryFile>();
    final Properties mappingProperties = getMetadataMappingFile();
    for (final String domainId : mappingProperties.stringPropertyNames()) {
      final String xmiFileLocation = mappingProperties.getProperty(domainId);
      final RepositoryFile xmiRepositoryFile = repository.getFile(xmiFileLocation);
      domainMappings.put(domainId, xmiRepositoryFile);
    }
    return domainMappings;
  }

  protected void dumpMappings() {
    if (logger.isTraceEnabled()) {
      try {
        final Map<String, RepositoryFile> mappings = loadMappingFile();
        final StringBuffer s = new StringBuffer();
        s.append("\n========== PentahoMetadataDomainRepositoryBackend - repository =============\n");
        for (final String domainId : mappings.keySet()) {
          s.append("\t").append(domainId).append("\t\t");
          final RepositoryFile file = mappings.get(domainId);
          s.append(file == null ? file : file.getPath()).append("\n");
        }
        s.append("============================================================================\n");
        logger.trace(s.toString());
      } catch (IOException e) {
        logger.trace("EXCEPTION LOADING MAPPING FILE - ", e);
      } catch (PentahoMetadataException e) {
        logger.trace("EXCEPTION LOADING MAPPING FILE - ", e);
      }
    }
  }
}
