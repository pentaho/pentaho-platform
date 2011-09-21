package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Immutable repository file. Use the {@link Builder} to create instances.
 * 
 * <p>
 * This class should use only GWT-emulated types.
 * </p>
 * 
 * @author mlowery
 */
public class RepositoryFile implements Comparable<RepositoryFile>, Serializable {

  // ~ Static fields/initializers ======================================================================================

  private static final long serialVersionUID = -6955142003557786114L;

  public static final String SEPARATOR = "/"; //$NON-NLS-1$

  /**
   * Key used in {@link #titleMap} or {@link #descriptionMap} that indicates what string to use when no locale 
   * information is available.
   */
  public static final String ROOT_LOCALE = "rootLocale"; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  private String name;

  private Serializable id;

  /**
   * Read-only.
   */
  private Date createdDate;

  private String creatorId;

  /**
   * Read-only.
   */
  private Date lastModifiedDate;

  private boolean folder;

  /**
   * Read-only.
   */
  private String path;

  private boolean hidden;

  private boolean versioned;

  private long fileSize;

  /**
   * The version name or number. Read-only.
   */
  private Serializable versionId;

  /**
   * Locked status. Read-only.
   */
  private boolean locked;

  /**
   * Username of the owner of the lock. Read-only. {@code null} if file not locked.
   */
  private String lockOwner;

  /**
   * Message left by the owner when he locked the file. Read-only. {@code null} if file not locked.
   */
  private String lockMessage;

  /**
   * The date that this lock was created. Read-only. {@code null} if file not locked.
   */
  private Date lockDate;

  /**
   * The owner of this file. Usually plays a role in access control. Read-only.
   */
  private RepositoryFileSid owner;

  /**
   * A title for the file for the current locale. If locale not available, the file's name is returned. Read-only.
   */
  private String title;

  /**
   * A description of the file for the current locale. Read-only.
   */
  private String description;

  /**
   * A map for titles. Keys are locale strings and values are titles. Write-only. {@code null} value means that no 
   * title will be created or updated.
   */
  private Map<String, String> titleMap;

  /**
   * A map for descriptions. Keys are locale strings and values are descriptions. Write-only. {@code null} value means 
   * that no description will be created or updated.
   */
  private Map<String, String> descriptionMap;

  /**
   * The locale string with which locale-sensitive fields (like title) are populated. Used in {@link #equals(Object)} 
   * calculation to guarantee caching works correctly. Read-only.
   */
  private String locale;

  /**
   * The original folder path where the file resided before it was deleted. If this file has been deleted (but not 
   * permanently deleted), then this field will be non-null. Read-only.
   */
  private String originalParentFolderPath;

  /**
   * The date this file was deleted. If this file has been deleted (but not permanently deleted), then this field will 
   * be non-null. Read-only.
   */
  private Date deletedDate;

  // ~ Constructors ===================================================================================================

  public RepositoryFile(final String name) {
    super();
    this.name = name;
  }

  public RepositoryFile(final Serializable id, final String name) {
    super();
    this.name = name;
    this.id = id;
  }

  // ~ Methods =========================================================================================================

  public String getName() {
    return name;
  }

  public Serializable getId() {
    return id;
  }

  public Date getCreatedDate() {
    // defensive copy
    return (createdDate != null ? new Date(createdDate.getTime()) : null);
  }

  public String getCreatorId() {
    return creatorId;
  }

  public Date getLastModifiedDate() {
    // defensive copy
    return (lastModifiedDate != null ? new Date(lastModifiedDate.getTime()) : null);
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
    return (lockDate != null ? new Date(lockDate.getTime()) : null);
  }

  public RepositoryFileSid getOwner() {
    return owner;
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

  public Map<String, String> getTitleMap() {
    // defensive copy
    return titleMap == null ? null : new HashMap<String, String>(titleMap);
  }

  public Map<String, String> getDescriptionMap() {
    // defensive copy
    return descriptionMap == null ? null : new HashMap<String, String>(descriptionMap);
  }

  public String getLocale() {
    return locale;
  }

  public String getOriginalParentFolderPath() {
    return originalParentFolderPath;
  }

  public Date getDeletedDate() {
    return deletedDate;
  }

  @Override
  public String toString() {
    // TODO mlowery remove this to be GWT-compatible
    return ToStringBuilder.reflectionToString(this);
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

    private boolean versioned;

    private Serializable versionId;

    private boolean locked;

    private String lockOwner;

    private String lockMessage;

    private Date lockDate;

    private RepositoryFileSid owner;

    private String title;

    private String description;

    private Map<String, String> titleMap;

    private Map<String, String> descriptionMap;

    private String locale;

    private String originalParentFolderPath;

    private Date deletedDate;

    public Builder(final String name) {
      assertHasText(name);
      this.name = name;
      this.clearTitleMap();
    }

    public Builder(final Serializable id, final String name) {
      assertNotNull(id);
      this.name = name;
      this.id = id;
      this.clearTitleMap();
    }

    public Builder(final RepositoryFile other) {
      this(other.id, other.name);
      this.path(other.path).createdDate(other.createdDate).creatorId(other.creatorId).fileSize(other.fileSize).folder(other.folder).lastModificationDate(
          other.lastModifiedDate).versioned(other.versioned).hidden(other.hidden).versionId(other.versionId).locked(
          other.locked).lockDate(other.lockDate).lockOwner(other.lockOwner).lockMessage(other.lockMessage).owner(
          other.owner).title(other.title).description(other.description).titleMap(other.titleMap).descriptionMap(
          other.descriptionMap).locale(other.locale).originalParentFolderPath(other.originalParentFolderPath)
          .deletedDate(other.deletedDate);
    }

    public RepositoryFile build() {
      RepositoryFile result = new RepositoryFile(id, name);
      result.createdDate = this.createdDate;
      result.creatorId = this.creatorId;
      result.lastModifiedDate = this.lastModifiedDate;
      result.folder = this.folder;
      result.fileSize = this.fileSize;
      result.path = this.path;
      result.hidden = this.hidden;
      result.versioned = this.versioned;
      result.versionId = this.versionId;
      result.locked = this.locked;
      result.lockOwner = this.lockOwner;
      result.lockMessage = this.lockMessage;
      result.lockDate = this.lockDate;
      result.owner = this.owner;
      result.title = this.title;
      result.description = this.description;
      result.titleMap = this.titleMap;
      result.descriptionMap = this.descriptionMap;
      result.locale = this.locale;
      result.originalParentFolderPath = this.originalParentFolderPath;
      result.deletedDate = this.deletedDate;
      return result;
    }

    public Builder createdDate(final Date createdDate) {
      // defensive copy
      this.createdDate = (createdDate != null ? new Date(createdDate.getTime()) : null);
      return this;
    }

    public Builder creatorId(final String creatorId) {
      this.creatorId = creatorId;
      return this;
    }

    public Builder lastModificationDate(final Date lastModifiedDate) {
      // defensive copy
      this.lastModifiedDate = (lastModifiedDate != null ? new Date(lastModifiedDate.getTime()) : null);
      return this;
    }

    /**
     * @param length
     * @return
     */
    public Builder fileSize(long fileSize) {
      this.fileSize = fileSize;
      return this;
    }

    public Builder folder(final boolean folder) {
      this.folder = folder;
      return this;
    }

    public Builder path(final String path) {
      this.path = path;
      return this;
    }

    public Builder hidden(final boolean hidden) {
      this.hidden = hidden;
      return this;
    }

    public Builder versioned(final boolean versioned) {
      this.versioned = versioned;
      return this;
    }

    public Builder versionId(final Serializable versionId) {
      this.versionId = versionId;
      return this;
    }

    public Builder locked(final boolean locked) {
      this.locked = locked;
      return this;
    }

    public Builder lockOwner(final String lockOwner) {
      this.lockOwner = lockOwner;
      return this;
    }

    public Builder lockMessage(final String lockMessage) {
      this.lockMessage = lockMessage;
      return this;
    }

    public Builder lockDate(final Date lockDate) {
      // defensive copy
      this.lockDate = (lockDate != null ? new Date(lockDate.getTime()) : null);
      return this;
    }

    public Builder owner(final RepositoryFileSid owner) {
      this.owner = owner;
      return this;
    }

    public Builder originalParentFolderPath(final String originalParentFolderPath) {
      this.originalParentFolderPath = originalParentFolderPath;
      return this;
    }

    public Builder deletedDate(final Date deletedDate) {
      this.deletedDate = (deletedDate != null ? new Date(deletedDate.getTime()) : null);
      return this;
    }

    public Builder title(final String title) {
      this.title = title;
      return this;
    }

    public Builder description(final String description) {
      this.description = description;
      return this;
    }

    public Builder titleMap(final Map<String, String> titleMap) {
      // defensive copy
      this.titleMap = (titleMap != null ? new HashMap<String, String>(titleMap) : null);
      return this;
    }

    public Builder clearTitleMap() {
      if (this.titleMap != null) {
        this.titleMap.clear();
      }
      return this;
    }

    public Builder title(final String localeString, final String title) {
      initTitleMap();
      this.titleMap.put(localeString, title);
      return this;
    }

    private void initTitleMap() {
      if (this.titleMap == null) {
        this.titleMap = new HashMap<String, String>();
        this.titleMap.put(ROOT_LOCALE, this.name);
      }
    }

    public Builder descriptionMap(final Map<String, String> descriptionMap) {
      // defensive copy
      this.descriptionMap = (descriptionMap != null ? new HashMap<String, String>(descriptionMap) : descriptionMap);
      return this;
    }

    public Builder clearDescriptionMap() {
      if (this.descriptionMap != null) {
        this.descriptionMap.clear();
      }
      return this;
    }

    public Builder description(final String localeString, final String description) {
      initDescriptionMap();
      this.descriptionMap.put(localeString, description);
      return this;
    }

    private void initDescriptionMap() {
      if (this.descriptionMap == null) {
        this.descriptionMap = new HashMap<String, String>();
      }
    }

    public Builder locale(final String locale) {
      this.locale = locale;
      return this;
    }

    /**
     * Implemented here to maintain GWT-compatibility.
     */
    protected void assertHasText(final String in) {
      if (in == null || in.length() == 0 || in.trim().length() == 0) {
        throw new IllegalArgumentException();
      }
    }

    /**
     * Implemented here to maintain GWT-compatibility.
     */
    protected void assertTrue(final boolean expression) {
      if (!expression) {
        throw new IllegalArgumentException();
      }
    }

    /**
     * Implemented here to maintain GWT-compatibility.
     */
    private void assertNotNull(final Object in) {
      if (in == null) {
        throw new IllegalArgumentException();
      }
    }

  }

  public int compareTo(final RepositoryFile other) {
    if (other == null) {
      throw new NullPointerException(); // per Comparable contract
    }
    if (equals(other)) {
      return 0;
    }
    // either this or other has a null id; fall back on name
    return getTitle().compareTo(other.getTitle());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((locale == null) ? 0 : locale.hashCode());
    result = prime * result + ((versionId == null) ? 0 : versionId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RepositoryFile other = (RepositoryFile) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (locale == null) {
      if (other.locale != null)
        return false;
    } else if (!locale.equals(other.locale))
      return false;
    if (versionId == null) {
      if (other.versionId != null)
        return false;
    } else if (!versionId.equals(other.versionId))
      return false;
    return true;
  }

}
