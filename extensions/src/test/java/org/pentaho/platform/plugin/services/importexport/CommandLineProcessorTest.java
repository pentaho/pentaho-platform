/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.plugin.services.importexport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.MockedStatic;
import org.pentaho.platform.engine.core.output.MultiOutputStream;
import org.pentaho.platform.plugin.services.importexport.CommandLineProcessor.RequestType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.when;


@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class CommandLineProcessorTest extends Assert {

  private static final PrintStream CONSOLE_OUT = System.out;
  private static final ByteArrayOutputStream CONSOLE_BUFFER = new ByteArrayOutputStream();
  private static boolean brokenConstructor = false;

  private static final String[] ARGS_INVALID = {
    "--import",
    "--url=http://localhost:8080/pentaho",
    "--username=admin",
    "--password=password",
    "-charset=UTF-8",
    "--path=/home/admin/2w",
    "--file-path=D:/test.zip",
    "--overwrite=true",
    "--retainOwnership=true",
    "--permission=false"
  };

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
  }

  private void testRequestType( String keyKey, String nameKey, RequestType rt ) throws ParseException {
    CommandLineProcessor clp = createCommandLineProcessor( keyKey, nameKey );
    assertEquals( rt, clp.getRequestType() );
  }

  private CommandLineProcessor createCommandLineProcessor( String keyKey, String nameKey ) throws ParseException {
    return new CommandLineProcessor( new String[] { getOption( "-", keyKey ), getOption( "--", nameKey ) } );
  }

  private String getOption( String key ) {
    return getOption( "", key );
  }

  private String getOption( String prefix, String key ) {
    if ( key == null ) {
      return "";
    }
    return prefix + key;
  }

  @Test
  public void get001ConstructorTest() throws ParseException {
    try {
      testRequestType( "h", null, RequestType.HELP );
      testRequestType( "rest", null, RequestType.REST );
      testRequestType( "backup", null, RequestType.BACKUP );
      testRequestType( "restore", null, RequestType.RESTORE );
      testRequestType( "i", null, RequestType.IMPORT );
      testRequestType( "e", null, RequestType.EXPORT );

      testRequestType( null, "help", RequestType.HELP );
      testRequestType( null, "rest", RequestType.REST );
      testRequestType( null, "backup", RequestType.BACKUP );
      testRequestType( null, "restore", RequestType.RESTORE );
      testRequestType( null, "import", RequestType.IMPORT );
      testRequestType( null, "export", RequestType.EXPORT );

      try {
        createCommandLineProcessor( null, null );
        fail();
      } catch ( ParseException e ) {
        // expected
      }

    } catch ( Exception e ) {
      brokenConstructor = true;
      throw e;
    }
  }

  private static void depensOnConstructor() {
    if ( brokenConstructor ) {
      throw new RuntimeException( "Constructor 'CommandLineProcessor' broken" );
    }
  }

  @Test
  public void get002PrintHelpTest() throws IllegalAccessException {
    Options options = (Options) FieldUtils.readDeclaredStaticField( CommandLineProcessor.class, "options", true );
    @SuppressWarnings( "unchecked" )
    java.util.Set<String> advancedKeys =
        (java.util.Set<String>) FieldUtils.readDeclaredStaticField( CommandLineProcessor.class, "ADVANCED_OPTION_KEYS", true );
    @SuppressWarnings( "unchecked" )
    Collection<Option> optionCollection = options.getOptions();

    // Default help lists the common options but hides the advanced per-component
    // backup/restore flags (those are shown only with --help-advanced).
    CommandLineProcessor.printHelp();
    String defaultHelp = CONSOLE_BUFFER.toString();
    for ( Option option : optionCollection ) {
      String longToken = option.getLongOpt() != null ? "--" + option.getLongOpt() : "-" + option.getOpt();
      if ( advancedKeys.contains( option.getOpt() ) ) {
        assertFalse( "Advanced option " + longToken + " must be hidden from the default help",
            defaultHelp.contains( longToken ) );
      } else {
        assertTrue( "Option -" + option.getOpt() + " must appear in the default help",
            defaultHelp.contains( "-" + option.getOpt() ) );
      }
    }

    // Advanced help (--help-advanced) lists every option, including the advanced ones.
    CONSOLE_BUFFER.reset();
    CommandLineProcessor.printHelp( true );
    String advancedHelp = CONSOLE_BUFFER.toString();
    for ( Option option : optionCollection ) {
      assertTrue( "Option -" + option.getOpt() + " must appear in the advanced help",
          advancedHelp.contains( "-" + option.getOpt() ) );
    }
  }

  @Test
  public void get003WriteFiletest() throws Exception {
    String exception = "Throw exception on purpose to test the writeFile() method.";

    try ( MockedStatic<Client> clientMock = mockStatic( Client.class ) ) {
      Client mockClient = mock( Client.class );
      WebResource mockWebResource = mock( WebResource.class );
      when( mockWebResource.type( nullable( String.class ) ) ).thenThrow( new RuntimeException( exception ) );
      when( mockClient.resource( nullable( String.class ) ) ).thenReturn( mockWebResource );
      clientMock.when( () -> Client.create( any() ) ).thenReturn( mockClient );

      File file = File.createTempFile( "CommandLineProcessorTest", ".log" );

      String[] unknownHostUrl = {
        "--import",
        "--url=http://test/pentaho",
        "--username=admin",
        "--password=password",
        "--path=/test/test/test",
        "--file-path=" + file.getAbsolutePath(),
        "--logfile=" + file.getAbsolutePath()};

      //When an unknown host URL is used with an import, the private method writeFile() is called.
      CommandLineProcessor.main( unknownHostUrl );
      try {
        assertEquals( exception, FileUtils.readFileToString( file ) );
      } finally {
        file.delete();
      }
      assertTrue( CONSOLE_BUFFER.toString().contains( exception ) );
    }
  }

  @Test
  public void get004GetOptionValueTest() throws ParseException {
    depensOnConstructor();

    String requestType = getOption( "-", "i" );
    String shortOption = getOption( "u" );
    String longOption = getOption( "username" );

    CommandLineProcessor clp = new CommandLineProcessor( new String[] { requestType, "-" + shortOption + "=value" } );
    assertEquals( "value", clp.getOptionValue( shortOption, false, true ) );
    assertEquals( "value", clp.getOptionValue( longOption, false, true ) );
    assertNull( clp.getOptionValue( "wrongKey", false, true ) );
    try {
      assertNull( clp.getOptionValue( "wrongKey", true, true ) );
      fail();
    } catch ( ParseException e ) {
      // expected
    }

    try {
      assertNull( clp.getOptionValue( "wrongKey", false, false ) );
      fail();
    } catch ( ParseException e ) {
      // expected
    }

    assertEquals( "value", clp.getOptionValue( longOption, false, true ) );
    assertNull( clp.getOptionValue( "wrongKey", false, true ) );
    try {
      assertNull( clp.getOptionValue( "wrongKey", true, true ) );
      fail();
    } catch ( ParseException e ) {
      // expected
    }

    try {
      assertNull( clp.getOptionValue( "wrongKey", false, false ) );
      fail();
    } catch ( ParseException e ) {
      // expected
    }
  }

  @Test
  public void get005ConfigFileDefaultsTest() throws Exception {
    depensOnConstructor();

    File config = File.createTempFile( "CommandLineProcessorConfig", ".properties" );
    try {
      FileUtils.writeStringToFile( config,
          "username=configUser\n"
              + "url=http://config-host:8080/pentaho\n"
              + "logLevel=DEBUG\n",
          java.nio.charset.StandardCharsets.UTF_8 );

      // Value supplied on the command line wins over the config file.
      CommandLineProcessor cliWins = new CommandLineProcessor( new String[] {
        getOption( "-", "i" ),
        "--config=" + config.getAbsolutePath(),
        "--username=cliUser" } );
      assertEquals( "cliUser", cliWins.getOptionValue( "username", true, false ) );

      // Missing on the command line -> value falls back to the config file.
      CommandLineProcessor configWins = new CommandLineProcessor( new String[] {
        getOption( "-", "i" ),
        "--config=" + config.getAbsolutePath() } );
      assertEquals( "configUser", configWins.getOptionValue( "username", true, false ) );
      assertEquals( "http://config-host:8080/pentaho", configWins.getOptionValue( "url", true, false ) );
      assertEquals( "DEBUG", configWins.getOptionValue( "logLevel", false, true ) );

      // Keys absent from both CLI and config still resolve to null.
      assertNull( configWins.getOptionValue( "password", false, true ) );
    } finally {
      config.delete();
    }
  }

  @Test
  public void get006MissingConfigFileWarnsButDoesNotFailTest() throws Exception {
    depensOnConstructor();

    String missingPath = new File( System.getProperty( "java.io.tmpdir" ),
        "pex-no-such-config-" + System.nanoTime() + ".properties" ).getAbsolutePath();

    // An explicitly named but missing config file must not break construction;
    // option resolution simply falls through to its normal (null) result.
    CommandLineProcessor clp = new CommandLineProcessor( new String[] {
      getOption( "-", "i" ),
      "--config=" + missingPath,
      "--username=cliUser" } );

    assertEquals( "cliUser", clp.getOptionValue( "username", true, false ) );
    assertNull( clp.getOptionValue( "url", false, true ) );
  }

  @Test
  public void testInvalidCharset() throws Exception {
    CommandLineProcessor.main( ARGS_INVALID );
    Assert.assertEquals( IllegalArgumentException.class, CommandLineProcessor.getException().getClass() );
  }

  @Test
  public void testInvalidArgument1() throws Exception {
    String[] invalid = { "--import", "--foo=wibble" };
    CommandLineProcessor.main( invalid );
    Assert.assertEquals( UnrecognizedOptionException.class, CommandLineProcessor.getException().getClass() );
  }

  @Test
  public void testInvalidArgument2() throws Exception {
    String[] invalid = { "--import", "-boo=foo" };
    CommandLineProcessor.main( invalid );
    Assert.assertEquals( UnrecognizedOptionException.class, CommandLineProcessor.getException().getClass() );
  }
}
