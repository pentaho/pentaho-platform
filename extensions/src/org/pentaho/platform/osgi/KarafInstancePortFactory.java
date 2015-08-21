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
 * Copyright 2015 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.settings.Service;
import org.yaml.snakeyaml.Yaml;

public class KarafInstancePortFactory {
  protected static KarafInstance karafInstance;
  private String importFilePath;

  public KarafInstancePortFactory( String importFilePath ) {
    this.importFilePath = importFilePath;
    if ( karafInstance == null ) {
      karafInstance = KarafInstance.getInstance();
    }
  }

  @SuppressWarnings( "unchecked" )
  public void process() throws FileNotFoundException {

    InputStream input = null;
    try {
      input = new FileInputStream( new File( importFilePath ) );

      Yaml yaml = new Yaml();
      Map<String, Object> map = (Map<String, Object>) yaml.load( input );
      List<Map<String, Object>> serviceList = (List<Map<String, Object>>) map.get( "Service" );
      for ( Map<String, Object> serviceMap : serviceList ) {
        Service service =
            new Service( serviceMap.get( "serviceName" ).toString(), serviceMap.get( "serviceDescription" ).toString() );
        karafInstance.registerService( service );
      }

      List<Map<String, Object>> portList = (List<Map<String, Object>>) map.get( "ServerPort" );
      for ( Map<String, Object> portMap : portList ) {
        KarafInstancePort karafPort =
            new KarafInstancePort( portMap.get( "id" ).toString(), portMap.get( "property" ).toString(), portMap.get(
                "friendlyName" ).toString(), (Integer) portMap.get( "startPort" ), (Integer) portMap.get( "endPort" ),
                portMap.get( "serviceName" ).toString() );
        karafInstance.registerPort( karafPort );
      }

    } catch ( FileNotFoundException e ) {
      throw new FileNotFoundException( "File " + importFilePath
          + " does not exist.  Could not determine karaf port assignment" );
    } catch (Exception e ) {
      throw new RuntimeException("Could not parse file " + importFilePath
          + ".", e);
    } finally {
      IOUtils.closeQuietly( input );
    }
  }

}
