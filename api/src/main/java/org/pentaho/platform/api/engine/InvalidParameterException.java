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

/**
 * This exception just signals that an exception occurred initializing parameters for an adcion-definition
 * 
 * @author dmoran
 * 
 */
public class InvalidParameterException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -5276376792590138849L;

  public InvalidParameterException() {
  }

  public InvalidParameterException( String msg ) {
    super( msg );
  }
}
