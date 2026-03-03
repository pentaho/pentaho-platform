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


package org.pentaho.platform.api.action;

/**
 * An exception thrown when the invocation of {@link org.pentaho.platform.api.action.IAction} fails.
 */
public class ActionInvocationException extends Exception {

  public ActionInvocationException( String msg ) {
    super( msg );
  }

  public ActionInvocationException( String msg, Throwable t ) {
    super( msg, t );
  }
}
