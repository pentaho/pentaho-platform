package org.pentaho.platform.osgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class assigns and configures separate karaf instances so that multiple client/server applications can run
 * simultaneously on the same host.
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
  private static final int MAX_NUMBER_OF_KARAF_INSTANCES = 50;
  private static final int BANNER_WIDTH = 79;
  private StringBuilder banner = new StringBuilder();

  public KarafInstance( String root ) {
    this.root = root;
    instanceNumber = assignInstanceNumber();
    cachePath = root + "/data" + instanceNumber;

    // Get 3 open ports to used to drive karaf, jmx and jetty server
    int[] assignedPorts = ( new PortAssigner( 4 ) ).assignPorts();
    System.setProperty( "karaf.port", "" + assignedPorts[0] );// was 8081
    System.setProperty( "org.pentaho.osgi.service.http.port", "" + assignedPorts[1] );// was 8181
    System.setProperty( "org.pentaho.jmx.rmi.server.port", "" + assignedPorts[2] );// was 44444
    System.setProperty( "org.pentaho.jmx.rmi.registry.port", "" + assignedPorts[3] );// was 1099
    System.setProperty( "karaf.data", cachePath );

    banner.append( "\n" + StringUtils.repeat( "*", BANNER_WIDTH ) );
    bannerLine( "Karaf instance " + instanceNumber + " set to port " + assignedPorts[0] );
    bannerLine( "Jetty server for Pax Web on port " + assignedPorts[1] );
    bannerLine( "JMX RMI Server on port " + assignedPorts[2] );
    bannerLine( "JMX RMI Registry on port " + assignedPorts[3] );
    bannerLine( "Karaf Caching to " + cachePath );
    banner.append( "\n" + StringUtils.repeat( "*", BANNER_WIDTH ) );

    logger.info( banner.toString() );
  }

  private int assignInstanceNumber() {
    int testInstance = 1;
    while ( testInstance < 50 ) {
      cachePath = root + "/data" + testInstance;
      File cacheFolder = new File( cachePath );
      if ( !cacheFolder.exists() ) {
        cacheFolder.mkdirs();
      }
      try {
        fileLock = testLock( testInstance );
        if ( fileLock != null ) {
          return testInstance;
        }
      } catch ( FileNotFoundException e ) {
        e.printStackTrace();
      } catch ( IOException e ) {
        e.printStackTrace();
      }
      testInstance++;
    }
    throw new RuntimeException( "Could not determine karaf instance number.  Limit of " + MAX_NUMBER_OF_KARAF_INSTANCES
        + " Karaf instances exceeded." );
  }

  @SuppressWarnings( "resource" )
  private FileLock testLock( int testInstanceNumber ) throws FileNotFoundException, IOException {
    File lockFile = new File( cachePath + "/LOCKFILE" );
    FileChannel channel;
    channel = new RandomAccessFile( lockFile, "rw" ).getChannel();
    FileLock lock = null;
    try {
      lock = channel.tryLock();
    } catch ( OverlappingFileLockException e ) {
      // File is already locked in this thread or virtual machine. This should not happen.
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

}
