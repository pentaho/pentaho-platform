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


package org.pentaho.platform.api.engine;

/**
 * This should only be used internally by the plugin manager
 */
public interface IContentGeneratorInfo {

  public String getDescription();

  public String getId();

  /**
   * @deprecated URL is determined by the system
   */
  @Deprecated
  public String getUrl();

  public String getTitle();

  public String getType();

  /**
   * @deprecated file info generators or solution file meta providers are now associated with content types, not
   *             content generators. This method is not called from within the platform.
   */
  @Deprecated
  public String getFileInfoGeneratorClassname();

  public String getClassname();
}
