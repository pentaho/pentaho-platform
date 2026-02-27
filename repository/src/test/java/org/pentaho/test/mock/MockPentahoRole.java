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
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.mt.ITenant;

public class MockPentahoRole implements IPentahoRole {

  String name;
  String description;
  ITenant tenant;

  public MockPentahoRole() {

  }

  public MockPentahoRole( ITenant tenant, String name, String description ) {
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

  public void setDescription( String arg0 ) {
    this.description = arg0;

  }

  public void setName( String name ) {
    this.name = name;
  }

  public void setTenant( ITenant tenant ) {
    this.tenant = tenant;
  }

  public boolean equals( Object obj ) {
    if ( obj instanceof MockPentahoRole == false ) {
      return false;
    }
    if ( this == obj ) {
      return true;
    }
    MockPentahoRole rhs = (MockPentahoRole) obj;
    boolean result;
    if ( ( tenant == null ) && ( rhs.tenant == null ) ) {
      result = new EqualsBuilder().append( name, rhs.name ).isEquals();
    } else {
      result = new EqualsBuilder().append( name, rhs.name ).append( tenant, rhs.tenant ).isEquals();
    }
    return result;
  }

  public int hashCode() {
    if ( ( tenant != null ) && ( tenant.getId() != null ) ) {
      return tenant.getId().concat( name ).hashCode();
    }
    return name.hashCode();
  }

}
