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


package org.pentaho.platform.api.engine;

import org.pentaho.actionsequence.dom.IActionDefinition;

public class ActionSequencePromptException extends ActionSequenceException {

  /**
   * 
   */
  private static final long serialVersionUID = 757868320776336597L;

  public ActionSequencePromptException() {

  }

  public ActionSequencePromptException( String msg ) {
    super( msg );
  }

  public ActionSequencePromptException( Throwable cause ) {
    super( cause );
  }

  public ActionSequencePromptException( String msg, Throwable cause ) {
    super( msg, cause );
  }

  public ActionSequencePromptException( String msg, Throwable cause, String sessionName, String instanceId,
      String actionSequenceName, IActionDefinition actionDefinition ) {
    super( msg, cause, sessionName, instanceId, actionSequenceName, actionDefinition );
  }

  public ActionSequencePromptException( String msg, String sessionName, String instanceId, String actionSequenceName,
      IActionDefinition actionDefinition ) {
    super( msg, sessionName, instanceId, actionSequenceName, actionDefinition );
  }
}
