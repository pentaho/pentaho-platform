/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.webservices;

import junit.framework.TestCase;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;

public class MessagesTest extends TestCase {

  public void testMessages() {

    assertEquals( "Wrong message returned", "test message 1", Messages.getInstance().getString( "test.MESSAGE1" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        "Wrong message returned", "test message 2: A", Messages.getInstance().getString( "test.MESSAGE2", "A" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    assertEquals(
        "Wrong message returned", "test message 3: A B", Messages.getInstance().getString( "test.MESSAGE3", "A", "B" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    assertEquals(
        "Wrong message returned", "test message 4: A B C", Messages.getInstance().getString( "test.MESSAGE4", "A", "B", "C" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    assertEquals(
        "Wrong message returned", "test message 5: A B C D", Messages.getInstance().getString( "test.MESSAGE5", "A", "B", "C", "D" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

  }

  public void testErrorMessages() {

    assertEquals(
      "Wrong message returned", "test.ERROR_0001 - test error 1", Messages.getInstance()
        .getErrorString( "test.ERROR_0001_TEST_ERROR1" ) );
    assertEquals(
        "Wrong message returned", "test.ERROR_0002 - test error 2: A", Messages.getInstance()
        .getErrorString( "test.ERROR_0002_TEST_ERROR2", "A" ) );
    assertEquals(
        "Wrong message returned", "test.ERROR_0003 - test error 3: A B", Messages.getInstance()
        .getErrorString( "test.ERROR_0003_TEST_ERROR3", "A", "B" ) );
    assertEquals(
        "Wrong message returned", "test.ERROR_0004 - test error 4: A B C", Messages.getInstance()
        .getErrorString( "test.ERROR_0004_TEST_ERROR4", "A", "B", "C" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

  }

  public void testBadKey() {

    assertEquals( "Wrong message returned", "!bogus key!", Messages.getInstance().getString( "bogus key" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        "Wrong message returned", "test.ERROR_0001 - !test.ERROR_0001_BOGUS!", Messages.getInstance().getErrorString( "test.ERROR_0001_BOGUS" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

  }

  public void testEncoding() {

    assertEquals( "Wrong message returned", "", Messages.getInstance().getEncodedString( null ) ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Wrong message returned", "test: &#x81; &#x99;", Messages.getInstance().getXslString( "test.encode1" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

  }

}
