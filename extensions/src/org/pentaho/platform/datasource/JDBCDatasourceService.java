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

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.datasource.DatasourceServiceException;
import org.pentaho.platform.api.datasource.IDatasource;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.api.datasource.IDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;

public class JDBCDatasourceService implements IDatasourceService{

  public static final String TYPE = "JDBC";
  IDatasourceMgmtService datasourceMgmtService;
  IAuthorizationPolicy policy;
  ActionBasedSecurityService helper;

  public JDBCDatasourceService(IDatasourceMgmtService datasourceMgmtService, IAuthorizationPolicy policy) {
    this.datasourceMgmtService = datasourceMgmtService;
    this.policy = policy;
    helper = new ActionBasedSecurityService(policy);
  }
  @Override
  public void add(IDatasource datasource, boolean overwrite) throws DatasourceServiceException, PentahoAccessControlException {
    helper.checkAdministratorAccess();
    try {
      if(datasource instanceof JDBCDatasource) {
        JDBCDatasource jdbcDatasource = (JDBCDatasource) datasource;
        datasourceMgmtService.createDatasource(jdbcDatasource.getDatasource());        
      } else {
        throw new DatasourceServiceException("Object is not of type JDBCDatasource");
      }

    } catch (DuplicateDatasourceException e) {
      throw new DatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public JDBCDatasource get(String id) throws DatasourceServiceException, PentahoAccessControlException {
    helper.checkAdministratorAccess();
    try {
      IDatabaseConnection databaseConnection = datasourceMgmtService.getDatasourceByName(id);
      return new JDBCDatasource(databaseConnection, new DatasourceInfo(databaseConnection.getName(), databaseConnection.getName(), TYPE));
    } catch (DatasourceMgmtServiceException e) {
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public void remove(String id) throws DatasourceServiceException, PentahoAccessControlException {
    helper.checkAdministratorAccess();
    try {
      datasourceMgmtService.deleteDatasourceByName(id);
    } catch (NonExistingDatasourceException e) {
      throw new DatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public void update(IDatasource datasource) throws DatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    try {
      if(datasource instanceof JDBCDatasource) {
        JDBCDatasource jdbcDatasource = (JDBCDatasource) datasource;
        IDatasourceInfo datasourceInfo = jdbcDatasource.getDatasourceInfo();
        if(datasourceInfo != null) {
          datasourceMgmtService.updateDatasourceByName(datasourceInfo.getId(), jdbcDatasource.getDatasource());          
        } else throw new DatasourceServiceException("datasource id is null");
      } else {
        throw new DatasourceServiceException("Object is not of type JDBCDatasource");
      }
    } catch (NonExistingDatasourceException e) {
      throw new DatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public List<IDatasourceInfo> getIds() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IDatasourceInfo> datasourceInfoList = new ArrayList<IDatasourceInfo>();
    try {
      for(IDatabaseConnection databaseConnection:datasourceMgmtService.getDatasources()) {
        datasourceInfoList.add(new DatasourceInfo(databaseConnection.getName(), databaseConnection.getName(), TYPE));
      }
      return datasourceInfoList;
    } catch (DatasourceMgmtServiceException e) {
      return null;
    }
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
