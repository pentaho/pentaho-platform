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

import org.apache.commons.codec.binary.Base64;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;

public class Base64PasswordService implements IPasswordService {

  public String encrypt( String password ) throws PasswordServiceException {
    return StringUtil.isEmpty( password ) ? password : new String( Base64.encodeBase64( password.getBytes() ) );
  }

  public String decrypt( String encryptedPassword ) throws PasswordServiceException {
    return StringUtil.isEmpty( encryptedPassword ) ? encryptedPassword : new String( Base64
        .decodeBase64( encryptedPassword.getBytes() ) );
  }
}
