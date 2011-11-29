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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.datasource.GenericDatasourceServiceException;
import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.api.datasource.IGenericDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;

public class MondrianDatasourceService implements IGenericDatasourceService{
  public static final String METADATA_EXT = ".xmi";
  public static final String TYPE = "Analysis";
  IMondrianCatalogService mondrianCatalogService;
  IMetadataDomainRepository metadataDomainRepository;
  IAuthorizationPolicy policy;
  ActionBasedSecurityService helper;
  
  public MondrianDatasourceService(IMondrianCatalogService mondrianCatalogService, IMetadataDomainRepository metadataDomainRepository, IAuthorizationPolicy policy) {
    this.mondrianCatalogService = mondrianCatalogService;
    this.metadataDomainRepository = metadataDomainRepository;
    this.policy = policy;
    helper = new ActionBasedSecurityService(policy);
  }
  @Override
  public void add(IGenericDatasource datasource) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    if(datasource instanceof MondrianDatasource) {
      MondrianDatasource mondrianDatasource = (MondrianDatasource) datasource;
      mondrianCatalogService.addCatalog(mondrianDatasource.getDatasource(), false, PentahoSessionHolder.getSession());      
    } else {
      throw new GenericDatasourceServiceException("Object is not of type MondrianDatasource");
    }
  }

  @Override
  public MondrianDatasource get(String id) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    MondrianCatalog mondrianCatalog = mondrianCatalogService.getCatalog(id, PentahoSessionHolder.getSession());
    return new MondrianDatasource(mondrianCatalog, mondrianCatalog.getName(), mondrianCatalog.getName(), TYPE);
  }

  @Override
  public void remove(String id) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    mondrianCatalogService.removeCatalog(id, PentahoSessionHolder.getSession());
  }

  @Override
  public void edit(IGenericDatasource datasource) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    if(datasource instanceof MondrianDatasource) {
      MondrianDatasource mondrianDatasource = (MondrianDatasource) datasource;
      mondrianCatalogService.addCatalog(mondrianDatasource.getDatasource(), true, PentahoSessionHolder.getSession());
    } else {
      throw new GenericDatasourceServiceException("Object is not of type MondrianDatasource");
    }
  }

  @Override
  public List<IGenericDatasource> getAll() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IGenericDatasource> mondrianDatasourceList = new ArrayList<IGenericDatasource>();
    for(MondrianCatalog mondrianCatalog: mondrianCatalogService.listCatalogs(PentahoSessionHolder.getSession(), true)) {
      mondrianDatasourceList.add(new MondrianDatasource(mondrianCatalog,mondrianCatalog.getName(), mondrianCatalog.getName(), TYPE));
    }
    return mondrianDatasourceList;
  }

  @Override
  public List<IGenericDatasourceInfo> getIds() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IGenericDatasourceInfo> datasourceInfoList = new ArrayList<IGenericDatasourceInfo>();
    for(MondrianCatalog mondrianCatalog: mondrianCatalogService.listCatalogs(PentahoSessionHolder.getSession(), true)) {
      String domainId = mondrianCatalog.getName() + METADATA_EXT;
      Domain domain = metadataDomainRepository.getDomain(domainId);
      if(domain == null) {
        datasourceInfoList.add(new GenericDatasourceInfo(mondrianCatalog.getName(), mondrianCatalog.getName(), TYPE));
      }
    }
    return datasourceInfoList;
  }
  
  @Override
  public String getType() {
    return TYPE;
  }
}
