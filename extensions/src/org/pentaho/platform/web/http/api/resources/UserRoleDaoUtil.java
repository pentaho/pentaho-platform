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
