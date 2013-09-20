/*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU General Public License, version 2 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
*
* Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
*/

package org.pentaho.test.mock;

import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.mt.ITenant;

public class MockPentahoRole implements IPentahoRole {

  String name;
  String description ;
  ITenant tenant;
  
  
  public MockPentahoRole () {
    
  }
  
  public MockPentahoRole(ITenant tenant, String name, String description) {
    this.tenant = tenant;
    this.name = name;
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public ITenant getTenant() {
    return tenant;
  }

  public void setDescription(String arg0) {
    this.description = arg0;

  }

  public void setName(String name) {
    this.name = name;
  }

  public void setTenant(ITenant tenant) {
    this.tenant = tenant;
  }

  public int hashCode() {
    if ((tenant != null) && (tenant.getId() != null)) {
      return tenant.getId().concat(name).hashCode();
    }
    return name.hashCode();
  }
  
  
}
