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

public class PentahoSessionParameterProvider extends SimpleParameterProvider {

  private IPentahoSession session;

  public PentahoSessionParameterProvider( final IPentahoSession session ) {
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
