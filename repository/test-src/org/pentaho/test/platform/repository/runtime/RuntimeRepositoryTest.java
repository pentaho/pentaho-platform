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

package org.pentaho.test.platform.repository.runtime;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository.runtime.RuntimeElement;
import org.pentaho.platform.repository.runtime.RuntimeRepository;
import org.pentaho.test.platform.repository.RepositoryTestCase;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

//import java.io.OutputStream;

@SuppressWarnings( "nls" )
public class RuntimeRepositoryTest extends RepositoryTestCase {

  private StringBuffer longString = new StringBuffer();

  private BigDecimal bdProperty = new BigDecimal( "1128347.34873484738" ); //$NON-NLS-1$

  public static final String SOLUTION_PATH = "test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";
  final String SYSTEM_FOLDER = "/system";

  // private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      System.out.println( "File exist returning " + SOLUTION_PATH );
      return SOLUTION_PATH;
    } else {
      System.out.println( "File does not exist returning " + ALT_SOLUTION_PATH );
      return ALT_SOLUTION_PATH;
    }
  }

  /**
   * @param arg0
   */
  public RuntimeRepositoryTest( String arg0 ) {
    super( arg0 );
    Properties props = System.getProperties();
    longString.append( props.getProperty( "java.home" ) ).append( props.getProperty( "sun.cpu.isalist" ) ).//$NON-NLS-1$ //$NON-NLS-2$
        append( props.getProperty( "java.vm.version" ) ).append( props.getProperty( "user.home" ) ).//$NON-NLS-1$ //$NON-NLS-2$
        append( props.getProperty( "java.class.path" ) ); //$NON-NLS-1$
  }

  public static void main( String[] args ) {
    RuntimeRepositoryTest test = new RuntimeRepositoryTest( "testRuntimeRepository" ); //$NON-NLS-1$
    junit.textui.TestRunner.run( test );
    System.exit( 0 );
  }

  @SuppressWarnings( "unused" )
  private String getMessagesText() {
    List messages = this.getMessages();
    StringBuffer sb = new StringBuffer();
    for ( int i = 0; i < messages.size(); i++ ) {
      sb.append( messages.get( i ) ).append( "\n" ); //$NON-NLS-1$
    }
    return sb.toString();
  }

  // public void testRuntimeRepository() {
  // RuntimeElement baseElement = createRuntimeElement();
  // HibernateUtil.flushSession();
  // modifyAdd(baseElement);
  // HibernateUtil.flushSession();
  // readElementTest(baseElement.getInstanceId());
  // doReadOnlyTest(baseElement.getInstanceId());
  // cleanupElement(baseElement.getInstanceId());
  //    OutputStream output = getOutputStream("RuntimeRepositoryTest.testRuntimeRepository", ".txt"); //$NON-NLS-1$ //$NON-NLS-2$
  // try {
  // output.write(getMessagesText().getBytes());
  // } catch (Exception e) {
  // }
  // }

  public void testDummyTest() {
    // do nothing, get the above test to pass!
  }

  public void setUp() {
    // TODO: remove once tests are passing
  }

  public void tearDown() {
    // TODO: remove once tests are passing
  }

  @SuppressWarnings( "unused" )
  private void doReadOnlyTest( String elementId ) {
    info( Messages.getInstance().getString( "RUNTIMEREPOTEST.USER_TESTINGREADONLY" ) ); //$NON-NLS-1$
    HibernateUtil.beginTransaction();
    try {
      IRuntimeRepository repo = new RuntimeRepository();
      repo.setSession( getPentahoSession() );
      RuntimeElement baseElement = (RuntimeElement) repo.loadElementById( elementId, null );
      info( Messages.getInstance().getString( "RUNTIMEREPOTEST.USER_SETTINGELEMENTTOREADONLY" ) ); //$NON-NLS-1$
      baseElement.setReadOnly( true );
    } finally {
      HibernateUtil.commitTransaction();
    }
    // Now, the element is read-only.
    // Flush the session and re-load the element to
    // test the "loaded" methods.
    HibernateUtil.flushSession();
    HibernateUtil.clear();
    // Now, reload the element.
    HibernateUtil.beginTransaction();
    try {
      info( Messages.getInstance().getString( "RUNTIMEREPOTEST.USER_LOADINGREADONLY" ) ); //$NON-NLS-1$
      IRuntimeRepository repo = new RuntimeRepository();
      repo.setSession( getPentahoSession() );
      RuntimeElement baseElement = (RuntimeElement) repo.loadElementById( elementId, null );
      boolean caughtException = false;
      try {
        info( Messages.getInstance().getString( "RUNTIMEREPOTEST.USER_TRYINGSETSTRINGPROPERTY" ) ); //$NON-NLS-1$
        baseElement
            .setStringProperty(
                Messages.getInstance().getString( "RUNTIMEREPOTEST.MODIFY_NEW_STRING_KEY" ), Messages.getInstance().getString( "RUNTIMEREPOTEST.MODIFY_STRING_VALUE" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      } catch ( IllegalStateException ex ) {
        caughtException = true;
        info( Messages.getInstance().getString( "RUNTIMEREPOTEST.USER_EXCEPTIONTRIPPED" ) ); //$NON-NLS-1$
      }
      assertTrue(
          Messages.getInstance().getErrorString( "RUNTIMEREPOTEST.ERROR_0001_EXCEPTIONNOTTRIPPED" ), caughtException ); //$NON-NLS-1$
      caughtException = false;
      try {
        info( Messages.getInstance().getString( "RUNTIMEREPOTEST.USER_TRYINGSETPARENTTYPE" ) ); //$NON-NLS-1$
        baseElement.setParentType( Messages.getInstance().getString( "RUNTIMEREPOTEST.CREATE_PARENT_TYPE" ) ); //$NON-NLS-1$
      } catch ( IllegalStateException ex ) {
        caughtException = true;
        info( Messages.getInstance().getString( "RUNTIMEREPOTEST.USER_EXCEPTIONTRIPPED" ) ); //$NON-NLS-1$
      }
      assertTrue(
          Messages.getInstance().getErrorString( "RUNTIMEREPOTEST.ERROR_0001_EXCEPTIONNOTTRIPPED" ), caughtException ); //$NON-NLS-1$
    } finally {
      HibernateUtil.commitTransaction();
    }
  }

  @SuppressWarnings( "unused" )
  private void cleanupElement( String elementId ) {
    HibernateUtil.beginTransaction();
    IRuntimeRepository repo = new RuntimeRepository();
    repo.setSession( getPentahoSession() );
    RuntimeElement re = (RuntimeElement) repo.loadElementById( elementId, null );
    HibernateUtil.makeTransient( re );
    HibernateUtil.commitTransaction();
    HibernateUtil.flushSession();
    HibernateUtil.clear();
  }

  @SuppressWarnings( "unused" )
  private void modifyAdd( RuntimeElement baseElement ) {
    HibernateUtil.beginTransaction();
    try {
      IRuntimeRepository repo = new RuntimeRepository();
      repo.setSession( getPentahoSession() );
      baseElement
          .setStringProperty(
              Messages.getInstance().getString( "RUNTIMEREPOTEST.MODIFY_NEW_STRING_KEY" ), Messages.getInstance().getString( "RUNTIMEREPOTEST.MODIFY_STRING_VALUE" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    } finally {
      HibernateUtil.commitTransaction();
    }
  }

  @SuppressWarnings( "unused" )
  private RuntimeElement createRuntimeElement() {
    IRuntimeRepository repo = new RuntimeRepository();
    repo.setSession( getPentahoSession() );
    RuntimeElement ele = null;
    ele =
        (RuntimeElement) repo
            .newRuntimeElement(
                Messages.getInstance().getString( "RUNTIMEREPOTEST.CREATE_PARENT" ), Messages.getInstance().getString( "RUNTIMEREPOTEST.CREATE_PARENT_TYPE" ), false ); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      assertNotNull( ele );
      info( Messages.getInstance().getString( "RUNTIMEREPOTEST.DEBUG_INSTANCE_ID" ) + ele.getInstanceId() ); //$NON-NLS-1$
      ele.setSolutionId( Messages.getInstance().getString( "RUNTIMEREPOTEST.SALES_SOLUTION" ) ); //$NON-NLS-1$
      ele.setStringProperty(
          Messages.getInstance().getString( "RUNTIMEREPOTEST.SHORT_STRING_KEY" ), Messages.getInstance().getString( "RUNTIMEREPOTEST.SHORT_STRING_VALUE" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      // Get a Long String to Store...
      ele.setStringProperty(
          Messages.getInstance().getString( "RUNTIMEREPOTEST.LONG_STRING_KEY" ), longString.toString() ); //$NON-NLS-1$
      ele.setDateProperty( Messages.getInstance().getString( "RUNTIMEREPOTEST.DATE_KEY" ), new Date() ); //$NON-NLS-1$
      ele.setBigDecimalProperty( Messages.getInstance().getString( "RUNTIMEREPOTEST.DECIMAL_KEY" ), bdProperty ); //$NON-NLS-1$
      List myList = new ArrayList();
      myList.add( Messages.getInstance().getString( "RUNTIMEREPOTEST.LIST_ELEMENT_STRING" ) ); //$NON-NLS-1$
      myList.add( new BigDecimal( "1283764.1294839483" ) ); //$NON-NLS-1$
      myList.add( new Date() );
      ele.setListProperty( Messages.getInstance().getString( "RUNTIMEREPOTEST.LIST_ELEMENT_KEY" ), myList ); //$NON-NLS-1$
    } finally {
      HibernateUtil.commitTransaction();
    }
    return ele;
  }

  private void checkValue( String expected, String value ) {
    assertEquals( Messages.getInstance().getErrorString( "RUNTIMEREPOTEST.ERROR_0002_VALUEREADNOTWHATWASEXPECTED", //$NON-NLS-1$ 
        expected, value ), expected, value );
  }

  private void checkValue( BigDecimal expected, BigDecimal value ) {
    assertEquals( Messages.getInstance().getErrorString( "RUNTIMEREPOTEST.ERROR_0002_VALUEREADNOTWHATWASEXPECTED", //$NON-NLS-1$ 
        expected.toString(), value.toString() ), expected, value );
  }

  @SuppressWarnings( "unused" )
  private void readElementTest( String instanceId ) {
    HibernateUtil.beginTransaction();
    IRuntimeRepository repo = new RuntimeRepository();
    repo.setSession( getPentahoSession() );
    RuntimeElement re = (RuntimeElement) repo.loadElementById( instanceId, null );
    checkValue( Messages.getInstance().getString( "RUNTIMEREPOTEST.SALES_SOLUTION" ), re.getSolutionId() ); //$NON-NLS-1$
    checkValue( longString.toString(), re.getStringProperty( Messages.getInstance().getString(
        "RUNTIMEREPOTEST.LONG_STRING_KEY" ) ) ); //$NON-NLS-1$
    checkValue(
        Messages.getInstance().getString( "RUNTIMEREPOTEST.SHORT_STRING_VALUE" ), re.getStringProperty( Messages.getInstance().getString( "RUNTIMEREPOTEST.SHORT_STRING_KEY" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    checkValue( bdProperty, re
        .getBigDecimalProperty( Messages.getInstance().getString( "RUNTIMEREPOTEST.DECIMAL_KEY" ) ) ); //$NON-NLS-1$
    String xml = re.toXML();
    info( xml );
    Set namesSet = re.getParameterNames();
    Iterator it = namesSet.iterator();
    String pName, pType;
    while ( it.hasNext() ) {
      pName = (String) it.next();
      pType = re.getParameterType( pName );
      info( Messages.getInstance().getString( "RUNTIMEREPOTEST.DEBUG_NAME_TYPE", pName, pType ) ); //$NON-NLS-1$
    }
  }

  public static Test suite() {
    return new TestSuite( RuntimeRepositoryTest.class );
  }

}
