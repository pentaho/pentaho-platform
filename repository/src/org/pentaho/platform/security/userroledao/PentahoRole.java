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

package org.pentaho.platform.security.userroledao;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.mt.ITenant;

/**
 * A role in the Pentaho platform. A role is also known as an authority.
 * 
 * 
 * @see PentahoUser
 * @author mlowery
 */
public class PentahoRole implements IPentahoRole {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final long serialVersionUID = 7280850318778455743L;

  private static final String FIELD_NAME = "name"; //$NON-NLS-1$
  private static final String FIELD_TENANT = "tenant"; //$NON-NLS-1$

  // ~ Instance fields
  // =================================================================================================

  private ITenant tenant;

  private String name;

  private String description;

  // ~ Constructors
  // ====================================================================================================

  protected PentahoRole() {
    // constructor reserved for use by Hibernate
  }

  public PentahoRole( String name ) {
    this( name, null );
  }

  public PentahoRole( String name, String description ) {
    this( null, name, description );
  }

  public PentahoRole( ITenant tenant, String name, String description ) {
    this.tenant = tenant;
    this.name = name;
    this.description = description;
  }

  /**
   * Copy constructor
   */
  public PentahoRole( IPentahoRole roleToCopy ) {
    this.tenant = roleToCopy.getTenant();
    this.name = roleToCopy.getName();
    this.description = roleToCopy.getDescription();
  }

  // ~ Methods
  // =========================================================================================================

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public boolean equals( Object obj ) {
    if ( obj instanceof PentahoRole == false ) {
      return false;
    }
    if ( this == obj ) {
      return true;
    }
    PentahoRole rhs = (PentahoRole) obj;
    boolean result;
    if ( ( tenant == null ) && ( rhs.tenant == null ) ) {
      result = new EqualsBuilder().append( name, rhs.name ).isEquals();
    } else {
      result = new EqualsBuilder().append( name, rhs.name ).append( tenant, rhs.tenant ).isEquals();
    }
    return result;
  }

  public int hashCode() {

    return tenant == null ? new HashCodeBuilder( 61, 167 ).append( name ).toHashCode() : new HashCodeBuilder( 61, 167 )
        .append( tenant ).append( name ).toHashCode();
  }

  public String toString() {
    return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE ).append( FIELD_TENANT, tenant ).append(
        FIELD_NAME, name ).toString();
  }

  public ITenant getTenant() {
    return tenant;
  }

}
