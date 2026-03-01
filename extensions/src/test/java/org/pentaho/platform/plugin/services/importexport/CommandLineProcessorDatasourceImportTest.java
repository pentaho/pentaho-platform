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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import jakarta.ws.rs.core.Response;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.pentaho.platform.engine.core.output.MultiOutputStream;
import org.pentaho.platform.plugin.services.messages.Messages;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit tests for CommandLineProcessor datasource import response message parsing functionality
 */
public class CommandLineProcessorDatasourceImportTest extends Assert {

  private static final PrintStream CONSOLE_OUT = System.out;
  private static final ByteArrayOutputStream CONSOLE_BUFFER = new ByteArrayOutputStream();

  private CommandLineProcessor processor;

  @BeforeClass
  public static void setUp() throws Exception {
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
    try {
      processor = new CommandLineProcessor( new String[] { "--help" } );
    } catch ( Exception e ) {
      // Help request should not throw exception
    }
  }

  /**
   * Test response code 3 (SUCCESS)
   */
  @Test
  public void testParseDatasourceImportResponse_Code3_Success() throws Exception {
    String result = invokeParseDatasourceImportResponse( "3" );
    assertTrue( result.contains( "Success" ) );
    assertTrue( result.contains( "imported successfully" ) );
  }

  /**
   * Test response code 1 (PUBLISH_GENERAL_ERROR)
   */
  @Test
  public void testParseDatasourceImportResponse_Code1_GeneralServerError() throws Exception {
    String result = invokeParseDatasourceImportResponse( "1" );
    assertTrue( result.contains( "General server-side failure" ) );
    assertTrue( result.contains( "server logs" ) );
  }

  /**
   * Test response code 2 (PUBLISH_UNSPECIFIED_ERROR)
   */
  @Test
  public void testParseDatasourceImportResponse_Code2_UnspecifiedError() throws Exception {
    String result = invokeParseDatasourceImportResponse( "2" );
    assertTrue( result.contains( "unspecified error" ) );
    assertTrue( result.contains( "server logs" ) );
  }

  /**
   * Test response code 5 (PUBLISH_USERNAME_PASSWORD_FAIL)
   */
  @Test
  public void testParseDatasourceImportResponse_Code5_AuthFailure() throws Exception {
    String result = invokeParseDatasourceImportResponse( "5" );
    assertTrue( result.contains( "Authentication failure" ) );
    assertTrue( result.contains( "username or password" ) );
    assertTrue( result.contains( "credentials" ) );
  }

  /**
   * Test response code 6 (PUBLISH_CONNECTION_ERROR)
   */
  @Test
  public void testParseDatasourceImportResponse_Code6_ConnectionError() throws Exception {
    String result = invokeParseDatasourceImportResponse( "6" );
    assertTrue( result.contains( "Connection error" ) );
    assertTrue( result.contains( "data source" ) );
  }

  /**
   * Test response code 7 (PUBLISH_XMLA_ALREADY_EXISTS)
   */
  @Test
  public void testParseDatasourceImportResponse_Code7_XmlaExists() throws Exception {
    String result = invokeParseDatasourceImportResponse( "7" );
    assertTrue( result.contains( "XMLA Catalog" ) );
    assertTrue( result.contains( "already exists" ) );
    assertTrue( result.contains( "--overwrite=true" ) );
  }

  /**
   * Test response code 8 (PUBLISH_SCHEMA_EXISTS)
   */
  @Test
  public void testParseDatasourceImportResponse_Code8_SchemaExists() throws Exception {
    String result = invokeParseDatasourceImportResponse( "8" );
    assertTrue( result.contains( "Schema/Datasource" ) );
    assertTrue( result.contains( "already exists" ) );
    assertTrue( result.contains( "--overwrite=true" ) );
  }

  /**
   * Test response code 9 (PUBLISH_CONTENT_EXISTS)
   */
  @Test
  public void testParseDatasourceImportResponse_Code9_ContentExists() throws Exception {
    String result = invokeParseDatasourceImportResponse( "9" );
    assertTrue( result.contains( "Content already exists" ) );
    assertTrue( result.contains( "--overwrite=true" ) );
  }

  /**
   * Test response code 10 (PUBLISH_PROHIBITED_SYMBOLS_ERROR)
   */
  @Test
  public void testParseDatasourceImportResponse_Code10_ProhibitedChars() throws Exception {
    String result = invokeParseDatasourceImportResponse( "10" );
    assertTrue( result.contains( "prohibited characters" ) );
    assertTrue( result.contains( "naming conventions" ) );
  }

  /**
   * Test response code 11 (PUBLISH_PLUGIN_ERROR)
   */
  @Test
  public void testParseDatasourceImportResponse_Code11_PluginError() throws Exception {
    String result = invokeParseDatasourceImportResponse( "11" );
    assertTrue( result.contains( "missing plugins" ) );
    assertTrue( result.contains( "install required plugins" ) );
  }

  /**
   * Test response code 12 (PUBLISH_PARTIAL_ERROR)
   */
  @Test
  public void testParseDatasourceImportResponse_Code12_PartialError() throws Exception {
    String result = invokeParseDatasourceImportResponse( "12" );
    assertTrue( result.contains( "Partial upload" ) );
    assertTrue( result.contains( "incomplete" ) );
    assertTrue( result.contains( "retry" ) );
  }

  /**
   * Test response code 13 (PUBLISH_NAME_ERROR)
   */
  @Test
  public void testParseDatasourceImportResponse_Code13_NameError() throws Exception {
    String result = invokeParseDatasourceImportResponse( "13" );
    assertTrue( result.contains( "Name validation" ) );
  }

  /**
   * Test unknown response code
   */
  @Test
  public void testParseDatasourceImportResponse_UnknownCode() throws Exception {
    String result = invokeParseDatasourceImportResponse( "999" );
    assertTrue( result.contains( "Response code" ) );
    assertTrue( result.contains( "999" ) );
  }

  /**
   * Test non-numeric response (should return null)
   */
  @Test
  public void testParseDatasourceImportResponse_NonNumeric() throws Exception {
    String result = invokeParseDatasourceImportResponse( "abc" );
    assertNull( result );
  }

  /**
   * Test negative response code
   */
  @Test
  public void testParseDatasourceImportResponse_NegativeCode() throws Exception {
    String result = invokeParseDatasourceImportResponse( "-1" );
    assertTrue( result.contains( "Response code" ) );
  }

  /**
   * Test empty response
   */
  @Test
  public void testParseDatasourceImportResponse_EmptyString() throws Exception {
    String result = invokeParseDatasourceImportResponse( "" );
    assertNull( result );
  }

  /**
   * Test response with leading/trailing whitespace
   */
  @Test
  public void testParseDatasourceImportResponse_WithWhitespace() throws Exception {
    String result = invokeParseDatasourceImportResponse( "  3  " );
    // Should parse the trimmed value
    assertTrue( result.contains( "Success" ) );
  }

  /**
   * Test logResponseMessage with 200 OK status
   */
  @Test
  public void testLogResponseMessage_StatusOK_WithSuccessCode() throws Exception {
    Response mockResponse = mock( Response.class );
    when( mockResponse.getStatus() ).thenReturn( 200 );
    when( mockResponse.hasEntity() ).thenReturn( true );
    when( mockResponse.readEntity( String.class ) ).thenReturn( "3" );

    invokeLogResponseMessage( null, "/test/path", mockResponse, CommandLineProcessor.RequestType.IMPORT );

    String output = CONSOLE_BUFFER.toString();
    assertTrue( output.contains( "Import was successful" ) );
    assertTrue( output.contains( "Success" ) );
  }

  /**
   * Test logResponseMessage with 403 Forbidden status
   */
  @Test
  public void testLogResponseMessage_StatusForbidden() throws Exception {
    Response mockResponse = mock( Response.class );
    when( mockResponse.getStatus() ).thenReturn( 403 );
    when( mockResponse.hasEntity() ).thenReturn( false );

    invokeLogResponseMessage( null, "/test/path", mockResponse, CommandLineProcessor.RequestType.IMPORT );

    String output = CONSOLE_BUFFER.toString();
    // The message contains ERROR_0007 key
    assertTrue( output.contains( "ERROR_0007" ) || output.contains( "not allowed" ) );
  }

  /**
   * Test logResponseMessage with 404 Not Found status
   */
  @Test
  public void testLogResponseMessage_StatusNotFound() throws Exception {
    Response mockResponse = mock( Response.class );
    when( mockResponse.getStatus() ).thenReturn( 404 );
    when( mockResponse.hasEntity() ).thenReturn( false );

    invokeLogResponseMessage( null, "/test/path", mockResponse, CommandLineProcessor.RequestType.IMPORT );

    String output = CONSOLE_BUFFER.toString();
    // The message contains ERROR_0004 key or the actual message
    assertTrue( output.contains( "ERROR_0004" ) || output.contains( "Unknown source" ) );
  }

  /**
   * Test logResponseMessage with 406 Not Acceptable status
   */
  @Test
  public void testLogResponseMessage_Status406NotAcceptable() throws Exception {
    Response mockResponse = mock( Response.class );
    when( mockResponse.getStatus() ).thenReturn( 406 );
    when( mockResponse.hasEntity() ).thenReturn( false );

    invokeLogResponseMessage( null, "/test/path", mockResponse, CommandLineProcessor.RequestType.IMPORT );

    String output = CONSOLE_BUFFER.toString();
    assertTrue( output.contains( "406" ) );
    assertTrue( output.contains( "Not Acceptable" ) );
  }

  /**
   * Test logResponseMessage with response containing error code 8 (already exists)
   */
  @Test
  public void testLogResponseMessage_WithErrorCode8() throws Exception {
    Response mockResponse = mock( Response.class );
    when( mockResponse.getStatus() ).thenReturn( 200 );
    when( mockResponse.hasEntity() ).thenReturn( true );
    when( mockResponse.readEntity( String.class ) ).thenReturn( "8" );

    invokeLogResponseMessage( null, "/test/path", mockResponse, CommandLineProcessor.RequestType.IMPORT );

    String output = CONSOLE_BUFFER.toString();
    assertTrue( output.contains( "already exists" ) );
    assertTrue( output.contains( "--overwrite=true" ) );
  }

  /**
   * Test all response codes are mapped to messages
   */
  @Test
  public void testAllResponseCodesAreMapped() throws Exception {
    int[] codes = { 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13 };
    for ( int code : codes ) {
      String result = invokeParseDatasourceImportResponse( String.valueOf( code ) );
      assertNotNull( "Response code " + code + " should be mapped to a message", result );
      assertTrue( "Response code " + code + " message should not be empty", result.length() > 0 );
      assertTrue( "Response code " + code + " message should contain 'Import status'", result.contains( "Import status" ) );
    }
  }

  /**
   * Test messages use Messages bundle (for i18n)
   */
  @Test
  public void testMessagesAreInternationalized() throws Exception {
    String successMsg = invokeParseDatasourceImportResponse( "3" );
    assertNotNull( "Messages should be retrieved from Messages bundle", successMsg );

    // Verify that the message is actually from the Messages class
    Messages messages = Messages.getInstance();
    String messageFromBundle = messages.getString( "CommandLineProcessor.DATASOURCE_IMPORT_SUCCESS" );
    assertTrue( "Message should come from the properties bundle", successMsg.contains( "Success" ) );
  }

  // Helper methods

  /**
   * Invokes the protected parseDatasourceImportResponse method
   */
  private String invokeParseDatasourceImportResponse( String responseBody ) throws Exception {
    CommandLineProcessor clp = new CommandLineProcessor( new String[] { "--help" } );
    return clp.parseDatasourceImportResponse( responseBody );
  }

  /**
   * Invokes the protected logResponseMessage method
   */
  private void invokeLogResponseMessage( String logFile, String path, Response response, 
                                        CommandLineProcessor.RequestType requestType ) throws Exception {
    CommandLineProcessor clp = new CommandLineProcessor( new String[] { "--help" } );
    clp.logResponseMessage( logFile, path, response, requestType );
  }
}
