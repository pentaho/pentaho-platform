/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.security.userrole.ws;

import org.junit.Before;
import org.pentaho.platform.security.userrole.ws.DefaultUserRoleListWebService;
import org.pentaho.platform.security.userrole.ws.IUserRoleListWebService;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import java.net.URL;

/**
 * This test wraps the regular unit test with a webservices endpoint, verifying the client conversion.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 * 
 */
@SuppressWarnings( "nls" )
public class UserDetailsRoleListEndpointIT extends UserDetailsRoleListWebServiceBase {

  IUserRoleListWebService userRoleListWebService;

  public static void main( String[] args ) throws Exception {

    // test against a live server, dev use only
    System.setProperty( "com.sun.xml.ws.monitoring.endpoint", "true" );
    System.setProperty( "com.sun.xml.ws.monitoring.client", "true" );
    System.setProperty( "com.sun.xml.ws.monitoring.registrationDebug", "FINE" );
    System.setProperty( "com.sun.xml.ws.monitoring.runtimeDebug", "true" );
    Service service =
        Service.create( new URL( "http://localhost:8080/pentaho/webservices/userRoleListService?wsdl" ), new QName(
            "http://www.pentaho.org/ws/1.0", "userRoleListService" ) );
    IUserRoleListWebService userDetailsRoleListWebService = service.getPort( IUserRoleListWebService.class );
    ( (BindingProvider) userDetailsRoleListWebService ).getRequestContext().put( BindingProvider.USERNAME_PROPERTY,
        "admin" );
    ( (BindingProvider) userDetailsRoleListWebService ).getRequestContext().put( BindingProvider.PASSWORD_PROPERTY,
        "password" );
    // accept cookies to maintain session on server
    ( (BindingProvider) userDetailsRoleListWebService ).getRequestContext().put(
        BindingProvider.SESSION_MAINTAIN_PROPERTY, true );

  }

  @Before
  public void setUp() throws Exception {
    try {
      Endpoint.publish( "http://localhost:8891/userrolelisttest", new DefaultUserRoleListWebService() ); //$NON-NLS-1$
    } catch ( Throwable th ) {
      //ignore
    }

    System.setProperty( "com.sun.xml.ws.monitoring.endpoint", "true" );
    System.setProperty( "com.sun.xml.ws.monitoring.client", "true" );
    System.setProperty( "com.sun.xml.ws.monitoring.registrationDebug", "FINE" );
    System.setProperty( "com.sun.xml.ws.monitoring.runtimeDebug", "true" );
    Service service =
        Service.create( new URL( "http://localhost:8891/userrolelisttest?wsdl" ), new QName(
            "http://www.pentaho.org/ws/1.0", "userRoleListService" ) );
    userRoleListWebService = service.getPort( IUserRoleListWebService.class );
  }



  @Override
  public IUserRoleListWebService getUserRoleListWebService() {
    return userRoleListWebService;
  }

}
