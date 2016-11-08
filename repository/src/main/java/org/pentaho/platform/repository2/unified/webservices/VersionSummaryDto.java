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

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement(name="versionSummaryDto")
public class VersionSummaryDto implements Serializable {
  private static final long serialVersionUID = -8333387280720917305L;

  String message;

  Date date;

  String author;

  String id;

  String versionedFileId;

  boolean aclOnlyChange;

  List<String> labels = new ArrayList<String>( 0 );

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

  private boolean isAclOnlyChange() {
    return aclOnlyChange;
  }

  private void setAclOnlyChange( boolean aclOnlyChange ) {
    this.aclOnlyChange = aclOnlyChange;
  }

}
