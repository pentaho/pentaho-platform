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


package org.pentaho.mantle.client.solutionbrowser.filepicklist;

import com.google.gwt.json.client.JSONObject;

public interface IFilePickItem {

  /**
   * @return the fullPath
   */
  public String getFullPath();

  /**
   * @param fullPath
   *          The full path required to access the file.
   */
  public void setFullPath( String fullPath );

  public String getTitle();

  /**
   * @param title
   *          User Friendly title to use in UI
   */
  public void setTitle( String title );

  public Long getLastUse();

  public void setLastUse( Long lastUse );

  public JSONObject toJson();

}
