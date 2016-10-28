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

package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Immutable repository file. Use the {@link Builder} to create instances.
 * 
 * @author mlowery
 */
public class RepositoryFile implements Comparable<RepositoryFile>, Serializable {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final long serialVersionUID = -6955142003557786114L;

  public static final String SEPARATOR = "/"; //$NON-NLS-1$

  public static final String DEFAULT_LOCALE = "default"; //$NON-NLS-1$

  public static final String ROOT_LOCALE = DEFAULT_LOCALE; //$NON-NLS-1$

  public static final String TITLE = "title"; //$NON-NLS-1$

  public static final String FILE_TITLE = "file.title"; //$NON-NLS-1$

  public static final String DESCRIPTION = "description"; //$NON-NLS-1$

  public static final String FILE_DESCRIPTION = "file.description"; //$NON-NLS-1$

  // ~ Instance fields
  // =================================================================================================

  public static final boolean HIDDEN_BY_DEFAULT = false;
  public static final boolean SCHEDULABLE_BY_DEFAULT = true;

  public static final String HIDDEN_KEY = "_PERM_HIDDEN";
  public static final String SCHEDULABLE_KEY = "_PERM_SCHEDULABLE";

  private final String name;

  private final Serializable id;

  /**
   * Read-only.
   */
  private final Date createdDate;

  private final String creatorId;

  /**
   * Read-only.
   */
  private final Date lastModifiedDate;

  private final boolean folder;

  /**
   * Read-only.
   */
  private final String path;

  private final boolean hidden;

  private final boolean schedulable;

  private final boolean versioned;

  private final long fileSize;

  /**
   * The version name or number. Read-only.
   */
  private final Serializable versionId;

  /**
   * Locked status. Read-only.
   */
  private final boolean locked;

  /**
   * Username of the owner of the lock. Read-only. {@code null} if file not locked.
   */
  private final String lockOwner;

  /**
   * Message left by the owner when he locked the file. Read-only. {@code null} if file not locked.
   */
  private final String lockMessage;

  /**
   * The date that this lock was created. Read-only. {@code null} if file not locked.
   */
  private final Date lockDate;

  /**
   * A title for the file for the current locale. If locale not available, the file's name is returned. Read-only.
   */
  private final String title;

  /**
   * A description of the file for the current locale. Read-only.
   */
  private final String description;

  /**
   * The locale string with which locale-sensitive fields (like title) are populated. Used in {@link #equals(Object)}
   * calculation to guarantee caching works correctly. Read-only.
   */
  private final String locale;

  /**
   * The original folder path where the file resided before it was deleted. If this file has been deleted (but not
   * permanently deleted), then this field will be non-null. Read-only.
   */
  private final String originalParentFolderPath;

  /**
   * The date this file was deleted. If this file has been deleted (but not permanently deleted), then this field will
   * be non-null. Read-only.
   */
  private final Date deletedDate;

  /**
   * A map for locale properties. Keys are locale names and values are locale key-value pairs.
   */
  private final Map<String, Properties> localePropertiesMap;

  /**
   * A boolean describing if a repository file is acl node or not. The purpose of a acl node is to be a placeholder
   * for ACLs on another node which cannot have such ACLs on it's own. An example of such a scenario is one where the
   * node being shadowed is in an area of the repository which prevents any access (/etc/*). Subsystems can create a
   * acl node in this case to store and check access rules (ACLs), while still preserving the original acled node
   * in the restricted area.
   */
  private final boolean aclNode;

  // ~ Constructors
  // ===================================================================================================

  /*
   * This assumes all Serializables are immutable (because they are not defensively copied).
   */
  public RepositoryFile( Serializable id, String name, boolean folder, boolean hidden, boolean schedulable,
      boolean versioned,
      Serializable versionId, String path, Date createdDate, Date lastModifiedDate, boolean locked, String lockOwner,
      String lockMessage, Date lockDate, String locale, String title, String description,
      String originalParentFolderPath, Date deletedDate, long fileSize, String creatorId,
      Map<String, Properties> localePropertiesMap, boolean aclNode ) {
    super();
    this.id = id;
    this.name = name;
    this.folder = folder;
    this.hidden = hidden;
    this.schedulable = schedulable;
    this.versioned = versioned;
    this.versionId = versionId;
    this.path = path;
    this.createdDate = createdDate != null ? new Date( createdDate.getTime() ) : null;
    this.lastModifiedDate = lastModifiedDate != null ? new Date( lastModifiedDate.getTime() ) : null;
    this.locked = locked;
    this.lockOwner = lockOwner;
    this.lockMessage = lockMessage;
    this.lockDate = lockDate != null ? new Date( lockDate.getTime() ) : null;
    this.locale = locale;
    this.title = title;
    // this.titleMap = titleMap != null ? new HashMap<String, String>(titleMap) : null;
    this.description = description;
    // this.descriptionMap = descriptionMap != null ? new HashMap<String, String>(descriptionMap) : null;
    this.originalParentFolderPath = originalParentFolderPath;
    this.deletedDate = deletedDate != null ? new Date( deletedDate.getTime() ) : null;
    this.fileSize = fileSize;
    this.creatorId = creatorId;
    this.localePropertiesMap =
        localePropertiesMap != null ? new HashMap<String, Properties>( localePropertiesMap ) : null;
    this.aclNode = aclNode;
  }

  /*
   * This assumes all Serializables are immutable (because they are not defensively copied).
   */
  public RepositoryFile( Serializable id, String name, boolean folder, boolean hidden, boolean schedulable,
      boolean versioned,
      Serializable versionId, String path, Date createdDate, Date lastModifiedDate, boolean locked, String lockOwner,
      String lockMessage, Date lockDate, String locale, String title, String description,
      String originalParentFolderPath, Date deletedDate, long fileSize, String creatorId,
      Map<String, Properties> localePropertiesMap ) {
    this( id, name, folder, hidden, schedulable, versioned, versionId, path, createdDate, lastModifiedDate, locked,
        lockOwner,
        lockMessage, lockDate, locale, title, description, originalParentFolderPath, deletedDate, fileSize, creatorId,
        localePropertiesMap, false );
  }

  @Deprecated
  public RepositoryFile( Serializable id, String name, boolean folder, boolean hidden, boolean versioned,
      Serializable versionId, String path, Date createdDate, Date lastModifiedDate, boolean locked, String lockOwner,
      String lockMessage, Date lockDate, String locale, String title, String description,
      String originalParentFolderPath, Date deletedDate, long fileSize, String creatorId,
      Map<String, Properties> localePropertiesMap ) {
    this( id, name, folder, hidden, SCHEDULABLE_BY_DEFAULT, versioned, versionId, path, createdDate, lastModifiedDate,
        locked, lockOwner, lockMessage, lockDate, locale, title, description, originalParentFolderPath, deletedDate,
        fileSize, creatorId, localePropertiesMap, false );
  }

  // ~ Methods
  // =========================================================================================================

  public String getName() {
    return name;
  }

  public Serializable getId() {
    return id;
  }

  public Date getCreatedDate() {
    // defensive copy
    return ( createdDate != null ? new Date( createdDate.getTime() ) : null );
  }

  public String getCreatorId() {
    return creatorId;
  }

  public Date getLastModifiedDate() {
    // defensive copy
    return ( lastModifiedDate != null ? new Date( lastModifiedDate.getTime() ) : null );
  }

  public Long getFileSize() {
    return fileSize;
  }

  public boolean isFolder() {
    return folder;
  }

  public String getPath() {
    return path;
  }

  public boolean isHidden() {
    return hidden;
  }

  public boolean isSchedulable() {
    return schedulable;
  }

  public boolean isVersioned() {
    return versioned;
  }

  public Serializable getVersionId() {
    return versionId;
  }

  public boolean isLocked() {
    return locked;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public String getLockMessage() {
    return lockMessage;
  }

  public Date getLockDate() {
    // defensive copy
    return ( lockDate != null ? new Date( lockDate.getTime() ) : null );
  }

  /**
   * Returns title for current locale or file name if not available.
   */
  public String getTitle() {
    return title != null ? title : name;
  }

  public String getDescription() {
    return description;
  }

  public Map<String, Properties> getLocalePropertiesMap() {
    // defensive copy
    return localePropertiesMap == null ? null : new HashMap<String, Properties>( localePropertiesMap );
  }

  /*
   * public Map<String, String> getTitleMap() { // defensive copy return titleMap == null ? null : new HashMap<String,
   * String>(titleMap); }
   * 
   * public Map<String, String> getDescriptionMap() { // defensive copy return descriptionMap == null ? null : new
   * HashMap<String, String>(descriptionMap); }
   */

  public String getLocale() {
    return locale;
  }

  public String getOriginalParentFolderPath() {
    return originalParentFolderPath;
  }

  public Date getDeletedDate() {
    return deletedDate != null ? new Date( deletedDate.getTime() ) : null;
  }

  public boolean isAclNode() {
    return aclNode;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString( this );
  }

  public static class Builder {

    private String name;

    private Serializable id;

    private Date createdDate;

    private String creatorId;

    private Date lastModifiedDate;

    private long fileSize;

    private boolean folder;

    private String path;

    private boolean hidden;

    private boolean schedulable;

    private boolean versioned;

    private Serializable versionId;

    private boolean locked;

    private String lockOwner;

    private String lockMessage;

    private Date lockDate;

    private String title;

    private String description;

    private Map<String, Properties> localePropertiesMap;

    private String locale;

    private String originalParentFolderPath;

    private Date deletedDate;

    private boolean aclNode;

    public Builder( final String name ) {
      this.name = name;
    }

    public Builder( final Serializable id, final String name ) {
      notNull( id );
      this.name = name;
      this.id = id;
    }

    public Builder( final RepositoryFile other ) {
      this( other.getName() );
      this.id( other.getId() ).path( other.getPath() ).createdDate( other.getCreatedDate() ).creatorId(
          other.getCreatorId() ).fileSize( other.getFileSize() ).folder( other.isFolder() ).lastModificationDate(
              other.getLastModifiedDate() ).versioned( other.isVersioned() ).hidden( other.isHidden() ).schedulable(
                  other.isSchedulable() ).versionId(
          other.getVersionId() ).locked( other.isLocked() ).lockDate( other.getLockDate() ).lockOwner(
          other.getLockOwner() ).lockMessage( other.getLockMessage() ).title( other.getTitle() ).description(
          other.getDescription() ).locale( other.getLocale() ).originalParentFolderPath(
          other.getOriginalParentFolderPath() ).deletedDate( other.getDeletedDate() ).localePropertiesMap(
          other.getLocalePropertiesMap() ).aclNode( other.isAclNode() );
    }

    public RepositoryFile build() {
      return new RepositoryFile( id, name, this.folder, this.hidden, this.schedulable, this.versioned, this.versionId,
          this.path,
          this.createdDate, this.lastModifiedDate, this.locked, this.lockOwner, this.lockMessage, this.lockDate,
          this.locale, this.title, this.description, this.originalParentFolderPath, this.deletedDate, this.fileSize,
          this.creatorId, this.localePropertiesMap, this.aclNode );
    }

    public Builder createdDate( final Date createdDate1 ) {
      this.createdDate = createdDate1;
      return this;
    }

    public Builder creatorId( final String creatorId1 ) {
      this.creatorId = creatorId1;
      return this;
    }

    public Builder lastModificationDate( final Date lastModifiedDate1 ) {
      // defensive copy
      this.lastModifiedDate = lastModifiedDate1;
      return this;
    }

    /**
     * @param fileSize1
     * @return
     */
    public Builder fileSize( long fileSize1 ) {
      this.fileSize = fileSize1;
      return this;
    }

    public Builder folder( final boolean folder1 ) {
      this.folder = folder1;
      return this;
    }

    public Builder id( final Serializable id1 ) {
      this.id = id1;
      return this;
    }

    public Builder name( final String name1 ) {
      this.name = name1;
      return this;
    }

    public Builder path( final String path1 ) {
      this.path = path1;
      return this;
    }

    public Builder hidden( final boolean hidden1 ) {
      this.hidden = hidden1;
      return this;
    }

    public Builder schedulable( final boolean schedulable1 ) {
      this.schedulable = schedulable1;
      return this;
    }

    public Builder versioned( final boolean versioned1 ) {
      this.versioned = versioned1;
      return this;
    }

    public Builder versionId( final Serializable versionId1 ) {
      this.versionId = versionId1;
      return this;
    }

    public Builder locked( final boolean locked1 ) {
      this.locked = locked1;
      return this;
    }

    public Builder lockOwner( final String lockOwner1 ) {
      this.lockOwner = lockOwner1;
      return this;
    }

    public Builder lockMessage( final String lockMessage1 ) {
      this.lockMessage = lockMessage1;
      return this;
    }

    public Builder lockDate( final Date lockDate1 ) {
      // defensive copy
      this.lockDate = lockDate1;
      return this;
    }

    public Builder originalParentFolderPath( final String originalParentFolderPath1 ) {
      this.originalParentFolderPath = originalParentFolderPath1;
      return this;
    }

    public Builder deletedDate( final Date deletedDate1 ) {
      this.deletedDate = deletedDate1;
      return this;
    }

    public Builder title( final String title1 ) {
      this.title = title1;
      return this;
    }

    public Builder description( final String description1 ) {
      this.description = description1;
      return this;
    }

    public Builder localePropertiesMap( final Map<String, Properties> localePropertiesMap ) {
      this.localePropertiesMap = localePropertiesMap;
      if ( this.localePropertiesMap != null && !this.localePropertiesMap.containsKey( DEFAULT_LOCALE ) ) {
        this.localePropertiesMap.put( DEFAULT_LOCALE, new Properties() ); // required
      }
      return this;
    }

    public Builder clearLocalePropertiesMap() {
      if ( this.localePropertiesMap != null ) {
        this.localePropertiesMap.clear();
        this.localePropertiesMap.put( DEFAULT_LOCALE, new Properties() ); // required
      }
      return this;
    }

    public Builder localeProperties( final String locale, final Properties localeProperties ) {
      initLocalePropertiesMap();
      this.localePropertiesMap.put( locale, localeProperties );
      return this;
    }

    private void initLocalePropertiesMap() {
      if ( this.localePropertiesMap == null ) {
        this.localePropertiesMap = new HashMap<String, Properties>();
        this.localePropertiesMap.put( DEFAULT_LOCALE, new Properties() ); // required
      }
    }

    public Builder title( final String localeString, final String title1 ) {
      initLocalePropertiesMap();
      Properties properties = this.localePropertiesMap.get( localeString );
      if ( properties == null ) {
        properties = new Properties();
      }
      properties.put( FILE_TITLE, title1 );
      properties.put( TITLE, title1 );

      this.localePropertiesMap.put( localeString, properties );

      return this;
    }

    public Builder description( final String localeString, final String description1 ) {
      initLocalePropertiesMap();
      Properties properties = this.localePropertiesMap.get( localeString );
      if ( properties == null ) {
        properties = new Properties();
      }
      properties.put( FILE_DESCRIPTION, description1 );
      properties.put( DESCRIPTION, description1 );

      this.localePropertiesMap.put( localeString, properties );

      return this;
    }

    public Builder locale( final String locale1 ) {
      this.locale = locale1;
      return this;
    }

    public Builder aclNode( final boolean aclNode1 ) {
      this.aclNode = aclNode1;
      return this;
    }

    private void notNull( final Object in ) {
      if ( in == null ) {
        throw new IllegalArgumentException();
      }
    }

  }

  @Override
  public int compareTo( final RepositoryFile other ) {
    if ( other == null ) {
      throw new NullPointerException(); // per Comparable contract
    }
    if ( equals( other ) ) {
      return 0;
    }
    // either this or other has a null id; fall back on name
    return getTitle().compareTo( other.getTitle() );
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
    result = prime * result + ( ( locale == null ) ? 0 : locale.hashCode() );
    result = prime * result + ( ( versionId == null ) ? 0 : versionId.hashCode() );
    return result;
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
    RepositoryFile other = (RepositoryFile) obj;
    if ( id == null ) {
      if ( other.id != null ) {
        return false;
      } else if ( this.path != null ) {
        if ( !other.path.equals( this.path ) ) {
          return false;
        }
      }
    } else if ( !id.equals( other.id ) ) {
      return false;
    }
    if ( locale == null ) {
      if ( other.locale != null ) {
        return false;
      }
    } else if ( !locale.equals( other.locale ) ) {
      return false;
    }
    if ( versionId == null ) {
      if ( other.versionId != null ) {
        return false;
      }
    } else if ( !versionId.equals( other.versionId ) ) {
      return false;
    }
    return true;
  }

  @Override
  public RepositoryFile clone() {
    RepositoryFile.Builder builder = new RepositoryFile.Builder( this );
    builder.localePropertiesMap( localePropertiesMap != null ? new HashMap<String, Properties>( localePropertiesMap )
        : null );
    return builder.build();
  }

}
