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

package org.pentaho.mantle.client.objects;

import org.pentaho.ui.xul.XulOverlay;

import java.io.Serializable;

public class MantleXulOverlay implements Serializable, XulOverlay {

  private static final long serialVersionUID = -8768857471764745784L;

  private String id;

  private String overlayUri;

  private String source;

  private String resourceBundleUri;

  private int priority;

  public MantleXulOverlay() {
  }

  public MantleXulOverlay( String id, String overlayUri, String source, String resourceBundleUri ) {
    this( id, overlayUri, source, resourceBundleUri, DEFAULT_PRIORITY );
  }

  public MantleXulOverlay( String id, String overlayUri, String source, String resourceBundleUri, int priority ) {
    this.id = id;
    this.overlayUri = overlayUri;
    this.source = source;
    this.resourceBundleUri = resourceBundleUri;
    this.priority = priority;
  }

  public String getId() {
    return id;
  }

  public String getOverlayUri() {
    return overlayUri;
  }

  public String getOverlayXml() {
    return getSource();
  }

  public String getResourceBundleUri() {
    return resourceBundleUri;
  }

  public String getSource() {
    return source;
  }

  public int getPriority() {
    return priority;
  }
}
