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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Created by bgroves on 10/26/15.
 */
public class ThemeTest {

  private static final String NAME = anyString();
  private static final String ROOT_DIR = anyString();
  private static final boolean HIDDEN = anyBoolean();
  private static final String ID = anyString();

  @Test
  public void testTheme() {
    Theme theme = new Theme( ID, NAME, ROOT_DIR );
    theme.setHidden( HIDDEN );

    assertEquals( ID, theme.getId() );
    assertEquals( NAME, theme.getName() );
    assertEquals( HIDDEN, theme.isHidden() );
    assertEquals( ROOT_DIR, theme.getThemeRootDir() );
    assertNotNull( theme.getResources() );
    assertEquals( 0, theme.getResources().size() );

    ThemeResource resource = new ThemeResource( theme, "resource" );
    theme.addResource( resource );
    assertEquals( resource, theme.getResources().toArray()[0] );

    String newName = "newName";
    String newId = "newId";
    boolean newHidden = !HIDDEN;
    theme.setHidden( newHidden );
    theme.setId( newId );
    theme.setName( newName );
    theme.setResources( null );
    assertEquals( newId, theme.getId() );
    assertEquals( newName, theme.getName() );
    assertEquals( newHidden, theme.isHidden() );
    assertNull( theme.getResources() );

    // Test equals
    theme = new Theme( ID, NAME, ROOT_DIR );
    Theme diffTheme = new Theme( newId, newName, ROOT_DIR );
    assertTrue( theme.equals( theme ) );
    assertFalse( theme.equals( null ) );
    assertFalse( theme.equals( new String() ) );
    assertFalse( theme.equals( new Theme( ID, null, ROOT_DIR ) ) );
    assertFalse( theme.equals( new Theme( ID, "newName", ROOT_DIR ) ) );
    assertTrue( theme.equals( new Theme( ID, NAME, ROOT_DIR ) ) );
    theme.setName( null );
    assertFalse( theme.equals( new Theme( ID, NAME, ROOT_DIR ) ) );

    assertNotEquals( theme.hashCode(), diffTheme.hashCode() );
    diffTheme.setName( null );
    assertEquals( 0, diffTheme.hashCode() );
  }
}
