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

import org.pentaho.platform.api.action.IAction;

import java.util.ArrayList;
import java.util.List;

public class TestIndexedInputsAction implements IAction {

  private List<String> messages = new ArrayList<String>();
  private String scalarMessage;

  public void setMessages( List<String> messages ) {
    this.messages = messages;
  }

  public List<String> getMessages() {
    return messages;
  }


  public void execute() throws Exception {
  }

  public void setScalarMessage( String s ) {
    scalarMessage = s;
  }

  public String getTextOfScalarMessage() {
    return scalarMessage;
  }
}
