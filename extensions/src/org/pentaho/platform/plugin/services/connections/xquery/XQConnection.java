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

package org.pentaho.platform.plugin.services.connections.xquery;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.engine.core.system.IPentahoLoggingConnection;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

/**
 * @author wseyler
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class XQConnection implements IPentahoLoggingConnection {
  protected Configuration config = null;

  protected StaticQueryContext sqc = null;

  protected String lastQuery = null;

  protected ILogger logger = null;

  IPentahoResultSet resultSet = null;

  int maxRows = -1;

  public XQConnection() {
    super();
    config = new Configuration();
    sqc = new StaticQueryContext( config );
  }

  public void setLogger( final ILogger logger ) {
    this.logger = logger;
  }

  public void setProperties( Properties props ) {
    connect( props );
  }

  public boolean initialized() {
    // TODO create a good test
    return true;
  }

  public IPentahoResultSet prepareAndExecuteQuery( final String query, final List parameters ) throws Exception {
    throw new UnsupportedOperationException();
  }

  public boolean preparedQueriesSupported() {
    return false;
  }

  /**
   * return datasource type MDX
   * 
   * @return datasource type
   */
  public String getDatasourceType() {
    return IPentahoConnection.XML_DATASOURCE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#close()
   */
  public void close() {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#getLastQuery()
   */
  public String getLastQuery() {
    return lastQuery;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#executeQuery(java.lang.String)
   */
  public IPentahoResultSet executeQuery( final String query ) throws XPathException {
    return executeQuery( query, null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#executeQuery(java.lang.String)
   */
  public IPentahoResultSet executeQuery( final String query, final String[] columnTypes ) throws XPathException {
    XQueryExpression exp = sqc.compileQuery( query );
    DynamicQueryContext dynamicContext = new DynamicQueryContext( config );
    try {
      resultSet = new XQResultSet( this, exp, dynamicContext, columnTypes );
    } catch ( XPathException e ) {
      if ( e.getException() instanceof FileNotFoundException ) {
        logger.error( Messages.getInstance().getString( "XQConnection.ERROR_0001_UNABLE_TO_READ", query ) ); //$NON-NLS-1$
      } else {
        logger.error( Messages.getInstance().getString( "XQConnection.ERROR_0002_XQUERY_EXCEPTION", query ), e ); //$NON-NLS-1$
      }
    } catch ( Throwable t ) {
      logger.error( Messages.getInstance().getErrorString( "XQConnection.ERROR_0002_XQUERY_EXCEPTION", query ), t ); //$NON-NLS-1$
    }
    lastQuery = query;
    return resultSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#isClosed()
   */
  public boolean isClosed() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#isReadOnly()
   */
  public boolean isReadOnly() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#clearWarnings()
   */
  public void clearWarnings() {
    // TODO Auto-generated method stub

  }

  public IPentahoResultSet getResultSet() {
    return resultSet;
  }

  public boolean connect( final Properties props ) {
    if ( props != null ) {
      String query = props.getProperty( IPentahoConnection.QUERY_KEY );
      if ( ( query != null ) && ( query.length() > 0 ) ) {
        try {
          executeQuery( query );
        } catch ( XPathException e ) {
          logger.error( e.getLocalizedMessage() );
          return false;
        }
      }
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#setMaxRows(int)
   */
  public void setMaxRows( final int maxRows ) {
    this.maxRows = maxRows;
  }

  public int getMaxRows() {
    return this.maxRows;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#setFetchSize(int)
   */
  public void setFetchSize( final int fetchSize ) {
    // TODO Auto-generated method stub
    // throw new UnsupportedOperationException();
  }

}
