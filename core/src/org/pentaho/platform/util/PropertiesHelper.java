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
 * Copyright 2006 - 2014 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


public class PropertiesHelper {
  public static List<Properties> segment( Properties properties ) {
    Set<String> keys = properties.stringPropertyNames();
    HashMap<String, Properties> map = new HashMap<String, Properties>();
    for ( String key : keys ) {
      String[] split = key.split( "\\.", 2 );
      String segmentKey;
      String propertyKey;
      if ( split.length == 1 ) {
        segmentKey = "";
        propertyKey = key;
      } else {
        segmentKey = split[ 0 ];
        propertyKey = split[ 1 ];
      }
      Properties segment = getProperties( map, segmentKey );
      segment.setProperty( propertyKey, properties.getProperty( key ) );
      map.put( segmentKey, segment );
    }

    List<Properties> list = new ArrayList<Properties>();
    list.addAll( map.values() );
    return list;
  }

  private static Properties getProperties( Map<String, Properties> map, String key ) {
    Properties properties = map.get( key );
    if ( properties == null ) {
      properties = new Properties();
    }
    return properties;
  }
}
