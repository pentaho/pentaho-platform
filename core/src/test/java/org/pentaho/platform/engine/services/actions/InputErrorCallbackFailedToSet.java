/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.engine.services.actions;

import org.pentaho.platform.api.action.IAction;

import java.util.ArrayList;
import java.util.List;

public class InputErrorCallbackFailedToSet implements IAction {

  private List<String> inputs = new ArrayList<String>();

  public void setInputs( List<String> messages ) {
    this.inputs = messages;
  }

  public List<String> getInputs() {
    return inputs;
  }

  public void execute() throws Exception {
  }

}
