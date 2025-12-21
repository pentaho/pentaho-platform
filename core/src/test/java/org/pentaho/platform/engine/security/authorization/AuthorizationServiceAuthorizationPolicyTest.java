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

package org.pentaho.platform.engine.security.authorization;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationActionService;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationPrincipal;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationService;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createMockDecision;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

@SuppressWarnings( "removal" ) // For deprecated class usage
public class AuthorizationServiceAuthorizationPolicyTest {

  private IAuthorizationActionService mockActionService;
  private IAuthorizationService mockAuthorizationService;
  private Supplier<IAuthorizationPrincipal> mockCurrentPrincipalSupplier;
  private IAuthorizationUser mockUser;

  private IAuthorizationAction mockAction1;
  private IAuthorizationAction mockAction2;
  private IAuthorizationAction mockAction3;

  private IAuthorizationDecision mockGrantedDecision;
  private IAuthorizationDecision mockDeniedDecision;

  @SuppressWarnings( "unchecked" )
  @Before
  public void setUp() {
    mockActionService = mock( IAuthorizationActionService.class );
    mockAuthorizationService = mock( IAuthorizationService.class );

    mockCurrentPrincipalSupplier = (Supplier<IAuthorizationPrincipal>) mock( Supplier.class );

    mockUser = createTestUser();
    when( mockCurrentPrincipalSupplier.get() ).thenReturn( mockUser );

    // Create mock actions
    mockAction1 = createTestAction( "action1" );
    mockAction2 = createTestAction( "action2" );
    mockAction3 = createTestAction( "action3" );

    // Create mock decisions with a mock request instead of null
    var mockRequest = mock( IAuthorizationRequest.class );
    mockGrantedDecision = createMockDecision( mockRequest, true );
    mockDeniedDecision = createMockDecision( mockRequest, false );
  }

  // region Constructor Tests

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullActionServiceThrows() {
    //noinspection DataFlowIssue
    new AuthorizationServiceAuthorizationPolicy(
      null,
      mockAuthorizationService,
      mockCurrentPrincipalSupplier
    );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullAuthorizationServiceThrows() {
    //noinspection DataFlowIssue
    new AuthorizationServiceAuthorizationPolicy(
      mockActionService,
      null,
      mockCurrentPrincipalSupplier
    );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullCurrentPrincipalSupplierThrows() {
    //noinspection DataFlowIssue
    new AuthorizationServiceAuthorizationPolicy(
      mockActionService,
      mockAuthorizationService,
      null
    );
  }
  // endregion

  // region isAllowed(.) Tests

  @Test
  public void testIsAllowedWithUnknownActionReturnsFalse() {
    when( mockActionService.getAction( "unknown-action" ) ).thenReturn( Optional.empty() );

    var policy = new AuthorizationServiceAuthorizationPolicy(
      mockActionService,
      mockAuthorizationService,
      mockCurrentPrincipalSupplier
    );

    boolean result = policy.isAllowed( "unknown-action" );

    assertFalse( result );

    // Verify that authorization service was never called for unknown actions
    verify( mockAuthorizationService, never() ).authorize( any() );
  }

  @Test
  public void testIsAllowedWithKnownActionCallsAuthorizationService() {
    when( mockActionService.getAction( "action1" ) ).thenReturn( Optional.of( mockAction1 ) );
    when( mockAuthorizationService.authorize( any( AuthorizationRequest.class ) ) )
      .thenReturn( mockGrantedDecision );

    var policy = new AuthorizationServiceAuthorizationPolicy(
      mockActionService,
      mockAuthorizationService,
      mockCurrentPrincipalSupplier
    );

    policy.isAllowed( "action1" );

    verify( mockAuthorizationService, times( 1 ) ).authorize( any( AuthorizationRequest.class ) );
  }

  @Test
  public void testIsAllowedWithGrantedActionReturnsTrue() {
    when( mockActionService.getAction( "action1" ) ).thenReturn( Optional.of( mockAction1 ) );
    when( mockAuthorizationService.authorize( any( AuthorizationRequest.class ) ) )
      .thenReturn( mockGrantedDecision );

    var policy = new AuthorizationServiceAuthorizationPolicy(
      mockActionService,
      mockAuthorizationService,
      mockCurrentPrincipalSupplier
    );

    boolean result = policy.isAllowed( "action1" );

    assertTrue( result );
  }

  @Test
  public void testIsAllowedWithDeniedActionReturnsFalse() {
    when( mockActionService.getAction( "action1" ) ).thenReturn( Optional.of( mockAction1 ) );
    when( mockAuthorizationService.authorize( any( AuthorizationRequest.class ) ) )
      .thenReturn( mockDeniedDecision );

    var policy = new AuthorizationServiceAuthorizationPolicy(
      mockActionService,
      mockAuthorizationService,
      mockCurrentPrincipalSupplier
    );

    boolean result = policy.isAllowed( "action1" );

    assertFalse( result );
  }

  @Test
  public void testIsAllowedCreatesCorrectAuthorizationRequestWithCurrentPrincipal() {
    when( mockActionService.getAction( "action1" ) ).thenReturn( Optional.of( mockAction1 ) );
    when( mockAuthorizationService.authorize( any( AuthorizationRequest.class ) ) )
      .thenReturn( mockGrantedDecision );

    var policy = new AuthorizationServiceAuthorizationPolicy(
      mockActionService,
      mockAuthorizationService,
      mockCurrentPrincipalSupplier
    );

    policy.isAllowed( "action1" );

    var requestCaptor = org.mockito.ArgumentCaptor.forClass( AuthorizationRequest.class );
    verify( mockAuthorizationService ).authorize( requestCaptor.capture() );

    var capturedRequest = requestCaptor.getValue();
    assertSame( mockUser, capturedRequest.getPrincipal() );
    assertSame( mockAction1, capturedRequest.getAction() );

    verify( mockCurrentPrincipalSupplier, times( 1 ) ).get();
  }
  // endregion

  // region getAllowedActions Tests
  @Test
  public void testGetAllowedActionsWithEmptyNamespaceReturnsEmptyList() {
    when( mockActionService.getActions( "empty-namespace" ) ).thenReturn( Stream.empty() );

    var policy = new AuthorizationServiceAuthorizationPolicy(
      mockActionService,
      mockAuthorizationService,
      mockCurrentPrincipalSupplier
    );

    List<String> result = policy.getAllowedActions( "empty-namespace" );

    assertTrue( result.isEmpty() );
    verify( mockAuthorizationService, never() ).authorize( any() );
  }

  @Test
  public void testGetAllowedActionsFiltersBasedOnPermissions() {
    // Set up actions in namespace
    when( mockActionService.getActions( "test-namespace" ) )
      .thenReturn( Stream.of( mockAction1, mockAction2, mockAction3 ) );

    // Mock authorization decisions: action1=granted, action2=denied, action3=granted
    when( mockAuthorizationService.authorize( any( AuthorizationRequest.class ) ) )
      .thenAnswer( invocation -> {
        AuthorizationRequest request = invocation.getArgument( 0 );
        IAuthorizationAction action = request.getAction();

        if ( action == mockAction1 || action == mockAction3 ) {
          return mockGrantedDecision;
        }

        return mockDeniedDecision;
      } );

    var policy = new AuthorizationServiceAuthorizationPolicy(
      mockActionService,
      mockAuthorizationService,
      mockCurrentPrincipalSupplier
    );

    List<String> result = policy.getAllowedActions( "test-namespace" );

    // Should only contain action1 and action3 (the granted ones)
    assertEquals( 2, result.size() );
    assertTrue( result.contains( "action1" ) );
    assertTrue( result.contains( "action3" ) );
    assertFalse( result.contains( "action2" ) );

    // Verify authorization service was called for each action
    verify( mockAuthorizationService, times( 3 ) ).authorize( any( AuthorizationRequest.class ) );
  }

  @Test
  public void testGetAllowedActionsWithAllDeniedReturnsEmptyList() {
    when( mockActionService.getActions( "test-namespace" ) )
      .thenReturn( Stream.of( mockAction1, mockAction2 ) );

    when( mockAuthorizationService.authorize( any( AuthorizationRequest.class ) ) )
      .thenReturn( mockDeniedDecision );

    var policy = new AuthorizationServiceAuthorizationPolicy(
      mockActionService,
      mockAuthorizationService,
      mockCurrentPrincipalSupplier
    );

    List<String> result = policy.getAllowedActions( "test-namespace" );

    assertTrue( result.isEmpty() );
    verify( mockAuthorizationService, times( 2 ) ).authorize( any( AuthorizationRequest.class ) );
  }
  // endregion
}
