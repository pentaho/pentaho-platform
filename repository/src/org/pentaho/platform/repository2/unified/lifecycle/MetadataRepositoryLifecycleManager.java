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
 */
package org.pentaho.platform.repository2.unified.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryLifecycleManagerException;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.messages.Messages;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Callable;

/**
 * Class to handle the lifecycle management of Metadata. This class will create the metadata folder for each tenant
 * and create the system metadata mappings file (maps DomainIDs to sub-folders which contain all the files
 * related to the metadata).
 */
public class MetadataRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {
  /**
   * The logger
   */
  private static final Log logger = LogFactory.getLog(MetadataRepositoryLifecycleManager.class);

  /**
   * The name of the folder in which Pentaho Metadata should be stored (appended to the proper path for
   * each tenant) - the value is {@value}
   */
  private static final String METADATA_FOLDER_NAME = "metadata";

  /**
   * The name of the file use for mapping Pentaho Metadata {@code domain IDs} into the sub-folders that
   * hold all the information for the Pentaho Metadata.
   */
  private static final String METADATA_MAPPING_FILE_NAME = "metadata-mappings.properties";
  private static final Messages MSG = Messages.getInstance();

  /**
   * The repository in which Pentaho Metadata will be stored.
   */
  private IUnifiedRepository repository;
  private ISecurityHelper securityHelper;

  /**
   * Constructs this Metadata Lifecycle Manager. It will require access to the repository since it needs
   * to ensure that the Pentaho Metadata system is setup and configured correctly.
   *
   * @param repository the repository for this system
   */
  public MetadataRepositoryLifecycleManager(IUnifiedRepository repository) {
    super();
    assert null != repository;
    this.repository = repository;
    this.securityHelper = SecurityHelper.getInstance();
  }

  /**
   * Constructs this Metadata Lifecycle Manager. It will require access to the repository since it needs
   * to ensure that the Pentaho Metadata system is setup and configured correctly.
   *
   * @param repository the repository for this system
   * @param securityHelper the instance of a {@link ISecurityHelper} to use
   */
  public MetadataRepositoryLifecycleManager(IUnifiedRepository repository, ISecurityHelper securityHelper) {
    super();
    assert null != repository;
    this.repository = repository;
    this.securityHelper = securityHelper;
  }

  /**
   * To be called before any (non-admin) users interact with the backing repository.</p>
   * NO ACTION NEEDED
   */
  @Override
  public void startup() {
  }

  /**
   * To be called on repository shutdown.</p>
   * NO ACTION NEEDED
   */
  @Override
  public void shutdown() {
  }

  /**
   * To be called before any users belonging to a particular tenant interact with the backing repository.
   * </p>
   * This method will ensure that the metadata folders / files are setup for the specified repository. Since these
   * locations are tenant specific, each new tenant will need to be checked.
   *
   * @param tenantId new tenant id
   */
  @Override
  public void newTenant(final String tenantId) {
    logger.debug("newTenant() - Checking the status of Pentaho Metadata for tenantId [" + tenantId + "]");

    // Get the metadata locations
    final String metadataFolderPath = getMetadataFolderPath();
    final String metadataMappingFilePath = getMetadataMappingFilePath();
    logger.debug("Using metadataFolderPath=[" + metadataFolderPath + "] and " +
        "metadataMappingFilePath=[" + metadataMappingFilePath + "]");

    // We need elevated privileges to create these files / dirs
    try {
      securityHelper.runAsSystem(new Callable<Void>() {
        public Void call() throws RepositoryLifecycleManagerException {
          // Quick win - see if the mappings file already exists
          if (null != repository.getFile(metadataMappingFilePath)) {
            logger.debug("The Pentaho Metadata mapping file already exists - nothing to do");
            return null;
          }

          // Try to retrieve the metadata folder
          RepositoryFile metadataFolder = repository.getFile(metadataFolderPath);
          if (null == metadataFolder) {
            logger.debug("The folder does not exist - we need to create it");
            final String parentPath = getMetadataParentPath();
            final RepositoryFile parentFolder = repository.getFile(parentPath);
            if (null == parentFolder) {
              final String errorMessage = MSG.getString(
                  "MetadataRepositoryLifecycleManager.ERROR_0001_CANT_CREATE_FOLDER", parentPath);
              logger.error(errorMessage);
              throw new RepositoryLifecycleManagerException(errorMessage);
            }

            logger.debug("Found the parent folder - now creating the metadata folder");
            final RepositoryFile metadataFolderFile = new RepositoryFile.Builder(getMetadataFolderName()).folder(true).build();
            metadataFolder = repository.createFolder(parentFolder.getId(), metadataFolderFile,
                MSG.getString("MetadataRepositoryLifecycleManager.USER_0001_CREATE_METADATA_FOLDER_MESSAGE"));
            if (null == metadataFolder) {
              final String errorMessage =
                  MSG.getString("MetadataRepositoryLifecycleManager.ERROR_0002_CANT_CREATE_METADATA_FOLDER",
                      metadataFolderPath);
              logger.error(errorMessage);
              throw new RepositoryLifecycleManagerException(errorMessage);
            }
          }

          // Create a blank mapping file
          logger.debug("Creating a blank mappings file");
          repository.createFile(
              metadataFolder.getId(),
              new RepositoryFile(getMetadataMappingFileName()),
              new SimpleRepositoryFileData(new ByteArrayInputStream(new byte[0]), "UTF-8", "text/plain"),
              MSG.getString("MetadataRepositoryLifecycleManager.USER_0002_CREATE_METADATA_FILE_MESSAGE")
          );
          return null;
        }
      });
    } catch (RepositoryLifecycleManagerException e) {
      throw e;
    } catch (Exception e) {
      final String errorMessage = MSG.getString(
          "MetadataRepositoryLifecycleManager.ERROR_0003_UNKNOWN_EXCEPTION", e.getMessage());
      logger.error(errorMessage);
      throw new RepositoryLifecycleManagerException(errorMessage, e);
    }

    logger.debug("completed metadata initialization");
  }

  /**
   * To be called before any users belonging to the current tenant interact with the backing repository.
   * </p>
   * This method will get the default {@code tenantId} and call the {@link #newTenant(String)} method.
   */
  @Override
  public void newTenant() {
    final String tenantId = (String) PentahoSessionHolder.getSession().getAttribute(IPentahoSession.TENANT_ID_KEY);
    logger.debug("newTenant() using the default tenantId of [" + tenantId + "]");
    newTenant(tenantId);
  }

  /**
   * To be called before user indicated by {@code username} interacts with the backing repository.</p>
   * NO ACTION NEEDED
   *
   * @param tenantId tenant to which the user belongs
   * @param username new username
   */
  @Override
  public void newUser(final String tenantId, final String username) {
  }

  /**
   * To be called before current user interacts with the backing repository.</p>
   * NO ACTION NEEDED
   */
  @Override
  public void newUser() {
  }

  /**
   * Returns the name of the metadata folder
   */
  protected String getMetadataFolderName() {
    return METADATA_FOLDER_NAME;
  }

  /**
   * Returns the name of the metadata folder
   */
  protected String getMetadataMappingFileName() {
    return METADATA_MAPPING_FILE_NAME;
  }

  /**
   * Returns the path location in which the Pentaho Metadata folder will be created
   */
  protected String getMetadataParentPath() {
    return ClientRepositoryPaths.getEtcFolderPath();
  }

  /**
   * Generates the repository location for the Pentaho Metadata to be stored
   */
  protected String getMetadataFolderPath() {
    return getMetadataParentPath() + RepositoryFile.SEPARATOR + METADATA_FOLDER_NAME;
  }

  /**
   * Returns the full-path to the Pentaho Metadata mappings file
   */
  protected String getMetadataMappingFilePath() {
    return getMetadataFolderPath() + RepositoryFile.SEPARATOR + getMetadataMappingFileName();
  }

}
