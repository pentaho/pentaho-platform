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

package org.pentaho.test.platform.plugin.services.security.userrole.ldap;

import org.junit.Test;
import org.pentaho.platform.plugin.services.security.userrole.ldap.UnionizingLdapAuthoritiesPopulator;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.populator.DefaultLdapAuthoritiesPopulator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Tests the <code>UnionizingLdapAuthoritiesPopulator</code> class.
 * 
 * @author mlowery
 */
public class UnionizingLdapAuthoritiesPopulatorTests extends AbstractPentahoLdapIntegrationTests {

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

    GrantedAuthority[] auths = unionizer.getGrantedAuthorities( ctx, "suzy" ); //$NON-NLS-1$

    assertTrue( null != auths && auths.length > 0 );

    List authsList = Arrays.asList( auths );
    assertTrue( authsList.contains( new GrantedAuthorityImpl( "ROLE_POWER_USER" ) ) ); //$NON-NLS-1$
    assertTrue( authsList.contains( new GrantedAuthorityImpl( "ROLE_MARKETING" ) ) ); //$NON-NLS-1$

    System.out.println( authsList );
  }

}
