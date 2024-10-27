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


package org.pentaho.platform.repository2.unified.webservices;

import jakarta.xml.bind.annotation.XmlRootElement;

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
