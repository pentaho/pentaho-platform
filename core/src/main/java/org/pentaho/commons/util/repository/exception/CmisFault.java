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



package org.pentaho.commons.util.repository.exception;

public abstract class CmisFault extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 4519010697714251409L;

  private int errorCode;

  private String errorMessage;

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorCode( int errorCode ) {
    this.errorCode = errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage( String errorMessage ) {
    this.errorMessage = errorMessage;
  }

}
