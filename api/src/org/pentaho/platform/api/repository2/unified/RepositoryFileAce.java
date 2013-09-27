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

package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;
import java.util.EnumSet;

/**
 * Immutable access control entry (ACE).
 * 
 * @author mlowery
 */
public class RepositoryFileAce implements Serializable {

  private static final long serialVersionUID = 6916656647947322578L;

  private final RepositoryFileSid recipient;

  private final EnumSet<RepositoryFilePermission> permissions;

  public RepositoryFileAce( final RepositoryFileSid recipient, final RepositoryFilePermission first,
      final RepositoryFilePermission... rest ) {
    this( recipient, EnumSet.of( first, rest ) );
  }

  public RepositoryFileAce( final RepositoryFileSid recipient, final EnumSet<RepositoryFilePermission> permissions ) {
    super();
    notNull( recipient );
    notNull( permissions );
    this.recipient = recipient;
    this.permissions = EnumSet.copyOf( permissions );
  }

  private void notNull( final Object obj ) {
    if ( obj == null ) {
      throw new IllegalArgumentException();
    }
  }

  public RepositoryFileSid getSid() {
    return recipient;
  }

  public EnumSet<RepositoryFilePermission> getPermissions() {
    return EnumSet.copyOf( permissions );
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( permissions == null ) ? 0 : permissions.hashCode() );
    result = prime * result + ( ( recipient == null ) ? 0 : recipient.hashCode() );
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
    RepositoryFileAce other = (RepositoryFileAce) obj;
    if ( permissions == null ) {
      if ( other.permissions != null ) {
        return false;
      }
    } else if ( !permissions.equals( other.permissions ) ) {
      return false;
    }
    if ( recipient == null ) {
      if ( other.recipient != null ) {
        return false;
      }
    } else if ( !recipient.equals( other.recipient ) ) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "RepositoryFileAce[recipient=" + recipient + ", permissions=" + permissions + "]"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
  }

}
