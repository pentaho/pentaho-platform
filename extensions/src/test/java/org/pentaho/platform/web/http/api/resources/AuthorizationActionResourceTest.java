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


package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.pentaho.di.core.util.Assert;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.web.http.api.resources.services.AuthorizationActionService;

import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

/**
 * This unit test validates AuthorizationActionResource's behaviour, i.e. the expected javax Response status and content.
 * We won't dive much into the underlying service, as there are already unit tests that cover that one.
 */
public class AuthorizationActionResourceTest {

  AuthorizationActionResource resource;
  IAuthorizationAction readAction;

  /*
   * we won't dive much into the service, as there are already unit tests that cover it,
   * but we will be mocking its IAuthorizationPolicy.getPolicy()
   */
  AuthorizationActionServiceForTesting service;
  IAuthorizationPolicy policy;

  @Before
  public void setUp() {

    resource = mock( AuthorizationActionResource.class );
    readAction = mock( IAuthorizationAction.class );
    when( readAction.getName() ).thenReturn( "Read" );

    service = mock( AuthorizationActionServiceForTesting.class );
    policy = mock( IAuthorizationPolicy.class );

    when( policy.isAllowed( readAction.getName() ) ).thenReturn( true );

    when( service.getActionList() ).thenReturn( Arrays.asList( new IAuthorizationAction[] { readAction } ) );
    when( service.getPolicy() ).thenReturn( policy );

    when( service.validateAuth( nullable( String.class ) ) ).thenCallRealMethod();

    when( resource.getAuthorizationActionService() ).thenReturn( service );
    when( resource.validateAuth( nullable( String.class ) ) ).thenCallRealMethod();
  }

  @Test
  public void testCorrectActionIsProperlyGranted() throws Exception {

    Response r = resource.validateAuth( readAction.getName() );

    verify( resource.getAuthorizationActionService(), times( 1 ) ).validateAuth( readAction.getName() );
    assertExpectedResponse( r, Response.Status.OK.getStatusCode(), Boolean.TRUE.toString() );
  }

  @Test
  public void testIncorrectActionIsProperlyDenied() throws Exception {

    final String DUMMY_ACTION_NAME = "Dummy_Action_Name";

    Response r = resource.validateAuth( DUMMY_ACTION_NAME );

    verify( resource.getAuthorizationActionService(), times( 1 ) ).validateAuth( DUMMY_ACTION_NAME );
    assertExpectedResponse( r, Response.Status.OK.getStatusCode(), Boolean.FALSE.toString() );
  }

  @Test
  public void testNullActionIsProperlyDenied() throws Exception {

    Response r = resource.validateAuth( null );

    verify( resource.getAuthorizationActionService(), times( 1 ) ).validateAuth( null );
    assertExpectedResponse( r, Response.Status.OK.getStatusCode(), Boolean.FALSE.toString() );
  }

  @Test
  public void testEmptyActionIsProperlyDenied() throws Exception {

    Response r = resource.validateAuth( "" );

    verify( resource.getAuthorizationActionService(), times( 1 ) ).validateAuth( "" );
    assertExpectedResponse( r, Response.Status.OK.getStatusCode(), Boolean.FALSE.toString() );
  }

  @After
  public void tearDown() {
    resource = null;
    service = null;
    policy = null;
    readAction = null;
  }

  private void assertExpectedResponse( Response response, int expectedStatus, String expectedEntity ) {

    Assert.assertTrue( response != null && response.getEntity() != null );
    Assert.assertTrue( response.getStatus() == expectedStatus );
    Assert.assertTrue( response.getEntity().toString().equalsIgnoreCase( expectedEntity ) );
  }

  /*
   * we won't dive much into the service, as there are already unit tests that cover it,
   * but we will be mocking its IAuthorizationPolicy.getPolicy()
   */
  private class AuthorizationActionServiceForTesting extends AuthorizationActionService {

    // changing visibility to public to allow mocking it
    @Override public List<IAuthorizationAction> getActionList() {
      return super.getActionList();
    }

    // changing visibility to public to allow mocking it
    @Override public IAuthorizationPolicy getPolicy() {
      return super.getPolicy();
    }
  }
}
