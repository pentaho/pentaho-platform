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
