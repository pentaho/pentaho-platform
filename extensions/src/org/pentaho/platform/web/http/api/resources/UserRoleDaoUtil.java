package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;

public class UserRoleDaoUtil {

  public static List<String> roleListNames(List<IPentahoRole> pentahoRoles) {

    List<String> roleNames = new ArrayList<String>();
    for (IPentahoRole role : pentahoRoles) {
      roleNames.add(role.getName());
    }

    return roleNames;

  }

  public static List<String> userListUserNames(List<IPentahoUser> pentahoUsers) {

    List<String> userNames = new ArrayList<String>();
    for (IPentahoUser user : pentahoUsers) {
      userNames.add(user.getUsername());
    }

    return userNames;

  }

}
