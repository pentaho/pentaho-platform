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

package org.pentaho.test.platform.security.userroledao.ws;

import org.junit.Before;
import org.junit.Ignore;
import org.pentaho.platform.security.userroledao.ws.IUserRoleWebService;
import org.pentaho.platform.security.userroledao.ws.UserRoleSecurityInfo;
import org.pentaho.platform.security.userroledao.ws.UserRoleWebService;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import java.net.URL;

/**
 * This test wraps the regular unit test with a webservices endpoint, verifying the client conversion.
 * 
 * This can't be used in a live environment until metro 2.0 jars are available to test with.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 * 
 */
@SuppressWarnings( "nls" )
@Ignore
public class UserRoleEndpointTest extends UserRoleWebServiceTest {

  IUserRoleWebService userRoleWebService;

  public UserRoleEndpointTest() {
    Endpoint.publish( "http://localhost:9891/test", new UserRoleWebService() ); //$NON-NLS-1$ 
  }

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
    System.setProperty( "com.sun.xml.ws.monitoring.endpoint", "true" );
    System.setProperty( "com.sun.xml.ws.monitoring.client", "true" );
    System.setProperty( "com.sun.xml.ws.monitoring.registrationDebug", "FINE" );
    System.setProperty( "com.sun.xml.ws.monitoring.runtimeDebug", "true" );
    Service service =
        Service.create( new URL( "http://localhost:9891/test?wsdl" ), new QName( "http://www.pentaho.org/ws/1.0",
            "UserRoleWebServiceService" ) );
    userRoleWebService = service.getPort( IUserRoleWebService.class );
  }

  @Override
  public IUserRoleWebService getUserRoleWebService() {
    return userRoleWebService;
  }

}
