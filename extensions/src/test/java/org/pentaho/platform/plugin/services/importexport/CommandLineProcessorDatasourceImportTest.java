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


package org.pentaho.platform.plugin.services.importexport;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.engine.core.output.MultiOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit tests for CommandLineProcessor datasource import response message parsing functionality
 */
public class CommandLineProcessorDatasourceImportTest extends Assert {

  private static final PrintStream CONSOLE_OUT = System.out;
  private static final ByteArrayOutputStream CONSOLE_BUFFER = new ByteArrayOutputStream();

  @BeforeClass
  public static void setUp() throws Exception {
    Locale.setDefault( Locale.US );
    OutputStream multiOut = new MultiOutputStream( new OutputStream[] { CONSOLE_BUFFER, CONSOLE_OUT } );
    PrintStream ps = new PrintStream( multiOut );
    System.setOut( ps );
  }

  @AfterClass
  public static void tearDown() throws Exception {
    System.setOut( CONSOLE_OUT );
  }


  @Before
  public void before() {
    CONSOLE_BUFFER.reset();
  }

  @Test
  public void testLogResponseMessageSeparatesAppendedResponses() throws Exception {
    File logFile = File.createTempFile( "CommandLineProcessorDatasourceImportTest", ".log" );
    ClientResponse firstResponse = mock( ClientResponse.class );
    ClientResponse secondResponse = mock( ClientResponse.class );
    when( firstResponse.getStatus() ).thenReturn( 200 );
    when( firstResponse.hasEntity() ).thenReturn( true );
    when( firstResponse.getEntity( String.class ) ).thenReturn( "<html>first</html>" );
    when( secondResponse.getStatus() ).thenReturn( 200 );
    when( secondResponse.hasEntity() ).thenReturn( true );
    when( secondResponse.getEntity( String.class ) ).thenReturn( "<html>second</html>" );

    try {
      invokeLogResponseMessage( logFile.getAbsolutePath(), "/test/path", firstResponse,
        CommandLineProcessor.RequestType.IMPORT );
      invokeLogResponseMessage( logFile.getAbsolutePath(), "/test/path", secondResponse,
        CommandLineProcessor.RequestType.IMPORT );

      String contents = FileUtils.readFileToString( logFile, StandardCharsets.UTF_8 );
      assertTrue( contents.contains( "</html>" + System.lineSeparator() + "Import was successful" ) );
      assertTrue( contents.endsWith( System.lineSeparator() ) );
    } finally {
      logFile.delete();
    }
  }


  /**
   * Invokes package-private logResponseMessage method
   */
  private void invokeLogResponseMessage( String logFile, String path, ClientResponse response,
                                         CommandLineProcessor.RequestType requestType ) throws Exception {
    CommandLineProcessor clp = new CommandLineProcessor( new String[] { "--help" } );
    clp.logResponseMessage( logFile, path, response, requestType );
  }
}
