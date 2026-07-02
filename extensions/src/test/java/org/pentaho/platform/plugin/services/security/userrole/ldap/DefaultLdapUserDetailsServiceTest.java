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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.search.LdapUserSearch;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/30/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class DefaultLdapUserDetailsServiceTest {

  @Mock LdapUserSearch userSearch;
  @Mock LdapAuthoritiesPopulator authPop;
  @Mock ITenantedPrincipleNameResolver usernameUtils;

  @Test
  public void testLoadUserByUsername() {
    DefaultLdapUserDetailsService service = spy( new DefaultLdapUserDetailsService( userSearch, authPop, usernameUtils ) );
    when( usernameUtils.getPrincipleName( nullable( String.class ) ) ).thenReturn( "admin" );
    try {
      service.loadUserByUsername( "JOE" );
    } catch ( NullPointerException npe ) {
      // expected since we aren't configuring everything required by the super class
      // but we can verify the right stuff got called
      verify( usernameUtils ).getPrincipleName( "JOE" );
    }
  }
}
