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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.plugin.services.importer.mimeType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.test.platform.engine.core.MicroPlatform;

public class MimeTypeListFactoryTest {

  @Test
  public void testGetMimeTypeList() throws Exception {
    MicroPlatform microPlatform = new MicroPlatform();

    StandaloneSpringPentahoObjectFactory objectFactory = new StandaloneSpringPentahoObjectFactory();
    objectFactory.init( "test-res/MimeTypeFactoryTest/MimeTypeFactoryTest.spring.xml", null );
    PentahoSystem.registerObjectFactory( objectFactory );

    MimeTypeListFactory factory =
        new MimeTypeListFactory( "test-res/MimeTypeFactoryTest/ImportHandlerMimeTypeDefinitions.xml" );

    List<MimeType> list1 =
        factory.createMimeTypeList( "org.pentaho.platform.plugin.services.importer.RepositoryFileImportFileHandler" );
    assertNotNull( list1 );

    List<MimeType> list2 = factory.createMimeTypeList( "this.one.is.not.present" );
    assertNotNull( list2 );
    assertEquals( 0, list2.size() );

    List<MimeType> list3 = factory.createMimeTypeList( "org.pentaho.platform.plugin.services.importer.SolutionImportHandler" );
    assertNotNull( list3 );
    assertTrue( list3.size() > 0 );
  }
}
