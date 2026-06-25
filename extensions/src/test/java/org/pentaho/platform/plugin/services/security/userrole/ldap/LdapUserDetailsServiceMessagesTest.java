/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/30/15.
 */
public class LdapUserDetailsServiceMessagesTest {

  @Test
  public void testGetString() throws Exception {
    String s = LdapUserDetailsServiceMessages.getString( "any" );
    assertEquals( "!any!", s );
  }
}
