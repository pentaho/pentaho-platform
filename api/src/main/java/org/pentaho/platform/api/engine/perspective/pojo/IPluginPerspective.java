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


package org.pentaho.platform.api.engine.perspective.pojo;

import org.pentaho.ui.xul.XulOverlay;

import java.io.Serializable;
import java.util.ArrayList;

public interface IPluginPerspective extends Serializable {

  public String getId();

  public void setId( String id );

  public String getTitle();

  public void setTitle( String title );

  public String getContentUrl();

  public void setContentUrl( String contentUrl );

  public String getResourceBundleUri();

  public void setResourceBundleUri( String uri );

  public ArrayList<XulOverlay> getOverlays();

  public void setOverlays( ArrayList<XulOverlay> overlays );

  public int getLayoutPriority();

  public void setLayoutPriority( int layoutPriority );

  public ArrayList<String> getRequiredSecurityActions();

  public void setRequiredSecurityActions( ArrayList<String> actions );

}
