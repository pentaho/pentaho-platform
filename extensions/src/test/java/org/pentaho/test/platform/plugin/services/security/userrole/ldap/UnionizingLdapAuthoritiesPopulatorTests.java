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


package org.pentaho.test.platform.plugin.services.security.userrole.ldap;

import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.plugin.services.security.userrole.ldap.UnionizingLdapAuthoritiesPopulator;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Tests the <code>UnionizingLdapAuthoritiesPopulator</code> class.
 * 
 * @author mlowery
 */
public class UnionizingLdapAuthoritiesPopulatorTests extends AbstractPentahoLdapIntegrationTests {

  // the ApacheDS LDAP server used to run this test seems to not work under JDK11, and the latest
  // versions do not appear to work within the spring framework
  @Ignore
  @Test
  public void testGetGrantedAuthorities() throws Exception {
    DefaultLdapAuthoritiesPopulator wrappedPop;
    wrappedPop = new DefaultLdapAuthoritiesPopulator( getContextSource(), "ou=roles" ); //$NON-NLS-1$
    wrappedPop.setRolePrefix( "ROLE_" ); //$NON-NLS-1$
    wrappedPop.setGroupSearchFilter( "(roleOccupant={0})" ); //$NON-NLS-1$

    DefaultLdapAuthoritiesPopulator wrappedPop2;
    wrappedPop2 = new DefaultLdapAuthoritiesPopulator( getContextSource(), "ou=groups" ); //$NON-NLS-1$
    wrappedPop2.setRolePrefix( "ROLE_" ); //$NON-NLS-1$
    wrappedPop2.setGroupSearchFilter( "(uniqueMember={0})" ); //$NON-NLS-1$

    Set populators = new HashSet();
    populators.add( wrappedPop );
    populators.add( wrappedPop2 );
    UnionizingLdapAuthoritiesPopulator unionizer = new UnionizingLdapAuthoritiesPopulator();
    unionizer.setPopulators( populators );

    unionizer.afterPropertiesSet();

    // get the user record
    DirContextOperations ctx = new SpringSecurityLdapTemplate( getContextSource() ).retrieveEntry( "uid=suzy,ou=users", //$NON-NLS-1$
        null );

    Collection<? extends GrantedAuthority> auths = unionizer.getGrantedAuthorities( ctx, "suzy" ); //$NON-NLS-1$

    assertTrue( null != auths && auths.size() > 0 );

    assertTrue( auths.contains( new SimpleGrantedAuthority( "ROLE_CTO" ) ) ); //$NON-NLS-1$
    assertTrue( auths.contains( new SimpleGrantedAuthority( "ROLE_AUTHENTICATED" ) ) ); //$NON-NLS-1$
    assertTrue( auths.contains( new SimpleGrantedAuthority( "ROLE_MARKETING" ) ) ); //$NON-NLS-1$
    assertTrue( auths.contains( new SimpleGrantedAuthority( "ROLE_IS" ) ) ); //$NON-NLS-1$
  }
}
