package org.pentaho.platform.engine.security.userrole.ws;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import org.junit.Before;
import org.junit.Ignore;

/**
 * This test wraps the regular unit test with a webservices endpoint, verifying the client conversion.
 * 
 * This can't be used in a live environment until metro 2.0 jars are available to test with.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 *
 */
@SuppressWarnings("nls")
@Ignore
public class UserDetailsRoleListEndpointTest extends UserDetailsRoleListWebServiceTest {
  
  IUserRoleListWebService userRoleListWebService;
  
  public UserDetailsRoleListEndpointTest() {
    Endpoint.publish("http://localhost:8891/userrolelisttest", new DefaultUserRoleListWebService()); //$NON-NLS-1$ 
  }
  
  public static void main(String args[]) throws Exception {
    
    // test against a live server, dev use only
    System.setProperty("com.sun.xml.ws.monitoring.endpoint", "true");
    System.setProperty("com.sun.xml.ws.monitoring.client", "true");
    System.setProperty("com.sun.xml.ws.monitoring.registrationDebug", "FINE");
    System.setProperty("com.sun.xml.ws.monitoring.runtimeDebug", "true");
    Service service = Service.create(new URL("http://localhost:8080/pentaho/webservices/userRoleListService?wsdl"), new QName(
    "http://www.pentaho.org/ws/1.0", "userRoleListService"));
    IUserRoleListWebService userDetailsRoleListWebService = service.getPort(IUserRoleListWebService.class);
    ((BindingProvider) userDetailsRoleListWebService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "joe");
    ((BindingProvider) userDetailsRoleListWebService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "password");
    // accept cookies to maintain session on server
    ((BindingProvider) userDetailsRoleListWebService).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
  }
  
  @Before
  public void setUp() throws Exception {
    System.setProperty("com.sun.xml.ws.monitoring.endpoint", "true");
    System.setProperty("com.sun.xml.ws.monitoring.client", "true");
    System.setProperty("com.sun.xml.ws.monitoring.registrationDebug", "FINE");
    System.setProperty("com.sun.xml.ws.monitoring.runtimeDebug", "true");
    Service service = Service.create(new URL("http://localhost:8891/userrolelisttest?wsdl"), new QName(
    "http://www.pentaho.org/ws/1.0", "DefaultUserDetailsRoleListWebServiceService"));
    userRoleListWebService = service.getPort(IUserRoleListWebService.class);
  }
  
  @Override
  public IUserRoleListWebService getUserRoleListWebService() {
    return userRoleListWebService;
  }

}
