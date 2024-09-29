/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.core.system.status;

import org.pentaho.platform.api.engine.IServerStatusChangeListener;
import org.pentaho.platform.api.engine.IServerStatusProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author tkafalas
 *
 *         The purpose of this class is to provide a thread that will log a message periodically to the console. The
 *         message can be changed at any time and, in that case, the next periodic iteration will show the new message.
 * 
 */
public class PeriodicStatusLogger implements Runnable, IServerStatusChangeListener {

  private static Logger logger = LoggerFactory.getLogger( PeriodicStatusLogger.class );
  private static int cycleTime = 30000;
  private static PeriodicStatusLogger periodicStatusLogger;
  private static IServerStatusProvider serverStatusProvider;
  private static Thread runThread;

  private boolean stopFlag = false;
  private String[] lastMessages;
  private IServerStatusProvider.ServerStatus lastServerStatus;

  static {
    periodicStatusLogger = new PeriodicStatusLogger();

    serverStatusProvider = IServerStatusProvider.LOCATOR.getProvider();
    serverStatusProvider.registerServerStatusChangeListener( periodicStatusLogger );
  }

  private PeriodicStatusLogger() {
    // Access through static methods only
  }

  private static PeriodicStatusLogger getInstance() {
    return periodicStatusLogger;
  }

  public static synchronized void start() {
    if ( runThread != null ) {
      throw new IllegalStateException( "Only one instance of the PeriodicStatusLogger is allowed" );
    }
    runThread = new Thread( getInstance() );
    runThread.start();
  }

  public static synchronized void stop() {
    if ( periodicStatusLogger == null || runThread == null ) {
      throw new IllegalStateException( "The PeriodicStatusLogger has not been started" );
    }
    periodicStatusLogger.stopFlag = true;
    runThread = null;
  }

  public void run() {
    setCurrentValues();
    while ( !stopFlag ) {
      logMessages();
      try {
        Thread.sleep( cycleTime );
      } catch ( InterruptedException e ) {
        // The interrupt will force an immediate recycle. Interrupts occur when the status message is changed
      }
    }
  }

  private void logMessages() {
    if ( runThread != null &&  lastMessages != null ) {
      for ( String message : lastMessages ) {
        if ( logger.isInfoEnabled() ) {
          logger.info( message );
        } else {
          logger.error( message );
        }
      }
    }
  }

  public static String[] getStatusMessages() {
    return serverStatusProvider.getStatusMessages();
  }

  public static IServerStatusProvider.ServerStatus getServerStatus() {
    return serverStatusProvider.getStatus();
  }

  public static void setCycleTime( int cycleTime ) {
    PeriodicStatusLogger.cycleTime = cycleTime;
  }

  public static int getCycleTime() {
    return PeriodicStatusLogger.cycleTime;
  }

  @Override
  public void onStatusChange() {
    if ( lastMessages != serverStatusProvider.getStatusMessages()
        || lastServerStatus != serverStatusProvider.getStatus() ) {
      setCurrentValues();
      logMessages();
      if ( runThread != null ) {
        runThread.interrupt();
      }
    }
  }

  private void setCurrentValues() {
    lastMessages = serverStatusProvider.getStatusMessages();
    lastServerStatus = serverStatusProvider.getStatus();
  }
}
