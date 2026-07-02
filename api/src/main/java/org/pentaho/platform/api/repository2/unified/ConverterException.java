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
 * An exception that can be thrown from {@code Converter} implementations.
 */
public class ConverterException extends RuntimeException {

  private static final long serialVersionUID = -3180298582920444104L;

  public ConverterException() {
    super();
  }

  public ConverterException( final String message, final Throwable cause ) {
    super( message, cause );
  }

  public ConverterException( final String message ) {
    super( message );
  }

  public ConverterException( final Throwable cause ) {
    super( cause );
  }

}
