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
