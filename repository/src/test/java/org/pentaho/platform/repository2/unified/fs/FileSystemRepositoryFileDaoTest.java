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
            .idToPath( "C\t:Program Files:pentaho:design-tools:data-integration/Unsaved Report.xanalyzer" ) );
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
