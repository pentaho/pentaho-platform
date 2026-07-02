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


package org.pentaho.test.platform.security.userroledao.ws;

import org.junit.Before;
import org.pentaho.platform.security.userroledao.ws.IUserRoleWebService;
import org.pentaho.platform.security.userroledao.ws.UserRoleSecurityInfo;
import org.pentaho.platform.security.userroledao.ws.UserRoleWebService;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.Service;
import java.net.URL;

/**
 * This test wraps the regular unit test with a webservices endpoint, verifying the client conversion.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 * 
 */
@SuppressWarnings( "nls" )
public class UserRoleEndpointIT extends UserRoleWebServiceBase {

  IUserRoleWebService userRoleWebService;

  public static void main( String[] args ) throws Exception {

    // test against a live server, dev use only
    System.setProperty( "com.sun.xml.ws.monitoring.endpoint", "true" );
    System.setProperty( "com.sun.xml.ws.monitoring.client", "true" );
    System.setProperty( "com.sun.xml.ws.monitoring.registrationDebug", "FINE" );
    System.setProperty( "com.sun.xml.ws.monitoring.runtimeDebug", "true" );
    Service service =
        Service.create( new URL( "http://localhost:8080/pentaho/webservices/userRoleService?wsdl" ), new QName(
            "http://www.pentaho.org/ws/1.0", "userRoleService" ) );
    IUserRoleWebService userRoleWebService = service.getPort( IUserRoleWebService.class );
    ( (BindingProvider) userRoleWebService ).getRequestContext().put( BindingProvider.USERNAME_PROPERTY, "admin" );
    ( (BindingProvider) userRoleWebService ).getRequestContext().put( BindingProvider.PASSWORD_PROPERTY, "password" );
    // accept cookies to maintain session on server
    ( (BindingProvider) userRoleWebService ).getRequestContext().put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );

    UserRoleSecurityInfo info = userRoleWebService.getUserRoleSecurityInfo();

    // should be Authenticated
    System.out.println( info.getDefaultRoles().get( 0 ) );

  }

  @Before
  public void setUp() throws Exception {
    try {
      Endpoint.publish( "http://localhost:9891/test", new UserRoleWebService() ); //$NON-NLS-1$
    } catch ( Throwable th ) {
      //ignore
    }
    System.setProperty( "com.sun.xml.ws.monitoring.endpoint", "true" );
    System.setProperty( "com.sun.xml.ws.monitoring.client", "true" );
    System.setProperty( "com.sun.xml.ws.monitoring.registrationDebug", "FINE" );
    System.setProperty( "com.sun.xml.ws.monitoring.runtimeDebug", "true" );
    Service service =
        Service.create( new URL( "http://localhost:9891/test?wsdl" ), new QName( "http://www.pentaho.org/ws/1.0",
            "userRoleService" ) );

    mockUserAsAdmin( true /* run this test using a mocked admin user */ );

    userRoleWebService = service.getPort( IUserRoleWebService.class );
  }

  @Override
  public IUserRoleWebService getUserRoleWebService() {
    return userRoleWebService;
  }

}
