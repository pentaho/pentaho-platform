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
