/*!
 * Copyright 2010 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jackrabbit.core.security.authorization.acl;

import org.apache.jackrabbit.core.security.principal.UnknownPrincipal;
import org.pentaho.platform.repository2.unified.jcr.IPentahoInternalPrincipal;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;

/**
 * {@code Group} that is used in magic ACEs, ACEs that are added on-the-fly and never persisted.
 * 
 * <p>
 * Extends {@code UnknownPrincipal} so that Jackrabbit will not throw an exception if the principal does not exist.
 * </p>
 * 
 * @author mlowery
 */
public class MagicGroup extends UnknownPrincipal implements Group, IPentahoInternalPrincipal {

  private static final long serialVersionUID = 2395449661136335711L;

  public MagicGroup( final String name ) {
    super( name );
  }

  @Override
  public boolean addMember( Principal arg0 ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isMember( Principal arg0 ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Enumeration<? extends Principal> members() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeMember( Principal arg0 ) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "MagicGroup [name=" + getName() + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ( ( getName() == null ) ? 0 : getName().hashCode() );
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( !super.equals( obj ) ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    MagicGroup other = (MagicGroup) obj;
    if ( getName() == null ) {
      if ( other.getName() != null ) {
        return false;
      }
    } else if ( !getName().equals( other.getName() ) ) {
      return false;
    }
    return true;
  }

}
