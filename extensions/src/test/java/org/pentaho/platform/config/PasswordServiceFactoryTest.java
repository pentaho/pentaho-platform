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

import org.junit.Test;
import org.pentaho.platform.engine.security.CipherEncryptionService;
import org.pentaho.platform.util.Base64PasswordService;
import org.pentaho.platform.util.KettlePasswordService;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/21/15.
 */
public class PasswordServiceFactoryTest {

  @Test
  public void testInit() throws Exception {
    PasswordServiceFactory.init( "org.pentaho.platform.util.Base64PasswordService" );
    assertTrue( PasswordServiceFactory.getPasswordService() instanceof Base64PasswordService );

    PasswordServiceFactory.init( "org.pentaho.platform.util.KettlePasswordService" );
    assertTrue( PasswordServiceFactory.getPasswordService() instanceof KettlePasswordService );

    PasswordServiceFactory.init( "org.pentaho.platform.engine.security.CipherEncryptionService" );
    assertTrue( PasswordServiceFactory.getPasswordService() instanceof CipherEncryptionService );

    // for code coverage purposes
    PasswordServiceFactory hack = new PasswordServiceFactory();
  }

  @Test ( expected = RuntimeException.class )
  public void testInit_withFailure() throws Exception {
    PasswordServiceFactory.init( "org.pentaho.platform.util.InvalidClass" );
  }
}
