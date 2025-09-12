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

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

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
