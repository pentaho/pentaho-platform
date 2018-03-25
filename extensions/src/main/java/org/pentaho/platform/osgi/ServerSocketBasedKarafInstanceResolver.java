/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.osgi;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This implementation resolves Karaf instance numbers based on a ServerSocket port strategy.
 * <p/>
 * It also handles assigning a cache folder appropriate for the client type (spoon, kitchen, carte, etc) guaranteed to
 * not be in use by another instance.
 * <p/>
 * Created by nbaker on 3/21/16.
 */
class ServerSocketBasedKarafInstanceResolver implements IKarafInstanceResolver {
  private static final int START_PORT_NUMBER = determineStartPort();
  public static final String PENTAHO_KARAF_INSTANCE_START_PORT = "pentaho.karaf.instance.start.port";

  static int determineStartPort() {
    String portStr = System.getProperty( PENTAHO_KARAF_INSTANCE_START_PORT, "11000" );
    int portNo = NumberUtils.toInt( portStr );
    return portNo != 0 ? portNo : 11000;
  }

  private static final int MAX_NUMBER_OF_KARAF_INSTANCES = 1000;
  public static final String DATA = "data";
  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override public void resolveInstance( KarafInstance instance ) throws KarafInstanceResolverException {

    // Obtaining a valid instance number in and of itself isn't sufficient. Since ports will be assigned based on
    // the instance number as an offset all ports must resolve as well otherwise the instance isn't valid and another
    // should be tried.
    int latestOffsetTried = 0;
    do {
      latestOffsetTried = resolveInstanceNumber( instance, latestOffsetTried );
    } while ( !resolvePorts( instance ) );

    // If no exception was thrown we're here now with all ports resolved
    assignAvailableCacheFolderForType( instance );

  }

  private void assignAvailableCacheFolderForType( KarafInstance instance ) {

    // something like karaf/caches
    String cacheParentFolder = instance.getCacheParentFolder();

    // We separate the caches by client type to avoid reuse of an inappropriate data folder
    File clientTypeCacheFolder = new File( cacheParentFolder + "/" + instance.getClientType() );
    clientTypeCacheFolder.mkdirs();

    File[] dataDirectories = clientTypeCacheFolder.listFiles( new FilenameFilter() {
      @Override public boolean accept( File dir, String name ) {
        return name.startsWith( DATA );
      }
    } );

    int maxInstanceNoFound = 0;
    Pattern pattern = Pattern.compile( DATA + "\\-([0-9]+)" );

    // Go through existing data directories. If one is not in-use, use that. If not find the highest number and create
    // one greater
    for ( File dataDirectory : dataDirectories ) {
      boolean locked = true;
      Matcher matcher = pattern.matcher( dataDirectory.getName() );
      if ( !matcher.matches() ) {
        // unexpected directory, not a data folder, skipping
        continue;
      }
      // extract the data folder number
      int instanceNo = Integer.parseInt( matcher.group( 1 ) );

      maxInstanceNoFound = Math.max( maxInstanceNoFound, instanceNo );

      File lockFile = new File( dataDirectory, ".lock" );
      FileLock lock = null;
      if ( !lockFile.exists() ) {
        // Lock file was not present, we can use it
        locked = false;
      } else {
        // Lock file was there, see if another process actually holds a lock on it
        try {
          FileOutputStream fileOutputStream = new FileOutputStream( lockFile );
          try {
            lock = fileOutputStream.getChannel().tryLock();
            if ( lock != null ) {
              // not locked by another program
              instance.setCacheLock( lock );
              locked = false;
            }
          } catch ( Exception e ) {
            // Lock active on another program
          }
        } catch ( FileNotFoundException e ) {
          logger.error( "Error locking file in data cache directory", e );
        }
      }

      if ( !locked ) {
        instance.setCachePath( dataDirectory.getPath() );
        // we're good to use this one, break out of existing directory loop
        break;
      }
    }

    if ( instance.getCachePath() == null ) {
      // Create a new cache folder
      File newCacheFolder = null;
      while ( newCacheFolder == null ) {
        maxInstanceNoFound++;
        File candidate = new File( clientTypeCacheFolder, DATA + "-" + maxInstanceNoFound );
        if ( candidate.exists() ) {
          // Another process slipped in and created a folder, lets skip over them
          continue;
        }
        newCacheFolder = candidate;
      }

      FileOutputStream fileOutputStream = null;
      try {
        newCacheFolder.mkdir();
        // create lock file and lock it for this process

        File lockFile = new File( newCacheFolder, ".lock" );
        fileOutputStream = new FileOutputStream( lockFile );
        FileLock lock = fileOutputStream.getChannel().lock();
        instance.setCachePath( newCacheFolder.getPath() );
        instance.setCacheLock( lock );
      } catch ( IOException e ) {
        logger.error( "Error creating data cache folder", e );
      }
    }


  }

  private boolean resolvePorts( KarafInstance instance ) {
    List<KarafInstancePort> ports = instance.getPorts();
    int instanceNumber = instance.getInstanceNumber();
    for ( KarafInstancePort port : ports ) {
      int portNo = port.getStartPort() + instanceNumber;
      if ( !isPortAvailable( portNo ) ) {
        return false;
      }
      port.setAssignedPort( portNo );
    }

    return true;
  }

  private boolean isPortAvailable( int port ) {

    try ( Socket ignored = new Socket( "localhost", port ) ) {
      return false;
    } catch ( IOException e ) {
      return true;
    }
  }

  private int resolveInstanceNumber( KarafInstance instance, int latestOffsetTried )
      throws KarafInstanceResolverException {
    logger.debug( "Attempting to resolve available Karaf instance number by way of Server Socket" );
    int testInstance = latestOffsetTried + 1;
    Integer instanceNo = null;
    do {
      int candidate = START_PORT_NUMBER + testInstance;
      Socket socket = null;
      try {
        logger.debug( "Instance test. Trying port " + candidate );
        socket = new Socket( "localhost", candidate );
        socket.close();
      } catch ( ConnectException e ) {
       // port not in-use
        try {
          ServerSocket ssocket = new ServerSocket( candidate );
          instanceNo = testInstance;
          instance.setInstanceSocket( ssocket );
          instance.setInstanceNumber( instanceNo );
          logger.debug( "Karaf instance resolved to: " + instanceNo );
        } catch ( IOException ignored ) {
          // couldn't bind port, move on to the next candidate
        }
      } catch ( IOException ignored ) {
        // socket test failed, move on to the next candidate
      }
    } while ( instanceNo == null && testInstance++ <= MAX_NUMBER_OF_KARAF_INSTANCES );

    if ( instanceNo == null ) {
      throw new KarafInstanceResolverException( "Unable to resolve Karaf Instance number" );
    }
    return instanceNo;
  }
}
