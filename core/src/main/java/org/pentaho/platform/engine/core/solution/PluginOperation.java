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


package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.api.engine.IPluginOperation;

public class PluginOperation implements IPluginOperation {

  private String id;

  private String perspective;

  public PluginOperation( String id ) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getPerspective() {
    return perspective;
  }

  public void setPerspective( String perspective ) {
    this.perspective = perspective;
  }

}
