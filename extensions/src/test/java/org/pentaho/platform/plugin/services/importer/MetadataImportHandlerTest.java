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


package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.core.mimetype.MimeType;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryImporter;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * @author Andrei Abramov
 */
@RunWith( MockitoJUnitRunner.class )
public class MetadataImportHandlerTest {

  IPentahoMetadataDomainRepositoryImporter metadataImporter;

  MetadataImportHandler metadataHandler;

  PentahoPlatformImporter importer;

  @Before
  public void setUp() throws Exception {

    metadataImporter = mock( IPentahoMetadataDomainRepositoryImporter.class );

    List<IMimeType> mimeList = new ArrayList<IMimeType>();
    mimeList.add( new MimeType( "text/xmi+xml", "xmi" ) );

    metadataHandler = new MetadataImportHandler( mimeList, metadataImporter );
  }

  @Test
  public void testBiserver13711() throws Exception {
    // 13711.xmi is exported "Reporting Only" DSW and contains only one logical model which is not OLAP
    // When this datasource is imported back it should be imported as "metadata" not as "DSW".
    // In order to achieve it the xmi shouldn't contain next three properties:
    // "AGILE_BI_GENERATED_SCHEMA", "AGILE_BI_VERSION", "WIZARD_GENERATED_SCHEMA"
    // see more http://jira.pentaho.com/browse/BISERVER-13711

    String xmi = null;
    FileInputStream in = new FileInputStream( new File( TestResourceLocation.TEST_RESOURCES + "/ImportTest/13711.xmi" ) );
    byte[] is = IOUtils.toByteArray( in );
    xmi = new String( is, "UTF-8" );

    Assert.assertTrue( xmi.contains( "AGILE_BI_GENERATED_SCHEMA" ) );
    Assert.assertTrue( xmi.contains( "AGILE_BI_VERSION" ) );
    Assert.assertTrue( xmi.contains( "WIZARD_GENERATED_SCHEMA" ) );

    in = new FileInputStream( new File( TestResourceLocation.TEST_RESOURCES + "/ImportTest/13711.xmi" ) );
    InputStream processedStream = metadataHandler.StripDswFromStream( in );
    is = IOUtils.toByteArray( processedStream );
    xmi = new String( is, "UTF-8" );

    Assert.assertFalse( xmi.contains( "AGILE_BI_GENERATED_SCHEMA" ) );
    Assert.assertFalse( xmi.contains( "AGILE_BI_VERSION" ) );
    Assert.assertFalse( xmi.contains( "WIZARD_GENERATED_SCHEMA" ) );
  }

}
