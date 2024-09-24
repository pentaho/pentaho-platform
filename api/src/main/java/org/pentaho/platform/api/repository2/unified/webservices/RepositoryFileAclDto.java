/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.repository2.unified.webservices;

import javax.xml.bind.annotation.XmlRootElement;
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
