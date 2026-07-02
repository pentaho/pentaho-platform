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

/**
 * Represents versioning information about a repository file.
 * @author tkafalas
 *
 */
public interface IFileVersioningConfiguration {

  /**
   * True if new versions should be created when saved.
   * False if versioning is disabled for this file.
   * @return true or false
   */
  boolean isVersioningEnabled();

  /**
   * True if the UI should prompt for a version comment when the
   * file is created/saved.  False if comments are disabled.
   * @return true or false
   */
  boolean isVersionCommentEnabled();

}
