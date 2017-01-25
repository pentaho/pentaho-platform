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
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/30/15.
 */
public class NoOpLdapAuthoritiesPopulatorTest {

  @Test
  public void testGetGrantedAuthorities() throws Exception {
    NoOpLdapAuthoritiesPopulator noop = new NoOpLdapAuthoritiesPopulator();
    Collection<? extends GrantedAuthority> grantedAuthorities = noop.getGrantedAuthorities( null, null );
    assertNotNull( grantedAuthorities );
    assertEquals( 0, grantedAuthorities.size() );
  }
}
