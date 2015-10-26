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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.ui;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;

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
