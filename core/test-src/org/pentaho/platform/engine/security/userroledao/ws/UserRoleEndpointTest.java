package org.pentaho.platform.engine.security.userroledao.ws;

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
public class UserRoleEndpointTest extends UserRoleWebServiceTest {
  
  IUserRoleWebService userRoleWebService;
  
  public UserRoleEndpointTest() {
    Endpoint.publish("http://localhost:9891/test", new UserRoleWebService()); //$NON-NLS-1$ 
  }
  
  public static void main(String args[]) throws Exception {
    
    // test against a live server, dev use only
    System.setProperty("com.sun.xml.ws.monitoring.endpoint", "true");
    System.setProperty("com.sun.xml.ws.monitoring.client", "true");
    System.setProperty("com.sun.xml.ws.monitoring.registrationDebug", "FINE");
    System.setProperty("com.sun.xml.ws.monitoring.runtimeDebug", "true");
    Service service = Service.create(new URL("http://localhost:8080/pentaho/webservices/userRoleService?wsdl"), new QName(
    "http://www.pentaho.org/ws/1.0", "userRoleService"));
    IUserRoleWebService userRoleWebService = service.getPort(IUserRoleWebService.class);
    ((BindingProvider) userRoleWebService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "joe");
    ((BindingProvider) userRoleWebService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "password");
    // accept cookies to maintain session on server
    ((BindingProvider) userRoleWebService).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

    UserRoleSecurityInfo info = userRoleWebService.getUserRoleSecurityInfo();
    
    // should be Authenticated
    System.out.println(info.getDefaultRoles().get(0));
    
  }
  
  @Before
  public void setUp() throws Exception {
    System.setProperty("com.sun.xml.ws.monitoring.endpoint", "true");
    System.setProperty("com.sun.xml.ws.monitoring.client", "true");
    System.setProperty("com.sun.xml.ws.monitoring.registrationDebug", "FINE");
    System.setProperty("com.sun.xml.ws.monitoring.runtimeDebug", "true");
    Service service = Service.create(new URL("http://localhost:9891/test?wsdl"), new QName(
    "http://www.pentaho.org/ws/1.0", "UserRoleWebServiceService"));
    userRoleWebService = service.getPort(IUserRoleWebService.class);
  }
  
  @Override
  public IUserRoleWebService getUserRoleWebService() {
    return userRoleWebService;
  }

}
