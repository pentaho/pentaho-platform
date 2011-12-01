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
 * Register, manage and provide basic listing functionality of platform datasources.  
 */

public interface IDatasourceServiceManager {

  /**
   * This method registera a datasource service to the platform 
   * @param service
   */
  public void registerService(IDatasourceService service);

  /**
   * Returns an instance of a service for a given datasource type
   * @param serviceType
   * @return datasource service
   */
  public IDatasourceService getService(String datasourceType);

  /**
   * Returns all the datasource types that are registered with the service manager
   * @return list of datasource type
   */
  public List<String> getTypes();
  
  /**
   * Returns a list of datasource id
   * @return list of datasource id
   */
  public List<IDatasourceInfo> getIds() throws PentahoAccessControlException;

}
