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
 * Copyright 2015 Pentaho Corporation.  All rights reserved.
 */
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
