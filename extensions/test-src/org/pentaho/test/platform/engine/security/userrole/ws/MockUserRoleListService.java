package org.pentaho.test.platform.engine.security.userrole.ws;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;

public class MockUserRoleListService implements IUserRoleListService {

  public List<String> getAllRoles() {
    List<String> allAuths = new ArrayList<String>(7);
    allAuths.add("dev"); //$NON-NLS-1$
    allAuths.add("Admin"); //$NON-NLS-1$
    allAuths.add("devmgr"); //$NON-NLS-1$
    allAuths.add("ceo"); //$NON-NLS-1$
    allAuths.add("cto"); //$NON-NLS-1$
    allAuths.add("Authenticated"); //$NON-NLS-1$
    allAuths.add("is"); //$NON-NLS-1$
    return allAuths;
  }

  public List<String> getAllUsers() {
    List<String> allUsers = new ArrayList<String>(4);
    allUsers.add("pat"); //$NON-NLS-1$
    allUsers.add("tiffany"); //$NON-NLS-1$
    allUsers.add("joe"); //$NON-NLS-1$
    allUsers.add("suzy"); //$NON-NLS-1$
    return allUsers;
  }

  public List<String> getUsersInRole(String role) {
    if (role.equals("dev")) { //$NON-NLS-1$
      return Arrays.asList(new String[] { "pat", "tiffany" }); //$NON-NLS-1$ //$NON-NLS-2$
    } else if (role.equals("Admin")) { //$NON-NLS-1$
      return Arrays.asList(new String[] { "joe" });//$NON-NLS-1$
    } else if (role.equals("devmgr")) { //$NON-NLS-1$
      return Arrays.asList(new String[] { "tiffany" });//$NON-NLS-1$
    } else if (role.equals("ceo")) { //$NON-NLS-1$
      return Arrays.asList(new String[] { "joe" });//$NON-NLS-1$
    } else if (role.equals("cto")) { //$NON-NLS-1$
      return Arrays.asList(new String[] { "suzy" });//$NON-NLS-1$
    } else if (role.equals("is")) { //$NON-NLS-1$
      return Arrays.asList(new String[] { "suzy" });//$NON-NLS-1$
    }
    return Collections.emptyList();
  }

  public List<String> getRolesForUser(String username) {
    if (username.equals("pat")) { //$NON-NLS-1$
      return Arrays.asList(new String[] { "dev", "Authenticated" });//$NON-NLS-1$ //$NON-NLS-2$
    } else if (username.equals("tiffany")) {//$NON-NLS-1$
      return Arrays.asList(new String[] { "dev", "devmgr", "Authenticated" });//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } else if (username.equals("joe")) {//$NON-NLS-1$
      return Arrays.asList(new String[] { "Admin", "ceo", "Authenticated" });//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } else if (username.equals("suzy")) {//$NON-NLS-1$
      return Arrays.asList(new String[] { "cto", "is", "Authenticated" });//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    return Collections.emptyList();

  }

  @Override
  public List<String> getAllRoles(ITenant arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getAllUsers(ITenant arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getRolesForUser(ITenant arg0, String arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getUsersInRole(ITenant arg0, String arg1) {
    // TODO Auto-generated method stub
    return null;
  }

}
