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

import junit.framework.TestCase;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;

public class PasswordServiceTest extends TestCase {

  public void testPasswordService() {
    String password = "password"; //$NON-NLS-1$
    IPasswordService passwordService = new Base64PasswordService();
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
