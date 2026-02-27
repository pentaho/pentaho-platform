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

package org.pentaho.platform.engine.security.event;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link PentahoAuthenticationSuccessListener}
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoAuthenticationSuccessListenerTest {

  private PentahoAuthenticationSuccessListener listener;

  @Mock
  private IPentahoSession mockSession;

  @Mock
  private Authentication mockAuthentication;

  @Before
  public void setUp() {
    listener = new PentahoAuthenticationSuccessListener();
  }

  @Test
  public void testConstructor() {
    // Given & When
    PentahoAuthenticationSuccessListener newListener = new PentahoAuthenticationSuccessListener();

    // Then
    assertEquals( 100, newListener.getOrder() ); // Default order should be 100
  }

  @Test
  public void testGetOrder() {
    // Given & When
    int order = listener.getOrder();

    // Then
    assertEquals( 100, order );
  }

  @Test
  public void testSetOrder() {
    // Given
    int newOrder = 200;

    // When
    listener.setOrder( newOrder );

    // Then
    assertEquals( newOrder, listener.getOrder() );
  }

  @Test
  public void testOnApplicationEvent_WithAuthenticationSuccessEvent_Success() {
    // Given
    String username = "testuser";
    String sessionId = "session123";
    Collection authorities = Arrays.asList(
      new SimpleGrantedAuthority( "ROLE_USER" ),
      new SimpleGrantedAuthority( "ROLE_ADMIN" )
    );

    when( mockAuthentication.getName() ).thenReturn( username );
    when( mockAuthentication.getAuthorities() ).thenReturn( authorities );
    when( mockSession.getId() ).thenReturn( sessionId );
    when( mockSession.getName() ).thenReturn( username );
    when( mockSession.getActionName() ).thenReturn( "login" );
    when( mockSession.getObjectName() ).thenReturn( "testObject" );

    AuthenticationSuccessEvent event = new AuthenticationSuccessEvent( mockAuthentication );

    // When & Then
    try ( MockedStatic<PentahoSessionHolder> sessionHolderMock = mockStatic( PentahoSessionHolder.class );
          MockedStatic<AuditHelper> auditHelperMock = mockStatic( AuditHelper.class ) ) {

      sessionHolderMock.when( PentahoSessionHolder::getSession ).thenReturn( mockSession );

      listener.onApplicationEvent( event );

      // Verify session was updated
      verify( mockSession ).setAuthenticated( username );
      verify( mockSession ).setAttribute( IPentahoSession.SESSION_ROLES, authorities );

      // Verify audit was called
      auditHelperMock.verify( () -> AuditHelper.audit(
        eq( sessionId ),
        eq( username ),
        eq( "login" ),
        eq( "testObject" ),
        eq( "" ),
        eq( MessageTypes.SESSION_START ),
        eq( "" ),
        eq( "" ),
        eq( 0.0f ),
        isNull()
      ) );
    }
  }

  @Test
  public void testOnApplicationEvent_WithNonAuthenticationSuccessEvent_NoProcessing() {
    // Given
    ApplicationEvent nonSuccessEvent =
      new AuthenticationFailureLockedEvent( mockAuthentication, new BadCredentialsException( "test" ) );

    // When
    listener.onApplicationEvent( nonSuccessEvent );

    // Then
    verifyNoInteractions( mockSession );
    verifyNoInteractions( mockAuthentication );
  }

  @Test
  public void testOnApplicationEvent_WithNullSession_HandlesException() {
    // Given
    AuthenticationSuccessEvent event = new AuthenticationSuccessEvent( mockAuthentication );

    // When & Then
    try ( MockedStatic<PentahoSessionHolder> sessionHolderMock = mockStatic( PentahoSessionHolder.class ) ) {
      sessionHolderMock.when( PentahoSessionHolder::getSession ).thenReturn( null );

      // Should not throw exception, should be caught and logged
      listener.onApplicationEvent( event );

      verifyNoInteractions( mockSession );
    }
  }

  @Test
  public void testOnApplicationEvent_WithExceptionInSetUserDetails_HandlesException() {
    // Given
    when( mockAuthentication.getName() ).thenReturn( "testuser" );
    doThrow( new RuntimeException( "Test exception" ) ).when( mockAuthentication ).getAuthorities();

    AuthenticationSuccessEvent event = new AuthenticationSuccessEvent( mockAuthentication );

    // When & Then
    try ( MockedStatic<PentahoSessionHolder> sessionHolderMock = mockStatic( PentahoSessionHolder.class );
          MockedStatic<AuditHelper> auditHelperMock = mockStatic( AuditHelper.class ) ) {

      sessionHolderMock.when( PentahoSessionHolder::getSession ).thenReturn( mockSession );

      // Should not throw exception, should be caught and logged
      listener.onApplicationEvent( event );

      // Verify session.setAuthenticated was called (before exception)
      verify( mockSession ).setAuthenticated( "testuser" );

      // Verify audit was not called due to exception
      auditHelperMock.verify( () -> AuditHelper.audit(
        anyString(), anyString(), anyString(), anyString(), anyString(),
        any(), anyString(), anyString(), anyFloat(), any()
      ), never() );
    }
  }

  @Test
  public void testSetUserDetailsInPentahoSession_Success() {
    // Given
    String username = "testuser";
    Collection authorities = Collections.singletonList( new SimpleGrantedAuthority( "ROLE_USER" ) );

    when( mockAuthentication.getName() ).thenReturn( username );
    when( mockAuthentication.getAuthorities() ).thenReturn( authorities );

    // When
    listener.setUserDetailsInPentahoSession( mockSession, mockAuthentication );

    // Then
    verify( mockSession ).setAuthenticated( username );
    verify( mockSession ).setAttribute( IPentahoSession.SESSION_ROLES, authorities );
  }

  @Test
  public void testSetUserDetailsInPentahoSession_WithEmptyAuthorities() {
    // Given
    String username = "testuser";
    Collection authorities = Collections.emptyList();

    when( mockAuthentication.getName() ).thenReturn( username );
    when( mockAuthentication.getAuthorities() ).thenReturn( authorities );

    // When
    listener.setUserDetailsInPentahoSession( mockSession, mockAuthentication );

    // Then
    verify( mockSession ).setAuthenticated( username );
    verify( mockSession ).setAttribute( IPentahoSession.SESSION_ROLES, authorities );
  }

  @Test
  public void testSetUserDetailsInPentahoSession_WithNullAuthorities() {
    // Given
    String username = "testuser";

    when( mockAuthentication.getName() ).thenReturn( username );
    when( mockAuthentication.getAuthorities() ).thenReturn( null );

    // When
    listener.setUserDetailsInPentahoSession( mockSession, mockAuthentication );

    // Then
    verify( mockSession ).setAuthenticated( username );
    verify( mockSession ).setAttribute( IPentahoSession.SESSION_ROLES, null );
  }

  @Test
  public void testOnApplicationEvent_WithRealAuthentication() {
    // Given
    String username = "realuser";
    String password = "password";
    Collection authorities = Arrays.asList(
      new SimpleGrantedAuthority( "ROLE_USER" ),
      new SimpleGrantedAuthority( "ROLE_MANAGER" )
    );

    @SuppressWarnings( "unchecked" )
    Authentication realAuth = new UsernamePasswordAuthenticationToken( username, password, authorities );
    AuthenticationSuccessEvent event = new AuthenticationSuccessEvent( realAuth );

    when( mockSession.getId() ).thenReturn( "real-session-123" );
    when( mockSession.getName() ).thenReturn( username );
    when( mockSession.getActionName() ).thenReturn( "realAction" );
    when( mockSession.getObjectName() ).thenReturn( "realObject" );

    // When & Then
    try ( MockedStatic<PentahoSessionHolder> sessionHolderMock = mockStatic( PentahoSessionHolder.class );
          MockedStatic<AuditHelper> auditHelperMock = mockStatic( AuditHelper.class ) ) {

      sessionHolderMock.when( PentahoSessionHolder::getSession ).thenReturn( mockSession );

      listener.onApplicationEvent( event );

      // Verify session was updated with real authentication
      verify( mockSession ).setAuthenticated( username );
      verify( mockSession ).setAttribute( IPentahoSession.SESSION_ROLES, authorities );

      // Verify audit was called
      auditHelperMock.verify( () -> AuditHelper.audit(
        eq( "real-session-123" ),
        eq( username ),
        eq( "realAction" ),
        eq( "realObject" ),
        eq( "" ),
        eq( MessageTypes.SESSION_START ),
        eq( "" ),
        eq( "" ),
        eq( 0.0f ),
        isNull()
      ) );
    }
  }

  @Test
  public void testOnApplicationEvent_WithMultipleEvents() {
    // Given
    String user1 = "user1";
    String user2 = "user2";

    Authentication auth1 = mock( Authentication.class );
    Authentication auth2 = mock( Authentication.class );

    when( auth1.getName() ).thenReturn( user1 );
    Collection auth1Authorities = Collections.singletonList( new SimpleGrantedAuthority( "ROLE_USER" ) );
    when( auth1.getAuthorities() ).thenReturn( auth1Authorities );
    when( auth2.getName() ).thenReturn( user2 );
    Collection auth2Authorities = Collections.singletonList( new SimpleGrantedAuthority( "ROLE_ADMIN" ) );
    when( auth2.getAuthorities() ).thenReturn( auth2Authorities );

    AuthenticationSuccessEvent event1 = new AuthenticationSuccessEvent( auth1 );
    AuthenticationSuccessEvent event2 = new AuthenticationSuccessEvent( auth2 );

    when( mockSession.getId() ).thenReturn( "session-multi" );
    when( mockSession.getName() ).thenReturn( user1 ).thenReturn( user2 );
    when( mockSession.getActionName() ).thenReturn( "action" );
    when( mockSession.getObjectName() ).thenReturn( "object" );

    // When & Then
    try ( MockedStatic<PentahoSessionHolder> sessionHolderMock = mockStatic( PentahoSessionHolder.class );
          MockedStatic<AuditHelper> auditHelperMock = mockStatic( AuditHelper.class ) ) {

      sessionHolderMock.when( PentahoSessionHolder::getSession ).thenReturn( mockSession );

      // Process first event
      listener.onApplicationEvent( event1 );

      // Process second event
      listener.onApplicationEvent( event2 );

      // Verify both sessions were processed
      verify( mockSession ).setAuthenticated( user1 );
      verify( mockSession ).setAuthenticated( user2 );

      // Verify audit was called twice
      auditHelperMock.verify( () -> AuditHelper.audit(
        anyString(), anyString(), anyString(), anyString(), anyString(),
        eq( MessageTypes.SESSION_START ), anyString(), anyString(), anyFloat(), isNull()
      ), org.mockito.Mockito.times( 2 ) );
    }
  }

  @Test
  public void testOnApplicationEvent_OrderedInterface() {
    // Test that the listener implements Ordered interface correctly

    // Given
    int customOrder = 50;
    listener.setOrder( customOrder );

    // When & Then
    assertEquals( customOrder, listener.getOrder() );

    // Verify it's actually an Ordered instance
    assert listener instanceof org.springframework.core.Ordered;
  }
}