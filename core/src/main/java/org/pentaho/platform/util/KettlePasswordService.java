/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
