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


package org.pentaho.platform.config;

import org.pentaho.platform.api.util.IPasswordService;

public class PasswordServiceFactory {

  private static final String DEFAULT_IMPL = "org.pentaho.platform.util.KettlePasswordService"; //$NON-NLS-1$
  private static IPasswordService currentService;

  static {
    init( DEFAULT_IMPL );
  }

  public static synchronized void init( String classname ) {
    try {
      currentService = (IPasswordService) Class.forName( classname ).newInstance();
    } catch ( Throwable e ) {
      // wrap this as a runtime exception. This type of error is configuration related
      throw new RuntimeException( e );
    }
  }

  /**
   * returns the current implementation of IPasswordService
   *
   * @return datasource service
   *
   * @throws RuntimeException
   *           if class cannot be instantiated
   */
  public static IPasswordService getPasswordService() {
    return currentService;
  }
}
