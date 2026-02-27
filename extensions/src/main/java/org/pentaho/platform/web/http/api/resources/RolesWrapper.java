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

@XmlRootElement( name = "roles" )
public class RolesWrapper {

  List<String> roles = new ArrayList<String>();

  public RolesWrapper() {
  }

  public RolesWrapper( List<String> roles ) {
    this.roles.addAll( roles );
  }

  @XmlElement( name = "role" )
  public List<String> getRoles() {
    return roles;
  }

  public void setRoles( List<String> roles ) {
    if ( roles != this.roles ) {
      this.roles.clear();
      this.roles.addAll( roles );
    }
  }
}
