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

package org.pentaho.platform.web.servlet;

import java.io.Serializable;

public class GwtRpcProxyException extends RuntimeException implements Serializable {

  private static final long serialVersionUID = -5090524647540284482L;

  public GwtRpcProxyException() {
    // TODO Auto-generated constructor stub
  }

  public GwtRpcProxyException( String message ) {
    super( message );
    // TODO Auto-generated constructor stub
  }

  public GwtRpcProxyException( Throwable cause ) {
    super( cause );
    // TODO Auto-generated constructor stub
  }

  public GwtRpcProxyException( String message, Throwable cause ) {
    super( message, cause );
    // TODO Auto-generated constructor stub
  }

}
