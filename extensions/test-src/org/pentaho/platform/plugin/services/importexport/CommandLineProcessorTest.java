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
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importexport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.pentaho.platform.engine.core.output.MultiOutputStream;
import org.pentaho.platform.plugin.services.importexport.CommandLineProcessor.RequestType;
import org.pentaho.platform.plugin.services.messages.Messages;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class CommandLineProcessorTest extends Assert {

  private static final String MESSAGE = "message";

  private static final PrintStream CONSOLE_OUT = System.out;
  private static final ByteArrayOutputStream CONSOLE_BUFFER = new ByteArrayOutputStream();

  private static CommandLineProcessor clpMock;

  private static boolean brokenConstructor = false;

  @BeforeClass
  public static void setUp() throws Exception {
    OutputStream multiOut = new MultiOutputStream( new OutputStream[] { CONSOLE_BUFFER, CONSOLE_OUT } );

    PrintStream ps = new PrintStream( multiOut );
    System.setOut( ps );

    MockUp<CommandLineProcessor> mock = new MockUp<CommandLineProcessor>() {
      @Mock
      public void $init( String[] args ) {
      }
    };
    clpMock = new CommandLineProcessor( null );
    mock.tearDown();
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
    return prefix + Messages.getInstance().getString( "CommandLineProcessor." + key );
  }

  @Test
  public void get001ConstructorTest() throws ParseException {
    try {
      testRequestType( "INFO_OPTION_HELP_KEY", null, RequestType.HELP );
      testRequestType( "INFO_OPTION_REST_KEY", null, RequestType.REST );
      testRequestType( "INFO_OPTION_BACKUP_KEY", null, RequestType.BACKUP );
      testRequestType( "INFO_OPTION_RESTORE_KEY", null, RequestType.RESTORE );
      testRequestType( "INFO_OPTION_IMPORT_KEY", null, RequestType.IMPORT );
      testRequestType( "INFO_OPTION_EXPORT_KEY", null, RequestType.EXPORT );

      testRequestType( null, "INFO_OPTION_HELP_NAME", RequestType.HELP );
      testRequestType( null, "INFO_OPTION_REST_NAME", RequestType.REST );
      testRequestType( null, "INFO_OPTION_BACKUP_NAME", RequestType.BACKUP );
      testRequestType( null, "INFO_OPTION_RESTORE_NAME", RequestType.RESTORE );
      testRequestType( null, "INFO_OPTION_IMPORT_NAME", RequestType.IMPORT );
      testRequestType( null, "INFO_OPTION_EXPORT_NAME", RequestType.EXPORT );

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
  public void get002PrintHelpTest() throws IllegalAccessException, IOException {
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
  public void get003WriteFiletest() throws IOException, NoSuchMethodException, IllegalAccessException,
    InvocationTargetException, ParseException {
    File file = File.createTempFile( "CommandLineProcessorTest", ".log" );

    try {
      Deencapsulation.invoke( clpMock, "writeFile", MESSAGE, file.getPath() );
      assertEquals( MESSAGE, FileUtils.readFileToString( file ) );
    } finally {
      file.delete();
    }

    Deencapsulation.invoke( clpMock, "writeFile", MESSAGE, "http://brokenPath" );
    CONSOLE_BUFFER.toString().contains( "IOException" );
  }

  @Test
  public void get004GetOptionValueTest() throws ParseException {
    depensOnConstructor();

    String requestType = getOption( "-", "INFO_OPTION_IMPORT_KEY" );
    String shortOption = getOption( "INFO_OPTION_USERNAME_KEY" );
    String longOption = getOption( "INFO_OPTION_USERNAME_NAME" );

    CommandLineProcessor clp = new CommandLineProcessor( new String[] { requestType, "-" + shortOption + "=value" } );

    assertEquals( "value", clp.getOptionValue( shortOption, "", false, true ) );
    assertEquals( "value", clp.getOptionValue( "", longOption, false, true ) );
    assertEquals( null, clp.getOptionValue( "wrongKey", "", false, true ) );
    try {
      assertEquals( null, clp.getOptionValue( "wrongKey", "", true, true ) );
      fail();
    } catch ( ParseException e ) {
      // expected
    }
    try {
      assertEquals( null, clp.getOptionValue( "wrongKey", "", false, false ) );
      fail();
    } catch ( ParseException e ) {
      // expected
    }

    assertEquals( "value", clp.getOptionValue( shortOption, false, true ) );
    assertEquals( null, clp.getOptionValue( "wrongKey", false, true ) );
    try {
      assertEquals( null, clp.getOptionValue( "wrongKey", true, true ) );
      fail();
    } catch ( ParseException e ) {
      // expected
    }
    try {
      assertEquals( null, clp.getOptionValue( "wrongKey", false, false ) );
      fail();
    } catch ( ParseException e ) {
      // expected
    }
  }

}
