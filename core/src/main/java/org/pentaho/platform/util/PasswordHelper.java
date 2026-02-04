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


package org.pentaho.platform.util;

import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;

/**
 * decrypts passwords that would likely be saved in a properties file.
 */
public class PasswordHelper {
  private static final String ENC = "ENC:";
  private IPasswordService passwordService;

  public PasswordHelper() {
    this( PentahoSystem.get( IPasswordService.class, null ) );
  }

  public PasswordHelper( IPasswordService passwordService ) {
    this.passwordService = passwordService;
  }

  public String getPassword( String input ) {
    if ( input != null && input.startsWith( ENC ) ) {
      try {
        return passwordService.decrypt( input.substring( 4 ) );
      } catch ( PasswordServiceException e ) {
        Logger.error( this, "Exception decrypting password", e );
        throw new RuntimeException( e );
      }
    }
    return input;
  }

  public String encrypt( String password ) {
    try {
      return ENC + passwordService.encrypt( password );
    } catch ( PasswordServiceException e ) {
      Logger.error( this, "Exception encrypting password", e );
      throw new RuntimeException( e );
    }
  }
}
