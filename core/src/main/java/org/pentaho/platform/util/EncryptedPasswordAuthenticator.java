/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2023 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Password Authenticator for Pentaho stored encrypted passwords.
 */
public class EncryptedPasswordAuthenticator extends Authenticator {

  public EncryptedPasswordAuthenticator( String userId, String password, Base64PasswordService base64PasswordService  ) {
    this.userId = userId;
    this.password = password;
    this.base64PasswordService = base64PasswordService;
  }

  public EncryptedPasswordAuthenticator( String userId, String password ) {
    this( userId, password, new Base64PasswordService() );
  }

  final String password;
  final String userId;
  // Using this class to be consistent with previous implementations. Provides basic encryption/decryption base64
  final Base64PasswordService base64PasswordService;

  public static final String ENCRYPTED_PREFIX = "ENC:"; // standard PUC prefix

  /**
  * Similar logic as {@link KettlePasswordService#decrypt(String)}  }
  * @return
  */
  @Override
  protected PasswordAuthentication getPasswordAuthentication() {
    String decrypted;
    try {
      String tmpPass = password;
      if ( password.startsWith( ENCRYPTED_PREFIX ) ) {
        decrypted = base64PasswordService.decrypt( password.substring( ENCRYPTED_PREFIX.length(), tmpPass.length() ) );
      } else {
        decrypted = base64PasswordService.decrypt( password );
      }
    } catch ( Exception e ) {
      decrypted = password;
    }
    return new PasswordAuthentication( userId, decrypted );
  }
}
