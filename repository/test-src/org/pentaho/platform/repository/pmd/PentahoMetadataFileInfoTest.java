/*
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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *  
 * @author dkincade
 */
package org.pentaho.platform.repository.pmd;

import junit.framework.TestCase;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class PentahoMetadataFileInfoTest extends TestCase {
  private static final PentahoMetadataFileInfo.FileType PRO = PentahoMetadataFileInfo.FileType.PROPERTIES;
  private static final PentahoMetadataFileInfo.FileType XMI = PentahoMetadataFileInfo.FileType.XMI;
  private static final PentahoMetadataFileInfo.FileType UNK = PentahoMetadataFileInfo.FileType.UNKNOWN;

  private static final int PATH = 0;
  private static final int FILETYPE = 1;
  private static final int BASENAME = 2;
  private static final int FILENAME = 3;
  private static final int EXTENSION = 4;
  private static final int LOCALE = 5;


  private static Object[][] DATA = new Object[][]{
      new Object[]{null, UNK, null, null, null, null},
      new Object[]{"", UNK, "", "", "", null},
      new Object[]{" ", UNK, " ", " ", "", null},

      new Object[]{"sample.properties", PRO, "sample", "sample", "properties", null},
      new Object[]{"sample_en.properties", PRO, "sample_en", "sample", "properties", "en"},
      new Object[]{"sample_en_US.properties", PRO, "sample_en_US", "sample", "properties", "en_US"},
      new Object[]{"sample_en_US_Pentaho.properties", PRO, "sample_en_US_Pentaho", "sample", "properties", "en_US_Pentaho"},
      new Object[]{"sample_en_US_Pentaho_Rocks.properties", PRO, "sample_en_US_Pentaho_Rocks", "sample", "properties", "en_US_Pentaho_Rocks"},

      new Object[]{"sam_ple.properties", PRO, "sam_ple", "sam_ple", "properties", null},
      new Object[]{"sam_ple_en.properties", PRO, "sam_ple_en", "sam_ple", "properties", "en"},
      new Object[]{"sam_ple_en_US.properties", PRO, "sam_ple_en_US", "sam_ple", "properties", "en_US"},
      new Object[]{"sam_ple_en_US_Pentaho.properties", PRO, "sam_ple_en_US_Pentaho", "sam_ple", "properties", "en_US_Pentaho"},
      new Object[]{"sam_ple_en_US_Pentaho_Rocks.properties", PRO, "sam_ple_en_US_Pentaho_Rocks", "sam_ple", "properties", "en_US_Pentaho_Rocks"},

      new Object[]{"sample_eN.properties", PRO, "sample_eN", "sample_eN", "properties", null},
      new Object[]{"sample_E1.properties", PRO, "sample_E1", "sample_E1", "properties", null},
      new Object[]{"sample_e-.properties", PRO, "sample_e-", "sample_e-", "properties", null},
      new Object[]{"sample_en_us.properties", PRO, "sample_en_us", "sample_en", "properties", "us"},
      new Object[]{"sample_en_Us.properties", PRO, "sample_en_Us", "sample_en_Us", "properties", null},
      new Object[]{"sample_en_uS.properties", PRO, "sample_en_uS", "sample_en_uS", "properties", null},
      new Object[]{"sample_en_U2.properties", PRO, "sample_en_U2", "sample_en_U2", "properties", null},
      new Object[]{"sample_en_USA.properties", PRO, "sample_en_USA", "sample_en_USA", "properties", null},

      new Object[]{"sample.xmi", XMI, "sample", "sample", "xmi", null},
      new Object[]{"sample_en.xmi", XMI, "sample_en", "sample_en", "xmi", null},
      new Object[]{"sample_en_US.xmi", XMI, "sample_en_US", "sample_en_US", "xmi", null},
      new Object[]{"sample_en_US_Pentaho.xmi", XMI, "sample_en_US_Pentaho", "sample_en_US_Pentaho", "xmi", null},
      new Object[]{"sample_en_US_Pentaho_Rocks.xmi", XMI, "sample_en_US_Pentaho_Rocks", "sample_en_US_Pentaho_Rocks", "xmi", null},

      new Object[]{"sample.xmI", UNK, "sample", "sample", "xmI", null},
      new Object[]{"sample_en.xmI", UNK, "sample_en", "sample_en", "xmI", null},
      new Object[]{"sample_en_US.xmI", UNK, "sample_en_US", "sample_en_US", "xmI", null},
      new Object[]{"sample_en_US_Pentaho.xmI", UNK, "sample_en_US_Pentaho", "sample_en_US_Pentaho", "xmI", null},
      new Object[]{"sample_en_US_Pentaho_Rocks.xmI", UNK, "sample_en_US_Pentaho_Rocks", "sample_en_US_Pentaho_Rocks", "xmI", null},
  };

  public void testParsing() throws Exception {
    for (Object[] testData : DATA) {
      final String path = (String) testData[PATH];
      final PentahoMetadataFileInfo fileInfo = new PentahoMetadataFileInfo(path);
      assertEquals("Invalid filetype computed for path [" + path + "]", testData[FILETYPE], fileInfo.getFileType());
      assertEquals("Invalid basename computed for path [" + path + "]", testData[BASENAME], fileInfo.getBasename());
      assertEquals("Invalid filename computed for path [" + path + "]", testData[FILENAME], fileInfo.getFilename());
      assertEquals("Invalid extension computed for path [" + path + "]", testData[EXTENSION], fileInfo.getExtension());
      assertEquals("Invalid locale computed for path [" + path + "]", testData[LOCALE], fileInfo.getLocale());
    }
  }
}
