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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.osgi;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.settings.PortAssigner;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;
import org.pentaho.platform.settings.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class assigns and configures property settings for separate karaf instances so that multiple client/server
 * applications can run simultaneously on the same host. It assigns/creates a unique cache folder for each karaf
 * instance and maintains what folders are in use by implementing a lock file.
 * 
 * @author tkafalas
 *
 */
public class KarafInstance {
  Logger logger = LoggerFactory.getLogger( getClass() );
  private int instanceNumber;
  private String cachePath;
  private FileLock fileLock;
  private String root;
  private HashMap<String, KarafInstancePort> instancePorts = new HashMap<String, KarafInstancePort>();
  private static final int MAX_NUMBER_OF_KARAF_INSTANCES = 50;
  private static final int BANNER_WIDTH = 79;
  private static final String CACHE_DIR_PREFIX = "data";
  private static final String USED_PORT_FILENAME = "PortsAssigned.txt";
  private final PortAssigner portAssigner = PortAssigner.getInstance();
  private boolean started;
  private static KarafInstance instance; // Not final because unit test creates multiple instances

  private StringBuilder banner = new StringBuilder();

  KarafInstance( String root ) {
    KarafInstance.instance = this;
    this.root = root;
    String presetedCache = System.getProperty( "karaf.data" );
    updateKarafDataPath(  presetedCache == null ? root + "/" + CACHE_DIR_PREFIX : presetedCache );
  }

  public static KarafInstance getInstance() {
    if ( instance == null ) {
      throw new IllegalStateException( "Karaf instance has not yet been instantiated" );
    }
    return instance;
  }

  public void start() {
    if ( started ) {
      throw new IllegalStateException( "Attempt to start a karaf instance that is already started" );
    }
    started = true;
    for ( KarafInstancePort instancePort : instancePorts.values() ) {
      instancePort.assignPort();
    }

    // Writing the used ports may need to be moved somewhere after other plugins initialize. Here for now because the
    // sooner we write the file the lower the odds of another karaf instance grabbing the port.
    String usedPortFilePath = cachePath + "/" + USED_PORT_FILENAME;
    try {
      portAssigner.writeUsedPortFile( usedPortFilePath );
    } catch ( Exception e ) {
      // If the ports couldn't be written, log it, but
      logger.error( "Could not write " + usedPortFilePath
          + ".  This may cause port conflicts if multiple server instances are started on this host.", e );
    }

    banner.append( "\n" + StringUtils.repeat( "*", BANNER_WIDTH ) );
    bannerLine( "Karaf Instance Number: " + instanceNumber + " at " + cachePath );
    SortedSet<String> ids = new TreeSet<String>( instancePorts.keySet() );
    for ( String id : ids ) {
      ServerPort propertyInstance = instancePorts.get( id );
      bannerLine( propertyInstance.getFriendlyName() + ":" + propertyInstance.getValue() );
    }
    banner.append( "\n" + StringUtils.repeat( "*", BANNER_WIDTH ) );

    logger.info( banner.toString() );
  }

  private void updateKarafDataPath( String karafDataPath ) {
    int testInstance = 1;
    while ( testInstance <= MAX_NUMBER_OF_KARAF_INSTANCES ) {
      cachePath = karafDataPath + testInstance;
      File cacheFolder = new File( cachePath );
      if ( !cacheFolder.exists() ) {
        cacheFolder.mkdirs();
      }

      fileLock = testLock( cachePath );
      if ( fileLock != null ) {
        break; // Could get a lock, so we have an unused instance
      } else {
        processExternalPorts( cachePath );
      }

      testInstance++;
    }
    if ( testInstance > MAX_NUMBER_OF_KARAF_INSTANCES ) {
      throw new RuntimeException( "Could not determine karaf instance number.  Limit of "
          + MAX_NUMBER_OF_KARAF_INSTANCES + " Karaf instances exceeded." );
    }

    instanceNumber = testInstance;
    // Pull in any remaining externally reserved ports. Can't keep incrementing
    // because someone might have manually erased one of the cache folders (maybe to fix corruption, for instance).

    File file = new File( root );
    String[] folders = file.list( new FilenameFilter() {
      @Override
      public boolean accept( File current, String name ) {
        boolean result = false;
        if ( name.startsWith( CACHE_DIR_PREFIX ) && new File( current, name ).isDirectory() ) {
          //String pattern = CACHE_DIR_PREFIX + "\\d+";
          Pattern r = Pattern.compile( "(" + CACHE_DIR_PREFIX + ")(\\d+)$" );
          Matcher m = r.matcher( name );
          int testInstance = m.find() ? Integer.valueOf( m.group( 2 ) ) : 0;

          //int testInstance = Integer.valueOf( name.substring( CACHE_DIR_PREFIX.length() ) );
          if ( testInstance > instanceNumber ) {
            String folderName = root + "/" + name;
            FileLock lock = testLock( folderName );
            if ( lock != null ) {
              // We could get a lock so this instance is no in use
              IOUtils.closeQuietly( lock.channel() );
            } else {
              // This instance is in use.
              result = true;
            }
          }
        }
        return result;
      }
    } );

    for ( String folder : folders ) {
      processExternalPorts( root + "/" + folder );
    }
  }

  private void processExternalPorts( String externalCacheFolder ) {
    try {
      portAssigner.readUsedPortFile( externalCacheFolder + "/" + USED_PORT_FILENAME );
    } catch ( IOException e ) {
      logger.error( "No used port file " + externalCacheFolder + "/" + USED_PORT_FILENAME + " was found.", e );
    }
  }

  private FileLock testLock( String testFolder ) {
    File lockFile = new File( testFolder + "/LOCKFILE" );
    RandomAccessFile accessFile = null;
    FileLock lock = null;
    try {
      accessFile = new RandomAccessFile( lockFile, "rw" );
      FileChannel channel = accessFile.getChannel();
      lock = channel.tryLock();
    } catch ( OverlappingFileLockException e ) {
      // File is already locked in this thread or virtual machine. This should not happen.
      logger.error( "File is already locked" + e.getStackTrace() );
      try {
        if ( accessFile != null ) {
          accessFile.close();
        }
      } catch ( IOException e1 ) {
        logger.error( "Could not close resource LOCKFILE " + e.getStackTrace() );
      }
    } catch ( IOException e ) {
      // If we get an IO error here, there is probably something going on in the OS.
      logger.error( "Could not get lock on " + testFolder + "/LOCKFILE", e );
    }
    return lock;
  }

  private void bannerLine( String line ) {
    if ( line.length() > BANNER_WIDTH - 8 ) {
      bannerLine( line.substring( 0, BANNER_WIDTH - 8 ) );
      line = "  " + line.substring( BANNER_WIDTH - 8 );
    }
    banner.append( "\n*** " + line + StringUtils.repeat( " ", BANNER_WIDTH - line.length() - 7 ) + "***" );
  }

  /**
   * Used for unit tests. Normally the lock is release when the JVM closes
   * 
   * @throws IOException
   */
  public void close() throws IOException {
    FileChannel channel = fileLock.channel();
    fileLock.release();
    channel.close();
  }

  public String getBanner() {
    return banner.toString();
  }

  public int getInstanceNumber() {
    return instanceNumber;
  }

  public String getCachePath() {
    return cachePath;
  }

  public void registerPort( KarafInstancePort instancePort ) {
    if ( started ) {
      throw new IllegalStateException( "Must define properties before the karaf instance is started" );
    }
    if ( instancePorts.containsKey( instancePort.getId() ) ) {
      throw new IllegalStateException( "Id " + instancePort.getId() + " already defined." );
    }
    instancePorts.put( instancePort.getId(), instancePort );
    ServerPortRegistry.addPort( instancePort );
  }

  public void registerService( Service service ) {
    ServerPortRegistry.addService( service );
  }

  public KarafInstancePort getPort( String id ) {
    return instancePorts.get( id );
  }

  public Set<String> getPortIds() {
    return instancePorts.keySet();
  }

  public PortAssigner getPortAssigner() {
    return portAssigner;
  }

  public List<ServerPort> getProperties() {
    ArrayList<ServerPort> l = new ArrayList<ServerPort>();
    l.addAll( instancePorts.values() );
    return l;
  }

}
