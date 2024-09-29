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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

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
    CommandLineProcessor.printHelp();

    String help = CONSOLE_BUFFER.toString();

    Options options = (Options) FieldUtils.readDeclaredStaticField( CommandLineProcessor.class, "options", true );
    @SuppressWarnings( "unchecked" )
    Collection<Option> optionCollection = options.getOptions();
    for ( Option option : optionCollection ) {
      if ( !help.contains( "-" + option.getOpt() ) ) {
        fail();
      }
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
