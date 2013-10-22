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
