package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Immutable version summary for a {@code RepositoryFile}. This summary represents a single version in a 
 * RepositoryFile's version history.
 * 
 * @author mlowery
 */
public class VersionSummary implements Serializable {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  /**
   * The message the author left when he created this version.
   */
  private String message;

  /**
   * Date of creation for this version.
   */
  private Date date;

  /**
   * Username of the author of this version.
   */
  private String author;

  /**
   * The ID of this version, such as a version number like 1.3.
   */
  private Serializable id;

  /**
   * The ID of the node that is versioned.
   */
  private Serializable versionedFileId;

  /**
   * List of labels applied to this version (never {@code null}).
   */
  private List<String> labels;

  // ~ Constructors ====================================================================================================

  public VersionSummary(final Serializable id, final Serializable versionedFileId, final Date date,
      final String author, final String message, final List<String> labels) {
    super();
    assertNotNull(id);
    assertNotNull(versionedFileId);
    assertNotNull(date);
    assertHasText(author);
    assertNotNull(labels);
    this.id = id;
    this.versionedFileId = versionedFileId;
    this.date = new Date(date.getTime());
    this.author = author;
    this.message = message;
    this.labels = Collections.unmodifiableList(labels);
  }

  // ~ Methods =========================================================================================================

  public String getMessage() {
    return message;
  }

  public Date getDate() {
    return new Date(date.getTime());
  }

  public String getAuthor() {
    return author;
  }

  public Serializable getId() {
    return id;
  }

  public Serializable getVersionedFileId() {
    return versionedFileId;
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
  private void assertNotNull(final Object in) {
    if (in == null) {
      throw new IllegalArgumentException();
    }
  }

  public List<String> getLabels() {
    return labels;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    VersionSummary other = (VersionSummary) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "VersionSummary [author=" + author + ", date=" + date + ", id=" + id + ", labels=" + labels + ", message=" //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$
        + message + ", versionedFileId=" + versionedFileId + "]"; //$NON-NLS-1$//$NON-NLS-2$
  }

}
