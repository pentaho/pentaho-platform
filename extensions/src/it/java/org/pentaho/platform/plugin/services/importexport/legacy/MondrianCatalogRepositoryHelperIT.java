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
