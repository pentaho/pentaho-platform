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

package org.pentaho.platform.repository2.unified.webservices;

import java.io.Serializable;
import java.util.List;

public class RepositoryFileAclAceDto implements Serializable {
  private static final long serialVersionUID = 3274897756057989184L;
  String recipient;
  String tenantPath;
  boolean modifiable = true;

  /**
   * RepositoryFileSid.Type enum.
   */
  int recipientType = -1;

  /**
   * RepositoryFilePermission enum.
   */
  List<Integer> permissions;

  public RepositoryFileAclAceDto() {
    super();
    // TODO Auto-generated constructor stub
  }

  public String getTenantPath() {
    return tenantPath;
  }

  public void setTenantPath( String tenantPath ) {
    this.tenantPath = tenantPath;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient( String recipient ) {
    this.recipient = recipient;
  }

  public int getRecipientType() {
    return recipientType;
  }

  public void setRecipientType( int recipientType ) {
    this.recipientType = recipientType;
  }

  public List<Integer> getPermissions() {
    return permissions;
  }

  public void setPermissions( List<Integer> permissions ) {
    this.permissions = permissions;
  }
  
  public boolean isModifiable() {
    return modifiable;
  }

  public void setModifiable( boolean modifiable ) {
    this.modifiable = modifiable;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "RepositoryFileAclAceDto [recipient=" + recipient + ", recipientType=" + recipientType + ", permissions="
        + permissions + ", modifiable=" + modifiable + "]";
  }
}
