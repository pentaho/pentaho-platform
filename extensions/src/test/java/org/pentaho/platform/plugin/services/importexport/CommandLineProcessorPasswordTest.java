/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2018-2023 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.EnvUtil;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * @author Luis Martins
 */
@RunWith( MockitoJUnitRunner.class )
public class CommandLineProcessorPasswordTest {

  private static final String INFO_OPTION_USERNAME_NAME = "username";
  private static final String INFO_OPTION_PASSWORD_NAME = "password";
  private static MockedStatic<EnvUtil>  envUtilMock;

  @BeforeClass
  public static void beforeAll() {
    envUtilMock = mockStatic( EnvUtil.class );
    envUtilMock.when( () -> EnvUtil.getSystemProperty( Const.KETTLE_REDIRECT_STDOUT, "N" ) ).thenReturn( "N" );
    envUtilMock.when( () -> EnvUtil.getSystemProperty( Const.KETTLE_REDIRECT_STDERR, "N" ) ).thenReturn( "N" );
  }

  @AfterClass
  public static void afterAll() {
    envUtilMock.close();
  }

  @Before
  public void setup() {
    org.junit.Assume.assumeFalse( Const.isWindows() );  //skip tests on windows as it won't initialize for some reason.
    KettleClientEnvironment.reset();
  }

  @Test
  public void testGetUsername() throws Exception {
    CommandLineProcessor cmd = mock( CommandLineProcessor.class );
    doCallRealMethod().when( cmd ).getUsername();
    doReturn( "admin" ).when( cmd ).getOptionValue( INFO_OPTION_USERNAME_NAME, true, false );

    assertEquals( "admin", cmd.getUsername() );
  }

  /**
   * {@link org.pentaho.di.core.encryption.Encr} will use {@link org.pentaho.support.encryption.KettleTwoWayPasswordEncoder} as default.
   */
  @Test
  public void testInitRestServiceWithKettlePassword() throws Exception {
    envUtilMock.when( () -> EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ) ).thenReturn( null );

    CommandLineProcessor cmd = mock( CommandLineProcessor.class );
    doCallRealMethod().when( cmd ).getPassword();
    doReturn( "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde" ).when( cmd ).getOptionValue( INFO_OPTION_PASSWORD_NAME, true, false );

    assertEquals( "password", cmd.getPassword() );

  }

  /**
   * This test confirms that {@link org.pentaho.di.core.encryption.Encr} will try to look for the AES decoder.
   */
  @Test( expected = KettleException.class )
  public void testInitRestServiceWithAESPassword() throws Exception {
    envUtilMock.when( () -> EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ) ).thenReturn( "AES" );

    CommandLineProcessor cmd = mock( CommandLineProcessor.class );
    doCallRealMethod().when( cmd ).getPassword();
    lenient().doReturn( "AES PtdCGOdq6NMSvvjs5CCKIg==" ).when( cmd ).getOptionValue( INFO_OPTION_PASSWORD_NAME, true, false );

    try {
      cmd.getPassword();
    } catch ( KettleException e ) {
      assertThat( e.getMessage(), containsString( "Unable to find plugin with ID 'AES'" ) );
      throw e;
    }
  }
}
