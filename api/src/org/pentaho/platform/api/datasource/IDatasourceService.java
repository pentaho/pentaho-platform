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
 * @created Nov 12, 2011 
 * @author Ramaiz Mansoor
 */
package org.pentaho.platform.api.datasource;

import java.util.List;

import org.pentaho.platform.api.engine.PentahoAccessControlException;

/**
 * This is a wrapper service which provides a basic a CRUD operations 
 * for a datasource service
 */
public interface IDatasourceService {
  
  /**
   * Returns the datsource type
   * @return datasource type
   */
  public String getType();
  
  /**
   * Adds a new datasource to the platform repository
   * @param datasource
   * @param overwrite if true then it will overwrite the datasource if it exists
   * @throws GenericDatasourceServiceException
   */
  public void add(IDatasource datasource, boolean overwrite) throws DatasourceServiceException, PentahoAccessControlException;
  
  /**
   * Returns a datasource object with a managed object wrapped inside
   * @param id
   * @return datasource object
   */
  public IDatasource get(String id) throws DatasourceServiceException, PentahoAccessControlException;
  
  /**
   * Removes a selected datasource from the platform repository
   * @param id
   * @throws DatasourceServiceException
   */
  public void remove(String id)throws DatasourceServiceException, PentahoAccessControlException;

  /**
   * Updates a selected datasource in a platform repository
   * @param datasource
   * @throws DatasourceServiceException
   */
  public void update(IDatasource datasource)throws DatasourceServiceException, PentahoAccessControlException;
    
  /**
   * Checks whether a datasource exists with a given id
   * @param id
   * @return true if the datasourc exists with the id provided
   * @throws PentahoAccessControlException
   */
  public boolean exists(String id) throws PentahoAccessControlException;
  
  /**
   * Returns all datasource ids for a particular type
   * @return list of datasource ids
   */
  public List<IDatasourceInfo> getIds() throws PentahoAccessControlException;
}
