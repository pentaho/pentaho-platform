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
