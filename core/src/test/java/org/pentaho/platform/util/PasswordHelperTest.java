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

import static org.junit.Assert.*;
import org.junit.Test;

public class PasswordHelperTest {
  @Test
  public void testDecryptsWhenPasswordIndicatesEncryption() throws Exception {
    PasswordHelper helper = new PasswordHelper( new KettlePasswordService() );
    String contra = "uuddlrlrbas";
    String druidia = "12345";
    assertEquals( contra, helper.getPassword( "ENC:Encrypted 2be98afc86ad28780af15bc7ccc90aec9" ) );
    assertEquals( druidia, helper.getPassword( druidia ) );
    assertEquals( "", helper.getPassword( "" ) );
    assertNull(helper.getPassword( null ) );
  }

  @Test
  public void testEncryptsPassword() throws Exception {
    PasswordHelper helper = new PasswordHelper( new KettlePasswordService() );
    assertEquals( "ENC:Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde", helper.encrypt( "password" ) );
  }
}
