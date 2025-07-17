/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.plugin.services.connections.hql;

import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.Type;
import org.pentaho.commons.connection.ILimitableConnection;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.engine.core.system.IPentahoLoggingConnection;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author mdamour
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class HQLConnection implements IPentahoLoggingConnection, ILimitableConnection {
  protected String lastQuery = null;

  protected ILogger logger = null;

  IPentahoResultSet resultSet = null;

  File hibernateConfigFile = null;
  private int timeOut = -1; // in seconds
  private int maxRows = -1; // in seconds

  Configuration hibernateConfig = null;

  public HQLConnection() {
    super();
  }

  public void setConfigFile( final File hbmCfg ) {
    hibernateConfigFile = hbmCfg;
    hibernateConfig = new Configuration();
    hibernateConfig.configure( hibernateConfigFile );
  }

  public void setClassNames( final String[] classNames ) {
    for ( int i = 0; ( classNames != null ) && ( i < classNames.length ); i++ ) {
      try {
        hibernateConfig.addClass( Class.forName( classNames[i] ) );
      } catch ( ClassNotFoundException e ) {
        logger.error( null, e );
      }
    }
  }

  public void setLogger( final ILogger logger ) {
    this.logger = logger;
  }

  public void setProperties( Properties props ) {
  }

  public boolean initialized() {
    // TODO create a good test
    return true;
  }

  /**
   * return datasource type HQL
   * 
   * @return datasource type
   */
  public String getDatasourceType() {
    return IPentahoConnection.HQL_DATASOURCE;
  }

  public IPentahoResultSet prepareAndExecuteQuery( final String query, final List parameters ) throws Exception {
    throw new UnsupportedOperationException();
  }

  public boolean preparedQueriesSupported() {
    return false;
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
  public IPentahoResultSet executeQuery( final String query ) {
    lastQuery = query;
    Session sess = null;
    IPentahoResultSet localResultSet = null;
    try {
      SessionFactory sf = hibernateConfig.buildSessionFactory();
      // open session
      sess = sf.openSession();
      Query q = sess.createQuery( query );
      if ( timeOut >= 0 ) {
        q.setTimeout( timeOut );
      }
      if ( maxRows >= 0 ) {
        q.setMaxResults( maxRows );
      }
      List<Object[]> list = q.getResultList();
      String[] returnAliases = list.isEmpty() ? new String[0] : Arrays.stream( list.get( 0 ) ).map( Object::toString ).toArray( String[]::new );
      Type[] returnTypes = list.isEmpty() ? new Type[0] : Arrays.stream( list.get( 0 ) ).map( Object::getClass ).toArray( Type[]::new );
      localResultSet = generateResultSet( list, returnAliases, returnTypes );
    } finally {
      try {
        if ( sess != null ) {
          sess.close();
        }
      } catch ( Exception e ) {
        // Doesn't seem like we would get any exception from sess.close()
        logger.error( Messages.getInstance().getErrorString( "HQLConnection.ERROR_0001_UNABLE_TO_CLOSE" ), e ); //$NON-NLS-1$
      }
    }

    return localResultSet;
  }

  public IPentahoResultSet generateResultSet( final List list, final String[] columnHeaders,
                                              final Type[] columnTypes ) {
    HQLResultSet localResultSet = new HQLResultSet( list, columnHeaders, columnTypes );
    return localResultSet;
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
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#setMaxRows(int)
   */
  public void setMaxRows( final int value ) {
    this.maxRows = value;
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

  public void setQueryTimeout( final int value ) {
    this.timeOut = value;
  }

}
