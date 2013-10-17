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

package org.pentaho.platform.plugin.services.connections.mondrian;

import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.Query;
import mondrian.olap.Result;
import mondrian.olap.Role;
import mondrian.olap.Util;
import mondrian.rolap.RolapConnectionProperties;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.IPentahoLoggingConnection;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;

import javax.sql.DataSource;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author wseyler
 */
public class MDXConnection implements IPentahoLoggingConnection {
  /**
   * Defines the XML element in the component-definition that holds the mondrian-specific MDX Connection string.
   */
  public static final String CONNECTION_STRING_KEY = "mdx-connection-string"; //$NON-NLS-1$
  public static final String MDX_CONNECTION_MAPPER_KEY = "Mondrian-UserRoleMapper"; //$NON-NLS-1$

  protected Connection nativeConnection = null;

  private String lastQuery = null;

  private IPentahoResultSet resultSet = null;

  private ILogger logger = null;

  private boolean useExtendedColumnNames = false;

  /**
   * This role is set as a custom role impl for the Mondrian MDX connection Optional: if not set, the connection
   * processes roles as standard
   */
  private Role role = null;

  public MDXConnection() {
    super();
  }

  public void setLogger( final ILogger logger ) {
    this.logger = logger;
  }

  public void setProperties( final Properties props ) {

    // There is a PentahoConnectionFactory connection creation
    // method that will send these props in as NULL...
    if ( props == null ) {
      return;
    }

    // TODO: consolidate this in the init
    String connectStr = props.getProperty( IPentahoConnection.JNDI_NAME_KEY );
    if ( connectStr != null ) {
      init( connectStr );
    } else {
      final String connection = props.getProperty( IPentahoConnection.CONNECTION );
      final String provider = props.getProperty( IPentahoConnection.PROVIDER );
      final String userName = props.getProperty( IPentahoConnection.USERNAME_KEY );
      final String password = props.getProperty( IPentahoConnection.PASSWORD_KEY );
      if ( connection != null && provider != null ) {
        init( connection, provider, userName, password, props );
      } else {
        init( props );
      }
    }
  }

  /**
   * @param driver   - The name of the driver or the connection string
   * @param provider - the provider for MDX usally "mondrian"
   * @param userName - User to connect to the datasource with
   * @param password - Password for the user
   * @see MDXConnection(Properties props, ILogger logger)
   * @deprecated
   */
  @Deprecated
  public MDXConnection( final String driver, final String provider, final String userName, final String password ) {
    super();
    init( driver, provider, userName, password, new Properties() );
  }

  public MDXConnection( final String connectStr, final ILogger logger ) {
    super();
    this.logger = logger;
    init( connectStr );
  }

  protected void init( final String connectStr ) {
    Util.PropertyList properties = Util.parseConnectString( connectStr );
    init( properties );
  }

  @SuppressWarnings ( "unchecked" )
  protected void init( final Properties properties ) {
    Util.PropertyList pl = new Util.PropertyList();
    Enumeration enum1 = properties.keys();
    while ( enum1.hasMoreElements() ) {
      Object key = enum1.nextElement();
      Object value = properties.get( key );
      pl.put( key.toString(), value.toString() );
    }
    init( pl );
  }

  protected void init( final String driver, final String provider, final String userName, final String password,
                       final Properties props ) {
    StringBuffer buffer = new StringBuffer();
    buffer.append( "provider=" + provider ); //$NON-NLS-1$
    //
    // MB - This is a hack. Should instead have either a flag or a
    // different method for specifying a datasource instead of this.
    //
    // TODO: Fix for post 1.2 RC 2
    //

    //
    // WES - This hack was fixed up to maintain backward capability.
    // In addition methods were added so that connection info can be passed
    // in via a properties map.

    if ( driver.indexOf( "dataSource=" ) >= 0 ) { //$NON-NLS-1$
      buffer.append( "; " ).append( driver ); //$NON-NLS-1$
    } else {
      buffer.append( "; Jdbc=" + driver ); //$NON-NLS-1$
    }
    if ( userName != null ) {
      buffer.append( "; JdbcUser=" + userName ); //$NON-NLS-1$
    }
    if ( password != null ) {
      buffer.append( "; JdbcPassword=" + password ); //$NON-NLS-1$
    }

    Enumeration enum1 = props.keys();
    while ( enum1.hasMoreElements() ) {
      String key = (String) enum1.nextElement();
      if ( IPentahoConnection.CONNECTION.equals( key ) || IPentahoConnection.PROVIDER.equals( key )
        || IPentahoConnection.USERNAME_KEY.equals( key ) || IPentahoConnection.PASSWORD_KEY.equals( key ) ) {
        continue;
      }
      buffer.append( "; " + key + "=" + props.get( key ) );
    }

    init( buffer.toString() );
  }

  protected void mapPlatformRolesToMondrianRoles( Util.PropertyList properties ) throws PentahoAccessControlException {
    mapPlatformRolesToMondrianRolesHelper( properties );
  }

  public static void mapPlatformRolesToMondrianRolesHelper( Util.PropertyList properties )
    throws PentahoAccessControlException {
    if ( properties.get( RolapConnectionProperties.Role.name(), null ) == null ) {
      // Only if the action sequence/requester hasn't already injected a role in here do this.

      if ( PentahoSystem.getObjectFactory().objectDefined( MDXConnection.MDX_CONNECTION_MAPPER_KEY ) ) {
        IConnectionUserRoleMapper mondrianUserRoleMapper =
          PentahoSystem.get( IConnectionUserRoleMapper.class, MDXConnection.MDX_CONNECTION_MAPPER_KEY, null );
        if ( mondrianUserRoleMapper != null ) {
          // Do role mapping
          String[] validMondrianRolesForUser =
            mondrianUserRoleMapper.mapConnectionRoles( PentahoSessionHolder.getSession(), properties
              .get( RolapConnectionProperties.Catalog.name() ) );
          if ( ( validMondrianRolesForUser != null ) && ( validMondrianRolesForUser.length > 0 ) ) {
            StringBuffer buff = new StringBuffer();
            String aRole = null;
            for ( int i = 0; i < validMondrianRolesForUser.length; i++ ) {
              aRole = validMondrianRolesForUser[ i ];
              // According to http://mondrian.pentaho.org/documentation/configuration.php
              // double-comma escapes a comma
              if ( i > 0 ) {
                buff.append( "," ); //$NON-NLS-1$
              }
              buff.append( aRole.replaceAll( ",", ",," ) ); //$NON-NLS-1$//$NON-NLS-2$
            }
            properties.put( RolapConnectionProperties.Role.name(), buff.toString() );
          }
        }
      }
    }
  }

  protected void init( Util.PropertyList properties ) {
    try {
      if ( nativeConnection != null ) { // Assume we're open
        close();
      }

      // Set a locale for this connection if specified in the platform's mondrian metadata
      // This is required if mondrian.i18n.LocalizingDynamicSchemaProcessor is being used
      if ( properties.get( RolapConnectionProperties.Locale.name() ) == null ) {
        properties.put( RolapConnectionProperties.Locale.name(), LocaleHelper.getLocale().toString() );
      }

      String dataSourceName = properties.get( RolapConnectionProperties.DataSource.name() );

      mapPlatformRolesToMondrianRoles( properties );

      if ( dataSourceName != null ) {
        IDBDatasourceService datasourceService =
          PentahoSystem.getObjectFactory().get( IDBDatasourceService.class, null );
        DataSource dataSourceImpl = datasourceService.getDataSource( dataSourceName );
        if ( dataSourceImpl != null ) {
          properties.remove( RolapConnectionProperties.DataSource.name() );
          nativeConnection = DriverManager.getConnection( properties, null, dataSourceImpl );
        } else {
          nativeConnection = DriverManager.getConnection( properties, null );
        }
      } else {
        nativeConnection = DriverManager.getConnection( properties, null );
      }

      if ( nativeConnection != null ) {
        if ( role != null ) {
          nativeConnection.setRole( role );
        }
      }

      if ( nativeConnection == null ) {
        logger.error( Messages.getInstance().getErrorString(
          "MDXConnection.ERROR_0002_INVALID_CONNECTION",
          properties != null ? properties.toString() : "null" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    } catch ( Throwable t ) {
      if ( logger != null ) {
        logger.error( Messages.getInstance().getErrorString(
          "MDXConnection.ERROR_0002_INVALID_CONNECTION", properties != null ? properties.toString() : "null" ),
          t ); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        Logger.error( this.getClass().getName(), Messages.getInstance().getErrorString(
          "MDXConnection.ERROR_0002_INVALID_CONNECTION", properties != null ? properties.toString() : "null" ),
          t ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }

  public boolean initialized() {
    return nativeConnection != null;
  }

  @SuppressWarnings ( "unchecked" )
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
    if ( nativeConnection != null ) {
      nativeConnection.close();
    }
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
    Query mdxQuery = nativeConnection.parseQuery( query );
    Result result = nativeConnection.execute( mdxQuery );
    resultSet = new MDXResultSet( result, nativeConnection, useExtendedColumnNames );
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
    if ( nativeConnection != null ) { // Assume we're open
      close();
    }
    init( props );
    String query = props.getProperty( IPentahoConnection.QUERY_KEY );
    if ( ( query != null ) && ( query.length() > 0 ) && ( nativeConnection != null ) ) {
      executeQuery( query );
    }
    return nativeConnection != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#setMaxRows(int)
   */
  public void setMaxRows( final int maxRows ) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#setFetchSize(int)
   */
  public void setFetchSize( final int fetchSize ) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  public Connection getConnection() {
    return nativeConnection;
  }

  /**
   * return datasource type MDX
   *
   * @return datasource type
   */
  public String getDatasourceType() {
    return IPentahoConnection.MDX_DATASOURCE;
  }

  public void setUseExtendedColumnNames( boolean useExtendedColumnNames ) {
    this.useExtendedColumnNames = useExtendedColumnNames;
  }

  public void setRole( Role customRole ) {
    this.role = customRole;
  }
}
