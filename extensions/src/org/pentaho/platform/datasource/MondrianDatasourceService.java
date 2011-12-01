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
import org.pentaho.platform.api.datasource.DatasourceServiceException;
import org.pentaho.platform.api.datasource.IDatasource;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.api.datasource.IDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;

public class MondrianDatasourceService implements IDatasourceService{
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
  public void add(IDatasource datasource, boolean overwrite) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    if(datasource instanceof MondrianDatasource) {
      MondrianDatasource mondrianDatasource = (MondrianDatasource) datasource;
      mondrianCatalogService.addCatalog(mondrianDatasource.getDatasource(), overwrite, PentahoSessionHolder.getSession());      
    } else {
      throw new DatasourceServiceException("Object is not of type MondrianDatasource");
    }
  }

  @Override
  public MondrianDatasource get(String id) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    MondrianCatalog mondrianCatalog = mondrianCatalogService.getCatalog(id, PentahoSessionHolder.getSession());
    return new MondrianDatasource(mondrianCatalog, new DatasourceInfo(mondrianCatalog.getName(), mondrianCatalog.getName(), TYPE));
  }

  @Override
  public void remove(String id) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    mondrianCatalogService.removeCatalog(id, PentahoSessionHolder.getSession());
  }

  @Override
  public void update(IDatasource datasource) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    if(datasource instanceof MondrianDatasource) {
      MondrianDatasource mondrianDatasource = (MondrianDatasource) datasource;
      mondrianCatalogService.addCatalog(mondrianDatasource.getDatasource(), true, PentahoSessionHolder.getSession());
    } else {
      throw new DatasourceServiceException("Object is not of type MondrianDatasource");
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
        datasourceInfoList.add(new DatasourceInfo(mondrianCatalog.getName(), mondrianCatalog.getName(), TYPE));
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
}
