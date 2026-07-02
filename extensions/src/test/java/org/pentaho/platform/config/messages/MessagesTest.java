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


package org.pentaho.platform.config.messages;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by rfellows on 10/20/15.
 */
public class MessagesTest {

  @Test
  public void testGetInstance() throws Exception {
    assertNotNull( Messages.getInstance() );
  }

}
