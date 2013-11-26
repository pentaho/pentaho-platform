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
import org.pentaho.platform.plugin.services.security.userrole.ldap.RolePreprocessingMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Tests for <code>RolePreprocessingMapper</code>. Essentially mimics the steps taken by
 * <code>LdapAuthenticationProvider</code>.
 * 
 * @author mlowery
 */
public class RolePreprocessingMapperTests extends AbstractPentahoLdapIntegrationTests {

  @SuppressWarnings( "unchecked" )
  @Test
  public void testMapUserFromContext() throws Exception {
    RolePreprocessingMapper mapper = new RolePreprocessingMapper();
    mapper.setTokenName( "cn" ); //$NON-NLS-1$
    mapper.setRolePrefix( "" ); //$NON-NLS-1$
    mapper.setRoleAttributes( new String[] { "uniqueMember" } ); //$NON-NLS-1$

    // get the user record
    DirContextOperations ctx = new SpringSecurityLdapTemplate( getContextSource() ).retrieveEntry( "uid=suzy,ou=users", //$NON-NLS-1$
        null );

    // get any roles that aren't in the user record
    Set<String> extraRoles =
        new SpringSecurityLdapTemplate( getContextSource() ).searchForSingleAttributeValues(
            "ou=roles", "roleoccupant={0}", new String[] { "uid=suzy,ou=users,dc=pentaho,dc=org", "suzy" }, "cn" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    GrantedAuthority[] authorities = new GrantedAuthority[extraRoles.size()];
    int i = 0;
    for ( String extraRole : extraRoles ) {
      authorities[i++] = new GrantedAuthorityImpl( extraRole );
    }

    // use the mapper to create a UserDetails instance
    UserDetails userDetails = mapper.mapUserFromContext( ctx, "suzy", authorities ); //$NON-NLS-1$
    System.out.println( userDetails );

    // this asserts the ordering too; not strictly necessary
    GrantedAuthority[] expected =
        new GrantedAuthority[] { new GrantedAuthorityImpl( "A" ), new GrantedAuthorityImpl( "cto" ), //$NON-NLS-1$ //$NON-NLS-2$
          new GrantedAuthorityImpl( "is" ), new GrantedAuthorityImpl( "Authenticated" ) }; //$NON-NLS-1$//$NON-NLS-2$
    assertTrue( Arrays.equals( expected, userDetails.getAuthorities() ) );
  }
}
