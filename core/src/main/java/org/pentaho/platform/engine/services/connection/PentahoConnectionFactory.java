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


package org.pentaho.platform.engine.services.connection;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.IPentahoLoggingConnection;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Properties;

/**
 * @author wseyler
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
public class PentahoConnectionFactory {

  private static final String CONNECTION_PREFIX = "connection-"; //$NON-NLS-1$

  /**
   * @param datasourceType
   *          valid type is defined as SQL_DATASOURCE or MDX_DATASOURCE
   * @return a connection object that can be queried against.
   */
  public static IPentahoConnection getConnection( final String datasourceType, final IPentahoSession session,
      final ILogger logger ) {
    /*
     * TODO - This is where the "connection factory" action occurs. Based on if the datasourceType, location,
     * username, or password have changed then we create a new one.
     */
    return getConnection( datasourceType, (Properties) null, session, logger );
  }

  /**
   * @param datasourceType
   *          valid type is defined as SQL_DATASOURCE or MDX_DATASOURCE
   * @param connectStr
   *          - In the case of SQL_DATASOURCE, the name of the JNDI connection to use. Or in the case of
   *          MDX_DATASOURCE a properly formatted connection String.
   * @return a connection object that can be queried against.
   */
  public static IPentahoConnection getConnection( final String datasourceType, final String connectStr,
      final IPentahoSession session, final ILogger logger ) {

    Properties props = new Properties();
    props.put( IPentahoConnection.JNDI_NAME_KEY, connectStr );
    return getConnection( datasourceType, props, session, logger );
  }

  /**
   * @param datasourceType
   *          valid types are defined as SQL_DATASOURCE, MDX_DATASOURCE and XML_DATASOURCE
   * @param location
   *          - A string specfic to the location and type of datasource. For an SQL instance it would be the URL
   *          string required by the implementing driver.
   * @param userName
   * @param password
   * @return a connection object that can be queried against.
   */
  public static IPentahoConnection getConnection( final String datasourceType, final String driver,
      final String location, final String userName, final String password, final IPentahoSession session,
      final ILogger logger ) {
    Properties props = new Properties();
    if ( driver != null ) {
      props.put( IPentahoConnection.DRIVER_KEY, driver );
    }
    if ( location != null ) {
      props.put( IPentahoConnection.LOCATION_KEY, location );
    }
    if ( userName != null ) {
      props.put( IPentahoConnection.USERNAME_KEY, userName );
    }
    if ( password != null ) {
      props.put( IPentahoConnection.PASSWORD_KEY, password );
    }
    return getConnection( datasourceType, props, session, logger );
  }

  /**
   * 
   * @param datasourceType
   * @param properties
   *          can be null
   * @param session
   *          can be null
   * @param logger
   * @return
   */
  public static IPentahoConnection getConnection( final String datasourceType, Properties properties,
      final IPentahoSession session, final ILogger logger ) {
    /*
     * TODO - This is where the "connection factory" action occurs. Based on if the datasourceType, location,
     * username, or password have changed then we create a new one.
     */
    String key = CONNECTION_PREFIX + datasourceType;
    IPentahoConnection connection = null;
    try {
      //Validate if properties connection name is system DB and do not allow connection
      if ( isSystemConnection( properties ) && !hasSystemDataSourcePermission( session ) ) {
        throw new ObjectFactoryException( "Missing required permissions to make connection" );
      }
      connection = PentahoSystem.getObjectFactory().get( IPentahoConnection.class, key, session );
      if ( connection instanceof IPentahoLoggingConnection ) {
        ( (IPentahoLoggingConnection) connection ).setLogger( logger );
      }
      connection.setProperties( properties );
    } catch ( ObjectFactoryException e ) {
      Logger.error( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
          "PentahoConnectionFactory.ERROR_0001_COULD_NOT_CREATE_CONNECTION", key ), e ); //$NON-NLS-1$
    }

    return connection;
  }

  public static boolean isSystemConnection( Properties properties ) {

    if ( properties == null ) {
      return false;
    }
    String jndiConnectionName = properties.getProperty( IPentahoConnection.JNDI_NAME_KEY );
    String connectionName = properties.getProperty( IPentahoConnection.CONNECTION );

    return PentahoSystem.getSystemDatasourcesList().contains( jndiConnectionName )
      || PentahoSystem.getSystemDatasourcesList().contains( connectionName );
  }

  public static boolean hasSystemDataSourcePermission( IPentahoSession session ) {
    Authentication auth = SecurityHelper.getInstance().getAuthentication( session, true );

    for (GrantedAuthority userRole : auth.getAuthorities() ) {
      if ( PentahoSystem.getSystemDatasourcesRolesList().contains( userRole.getAuthority() ) ) {
        return true;
      }
    }

    return false;
  }
}
