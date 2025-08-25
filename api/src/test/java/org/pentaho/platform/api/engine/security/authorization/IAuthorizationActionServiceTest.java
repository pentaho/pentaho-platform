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
import static org.junit.jupiter.api.Assertions.assertSame;
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
  private IAuthorizationAction namespacedAction1;
  private IAuthorizationAction namespacedAction2;
  private IAuthorizationAction nestedNamespacedAction;

  @BeforeEach
  public void setUp() {
    selfAction1 = mock( IAuthorizationAction.class );
    when( selfAction1.getName() ).thenReturn( "selfAction1" );
    when( selfAction1.isSelfAction() ).thenReturn( true );
    when( selfAction1.isResourceAction() ).thenReturn( false );

    selfAction2 = mock( IAuthorizationAction.class );
    when( selfAction2.getName() ).thenReturn( "selfAction2" );
    when( selfAction2.isSelfAction() ).thenReturn( true );
    when( selfAction2.isResourceAction() ).thenReturn( false );

    resourceAction1 = mock( IAuthorizationAction.class );
    when( resourceAction1.getName() ).thenReturn( "resourceAction1" );
    when( resourceAction1.isSelfAction() ).thenReturn( false );
    when( resourceAction1.isResourceAction() ).thenReturn( true );
    when( resourceAction1.performsOnResourceType( "typeA" ) ).thenReturn( true );
    when( resourceAction1.performsOnResourceType( "typeB" ) ).thenReturn( true );

    resourceAction2 = mock( IAuthorizationAction.class );
    when( resourceAction2.getName() ).thenReturn( "resourceAction2" );
    when( resourceAction2.isSelfAction() ).thenReturn( false );
    when( resourceAction2.isResourceAction() ).thenReturn( true );
    when( resourceAction2.performsOnResourceType( "typeB" ) ).thenReturn( true );

    // Actions with namespaces for testing getActions(String namespace)
    namespacedAction1 = mock( IAuthorizationAction.class );
    when( namespacedAction1.getName() ).thenReturn( "com.pentaho.read" );
    when( namespacedAction1.isSelfAction() ).thenReturn( false );
    when( namespacedAction1.isResourceAction() ).thenReturn( false );

    namespacedAction2 = mock( IAuthorizationAction.class );
    when( namespacedAction2.getName() ).thenReturn( "com.pentaho.write" );
    when( namespacedAction2.isSelfAction() ).thenReturn( false );
    when( namespacedAction2.isResourceAction() ).thenReturn( false );

    nestedNamespacedAction = mock( IAuthorizationAction.class );
    when( nestedNamespacedAction.getName() ).thenReturn( "com.pentaho.admin.manage" );
    when( nestedNamespacedAction.isSelfAction() ).thenReturn( false );
    when( nestedNamespacedAction.isResourceAction() ).thenReturn( false );

    List<IAuthorizationAction> actions = Arrays.asList(
      selfAction1, selfAction2, resourceAction1, resourceAction2,
      namespacedAction1, namespacedAction2, nestedNamespacedAction
    );

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
    assertThrows( IllegalArgumentException.class, () -> service.getResourceActions( null ) );
  }

  @Test
  void testGetResourceActionsWithEmptyTypeThrows() {
    assertThrows( IllegalArgumentException.class, () -> service.getResourceActions( "" ) );
  }

  // region getActions(String namespace) Tests

  @Test
  void testGetActionsWithNullNamespace() {
    var result = service.getActions( null )
      .collect( Collectors.toList() );
    assertEquals( 7, result.size() ); // All actions should be returned
  }

  @Test
  void testGetActionsWithEmptyNamespace() {
    var result = service.getActions( "" )
      .collect( Collectors.toList() );
    assertEquals( 7, result.size() ); // All actions should be returned
  }

  @Test
  void testGetActionsWithNamespacePrefix() {
    var result = service.getActions( "com.pentaho" )
      .collect( Collectors.toList() );
    assertEquals( 3, result.size() );
    assertTrue( result.contains( namespacedAction1 ) );
    assertTrue( result.contains( namespacedAction2 ) );
    assertTrue( result.contains( nestedNamespacedAction ) );
  }

  @Test
  void testGetActionsWithNamespacePrefixEndingWithDot() {
    var result = service.getActions( "com.pentaho." )
      .collect( Collectors.toList() );
    assertEquals( 3, result.size() );
    assertTrue( result.contains( namespacedAction1 ) );
    assertTrue( result.contains( namespacedAction2 ) );
    assertTrue( result.contains( nestedNamespacedAction ) );
  }

  @Test
  void testGetActionsWithNestedNamespace() {
    var result = service.getActions( "com.pentaho.admin" )
      .collect( Collectors.toList() );
    assertEquals( 1, result.size() );
    assertSame( nestedNamespacedAction, result.get( 0 ) );
  }

  @Test
  void testGetActionsWithNonMatchingNamespace() {
    var result = service.getActions( "com.example" )
      .collect( Collectors.toList() );
    assertEquals( 0, result.size() );
  }

  // endregion

  // region getAction(String actionName) Tests

  @Test
  void testGetActionWithValidName() {
    var result = service.getAction( "selfAction1" );
    assertTrue( result.isPresent() );
    assertSame( selfAction1, result.get() );
  }

  @Test
  void testGetActionWithNonExistingName() {
    var result = service.getAction( "nonExisting" );
    assertTrue( result.isEmpty() );
  }

  @SuppressWarnings( "DataFlowIssue" )
  @Test
  void testGetActionWithNullNameThrows() {
    assertThrows( IllegalArgumentException.class, () ->
      service.getAction( null ) );
  }

  @Test
  void testGetActionWithEmptyNameThrows() {
    assertThrows( IllegalArgumentException.class, () ->
      service.getAction( "" ) );
  }

  // endregion
}
