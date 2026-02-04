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

import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement( name = "userList" )
public class UserListWrapper {
  List<String> users = new ArrayList<String>();

  public UserListWrapper() {
  }

  public UserListWrapper( List<IPentahoUser> users ) {
    List<String> userList = new ArrayList<String>();
    for ( IPentahoUser user : users ) {
      userList.add( user.getUsername() );
    }
    this.users.addAll( userList );
  }

  public UserListWrapper( Collection<String> users ) {
    this.users.addAll( users );
  }

  public List<String> getUsers() {
    return users;
  }

  public void setUsers( List<String> users ) {
    if ( users != this.users ) {
      this.users.clear();
      this.users.addAll( users );
    }
  }
}
