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


package org.pentaho.platform.engine.security.messages;

import static org.junit.Assert.*;
import org.junit.Test;

public class MessagesTest {

  @Test
  public void testGetInstance() {
    Messages instance = Messages.getInstance();
    assertNotNull( instance );
  }
}
