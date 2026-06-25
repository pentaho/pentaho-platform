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



package org.pentaho.platform.api.action;

public class ActionPreProcessingException extends Exception {

  private static final long serialVersionUID = -7724761382763461571L;

  public ActionPreProcessingException() {
    super();
  }

  public ActionPreProcessingException( String message, Throwable cause ) {
    super( message, cause );
  }

  public ActionPreProcessingException( String message ) {
    super( message );
  }

  public ActionPreProcessingException( Throwable cause ) {
    super( cause );
  }

}
