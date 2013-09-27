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

package org.pentaho.platform.engine.security;

import org.pentaho.platform.api.engine.IPermissionRecipient;

public class SimpleUser implements IPermissionRecipient {

  String userName;

  public SimpleUser( final String userName ) {
    this.userName = userName;
  }

  public String getName() {
    return userName;
  }

  @Override
  public boolean equals( final Object o ) {
    return ( ( o instanceof SimpleUser ) ? userName.equals( ( (SimpleUser) o ).getName() ) : false );
  }

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return userName.hashCode();
  }

  @Override
  public String toString() {
    return String.format( "SimpleUser[userName=%s]", userName ); //$NON-NLS-1$
  }
}
