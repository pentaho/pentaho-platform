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
import java.util.Date;
import java.util.List;

@XmlRootElement( name = "versionSummaryDto" )
public class VersionSummaryDto implements Serializable {
  private static final long serialVersionUID = -8333387280720917305L;

  private String message;

  private Date date;

  private String author;

  private String id;

  private String versionedFileId;

  private boolean aclOnlyChange;

  private List<String> labels = new ArrayList<String>( 0 );

  public VersionSummaryDto() {
    super();
    // TODO Auto-generated constructor stub
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public Date getDate() {
    return date;
  }

  public void setDate( Date date ) {
    this.date = date;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor( String author ) {
    this.author = author;
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getVersionedFileId() {
    return versionedFileId;
  }

  public void setVersionedFileId( String versionedFileId ) {
    this.versionedFileId = versionedFileId;
  }

  public List<String> getLabels() {
    return labels;
  }

  public void setLabels( List<String> labels ) {
    this.labels = labels;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "VersionSummaryDto [id=" + id + ", versionedFileId=" + versionedFileId + ", author=" + author + ", date="
        + date + ", labels=" + labels + ", message=" + message + "]";
  }

  public boolean isAclOnlyChange() {
    return aclOnlyChange;
  }

  public void setAclOnlyChange( boolean aclOnlyChange ) {
    this.aclOnlyChange = aclOnlyChange;
  }

}
