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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;

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
