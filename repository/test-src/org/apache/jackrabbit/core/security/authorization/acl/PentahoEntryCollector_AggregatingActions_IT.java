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

package org.apache.jackrabbit.core.security.authorization.acl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.ISessionAwareAuthorizationPolicy;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class PentahoEntryCollector_AggregatingActions_IT {

  private MicroPlatform mp;
  private PentahoEntryCollector collector;

  @Before
  public void setUp() {
    mp = new MicroPlatform();

    collector = mock( PentahoEntryCollector.class );
    when( collector.getSessionAwarePolicy() ).thenCallRealMethod();
  }

  @After
  public void tearDown() {
    if ( mp.isInitialized() ) {
      mp.stop();
    }

    mp = null;
    collector = null;
  }

  @Test
  public void getSessionAwarePolicy_PicksExisting() throws Exception {
    ISessionAwareAuthorizationPolicy defined = mock( ISessionAwareAuthorizationPolicy.class );
    mp.defineInstance( "nonTransactedAuthorizationPolicy", defined );
    mp.start();

    ISessionAwareAuthorizationPolicy picked = collector.getSessionAwarePolicy();
    assertEquals( defined, picked );
  }

  @Test
  public void getSessionAwarePolicy_CreatesAndRegistersAbsent() throws Exception {
    IAuthorizationAction fakeAction = mock( IAuthorizationAction.class );
    when( fakeAction.getName() ).thenReturn( "fakeAction" );

    IRoleAuthorizationPolicyRoleBindingDao nonTransactedDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    when( nonTransactedDao.getBoundLogicalRoleNames( anyListOf( String.class ) ) )
      .thenReturn( singletonList( "fakeAction" ) );

    mp.defineInstance( IAuthorizationAction.class, fakeAction );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", nonTransactedDao );
    mp.start();

    ISessionAwareAuthorizationPolicy policy = collector.getSessionAwarePolicy();
    ISessionAwareAuthorizationPolicy registered = PentahoSystem.get( ISessionAwareAuthorizationPolicy.class,
      "nonTransactedAuthorizationPolicy", PentahoSessionHolder.getSession() );
    assertEquals( policy, registered );

    Authentication origAuth = SecurityContextHolder.getContext().getAuthentication();
    try {
      Authentication authentication = mock( Authentication.class );
      // empty array does not matter, fake DAO accepts any input
      when( authentication.getAuthorities() ).thenReturn( new GrantedAuthority[ 0 ] );
      SecurityContextHolder.getContext().setAuthentication( authentication );

      List<String> actions = policy.getAllowedActions( null );
      assertEquals( 1, actions.size() );
      assertEquals( fakeAction.getName(), actions.get( 0 ) );
    } finally {
      SecurityContextHolder.getContext().setAuthentication( origAuth );
    }
  }

  @Test( expected = IllegalStateException.class )
  public void getSessionAwarePolicy_FailsWhenNonTransactedDaoIsAbsent() {
    collector.getSessionAwarePolicy();
  }
}
