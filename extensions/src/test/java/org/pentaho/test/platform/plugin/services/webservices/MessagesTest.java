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
