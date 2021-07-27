/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.util.messages.LocaleHelper;

import junit.framework.Assert;
import junit.framework.TestCase;

@SuppressWarnings( "nls" )
public class SystemSettingsTest extends TestCase {

  private static final String SOLUTION_PATH                   = "src/test/resources/solution";
  private static final String ALT_SOLUTION_PATH               = "src/test/resources/solution";
  private static final String PENTAHO_XML_PATH                = "/system/pentaho.xml";
  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";
  final String                SYSTEM_FOLDER                   = "/system";

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

  public boolean init() {
    PentahoSystem.setSystemSettingsService( new PathBasedSystemSettings() );
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    StandaloneApplicationContext applicationContext = null;
    if ( file.exists() ) {
      applicationContext = new StandaloneApplicationContext( SystemSettingsTest.SOLUTION_PATH, "" ); //$NON-NLS-1$
    } else {
      applicationContext = new StandaloneApplicationContext( SystemSettingsTest.ALT_SOLUTION_PATH, "" ); //$NON-NLS-1$
    }
    String inContainer = System.getProperty( "incontainer", "false" ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( inContainer.equalsIgnoreCase( "false" ) ) { //$NON-NLS-1$
      // Setup simple-jndi for datasources
      System.setProperty( "java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory" ); //$NON-NLS-1$ //$NON-NLS-2$
      System.setProperty( "org.osjava.sj.root", getSolutionPath() + "/system/simple-jndi" ); //$NON-NLS-1$ //$NON-NLS-2$
      System.setProperty( "org.osjava.sj.delimiter", "/" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    String objectFactoryCreatorCfgFile = getSolutionPath() + SYSTEM_FOLDER + "/" + DEFAULT_SPRING_CONFIG_FILE_NAME; //$NON-NLS-1$
    IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
    pentahoObjectFactory.init( objectFactoryCreatorCfgFile, null );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );
    return PentahoSystem.init( applicationContext );
  }

  public void testASetUp() {
    File fileTest = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( fileTest.exists() ) {
      System.out.println( "system test File exist returning " + SOLUTION_PATH );
      LocaleHelper.setThreadLocaleBase( Locale.getDefault() );
      Assert.assertNotNull( SystemSettingsTest.SOLUTION_PATH );
      Assert.assertNotSame( "", SystemSettingsTest.SOLUTION_PATH ); //$NON-NLS-1$

      File file = new File( SystemSettingsTest.SOLUTION_PATH );
      Assert.assertTrue( "Solution base directory does not exist: " + file.getAbsolutePath(), file.exists() ); //$NON-NLS-1$

      file = new File( file, "system" ); //$NON-NLS-1$
      Assert.assertTrue( "Solution system directory does not exist: " + file.getAbsolutePath(), file.exists() ); //$NON-NLS-1$

      file = new File( SystemSettingsTest.SOLUTION_PATH + "/system/pentaho.xml" );
      Assert.assertTrue( "Solution system settings files does not exist: " + file.getAbsolutePath(), file.exists() ); //$NON-NLS-1$

    } else {
      System.out.println( "system test File does not exist returning " + ALT_SOLUTION_PATH );
      LocaleHelper.setThreadLocaleBase( Locale.getDefault() );
      Assert.assertNotNull( SystemSettingsTest.ALT_SOLUTION_PATH );
      Assert.assertNotSame( "", SystemSettingsTest.ALT_SOLUTION_PATH ); //$NON-NLS-1$

      File file = new File( SystemSettingsTest.ALT_SOLUTION_PATH );
      Assert.assertTrue( "Solution base directory does not exist: " + file.getAbsolutePath(), file.exists() ); //$NON-NLS-1$

      file = new File( file, "system" ); //$NON-NLS-1$
      Assert.assertTrue( "Solution system directory does not exist: " + file.getAbsolutePath(), file.exists() ); //$NON-NLS-1$

      file = new File( SystemSettingsTest.ALT_SOLUTION_PATH + "/system/pentaho.xml" );
      Assert.assertTrue( "Solution system settings files does not exist: " + file.getAbsolutePath(), file.exists() ); //$NON-NLS-1$
    }
  }

  @SuppressWarnings( "deprecation" )
  public void testPublishers() {
    Assert.assertTrue( "Initialization of the platform failed", init() );
    String publishersXml = null;
    try {
      Document publishersDocument = PentahoSystem.getPublishersDocument();
      publishersXml = publishersDocument.asXML();
      List publisherNodes = publishersDocument.selectNodes( "publishers/publisher" ); //$NON-NLS-1$
      Iterator publisherIterator = publisherNodes.iterator();
      while ( publisherIterator.hasNext() ) {
        Node publisherNode = (Node) publisherIterator.next();
        Assert.assertNotNull( publisherNode.selectSingleNode( "name" ) ); //$NON-NLS-1$
        Assert.assertNotNull( publisherNode.selectSingleNode( "description" ) ); //$NON-NLS-1$
        Assert.assertNotNull( publisherNode.selectSingleNode( "class" ) ); //$NON-NLS-1$
        Assert.assertNotSame( "", publisherNode.selectSingleNode( "class" ).getText() ); //$NON-NLS-1$ //$NON-NLS-2$
        String publisherClass = publisherNode.selectSingleNode( "class" ).getText(); //$NON-NLS-1$
        Object publisher = PentahoSystem.createObject( publisherClass, null );
        Assert.assertNotNull( publisher );
        Assert.assertTrue( publisher instanceof IPentahoPublisher );
      }
    } catch ( Throwable e ) {
      Assert.assertTrue( "Error trying to get publishers description document", false ); //$NON-NLS-1$
    }
    // TODO check the XML against the expected output
    Assert.assertNotNull( "Publishers description document could not be generated", publishersXml ); //$NON-NLS-1$
  }

  public void testVersion() {
    Assert.assertTrue( "Initialization of the platform failed", init() );
    IVersionHelper versionHelper = PentahoSystem.get( IVersionHelper.class, null );
    String version = versionHelper.getVersionInformation( PentahoSystem.class );
    Assert.assertNotNull( version );
    Assert.assertNotSame( version, "" ); //$NON-NLS-1$
    Assert.assertNotSame( version, "1.0.0" ); //$NON-NLS-1$
  }

}
