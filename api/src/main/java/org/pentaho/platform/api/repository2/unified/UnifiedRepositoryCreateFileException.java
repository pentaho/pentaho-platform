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


package org.pentaho.platform.api.repository2.unified;

/**
 * An exception that can be thrown from {@code IUnifiedRepository} implementations.
 * 
 * @author mlowery
 */
public class UnifiedRepositoryCreateFileException extends UnifiedRepositoryException {

  private static final long serialVersionUID = -7800484179397724352L;
  public static final String PREFIX = "ExceptionOnCreatingFile";

  public UnifiedRepositoryCreateFileException() {
    super();
  }

  public UnifiedRepositoryCreateFileException( final String message, final Throwable cause ) {
    super( message, cause );
  }

  public UnifiedRepositoryCreateFileException( final String message ) {
    super( message );
  }

  public UnifiedRepositoryCreateFileException( final Throwable cause ) {
    super( cause );
  }

  @Override public String getMessage() {
    return PREFIX + ": " + super.getMessage();
  }

}
