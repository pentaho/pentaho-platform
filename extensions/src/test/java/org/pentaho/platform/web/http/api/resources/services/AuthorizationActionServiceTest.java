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
