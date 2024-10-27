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


package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class Overlay implements Serializable {

  private static final long serialVersionUID = 179177428881196678L;

  private String id;
  private String resourceBundleUri;
  private String source;
  private String overlayXml;
  private String overlayUri;
  private int priority;

  public Overlay() {
  }

  public Overlay( String id, String overlayUri, String source, String resourceBundleUri, int priority ) {
    this.id = id;
    this.overlayUri = overlayUri;
    this.source = source;
    this.resourceBundleUri = resourceBundleUri;
    this.priority = priority;
  }

  public String getResourceBundleUri() {
    return resourceBundleUri;
  }

  public void setResourceBundleUri( String resourceBundleUri ) {
    this.resourceBundleUri = resourceBundleUri;
  }

  public String getSource() {
    return source;
  }

  public void setSource( String source ) {
    this.source = source;
  }

  public String getOverlayXml() {
    return overlayXml;
  }

  public void setOverlayXml( String overlayXml ) {
    this.overlayXml = overlayXml;
  }

  public String getOverlayUri() {
    return overlayUri;
  }

  public void setOverlayUri( String overlayUri ) {
    this.overlayUri = overlayUri;
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority( int priority ) {
    this.priority = priority;
  }
}
