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

package org.pentaho.platform.plugin.services.security.userrole.ldap.transform;

import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Adds the roles from <code>extraRoles</code> to the roles input into this transformer. The roles are added as strings
 * so a subsequent transformer must convert them to <code>GrantedAuthority</code> instances.
 * 
 * <p>
 * Transformer input: <code>String</code> instance, <code>Collection</code> of <code>String</code> instances, or array
 * of <code>String</code> instances.
 * </p>
 * <p>
 * Transformer output: <code>Collection</code> of <code>String</code> instances, or array of <code>String</code>
 * instances.
 * </p>
 * 
 * @deprecated Use org.pentaho.platform.plugin.services.security.userrole.ExtraRolesUserRoleListServiceDecorator
 * @author mlowery
 */
@Deprecated
public class ExtraRoles implements Transformer, InitializingBean {

  // ~ Instance fields =======================================================

  private Set extraRoles;

  // ~ Methods ===============================================================

  public Object transform( final Object obj ) {
    Object transformed;
    Set authSet = new HashSet();
    if ( obj instanceof String ) {
      authSet.add( obj );
    } else if ( obj instanceof Collection ) {
      authSet.addAll( (Collection) obj );
    } else if ( obj instanceof Object[] ) {
      authSet.addAll( Arrays.asList( (Object[]) obj ) );
    }
    authSet.addAll( extraRoles );
    if ( obj instanceof Object[] ) {
      transformed = authSet.toArray();
    } else {
      transformed = authSet;
    }
    return transformed;
  }

  public Set getExtraRoles() {
    return extraRoles;
  }

  public void setExtraRoles( final Set extraRoles ) {
    this.extraRoles = extraRoles;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull( extraRoles );
  }
}
