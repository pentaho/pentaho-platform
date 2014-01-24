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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 * User: pminutillo
 * Date: 1/16/13
 * Time: 4:52 PM
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

public class DefaultExportHandlerTest {
  static RepositoryFile repositoryFile;
  static Map<String, Converter> converters = new HashMap<String, Converter>();
  static StreamConverter streamConverter;
  static InputStream inputStream;

  @BeforeClass
  public static void setUp() throws Exception {
    repositoryFile = mock( RepositoryFile.class );

    when( repositoryFile.getId() ).thenReturn( "1234-1234-12345" );

    // mock input stream
    inputStream = mock( InputStream.class );

    // mock converter and build map
    streamConverter = mock( StreamConverter.class );
    converters.put( "prpt", streamConverter );
    when( streamConverter.convert( "1234-1234-12345" ) ).thenReturn( inputStream );
  }

  @AfterClass
  public static void tearDown() throws Exception {

  }

  @Test
  public void testDoExport() throws Exception {
    when( repositoryFile.getName() ).thenReturn( "Inventory.prpt" );
    String filePath = "/public/pentaho-solutions/steel-wheels/reports/Inventory";
    DefaultExportHandler defaultExportHandler = new DefaultExportHandler();
    defaultExportHandler.setConverters( converters );

    assertEquals( defaultExportHandler.doExport( repositoryFile, filePath ), inputStream );

    // lets make sure the expected methods get invoked
    verify( repositoryFile ).getId();
    verify( repositoryFile, atLeast( 1 ) ).getName();
    verify( streamConverter ).convert( "1234-1234-12345" );
  }

  /**
   * repo files without an exception will be skipped since export depends on an extension
   * 
   * @throws Exception
   */
  @Test
  public void testDoExportNoExtension() throws Exception {
    when( repositoryFile.getName() ).thenReturn( "Inventory" );
    String filePath = "/public/pentaho-solutions/steel-wheels/reports/Inventory";
    DefaultExportHandler defaultExportHandler = new DefaultExportHandler();
    defaultExportHandler.setConverters( converters );

    assertNull( defaultExportHandler.doExport( repositoryFile, filePath ) );
  }
}
