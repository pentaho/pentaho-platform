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

import org.pentaho.platform.api.engine.IPentahoSession;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomSettingsParameterProvider extends BaseParameterProvider {

  private IPentahoSession session;

  public void setSession( IPentahoSession session ) {
    this.session = session;
  }

  @Override
  public Object getParameter( String name ) {
    return getValue( name );
  }

  @Override
  protected String getValue( String path ) {
    // apply templates to the part
    if ( session != null ) {
      path = path.replace( "{$user}", session.getName() ); //$NON-NLS-1$
    }
    return SystemSettingsParameterProvider.getSystemSetting( "components/" + path ); //$NON-NLS-1$
  }

  public Iterator getParameterNames() {
    // this will return hundreds of things so we're just going to return an empty list
    List<String> list = new ArrayList<String>();
    return list.iterator();
  }

}
