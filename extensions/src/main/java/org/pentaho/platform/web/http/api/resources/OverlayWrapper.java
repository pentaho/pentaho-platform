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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.List;


@XmlAccessorType( XmlAccessType.FIELD )
public class OverlayWrapper {

  @XmlElement( name = "overlay" )
  private List<Overlay> overlays;

  public OverlayWrapper() {

  }

  public OverlayWrapper( List<Overlay> overlays ) {
    this.overlays = overlays;
  }

  public List<Overlay> getOverlays() {
    return overlays;
  }

  public void setOverlays( List<Overlay> overlays ) {
    this.overlays = overlays;
  }
}
