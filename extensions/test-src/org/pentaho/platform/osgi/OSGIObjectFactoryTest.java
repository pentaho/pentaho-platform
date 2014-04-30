/*
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
 * Copyright 2013 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.osgi;

import org.junit.AfterClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.AggregateObjectFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link OSGIObjectFactory}.
 */
@SuppressWarnings( "unchecked" )
public class OSGIObjectFactoryTest {

  @Test
  public void testGet() throws Exception {

    StandaloneSession session = new StandaloneSession();
    BundleContext mockContext = Mockito.mock( BundleContext.class );
    ServiceReference<String> ref = Mockito.mock( ServiceReference.class );

    when( mockContext.getServiceReferences( String.class, null ) ).thenReturn( Collections.singletonList( ref ) );
    when( mockContext.getService( ref ) ).thenReturn( "SomeString" );

    when( mockContext.getServiceReferences( java.lang.Integer.class, null ) )
      .thenThrow( new InvalidSyntaxException( "bad", "call" ) );

    OSGIObjectFactory factory = new OSGIObjectFactory( mockContext );
    String actual = factory.get( String.class, session );
    Integer missing = factory.get( Integer.class, session );


    assertEquals( "SomeString", actual );
    assertNull( missing );

    Float missing2 = factory.get( Float.class, session );
    assertNull( missing2 );

    verify( mockContext ).getServiceReferences( String.class, null );

  }

  @Test
  public void testGetWInitializer() throws Exception {

    StandaloneSession session = new StandaloneSession();
    BundleContext mockContext = Mockito.mock( BundleContext.class );

    ServiceReference<IPentahoInitializer> ref = Mockito.mock( ServiceReference.class );

    when( mockContext.getServiceReferences( IPentahoInitializer.class, null ) )
      .thenReturn( Collections.singletonList( ref ) );


    final AtomicBoolean initialized = new AtomicBoolean( false );
    IPentahoInitializer initializer = new IPentahoInitializer() {

      @Override
      public void init( IPentahoSession session ) {
        initialized.set( true );
      }
    };
    when( mockContext.getService( ref ) ).thenReturn( initializer );

    assertTrue( "initializer should be rested", initialized.get() == false );

    OSGIObjectFactory factory = new OSGIObjectFactory( mockContext );
    IPentahoInitializer actual = factory.get( IPentahoInitializer.class, session );

    assertSame( initializer, actual );
    assertTrue( "initializer not called", initialized.get() );

    verify( mockContext ).getServiceReferences( IPentahoInitializer.class, null );

    List<IPentahoInitializer> actuals = factory.getAll( IPentahoInitializer.class, session );

  }

  @Test
  public void testObjectDefined() throws Exception {
    BundleContext mockContext = Mockito.mock( BundleContext.class );
    ServiceReference<String> ref = Mockito.mock( ServiceReference.class );

    ServiceReference ref2 = Mockito.mock( ServiceReference.class );

    when( mockContext.getServiceReference( String.class ) ).thenReturn( ref );
    when( mockContext.getServiceReference( String.class.getName() ) ).thenReturn( ref2 );

    OSGIObjectFactory factory = new OSGIObjectFactory( mockContext );

    assertEquals( true, factory.objectDefined( String.class ) );
    assertEquals( false, factory.objectDefined( Integer.class ) );

    assertEquals( true, factory.objectDefined( String.class.getName() ) );
    assertEquals( false, factory.objectDefined( Integer.class.getName() ) );

    try {
      factory.objectDefined( (String) null );
      fail( "Should have thrown IllegalStateException" );
    } catch ( IllegalStateException e ) {
    }

    try {
      factory.objectDefined( (Class) null );
      fail( "Should have thrown IllegalStateException" );
    } catch ( IllegalStateException e ) {
    }

    verify( mockContext ).getServiceReference( String.class );
  }

  @Test
  public void testGetImplementingClass() throws Exception {
    BundleContext mockContext = Mockito.mock( BundleContext.class );
    OSGIObjectFactory factory = new OSGIObjectFactory( mockContext );
    try {
      factory.getImplementingClass( "java.lang.String" );
      fail( "Should have thrown an exception" );
    } catch ( UnsupportedOperationException e ) {

    }
    assertTrue( true );
  }


  @Test
  public void testGetAll() throws Exception {

    StandaloneSession session = new StandaloneSession();
    BundleContext mockContext = Mockito.mock( BundleContext.class );
    ServiceReference<String> ref = Mockito.mock( ServiceReference.class );
    ServiceReference<String> ref2 = Mockito.mock( ServiceReference.class );

    when( mockContext.getServiceReferences( String.class, null ) ).thenReturn( Arrays.asList( ref, ref2 ) );
    when( mockContext.getServiceReferences( Integer.class, null ) )
      .thenThrow( new InvalidSyntaxException( "bad", "call" ) );
    when( mockContext.getService( ref ) ).thenReturn( "SomeString" );
    when( mockContext.getService( ref2 ) ).thenReturn( "SomeString2" );

    OSGIObjectFactory factory = new OSGIObjectFactory( mockContext );
    List<String> actual = factory.getAll( String.class, session );


    assertEquals( 2, actual.size() );
    assertEquals( "SomeString", actual.get( 0 ) );
    assertEquals( "SomeString2", actual.get( 1 ) );

    verify( mockContext ).getServiceReferences( String.class, null );

    List<Integer> madInts = factory.getAll( Integer.class, session );
    assertNotNull( madInts );
    assertEquals( 0, madInts.size() );
    List<Float> emptyFloats = factory.getAll( Float.class, session );
    assertNotNull( emptyFloats );
    assertEquals( 0, emptyFloats.size() );


  }

  @Test
  public void testGetObjectReference() throws Exception {

    StandaloneSession session = new StandaloneSession();
    BundleContext mockContext = Mockito.mock( BundleContext.class );
    ServiceReference<String> ref = Mockito.mock( ServiceReference.class );

    when( mockContext.getServiceReferences( String.class, null ) ).thenReturn( Collections.singletonList( ref ) );
    when( mockContext.getServiceReferences( Integer.class, null ) )
      .thenThrow( new InvalidSyntaxException( "bad", "call" ) );

    when( mockContext.getService( ref ) ).thenReturn( "SomeString" );

    // props
    when( ref.getPropertyKeys() ).thenReturn( new String[] { "prop1", "prop2" } );
    when( ref.getProperty( "prop1" ) ).thenReturn( "value1" );
    when( ref.getProperty( "prop2" ) ).thenReturn( "value2" );

    OSGIObjectFactory factory = new OSGIObjectFactory( mockContext );
    IPentahoObjectReference<String> objectReference = factory.getObjectReference( String.class, session );

    assertEquals( "SomeString", objectReference.getObject() );
    assertEquals( 2, objectReference.getAttributes().size() );
    assertTrue( objectReference.getAttributes().containsKey( "prop1" ) );

    IPentahoObjectReference<Integer> missing = factory.getObjectReference( Integer.class, session );
    assertNull( missing );

    verify( mockContext ).getServiceReferences( String.class, null );

  }

  @Test
  public void testGetObjectReferenceWithQuery() throws Exception {

    StandaloneSession session = new StandaloneSession();
    BundleContext mockContext = Mockito.mock( BundleContext.class );
    ServiceReference<String> ref = Mockito.mock( ServiceReference.class );
    ServiceReference<String> ref2 = Mockito.mock( ServiceReference.class );

    when( mockContext.getServiceReferences( String.class, "(&(name=foo))" ) )
      .thenReturn( Collections.singletonList( ref ) );
    when( mockContext.getServiceReferences( String.class, null ) ).thenReturn( Collections.singletonList( ref ) );
    when( mockContext.getServiceReferences( String.class, "(&(emptyPriority=true))" ) )
      .thenReturn( Collections.singletonList( ref2 ) );

    when( mockContext.getService( ref ) ).thenReturn( "SomeString" );

    // props
    when( ref.getPropertyKeys() ).thenReturn( new String[] { "name", "service.ranking" } );
    when( ref.getProperty( "name" ) ).thenReturn( "foo" );
    when( ref.getProperty( "service.ranking" ) ).thenReturn( 20 );
    when( ref2.getProperty( "service.ranking" ) ).thenReturn( null );


    OSGIObjectFactory factory = new OSGIObjectFactory( mockContext );
    IPentahoObjectReference<String> objectReference =
      factory.getObjectReference( String.class, session, Collections.singletonMap( "name", "foo" ) );

    assertNotNull( objectReference );
    assertEquals( "SomeString", objectReference.getObject() );
    assertEquals( 2, objectReference.getAttributes().size() );

    objectReference = factory.getObjectReference( String.class, session, Collections.singletonMap( "name", "foobar" ) );

    assertNull( objectReference );

    verify( mockContext ).getServiceReferences( String.class, "(&(name=foo))" );
    verify( mockContext ).getServiceReferences( String.class, "(&(name=foobar))" );

    objectReference = factory.getObjectReference( String.class, session, Collections.singletonMap( "name", "foo(" ) );
    assertNull( objectReference );

    objectReference = factory.getObjectReference( String.class, session, null );
    assertNotNull( objectReference );
    objectReference = factory.getObjectReference( String.class, session, Collections.<String, String>emptyMap() );
    assertNotNull( objectReference );


    IPentahoObjectReference<String> objectReference2 =
      factory.getObjectReference( String.class, session, Collections.singletonMap( "emptyPriority", "true" ) );

    assertEquals( 1, objectReference.compareTo( new IPentahoObjectReference<String>() {
      @Override
      public Map<String, Object> getAttributes() {
        return null;
      }

      @Override
      public String getObject() {
        return null;
      }

      @Override
      public int compareTo( IPentahoObjectReference<String> o ) {
        return 0;
      }

      @Override
      public Integer getRanking() {
        return 0;
      }

      @Override public Class<?> getObjectClass() {
        return String.class;
      }
    } ) );

    assertEquals( -1, objectReference.compareTo( new IPentahoObjectReference<String>() {
      @Override
      public Map<String, Object> getAttributes() {
        return null;
      }

      @Override
      public String getObject() {
        return null;
      }

      @Override
      public int compareTo( IPentahoObjectReference<String> o ) {
        return 0;
      }

      @Override
      public Integer getRanking() {
        return 30;
      }

      @Override public Class<?> getObjectClass() {
        return String.class;
      }
    } ) );

    assertEquals( 0, objectReference.compareTo( new IPentahoObjectReference<String>() {
      @Override
      public Map<String, Object> getAttributes() {
        return null;
      }

      @Override
      public String getObject() {
        return null;
      }

      @Override
      public int compareTo( IPentahoObjectReference<String> o ) {
        return 0;
      }

      @Override
      public Integer getRanking() {
        return 20;
      }

      @Override public Class<?> getObjectClass() {
        return String.class;
      }
    } ) );

    assertEquals( (Integer) 0, objectReference2.getRanking() );

    assertEquals( 1, objectReference.compareTo( null ) );

  }

  @Test
  public void testGetObjectReferencesWithQuery() throws Exception {

    StandaloneSession session = new StandaloneSession();
    BundleContext mockContext = Mockito.mock( BundleContext.class );

    ServiceReference<String> ref = (ServiceReference<String>) Mockito.mock( ServiceReference.class );
    ServiceReference<String> ref2 = Mockito.mock( ServiceReference.class );

    when( mockContext.getServiceReferences( String.class, "(&(name=foo))" ) ).thenReturn( Arrays.asList( ref, ref2 ) );
    when( mockContext.getServiceReferences( Integer.class, null ) )
      .thenThrow( new InvalidSyntaxException( "bad", "call" ) );

    when( mockContext.getService( ref ) ).thenReturn( "SomeString" );
    when( mockContext.getService( ref2 ) ).thenReturn( "SomeString2" );

    // props
    when( ref.getPropertyKeys() ).thenReturn( new String[] { "name" } );
    when( ref.getProperty( "name" ) ).thenReturn( "foo" );

    when( ref2.getPropertyKeys() ).thenReturn( new String[] { "name" } );
    when( ref2.getProperty( "name" ) ).thenReturn( "foo" );


    OSGIObjectFactory factory = new OSGIObjectFactory( mockContext );
    List<IPentahoObjectReference<String>> objectReferences =
      factory.getObjectReferences( String.class, session, Collections.singletonMap( "name", "foo" ) );

    assertNotNull( objectReferences );
    assertEquals( 2, objectReferences.size() );
    assertEquals( "SomeString", objectReferences.get( 0 ).getObject() );
    assertEquals( "SomeString2", objectReferences.get( 1 ).getObject() );

    objectReferences = factory.getObjectReferences( String.class, session, Collections.singletonMap( "name", "bar" ) );
    assertTrue( objectReferences.isEmpty() );

    verify( mockContext ).getServiceReferences( String.class, "(&(name=foo))" );


  }


  @Test
  public void testGetName() throws Exception {
    BundleContext mockContext = Mockito.mock( BundleContext.class );
    OSGIObjectFactory factory = new OSGIObjectFactory( mockContext );
    assertEquals( "OSGIObjectFactory", factory.getName() );
  }

  @Test
  public void testInit() {
    BundleContext mockContext = Mockito.mock( BundleContext.class );
    OSGIObjectFactory factory = new OSGIObjectFactory( mockContext );
    factory.init( null, null );

  }


  @AfterClass
  public static void afterClass() {
    ( (AggregateObjectFactory) PentahoSystem.getObjectFactory() ).clear();
  }
}
