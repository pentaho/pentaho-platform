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
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
 *  
 */
package org.pentaho.platform.plugin.services.metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.util.messages.LocaleHelper;

/**
 * This is the platform implementation of the IMetadataDomainRepository.
 * 
 * TODO: Update this class to use CacheControl, getting created per session 
 * per Marc's input
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class MetadataDomainRepository implements IMetadataDomainRepository {
  
  private String metadataRootFolder = "/public/pentaho-solutions/metadata";
  protected final Log logger = LogFactory.getLog(MetadataDomainRepository.class);
  protected Map<String, Domain> domains = Collections.synchronizedMap(new HashMap<String, Domain>());
  
  public Set<String> getDomainIds() {
    synchronized(domains) {
      reloadDomains(false);
      Set<String> set = null;
      synchronized (domains) {
        set = new TreeSet<String>(domains.keySet());
      }
      return set;
    }
  }
  
  public String getMetadataRootFolder() {
    return metadataRootFolder;
  }

  public void setMetadataRootFolder(String metadataRootFolder) {
    this.metadataRootFolder = metadataRootFolder;
  }

  public void flushDomains() {
    domains.clear();
  }
  
  public void reloadDomains() {
    synchronized(domains) {
      reloadDomains(true);
    }
  }
  
  private RepositoryFile getXmiFile(String domainId) {
    if (!domainId.toLowerCase().endsWith(".xmi")) { //$NON-NLS-1$
      domainId = domainId + ".xmi"; //$NON-NLS-1$
    }
    String xmiFilePath = FilenameUtils.concat(getMetadataRootFolder(), domainId);
    IUnifiedRepository unifiedRepository = PentahoSystem.get(IUnifiedRepository.class, null);
    return unifiedRepository.getFile(xmiFilePath);
  }
  
  private List<RepositoryFile> getXmiFiles() {
    IUnifiedRepository unifiedRepository = PentahoSystem.get(IUnifiedRepository.class, null);
    RepositoryFile metadataFolder = unifiedRepository.getFile(getMetadataRootFolder());
    return unifiedRepository.getChildren(metadataFolder.getId(), "*.xmi"); //$NON-NLS-1$
  }
  
  public void removeDomain(String domainId) {
    RepositoryFile xmiFile = getXmiFile(domainId);
    domainId = xmiFile.getPath().substring(getMetadataRootFolder().length() + 1);
    synchronized(domains) {
      IUnifiedRepository unifiedRepository = PentahoSystem.get(IUnifiedRepository.class, null);
      unifiedRepository.deleteFile(xmiFile.getId(), null);
      domains.remove(domainId);
    }
  }
  
  public Domain getDomain(String id) {
    if (!id.toLowerCase().endsWith(".xmi")) {
      id = id + ".xmi";
    }
    Domain domain = domains.get(id);
    if (domain == null) {
      reloadDomains(false);
      domain = domains.get(id);
    }
    return domain;
  }
 
  private void reloadDomains(boolean overwrite) {
    for (RepositoryFile xmiFile : getXmiFiles()) {
      if (overwrite || !domains.containsKey(xmiFile.getPath().substring(getMetadataRootFolder().length() + 1))) {
        loadMetadata(xmiFile);
      }      
    }
  }

  private void loadMetadata(RepositoryFile xmiFile) {
    InputStream xmiInputStream = null;
    try {
      xmiInputStream = new RepositoryFileInputStream(xmiFile);
      Domain domain = new XmiParser().parseXmi(xmiInputStream);
      String domainId = xmiFile.getPath().substring(getMetadataRootFolder().length() + 1);
      domain.setId(domainId);
      domains.put(domainId, domain);
    } catch (Throwable t) {
      logger.error(Messages.getInstance().getString("MetadataPublisher.ERROR_0001_COULD_NOT_LOAD", xmiFile.getPath()), t); //$NON-NLS-1$
      throw new RuntimeException(Messages.getInstance().getString("MetadataPublisher.ERROR_0001_COULD_NOT_LOAD"), t); //$NON-NLS-1$
    } finally {
      if (xmiInputStream != null) {
        try {
          xmiInputStream.close();
        } catch (IOException ex) {
          logger.error(Messages.getInstance().getString("MetadataPublisher.ERROR_0001_COULD_NOT_LOAD", xmiFile.getPath()), ex); //$NON-NLS-1$
          throw new RuntimeException(Messages.getInstance().getString("MetadataPublisher.ERROR_0001_COULD_NOT_LOAD"), ex); //$NON-NLS-1$
        }
      }
    }
  }
  
  public void removeModel(String domainId, String modelId) throws DomainIdNullException, DomainStorageException {
    RepositoryFile xmiFile = getXmiFile(domainId);
    domainId = xmiFile.getPath().substring(getMetadataRootFolder().length() + 1);
    synchronized (domains) {
      Domain domain = domains.get(domainId);
      if (domain != null) {
        // remove the model
        Iterator<LogicalModel> iter = domain.getLogicalModels().iterator();
        while (iter.hasNext()) {
          LogicalModel model = iter.next();
          if (modelId.equals(model.getId())) {
            iter.remove();
            break;
          }
        }

        if (domain.getLogicalModels().size() == 0) {
          IUnifiedRepository unifiedRepository = PentahoSystem.get(IUnifiedRepository.class, null);
          unifiedRepository.deleteFile(xmiFile.getId(), null);
          domains.remove(domainId);
        } else {
          try {
            storeDomain(domain, true);
          } catch (DomainAlreadyExistsException e) {
            // This won't happen since overwrite is set to true.
          }
        }
      }
    }
  }
  
  public void storeDomain(Domain domain, boolean overwrite) throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
    synchronized(domains) {
      if (domain.getId() == null) {
        throw new DomainIdNullException(org.pentaho.metadata.messages.Messages.getErrorString("IMetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL")); //$NON-NLS-1$
      }
  
      if (!overwrite && domains.get(domain.getId()) != null) {
        throw new DomainAlreadyExistsException(org.pentaho.metadata.messages.Messages.getErrorString("IMetadataDomainRepository.ERROR_0002_DOMAIN_OBJECT_EXISTS", domain.getId())); //$NON-NLS-1$
      }
  
      IUnifiedRepository unifiedRepository = PentahoSystem.get(IUnifiedRepository.class, null);
      
      InputStream inputStream = null;
      XmiParser parser = new XmiParser();
      String xmi = parser.generateXmi(domain);
      try {
        RepositoryFile xmiFile = unifiedRepository.getFile(getMetadataRootFolder() + RepositoryFile.SEPARATOR + domain.getId());
        if (xmiFile == null) {
          RepositoryFile metadataRootFolder = unifiedRepository.getFile(getMetadataRootFolder());
          if(metadataRootFolder != null) {
            inputStream = new ByteArrayInputStream(xmi.getBytes(LocaleHelper.getSystemEncoding()));
            RepositoryFile file = new RepositoryFile.Builder(domain.getId()).title(RepositoryFile.ROOT_LOCALE, domain.getId()).versioned(true).build();
            file = unifiedRepository.createFile(metadataRootFolder.getId(), file, new SimpleRepositoryFileData(inputStream, LocaleHelper.getSystemEncoding(), "text/xml"), null);
          }
        }
        // adds the domain to the domains list
        domains.put(domain.getId(), domain);
        
      } catch (Exception e) {
        throw new DomainStorageException(Messages.getInstance().getErrorString("MetadataDomainRepository.ERROR_0006_FAILED_TO_STORE_LEGACY_DOMAIN", domain.getId()), e); //$NON-NLS-1$
      } finally {
        try {
          if (inputStream != null) {
            inputStream.close();
          }
        } catch (Exception ex) {
          // Do nothing
        }
      }
    }
  }
  
  public String generateRowLevelSecurityConstraint(LogicalModel model) {
    return null;
  }
  /**
   * The aclHolder cannot be null unless the access type requested is ACCESS_TYPE_SCHEMA_ADMIN.
   */
  public boolean hasAccess(int accessType, IConcept aclHolder) {
    // Subclasses can override this for ACL and Session/Credential checking
    return true;
  }
}
