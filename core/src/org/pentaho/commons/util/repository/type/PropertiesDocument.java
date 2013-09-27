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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.commons.util.repository.type;

public class PropertiesDocument {

  public static final String OBJECTID = "ObjectId"; //$NON-NLS-1$
  public static final String URI = "Uri"; //$NON-NLS-1$
  public static final String OBJECTTYPEID = "ObjectTypeId"; //$NON-NLS-1$
  public static final String CREATEDBY = "CreatedBy"; //$NON-NLS-1$
  public static final String CREATIONDATE = "CreationDate"; //$NON-NLS-1$
  public static final String LASTMODIFIEDBY = "LastModifiedBy"; //$NON-NLS-1$
  public static final String LASTMODIFICATIONDATE = "LastModificationDate"; //$NON-NLS-1$
  public static final String CHANGETOKEN = "ChangeToken"; //$NON-NLS-1$
  public static final String ISIMMUTABLE = "IsImmutable"; //$NON-NLS-1$
  public static final String ISLATESTVERSION = "isLatestVersion"; //$NON-NLS-1$
  public static final String ISMAJORVERSION = "IsMajorVersion"; //$NON-NLS-1$
  public static final String ISLATESTMAJORVERSION = "IsLatestMajorVersion"; //$NON-NLS-1$
  public static final String VERSIONLABEL = "VersionLabel"; //$NON-NLS-1$
  public static final String VERSIONSERIESID = "VersionSeriesId"; //$NON-NLS-1$
  public static final String ISVERRSIONSERIESCHECKEDOUT = "IsVersionSeriesCheckedOut"; //$NON-NLS-1$
  public static final String VERSIONSERIESCHECKEDOUTBY = "VersionSeriesCheckedOutBy"; //$NON-NLS-1$
  public static final String VERSIONSERIESCHECKEDOUTID = "VersionSeriesCheckedOutId"; //$NON-NLS-1$
  public static final String CHECKINCOMMENT = "CheckinComment"; //$NON-NLS-1$
  public static final String CONTENTSTREAMALLOWED = "ContentStreamAllowed"; //$NON-NLS-1$
  public static final String CONTENTSTREAMLENGTH = "ContentStreamLength"; //$NON-NLS-1$
  public static final String CONTENTSTREAMMIMETYPE = "ContentStreamMimeType"; //$NON-NLS-1$
  public static final String CONTENTSTREAMFILENAME = "ContentStreamFilename"; //$NON-NLS-1$
  public static final String CONTENTSTREAMURI = "ContentStreamUri"; //$NON-NLS-1$

  private String value;

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }
}
