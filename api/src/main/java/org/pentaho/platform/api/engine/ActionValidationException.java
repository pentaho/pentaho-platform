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

public class ActionValidationException extends ActionSequenceException {

  /**
   * 
   */
  private static final long serialVersionUID = -6313915362847367483L;

  public ActionValidationException( String msg ) {
    super( msg );
  }

  public ActionValidationException( Throwable cause ) {
    super( cause );
  }

  public ActionValidationException( String msg, Throwable cause ) {
    super( msg, cause );
  }

  public ActionValidationException( String msg, String componentName ) {
    super( msg );
    setActionClass( componentName );
  }

  public ActionValidationException( String msg, Throwable cause, String componentName ) {
    super( msg, cause );
    setActionClass( componentName );
  }

  public ActionValidationException( String msg, Throwable cause, String sessionName, String instanceId,
      String actionSequenceName, String actionDescription, String componentName ) {
    super( msg, cause, sessionName, instanceId, actionSequenceName, actionDescription, componentName );
  }

  public ActionValidationException( String msg, Throwable cause, String sessionName, String instanceId,
      String actionSequenceName, IActionDefinition actionDefinition ) {
    super( msg, cause, sessionName, instanceId, actionSequenceName, actionDefinition );
  }

  public ActionValidationException( String msg, String sessionName, String instanceId, String actionSequenceName,
      IActionDefinition actionDefinition ) {
    super( msg, sessionName, instanceId, actionSequenceName, actionDefinition );
  }
}
