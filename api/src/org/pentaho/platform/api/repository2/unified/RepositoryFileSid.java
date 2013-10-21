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

/**
 * Repository file security identifier (SID) used as file owners and permission recipients in access control
 * entries.
 * 
 * <p>
 * Same abstraction as {@code org.springframework.security.acls.sid.Sid}.
 * </p>
 */
public class RepositoryFileSid implements Serializable {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final long serialVersionUID = 6081475460363558001L;

  // ~ Instance fields
  // =================================================================================================

  private final String name;

  private final RepositoryFileSid.Type type;

  // ~ Constructors
  // ====================================================================================================

  /**
   * Creates a new Sid with given name and type of {@link Type#USER}.
   */
  public RepositoryFileSid( final String name ) {
    this( name, Type.USER );
  }

  public RepositoryFileSid( final String name, final RepositoryFileSid.Type type ) {
    super();
    notNull( name );
    notNull( type );
    this.name = name;
    this.type = type;
  }

  // ~ Methods
  // =========================================================================================================

  private void notNull( final Object obj ) {
    if ( obj == null ) {
      throw new IllegalArgumentException();
    }
  }

  public String getName() {
    return name;
  }

  public RepositoryFileSid.Type getType() {
    return type;
  }

  public static enum Type {
    USER, ROLE;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    result = prime * result + type.hashCode();
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
    RepositoryFileSid other = (RepositoryFileSid) obj;
    if ( name == null ) {
      if ( other.name != null ) {
        return false;
      }
    } else if ( !name.equals( other.name ) ) {
      return false;
    }
    if ( type == null ) {
      if ( other.type != null ) {
        return false;
      }
    } else if ( !type.equals( other.type ) ) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "RepositoryFileSid[name=" + name + ", type=" + type + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

}
