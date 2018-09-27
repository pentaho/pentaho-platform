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
 * Copyright (c) 2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.KettleTwoWayPasswordEncoder;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.EnvUtil;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.reflect.Whitebox.getInternalState;

/**
 * @author Luis Martins
 */
@RunWith( PowerMockRunner.class )
@PrepareForTest( EnvUtil.class )
public class CommandLineProcessorAESPasswordTest {

  private static final String INFO_OPTION_USERNAME_NAME = "username";
  private static final String INFO_OPTION_PASSWORD_NAME = "password";

  @Before
  public void setup() {
    spy( EnvUtil.class );
  }

  @Test( expected = KettleException.class )
  public void testInitRestServiceWithAESPassword() throws Exception {
    when( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ) ).thenReturn( "AES" );

    CommandLineProcessor cmd = mock( CommandLineProcessor.class );
    doCallRealMethod().when( cmd ).getPassword();
    doReturn( "AES PtdCGOdq6NMSvvjs5CCKIg==" ).when( cmd ).getOptionValue( INFO_OPTION_PASSWORD_NAME, true, false );

    try {
      cmd.getPassword();
    } catch ( KettleException e ) {
      assertEquals( "\nUnable to find plugin with ID 'AES'\n", e.getMessage() );
      throw e;
    }
  }
}
