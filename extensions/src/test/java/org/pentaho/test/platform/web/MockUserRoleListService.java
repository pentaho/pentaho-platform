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


package org.pentaho.test.platform.web;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockUserRoleListService implements IUserRoleListService {

  @Override
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

  @Override
  public List<String> getAllUsers() {
    List<String> allUsers = new ArrayList<String>( 4 );
    allUsers.add( "pat" ); //$NON-NLS-1$
    allUsers.add( "tiffany" ); //$NON-NLS-1$
    allUsers.add( "admin" ); //$NON-NLS-1$
    allUsers.add( "suzy" ); //$NON-NLS-1$
    return allUsers;
  }

  @Override
  public List<String> getAllRoles( ITenant tenant ) {
    List<String> tenant_1 = new ArrayList<String>( 7 );
    tenant_1.add( "dev" ); //$NON-NLS-1$
    tenant_1.add( "Admin" ); //$NON-NLS-1$
    tenant_1.add( "devmgr" ); //$NON-NLS-1$
    tenant_1.add( "ceo" ); //$NON-NLS-1$
    tenant_1.add( "cto" ); //$NON-NLS-1$
    tenant_1.add( "Authenticated" ); //$NON-NLS-1$
    tenant_1.add( "is" ); //$NON-NLS-1$

    List<String> tenant_2 = new ArrayList<String>( 7 );
    tenant_1.add( "dev" ); //$NON-NLS-1$
    tenant_1.add( "Admin" ); //$NON-NLS-1$
    tenant_1.add( "devmgr" ); //$NON-NLS-1$
    tenant_1.add( "Authenticated" ); //$NON-NLS-1$
    tenant_1.add( "is" ); //$NON-NLS-1$

    List<String> tenant_3 = new ArrayList<String>( 7 );
    tenant_1.add( "dev" ); //$NON-NLS-1$
    tenant_1.add( "ceo" ); //$NON-NLS-1$
    tenant_1.add( "cto" ); //$NON-NLS-1$
    tenant_1.add( "Authenticated" ); //$NON-NLS-1$
    tenant_1.add( "is" ); //$NON-NLS-1$

    Map<String, List<String>> roleMap = new HashMap<String, List<String>>();
    roleMap.put( "tenant0", tenant_1 ); //$NON-NLS-1$
    roleMap.put( "tenant1", tenant_2 ); //$NON-NLS-1$
    roleMap.put( "tenant2", tenant_3 ); //$NON-NLS-1$
    return roleMap.get( tenant.getId() );
  }

  @Override
  public List<String> getAllUsers( ITenant tenant ) {
    // TODO Auto-generated method stub
    List<String> tenant_1 = new ArrayList<String>( 4 );
    tenant_1.add( "pat" ); //$NON-NLS-1$
    tenant_1.add( "tiffany" ); //$NON-NLS-1$
    tenant_1.add( "admin" ); //$NON-NLS-1$
    tenant_1.add( "suzy" ); //$NON-NLS-1$

    List<String> tenant_2 = new ArrayList<String>( 4 );
    tenant_1.add( "mary" ); //$NON-NLS-1$
    tenant_1.add( "jill" ); //$NON-NLS-1$
    tenant_1.add( "jack" ); //$NON-NLS-1$
    tenant_1.add( "jeremy" ); //$NON-NLS-1$

    List<String> tenant_3 = new ArrayList<String>( 4 );
    tenant_1.add( "johny" ); //$NON-NLS-1$
    tenant_1.add( "tom" ); //$NON-NLS-1$
    tenant_1.add( "jerry" ); //$NON-NLS-1$
    tenant_1.add( "jane" ); //$NON-NLS-1$

    Map<String, List<String>> userMap = new HashMap<String, List<String>>();
    userMap.put( "tenant0", tenant_1 ); //$NON-NLS-1$
    userMap.put( "tenant1", tenant_2 ); //$NON-NLS-1$
    userMap.put( "tenant2", tenant_3 ); //$NON-NLS-1$
    return userMap.get( tenant.getId() );
  }

  @Override
  public List<String> getUsersInRole( ITenant tenant, String role ) {
    if ( tenant == null ) {
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
    if ( tenant.getId().equals( "tenant0" ) ) { //$NON-NLS-1$
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
    } else if ( tenant.getId().equals( "tenant1" ) ) { //$NON-NLS-1$
      if ( role.equals( "dev" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "mary", "jill" } ); //$NON-NLS-1$ //$NON-NLS-2$
      } else if ( role.equals( "Admin" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "jack" } ); //$NON-NLS-1$
      } else if ( role.equals( "devmgr" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "jeremy" } ); //$NON-NLS-1$
      } else if ( role.equals( "is" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "mary" } ); //$NON-NLS-1$
      }
    } else if ( tenant.getId().equals( "tenant2" ) ) { //$NON-NLS-1$
      if ( role.equals( "dev" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "johny", "tom" } ); //$NON-NLS-1$ //$NON-NLS-2$
      } else if ( role.equals( "ceo" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "jerry" } ); //$NON-NLS-1$
      } else if ( role.equals( "cto" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "jane" } ); //$NON-NLS-1$
      } else if ( role.equals( "is" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "johny" } ); //$NON-NLS-1$
      }
    }
    return Collections.emptyList();
  }

  @Override
  public List<String> getRolesForUser( ITenant tenant, String username ) {
    // TODO Auto-generated method stub
    if ( tenant == null ) {
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
    } else if ( tenant.getId().equals( "tenant0" ) ) { //$NON-NLS-1$
      if ( username.equals( "pat" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "dev", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$
      } else if ( username.equals( "tiffany" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "dev", "devmgr", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else if ( username.equals( "admin" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "Admin", "ceo", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else if ( username.equals( "suzy" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "cto", "is", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    } else if ( tenant.getId().equals( "tenant1" ) ) { //$NON-NLS-1$
      if ( username.equals( "mary" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "dev", "is", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else if ( username.equals( "jill" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "dev", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else if ( username.equals( "jeremy" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "devmgr", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else if ( username.equals( "jack" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "Admin", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    } else if ( tenant.getId().equals( "tenant2" ) ) { //$NON-NLS-1$
      if ( username.equals( "johny" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "dev", "is", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else if ( username.equals( "tom" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "dev", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else if ( username.equals( "jerry" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "ceo", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else if ( username.equals( "jane" ) ) { //$NON-NLS-1$
        return Arrays.asList( new String[] { "cto", "Authenticated" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    }
    return Collections.emptyList();
  }

  @Override
  public List<String> getSystemRoles() {
    return Arrays.asList( new String[] { "Admin" } );
  }

}
