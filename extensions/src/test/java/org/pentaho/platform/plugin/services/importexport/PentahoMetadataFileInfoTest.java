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

package org.pentaho.platform.plugin.services.importexport;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;

/**
 * Class Description
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class PentahoMetadataFileInfoTest extends TestCase {
  private static final PentahoMetadataFileInfo.FileType UNKNOWN = PentahoMetadataFileInfo.FileType.UNKNOWN;
  private static final PentahoMetadataFileInfo.FileType XMI = PentahoMetadataFileInfo.FileType.XMI;
  private static final PentahoMetadataFileInfo.FileType PROPERTIES = PentahoMetadataFileInfo.FileType.PROPERTIES;
  private static final int PATH = 0;
  private static final int DOMAIN = 1;

  private static final int LOCALE = 2;

  private static String[] UNKNOWN_DATA =
      new String[] { null, "", " ", "metadata.xmi", "//\\//\\//\\//\\//\\//\\", "/metadata.xmi", "//metadata.xmi",
        "admin/resources/metadata/.xmi", "pentaho-solutions/steel-wheels/metadata.properties",
        "pentaho-solutions/steel-wheels/metadata_en_.properties",
        "pentaho-solutions/steel-wheels/metadata_en_US_.properties",
        "test/admin/resource/metadata/metadata.properties", "test/admin/resource/metadata/metadata_en_.properties",
        "test/admin/resource/metadata/metadata_en_US_.properties", };

  private static String[][] XMI_DATA = new String[][] {
    new String[] { "steel-wheels/metadata.xmi", "steel-wheels" },
    new String[] { "/steel-wheels/metadata.xmi", "steel-wheels" },
    new String[] { "pentaho-solutions/steel-wheels/metadata.xmi", "steel-wheels" },
    new String[] { "/pentaho-solutions/steel-wheels/metadata.xmi", "steel-wheels" },
    new String[] { "\\pentaho-solutions\\steel-wheels\\metadata.xmi", "steel-wheels" },

    new String[] { "admin/resources/metadata/Sample Metadata_Name.xmi",
      "admin/resources/metadata/Sample Metadata_Name.xmi" },
    new String[] { "/admin/resources/metadata/Sample Metadata_Name.xmi",
      "admin/resources/metadata/Sample Metadata_Name.xmi" },
    new String[] { "with space/resources/metadata/Sample Metadata_Name.xmi",
      "with space/resources/metadata/Sample Metadata_Name.xmi" },
    new String[] { "/with space/resources/metadata/Sample Metadata_Name.xmi",
      "with space/resources/metadata/Sample Metadata_Name.xmi" },
    new String[] { "/one/two/resources/metadata/Sample Metadata_Name.xmi",
      "two/resources/metadata/Sample Metadata_Name.xmi" },
    new String[] { "one/two/resources/metadata/Sample Metadata_Name.xmi",
      "two/resources/metadata/Sample Metadata_Name.xmi" }, };

  private static String[][] LOCALE_DATA = new String[][] {
    new String[] { "steel-wheels/metadata_en_US.properties", "steel-wheels", "en_US" },
    new String[] { "/steel-wheels/metadata_en_US.properties", "steel-wheels", "en_US" },
    new String[] { "pentaho-solutions/steel-wheels/metadata_en_US.properties", "steel-wheels", "en_US" },
    new String[] { "/pentaho-solutions/steel-wheels/metadata_en_US.properties", "steel-wheels", "en_US" },
    new String[] { "\\pentaho-solutions\\steel-wheels\\metadata_en_US.properties", "steel-wheels", "en_US" },

    new String[] { "admin/resources/metadata/Sample Metadata_Name_en_US.properties",
      "admin/resources/metadata/Sample Metadata_Name.xmi", "en_US" },
    new String[] { "/admin/resources/metadata/Sample Metadata_Name_en_US.properties",
      "admin/resources/metadata/Sample Metadata_Name.xmi", "en_US" },
    new String[] { "with space/resources/metadata/Sample Metadata_Name_en_US.properties",
      "with space/resources/metadata/Sample Metadata_Name.xmi", "en_US" },
    new String[] { "/with space/resources/metadata/Sample Metadata_Name_en_US.properties",
      "with space/resources/metadata/Sample Metadata_Name.xmi", "en_US" },
    new String[] { "/one/two/resources/metadata/Sample Metadata_Name_en_US.properties",
      "two/resources/metadata/Sample Metadata_Name.xmi", "en_US" },
    new String[] { "one/two/resources/metadata/Sample Metadata_Name_en_US.properties",
      "two/resources/metadata/Sample Metadata_Name.xmi", "en_US" }, };

  public void testParsing() throws Exception {
    // final String data = "/admin/resources/metadata/Sample Metadata_Name_en_US.properties";
    // final Matcher matcher =
    // Pattern.compile(".*/([^/]+/resources/metadata/[^/]+)_([a-z]{2}_[A-Z]{2})\\.properties$").matcher(data);
    // assertTrue(matcher.matches());
    // System.err.println(matcher.group(1));
    // System.err.println(matcher.group(2));

    // Test non-metadata files
    for ( final String testData : UNKNOWN_DATA ) {
      final PentahoMetadataFileInfo fileInfo = new PentahoMetadataFileInfo( testData );
      assertEquals( "Invalid filetype computed for path [" + testData + "]", UNKNOWN, fileInfo.getFileType() );
      assertNull( fileInfo.getDomainId() );
      assertNull( fileInfo.getLocale() );
      assertTrue( !StringUtils.isEmpty( fileInfo.toString() ) );
    }

    // Test metadata xmi files
    for ( final String[] testData : XMI_DATA ) {
      final String path = testData[PATH];
      final PentahoMetadataFileInfo fileInfo = new PentahoMetadataFileInfo( path );
      assertEquals( "Invalid filetype computed for path [" + path + "]", XMI, fileInfo.getFileType() );
      assertEquals( "Invalid domain id computed for path [" + path + "]", testData[DOMAIN], fileInfo.getDomainId() );
      assertNull( fileInfo.getLocale() );
      assertTrue( !StringUtils.isEmpty( fileInfo.toString() ) );
    }

    // Test metadata property bundle files
    for ( final String[] testData : LOCALE_DATA ) {
      final String path = testData[PATH];
      final PentahoMetadataFileInfo fileInfo = new PentahoMetadataFileInfo( path );
      assertEquals( "Invalid filetype computed for path [" + path + "]", PROPERTIES, fileInfo.getFileType() );
      assertEquals( "Invalid domain id computed for path [" + path + "]", testData[DOMAIN], fileInfo.getDomainId() );
      assertEquals( "Invalid locale computed for path [" + path + "]", testData[LOCALE], fileInfo.getLocale() );
      assertTrue( !StringUtils.isEmpty( fileInfo.toString() ) );
    }
  }
}
