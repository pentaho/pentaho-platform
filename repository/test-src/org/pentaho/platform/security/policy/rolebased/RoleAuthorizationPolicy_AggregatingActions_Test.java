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

package org.pentaho.platform.security.policy.rolebased;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAggregatingAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class RoleAuthorizationPolicy_AggregatingActions_Test {

  private RoleAuthorizationPolicy policy;

  @Before
  public void setUp() {
    policy = mock( RoleAuthorizationPolicy.class );
    when( policy.buildActionsMapping( anyListOf( IAuthorizationAction.class ) ) ).thenCallRealMethod();
  }


  @Test
  public void noActions() {
    testAggregating(
      Collections.<IAuthorizationAction>emptyList(),
      ImmutableListMultimap.<IAggregatingAuthorizationAction, String>builder().build()
    );
  }

  @Test
  public void noAggregatingActions() {
    testAggregating(
      asList( new RepositoryCreateAction(), new RepositoryReadAction(), new AdministerSecurityAction() ),
      ImmutableListMultimap.<IAggregatingAuthorizationAction, String>builder().build()
    );
  }

  @Test
  public void oneLevelAggregation() {
    ListMultimap<IAggregatingAuthorizationAction, String> map = ArrayListMultimap.create( 1, 3 );
    map.putAll( new PublishAction(),
      asList( PublishAction.NAME, RepositoryCreateAction.NAME, RepositoryReadAction.NAME ) );

    testAggregating(
      asList( new RepositoryReadAction(), new RepositoryCreateAction(), new AdministerSecurityAction() ),
      map
    );
  }

  @Test
  public void twoLevelAggregation() {
    // 1 <- 2 <- 3
    IAggregatingAuthorizationAction action1 = createAggregatingAction( "action1", "action2" );
    IAggregatingAuthorizationAction action2 = createAggregatingAction( "action2", "action3" );
    IAggregatingAuthorizationAction action3 = createAggregatingAction( "action3" );

    ListMultimap<IAggregatingAuthorizationAction, String> map = ArrayListMultimap.create( 3, 3 );
    map.putAll( action1, asList( "action1", "action2", "action3" ) );
    map.putAll( action2, asList( "action2", "action3" ) );
    map.put( action3, "action3" );

    testAggregating( singletonList( new RepositoryReadAction() ), map );
  }

  private void testAggregating( List<? extends IAuthorizationAction> plainActions,
                                ListMultimap<IAggregatingAuthorizationAction, String> aggregatingActions ) {
    Map<IAggregatingAuthorizationAction, Collection<String>> aggregatingMap = aggregatingActions.asMap();

    final int expectedSize = plainActions.size() + aggregatingMap.size();
    List<IAuthorizationAction> all = new ArrayList<>( expectedSize );
    all.addAll( plainActions );
    all.addAll( aggregatingMap.keySet() );
    ListMultimap<String, String> multimap = policy.buildActionsMapping( all );

    assertEquals( expectedSize, multimap.keySet().size() );
    for ( IAuthorizationAction action : plainActions ) {
      assertActions( multimap.get( action.getName() ), singletonList( action.getName() ) );
    }
    for ( Map.Entry<IAggregatingAuthorizationAction, Collection<String>> entry : aggregatingMap.entrySet() ) {
      assertActions( multimap.get( entry.getKey().getName() ), entry.getValue() );
    }
  }


  @Test( expected = IllegalStateException.class )
  public void circularDependency() {
    IAggregatingAuthorizationAction action1 = createAggregatingAction( "action1", "action2" );
    IAggregatingAuthorizationAction action2 = createAggregatingAction( "action2", "action1" );
    policy.buildActionsMapping( asList( action1, action2, new RepositoryReadAction() ) );
  }


  private static IAggregatingAuthorizationAction createAggregatingAction( String name, String... implicitlyIncluded ) {
    IAggregatingAuthorizationAction action = mock( IAggregatingAuthorizationAction.class );
    when( action.getName() ).thenReturn( name );

    if ( implicitlyIncluded == null ) {
      implicitlyIncluded = new String[ 0 ];
    }
    when( action.getAggregatedActions() ).thenReturn( asList( implicitlyIncluded ) );
    return action;
  }


  private static void assertActions( List<String> actions, Collection<String> expected ) {
    assertEquals( expected.size(), actions.size() );

    Set<String> set = new HashSet<>( expected );
    for ( String action : actions ) {
      assertTrue( action, set.remove( action ) );
    }
  }
}
