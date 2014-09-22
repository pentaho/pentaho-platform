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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.mt.ITenant;

public class MockPentahoUser implements IPentahoUser {

  String description;
  String password;
  ITenant tenant;
  String userName;
  boolean enabled;

  public MockPentahoUser() {
  }

  public MockPentahoUser( ITenant tenant, String userName, String password, String description, boolean enabled ) {
    this.tenant = tenant;
    this.userName = userName;
    this.password = password;
    this.description = description;
    this.enabled = enabled;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public ITenant getTenant() {
    return tenant;
  }

  public void setTenant( ITenant tenant ) {
    this.tenant = tenant;
  }

  public String getUsername() {
    return userName;
  }

  public void setUsername( String userName ) {
    this.userName = userName;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled( boolean enabled ) {
    this.enabled = enabled;
  }

  public boolean equals( Object obj ) {
    if ( obj instanceof MockPentahoUser == false ) {
      return false;
    }
    if ( this == obj ) {
      return true;
    }
    MockPentahoUser rhs = (MockPentahoUser) obj;
    boolean result;
    if ( ( getTenant() == null ) && ( rhs.getTenant() == null ) ) {
      result = new EqualsBuilder().append( userName, rhs.userName ).isEquals();
    } else {
      result = new EqualsBuilder().append( userName, rhs.userName ).append( tenant, rhs.tenant ).isEquals();
    }
    return result;
  }  
  
  public int hashCode() {
    if ( ( tenant != null ) && ( tenant.getId() != null ) ) {
      return tenant.getId().concat( userName ).hashCode();
    }
    return userName.hashCode();
  }
}
