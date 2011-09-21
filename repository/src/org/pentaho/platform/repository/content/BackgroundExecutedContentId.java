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
 * Copyright 2006-2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.repository.content;

import org.pentaho.platform.api.repository.IBackgroundExecutedContentId;

public class BackgroundExecutedContentId implements IBackgroundExecutedContentId {

  private static final long serialVersionUID = -6839129181355560217L;

  private String id;

  private String userName;

  private int revision = -1; // Hibernate Revision

  protected BackgroundExecutedContentId() {
    // Default empty constructor for Hibernate
  }

  protected BackgroundExecutedContentId(final String userName, final String id) {
    this.userName = userName;
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getUserName() {
    return userName;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setUserName(final String userName) {
    this.userName = userName;
  }

  public int getRevision() {
    return this.revision;
  }

  public void setRevision(final int revision) {
    this.revision = revision;
  }

}
