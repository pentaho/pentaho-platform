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

package org.pentaho.platform.web.http.api.resources.utils;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class FileUtilsTest {

  @Test
  public void convertCommaSeparatedString() {
    String comma = ",";
    String string2 = "string2";
    String string1 = "string1";

    String commaSeparatedStr = string1 + comma + string2;
    String[] result = FileUtils.convertCommaSeparatedStringToArray( commaSeparatedStr );
    assertEquals( result.length, 2 );
    assertEquals( result[ 0 ], string1 );
    assertEquals( result[ 1 ], string2 );
  }

  @Test
  public void convertSingleString() {
    String string = "string";

    String[] result = FileUtils.convertCommaSeparatedStringToArray( string );
    assertEquals( result.length, 1 );
    assertEquals( result[ 0 ], string );
  }

  @Test( expected = IllegalArgumentException.class )
  public void convertEmptyString() {
    FileUtils.convertCommaSeparatedStringToArray( StringUtils.EMPTY );
  }
}
