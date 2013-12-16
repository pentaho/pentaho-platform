/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.platform.web.servlet;

import mondrian.spi.DataSourceResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
  private static final Log logger =
    LogFactory.getLog( PentahoDataSourceResolver.class );

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
