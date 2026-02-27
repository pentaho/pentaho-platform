/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Enumeration;

/**
 * In Spring Security, there are users and roles. This class represents a Spring Security role. This class is the
 * Jackrabbit representation of a {@code org.springframework.security.acls.sid.GrantedAuthoritySid}. This class was
 * required as no Group implementations were found that could re-used.
 * 
 * <p>
 * Why Group and not Principal? Group is more like a Spring Security role in that there can be "members" that have
 * that role assigned. On the client side, there is code that tests to see if the principal is a group and if so
 * creates a Spring Security role.
 * </p>
 * 
 * @author mlowery
 */
public class SpringSecurityRolePrincipal implements Principal {

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
