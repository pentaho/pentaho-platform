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


package org.pentaho.platform.api.ui;

import org.junit.jupiter.api.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Created by bgroves on 10/26/15.
 */
public class ModuleThemeInfoTest {

  private static final String MOD = anyString();

  @Test
  public void testModuleThemeInfo() {
    ModuleThemeInfo info = new ModuleThemeInfo( MOD );

    assertEquals( MOD, info.getModule() );
    assertNotNull( info.getModuleThemes() );
    assertEquals( 0, info.getModuleThemes().size() );
    assertNotNull( info.getSystemThemes() );
    assertEquals( 0, info.getSystemThemes().size() );
  }
}
