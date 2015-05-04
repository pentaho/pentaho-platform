package org.pentaho.platform.engine.core.system.osgi;

import java.util.Map;

/**
 * Created by nbaker on 4/27/15.
 */
public class OSGIUtils {
  public static String createFilter( Map<String, String> props ) {
    if ( props == null || props.size() == 0 ) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    sb.append( "(" );
    for ( Map.Entry<String, String> entry : props.entrySet() ) {
      sb.append( "&(" ).append( entry.getKey() ).append( "=" ).append( entry.getValue() ).append( ")" );
    }
    sb.append( ")" );
    return sb.toString();
  }

}
