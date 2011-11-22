/*
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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.repository2.unified.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.messages.Messages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implementation of the Metadata Domain Repository to work with the JCR repository. This implementation
 * will store all metadata related files into a directory structure located at: /etc/metadata
 * In the folder, there will be a "master file" (metadata_mapping.properties) which will contain mappings
 * in the form (DomainID)=(directory name). The (directory name) will be a sub-folder in the JCR (under the metadata
 * folder) which will contain the metadata file for that Domain ID (in a file names metadata.xml) and all the
 * related I18N properties files (metadata_locale.properties)
 * </p>
 * This implementation should be the one-and-only interface into these files - they should not be directly accessed
 * in the repository by other methods.
 */
public class PentahoMetadataDomainRepository implements IMetadataDomainRepository {
  private static final Log logger = LogFactory.getLog(PentahoMetadataDomainRepository.class);

  private static final Messages messages = Messages.getInstance();

  private Map<String, Domain> domains = Collections.synchronizedMap(new HashMap<String, Domain>());

  private PentahoMetadataDomainRepositoryBackend jcrBackend;

  /**
   * Creates a PentahoMetadataDomainRepository using the default Backing implementation and the specified repository
   */
  public PentahoMetadataDomainRepository(final IUnifiedRepository repository) {
    logger.warn("PentahoMetadataDomainRepository("+repository.toString()+")");
    if (null == repository) {
      throw new NullPointerException(messages.getErrorString("MetadataDomainRepository.ERROR_0020_REPOSITORY_IS_NULL"));
    }
    this.jcrBackend = new PentahoMetadataDomainRepositoryBackend(repository);
  }

  /**
   * Creates a PentahoMetadataDomainRepository using the default Backing implementation and the specified repository
   */
  public PentahoMetadataDomainRepository(final PentahoMetadataDomainRepositoryBackend backend) {
    logger.warn("PentahoMetadataDomainRepository("+backend.toString()+")");
    if (null == backend) {
      throw new NullPointerException(messages.getErrorString("MetadataDomainRepository.ERROR_0021_BACKEND_IS_NULL"));
    }
    this.jcrBackend = backend;
  }

  /**
   * return a list of all the domain ids in the repository.  triggers a call to reloadDomains if necessary.
   *
   * @return the domain Ids.
   */
  @Override
  public Set<String> getDomainIds() {
    logger.debug("getDomainIds()...");

    reloadDomains(false);
    final Set<String> domainIds = new TreeSet<String>(domains.keySet());

    logger.debug("getDomainIds() returning a set of size " + domainIds.size());
    dumpDomains();
    return domainIds;
  }

  /**
   * retrieve a domain from the repo.  This does lazy loading of the repo, so it calls reloadDomains()
   * if not already loaded.
   *
   * @param domainId domain to get from the repository
   * @return domain object
   */
  @Override
  public Domain getDomain(String domainId) {
    final String methodCall = "getDomain(\"" + domainId + "\")";
    logger.debug(methodCall);

    // Get the domain from the loaded set
    final Domain domain = getDomainWithRetry(domainId, false);

    // If it was not found, log a message
    if (null == domain) {
      logger.warn(messages.getString("MetadataDomainRepository.WARN_0001_DOMAIN_NOT_FOUND", domainId));
    }

    // Return what was found
    logger.debug(methodCall + " returning " + domain);
    dumpDomains();
    return domain;
  }

  /**
   * reload domains from disk
   */
  @Override
  public void reloadDomains() {
    logger.debug("reloadDomains()");
    reloadDomains(true);
    logger.debug("reloadDomains() complete");
    dumpDomains();
  }

  /**
   * flush the domains from memory
   */
  @Override
  public void flushDomains() {
    logger.debug("flushDomains()");
    synchronized (domains) {
      domains.clear();
    }
    logger.debug("flushDomains() complete");
    dumpDomains();
  }

  /**
   * Store a domain to the repository.  The domain should persist between JVM restarts.
   *
   * @param domain    domain object to store
   * @param overwrite if true, overwrite existing domain
   * @throws DomainIdNullException        if domain id is null
   * @throws DomainAlreadyExistsException if domain exists and overwrite = false
   * @throws DomainStorageException       if there is a problem storing the domain
   */
  @Override
  public void storeDomain(Domain domain, boolean overwrite) throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
    validateDomainParameter(domain);
    final String newDomainId = domain.getId();
    final String methodCall = "storeDomain('" + newDomainId + "', " + overwrite + ")";

    synchronized (domains) {
      // Quick fail if overwrite == false and the domain already exists
      if (!overwrite && getDomainWithRetry(newDomainId, false) != null) {
        final String errorString = messages.getErrorString("MetadataDomainRepository.ERROR_0002_DOMAIN_OBJECT_EXISTS",
            domain.getId());
        logger.error(errorString);
        throw new DomainAlreadyExistsException(errorString);
      }

      // Try to add to the repository before adding to the domain set
      logger.debug(methodCall + " - adding domain to the repository");
      saveDomain(domain);
      domains.put(newDomainId, domain);
    }
    logger.debug(methodCall + " - added domain to domain set");
    dumpDomains();
  }

  /**
   * remove a domain from disk and memory.
   *
   * @param domainId
   */
  @Override
  public void removeDomain(String domainId) {
    final String methodCall = "removeDomain(" + domainId + ")";
    logger.debug(methodCall);

    synchronized (domains) {
      // See if we have it
      if (null == getDomainWithRetry(domainId, false)) {
        logger.debug(methodCall + " - could not find domainId - nothing to do");
        return;
      }

      // Remove it from the cache
      logger.debug(methodCall + " - removing domain id from domain set");
      domains.remove(domainId);

      // Remove it from the backend
      try {
        jcrBackend.removeDomainFromRepository(domainId);
      } catch (Exception e) {
        final String errorMessage = messages.getErrorString("MetadataDomainRepository.ERROR_0022_ERROR_DELETING_DOMAIN",
            domainId, e.getLocalizedMessage());
        logger.error(errorMessage);
      }
    }
    logger.debug(methodCall + " - complete");
    dumpDomains();
  }

  /**
   * remove a model from a domain which is stored either on a disk or memory.
   *
   * @param domainId
   * @param modelId
   */
  @Override
  public void removeModel(String domainId, String modelId) throws DomainIdNullException, DomainStorageException {
    final String methodCall = "removeModel('" + domainId + "', '" + modelId + "')";
    logger.debug(methodCall);

    // Validate the parameters
    final Domain domain = getDomainWithRetry(domainId, false);
    if (null == domain) {
      final String errorMessage = messages.getErrorString("MetadataDomainRepository" +
          ".ERROR_0024_ERROR_DOMAIN_NOT_FOUND", domainId);
      logger.error(errorMessage);
      throw new DomainStorageException(errorMessage, null);
    }
    if (null == modelId) {
      final String errorMessage = messages.getErrorString("MetadataDomainRepository.WARN_0002_MODEL_ID_NULL");
      logger.error(errorMessage);
      throw new NullPointerException(errorMessage);
    }

    // Remove the model from the domain
    logger.debug(methodCall + " - removing model");
    if (removeModelFromDomain(modelId, domain) == null) {
      // The model is empty - remove the domain
      logger.debug(methodCall + " - removing domain");
      removeDomain(domainId);
    } else {
      // We need to update the domain now that the model has been removed
      logger.debug(methodCall + " - updating domain");
      saveDomain(domain);
    }
    logger.debug(methodCall + " - done");
  }

  @Override
  public String generateRowLevelSecurityConstraint(LogicalModel model) {
    return null;
  }

  /**
   * The aclHolder cannot be null unless the access type requested is ACCESS_TYPE_SCHEMA_ADMIN.
   */
  @Override
  public boolean hasAccess(int accessType, IConcept aclHolder) {
    // Subclasses can override this for ACL and Session/Credential checking
    return true;
  }

  /**
   * Reloads the domain information from the repository. If {@code overwrite} is {@code true},
   * then this method will reload all metadata domains. Otherwise it will just all any new domains that are found
   *
   * @param fullReload indicates if the domain information should be completely reloaded or just updated
   */
  protected void reloadDomains(boolean fullReload) {
    logger.debug("reloadDomains(" + fullReload + ")");
    final Map<String, RepositoryFile> repoDomainMapping;
    try {
      repoDomainMapping = jcrBackend.loadDomainMappingFromRepository();
      logger.debug("Found a set of " + repoDomainMapping.size() + " domains in the repository");

      synchronized (domains) {
        if (fullReload) {
          logger.debug("Clearing out the list of domains to force a full reload");
          domains.clear();
        }

        for (final String domainId : repoDomainMapping.keySet()) {
          logger.debug("reload checking for current existance of domain [" + domainId + "]");
          if (!domains.containsKey(domainId)) {
            logger.debug("Loading domain from repository");
            final Domain domain = jcrBackend.loadDomain(domainId, repoDomainMapping.get(domainId));
            domains.put(domainId, domain);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      final String errorMessage = messages.getErrorString("MetadataDomainRepository.ERROR_0023_ERROR_RELOADING_DOMAINS",
          e.getLocalizedMessage());
      logger.error(errorMessage);
      domains.clear();
    }
  }

  /**
   * Returns the domain we have in the cache if have it - and reloads the cache from the backend if we don't just to
   * make sure we have the latest information.
   *
   * @param domainId   the requested domainID (shouldn't be {@code null}
   * @param fullReload {@code true} indicates we should perform a clean and reload, {@code false} indicates we should
   *                   just reload anything that we don't already have
   * @return the {@link Domain} with the specified domainId, or {@code null} if the domainId does not exist
   * @throws NullPointerException indicates the {@code domainId} supplied is null
   */
  protected Domain getDomainWithRetry(final String domainId, final boolean fullReload) {
    if (null == domainId) {
      throw new NullPointerException(messages.getErrorString("MetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL"));
    }

    final String methodCall = "getDomainWithRetry(" + domainId + ", " + fullReload + ")";
    logger.debug(methodCall);
    assert domainId != null;

    // See if we have it ... and reload the cache from the backend if we don't
    Domain domain = domains.get(domainId);
    if (null == domain) {
      reloadDomains(fullReload);
      domain = domains.get(domainId);
    }

    // If it was not found, log a warning
    if (null == domain) {
      logger.warn(messages.getString("MetadataDomainRepository.WARN_0001_DOMAIN_NOT_FOUND", domainId));
    }

    logger.debug(methodCall + " - returning domain: " + (null == domain ? "null" : domain.getId()));
    return domain;
  }

  /**
   * Saves the domain to the backend repository
   * @param domain the domain to save
   * @throws DomainStorageException indicates an error saving the domain
   */
  protected void saveDomain(final Domain domain) throws DomainStorageException {
    try {
      jcrBackend.addDomainToRepository(domain);
    } catch (Exception e) {
      final String errorMessage = messages.getErrorString("MetadataDomainRepository" +
          ".ERROR_0004_DOMAIN_STORAGE_EXCEPTION", e.getLocalizedMessage());
      logger.error(errorMessage);
      throw new DomainStorageException(errorMessage, e);
    }
  }

  /**
   * Internal method to remove a model from the specified domain.
   * </p>
   * NOTE: if the model was not found, the model will not be changed
   * @param modelId the model id to remove from the domain
   * @param domain the domain from which the model will be removed
   * @return the updated domain object (the same one passed as a parameter) after the update, or {@code null} if the
   * domain object does not contain any more models
   */
  protected Domain removeModelFromDomain(final String modelId, final Domain domain) {
    final String methodCall = "removeModelFromDomain('"+modelId+"', '"+domain.getId()+"'";
    logger.debug(methodCall);

    // Find the specified model
    boolean found = false;
    final Iterator<LogicalModel> iter = domain.getLogicalModels().iterator();
    while (iter.hasNext()) {
      LogicalModel model = iter.next();
      logger.debug(methodCall + " - checking model "+model.getId());
      if (modelId.equals(model.getId())) {
        logger.debug(methodCall + " - removing model");
        iter.remove();
        found = true;
        break;
      }
    }

    // Did we find it?
    if (!found) {
      logger.debug(methodCall + " - we didn't find the model");
    }

    // If there are no more models, then we should delete this domain
    final int modelCount = domain.getLogicalModels().size();
    logger.debug(methodCall + " - there are "+modelCount+" models in the domain");
    return (modelCount > 0 ? domain : null);
  }

  /**
   * Validates the Domain parameter. This method will return successful if it is valid or throw an exception
   * if it is not valid
   * @param domain the {@link Domain} object to validated
   * @throws NullPointerException indicates the supplied paratmer is {@code null}
   * @throws DomainIdNullException indicates the Domain object contains a {@code null} domainId
   */
  protected void validateDomainParameter(final Domain domain) throws NullPointerException, DomainIdNullException {
    if (null == domain) {
      throw new NullPointerException();
    }
    if (null == domain.getId()) {
      final String errorString = messages.getErrorString("MetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL");
      throw new DomainIdNullException(errorString);
    }
  }

  /**
   * Returns the number of domains currently loaded in the cache
   */
  protected int getLoadedDomainCount() {
    return domains.size();
  }

  protected void dumpDomains() {
    if (logger.isTraceEnabled()) {
      StringBuffer s = new StringBuffer();
      s.append("\n======== PentahoMetadataDomainRepository - Domain List ========\n");
      for (final String domainId : domains.keySet()) {
        s.append("\t").append(domainId).append("\t\t").append(domains.get(domainId)).append("\n");
      }
      s.append("===============================================================\n");
      logger.trace(s.toString());
    }
  }
}
