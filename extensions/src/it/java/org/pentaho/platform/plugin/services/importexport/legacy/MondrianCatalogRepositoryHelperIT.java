/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport.legacy;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.MondrianSchemaAnnotator;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MondrianCatalogRepositoryHelperIT {
  @BeforeClass
  public static void setUpClass() throws Exception {
    MicroPlatform platform = new MicroPlatform( TestResourceLocation.TEST_RESOURCES + "/solution" );
    platform.defineInstance( "inlineModeling", new MondrianSchemaAnnotator() {
      @Override public InputStream getInputStream(
        final InputStream schemaInputStream, final InputStream annotationsInputStream ) {
        return new java.io.SequenceInputStream( schemaInputStream, annotationsInputStream );
      }
    } );
    platform.start();
  }

  @Test
  public void testSchemaFilesWithoutAnnotations() throws Exception {
    FileSystemBackedUnifiedRepository repository =
        new FileSystemBackedUnifiedRepository( TestResourceLocation.TEST_RESOURCES + "/MondrianCatalogRepositoryHelperTest" );
    MondrianCatalogRepositoryHelper helper = new MondrianCatalogRepositoryHelper( repository );
    Map<String, InputStream> schemaFiles = helper.getModrianSchemaFiles( "sample" );
    assertEquals( 1, schemaFiles.size() );
    assertSchemaFile(
        TestResourceLocation.TEST_RESOURCES + "/MondrianCatalogRepositoryHelperTest/etc/mondrian/sample/schema.xml", schemaFiles.get( "schema.xml" ) );
  }

  @Test
  public void testSchemaFilesContainsAnnotatedSchema() throws Exception {

    FileSystemBackedUnifiedRepository repository =
        new FileSystemBackedUnifiedRepository( TestResourceLocation.TEST_RESOURCES + "/MondrianCatalogRepositoryHelperTest" );
    MondrianCatalogRepositoryHelper helper = new MondrianCatalogRepositoryHelper( repository );
    Map<String, InputStream> schemaFiles = helper.getModrianSchemaFiles( "food" );
    assertEquals( 3, schemaFiles.size() );
    assertSchemaFile(
        TestResourceLocation.TEST_RESOURCES + "/MondrianCatalogRepositoryHelperTest/etc/mondrian/food/schema.xml",
        schemaFiles.get( "schema.xml" ) );
    assertSchemaFile(
        TestResourceLocation.TEST_RESOURCES + "/MondrianCatalogRepositoryHelperTest/etc/mondrian/food/annotations.xml",
        schemaFiles.get( "annotations.xml" ) );
    assertSchemaFile(
        TestResourceLocation.TEST_RESOURCES + "/MondrianCatalogRepositoryHelperTest/schema.annotated.xml", schemaFiles.get( "schema.annotated.xml" ) );
  }

  private void assertSchemaFile( final String expectedPAth, final InputStream actualSchemaFile ) throws IOException {
    assertEquals(
        IOUtils.toString(
          new FileInputStream(
            new File( expectedPAth ) ) ),
        IOUtils.toString( actualSchemaFile ) );
  }
}
