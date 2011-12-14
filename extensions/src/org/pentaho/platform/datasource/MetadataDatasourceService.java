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

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.datasource.DatasourceServiceException;
import org.pentaho.platform.api.datasource.IDatasource;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.api.datasource.IDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;

public class MetadataDatasourceService implements IDatasourceService{
  IMetadataDomainRepository metadataDomainRepository;
  IAuthorizationPolicy policy;
  ActionBasedSecurityService helper;
  public static final String TYPE = "Metadata";
  boolean editable;
  boolean removable;
  boolean importable;
  boolean exportable;
  String defaultNewUI = "$wnd.pho.showMetadataImportDialog()";
  String defaultEditUI = "";
  String newUI;
  String editUI;

  public  MetadataDatasourceService(IMetadataDomainRepository metadataDomainRepository, IAuthorizationPolicy policy) {
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
        metadataDomainRepository.storeDomain(convertToDomain(datasourceXml), overwrite);
    } catch (DomainIdNullException e) {
      throw new DatasourceServiceException(e);
    } catch (DomainAlreadyExistsException e) {
      throw new DatasourceServiceException(e);
    } catch (DomainStorageException e) {
      throw new DatasourceServiceException(e);
    } catch (JAXBException e) {
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public String get(String id) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    Domain domain = metadataDomainRepository.getDomain(id);
    if(domain != null) {
      try {
        return convertFromDomain(domain);
      } catch (JAXBException e) {
        throw new DatasourceServiceException(e);
      }      
    } else {
      return null;
    }

  }

  @Override
  public void remove(String id) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    metadataDomainRepository.removeDomain(id);
  }

  @Override
  public void update(String datasourceXml) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    try {
        metadataDomainRepository.storeDomain(convertToDomain(datasourceXml), true);
    } catch (DomainIdNullException e) {
      throw new DatasourceServiceException(e);
    } catch (DomainAlreadyExistsException e) {
      throw new DatasourceServiceException(e);
    } catch (DomainStorageException e) {
      throw new DatasourceServiceException(e);
    } catch (JAXBException e) {
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public List<IDatasourceInfo> getIds() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IDatasourceInfo> datasourceInfoList = new ArrayList<IDatasourceInfo>();
    for(String id:metadataDomainRepository.getDomainIds()) {
      try {
        if(isMetadataDatasource(id)) {
          datasourceInfoList.add(new DatasourceInfo(id, id, TYPE, editable, removable, importable, exportable));
        }
      } catch (DatasourceServiceException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (JAXBException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return datasourceInfoList;
  }
  
  @Override
  public String getType() {
    return TYPE;
  }

  private boolean isMetadataDatasource(String id) throws DatasourceServiceException, JAXBException{
    String datasourceXml = null;
    try {
      datasourceXml = get(id);
    } catch (PentahoAccessControlException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Domain domain = convertToDomain(datasourceXml);
    List<LogicalModel> logicalModelList = domain.getLogicalModels();
    if(logicalModelList != null && logicalModelList.size() >= 1) {
      LogicalModel logicalModel = logicalModelList.get(0);
      Object property = logicalModel.getProperty("AGILE_BI_GENERATED_SCHEMA"); //$NON-NLS-1$
      if(property == null) {
        return true;    
      } 
    } else {
      return true;
    }
    return false;
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
  
  private Domain convertToDomain(String datasourceXml) throws JAXBException{
    JAXBContext context = JAXBContext.newInstance(Domain.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    return (Domain) unmarshaller.unmarshal(new StringReader(datasourceXml));
  }
  

  private String convertFromDomain(Domain domain) throws JAXBException {
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

    JAXBContext context = JAXBContext.newInstance(Domain.class);
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    m.marshal(domain, outputStream);
    return outputStream.toString();
  }
}
