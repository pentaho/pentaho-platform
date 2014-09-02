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

package org.pentaho.platform.plugin.services.importer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.test.platform.engine.core.MicroPlatform;

/**
 * User: nbaker Date: 6/13/12
 */
public class PlatformImporterTest {

  @Test
  public void testNoMatchingMime() throws Exception {

    List<MimeType> mimeList = new ArrayList<MimeType>();
    IPlatformImportHandler mockImportHandler = mock( IPlatformImportHandler.class );
    when( mockImportHandler.getMimeTypes() ).thenReturn( mimeList );
    List<IPlatformImportHandler> handlers = new ArrayList<IPlatformImportHandler>();
    handlers.add( mockImportHandler );

    MicroPlatform microPlatform = new MicroPlatform();
    NameBaseMimeResolver nameResolver = new NameBaseMimeResolver();
    microPlatform.defineInstance( IPlatformImportMimeResolver.class, nameResolver );

    PentahoPlatformImporter importer =
        new PentahoPlatformImporter( handlers, new DefaultRepositoryContentConverterHandler(
            new HashMap<String, Converter>() ) );
    importer.setDefaultHandler( mockImportHandler );

    FileInputStream in = new FileInputStream( new File( "test-res/ImportTest/steel-wheels.xmi" ) );

    Log4JRepositoryImportLogger importLogger = new Log4JRepositoryImportLogger();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    importLogger.startJob( outputStream, "", Level.DEBUG );


    // With custom domain id
    final IPlatformImportBundle bundle1 =
        ( new RepositoryFileImportBundle.Builder().input( in ).charSet( "UTF-8" ).hidden( false ).overwriteFile( true )
            .name( "steel-wheels.xmi" ).comment( "Test Metadata Import" ).withParam( "domain-id",
                "parameterized-domain-id" ) ).build();

    try {
      importer.setRepositoryImportLogger( importLogger );
      importer.importFile( bundle1 );
      String result = new String( outputStream.toByteArray() );
      Assert.assertTrue( result.contains( "Error computing or retrieving mime-type" ) );
    } catch ( PlatformImportException e ) {
      e.printStackTrace();
      return;
    }
    importLogger.endJob();
  }

  @Test
  public void testMatchingMimeAndHandler() throws Exception {

    List<MimeType> mimeList = new ArrayList<MimeType>();
    mimeList.add( new MimeType( "text/xmi+xml", "xmi" ) );
    IPlatformImportHandler mockImportHandler = mock( IPlatformImportHandler.class );
    when( mockImportHandler.getMimeTypes() ).thenReturn( mimeList );
    List<IPlatformImportHandler> handlers = new ArrayList<IPlatformImportHandler>();
    handlers.add( mockImportHandler );

    MicroPlatform microPlatform = new MicroPlatform();
    NameBaseMimeResolver nameResolver = new NameBaseMimeResolver();
    microPlatform.defineInstance( IPlatformImportMimeResolver.class, nameResolver );

    // mock logger to prevent npe
    IRepositoryImportLogger importLogger = new Log4JRepositoryImportLogger();

    PentahoPlatformImporter importer =
        new PentahoPlatformImporter( handlers, new DefaultRepositoryContentConverterHandler(
            new HashMap<String, Converter>() ) );

    importer.setRepositoryImportLogger( importLogger );

    FileInputStream in = new FileInputStream( new File( "test-res/ImportTest/steel-wheels.xmi" ) );

    // With custom domain id
    final IPlatformImportBundle bundle1 =
        ( new RepositoryFileImportBundle.Builder().input( in ).charSet( "UTF-8" ).hidden( false ).mime( "text/xmi+xml" )
            .name( "steel-wheels.xmi" ).comment( "Test Metadata Import" ).withParam( "domain-id",
                "parameterized-domain-id" ) ).build();

    importer.importFile( bundle1 );

    verify( mockImportHandler, times( 1 ) ).importFile( bundle1 );
  }
}
