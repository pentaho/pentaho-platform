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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.config.i18n;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Created by rfellows on 10/20/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class MessagesTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testGetMessages() throws Exception {
    assertEquals( "Wrong message returned", "Normal", Messages.getString( "XmlSerializer.stateNormal" ) );

    assertEquals( "Wrong message returned", "Unknown Host: localhost",
      Messages.getString( "StopJettyServer.ERROR_0003_UNKNOWN_HOST", "localhost" ) );

    assertEquals( "Wrong message returned", "Client request for URI \"A\" failed. Reason for failure: B.",
      Messages.getString( "ThreadSafeHttpClient.ERROR_0001_CLIENT_REQUEST_FAILED", "A", "B" ) );

  }

  @Test
  public void testErrorMessages() {

    assertEquals(
      "Wrong message returned", "StopJettyServer.ERROR_0003 - Unknown Host: localhost",
      Messages.getErrorString( "StopJettyServer.ERROR_0003_UNKNOWN_HOST", "localhost" ) );

    assertEquals( "Wrong message returned", "PacService.ERROR_0048 - Failed to rollback transaction.",
      Messages.getErrorString( "PacService.ERROR_0048_ROLLBACK_FAILED" ) );
  }

  @Test
  public void testBadKey() {

    assertEquals( "Wrong message returned", "!bogus key!", Messages.getString( "bogus key" ) );

    assertEquals(
      "Wrong message returned", "test.ERROR_0001 - !test.ERROR_0001_BOGUS!",
      Messages.getErrorString( "test.ERROR_0001_BOGUS" ) );

  }

  @Test
  public void testBadEncoding() {
    //the messages.properties file has a bad encoding for the test.encode1 property, this causes a
    // MissingResourceException which
    //manifests as a returned string of !<key>! for all getString calls including the good strings
    assertEquals( "!test.bad_encode1!", Messages.getString( "test.bad_encode1" ) );
    //it seems that the successful retrieval of a good message inside a bundle that has a bad encoding is not
    // consistent.
    //Therefore, the following check is not very useful.
    //    assertEquals("!test.MESSAGE1!", Messages.getString("test.MESSAGE1")); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
