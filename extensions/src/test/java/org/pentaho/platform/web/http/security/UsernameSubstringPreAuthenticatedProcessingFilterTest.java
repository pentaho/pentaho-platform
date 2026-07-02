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


package org.pentaho.platform.web.http.security;

import org.springframework.mock.web.MockHttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;

import static org.junit.Assert.assertEquals;

@SuppressWarnings( "nls" )
public class UsernameSubstringPreAuthenticatedProcessingFilterTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetPreAuthenticatedPrincipal() {
    UsernameSubstringPreAuthenticatedProcessingFilter filter = new UsernameSubstringPreAuthenticatedProcessingFilter();
    String expected = "testUser";
    String user = "testUser";
    HttpServletRequest req = null;

    // null regex
    filter.setRegex( null );
    req = getRequest( user );
    assertEquals( expected, filter.getPreAuthenticatedPrincipal( req ) );
    assertEquals( user, req
        .getAttribute( UsernameSubstringPreAuthenticatedProcessingFilter.PENTAHO_ORIG_USER_PRINCIPAL ) );

    // whitespace regex
    filter.setRegex( " " );
    req = getRequest( user );
    assertEquals( expected, filter.getPreAuthenticatedPrincipal( req ) );
    assertEquals( user, req
        .getAttribute( UsernameSubstringPreAuthenticatedProcessingFilter.PENTAHO_ORIG_USER_PRINCIPAL ) );

    // empty string regex
    filter.setRegex( "" );
    req = getRequest( user );
    assertEquals( expected, filter.getPreAuthenticatedPrincipal( req ) );
    assertEquals( user, req
        .getAttribute( UsernameSubstringPreAuthenticatedProcessingFilter.PENTAHO_ORIG_USER_PRINCIPAL ) );

    // windows domain regex
    user = "domain\\testUser";
    filter.setRegex( ".+\\\\(.+)" );
    req = getRequest( user );
    assertEquals( expected, filter.getPreAuthenticatedPrincipal( req ) );
    assertEquals( user, req
        .getAttribute( UsernameSubstringPreAuthenticatedProcessingFilter.PENTAHO_ORIG_USER_PRINCIPAL ) );

    // email address regex
    user = "testUser@domain.org";
    filter.setRegex( "(.+)@.+" );
    req = getRequest( user );
    assertEquals( expected, filter.getPreAuthenticatedPrincipal( req ) );
    assertEquals( user, req
        .getAttribute( UsernameSubstringPreAuthenticatedProcessingFilter.PENTAHO_ORIG_USER_PRINCIPAL ) );

    // email address regex with null username
    filter.setRegex( "(.+)@.+" );
    req = getRequest( null );
    assertEquals( null, filter.getPreAuthenticatedPrincipal( req ) );
    assertEquals( null, req
        .getAttribute( UsernameSubstringPreAuthenticatedProcessingFilter.PENTAHO_ORIG_USER_PRINCIPAL ) );
  }

  private final HttpServletRequest getRequest( final String aUserName ) {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRemoteUser( aUserName );
    req.setUserPrincipal( new Principal() {
      public String getName() {
        return aUserName;
      }
    } );
    return req;
  }

}
