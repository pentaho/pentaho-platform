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
 * Copyright 2015-2017 Hitachi Vantara. All rights reserved.
 */
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
