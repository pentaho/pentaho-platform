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
