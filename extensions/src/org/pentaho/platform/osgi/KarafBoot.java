/*
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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.osgi;

import org.apache.karaf.main.Main;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * This Pentaho SystemListener starts the Embedded Karaf framework to support OSGI in the platform.
 *
 * Created by nbaker on 7/29/14.
 */
public class KarafBoot implements IPentahoSystemListener {
  private Main main;
  Logger logger = LoggerFactory.getLogger( getClass() );
  private static boolean initialized;

  @Override public boolean startup( IPentahoSession session ) {
    try {
      String solutionRootPath = PentahoSystem.getApplicationContext().getSolutionRootPath();
      String root = solutionRootPath + "/system/karaf";

      System.setProperty( "karaf.home", root );
      System.setProperty( "karaf.base", root );
      System.setProperty( "karaf.data", root + "/data" );
      System.setProperty( "karaf.history", root + "/data/history.txt" );
      System.setProperty( "karaf.instances", root + "/instances" );
      System.setProperty( "karaf.startLocalConsole", "false" );
      System.setProperty( "karaf.startRemoteShell", "true" );
      System.setProperty( "karaf.lock", "false" );

      // set the location of the log4j config file, since OSGI won't pick up the one in webapp
      System.setProperty( "log4j.configuration", "file:" + solutionRootPath + "/system/osgi/log4j.xml" );
      // Setting ignoreTCL to true such that the OSGI classloader used to initialize log4j will be the
      // same one used when instatiating appenders.
      System.setProperty( "log4j.ignoreTCL", "true" );

      main = new Main( new String[ 0 ] );
      main.launch();
    } catch ( Exception e ) {
      main = null;
      logger.error( "Error starting Karaf", e );
    }
    return true;
  }

  @Override public void shutdown() {
    try {
      main.destroy();
    } catch ( Exception e ) {
      logger.error( "Error stopping Karaf", e );
    }
  }
}
