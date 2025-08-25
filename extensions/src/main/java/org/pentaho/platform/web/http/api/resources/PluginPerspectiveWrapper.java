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

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessType;

import java.util.List;

@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
public class PluginPerspectiveWrapper {

  @XmlElement( name = "pluginPerspective" )
  private List<PluginPerspective> pluginPerspectives;

  public PluginPerspectiveWrapper() {

  }

  public PluginPerspectiveWrapper( List<PluginPerspective> pluginPerspectives ) {
    this.pluginPerspectives = pluginPerspectives;
  }

  public List<PluginPerspective> getPluginPerspectives() {
    return pluginPerspectives;
  }

  public void setPluginPerspectives( List<PluginPerspective> pluginPerspectives ) {
    this.pluginPerspectives = pluginPerspectives;
  }
}
