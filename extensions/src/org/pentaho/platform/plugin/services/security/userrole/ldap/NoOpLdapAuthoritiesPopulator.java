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

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.ldap.LdapAuthoritiesPopulator;

/**
 * For use when authorities are stored in the user object (e.g. <code>objectClass=Person</code>) and therefore retrieved
 * by an <code>UserDetailsContextMapper</code> instance. This class helps since <code>LdapUserDetailsService</code>
 * requires a non-null <code>LdapAuthoritiesPopulator</code> instance.
 * <p>
 * <code>LdapAuthenticationProvider</code> actually defines a <code>NullLdapAuthoritiesPopulator</code> which does the
 * same thing as this class. Unfortunately, <code>NullLdapAuthoritiesPopulator</code> is not visible outside of
 * <code>LdapAuthenticationProvider</code>.
 * </p>
 * 
 * @author mlowery
 */
public class NoOpLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

  public GrantedAuthority[] getGrantedAuthorities( DirContextOperations userDetails, String username ) {
    return new GrantedAuthority[0];
  }

}
