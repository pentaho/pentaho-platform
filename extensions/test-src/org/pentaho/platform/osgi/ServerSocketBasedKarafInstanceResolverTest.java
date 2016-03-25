/*
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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.osgi;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by nbaker on 3/29/16.
 */
public class ServerSocketBasedKarafInstanceResolverTest {

  @Test
  public void testDetermineStartPort() throws Exception {
    // get the default
    int port = ServerSocketBasedKarafInstanceResolver.determineStartPort();
    assertEquals( 11000, port );

    // set custom start
    System.setProperty( ServerSocketBasedKarafInstanceResolver.PENTAHO_KARAF_INSTANCE_START_PORT, "1234" );
    port = ServerSocketBasedKarafInstanceResolver.determineStartPort();
    assertEquals( 1234, port );

    // set a bad input
    System.setProperty( ServerSocketBasedKarafInstanceResolver.PENTAHO_KARAF_INSTANCE_START_PORT, "isbad" );
    port = ServerSocketBasedKarafInstanceResolver.determineStartPort();
    assertEquals( 11000, port );

  }
}