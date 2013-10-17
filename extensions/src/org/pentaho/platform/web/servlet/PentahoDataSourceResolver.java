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

package org.pentaho.platform.web.servlet;

import mondrian.spi.DataSourceResolver;
import org.apache.log4j.Logger;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.sql.DataSource;

/**
 * This class provides SPI functionality to Mondrian. It resolves relational data sources by their name. It uses the
 * {@link PentahoSessionHolder}.
 * 
 * @author Luc Boudreau
 */
public class PentahoDataSourceResolver implements DataSourceResolver {
  Logger logger = Logger.getLogger( PentahoDataSourceResolver.class );

  public DataSource lookup( String dataSourceName ) throws Exception {
    try {
      IDBDatasourceService datasourceSvc =
          PentahoSystem.getObjectFactory().get( IDBDatasourceService.class, PentahoSessionHolder.getSession() );
      javax.sql.DataSource datasource = datasourceSvc.getDataSource( datasourceSvc.getDSUnboundName( dataSourceName ) );
      return datasource;
    } catch ( ObjectFactoryException e ) {
      logger.error( Messages.getInstance().getErrorString( "PentahoXmlaServlet.ERROR_0002_UNABLE_TO_INSTANTIATE" ), e ); //$NON-NLS-1$
      throw e;
    } catch ( DBDatasourceServiceException e ) {
      logger.error( Messages.getInstance().getErrorString( "PentahoXmlaServlet.ERROR_0002_UNABLE_TO_INSTANTIATE" ), e ); //$NON-NLS-1$
      throw e;
    }
  }
}
