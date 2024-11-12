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
