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

package org.pentaho.test.platform.engine.security.userrole.ws;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MockUserRoleListService implements IUserRoleListService {

  public List<String> getAllRoles() {
    List<String> allAuths = new ArrayList<String>( 7 );
    allAuths.add( "dev" ); //$NON-NLS-1$
    allAuths.add( "Admin" ); //$NON-NLS-1$
    allAuths.add( "devmgr" ); //$NON-NLS-1$
    allAuths.add( "ceo" ); //$NON-NLS-1$
    allAuths.add( "cto" ); //$NON-NLS-1$
    allAuths.add( "Authenticated" ); //$NON-NLS-1$
    allAuths.add( "is" ); //$NON-NLS-1$
    return allAuths;
  }

  public List<String> getAllUsers() {
    List<String> allUsers = new ArrayList<String>( 4 );
    allUsers.add( "pat" ); //$NON-NLS-1$
    allUsers.add( "tiffany" ); //$NON-NLS-1$
    allUsers.add( "admin" ); //$NON-NLS-1$
    allUsers.add( "suzy" ); //$NON-NLS-1$
    return allUsers;
  }

  public List<String> getUsersInRole( String role ) {
    if ( role.equals( "dev" ) ) { //$NON-NLS-1$
      return Arrays.asList( new String[] { "pat", "tiffany" } ); //$NON-NLS-1$ //$NON-NLS-2$
    } else if ( role.equals( "Admin" ) ) { //$NON-NLS-1$
      return Arrays.asList( new String[] { "admin" } ); //$NON-NLS-1$
    } else if ( role.equals( "devmgr" ) ) { //$NON-NLS-1$
      return Arrays.asList( new String[] { "tiffany" } ); //$NON-NLS-1$
    } else if ( role.equals( "ceo" ) ) { //$NON-NLS-1$
      return Arrays.asList( new String[] { "admin" } ); //$NON-NLS-1$
    } else if ( role.equals( "cto" ) ) { //$NON-NLS-1$
      return Arrays.asList( new String[] { "suzy" } ); //$NON-NLS-1$
    } else if ( role.equals( "is" ) ) { //$NON-NLS-1$
      return Arrays.asList( new String[] { "suzy" } ); //$NON-NLS-1$
    }
    return Collections.emptyList();
  }

  public List<String> getRolesForUser( String username ) {
    if ( username.equals( "pat" ) ) { //$NON-NLS-1$
      return Arrays.asList( new String[] { "dev", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$
    } else if ( username.equals( "tiffany" ) ) { //$NON-NLS-1$
      return Arrays.asList( new String[] { "dev", "devmgr", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } else if ( username.equals( "admin" ) ) { //$NON-NLS-1$
      return Arrays.asList( new String[] { "Admin", "ceo", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } else if ( username.equals( "suzy" ) ) { //$NON-NLS-1$
      return Arrays.asList( new String[] { "cto", "is", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    return Collections.emptyList();

  }

  @Override
  public List<String> getAllRoles( ITenant arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getAllUsers( ITenant arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getRolesForUser( ITenant arg0, String arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getUsersInRole( ITenant arg0, String arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getSystemRoles() {
    List<String> systemRoles = new ArrayList<String>( 2 );
    systemRoles.add( "Admin" );
    systemRoles.add( "Authenticated" );
    return systemRoles;
  }

}
