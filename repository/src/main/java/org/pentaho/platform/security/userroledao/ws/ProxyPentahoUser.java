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

public class ProxyPentahoUser implements Serializable, Cloneable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String name;

  private String password = ""; //$NON-NLS-1$

  private String description = ""; //$NON-NLS-1$

  private boolean enabled = true;

  private Tenant tenant;

  public ProxyPentahoUser() {
  }

  public String getName() {
    return name;
  }

  public String getPassword() {
    return password;
  }

  public boolean getEnabled() {
    return enabled;
  }

  public String getDescription() {
    return description;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public void setEnabled( boolean enabled ) {
    this.enabled = enabled;
  }

  public boolean equals( Object o ) {
    return ( ( o instanceof ProxyPentahoUser ) ? tenant.equals( ( (ProxyPentahoUser) o ).getTenant() )
        && name.equals( ( (ProxyPentahoUser) o ).getName() ) : false );
  }

  public int hashCode() {
    return name.hashCode();
  }

  public Object clone() {
    ProxyPentahoUser o = new ProxyPentahoUser();
    o.name = name;
    o.password = password;
    o.description = description;
    o.enabled = enabled;
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
