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

package org.pentaho.platform.engine.core;

import junit.framework.TestCase;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;

public class StandaloneObjectFactoryTest extends TestCase {

  public void testNoCreator() {

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    try {
      factory.get( Object1.class, session1 );
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }

  }

  public void testBadScope() throws Exception {

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), Object1.class.getName(), null );

    Object obj = factory.get( Object1.class, session1 );
    assertNull( "object should be null", obj ); //$NON-NLS-1$

  }

  public void testImplementingClass() throws Exception {

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), Object1.class.getName(), Scope.GLOBAL );
    factory.defineObject( "bogus2", "bogus", Scope.GLOBAL ); //$NON-NLS-1$ //$NON-NLS-2$ 

    Object1 obj = factory.get( Object1.class, session1 );
    assertNotNull( obj );

    assertEquals( Object1.class.getName(), factory.getImplementingClass( Object1.class.getSimpleName() ).getName() );
  }

  public void testUndefinedImplementingClass() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    assertNull( factory.getImplementingClass( "bogus" ) );
  }

  public void testImplementingClassNotFound() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( "bogus", "NotARealClass", Scope.GLOBAL ); //$NON-NLS-1$ //$NON-NLS-2$ 

    try {
      factory.getImplementingClass( "bogus" );
      fail( "exception was expected" );
    } catch ( RuntimeException e ) {
      //ignored
    }
  }

  public void testInit() throws Exception {

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), Object1.class.getName(), Scope.GLOBAL );

    assertTrue( "Object is not defined", factory.objectDefined( Object1.class.getSimpleName() ) ); //$NON-NLS-1$
    Object1 obj1 = factory.get( Object1.class, session1 );
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$

    factory.init( null, null );
    assertFalse( "Object is defined", factory.objectDefined( Object1.class.getSimpleName() ) ); //$NON-NLS-1$

    assertNull( factory.getImplementingClass( Object1.class.getSimpleName() ) );

    try {
      factory.get( Object1.class, session1 );
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }

  }

  public void testGlobalObject1() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), Object1.class.getName(), Scope.GLOBAL );

    factory.defineObject( BadObjectRuntime.class.getSimpleName(), BadObjectRuntime.class.getName(), Scope.GLOBAL );
    factory.defineObject( BadObject.class.getSimpleName(), BadObject.class.getName(), Scope.GLOBAL );

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    IPentahoSession session2 = new StandaloneSession( "test user 2" ); //$NON-NLS-1$

    Object1 obj1 = factory.get( Object1.class, session1 );
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$

    Object1 obj2 = factory.get( Object1.class, session2 );
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$

    assertTrue( "Objects are not same", obj1 == obj2 ); //$NON-NLS-1$

    try {
      factory.get( BadObjectRuntime.class, session1 );
      assertFalse( true );
    } catch ( RuntimeException e ) {
      assertTrue( true );
    }

    try {
      factory.get( BadObject.class, session1 );
      assertFalse( true );
    } catch ( ObjectFactoryException e ) {
      assertTrue( true );
    }

  }

  public void testGlobalObject2() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object2.class.getSimpleName(), Object2.class.getName(), Scope.GLOBAL );

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    IPentahoSession session2 = new StandaloneSession( "test user 2" ); //$NON-NLS-1$

    Object2 obj1 = factory.get( Object2.class, session1 );
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$

    Object2 obj2 = factory.get( Object2.class, session2 );
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$

    assertTrue( "Objects are not same", obj1 == obj2 ); //$NON-NLS-1$

  }

  public void testGlobalObject3() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object2.class.getSimpleName(), Object2.class.getName(), Scope.GLOBAL, getClass()
        .getClassLoader() );

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    IPentahoSession session2 = new StandaloneSession( "test user 2" ); //$NON-NLS-1$

    Object2 obj1 = factory.get( Object2.class, session1 );
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$

    Object2 obj2 = factory.get( Object2.class, session2 );
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$

    assertTrue( "Objects are not same", obj1 == obj2 ); //$NON-NLS-1$

  }

  public void testGlobalObjectFail() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), "bogus", Scope.GLOBAL ); //$NON-NLS-1$  

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    try {
      factory.get( Object1.class, session1 );
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }

  }

  public void testGlobalObjectFail2() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object2.class.getSimpleName(), Object2.class.getName(), Scope.GLOBAL, null );
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    try {
      factory.get( Object2.class, session1 );
      assertTrue( "Exception expected", false ); //$NON-NLS-1$
    } catch ( NullPointerException e ) {
      assertFalse( "Exception expected", false ); //$NON-NLS-1$
    }

  }

  public void testSessionObject1() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), Object1.class.getName(), Scope.SESSION );

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    IPentahoSession session2 = new StandaloneSession( "test user 2" ); //$NON-NLS-1$

    Object1 obj1 = factory.get( Object1.class, session1 );
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$

    Object1 obj2 = factory.get( Object1.class, session2 );
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$

    assertTrue( "Objects are same", obj1 != obj2 ); //$NON-NLS-1$

    Object1 obj3 = factory.get( Object1.class, session1 );
    assertTrue( "Objects are not same", obj1 == obj3 ); //$NON-NLS-1$

  }

  public void testSessionObject2() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object2.class.getSimpleName(), Object2.class.getName(), Scope.SESSION );

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    IPentahoSession session2 = new StandaloneSession( "test user 2" ); //$NON-NLS-1$

    Object2 obj1 = factory.get( Object2.class, session1 );
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$

    Object2 obj2 = factory.get( Object2.class, session2 );
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$

    assertTrue( "Objects are same", obj1 != obj2 ); //$NON-NLS-1$

  }

  public void testSessionObjectFail1() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), "bogus", Scope.SESSION ); //$NON-NLS-1$  

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    try {
      factory.get( Object1.class, session1 );
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }

  }

  public void testSessionObjectFail2() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), Object1.class.getName(), Scope.SESSION );

    try {
      factory.get( Object1.class, null );
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( IllegalArgumentException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }

  }

  public void testLocalObject1() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), Object1.class.getName(), Scope.LOCAL );

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    Object1 obj1 = factory.get( Object1.class, session1 );
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$

    Object1 obj2 = factory.get( Object1.class, session1 );
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$

    assertTrue( "Objects are same", obj1 != obj2 ); //$NON-NLS-1$

    Object1 obj3 = factory.get( Object1.class, session1 );
    assertTrue( "Objects are same", obj1 != obj3 ); //$NON-NLS-1$

  }

  public void testLocalObject2() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object2.class.getSimpleName(), Object2.class.getName(), Scope.LOCAL );

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    Object2 obj1 = factory.get( Object2.class, session1 );
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$

    Object2 obj2 = factory.get( Object2.class, session1 );
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$

    assertTrue( "Objects are same", obj1 != obj2 ); //$NON-NLS-1$

    Object2 obj3 = factory.get( Object2.class, session1 );
    assertTrue( "Objects are same", obj1 != obj3 ); //$NON-NLS-1$

  }

  public void testLocalObjectFail1() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), "bogus", Scope.LOCAL ); //$NON-NLS-1$  

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    try {
      factory.get( Object1.class, session1 );
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }

  }

  public void testThreadObject1() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), Object1.class.getName(), Scope.THREAD );

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    Object1 obj1 = factory.get( Object1.class, session1 );
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$

    Object1 obj2 = factory.get( Object1.class, session1 );
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$

    assertTrue( "Objects are not same", obj1 == obj2 ); //$NON-NLS-1$

    Object1 obj3 = factory.get( Object1.class, session1 );
    assertTrue( "Objects are not same", obj1 == obj3 ); //$NON-NLS-1$

  }

  public void testThreadObject2() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object2.class.getSimpleName(), Object2.class.getName(), Scope.THREAD );

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    Object2 obj1 = factory.get( Object2.class, session1 );
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$

    Object2 obj2 = factory.get( Object2.class, session1 );
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$

    assertTrue( "Objects are not same", obj1 == obj2 ); //$NON-NLS-1$

    Object2 obj3 = factory.get( Object2.class, session1 );
    assertTrue( "Objects are not same", obj1 == obj3 ); //$NON-NLS-1$

  }

  public void testThreadObjectFail1() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.defineObject( Object1.class.getSimpleName(), "bogus", Scope.THREAD ); //$NON-NLS-1$

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    try {
      factory.get( Object1.class, session1 );
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }

  }

}
