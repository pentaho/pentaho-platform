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

package org.pentaho.platform.engine.services.connection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;

public class PentahoConnectionFactoryTest {

  @Test
  public void testGetConnection() {
    PentahoConnectionFactory factory = new PentahoConnectionFactory(); //Test constructor.
    assertNotNull( factory );

    String dsType = "connection-test";
    IPentahoSession session = mock( IPentahoSession.class );
    ILogger logger = mock( ILogger.class );
    IPentahoConnection connection = PentahoConnectionFactory.getConnection( dsType, session, logger );
    assertNull( connection );

    String connectStr = "connect-str";
    connection = PentahoConnectionFactory.getConnection( dsType, connectStr, session, logger );
    assertNull( connection );

    String driver = "ds-driver";
    String location = "location";
    String userName = "username";
    String password = "password";
    connection = PentahoConnectionFactory.getConnection( dsType, driver, location, userName, password, session, logger );
    assertNull( connection );

    connection = PentahoConnectionFactory.getConnection( dsType, null, null, null, null, session, logger );
    assertNull( connection );
  }
}
