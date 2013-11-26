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
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.mt.ITenant;

/**
 * A user of the Pentaho platform.
 * 
 * @see PentahoRole
 * @author mlowery
 */
public class PentahoUser implements IPentahoUser {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final long serialVersionUID = 3647003745944124252L;

  private static final String FIELD_ENABLED = "enabled"; //$NON-NLS-1$

  private static final String PASSWORD_MASK = "[PROTECTED]"; //$NON-NLS-1$

  private static final String FIELD_PASSWORD = "password"; //$NON-NLS-1$

  private static final String FIELD_USERNAME = "username"; //$NON-NLS-1$

  private static final String FIELD_TENANT = "tenant"; //$NON-NLS-1$

  private static final String FIELD_DESCRIPTION = "description"; //$NON-NLS-1$

  // ~ Instance fields
  // =================================================================================================

  private String username;

  private ITenant tenant;

  private String password;

  private String description;

  private boolean enabled = true;

  // ~ Constructors
  // ====================================================================================================

  protected PentahoUser() {
    // constructor reserved for use by Hibernate
  }

  public PentahoUser( String username ) {
    this( username, null, null, true );
  }

  public PentahoUser( String username, String password, String description, boolean enabled ) {
    this.username = username;
    this.password = password;
    this.description = description;
    this.enabled = enabled;
  }

  public PentahoUser( ITenant tenant, String username, String password, String description, boolean enabled ) {
    this.tenant = tenant;
    this.username = username;
    this.password = password;
    this.description = description;
    this.enabled = enabled;
  }

  /**
   * Copy constructor
   */
  public PentahoUser( IPentahoUser userToCopy ) {
    this.username = userToCopy.getUsername();
    this.description = userToCopy.getDescription();
    this.enabled = userToCopy.isEnabled();
  }

  // ~ Methods
  // =========================================================================================================

  public String getUsername() {
    return username;
  }

  public ITenant getTenant() {
    return this.tenant;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled( boolean enabled ) {
    this.enabled = enabled;
  }

  public boolean equals( Object obj ) {
    if ( obj instanceof PentahoUser == false ) {
      return false;
    }
    if ( this == obj ) {
      return true;
    }
    PentahoUser rhs = (PentahoUser) obj;
    boolean result;
    if ( ( getTenant() == null ) && ( rhs.getTenant() == null ) ) {
      result = new EqualsBuilder().append( username, rhs.username ).isEquals();
    } else {
      result = new EqualsBuilder().append( username, rhs.username ).append( tenant, rhs.tenant ).isEquals();
    }
    return result;
  }

  public int hashCode() {
    return tenant == null ? new HashCodeBuilder( 71, 223 ).append( username ).toHashCode() : new HashCodeBuilder( 71,
        223 ).append( tenant ).append( username ).toHashCode();
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String toString() {
    return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE ).append( FIELD_TENANT, tenant ).append(
        FIELD_USERNAME, username ).append( FIELD_PASSWORD, PASSWORD_MASK ).append( FIELD_DESCRIPTION, description )
        .append( FIELD_ENABLED, enabled ).toString();
  }
}
