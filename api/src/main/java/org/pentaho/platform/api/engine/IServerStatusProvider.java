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


package org.pentaho.platform.api.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a facility that reports the current status of the server. This object will be tapped during startup to get
 * the current status of the server.
 * 
 * @author tkafalas
 */
public interface IServerStatusProvider {

  ServerStatus getStatus();

  void setStatus( IServerStatusProvider.ServerStatus serverStatus );

  String[] getStatusMessages();

  void setStatusMessages( String[] messages );

  void registerServerStatusChangeListener( IServerStatusChangeListener serverStatusChangeListener );

  void removeServerStatusChangeListener( IServerStatusChangeListener serverStatusChangeListener );

  public enum ServerStatus {
    DOWN, STARTING, STARTED, STOPPING, ERROR
  }

  Locator LOCATOR = new Locator();

  class Locator {
    private static final String PROVIDER_CLASS = "org.pentaho.platform.api.engine.IServerStatusProvider.class";
    IServerStatusProvider instance;
    private static Logger logger = LoggerFactory.getLogger( IServerStatusProvider.Locator.class );

    public IServerStatusProvider getProvider() {

      if ( instance == null ) {
        if ( System.getProperty( PROVIDER_CLASS ) != null ) {
          try {
            instance = (IServerStatusProvider) Class.forName( System.getProperty( PROVIDER_CLASS ) ).newInstance();
          } catch ( ClassNotFoundException e ) {
            logger.error( "ServerStatusProvider class not found", e );
          } catch ( InstantiationException | IllegalAccessException e ) {
            logger.error( "ServerStatusProvider class could not be instantiated", e );
          }
        }
        if ( instance == null ) {
          instance = new ServerStatusProvider();
        }
      }
      return instance;
    }
  }
}
