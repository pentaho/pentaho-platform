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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.settings.PortFileManager;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;
import org.pentaho.platform.settings.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class assigns and configures property settings for separate karaf instances so that multiple client/server
 * applications can run simultaneously on the same host. It assigns/creates a unique cache folder for each karaf
 * instance and maintains what folders are in use by implementing a lock file.
 *
 * @author tkafalas
 */
public class KarafInstance {
  private static final String YAML_FILE_NAME = "KarafPorts.yaml";

  private static Logger logger = LoggerFactory.getLogger( KarafInstance.class );
  private int instanceNumber = 0;
  private ServerSocket instanceSocket;
  private String cachePath;
  private final String cacheParentFolder;
  private HashMap<String, KarafInstancePort> instancePorts = new HashMap<>();

  private static final int BANNER_WIDTH = 79;
  private static final String USED_PORT_FILENAME = "PortsAssigned.txt";
  private boolean started;

  private StringBuilder banner = new StringBuilder();

  private String instanceFilePath;
  private String clientType;
  private FileLock cacheLock;

  static {
    getResolver(); // Eliminate race condition by getting the resolver
  }

  public KarafInstance( String root, String clientType ) {
    this( root, System.getProperty( "karaf.etc" ) + "/" + YAML_FILE_NAME, "default" );
    this.clientType = clientType;
  }

  public KarafInstance( String root, String instanceFilePath, String clientType ) {
    this.cacheParentFolder = root + "/caches";
    this.instanceFilePath = instanceFilePath;

    try {
      processConfigFile();
    } catch ( FileNotFoundException e ) {
      throw new IllegalStateException( "Port Config file (" + this.instanceFilePath + ") not found", e );
    }
  }

  public void setCachePath( String cachePath ) {
    this.cachePath = cachePath;
    System.setProperty( "karaf.data", cachePath );
  }

  @SuppressWarnings( "unchecked" )
  public void processConfigFile() throws FileNotFoundException {

    try ( InputStream input = new FileInputStream( new File( instanceFilePath ) ) ) {

      Yaml yaml = new Yaml();
      Map<String, Object> map = (Map<String, Object>) yaml.load( input );
      List<Map<String, Object>> serviceList = (List<Map<String, Object>>) map.get( "Service" );
      for ( Map<String, Object> serviceMap : serviceList ) {
        Service service =
            new Service( serviceMap.get( "serviceName" ).toString(),
                serviceMap.get( "serviceDescription" ).toString() );
        ServerPortRegistry.addService( service );
      }

      List<Map<String, Object>> portList = (List<Map<String, Object>>) map.get( "ServerPort" );
      for ( Map<String, Object> portMap : portList ) {
        KarafInstancePort karafPort =
            new KarafInstancePort( this, portMap.get( "id" ).toString(), portMap.get( "property" ).toString(),
                portMap.get(
                    "friendlyName" ).toString(), (Integer) portMap.get( "startPort" ),
                portMap.get( "serviceName" ).toString() );
        this.registerPort( karafPort );
      }

    } catch ( FileNotFoundException e ) {
      throw new FileNotFoundException( "File " + instanceFilePath
          + " does not exist.  Could not determine karaf port assignment" );
    } catch ( Exception e ) {
      throw new RuntimeException( "Could not parse file " + instanceFilePath
          + ".", e );
    }
  }

  protected static IKarafInstanceResolver resolver;

  protected static IKarafInstanceResolver getResolver() {
    if ( resolver == null ) {
      String clazz = System.getProperty( KarafBoot.PENTAHO_KARAF_INSTANCE_RESOLVER_CLASS );
      if ( clazz != null ) {
        try {
          resolver = (IKarafInstanceResolver) Class.forName( clazz ).newInstance();
        } catch ( Exception e ) {
          logger.error( "Error instantiating user defined Karaf Instance Resolver: " + clazz, e );
        }
      }
      if ( resolver == null ) {
        // Something went wrong or we're using the default
        resolver = new ServerSocketBasedKarafInstanceResolver();
      }
    }
    return resolver;
  }

  public void assignPortsAndCreateCache() throws KarafInstanceResolverException {

    getResolver().resolveInstance( this );

    if ( started ) {
      throw new IllegalStateException( "Attempt to start a karaf instance that is already started" );
    }
    started = true;

    banner.append( "\n" ).append( StringUtils.repeat( "*", BANNER_WIDTH ) );
    bannerLine( "Karaf Instance Number: " + instanceNumber + " at " + cachePath );
    SortedSet<String> ids = new TreeSet<>( instancePorts.keySet() );
    for ( String id : ids ) {
      ServerPort propertyInstance = instancePorts.get( id );
      bannerLine( propertyInstance.getFriendlyName() + ":" + propertyInstance.getAssignedPort() );
    }
    banner.append( "\n" ).append( StringUtils.repeat( "*", BANNER_WIDTH ) );

    logger.info( banner.toString() );

    // Writing the used ports
    String usedPortFilePath = cachePath + "/" + USED_PORT_FILENAME;
    try {
      PortFileManager.getInstance().writeUsedPortFile( usedPortFilePath );
    } catch ( Exception e ) {
      // If the ports couldn't be written, log it
      logger.warn( "Could not write " + usedPortFilePath + ".", e );
    }
  }


  private void bannerLine( String line ) {
    if ( line.length() > BANNER_WIDTH - 8 ) {
      bannerLine( line.substring( 0, BANNER_WIDTH - 8 ) );
      line = "  " + line.substring( BANNER_WIDTH - 8 );
    }
    banner.append( "\n*** " ).append( line ).append( StringUtils.repeat( " ", BANNER_WIDTH - line.length() - 7 ) )
        .append( "***" );
  }

  public void close() throws IOException {
    instanceSocket.close();
    cacheLock.acquiredBy().close();
  }

  public int getInstanceNumber() {
    return instanceNumber;
  }

  public String getCachePath() {
    return cachePath;
  }

  public void registerPort( KarafInstancePort instancePort ) {
    if ( started ) {
      throw new IllegalStateException( "Ports must be added Before the Karaf instance is started" );
    }
    if ( instancePorts.containsKey( instancePort.getId() ) ) {
      throw new IllegalStateException( "Id " + instancePort.getId() + " already defined." );
    }
    instancePorts.put( instancePort.getId(), instancePort );
    ServerPortRegistry.addPort( instancePort );
  }

  public KarafInstancePort getPort( String id ) {
    return instancePorts.get( id );
  }

  public Set<String> getPortIds() {
    return instancePorts.keySet();
  }

  public List<KarafInstancePort> getPorts() {
    return new ArrayList<>( instancePorts.values() );
  }

  @VisibleForTesting String getCacheParentFolder() {
    return cacheParentFolder;
  }

  public void setInstanceNumber( Integer instanceNumber ) {
    this.instanceNumber = instanceNumber;
  }

  public void setInstanceSocket( ServerSocket instanceSocket ) {
    this.instanceSocket = instanceSocket;
  }

  public String getClientType() {
    return clientType;
  }

  public void setCacheLock( FileLock cacheLock ) {
    this.cacheLock = cacheLock;
  }
}
