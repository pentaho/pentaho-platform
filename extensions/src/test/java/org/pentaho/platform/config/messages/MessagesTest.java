/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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
