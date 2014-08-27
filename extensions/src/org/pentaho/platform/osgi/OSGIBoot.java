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
 * Copyright 2013 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * This {@link org.pentaho.platform.api.engine.IPentahoSystemListener} starts a Felix OSGI container. It then installs
 * bundles found in system/osgi and finally calls the {@link PentahoOSGIActivator} to bootstrap OSGI into the
 * PentahoSystem aggregate objectFactory
 */
public class OSGIBoot implements IPentahoSystemListener {
  private final PentahoOSGIActivator pentahoOSGIActivator = new PentahoOSGIActivator();
  Framework framework;
  private Logger logger = LoggerFactory.getLogger( OSGIBoot.class );

  @Override
  public boolean startup( IPentahoSession session ) {

    logger.info( "Starting OSGI Environment" );
    String solutionRootPath = PentahoSystem.getApplicationContext().getSolutionRootPath();

    final String sep = File.separator;

    // set the location of the log4j config file, since OSGI won't pick up the one in webapp
    System.setProperty( "log4j.configuration", "file://" + solutionRootPath + "/system/osgi/log4j.xml" );
    // Setting ignoreTCL to true such that the OSGI classloader used to initialize log4j will be the
    // same one used when instatiating appenders.
    System.setProperty( "log4j.ignoreTCL", "true" );

    Properties osgiProps = new Properties();
    File propsFile = new File( solutionRootPath + sep + "system" + sep + "osgi" + sep + "config.properties" );
    if ( propsFile.exists() ) {
      try {
        osgiProps.load( new FileInputStream( propsFile ) );
      } catch ( IOException e ) {
        logger.error( "Error reading OSGI Host config", e );
      }
    }

    Map<String, String> configProps = new HashMap<String, String>();

    for ( Map.Entry<Object, Object> entry : osgiProps.entrySet() ) {
      configProps.put( entry.getKey().toString(), entry.getValue().toString() );
    }

    // configProps.put( FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, Collections.singletonList(new
    // org.pentaho.platform.osgi.PentahoOSGIActivator()) );
    configProps.put( Constants.FRAMEWORK_STORAGE, solutionRootPath + "/system/osgi/cache" );
    System.setProperty( "felix.fileinstall.dir", solutionRootPath + "/system/osgi/bundles" );

    configProps.put( "felix.fileinstall.debug", "4" );
    configProps.put( "felix.fileinstall.bundles.new.start", "true" );
    configProps.put( "felix.fileinstall.bundles.startActivationPolicy", "false" );

    configProps.put( "org.eclipse.virgo.kernel.home", solutionRootPath + "/osgi/virgo" );
    configProps.put( "org.eclipse.virgo.kernel.config", solutionRootPath + "/osgi/virgo/configuration" );
    configProps.put( "osgi.sharedConfiguration.area", solutionRootPath + "/osgi/virgo/configuration" );
    configProps.put( "osgi.configuration.area", solutionRootPath + "/osgi/virgo/configuration" );
    configProps.put( "osgi.install.area", solutionRootPath + "/osgi/virgo" );
    configProps.put( "eclipse.ignoreApp", "true" );

    try {
      logger.debug( "Attempting to load OSGI FrameworkFactory." );
      FrameworkFactory factory = ServiceLoader.load( FrameworkFactory.class ).iterator().next();
      logger.debug( "FrameworkFactory found" );
      framework = factory.newFramework( configProps );
      logger.debug( "Initializing FrameworkFactory" );
      framework.init();

      logger.debug( "Starting FrameworkFactory" );
      framework.start();

      Runtime.getRuntime().addShutdownHook( new Thread( "Felix Shutdown Hook" ) {
        public void run() {
          shutdownFramework();
        }
      } );

      List<Bundle> bundleList = new ArrayList<Bundle>();

      File[] bundleDirectories =
        new File[] {
          new File( solutionRootPath + File.separator + "system" + File.separator + "osgi" + File.separator
            + "core_bundles" ),
          new File( solutionRootPath + File.separator + "system" + File.separator + "osgi" + File.separator
            + "fragment_bundles" ),
          new File( solutionRootPath + File.separator + "system" + File.separator + "osgi" + File.separator
            + "bundles" ) };

      logger.debug( "Installing bundles" );
      for ( File bundleDirectory : bundleDirectories ) {
        if ( bundleDirectory.exists() == false ) {
          logger.warn( "Bundle directory: " + bundleDirectory.getName() + " does not exist" );
          continue;
        }
        File[] files = bundleDirectory.listFiles();
        Arrays.sort( files );
        for ( File f : files ) {
          if ( f.isFile() && f.getName().endsWith( ".jar" ) ) {
            try {
              Bundle b = framework.getBundleContext().installBundle( f.toURI().toString() );
              bundleList.add( b );
            } catch ( Exception e ) {
              logger.error( "Error installing Bundle", e );
            }
          }
        }
      }

      logger.debug( "Starting bundles" );
      for ( Bundle bundle : bundleList ) {
        try {
          // detect if a fragment bundle and skip. They cannot be started..
          if ( bundle.getHeaders().get( "Fragment-Host" ) != null ) {
            continue;
          }
          bundle.start();
        } catch ( Exception e ) {
          logger.error( "Error installing Bundle", e );
        }
      }

      pentahoOSGIActivator.setBundleContext( framework.getBundleContext() );

      return true;
    } catch ( Exception ex ) {
      logger.error( "Error starting OSGI environment", ex );
      return false;
    }
  }

  private void shutdownFramework() {
    try {
      if ( framework != null ) {
        framework.stop();
        framework.waitForStop( 0 );
      }
    } catch ( Exception ex ) {
      logger.error( "Error stopping OSGI", ex );
    }
  }

  @Override
  public void shutdown() {
    pentahoOSGIActivator.shutdown();
    shutdownFramework();
  }
}
