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

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class MetadataImportHandler implements ImportHandler {
  private static final Log log = LogFactory.getLog(MetadataImportHandler.class);
  private static final Messages messages = Messages.getInstance();

  private IUnifiedRepository repository;
  private org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryImporter metadataImporter;
  private String url, username, password;

  public MetadataImportHandler(final IUnifiedRepository repository) {
    if (null == repository) {
      throw new IllegalArgumentException();
    }
    this.repository = repository;
  }
  
  public MetadataImportHandler(String username, String password, String url) {
	if (null == username || null == password || null == url) {
		throw new IllegalArgumentException();
	} 
	this.username = username;
	this.password = password;
	this.url = url;
  }

  protected org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryImporter getMetadataImporter() {
    if (metadataImporter == null) {
      metadataImporter = new org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository(repository);
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
    log.debug("MetadataImportHandler.doImport()");
    if (null == importFileSet) {
      throw new IllegalArgumentException();
    }

    // Create a list of processed metadata files to save
    final Set<String> processedDomains = new HashSet<String>();
    final Map<ImportSource.IRepositoryFileBundle, PentahoMetadataFileInfo> potentialMissedLocaleFiles
        = new HashMap<ImportSource.IRepositoryFileBundle, PentahoMetadataFileInfo>();

    // Find the metadata files
    for (Iterator iterator = importFileSet.iterator(); iterator.hasNext(); ) {
      final ImportSource.IRepositoryFileBundle file = (ImportSource.IRepositoryFileBundle) iterator.next();
      final String name = file.getFile().getName();
      final String fullFilename = RepositoryFilenameUtils.concat(file.getPath(), name);
      final PentahoMetadataFileInfo info = new PentahoMetadataFileInfo(fullFilename);
      if (info.getFileType() == PentahoMetadataFileInfo.FileType.XMI) {
        log.trace("\t[" + fullFilename + "] = " + info.toString());
        log.trace("\tprocessing as a Pentaho Metadata Domain File");
        processMetadataFile(file, fullFilename, overwrite);
        processedDomains.add(info.getDomainId());
        iterator.remove();
      } else if (info.getFileType() == PentahoMetadataFileInfo.FileType.PROPERTIES) {
        if (processedDomains.contains(info.getDomainId())) {
          log.trace("\t[" + fullFilename + "] = " + info.toString());
          log.trace("\tprocessing as a Pentaho Metadata Sidecar Locale file - domain already imported");
          processLocaleFile(file, info, overwrite);
          iterator.remove();
        } else {
          log.trace("\t[" + fullFilename + "] = " + info.toString());
          log.trace("\tprocessing as a Pentaho Metadata Sidecar Locale file - domain not found yet");
          potentialMissedLocaleFiles.put(file, info);
        }
      }
    }

    // If there are potentially missed locale files, we need to make a 2nd pass
    if (!potentialMissedLocaleFiles.isEmpty()) {
      for (Iterator iterator = importFileSet.iterator(); iterator.hasNext(); ) {
        final ImportSource.IRepositoryFileBundle file = (ImportSource.IRepositoryFileBundle) iterator.next();
        final PentahoMetadataFileInfo info = potentialMissedLocaleFiles.get(file);
        if (info != null) {
          if (processedDomains.contains(info.getDomainId())) {
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
    final PentahoMetadataFileInfo info = new PentahoMetadataFileInfo(filename);
    try {
      log.debug("Importing [" + info.getPath() + "] as metadata - [domain=" + info.getDomainId() + "]");
      if (!StringUtils.isEmpty(info.getDomainId())) {
        log.debug("importing [" + filename + "] as metadata [domain=" + info.getDomainId() + " : overwrite=" + overwrite + "]");
        if(this.repository != null) {
        	getMetadataImporter().storeDomain(bundle.getInputStream(), info.getDomainId(), overwrite);
        } else {
        	storeDomain(bundle.getInputStream(), info.getDomainId());
        }
      }
      return info.getDomainId();
    } catch (Exception e) {
      final String errorMessage = messages.getErrorString("MetadataImportHandler.ERROR_0001_IMPORTING_METADATA",
          info.getPath(), info.getDomainId(), e.getLocalizedMessage());
      log.error(errorMessage, e);
    }
    return null;
  }

  private void processLocaleFile(final ImportSource.IRepositoryFileBundle file, final PentahoMetadataFileInfo info, final boolean overwrite) {
    try {
      log.debug("Importing [" + info.getPath() + "] as properties - [domain=" + info.getDomainId() + " : locale=" + info.getLocale() + "]");
      if(this.repository != null) {
        getMetadataImporter().addLocalizationFile(info.getDomainId(), info.getLocale(), file.getInputStream(), overwrite);
      } else {
    	addLocalizationFile(info.getDomainId(), info.getLocale(), file.getInputStream());
      }
    } catch (Exception e) {
      final String errorMessage = messages.getErrorString("MetadataImportHandler.ERROR_0002_IMPORTING_LOCALE_FILE",
          info.getPath(), info.getDomainId(), info.getLocale(), e.getLocalizedMessage());
      log.error(errorMessage, e);
    }
  }

  /**
   * Returns the name of this Import Handler
   */
  @Override
  public String getName() {
    return "PentahoMetadataImportHandler";
  }
  
  private void storeDomain(InputStream metadataFile, String domainId) throws Exception {
	  
	  ClientConfig clientConfig = new DefaultClientConfig(); 
      Client client = Client.create(clientConfig); 
      client.addFilter(new HTTPBasicAuthFilter(this.username, this.password)); 

      String storeDomainUrl = this.url + "/plugin/data-access/api/metadata/storeDomain?domainId=" + domainId;
      WebResource resource = client.resource(storeDomainUrl); 
      ClientResponse response = resource.put(ClientResponse.class, metadataFile);

      int status = response.getStatus();
      if (status != HttpStatus.SC_OK) {
    	  throw new Exception(messages.getErrorString("MetadataImportHandler.ERROR_0001_IMPORTING_METADATA"));
      }
  }
  
  private void addLocalizationFile(String domainId, String locale, InputStream propertiesFile) throws Exception {
	  
	  ClientConfig clientConfig = new DefaultClientConfig(); 
      Client client = Client.create(clientConfig); 
      client.addFilter(new HTTPBasicAuthFilter(this.username, this.password)); 

      String storeDomainUrl = this.url + "/plugin/data-access/api/metadata/addLocalizationFile?domainId=" + domainId + "&locale=" + locale;
      WebResource resource = client.resource(storeDomainUrl); 
      ClientResponse response = resource.put(ClientResponse.class, propertiesFile);

      int status = response.getStatus();
      if (status != HttpStatus.SC_OK) {
    	  throw new Exception(messages.getErrorString("MetadataImportHandler.ERROR_0002_IMPORTING_LOCALE_FILE"));
      }
  }
}
