/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "RoleExport", propOrder = { "rolename", "permissions" } )
public class RoleExport {
  @XmlElement( name = "rolename" )
  private String rolename;

  @XmlElement( name = "permissions" )
  List<String> permissions = new ArrayList<String>();

  public String getRolename() {
    return rolename;
  }

  public void setRolename( String rolename ) {
    this.rolename = rolename;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  public void setPermission( List<String> permissions ) {
    this.permissions = permissions;
  }
}
