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

import org.apache.logging.log4j.Level;
import org.junit.Test;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.core.mimetype.MimeType;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * User: nbaker Date: 6/13/12
 */
public class PlatformImporterTest {

  @Test
  public void testNoMatchingMime() throws Exception {

    IPlatformImportHandler mockImportHandler = mock( IPlatformImportHandler.class );
    when( mockImportHandler.getMimeTypes() ).thenReturn( Collections.<IMimeType>emptyList() );
    List<IPlatformImportHandler> handlers = new ArrayList<IPlatformImportHandler>();
    handlers.add( mockImportHandler );

    NameBaseMimeResolver nameResolver = new NameBaseMimeResolver();
    PentahoSystem.registerObject( nameResolver );

    PentahoPlatformImporter importer =
        new PentahoPlatformImporter( handlers, new DefaultRepositoryContentConverterHandler(
            new HashMap<String, Converter>() ) );
    importer.setDefaultHandler( mockImportHandler );

    FileInputStream in = new FileInputStream( new File( TestResourceLocation.TEST_RESOURCES + "/ImportTest/steel-wheels.xmi" ) );

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
      importer.getRepositoryImportLogger().setPerformingRestore( false );
      importer.importFile( bundle1 );
      String result = new String( outputStream.toByteArray() );
      assertTrue( result.contains( "Start Import Job" ) ); // Logged at INFO level
      assertTrue( result.contains( "Error computing or retrieving mime-type" ) ); // Logged at ERROR level
    } catch ( PlatformImportException e ) {
      e.printStackTrace();
      return;
    }
    importLogger.endJob();
  }

  @Test
  public void testMatchingMimeAndHandler() throws Exception {

    List<IMimeType> mimeList = Collections.singletonList( (IMimeType) new MimeType( "text/xmi+xml", "xmi" ) );
    IPlatformImportHandler mockImportHandler = mock( IPlatformImportHandler.class );
    when( mockImportHandler.getMimeTypes() ).thenReturn( mimeList );
    List<IPlatformImportHandler> handlers = Collections.singletonList( mockImportHandler );

    NameBaseMimeResolver nameResolver = new NameBaseMimeResolver();
    PentahoSystem.registerObject( nameResolver );

    // mock logger to prevent npe
    IRepositoryImportLogger importLogger = new Log4JRepositoryImportLogger();

    PentahoPlatformImporter importer =
        new PentahoPlatformImporter( handlers, new DefaultRepositoryContentConverterHandler(
            new HashMap<String, Converter>() ) );

    importer.setRepositoryImportLogger( importLogger );

    FileInputStream in = new FileInputStream( new File( TestResourceLocation.TEST_RESOURCES + "/ImportTest/steel-wheels.xmi" ) );

    // With custom domain id
    final IPlatformImportBundle bundle1 =
        ( new RepositoryFileImportBundle.Builder().input( in ).charSet( "UTF-8" ).hidden( false ).mime( "text/xmi+xml" )
            .name( "steel-wheels.xmi" ).comment( "Test Metadata Import" ).withParam( "domain-id",
                "parameterized-domain-id" ) ).build();

    importer.importFile( bundle1 );

    verify( mockImportHandler, times( 1 ) ).importFile( bundle1 );
  }

  @Test
  public void testUseDefaultHandler() throws Exception {

    List<IMimeType> mimeList = Collections.singletonList( (IMimeType) new MimeType( "text/html", "html" ) );
    IPlatformImportHandler mockImportHandler = mock( IPlatformImportHandler.class );
    when( mockImportHandler.getMimeTypes() ).thenReturn( mimeList );
    List<IPlatformImportHandler> handlers = Collections.singletonList( mockImportHandler );

    NameBaseMimeResolver nameResolver = new NameBaseMimeResolver();
    PentahoSystem.registerObject( nameResolver );

    // mock logger to prevent npe
    IRepositoryImportLogger importLogger = new Log4JRepositoryImportLogger();

    PentahoPlatformImporter importer =
        new PentahoPlatformImporter( handlers, new DefaultRepositoryContentConverterHandler(
            new HashMap<String, Converter>() ) );


    IPlatformImportHandler mockDefaultImportHandler = mock( IPlatformImportHandler.class );
    importer.setDefaultHandler( mockDefaultImportHandler );
    importer.setRepositoryImportLogger( importLogger );

    FileInputStream in = new FileInputStream( new File( TestResourceLocation.TEST_RESOURCES + "/ImportTest/steel-wheels.xmi" ) );

    // With custom domain id
    final IPlatformImportBundle bundle1 =
        ( new RepositoryFileImportBundle.Builder().input( in ).charSet( "UTF-8" ).hidden( false ).mime( "text/xmi+xml" )
            .name( "steel-wheels.xmi" ).comment( "Test Metadata Import" ).withParam( "domain-id",
                "parameterized-domain-id" ) ).build();

    importer.importFile( bundle1 );

    verify( mockDefaultImportHandler, times( 1 ) ).importFile( bundle1 );
  }
}
