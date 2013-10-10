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

package org.pentaho.platform.scheduler2.quartz.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;

@SuppressWarnings( { "nls", "unchecked" } )
public class StubUserRoleListService implements IUserRoleListService {

  public List getAllRoles() {
    // TODO Auto-generated method stub
    return null;
  }

  public List getAllUsers() {
    // TODO Auto-generated method stub
    return null;
  }

  public List getUsersInRole( String role ) {
    // TODO Auto-generated method stub
    return null;
  }

  public List getRolesForUser( String userName ) {
    return Arrays.asList( "FL_GATOR", "FS_SEMINOLE" );
  }

  @Override
  public List<String> getAllRoles( ITenant tenant ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getAllUsers( ITenant tenant ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getUsersInRole( ITenant tenant, String role ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getRolesForUser( ITenant tenant, String username ) {
    List roles = new ArrayList<String>();
    roles.add( "Admin" );
    roles.add( "Authenticated" );
    return roles;
  }

  @Override
  public List<String> getSystemRoles() {
    // TODO Auto-generated method stub
    return null;
  }

}
