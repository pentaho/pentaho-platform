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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.security.userroledao.jackrabbit;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.repository2.userroledao.jackrabbit.security.DefaultPentahoPasswordEncoder;
import org.pentaho.test.platform.security.userroledao.ws.UserRoleWebServiceBase;

public class DefaultPentahoPasswordEncoderTest {

  @Test
  public void testValidCredentials( ) {
    DefaultPentahoPasswordEncoder passwordEncoder = new DefaultPentahoPasswordEncoder();
    String password = "helloworld";
    String encryptedPassword =  new UserRoleWebServiceBase.PasswordEncoderMock().encode( password );
    Assert.assertTrue( passwordEncoder.isPasswordValid( encryptedPassword, password, null ) );
  }

  @Test
  public void testInvalidCredentials( ) {
    DefaultPentahoPasswordEncoder passwordEncoder = new DefaultPentahoPasswordEncoder( );
    Assert.assertFalse( passwordEncoder.isPasswordValid( "password", null, null ) );
    Assert.assertFalse( passwordEncoder.isPasswordValid( passwordEncoder.encodePassword( "", null ), "password", null ) );
    Assert.assertFalse( passwordEncoder.isPasswordValid( null, null, null ) );
  }
}
