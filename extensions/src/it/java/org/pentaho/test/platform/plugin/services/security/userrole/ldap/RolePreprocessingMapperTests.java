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

import org.junit.Test;
import org.pentaho.platform.plugin.services.security.userrole.ldap.RolePreprocessingMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

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

    List<GrantedAuthority> authorities = new ArrayList<>();
    for ( String extraRole : extraRoles ) {
      authorities.add( new SimpleGrantedAuthority( extraRole ) );
    }

    // use the mapper to create a UserDetails instance
    UserDetails userDetails = mapper.mapUserFromContext( ctx, "suzy", authorities ); //$NON-NLS-1$
    System.out.println( userDetails );

    // this asserts the ordering too; not strictly necessary
    Collection<? extends GrantedAuthority> expectedAuthorities = new ArrayList<GrantedAuthority>() {{
        add( new SimpleGrantedAuthority( "A" ) );
        add( new SimpleGrantedAuthority( "Authenticated" ) );
        add( new SimpleGrantedAuthority( "is" ) );
        add( new SimpleGrantedAuthority( "cto" ) ); }};

    Collection<? extends GrantedAuthority> unexpectedAuthorities = userDetails.getAuthorities();

    assertEquals( expectedAuthorities, unexpectedAuthorities );
  }
}
