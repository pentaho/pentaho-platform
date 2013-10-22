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

package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import org.springframework.security.GrantedAuthority;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;

/**
 * In Spring Security, there are users and roles. This class represents a Spring Security role. This class is the
 * Jackrabbit representation of a {@code org.springframework.security.acls.sid.GrantedAuthoritySid}. This class was
 * required as no {@link Group} implementations were found that could re-used.
 * 
 * <p>
 * Why Group and not Principal? Group is more like a Spring Security role in that there can be "members" that have
 * that role assigned. On the client side, there is code that tests to see if the principal is a group and if so
 * creates a Spring Security role.
 * </p>
 * 
 * @author mlowery
 */
public class SpringSecurityRolePrincipal implements Group {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private String name;

  // ~ Constructors
  // ====================================================================================================

  public SpringSecurityRolePrincipal( final String name ) {
    super();
    this.name = name;
  }

  public SpringSecurityRolePrincipal( final GrantedAuthority authority ) {
    this( authority.getAuthority() );
  }

  // ~ Methods
  // =========================================================================================================

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
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
    SpringSecurityRolePrincipal other = (SpringSecurityRolePrincipal) obj;
    if ( name == null ) {
      if ( other.name != null ) {
        return false;
      }
    } else if ( !name.equals( other.name ) ) {
      return false;
    }
    return true;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "SpringSecurityRolePrincipal[name=" + name + "]";
  }

  public boolean addMember( final Principal user ) {
    throw new UnsupportedOperationException();
  }

  public boolean isMember( final Principal member ) {
    throw new UnsupportedOperationException();
  }

  public Enumeration<? extends Principal> members() {
    throw new UnsupportedOperationException();
  }

  public boolean removeMember( final Principal user ) {
    throw new UnsupportedOperationException();
  }

}
