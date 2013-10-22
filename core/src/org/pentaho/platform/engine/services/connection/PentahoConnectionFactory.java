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
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.connection;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.IPentahoLoggingConnection;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

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
}
