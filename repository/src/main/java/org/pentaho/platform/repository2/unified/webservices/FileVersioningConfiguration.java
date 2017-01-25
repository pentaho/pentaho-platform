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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.repository2.unified.webservices;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.api.repository2.unified.IFileVersioningConfiguration;

/**
 * Versioning information returned by the versioningConfiguration web service
 * 
 * @author tkafalas
 *
 */
@XmlRootElement
public class FileVersioningConfiguration implements IFileVersioningConfiguration {
  private boolean versioningEnabled;
  private boolean versionCommentEnabled;

  public FileVersioningConfiguration( boolean versioningEnabled, boolean versionCommentEnabled ) {
    this.versioningEnabled = versioningEnabled;
    this.versionCommentEnabled = versionCommentEnabled;
  }

  public FileVersioningConfiguration() {

  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.webservices.IFileVersioningConfiguration#isVersioningEnabled()
   */
  @Override
  public boolean isVersioningEnabled() {
    return versioningEnabled;
  }

  public void setVersioningEnabled( boolean versioningEnabled ) {
    this.versioningEnabled = versioningEnabled;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.repository2.unified.webservices.IFileVersioningConfiguration#isVersionCommentEnabled()
   */
  @Override
  public boolean isVersionCommentEnabled() {
    return versionCommentEnabled;
  }

  public void setVersionCommentEnabled( boolean versionCommentEnabled ) {
    this.versionCommentEnabled = versionCommentEnabled;
  }
}
