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

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement( name = "roleList" )
public class RoleListWrapper {
  List<String> roles = new ArrayList<String>();

  public RoleListWrapper() {
  }

  public RoleListWrapper( List<IPentahoRole> roles ) {
    List<String> roleList = new ArrayList<String>();
    for ( IPentahoRole role : roles ) {
      roleList.add( role.getName() );
    }
    this.roles.addAll( roleList );
  }

  public RoleListWrapper( Collection<String> roles ) {
    this.roles.addAll( roles );
  }

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
