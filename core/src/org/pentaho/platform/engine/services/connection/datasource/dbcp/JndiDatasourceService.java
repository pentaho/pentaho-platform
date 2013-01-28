/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *  
 * @created Jul 07, 2008 
 * @author rmansoor
 */
package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import javax.sql.DataSource;

import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.engine.services.messages.Messages;

public class JndiDatasourceService extends BaseDatasourceService {

  /**
   * Since JNDI is supported different ways in different app servers, it's
   * nearly impossible to have a ubiquitous way to look up a datasource. This
   * method is intended to hide all the lookups that may be required to find a
   * jndi name.
   * 
   * @param dsName
   *            The Datasource name
   * @return DataSource if there is one bound in JNDI
   * @throws DatasourceServiceException
   */
  public DataSource getDataSource(final String dsName) throws DBDatasourceServiceException {
    try {
      return getJndiDataSource(dsName);  
    }
    catch(DBDatasourceServiceException dse) {
     throw new DBDatasourceServiceException(Messages.getInstance().getErrorString("JndiDatasourceService.ERROR_0003_UNABLE_TO_GET_JNDI_DATASOURCE") ,dse);  //$NON-NLS-1$
    }
  }

}
