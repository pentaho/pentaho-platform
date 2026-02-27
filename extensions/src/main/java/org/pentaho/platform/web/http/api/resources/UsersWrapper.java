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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement( name = "users" )
public class UsersWrapper {

  List<String> users = new ArrayList<String>();

  public UsersWrapper() {
  }

  public UsersWrapper( List<String> users ) {
    this.users.addAll( users );
  }

  @XmlElement( name = "user" )
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
