package org.pentaho.platform.engine.security.userrole.ws;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("nls")
public class UserDetailsRoleListWebServiceTest {
  private MicroPlatform microPlatform;

  @Before
  public void init0() {
    microPlatform = new MicroPlatform();
    microPlatform.define(IUserRoleListService.class, MockUserRoleListService.class);
  }
  
  public IUserRoleListWebService getUserRoleListWebService() {
    return new DefaultUserRoleListWebService();
  }
  @Test
  public void testGetAllRoles()  throws Exception {
    IUserRoleListWebService service = getUserRoleListWebService();

    try {
      List<String> allRoles = service.getAllRoles();
      assertNotNull(allRoles);
      assertEquals(allRoles.size(), 7); // Should have exactly 7 roles
      assertEquals(allRoles.get(0), "dev");//$NON-NLS-1$
      assertEquals(allRoles.get(6), "is"); //$NON-NLS-1$

    } catch (Exception e) {
      Assert.fail();
    }
  }
  @Test
  public void testGetAllUsers() throws Exception {
    IUserRoleListWebService service = getUserRoleListWebService();

    try {
      List<String> allUsers = service.getAllUsers();
      assertNotNull(allUsers);
      assertEquals(allUsers.size(), 4);
      assertEquals(allUsers.get(0), "pat");//$NON-NLS-1$
      assertEquals(allUsers.get(3), "suzy");//$NON-NLS-1$

    } catch (Exception e) {
      Assert.fail();
    }
  }
  @Test
  public void testGetUserRoleInfo() throws Exception {
    IUserRoleListWebService service = getUserRoleListWebService();

    try {
    
    UserRoleInfo userRoleInfo = service.getUserRoleInfo();//$NON-NLS-1$
    assertNotNull(userRoleInfo);
    assertEquals(userRoleInfo.getRoles().size(), 7);
    assertEquals(userRoleInfo.getUsers().size(), 4);
    } catch (Exception e) {
      Assert.fail();
    }
  }
}
