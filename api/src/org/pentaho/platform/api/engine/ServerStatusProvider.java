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
package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.engine.IServerStatusChangeListener;
import org.pentaho.platform.api.engine.IServerStatusProvider;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is the default implementation of server status log message provider.
 * 
 * @author tkafalas
 *
 */
public class ServerStatusProvider implements IServerStatusProvider {

  private String[] messages;
  private IServerStatusProvider.ServerStatus serverStatus = IServerStatusProvider.ServerStatus.DOWN;
  private final List<IServerStatusChangeListener> listeners = new CopyOnWriteArrayList<IServerStatusChangeListener>();

  @Override
  public ServerStatus getStatus() {
    return serverStatus;
  }

  @Override
  public String[] getStatusMessages() {
    return messages;
  }

  public void setStatus( IServerStatusProvider.ServerStatus serverStatus ) {
    if ( this.serverStatus != serverStatus ) {
      this.serverStatus = serverStatus;
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

  public void setStatusMessages( String[] messages ) {
    if ( this.messages != messages ) {
      this.messages = messages;
      fireOnChange();
    }
  }

  private void fireOnChange() {
    for ( IServerStatusChangeListener listener : listeners ) {
      listener.onStatusChange();
    }
  }

}
