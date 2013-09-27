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

package org.pentaho.platform.core.mt;

import org.pentaho.platform.api.mt.ITenant;

public class Tenant implements ITenant {
  private static final long serialVersionUID = 1L;
  private String rootFolderAbsolutePath;

  public String getRootFolderAbsolutePath() {
    return rootFolderAbsolutePath;
  }

  public void setRootFolderAbsolutePath( String rootFolderAbsolutePath ) {
    this.rootFolderAbsolutePath = rootFolderAbsolutePath;
  }

  private boolean enabled = true;

  public Tenant() {

  }

  public Tenant( String tenantId, Boolean enabled ) {
    rootFolderAbsolutePath = tenantId;
    this.enabled = enabled;
  }

  @Override
  public String getId() {
    return rootFolderAbsolutePath;
  }

  @Override
  public String getName() {
    return rootFolderAbsolutePath.substring( rootFolderAbsolutePath.lastIndexOf( "/" ) + 1 );
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    ITenant tenant = (ITenant) obj;
    return ( this.getId() != null ) && this.getId().equals( tenant.getId() );
  }

  @Override
  public int hashCode() {
    return 31 * rootFolderAbsolutePath.hashCode();
  }

  public String toString() {
    return "TENANT ID = " + rootFolderAbsolutePath; //$NON-NLS-1$//$NON-NLS-3$
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled( boolean enabled ) {
    this.enabled = enabled;
  }

}
