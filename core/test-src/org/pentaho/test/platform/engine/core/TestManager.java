/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 3 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2005 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.test.platform.engine.core;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class TestManager extends PentahoBase {

  private static final long serialVersionUID = -4893201028464116019L;

  public static final int STATUS_RUNNING = 1;

  public static final int STATUS_SUCCESS = 2;

  public static final int STATUS_FAILED = 3;

  private static ArrayList<String> messages;

  private static ArrayList<SuiteInfo> suites;

  private static TestManager manager;

  private String sampleDataDriver;

  private String hibernateDriver;

  public Log getLogger() {
    return LogFactory.getLog( TestManager.class );
  }

  private static String[] propertyNames = { "os.name", //$NON-NLS-1$
    "os.version", //$NON-NLS-1$
    "java.version", //$NON-NLS-1$
    "java.vendor", //$NON-NLS-1$
    "user.language", //$NON-NLS-1$
    "user.country" }; //$NON-NLS-1$

  public static TestManager getInstance( TestSuite all ) throws Exception {
    if ( manager == null ) {
      String testManagerClassName =
          PentahoSystem.getSystemSetting(
              "test-suite/test-settings.xml", "test-manager", "org.pentaho.test.TestManager" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if ( testManagerClassName == null ) {
        testManagerClassName = "org.pentaho.test.TestManager"; //$NON-NLS-1$
      }
      Class componentClass = Class.forName( testManagerClassName.trim() );
      manager = (TestManager) componentClass.newInstance();
      if ( manager != null ) {
        manager.init( all );
      } else {
        throw new ClassNotFoundException();
      }
    }
    return manager;
  }

  public int getSuiteIndex( String suite ) {
    for ( int idx = 0; idx < suites.size(); idx++ ) {
      if ( ( suites.get( idx ) ).className.equals( suite ) ) {
        return idx;
      }
    }
    return -1;
  }

  public String getSuite( int idx ) {
    if ( idx < suites.size() ) {
      return ( suites.get( idx ) ).className;
    }
    return null;
  }

  protected Enumeration getSuites( TestSuite all ) {

    return all.tests();

  }

  private void init( TestSuite all ) {

    Enumeration suitesEnum = getSuites( all );

    suites = new ArrayList<SuiteInfo>();
    while ( suitesEnum.hasMoreElements() ) {
      TestSuite suite = (TestSuite) suitesEnum.nextElement();
      SuiteInfo suiteInfo = new SuiteInfo( suite );
      suites.add( suiteInfo );
      suiteInfo.init();
    }

    // TODO find out the JDBC driver for the Sample Data connection

    // TODO find out the JDBC driver for the Hibernate connection
    sampleDataDriver = Messages.getInstance().getString( "UI.USER_TEST_SUITE_UNKNOWN" ); //$NON-NLS-1$

    hibernateDriver = Messages.getInstance().getString( "UI.USER_TEST_SUITE_UNKNOWN" ); //$NON-NLS-1$

  }

  public SuiteInfo getSuite( String suiteClass ) {
    for ( int idx = 0; idx < suites.size(); idx++ ) {
      SuiteInfo suiteInfo = suites.get( idx );
      if ( suiteInfo.className.equals( suiteClass ) ) {
        return suiteInfo;
      }
    }
    return null;
  }

  public Document getStatus( IPentahoSession userSession ) {
    Document doc = DocumentHelper.createDocument();
    Element root = doc.addElement( "test-suites" ); //$NON-NLS-1$
    Element propertiesNode = root.addElement( "properties" ); //$NON-NLS-1$
    Properties properties = System.getProperties();
    for ( String propertyName : propertyNames ) {
      String value = properties.getProperty( propertyName );
      Element propertyNode = propertiesNode.addElement( "property" ); //$NON-NLS-1$
      propertyNode.addAttribute( "name", propertyName ); //$NON-NLS-1$
      propertyNode.addAttribute( "value", value ); //$NON-NLS-1$
    }

    // add some standard settings
    Element propertyNode = propertiesNode.addElement( "property" ); //$NON-NLS-1$
    propertyNode.addAttribute( "name", Messages.getInstance().getString( "UI.USER_TEST_SUITE_SOLUTION_REPOSITORY" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    propertyNode.addAttribute(
        "value", PentahoSystem.get( IUnifiedRepository.class, userSession ).getClass().toString() ); //$NON-NLS-1$

    propertyNode = propertiesNode.addElement( "property" ); //$NON-NLS-1$
    propertyNode.addAttribute( "name", "data.driver" ); //$NON-NLS-1$ //$NON-NLS-2$
    propertyNode.addAttribute( "value", sampleDataDriver ); //$NON-NLS-1$
    propertyNode = propertiesNode.addElement( "property" ); //$NON-NLS-1$
    propertyNode.addAttribute( "name", "repository.driver" ); //$NON-NLS-1$ //$NON-NLS-2$
    propertyNode.addAttribute( "value", hibernateDriver ); //$NON-NLS-1$

    for ( int idx = 0; idx < suites.size(); idx++ ) {
      SuiteInfo suiteInfo = suites.get( idx );
      suiteInfo.getStatus( root );
    }
    return doc;

  }

  public TestInfo getTest( String suiteClass, String method ) {
    SuiteInfo suiteInfo = getSuite( suiteClass );
    if ( suiteInfo == null ) {
      return null;
    }
    TestInfo test = suiteInfo.getTest( method );
    return test;
  }

  public void runSuite( String suiteClass ) {
    SuiteInfo suite = getSuite( suiteClass );
    if ( suite != null ) {
      suite.run();
    }
  }

  public void runTest( String suiteClass, String method ) {
    TestInfo test = getTest( suiteClass, method );
    test.run();
  }

  public static List<String> getMessagesList() {
    return messages;
  }

  public class SuiteInfo implements TestListener {

    private int NOT_RUNNING = 0;

    private int PASS = 1;

    private int FAIL = 2;

    private int RUNNING = 3;

    private int runCount = 0;

    private int passCount = 0;

    private int errorCount = 0;

    private int failCount = 0;

    private int status;

    private HashMap<String, TestInfo> methodMap = new HashMap<String, TestInfo>();

    private HashMap<Test, TestInfo> testMap = new HashMap<Test, TestInfo>();

    private ArrayList<TestInfo> testList = new ArrayList<TestInfo>();

    private TestSuite suite;

    private String name;

    private String className;

    TestResult result;

    private String message;

    private int testCount;

    public SuiteInfo( TestSuite suite ) {
      this.suite = suite;
      name = suite.getName();
      name = name.substring( name.lastIndexOf( '.' ) + 1 );
      className = suite.getName();
      testCount = suite.countTestCases();
      result = new TestResult();
      result.addListener( this );
    }

    public void init() {
      Enumeration testsEnum = suite.tests();
      while ( testsEnum.hasMoreElements() ) {

        Test test = (Test) testsEnum.nextElement();
        TestInfo testInfo = new TestInfo( test, this );
        addTestInfo( testInfo );
      }
    }

    public void addTestInfo( TestInfo info ) {
      methodMap.put( info.methodName, info );
      testMap.put( info.test, info );
      testList.add( info );
    }

    public TestInfo getTest( String method ) {
      return methodMap.get( method );
    }

    public void run() {
      status = RUNNING;
      suite.run( result );
    }

    public void stop() {
      result.stop();
      status = NOT_RUNNING;
      message = Messages.getInstance().getString( "UI.USER_TEST_SUITE_STOPPED" ); //$NON-NLS-1$
    }

    public void addError( Test test, Throwable error ) {
      message = error.getMessage();
      errorCount++;
      status = FAIL;
      TestInfo testInfo = testMap.get( test );
      testInfo.addError( error );
      message = Messages.getInstance().getString( "UI.USER_TEST_SUITE_FAILED" ); //$NON-NLS-1$
    }

    public void addFailure( Test test, AssertionFailedError error ) {
      message = error.getMessage();
      failCount++;
      status = FAIL;
      TestInfo testInfo = testMap.get( test );
      testInfo.addFailure( error );
      message = Messages.getInstance().getString( "UI.USER_TEST_SUITE_FAILED" ); //$NON-NLS-1$
    }

    public void endTest( Test test ) {
      if ( status != FAIL ) {
        passCount++;
      }
      status = NOT_RUNNING;
      TestInfo testInfo = testMap.get( test );
      testInfo.endTest();
      message = ""; //$NON-NLS-1$
    }

    public void startTest( Test test ) {
      runCount++;
      TestInfo testInfo = testMap.get( test );
      testInfo.startTest();
      message = Messages.getInstance().getString( "UI.USER_TEST_SUITE_RUNNING" ); //$NON-NLS-1$
    }

    public Node getStatus( Element parent ) {
      Element node = parent.addElement( "suite" ); //$NON-NLS-1$
      node.addAttribute( "class", className ); //$NON-NLS-1$
      node.addAttribute( "name", name ); //$NON-NLS-1$
      node.addAttribute( "test-count", Integer.toString( testCount ) ); //$NON-NLS-1$
      node.addAttribute( "run-count", Integer.toString( runCount ) ); //$NON-NLS-1$
      node.addAttribute( "pass-count", Integer.toString( passCount ) ); //$NON-NLS-1$
      node.addAttribute( "fail-count", Integer.toString( errorCount + failCount ) ); //$NON-NLS-1$
      Element messageNode = node.addElement( "message" ); //$NON-NLS-1$
      Element tests = node.addElement( "tests" ); //$NON-NLS-1$
      int currentPassCount = 0;
      int currentRunCount = 0;
      int currentFailCount = 0;
      for ( int idx = 0; idx < testList.size(); idx++ ) {
        TestInfo test = testList.get( idx );
        test.getStatus( tests );
        if ( test.lastResult == PASS ) {
          currentPassCount++;
        }
        if ( test.lastResult == FAIL ) {
          currentFailCount++;
        }
        if ( test.lastResult != test.NOT_RUN ) {
          currentRunCount++;
        }
      }
      if ( currentRunCount == 0 ) {
        message = Messages.getInstance().getString( "UI.USER_TEST_SUITE_NOT_RUN" ); //$NON-NLS-1$
      } else if ( currentRunCount == currentPassCount && currentRunCount < testCount ) {
        message = Messages.getInstance().getString( "UI.USER_TEST_SUITE_SOME_PASSED" ); //$NON-NLS-1$
      } else if ( currentRunCount == currentPassCount && currentRunCount == testCount ) {
        message = Messages.getInstance().getString( "UI.USER_TEST_SUITE_ALL_PASSED" ); //$NON-NLS-1$
      } else if ( currentFailCount > 0 ) {
        message = Messages.getInstance().getString( "UI.USER_TEST_SUITE_SOME_FAILURES" ); //$NON-NLS-1$
      }
      messageNode.setText( ( message == null ) ? "" : message ); //$NON-NLS-1$
      return node;
    }

  }

  public class TestInfo implements TestListener {
    private int NOT_RUNNING = 0;

    private int PASS = 1;

    private int FAIL = 2;

    private int NOT_RUN = 3;

    private int RUNNING = 3;

    private int runCount = 0;

    private int passCount = 0;

    private int errorCount = 0;

    private int failCount = 0;

    private int status = NOT_RUNNING;

    private int lastResult = NOT_RUN;

    private Date timestamp;

    private String message;

    private Test test;

    private String name;

    private String methodName;

    TestResult result;

    double duration = 0;

    DecimalFormat fmt = new DecimalFormat( "#.000" ); //$NON-NLS-1$

    SuiteInfo suiteInfo;

    public TestInfo( Test test, SuiteInfo suiteInfo ) {
      this.test = test;
      this.suiteInfo = suiteInfo;
      String testName = test.toString();
      testName = testName.substring( 0, testName.indexOf( '(' ) );
      methodName = testName;
      name = testName.substring( 4 );
      message = Messages.getInstance().getString( "UI.USER_TEST_SUITE_NOT_RUN" ); //$NON-NLS-1$
      result = new TestResult();
      result.addListener( this );
    }

    public void run() {
      status = RUNNING;
      timestamp = new Date();
      test.run( result );
    }

    public void stop() {
      result.stop();
      status = NOT_RUNNING;
      duration = -1;
    }

    public void addError( Test theTest, Throwable error ) {
      suiteInfo.addError( theTest, error );
    }

    public void addError( Throwable error ) {
      errorCount++;
      status = FAIL;
      message = error.getLocalizedMessage();
      duration = -1;
    }

    public void addFailure( Test theTest, AssertionFailedError error ) {
      suiteInfo.addFailure( theTest, error );
    }

    public void addFailure( AssertionFailedError error ) {
      failCount++;
      status = FAIL;
      message = error.getLocalizedMessage();
      duration = -1;
    }

    public void endTest( Test theTest ) {
      suiteInfo.endTest( theTest );
    }

    public void endTest() {
      if ( status != FAIL ) {
        passCount++;
        Date now = new Date();
        duration = ( now.getTime() - timestamp.getTime() );
        lastResult = PASS;
        message = Messages.getInstance().getString( "UI.USER_TEST_SUITE_PASSED" ); //$NON-NLS-1$
      } else {
        lastResult = FAIL;
      }
      status = NOT_RUNNING;
    }

    public void startTest( Test theTest ) {
      suiteInfo.startTest( theTest );
    }

    public void startTest() {
      runCount++;
      status = RUNNING;
      message = Messages.getInstance().getString( "UI.USER_TEST_SUITE_RUNNING" ); //$NON-NLS-1$
      timestamp = new Date();
    }

    public Node getStatus( Element parent ) {
      Element node = parent.addElement( "test" ); //$NON-NLS-1$
      node.addAttribute( "method", methodName ); //$NON-NLS-1$
      node.addAttribute( "name", name ); //$NON-NLS-1$
      node.addAttribute(
          "last-run", ( timestamp == null ) ? Messages.getInstance().getString( "UI.USER_TEST_SUITE_UNKNOWN" ) : DateFormat.getDateTimeInstance().format( timestamp ) ); //$NON-NLS-1$ //$NON-NLS-2$
      node.addAttribute( "run-count", Integer.toString( runCount ) ); //$NON-NLS-1$
      node.addAttribute( "pass-count", Integer.toString( passCount ) ); //$NON-NLS-1$
      node.addAttribute( "fail-count", Integer.toString( errorCount + failCount ) ); //$NON-NLS-1$
      if ( lastResult == FAIL ) {
        node.addAttribute( "duration", "-1" ); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        node.addAttribute( "duration", fmt.format( duration / 1000 ) ); //$NON-NLS-1$
      }
      Element messageNode = node.addElement( "message" ); //$NON-NLS-1$
      messageNode.setText( ( message == null ) ? "" : message ); //$NON-NLS-1$
      return node;
    }

  }

}
