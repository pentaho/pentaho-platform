/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.security.policy.rolebased.ws;

import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.security.policy.rolebased.ISessionAwareAuthorizationPolicy;

import javax.jcr.Session;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Andrey Khayrutdinov
 */
public class DefaultAuthorizationPolicyWebServiceTest {

  @Test
  public void isAllowed_CallsSessionAwareDelegate() {
    ISessionAwareAuthorizationPolicy policy = mock( ISessionAwareAuthorizationPolicy.class );
    DefaultAuthorizationPolicyWebService ws = new DefaultAuthorizationPolicyWebService( policy );

    final Session session = mock( Session.class );
    final String action = "action";
    ws.isAllowed( session, action );

    verify( policy ).isAllowed( eq( session ), eq( action ) );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void isAllowed_ThrowsException_WhenDelegateIsNotSessionAware() {
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    new DefaultAuthorizationPolicyWebService( policy )
      .isAllowed( mock( Session.class ), "action" );
  }
}
