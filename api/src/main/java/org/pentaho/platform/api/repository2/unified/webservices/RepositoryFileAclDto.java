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

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class RepositoryFileAclDto implements Serializable {
  private static final long serialVersionUID = -3534878030681136321L;

  private List<RepositoryFileAclAceDto> aces = new ArrayList<RepositoryFileAclAceDto>( 0 );

  private String id;

  private String owner;

  private String tenantPath;

  /**
   * RepositoryFileSid.Type enum.
   */
  private int ownerType = -1;

  private boolean entriesInheriting = true;

  public RepositoryFileAclDto() {
    super();
    // TODO Auto-generated constructor stub
  }

  public List<RepositoryFileAclAceDto> getAces() {
    return aces;
  }

  public void setAces( List<RepositoryFileAclAceDto> aces, boolean inheriting ) {
    entriesInheriting = inheriting;
    this.aces = aces;
  }
  @Deprecated
  public void setAces( List<RepositoryFileAclAceDto> aces ) {
    setAces( aces, false );
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner( String owner ) {
    this.owner = owner;
  }

  public int getOwnerType() {
    return ownerType;
  }

  public void setOwnerType( int ownerType ) {
    this.ownerType = ownerType;
  }

  public boolean isEntriesInheriting() {
    return entriesInheriting;
  }

  public void setEntriesInheriting( boolean entriesInheriting ) {
    this.entriesInheriting = entriesInheriting;
  }

  public String getTenantPath() {
    return tenantPath;
  }

  public void setTenantPath( String tenantPath ) {
    this.tenantPath = tenantPath;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "RepositoryFileAclDto [id=" + id + ", entriesInheriting=" + entriesInheriting + ", owner=" + owner
        + ", ownerType=" + ownerType + ", aces=" + aces + "]";
  }

}
