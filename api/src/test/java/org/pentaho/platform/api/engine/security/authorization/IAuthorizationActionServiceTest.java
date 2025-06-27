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

package org.pentaho.platform.api.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pentaho.platform.api.engine.IAuthorizationAction;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IAuthorizationActionServiceTest {

  private IAuthorizationActionService service;
  private IAuthorizationAction selfAction1;
  private IAuthorizationAction resourceAction1;
  private IAuthorizationAction selfAction2;
  private IAuthorizationAction resourceAction2;

  @BeforeEach
  public void setUp() {
    selfAction1 = mock( IAuthorizationAction.class );
    when( selfAction1.isSelfAction() ).thenReturn( true );
    when( selfAction1.isResourceAction() ).thenReturn( false );

    selfAction2 = mock( IAuthorizationAction.class );
    when( selfAction2.isSelfAction() ).thenReturn( true );
    when( selfAction2.isResourceAction() ).thenReturn( false );

    resourceAction1 = mock( IAuthorizationAction.class );
    when( resourceAction1.isSelfAction() ).thenReturn( false );
    when( resourceAction1.isResourceAction() ).thenReturn( true );
    when( resourceAction1.performsOnResourceType( "typeA" ) ).thenReturn( true );
    when( resourceAction1.performsOnResourceType( "typeB" ) ).thenReturn( true );

    resourceAction2 = mock( IAuthorizationAction.class );
    when( resourceAction2.isSelfAction() ).thenReturn( false );
    when( resourceAction2.isResourceAction() ).thenReturn( true );
    when( resourceAction2.performsOnResourceType( "typeB" ) ).thenReturn( true );

    List<IAuthorizationAction> actions = Arrays.asList( selfAction1, selfAction2, resourceAction1, resourceAction2 );

    service = new IAuthorizationActionService() {
      @Override
      @NonNull
      public Stream<IAuthorizationAction> getActions() {
        return actions.stream();
      }

      @Override
      @NonNull
      public Stream<IAuthorizationAction> getSystemActions() {
        return Stream.empty();
      }

      @Override
      @NonNull
      public Stream<IAuthorizationAction> getPluginActions() {
        return Stream.empty();
      }

      @Override
      @NonNull
      public Stream<IAuthorizationAction> getPluginActions( @NonNull String pluginId ) {
        return Stream.empty();
      }
    };
  }

  @Test
  void testGetSelfActions() {
    List<IAuthorizationAction> result = service.getSelfActions().collect( Collectors.toList() );
    assertEquals( 2, result.size() );
    assertEquals( selfAction1, result.get( 0 ) );
    assertEquals( selfAction2, result.get( 1 ) );
  }

  @Test
  void testGetResourceActions() {
    List<IAuthorizationAction> result = service.getResourceActions().collect( Collectors.toList() );
    assertEquals( 2, result.size() );
    assertEquals( resourceAction1, result.get( 0 ) );
    assertEquals( resourceAction2, result.get( 1 ) );
  }

  @Test
  void testGetResourceActionsWithType() {
    List<IAuthorizationAction> result = service.getResourceActions( "typeA" ).collect( Collectors.toList() );
    assertEquals( 1, result.size() );
    assertTrue( result.contains( resourceAction1 ) );

    result = service.getResourceActions( "typeB" ).collect( Collectors.toList() );
    assertEquals( 2, result.size() );
    assertEquals( resourceAction1, result.get( 0 ) );
    assertEquals( resourceAction2, result.get( 1 ) );

    result = service.getResourceActions( "typeC" ).collect( Collectors.toList() );
    assertEquals( 0, result.size() );
  }

  @SuppressWarnings( "DataFlowIssue" )
  @Test
  void testGetResourceActionsWithNullTypeThrows() {
    assertThrows( IllegalArgumentException.class, () -> {
      service.getResourceActions( null );
    } );
  }

  @Test
  void testGetResourceActionsWithEmptyTypeThrows() {
    assertThrows( IllegalArgumentException.class, () -> {
      service.getResourceActions( "" );
    } );
  }
}
