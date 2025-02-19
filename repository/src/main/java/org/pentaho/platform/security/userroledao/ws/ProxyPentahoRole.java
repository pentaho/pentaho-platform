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


package org.pentaho.platform.security.userroledao.ws;

import org.pentaho.platform.core.mt.Tenant;

import java.io.Serializable;

public class ProxyPentahoRole implements Serializable, Cloneable {

  /**
   * 
   */
  private static final long serialVersionUID = 69L;
  Tenant tenant;
  String name;
  String description;

  public ProxyPentahoRole() {
  }

  public ProxyPentahoRole( String roleName ) {
    this.name = roleName;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public boolean equals( Object o ) {
    return ( ( o instanceof ProxyPentahoRole ) ? tenant.equals( ( (ProxyPentahoUser) o ).getTenant() )
        && name.equals( ( (ProxyPentahoRole) o ).getName() ) : false );
  }

  public int hashCode() {
    return name.hashCode();
  }

  public Object clone() {
    ProxyPentahoRole o = new ProxyPentahoRole();
    o.name = name;
    o.description = description;
    o.tenant = tenant;
    return o;
  }

  public Tenant getTenant() {
    return tenant;
  }

  public void setTenant( Tenant tenant ) {
    this.tenant = tenant;
  }

}
