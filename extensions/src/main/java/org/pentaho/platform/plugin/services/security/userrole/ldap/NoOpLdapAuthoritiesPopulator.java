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


package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.ArrayList;
import java.util.Collection;

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

  public Collection<? extends GrantedAuthority> getGrantedAuthorities( DirContextOperations userDetails, String username ) {
    return new ArrayList<GrantedAuthority>();
  }

}
