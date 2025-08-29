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

public class ActionInitializationException extends ActionSequenceException {

  /**
   * 
   */
  private static final long serialVersionUID = -7105753821507187932L;

  public ActionInitializationException() {

  }

  public ActionInitializationException( String msg ) {
    super( msg );
  }

  public ActionInitializationException( String msg, Throwable cause ) {
    super( msg, cause );
  }

  public ActionInitializationException( Throwable cause ) {
    super( cause );
  }

  public ActionInitializationException( String msg, Throwable cause, String sessionName, String instanceId,
      String actionSequenceName, IActionDefinition actionDefinition ) {
    super( msg, cause, sessionName, instanceId, actionSequenceName, actionDefinition );
  }

  public ActionInitializationException( String msg, String sessionName, String instanceId, String actionSequenceName,
      IActionDefinition actionDefinition ) {
    super( msg, sessionName, instanceId, actionSequenceName, actionDefinition );
  }
}
