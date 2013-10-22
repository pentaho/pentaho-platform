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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SystemSettingsParameterProvider extends BaseParameterProvider {

  @Override
  public Object getParameter( String name ) {
    return getValue( name );
  }

  public static String getSystemSetting( String path ) {
    // parse out the path
    int pos1 = path.indexOf( '{' );
    int pos2 = path.indexOf( '}' );
    if ( pos1 > 0 && pos2 > 0 ) {
      String file = path.substring( 0, pos1 );
      String setting = path.substring( pos1 + 1, pos2 );
      String value = PentahoSystem.getSystemSetting( file, setting, null );
      if ( value != null ) {
        return value;
      }
    }
    return null;
  }

  @Override
  protected String getValue( String path ) {
    // parse out the path
    return getSystemSetting( path );
  }

  public Iterator getParameterNames() {
    // this will return hundreds of things so we're just going to return an empty list
    List<String> list = new ArrayList<String>();
    return list.iterator();
  }

}
