/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.fs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: kwalker Date: 8/23/13 Time: 8:44 AM
 */
public class FileSystemRepositoryFileDaoTest {
  @Test
  public void testIdToPath() throws Exception {
    assertEquals( "C:/Program Files/pentaho/design-tools/data-integration/Unsaved Report.xanalyzer",
        FileSystemRepositoryFileDao
            .idToPath( "C::Program Files:pentaho:design-tools:data-integration/Unsaved Report.xanalyzer" ) );
    assertEquals( "C:/Program Files\\pentaho\\design-tools\\data-integration\\Unsaved Report.xanalyzer",
        FileSystemRepositoryFileDao
            .idToPath( "C:/Program Files\\pentaho\\design-tools\\data-integration\\Unsaved Report.xanalyzer" ) );
    assertEquals( "C:/Program Files\\pentaho\\design-tools\\data-integration\\Unsaved Report.xanalyzer",
        FileSystemRepositoryFileDao
            .idToPath( "C:\\Program Files\\pentaho\\design-tools\\data-integration\\Unsaved Report.xanalyzer" ) );
    assertEquals( "/home/pentaho/design-tools/data-integration/Unsaved Report.xanalyzer", FileSystemRepositoryFileDao
        .idToPath( ":home:pentaho:design-tools:data-integration/Unsaved Report.xanalyzer" ) );
  }
}
