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


package org.pentaho.platform.api.repository2.unified.webservices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties( ignoreUnknown = true )
public class RepositoryFileAclAceDto implements Serializable {
  private static final long serialVersionUID = 3274897756057989184L;
  private String recipient;
  private String tenantPath;
  private boolean modifiable = true;

  /**
   * RepositoryFileSid.Type enum.
   */
  private int recipientType = -1;

  /**
   * RepositoryFilePermission enum.
   */
  private List<Integer> permissions;

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

  @JsonProperty( "permissions" )
  public Object getPermissionsAsStrings() {
    if ( permissions.size() == 1 ) {
      return String.valueOf( permissions.get( 0 ) );
    }
    return permissions.stream()
      .map( String::valueOf )
      .collect( Collectors.toList() );
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
