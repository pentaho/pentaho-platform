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

  // ~ Static fields/initializers
  // ======================================================================================

  private static final long serialVersionUID = -6452375912236787125L;

  // ~ Instance fields
  // =================================================================================================

  /**
   * The message the author left when he created this version.
   */
  private final String message;

  /**
   * Date of creation for this version.
   */
  private final Date date;

  /**
   * Username of the author of this version.
   */
  private final String author;

  /**
   * The ID of this version, such as a version number like 1.3.
   */
  private final Serializable id;

  /**
   * The ID of the node that is versioned.
   */
  private final Serializable versionedFileId;

  /**
   * {@code true} if this version is the result of a change that is purely access control related. Only applicable
   * in implementations where changing access control creates a new revision.
   */
  private final boolean aclOnlyChange;

  /**
   * List of labels applied to this version (never {@code null}).
   */
  private final List<String> labels;

  // ~ Constructors
  // ====================================================================================================

  public VersionSummary( final Serializable id, final Serializable versionedFileId, final boolean aclOnlyChange,
      final Date date, final String author, final String message, final List<String> labels ) {
    super();
    notNull( id );
    notNull( versionedFileId );
    notNull( date );
    hasText( author );
    notNull( labels );
    this.id = id;
    this.versionedFileId = versionedFileId;
    this.date = new Date( date.getTime() );
    this.author = author;
    this.message = message;
    this.labels = Collections.unmodifiableList( labels );
    this.aclOnlyChange = aclOnlyChange;
  }

  // ~ Methods
  // =========================================================================================================

  public String getMessage() {
    return message;
  }

  public Date getDate() {
    return new Date( date.getTime() );
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

  protected void hasText( final String in ) {
    if ( in == null || in.length() == 0 || in.trim().length() == 0 ) {
      throw new IllegalArgumentException();
    }
  }

  private void notNull( final Object in ) {
    if ( in == null ) {
      throw new IllegalArgumentException();
    }
  }

  public List<String> getLabels() {
    return Collections.unmodifiableList( labels );
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
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
    VersionSummary other = (VersionSummary) obj;
    if ( id == null ) {
      if ( other.id != null ) {
        return false;
      }
    } else if ( !id.equals( other.id ) ) {
      return false;
    }
    return true;
  }

  public boolean isAclOnlyChange() {
    return aclOnlyChange;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "VersionSummary [id=" + id + ", versionedFileId=" + versionedFileId + ", aclOnlyChange=" + aclOnlyChange
        + ", date=" + date + ", author=" + author + ", message=" + message + ", labels=" + labels + "]";
  }

}
