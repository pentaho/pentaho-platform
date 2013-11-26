/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.session;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;

public class HttpSessionParameterProvider extends SimpleParameterProvider {

  private IPentahoSession session;

  public HttpSessionParameterProvider( final IPentahoSession session ) {
    this.session = session;
  }

  @Override
  public Object getParameter( final String name ) {
    if ( "name".equals( name ) ) { //$NON-NLS-1$
      return session.getName();
    }
    return session.getAttribute( name );
  }

  @Override
  public String getStringParameter( final String name, final String defaultValue ) {
    Object value = getParameter( name );
    if ( value != null ) {
      return value.toString();
    }
    return defaultValue;
  }

}
