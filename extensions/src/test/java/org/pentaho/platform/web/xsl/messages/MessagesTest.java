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


package org.pentaho.platform.web.xsl.messages;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class MessagesTest {

  @Test
  public void test() {
    try {
      Constructor<Messages> constructor = Messages.class.getDeclaredConstructor();
      assertTrue( Modifier.isPrivate( constructor.getModifiers() ) );
    } catch ( Exception e ) {
      fail( Messages.class.getSimpleName() + " Does not have a private constructor " );
    }

    assertNotNull( Messages.getInstance() );
  }

}
