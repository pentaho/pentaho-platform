/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.connections;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.pentaho.platform.util.logging.Logger;

//This driver will delegate to drivers found in the Hitachi Vantara Object System
//For example it can delegate to the Mondrian 4 Olap4J Driver that lives in an OSGI Bundle
public class PentahoSystemDriver implements Driver {
  static {
    try {
      DriverManager.registerDriver( new PentahoSystemDriver() );
    } catch ( SQLException e ) {
      Logger.warn(
        PentahoSystemDriver.class.getName(),
        Messages.getInstance().getErrorString( "PentahoSystemDriver.ERROR_0001_COULD_NOT_REGISTER_DRIVER" ),
        e );
    }
  }

  private static final String JDBC = "jdbc:";

  List<Driver> getAllDrivers() {
    try {
      return PentahoSystem.getAll( Driver.class );
    } catch ( Throwable t ) {
      return Collections.emptyList();
    }
  }

  Map<String, String> getTranslationMap() {
    try {
      return PentahoSystem.get( Map.class, "jdbcDriverTranslationMap", PentahoSessionHolder.getSession() );
    } catch ( Throwable t ) {
      return Collections.emptyMap();
    }
  }

  private String translate( String url ) {
    Map<String, String> translationMap = getTranslationMap();
    if ( translationMap != null && url.startsWith( JDBC ) ) {
      String initial = url.substring( 5, url.indexOf( ":", 5 ) );
      if ( translationMap.containsKey( initial ) ) {
        return url.replace( JDBC + initial, JDBC + translationMap.get( initial ) );
      }
    }
    return url;
  }

  @Override
  public Connection connect( final String url, final Properties info ) throws SQLException {
    String translatedUrl = translate( url );
    List<Throwable> listOfExceptions = new ArrayList<>();
    SQLException exception = new SQLException("There were failing connections to the url: " + url);

    for ( Driver driver : getAllDrivers() ) {
      if ( driver.acceptsURL( translatedUrl ) ) {
        try {
          Connection conn = driver.connect( translatedUrl, info );
          if ( conn != null ) {
            return conn;
          }
        } catch ( SQLException e ) {
            exception.setNextException( e );
        } catch(Exception e ){
          throw e;
        }
      }

    }
    if ( exception.getNextException() !=  null  && !exception.getNextException().getMessage().equals(exception.getMessage())) {
      SQLException currentException  = exception.getNextException();
      while(currentException != null && currentException.getNextException()!= null && !currentException.getNextException().getMessage().equals(currentException.getMessage()) ){
        Logger.info( PentahoSystemDriver.class.getName(), currentException.getMessage(),currentException );
        currentException =  currentException.getNextException();

      }
      throw exception;
    }
    return null;
  }

  @Override
  public boolean acceptsURL( final String url ) throws SQLException {
    String translatedUrl = translate( url );
    for ( Driver driver : getAllDrivers() ) {
      if ( driver.acceptsURL( translatedUrl ) ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo( final String url, final Properties info ) throws SQLException {
    String translatedUrl = translate( url );
    for ( Driver driver : getAllDrivers() ) {
      if ( driver.acceptsURL( translatedUrl ) ) {
        return driver.getPropertyInfo( translatedUrl, info );
      }
    }
    return null;
  }

  @Override
  public int getMajorVersion() {
    return 0;
  }

  @Override
  public int getMinorVersion() {
    return 0;
  }

  @Override
  public boolean jdbcCompliant() {
    return true;
  }

  //don't add @Override annotation for Java 6 compatibility (class Driver doesn't have getParentLogger method in Java 6)
  public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException( "Impossible to know which Driver to fetch the logger from" );
  }
}
