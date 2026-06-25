/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.util;

import junit.framework.TestCase;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;

public class PasswordServiceTest extends TestCase {

  public void testPasswordService() {
    String password = "password"; //$NON-NLS-1$
    IPasswordService passwordService = new KettlePasswordService();
    String encryptedPassword = null;
    try {
      encryptedPassword = passwordService.encrypt( password );
      String decryptedPassword = passwordService.decrypt( encryptedPassword );
      assertEquals( password, decryptedPassword );
    } catch ( PasswordServiceException pse ) {
      fail( "should not have thrown the exception" ); //$NON-NLS-1$
      pse.printStackTrace();
    }
  }

}
