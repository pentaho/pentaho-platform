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
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * JAXB-safe version of {@code RepositoryFile}. ({@code RepositoryFile} has no zero-arg constructor and no public
 * mutators.)
 * 
 * @see RepositoryFileAdapter
 * 
 * @author mlowery
 */
@XmlRootElement
public class RepositoryFileDto implements Serializable {
  private static final long serialVersionUID = 3578911355440278525L;

  String name;

  String id;

  Date createdDate;

  String creatorId;

  Date lastModifiedDate;

  long fileSize;

  boolean folder;

  String path;

  boolean hidden;

  boolean versioned;

  String versionId;

  boolean locked;

  String lockOwner;

  String lockMessage;

  Date lockDate;

  String owner;

  String ownerTenantPath;

  /**
   * RepositoryFileSid.Type enum.
   */
  int ownerType = -1;

  String title;

  String description;

  String locale;

  String originalParentFolderPath;

  Date deletedDate;

  List<LocaleMapDto> localePropertiesMapEntries;
  
  RepositoryFileAclDto repositoryFileAclDto;

  public RepositoryFileDto() {
    super();
    // TODO Auto-generated constructor stub
  }

  public String getOwnerTenantPath() {
    return ownerTenantPath;
  }

  public void setOwnerTenantPath( String ownerTenantPath ) {
    this.ownerTenantPath = ownerTenantPath;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  @XmlJavaTypeAdapter( value = DateAdapter.class )
  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate( Date createdDate ) {
    this.createdDate = createdDate;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId( String creatorId ) {
    this.creatorId = creatorId;
  }

  @XmlJavaTypeAdapter( value = DateAdapter.class )
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate( Date lastModifiedDate ) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize( long fileSize ) {
    this.fileSize = fileSize;
  }

  public boolean isFolder() {
    return folder;
  }

  public void setFolder( boolean folder ) {
    this.folder = folder;
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  public boolean isVersioned() {
    return versioned;
  }

  public void setVersioned( boolean versioned ) {
    this.versioned = versioned;
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId( String versionId ) {
    this.versionId = versionId;
  }

  public boolean isLocked() {
    return locked;
  }

  public void setLocked( boolean locked ) {
    this.locked = locked;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public void setLockOwner( String lockOwner ) {
    this.lockOwner = lockOwner;
  }

  public String getLockMessage() {
    return lockMessage;
  }

  public void setLockMessage( String lockMessage ) {
    this.lockMessage = lockMessage;
  }

  @XmlJavaTypeAdapter( value = DateAdapter.class )
  public Date getLockDate() {
    return lockDate;
  }

  public void setLockDate( Date lockDate ) {
    this.lockDate = lockDate;
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

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale( String locale ) {
    this.locale = locale;
  }

  public String getOriginalParentFolderPath() {
    return originalParentFolderPath;
  }

  public void setOriginalParentFolderPath( String originalParentFolderPath ) {
    this.originalParentFolderPath = originalParentFolderPath;
  }

  @XmlJavaTypeAdapter( value = DateAdapter.class )
  public Date getDeletedDate() {
    return deletedDate;
  }

  public void setDeletedDate( Date deletedDate ) {
    this.deletedDate = deletedDate;
  }
  
  public RepositoryFileAclDto getRepositoryFileAclDto() {
    return repositoryFileAclDto;
  }

  public void setRepositoryFileAclDto( RepositoryFileAclDto repositoryFileAclDto ) {
    this.repositoryFileAclDto = repositoryFileAclDto;
  }

  public List<LocaleMapDto> getLocalePropertiesMapEntries() {
    return localePropertiesMapEntries;
  }

  public void setLocalePropertiesMapEntries( List<LocaleMapDto> localePropertiesMapEntries ) {
    this.localePropertiesMapEntries = localePropertiesMapEntries;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "RepositoryFileDto [id=" + id + ", name=" + name + ", path=" + path + ", folder=" + folder + ", size="
        + fileSize + ", createdDate=" + createdDate + ", creatorId=" + creatorId + ", deletedDate=" + deletedDate
        + ", description=" + description + ", hidden=" + hidden + ", lastModifiedDate=" + lastModifiedDate
        + ", locale=" + locale + ", lockDate=" + lockDate + ", lockMessage=" + lockMessage + ", lockOwner=" + lockOwner
        + ", locked=" + locked + ", originalParentFolderPath=" + originalParentFolderPath + ", owner=" + owner
        + ", ownerType=" + ownerType + ", title=" + title + ", localePropertiesMapEntries="
        + localePropertiesMapEntries + ", versionId=" + versionId + ", versioned=" + versioned + ", hasAcl=" + (repositoryFileAclDto != null) + "]";
  }

}
