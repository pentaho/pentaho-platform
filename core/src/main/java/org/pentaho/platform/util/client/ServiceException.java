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



package org.pentaho.platform.util.client;

public class ServiceException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 3965771811870311265L;

  public ServiceException( Exception causedBy ) {
    super( causedBy );
  }

  public ServiceException( String message ) {
    super( message );
  }

}
