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
