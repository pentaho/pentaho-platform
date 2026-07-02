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


package org.pentaho.platform.security.policy.rolebased;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.security.policy.rolebased.actions.SchedulerAction;
import org.pentaho.platform.security.policy.rolebased.ws.IAuthorizationPolicyWebService;
import org.pentaho.platform.security.policy.rolebased.ws.IRoleAuthorizationPolicyRoleBindingDaoWebService;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * To run this, put Metro JARs in your classpath.
 */
@SuppressWarnings( "nls" )
public class AuthorizationPolicyClient {
  private IAuthorizationPolicy policy;

  private IRoleAuthorizationPolicyRoleBindingDaoWebService roleBindingDaoWebService;

  @Before
  public void setUp() throws Exception {
    Service service =
        Service.create( new URL( "http://localhost:8080/pentaho/webservices/authorizationPolicy?wsdl" ), new QName(
            "http://www.pentaho.org/ws/1.0", "authorizationPolicy" ) );

    policy = service.getPort( IAuthorizationPolicyWebService.class );

    service =
        Service.create( new URL( "http://localhost:8080/pentaho/webservices/roleBindingDao?wsdl" ), new QName(
            "http://www.pentaho.org/ws/1.0", "roleBindingDao" ) );

    roleBindingDaoWebService = service.getPort( IRoleAuthorizationPolicyRoleBindingDaoWebService.class );

    // basic auth
    ( (BindingProvider) policy ).getRequestContext().put( BindingProvider.USERNAME_PROPERTY, "suzy" );
    ( (BindingProvider) policy ).getRequestContext().put( BindingProvider.PASSWORD_PROPERTY, "password" );
    // accept cookies to maintain session on server
    ( (BindingProvider) policy ).getRequestContext().put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );
    ( (BindingProvider) roleBindingDaoWebService ).getRequestContext()
      .put( BindingProvider.USERNAME_PROPERTY, "admin" );
    ( (BindingProvider) roleBindingDaoWebService ).getRequestContext().put( BindingProvider.PASSWORD_PROPERTY,
        "password" );
    // accept cookies to maintain session on server
    ( (BindingProvider) roleBindingDaoWebService ).getRequestContext().put( BindingProvider.SESSION_MAINTAIN_PROPERTY,
        true );
  }

  @Test
  public void testEverything() {
    final String RUNTIME_ROLE_AUTHENTICATED = "Authenticated";
    roleBindingDaoWebService.setRoleBindings( RUNTIME_ROLE_AUTHENTICATED, Arrays.asList( new String[] {
      RepositoryReadAction.NAME, RepositoryCreateAction.NAME, SchedulerAction.NAME } ) );

    List<String> allowedActions = policy.getAllowedActions( "org.pentaho" );
    assertEquals( 3, allowedActions.size() );
  }
}
