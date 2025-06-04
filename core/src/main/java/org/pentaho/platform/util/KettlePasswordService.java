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

import org.pentaho.platform.api.util.PasswordServiceException;
import org.pentaho.di.core.encryption.KettleTwoWayPasswordEncoder;

/*
 * The purpose of this class is to allow existing Base64-encoded passwords to be able to be
 * decoded, but going forward, to enforce using the KettleTwoWayPasswordEncoder for stronger
 * password encoding. If the password passed into the decrypt function doesn't start with the
 * registered prefix of the KettleTwoWayPasswordEncoder, then it's the password provided was
 * just Base64 encoded.
 *
 */
public class KettlePasswordService extends Base64PasswordService {

  String prefix;
  KettleTwoWayPasswordEncoder encoder;

  public KettlePasswordService() {
    super();
    encoder = new KettleTwoWayPasswordEncoder();
    String[] prefixes = encoder.getPrefixes();
    prefix = prefixes[0]; // prefixes should *never* be null by contract
  }

  @Override
  public String encrypt( String password ) throws PasswordServiceException {
    if ( StringUtil.isEmpty( password ) ) {
      return password;
    } else {
      return encoder.encode( password );
    }
  }

  @Override
  public String decrypt( String encryptedPassword ) throws PasswordServiceException {
    if ( StringUtil.isEmpty( encryptedPassword ) ) {
      return encryptedPassword;
    } else {
      if ( encryptedPassword.startsWith( prefix ) ) { // If it starts with "Encrypted " then decode using KettleTwoWayPasswordEncoder
        return encoder.decode( encryptedPassword );
      } else { // If not, likely Base64 encoded so twy that
        return super.decrypt( encryptedPassword );
      }
    }
  }

}
