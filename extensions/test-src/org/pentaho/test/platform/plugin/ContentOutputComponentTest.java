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

package org.pentaho.test.platform.plugin;

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.FileHelper;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings( "nls" )
public class ContentOutputComponentTest extends BaseTest {

  private static final String CO_TEST_NAME = "MultipleComponentTest_ContentOutput_"; //$NON-NLS-1$

  private static final String CO_TEST_EXTN = ".txt"; //$NON-NLS-1$

  private ByteArrayOutputStream lastStream;

  final String SYSTEM_FOLDER = "/system";

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void setUp() {
    super.setUp();
    StandaloneApplicationContext applicationContext = new StandaloneApplicationContext( getSolutionPath(), "" ); //$NON-NLS-1$
    PentahoSystem.init( applicationContext, getRequiredListeners() );

  }

  public void testSuccessPaths() {
    startTest();
    String testName = CO_TEST_NAME + "string_" + System.currentTimeMillis(); //$NON-NLS-1$
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    IRuntimeContext context =
        run( "/test/platform/ContentOutputTest.xaction", parameterProvider, testName, CO_TEST_EXTN ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn = context.getOutputParameter( "content" ); //$NON-NLS-1$
    assertNotNull( rtn );
    InputStream is = this.getInputStreamFromOutput( testName, CO_TEST_EXTN );
    assertNotNull( is ); // Did the test execute properly...
    String lookingFor = "This is sample output from the content-output component."; //$NON-NLS-1$
    String wasRead = FileHelper.getStringFromInputStream( is );
    assertTrue( wasRead.startsWith( lookingFor ) );

    // Test different path - Byte Array Output Stream
    lookingFor = "This is as sample bytearray output stream"; //$NON-NLS-1$
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      baos.write( lookingFor.getBytes() );
    } catch ( Exception ex ) {
      fail();
    }
    testName = CO_TEST_NAME + "ByteArrayOutputStream_" + System.currentTimeMillis(); //$NON-NLS-1$
    parameterProvider.setParameter( "CONTENTOUTPUT", baos ); //$NON-NLS-1$
    context = run( "/test/platform/ContentOutputTest_Bytearray.xaction", parameterProvider, testName, CO_TEST_EXTN ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() );
    is = this.getInputStreamFromOutput( testName, CO_TEST_EXTN );
    assertNotNull( is ); // Did the test execute properly...
    wasRead = FileHelper.getStringFromInputStream( is );
    FileHelper.getStringFromFile( new File( PentahoSystem.getApplicationContext().getSolutionPath(
        "test/datasource/books.xml" ) ) ); //$NON-NLS-1$
    try {
      FileHelper.getBytesFromFile( new File( PentahoSystem.getApplicationContext().getSolutionPath(
          "test/datasource/books.xml" ) ) ); //$NON-NLS-1$
    } catch ( IOException io ) {
      // do nothing
    }
    File f = null;
    FileHelper.getStringFromFile( f );
    assertTrue( wasRead.startsWith( lookingFor ) );

    // Test different path - InputStream
    testName = CO_TEST_NAME + "ByteArrayInputStream_" + System.currentTimeMillis(); //$NON-NLS-1$
    lookingFor = "This is as a simple bytearray input stream"; //$NON-NLS-1$
    ByteArrayInputStream bais = new ByteArrayInputStream( lookingFor.getBytes() );
    parameterProvider.setParameter( "CONTENTOUTPUT", bais ); //$NON-NLS-1$
    context = run( "/test/platform/ContentOutputTest_Bytearray.xaction", parameterProvider, testName, CO_TEST_EXTN ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() );
    is = this.getInputStreamFromOutput( testName, CO_TEST_EXTN );
    assertNotNull( is ); // Did the test execute properly...
    String newText = FileHelper.getStringFromInputStream( is );
    System.out.println( "Read Text from the input stream" + newText ); //$NON-NLS-1$
    String newTextFromIS = FileHelper.getStringFromInputStream( is );
    System.out.println( "Read Text from the input stream" + newTextFromIS ); //$NON-NLS-1$    
    assertTrue( newText.startsWith( lookingFor ) );

    finishTest();
  }

  /*
   * public void testErrorPaths() { startTest();
   * 
   * // Tests with bad output stream String neverWritten = "This data cannot be written"; //$NON-NLS-1$ String testName
   * = CO_TEST_NAME + "BAD_OUTPUTSTREAM_" + System.currentTimeMillis(); //$NON-NLS-1$ SimpleParameterProvider
   * parameterProvider = new SimpleParameterProvider(); parameterProvider.setParameter("CONTENTOUTPUT", neverWritten);
   * //$NON-NLS-1$ IRuntimeContext context = run( "test", "platform", "ContentOutputTest_Bytearray.xaction",
   * parameterProvider, testName, CO_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE,
   * context.getStatus()); //$NON-NLS-1$
   * 
   * // Another test with a bad output stream... ByteArrayOutputStream baos = new ByteArrayOutputStream(); try {
   * baos.write(neverWritten.getBytes()); } catch (Exception ex) { fail(); }
   * parameterProvider.setParameter("CONTENTOUTPUT", baos); //$NON-NLS-1$ context = run("test", "platform",
   * "ContentOutputTest_Bytearray.xaction", parameterProvider, testName, CO_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$
   * //$NON-NLS-3$ String newText = FileHelper.getStringFromInputStream(null); System.out.println("Null Input Stream" +
   * newText); //$NON-NLS-1$ assertEquals(IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus());
   * 
   * // Final test with a bad output stream... parameterProvider.setParameter("CONTENTOUTPUT", new
   * ByteArrayInputStream(neverWritten.getBytes())); //$NON-NLS-1$ context = run("test", "platform",
   * "ContentOutputTest_Bytearray.xaction", parameterProvider, testName, CO_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$
   * //$NON-NLS-3$ assertEquals(IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus());
   * 
   * // Tests with bad action sequences context = run("test", "platform", "ContentOutputTest_error1.xaction");
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL,
   * context.getStatus());
   * 
   * parameterProvider = new SimpleParameterProvider(); // Empty Parameter Provider context = run("test", "platform",
   * "ContentOutputTest_error2.xaction", parameterProvider, testName, CO_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$
   * //$NON-NLS-3$ assertEquals(IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus());
   * 
   * // Test with an invalid input parameter parameterProvider.setParameter("CONTENTOUTPUT",
   * neverWritten.getBytes()); //$NON-NLS-1$ context = run("test", "platform", "ContentOutputTest_Bytearray.xaction",
   * parameterProvider, testName, CO_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals(IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus());
   * 
   * finishTest(); }
   */

  protected OutputStream getOutputStream( String testName, String extension ) {
    if ( testName.indexOf( "BAD_OUTPUTSTREAM_" ) > 0 ) { //$NON-NLS-1$ 
      ByteArrayOutputStream exceptionStream = new ByteArrayOutputStream() {
        public static final String ERROR_MSG = "Cannot write to this stream."; //$NON-NLS-1$

        public synchronized void write( int b ) {
          throw new RuntimeException( ERROR_MSG );
        }

        public synchronized void write( byte[] b, int off, int len ) {
          throw new RuntimeException( ERROR_MSG );
        }
      };
      return exceptionStream;

    } else {
      lastStream = new ByteArrayOutputStream();
      return lastStream;
    }
  }

  protected InputStream getInputStreamFromOutput( String testName, String extension ) {
    return new ByteArrayInputStream( lastStream.toByteArray() );
  }

  public static void main( String[] args ) {
    ContentOutputComponentTest test = new ContentOutputComponentTest();
    try {
      test.setUp();
      test.testSuccessPaths();
      // test.testErrorPaths();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
