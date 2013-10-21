/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository.datasource;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.IPentahoInitializer;

import java.util.List;

/**
 * Service to manage JDBC datasources in the platform. is a {@link IDatabaseConnection}.
 * 
 * 
 */

public interface IDatasourceMgmtService extends IPentahoInitializer {

  public static final String IDATASOURCEMGMTSERVICE = "IDatasourceMgmtService"; //$NON-NLS-1$

  /**
   * Creates a JDBC datasource in a given repository and return an id
   * 
   * @param databaseConnection
   * @return id
   * @throws DuplicateDatasourceException
   * @throws DatasourceMgmtServiceException
   */
  public String createDatasource( IDatabaseConnection databaseConnection ) throws DuplicateDatasourceException,
    DatasourceMgmtServiceException;

  /**
   * Permanently deletes a JDBC datasource from a repository by name
   * 
   * @param name
   * @throws NonExistingDatasourceException
   * @throws DatasourceMgmtServiceException
   */
  public void deleteDatasourceByName( String name ) throws NonExistingDatasourceException,
    DatasourceMgmtServiceException;

  /**
   * Permanently deletes a JDBC datasource from a repository by id
   * 
   * @param id
   * @throws NonExistingDatasourceException
   * @throws DatasourceMgmtServiceException
   */
  public void deleteDatasourceById( String id ) throws NonExistingDatasourceException, DatasourceMgmtServiceException;

  /**
   * Retrieves a JDBC datasource form the repository by name
   * 
   * @param name
   * @return IDatabaseConnection
   * @throws DatasourceMgmtServiceException
   */
  public IDatabaseConnection getDatasourceByName( String name ) throws DatasourceMgmtServiceException;

  /**
   * Retrieves a JDBC datasource form the repository by id
   * 
   * @param id
   * @return IDatabaseConnection
   * @throws DatasourceMgmtServiceException
   */
  public IDatabaseConnection getDatasourceById( String id ) throws DatasourceMgmtServiceException;

  /**
   * Retrieves all JDBC datasources from the repository
   * 
   * @return databaseConnection List
   * @throws DatasourceMgmtServiceException
   */
  public List<IDatabaseConnection> getDatasources() throws DatasourceMgmtServiceException;

  /**
   * Retrieves all JDBC datasource ids from the repository
   * 
   * @return list of ids
   * @throws DatasourceMgmtServiceException
   */
  public List<String> getDatasourceIds() throws DatasourceMgmtServiceException;

  /**
   * Updates a given JDBC datasource by name
   * 
   * @param name
   * @param databaseConnection
   * @return id
   * @throws NonExistingDatasourceException
   * @throws DatasourceMgmtServiceException
   */
  public String updateDatasourceByName( String name, IDatabaseConnection databaseConnection )
    throws NonExistingDatasourceException, DatasourceMgmtServiceException;

  /**
   * Updates a given JDBC datasource by id
   * 
   * @param id
   * @param databaseConnection
   * @return id
   * @throws NonExistingDatasourceException
   * @throws DatasourceMgmtServiceException
   */
  public String updateDatasourceById( String id, IDatabaseConnection databaseConnection )
    throws NonExistingDatasourceException, DatasourceMgmtServiceException;
}
