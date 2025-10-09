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


package org.pentaho.platform.web.http.api.resources.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;

public class AuthorizationActionServiceTest {

  private AuthorizationActionService authorizationActionService;

  @Before
  public void setUp() {
    authorizationActionService = spy( new AuthorizationActionService( null ) );
  }

  @After
  public void cleanup() {
    authorizationActionService = null;
  }

  @Test
  public void testDoValidateAuth() {

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    List<IAuthorizationAction> actions = new ArrayList();
    actions.add( new AdministerSecurityAction() );

    doReturn( actions ).when( authorizationActionService ).getActionList();
    doReturn( policy ).when( authorizationActionService ).getPolicy();
    doReturn( true ).when( policy ).isAllowed( "org.pentaho.security.administerSecurity" );
    boolean isAllowed = authorizationActionService.validateAuth( "org.pentaho.security.administerSecurity" );
    assertEquals( isAllowed, true );

    isAllowed = authorizationActionService.validateAuth( "invalid-auth" );
    assertEquals( isAllowed, false );
  }
}
