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

package org.pentaho.platform.web.http.security;

import com.mockrunner.mock.web.MockHttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
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
