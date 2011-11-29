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
import org.pentaho.platform.api.datasource.GenericDatasourceServiceException;
import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.api.datasource.IGenericDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class JDBCDatasourceService implements IGenericDatasourceService{

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
  public void add(IGenericDatasource datasource) throws GenericDatasourceServiceException, PentahoAccessControlException {
    helper.checkAdministratorAccess();
    try {
      if(datasource instanceof JDBCDatasource) {
        JDBCDatasource jdbcDatasource = (JDBCDatasource) datasource;
        datasourceMgmtService.createDatasource(jdbcDatasource.getDatasource());        
      } else {
        throw new GenericDatasourceServiceException("Object is not of type JDBCDatasource");
      }

    } catch (DuplicateDatasourceException e) {
      throw new GenericDatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new GenericDatasourceServiceException(e);
    }
  }

  @Override
  public JDBCDatasource get(String id) throws GenericDatasourceServiceException, PentahoAccessControlException {
    helper.checkAdministratorAccess();
    try {
      IDatabaseConnection databaseConnection = datasourceMgmtService.getDatasourceByName(id);
      return new JDBCDatasource(databaseConnection, databaseConnection.getName(), databaseConnection.getName(), TYPE);
    } catch (DatasourceMgmtServiceException e) {
      return null;
    }
  }

  @Override
  public void remove(String id) throws GenericDatasourceServiceException, PentahoAccessControlException {
    helper.checkAdministratorAccess();
    try {
      datasourceMgmtService.deleteDatasourceByName(id);
    } catch (NonExistingDatasourceException e) {
      throw new GenericDatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new GenericDatasourceServiceException(e);
    }
  }

  @Override
  public void edit(IGenericDatasource datasource) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    try {
      if(datasource instanceof JDBCDatasource) {
        JDBCDatasource jdbcDatasource = (JDBCDatasource) datasource;
        datasourceMgmtService.updateDatasourceByName(jdbcDatasource.getId(), jdbcDatasource.getDatasource());
      } else {
        throw new GenericDatasourceServiceException("Object is not of type JDBCDatasource");
      }
    } catch (NonExistingDatasourceException e) {
      throw new GenericDatasourceServiceException(e);
    } catch (DatasourceMgmtServiceException e) {
      throw new GenericDatasourceServiceException(e);
    }
  }

  @Override
  public List<IGenericDatasource> getAll() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IGenericDatasource> jdbcDatasourceList = new ArrayList<IGenericDatasource>();
    try {
      for(IDatabaseConnection databaseConnection:datasourceMgmtService.getDatasources()) {
        jdbcDatasourceList.add(new JDBCDatasource(databaseConnection, databaseConnection.getName(), databaseConnection.getName(), TYPE));
      }
      return jdbcDatasourceList;
    } catch (DatasourceMgmtServiceException e) {
      return null;
    }
  }

  @Override
  public List<IGenericDatasourceInfo> getIds() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IGenericDatasourceInfo> datasourceInfoList = new ArrayList<IGenericDatasourceInfo>();
    try {
      for(IDatabaseConnection databaseConnection:datasourceMgmtService.getDatasources()) {
        datasourceInfoList.add(new GenericDatasourceInfo(databaseConnection.getName(), databaseConnection.getName(), TYPE));
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

}
