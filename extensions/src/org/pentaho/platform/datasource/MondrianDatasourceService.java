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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved. 
 *
 *
 * @created Nov 12, 2011
 * @author Ramaiz Mansoor
 */
package org.pentaho.platform.datasource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.datasource.DatasourceServiceException;
import org.pentaho.platform.api.datasource.IDatasource;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.api.datasource.IDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;

public class MondrianDatasourceService implements IDatasourceService{
  public static final String METADATA_EXT = ".xmi";
  public static final String TYPE = "Analysis";
  IMondrianCatalogService mondrianCatalogService;
  IMetadataDomainRepository metadataDomainRepository;
  IAuthorizationPolicy policy;
  ActionBasedSecurityService helper;
  boolean editable;
  boolean removable;
  boolean importable;
  boolean exportable;
  String defaultNewUI = "$wnd.pho.showAnalysisImportDialog()";
  String defaultEditUI = "";
  String newUI;
  String editUI;
  
  public MondrianDatasourceService(IMondrianCatalogService mondrianCatalogService, IMetadataDomainRepository metadataDomainRepository, IAuthorizationPolicy policy) {
    this.mondrianCatalogService = mondrianCatalogService;
    this.metadataDomainRepository = metadataDomainRepository;
    this.policy = policy;
    helper = new ActionBasedSecurityService(policy);
    this.editable = false;
    this.removable = true;
    this.importable = true;
    this.exportable = true;
  }
  @Override
  public void add(String datasourceXml, boolean overwrite) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
      try {
        mondrianCatalogService.addCatalog(convertToMondrianCatalog(datasourceXml), overwrite, PentahoSessionHolder.getSession());
      } catch (MondrianCatalogServiceException e) {
        throw new DatasourceServiceException(e);
      } catch (JAXBException e) {
        throw new DatasourceServiceException(e);
      }      
  }

  @Override
  public String get(String id) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    MondrianCatalog mondrianCatalog = mondrianCatalogService.getCatalog(id, PentahoSessionHolder.getSession());
    try {
      return convertFromMondrianCatalog(mondrianCatalog);
    } catch (JAXBException e) {
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public void remove(String id) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    mondrianCatalogService.removeCatalog(id, PentahoSessionHolder.getSession());
  }

  @Override
  public void update(String datasourceXml) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
      try {
        mondrianCatalogService.addCatalog(convertToMondrianCatalog(datasourceXml), true, PentahoSessionHolder.getSession());
      } catch (MondrianCatalogServiceException e) {
        throw new DatasourceServiceException(e);
      } catch (JAXBException e) {
        throw new DatasourceServiceException(e);
      }
  }

  @Override
  public List<IDatasourceInfo> getIds() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IDatasourceInfo> datasourceInfoList = new ArrayList<IDatasourceInfo>();
    for(MondrianCatalog mondrianCatalog: mondrianCatalogService.listCatalogs(PentahoSessionHolder.getSession(), true)) {
      String domainId = mondrianCatalog.getName() + METADATA_EXT;
      Domain domain = metadataDomainRepository.getDomain(domainId);
      if(domain == null) {
        datasourceInfoList.add(new DatasourceInfo(mondrianCatalog.getName(), mondrianCatalog.getName(), TYPE, editable, removable, importable, exportable));
      }
    }
    return datasourceInfoList;
  }
  
  @Override
  public String getType() {
    return TYPE;
  }
  @Override
  public boolean exists(String id) throws PentahoAccessControlException {
    try {
      return get(id) != null;
    } catch (DatasourceServiceException e) {
      return false;
    }
  }

  @Override
  public void registerNewUI(String newUI) throws PentahoAccessControlException {
    this.newUI = newUI;    
  }
  @Override
  public void registerEditUI(String editUI) throws PentahoAccessControlException {
    this.editUI = editUI;
  }
  @Override
  public String getNewUI() throws PentahoAccessControlException {
    if(newUI == null) {
      return defaultNewUI;
    } else {
      return newUI;
    }
  }
  @Override
  public String getEditUI() throws PentahoAccessControlException {
    if(newUI == null) {
      return defaultNewUI;
    } else {
      return newUI;
    }
  }
  
  private MondrianCatalog convertToMondrianCatalog(String datasourceXml) throws JAXBException{
    JAXBContext context = JAXBContext.newInstance(MondrianCatalog.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    return (MondrianCatalog) unmarshaller.unmarshal(new StringReader(datasourceXml));
  }
  

  private String convertFromMondrianCatalog(MondrianCatalog mondrianCatalog) throws JAXBException {
    OutputStream outputStream = new OutputStream()
    {
        private StringBuilder string = new StringBuilder();
        @Override
        public void write(int b) throws IOException {
            this.string.append((char) b );
        }

        //Netbeans IDE automatically overrides this toString()
        public String toString(){
            return this.string.toString();
        }
    };

    JAXBContext context = JAXBContext.newInstance(MondrianCatalog.class);
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    m.marshal(mondrianCatalog, outputStream);
    return outputStream.toString();
  }
}
