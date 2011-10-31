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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jul 07, 2008 
 * @author rmansoor
 */
package org.pentaho.platform.api.repository.datasource;

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.IPentahoInitializer;

public interface IDatasourceMgmtService extends IPentahoInitializer {
  
  public static final String IDATASOURCEMGMTSERVICE = "IDatasourceMgmtService"; //$NON-NLS-1$
  
  public void createDatasource(IDatabaseConnection databaseConnection) throws DuplicateDatasourceException, DatasourceMgmtServiceException;

  public void deleteDatasourceByName(String name) throws NonExistingDatasourceException, DatasourceMgmtServiceException;

  //public void deleteDatasourceById(Serializable id) throws NonExistingDatasourceException, DatasourceMgmtServiceException;

  public IDatabaseConnection getDatasourceByName(String name) throws DatasourceMgmtServiceException;

  //public IDatabaseConnection getDatasourceById(Serializable id) throws DatasourceMgmtServiceException;

  public List<IDatabaseConnection> getDatasources() throws DatasourceMgmtServiceException;
  
  //public List<Serializable> getDatasourceIds() throws DatasourceMgmtServiceException;

  public void updateDatasourceByName(String name, IDatabaseConnection databaseConnection) throws NonExistingDatasourceException, DatasourceMgmtServiceException;

  //public void updateDatasourceById(Serializable id, IDatabaseConnection databaseConnection) throws NonExistingDatasourceException, DatasourceMgmtServiceException;
}
