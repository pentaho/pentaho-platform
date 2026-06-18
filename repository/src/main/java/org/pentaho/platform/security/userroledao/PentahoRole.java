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
