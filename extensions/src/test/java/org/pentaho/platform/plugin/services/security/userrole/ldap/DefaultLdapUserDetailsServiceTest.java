/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.search.LdapUserSearch;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 10/30/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class DefaultLdapUserDetailsServiceTest {

  @Mock LdapUserSearch userSearch;
  @Mock LdapAuthoritiesPopulator authPop;
  @Mock ITenantedPrincipleNameResolver usernameUtils;

  @Test
  public void testLoadUserByUsername() throws Exception {
    DefaultLdapUserDetailsService service = spy( new DefaultLdapUserDetailsService( userSearch, authPop, usernameUtils ) );
    when( usernameUtils.getPrincipleName( anyString() ) ).thenReturn( "admin" );
    try {
      service.loadUserByUsername( "JOE" );
    } catch ( NullPointerException npe ) {
      // expected since we aren't configuring everything required by the super class
      // but we can verify the right stuff got called
      verify( usernameUtils ).getPrincipleName( "JOE" );
    }
  }
}
