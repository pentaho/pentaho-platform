/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.engine.core.system.osgi;

import java.util.Map;

/**
 * Created by nbaker on 4/27/15.
 */
public class OSGIUtils {
  public static String createFilter( Map<String, String> props ) {
    int numberOfProperties = props != null ? props.size() : 0;

    if ( numberOfProperties == 0 ) {
      return null;
    }

    StringBuilder sb = new StringBuilder();

    if ( numberOfProperties > 1 ) {
      sb.append( "(&" );
    }

    for ( Map.Entry<String, String> entry : props.entrySet() ) {
      sb.append( "(" ).append( entry.getKey() ).append( "=" ).append( entry.getValue() ).append( ")" );
    }

    if ( numberOfProperties > 1 ) {
      sb.append( ")" );
    }

    return sb.toString();
  }

}
