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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Created by bgroves on 10/26/15.
 */
public class ThemeResourceTest {
  private static final String NAME = anyString();
  private static final String ROOT_DIR = anyString();
  private static final String ID = anyString();

  private static final Theme THEME = new Theme( ID, NAME, ROOT_DIR );
  private static final String RESOURCE = anyString();

  @Test
  public void testThemeResource() {
    ThemeResource resource = new ThemeResource( THEME, RESOURCE );
    assertEquals( RESOURCE, resource.getLocation() );
    assertEquals( THEME, resource.getTheme() );

    Theme newTheme = new Theme( ID, "newTheme", ROOT_DIR  );
    resource.setTheme( newTheme );
    assertEquals( newTheme, resource.getTheme() );
    String newLocation = "newLocation";
    resource.setLocation( newLocation );
    assertEquals( newLocation, resource.getLocation() );
  }
}
