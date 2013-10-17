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

package org.pentaho.platform.web.http.api.resources;

import javax.xml.bind.annotation.XmlRootElement;
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
