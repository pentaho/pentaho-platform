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


package org.pentaho.platform.api.repository;

public class ContentException extends RepositoryException {

  /**
   * 
   */
  private static final long serialVersionUID = -3190555068029192935L;

  /**
   * Constructor
   * 
   * @param message
   *          The message
   */
  public ContentException( final String message ) {
    super( message );
  }

  /**
   * Constructor
   * 
   * @param message
   *          The exception message
   * @param reas
   *          The throwable reason
   */
  public ContentException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  /**
   * Constructor
   * 
   * @param reas
   *          The throwable reason
   */
  public ContentException( final Throwable reas ) {
    super( reas );
  }

}
