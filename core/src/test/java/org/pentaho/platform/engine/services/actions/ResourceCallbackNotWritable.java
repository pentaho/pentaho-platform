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


package org.pentaho.platform.engine.services.actions;

import java.util.List;

import org.pentaho.platform.api.action.IAction;

public class ResourceCallbackNotWritable implements IAction {

  private List<String> inputNames;
  private List<String> outputNames;

  public List<String> getInputNames() {
    return inputNames;
  }

  public void setInputNames( List<String> inputNames ) {
    this.inputNames = inputNames;
  }

  public List<String> getOutputNames() {
    return outputNames;
  }

  public void setOutputNames( List<String> outputNames ) {
    this.outputNames = outputNames;
  }

  public void execute() throws Exception {
  }

}
