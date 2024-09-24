/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util.beans;

import static org.junit.Assert.*;

import org.junit.Test;

public class AlternateIndexFormatterTest {

  private static final String SIMPLE_NAME = "simpleName";

  private static final String INDEXED_NAME = "indexedName_0";

  private static final String INDEXED_NAME_CONVERTED = "indexedName[0]";

  private static final String UNDERSCORE_NAME = "indexed_Name";


  @Test
  public void testFormat_indexed() {
    AlternateIndexFormatter formatter = new AlternateIndexFormatter();
    String actual = formatter.format( INDEXED_NAME );
    assertEquals( "indexed name should be formated", INDEXED_NAME_CONVERTED, actual );
  }

  @Test
  public void testFormat_underscore() {
    AlternateIndexFormatter formatter = new AlternateIndexFormatter();
    String actual = formatter.format( UNDERSCORE_NAME );
    assertEquals( "underscore without number should not be formated", UNDERSCORE_NAME, actual );
  }

  @Test
  public void testFormat_simpleName() {
    AlternateIndexFormatter formatter = new AlternateIndexFormatter();
    String actual = formatter.format( SIMPLE_NAME );
    assertEquals( "simple name should not be formated", SIMPLE_NAME, actual );
  }

}
