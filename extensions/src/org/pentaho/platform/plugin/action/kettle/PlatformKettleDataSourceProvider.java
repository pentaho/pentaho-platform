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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 * @author mbatchel
 */
 
package org.pentaho.platform.plugin.action.kettle;

import javax.sql.DataSource;

import org.pentaho.di.core.database.DataSourceNamingException;
import org.pentaho.di.core.database.DataSourceProviderFactory;
import org.pentaho.di.core.database.DataSourceProviderInterface;
import org.pentaho.platform.api.data.DatasourceServiceException;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PlatformKettleDataSourceProvider implements DataSourceProviderInterface {

  protected static final PlatformKettleDataSourceProvider instance = new PlatformKettleDataSourceProvider();

  private PlatformKettleDataSourceProvider() {
    // Private constructor
  }
  
  public DataSourceProviderInterface getInstance() {
    return instance;
  }
  
  protected static void hookupProvider() {
    DataSourceProviderFactory.setDataSourceProviderInterface(instance);
  }
  
  public DataSource getNamedDataSource(String dataSourceName) throws DataSourceNamingException {
    IDatasourceService datasourceService =  (IDatasourceService) PentahoSystem.get(IDatasourceService.class, null);  
    if (datasourceService != null) {
      try {
        return datasourceService.getDataSource(dataSourceName);
      } catch (DatasourceServiceException ex) {
        throw new DataSourceNamingException(ex);
      }
    }
    return null;
  }

}
