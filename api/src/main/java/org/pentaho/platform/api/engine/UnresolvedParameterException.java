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

public class UnresolvedParameterException extends ActionSequenceException {
  /**
   * 
   */
  private static final long serialVersionUID = -2676655593926463813L;
  private String parameterName;

  public UnresolvedParameterException() {

  }

  public UnresolvedParameterException( String parameterName ) {
    this.parameterName = parameterName;
  }

  public UnresolvedParameterException( String msg, String parameterName ) {
    super( msg );
    this.parameterName = parameterName;
  }

  public UnresolvedParameterException( String msg, Throwable cause, String sessionName, String instanceId,
      String actionSequenceName, IActionDefinition actionDefinition ) {
    super( msg, cause, sessionName, instanceId, actionSequenceName, actionDefinition );
  }

  public UnresolvedParameterException( String msg, String sessionName, String instanceId, String actionSequenceName,
      IActionDefinition actionDefinition ) {
    super( msg, sessionName, instanceId, actionSequenceName, actionDefinition );
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName( String parameterName ) {
    this.parameterName = parameterName;
  }

}
