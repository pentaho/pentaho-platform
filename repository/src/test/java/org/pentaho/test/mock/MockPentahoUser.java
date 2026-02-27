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
