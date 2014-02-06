/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */
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
