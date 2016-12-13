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
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.webservices;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

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

  boolean hidden = RepositoryFile.HIDDEN_BY_DEFAULT;

  boolean notSchedulable = !RepositoryFile.SCHEDULABLE_BY_DEFAULT;

  boolean aclNode;

  //This is the versioned property stored in the repo
  boolean versioned;

  String versionId;

  boolean locked;

  String lockOwner;

  String lockMessage;

  Date lockDate;

  String owner;

  String ownerTenantPath;

  // If versioning currently enabled for this file (Will be null if not loaded)
  Boolean versioningEnabled;

  // If version Comments are enabled for this file (Will be null if not loaded)
  Boolean versionCommentEnabled;

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

  public boolean isNotSchedulable() {
    return notSchedulable;
  }

  public void setNotSchedulable( boolean notSchedulable ) {
    this.notSchedulable = notSchedulable;
  }

  public boolean isAclNode() {
    return aclNode;
  }

  public void setAclNode( boolean aclNode ) {
    this.aclNode = aclNode;
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
    StringBuilder sb = new StringBuilder();
    sb.append( "RepositoryFileDto [id=" );
    sb.append( id );
    sb.append( ", name=" );
    sb.append( name );
    sb.append( ", path=" );
    sb.append( path );
    sb.append( ", folder=" );
    sb.append( folder );
    sb.append( ", size=" );
    sb.append( fileSize );
    sb.append( ", createdDate=" );
    sb.append( createdDate );
    sb.append( ", creatorId=" );
    sb.append( creatorId );
    sb.append( ", deletedDate=" );
    sb.append( deletedDate );
    sb.append( ", description=" );
    sb.append( description );
    sb.append( ", hidden=" );
    sb.append( hidden );
    sb.append( ", notSchedulable=" );
    sb.append( notSchedulable );
    sb.append( ", aclNode=" );
    sb.append( aclNode );
    sb.append( ", lastModifiedDate=" );
    sb.append( lastModifiedDate );
    sb.append( ", locale=" );
    sb.append( locale );
    sb.append( ", lockDate=" );
    sb.append( lockDate );
    sb.append( ", lockMessage=" );
    sb.append( lockMessage );
    sb.append( ", lockOwner=" );
    sb.append( lockOwner );
    sb.append( ", locked=" );
    sb.append( locked );
    sb.append( ", originalParentFolderPath=" );
    sb.append( originalParentFolderPath );
    sb.append( ", owner=" );
    sb.append( owner );
    sb.append( ", ownerType=" );
    sb.append( ownerType );
    sb.append( ", title=" );
    sb.append( title );
    sb.append( ", localePropertiesMapEntries=" );
    sb.append( localePropertiesMapEntries );
    sb.append( ", versionId=" );
    sb.append( versionId );
    sb.append( ", versioned=" );
    sb.append( versioned );
    sb.append( ", versioningEnabled=" );
    sb.append( versioningEnabled );
    sb.append( ", versionCommentEnabled=" );
    sb.append( versionCommentEnabled );
    sb.append( ", hasAcl=" );
    sb.append( ( repositoryFileAclDto != null ) );
    sb.append( "]" );
    return sb.toString();
  }

  public Boolean getVersioningEnabled() {
    return versioningEnabled;
  }

  public void setVersioningEnabled( Boolean versioningEnabled ) {
    this.versioningEnabled = versioningEnabled;
  }

  public Boolean getVersionCommentEnabled() {
    return versionCommentEnabled;
  }

  public void setVersionCommentEnabled( Boolean versionCommentEnabled ) {
    this.versionCommentEnabled = versionCommentEnabled;
  }

}
