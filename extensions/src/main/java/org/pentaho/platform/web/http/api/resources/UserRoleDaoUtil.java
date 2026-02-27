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


package org.pentaho.platform.web.http.api.resources;

import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;

import java.util.ArrayList;
import java.util.List;

public class UserRoleDaoUtil {

  public static List<String> roleListNames( List<IPentahoRole> pentahoRoles ) {

    List<String> roleNames = new ArrayList<String>();
    for ( IPentahoRole role : pentahoRoles ) {
      roleNames.add( role.getName() );
    }

    return roleNames;

  }

  public static List<String> userListUserNames( List<IPentahoUser> pentahoUsers ) {

    List<String> userNames = new ArrayList<String>();
    for ( IPentahoUser user : pentahoUsers ) {
      userNames.add( user.getUsername() );
    }

    return userNames;

  }

}
