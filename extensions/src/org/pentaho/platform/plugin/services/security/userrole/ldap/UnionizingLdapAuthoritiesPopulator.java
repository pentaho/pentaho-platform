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

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.pentaho.platform.plugin.services.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.ldap.LdapAuthoritiesPopulator;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Delegates to populators and unions the results. Use in conjunction with {@code UnionizingLdapSearch}.
 * 
 * @author mlowery
 */
public class UnionizingLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator, InitializingBean {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private Set<LdapAuthoritiesPopulator> populators;

  // ~ Constructors ====================================================================================================

  public UnionizingLdapAuthoritiesPopulator() {
    super();
  }

  // ~ Methods =========================================================================================================

  public GrantedAuthority[] getGrantedAuthorities( final DirContextOperations userData, final String username ) {
    Set<GrantedAuthority> allAuthorities = new HashSet<GrantedAuthority>();
    for ( LdapAuthoritiesPopulator populator : populators ) {
      GrantedAuthority[] auths = populator.getGrantedAuthorities( userData, username );
      if ( ( null != auths ) && ( auths.length > 0 ) ) {
        allAuthorities.addAll( Arrays.asList( auths ) );
      }

    }
    return allAuthorities.toArray( new GrantedAuthority[0] );
  }

  public void setPopulators( final Set<LdapAuthoritiesPopulator> populators ) {
    this.populators = populators;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull( populators, Messages.getInstance().getString(
      "UnionizingLdapAuthoritiesPopulator.ERROR_0001_POPULATOR_NULL" ) ); //$NON-NLS-1$
  }

}
