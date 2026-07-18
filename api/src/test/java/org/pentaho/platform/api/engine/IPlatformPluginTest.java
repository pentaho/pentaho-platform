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



package org.pentaho.platform.api.engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class IPlatformPluginTest {

  @Test
  public void testClassLoaderTypeEnum() {
    assertNotNull( IPlatformPlugin.ClassLoaderType.valueOf( "DEFAULT" ) );
    assertNotNull( IPlatformPlugin.ClassLoaderType.valueOf( "OVERRIDING" ) );
  }

}
