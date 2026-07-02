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
