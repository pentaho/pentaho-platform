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

package org.pentaho.platform.engine.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import junit.framework.TestCase;

@SuppressWarnings( { "all" } )
public class StandaloneSpringPentahoObjectFactoryTest extends TestCase {

  @Before
  public void setup() {
    PentahoSystem.clearObjectFactory();
  }

  public void testInitFromXml() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSession session2 = new StandaloneSession();

    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );

    GoodObject goodObject = factory.get( GoodObject.class, session );
    assertNotNull( goodObject );

    try {
      factory.get( BadObject.class, null );
      assertFalse( true );
    } catch ( ObjectFactoryException e ) {
      // ignored
    }

    GoodObject goodObject1 = factory.get( GoodObject.class, session );
    GoodObject goodObject2 = factory.get( GoodObject.class, session );

    assertEquals( goodObject1, goodObject2 );
    assertEquals( session, goodObject1.initSession );

    GoodObject goodObject3 = factory.get( GoodObject.class, session2 );
    assertNotSame( goodObject1, goodObject3 );
    assertEquals( session2, goodObject3.initSession );

  }

  public void testNoInit() throws Exception {

    StandaloneSession session = new StandaloneSession();

    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();

    try {
      GoodObject obj = factory.get( GoodObject.class, session );
      assertNotNull( obj );
      assertFalse( true );
    } catch ( ObjectFactoryException e ) {
      assertTrue( true );
    }

  }

  public void testBadInit() throws Exception {

    StandaloneSession session = new StandaloneSession();

    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();

    try {
      factory.init( null, "bogus" );
      assertFalse( true );
    } catch ( IllegalArgumentException e ) {
      assertTrue( true );
    }

    try {
      GoodObject obj = factory.get( GoodObject.class, session );
      assertNotNull( obj );
      assertFalse( true );
    } catch ( ObjectFactoryException e ) {
      assertTrue( true );
    }

  }

  public void testGetAll() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );
    PentahoSystem.registerObjectFactory( factory );

    List<MimeTypeListener> mimes = PentahoSystem.getAll( MimeTypeListener.class, session );

    assertNotNull( mimes );

    assertEquals( 5, mimes.size() );

    assertNotSame( mimes.get( 0 ), mimes.get( 1 ) );

  }

  public void testReferences() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );
    PentahoSystem.registerObjectFactory( factory );

    IPentahoObjectReference reference = PentahoSystem.getObjectReference( MimeTypeListener.class, session );

    assertEquals( "30", reference.getAttributes().get( "priority" ) );

    assertEquals( ( (MimeTypeListener) reference.getObject() ).name, "Higher Priority MimeTypeListener" );
  }

  public void testGetByProperty() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );
    PentahoSystem.registerObjectFactory( factory );

    MimeTypeListener obj =
        PentahoSystem.get( MimeTypeListener.class, session, Collections.singletonMap( "someKey", "1" ) );
    assertEquals( "Test Attr1", obj.name );

    obj = PentahoSystem.get( MimeTypeListener.class, session, Collections.singletonMap( "someKey", "2" ) );
    assertEquals( "Test Attr2", obj.name );

    // Multiple Attributes
    HashMap<String, String> map = new HashMap<String, String>();
    map.put( "someKey", "3" );
    map.put( "foo", "bar" );
    obj = PentahoSystem.get( MimeTypeListener.class, session, map );
    assertEquals( "Test Attr3", obj.name );

    // Not found, will default to
    map = new HashMap<String, String>();
    map.put( "someKey", "3" );
    map.put( "foo", "bang" );
    obj = PentahoSystem.get( MimeTypeListener.class, session, map );
    assertEquals( null, obj );
  }

  public void testReferenceList() throws Exception {
    PentahoSystem.shutdown();

    StandaloneSession session = new StandaloneSession();
    PentahoSessionHolder.setSession( session );
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );
    PentahoSystem.registerObjectFactory( factory );

    MimeListenerCollection collection = PentahoSystem.get( MimeListenerCollection.class, session );
    assertNotNull( collection );

    assertEquals( 5, collection.getListeners().size() );
    // Highest priorty first?
    assertEquals( "Higher Priority MimeTypeListener", collection.getListeners().get( 0 ).name );

    // Queried list has only one match
    assertEquals( 1, collection.getQueriedList().size() );

    // queried list returned correct impl #3
    assertEquals( "Test Attr3", collection.getQueriedList().get( 0 ).name );

    // Check the bean that was injected is the highest priority one available.
    assertEquals( "Higher Priority MimeTypeListener", collection.getHighestListener().name );

    // Check that the bean injected is based on the query for #2
    assertEquals( "Test Attr2", collection.getQueriedBean().name );

  }

  /**
   * Test <pen:bean> fallback behavior when no object is published matching the request. It should fall-back to
   * PentahoSystem.get() behavior where beans matching the simple-name of the queried class are returned.
   * 
   * @throws Exception
   */
  public void testFallbackPenBean() throws Exception {

    PentahoSystem.shutdown();

    ISystemConfig config = mock( ISystemConfig.class );
    when( config.getProperty( "system.dampening-timeout" ) ).thenReturn( "3000" );

    PentahoSystem.registerObject( config );

    StandaloneSession session = new StandaloneSession();
    PentahoSessionHolder.setSession( session );
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );
    PentahoSystem.registerObjectFactory( factory );

    SimpleObjectHolder integerHolder = PentahoSystem.get( SimpleObjectHolder.class, "SimpleIntegerHolder", session ); // fallback
                                                                                                                      // to
                                                                                                                      // simpleName
    assertNotNull( integerHolder );
    assertEquals( 123, (int) integerHolder.getVal() );

    SimpleObjectHolder stringHolder = PentahoSystem.get( SimpleObjectHolder.class, "SimpleStringHolder", session ); // fallback
                                                                                                                    // to
                                                                                                                    // simpleName
    assertNotNull( stringHolder );
    assertEquals( "testing_fallback_by_interface", stringHolder.getVal().toString() );

    SimpleObjectHolder integerHolder2 = PentahoSystem.get( SimpleObjectHolder.class, "SimpleIntegerHolder2", session ); // checks
                                                                                                                        // ID
    assertNotNull( integerHolder2 );
    assertEquals( 456, (int) integerHolder2.getVal() );

  }

  public void testPriority() throws Exception {

    StandaloneSession session = new StandaloneSession();

    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );
    PentahoSystem.registerObjectFactory( factory );

    MimeTypeListener obj = PentahoSystem.get( MimeTypeListener.class, session );

    assertEquals( "Higher Priority MimeTypeListener", obj.name );
  }

  public void testSessionProperties() throws Exception {

    StandaloneSession session = new StandaloneSession();
    PentahoSessionHolder.setSession( session );
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );
    PentahoSystem.registerObjectFactory( factory );

    IContentInfo obj = PentahoSystem.get( IContentInfo.class, session );
    assertEquals( "Test Session", obj.getTitle() );

    IContentInfo obj_again = PentahoSystem.get( IContentInfo.class, session );
    assertSame( obj_again, obj );

    session = new StandaloneSession();
    PentahoSessionHolder.setSession( session );
    IContentInfo obj_newer = PentahoSystem.get( IContentInfo.class, session );
    assertNotSame( obj, obj_newer );
  }

  public void testInitFromObject() throws Exception {

    StandaloneSession session = new StandaloneSession();

    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();

    File f = new File( "src/test/resources/solution/system/pentahoObjects.spring.xml" );
    FileSystemResource fsr = new FileSystemResource( f );
    GenericApplicationContext appCtx = new GenericApplicationContext();
    appCtx.refresh();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader( appCtx );
    xmlReader.loadBeanDefinitions( fsr );

    factory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", appCtx );

    GoodObject obj = factory.get( GoodObject.class, session );
    assertNotNull( obj );
  }

}
