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


package org.pentaho.platform.plugin.services.importer.mimeType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.core.mimetype.MimeType;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;

public class MimeTypeListFactoryIT {

  @Test
  public void testGetMimeTypeList() throws Exception {
    MicroPlatform microPlatform = new MicroPlatform();

    StandaloneSpringPentahoObjectFactory objectFactory = new StandaloneSpringPentahoObjectFactory();
    objectFactory.init( TestResourceLocation.TEST_RESOURCES + "/MimeTypeFactoryTest/MimeTypeFactoryTest.spring.xml", null );
    PentahoSystem.registerObjectFactory( objectFactory );

    MimeTypeListFactory factory =
        new MimeTypeListFactory( TestResourceLocation.TEST_RESOURCES + "/MimeTypeFactoryTest/ImportHandlerMimeTypeDefinitions.xml" );

    List<IMimeType> list1 =
        factory.createMimeTypeList( "org.pentaho.platform.plugin.services.importer.RepositoryFileImportFileHandler" );
    assertNotNull( list1 );

    list1 = factory.createMimeTypeList( "this.one.is.not.present" );
    assertNotNull( list1 );
    assertEquals( 0, list1.size() );

    list1 = factory.createMimeTypeList( "org.pentaho.platform.plugin.services.importer.SolutionImportHandler" );
    assertNotNull( list1 );
    assertTrue( list1.size() > 0 );
    
    list1 = factory.createMimeTypeList( "com.pentaho.repository.importexport.PDIImportFileHandler" );
    assertNotNull( list1 );
    assert ( list1.contains( new MimeType( "application/vnd.pentaho.job", "ktr" ) ) );
  }
}
