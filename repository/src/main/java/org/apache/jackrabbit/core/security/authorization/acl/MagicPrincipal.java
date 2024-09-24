/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.apache.jackrabbit.core.security.authorization.acl;

import org.apache.jackrabbit.core.security.principal.UnknownPrincipal;
import org.pentaho.platform.repository2.unified.jcr.IPentahoInternalPrincipal;

/**
 * {@code Principal} that is used in magic ACEs, ACEs that are added on-the-fly and never persisted.
 * 
 * <p>
 * Extends {@code UnknownPrincipal} so that Jackrabbit will not throw an exception if the principal does not exist.
 * </p>
 * 
 * @author mlowery
 */
public class MagicPrincipal extends UnknownPrincipal implements IPentahoInternalPrincipal {

  private static final long serialVersionUID = -4264281460133459881L;

  public MagicPrincipal( final String name ) {
    super( name );
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( getName() == null ) ? 0 : getName().hashCode() );
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    MagicPrincipal other = (MagicPrincipal) obj;
    if ( getName() == null ) {
      if ( other.getName() != null ) {
        return false;
      }
    } else if ( !getName().equals( other.getName() ) ) {
      return false;
    }
    return true;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "MagicPrincipal [name=" + getName() + "]";
  }

}
