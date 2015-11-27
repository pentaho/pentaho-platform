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
package org.pentaho.platform.engine.core.system.status;

import org.pentaho.platform.api.engine.IServerStatusChangeListener;
import org.pentaho.platform.api.engine.IServerStatusProvider;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is intended as a temporary implementation of server status log message provider.
 * 
 * @author tkafalas
 *
 */
public class ServerStatusProvider implements IServerStatusProvider {

  private static String[] messages;
  private static IServerStatusProvider.ServerStatus serverStatus = IServerStatusProvider.ServerStatus.DOWN;
  private static final List<IServerStatusChangeListener> listeners = new CopyOnWriteArrayList<IServerStatusChangeListener>();
  private static final ServerStatusProvider serverStatusProvider = new ServerStatusProvider();

  private ServerStatusProvider() {
    // Access through static methods only
  }

  public static ServerStatusProvider getInstance() {
    return serverStatusProvider;
  }

  @Override
  public ServerStatus getStatus() {
    return serverStatus;
  }

  @Override
  public String[] getStatusMessages() {
    return messages;
  }

  public static void setServerStatus( IServerStatusProvider.ServerStatus serverStatus ) {
    if ( ServerStatusProvider.serverStatus != serverStatus ) {
      ServerStatusProvider.serverStatus = serverStatus;
      fireOnChange();
    }
  }

  @Override
  public void registerServerStatusChangeListener( IServerStatusChangeListener serverStatusChangeListener ) {
    listeners.add( serverStatusChangeListener );
  }

  @Override
  public void removeServerStatusChangeListener( IServerStatusChangeListener serverStatusChangeListener ) {
    listeners.remove( serverStatusChangeListener );
  }

  public static void setStatusMessages( String[] messages ) {
    if ( ServerStatusProvider.messages != messages ) {
      ServerStatusProvider.messages = messages;
      fireOnChange();
    }
  }

  private static void fireOnChange() {
    for ( IServerStatusChangeListener listener : listeners ) {
      listener.onStatusChange();
    }
  }

}
