package org.pentaho.platform.engine.core.system.objfac;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.references.PrototypePentahoObjectReference;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by nbaker on 2/28/14.
 */
public class RuntimeObjectFactoryTest {
  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testRegisterObject() throws Exception {
    {
      RuntimeObjectFactory factory = new RuntimeObjectFactory();
      factory.registerObject( "hello", RuntimeObjectFactory.Types.CLASSES );
      String out = factory.get( String.class, PentahoSessionHolder.getSession() );
      assertEquals( "hello", out );
      CharSequence out2 = factory.get( CharSequence.class, PentahoSessionHolder.getSession() );
      assertNull( out2 );
    } {
      RuntimeObjectFactory factory = new RuntimeObjectFactory();
      factory
        .registerReference( new SingletonPentahoObjectReference<String>( String.class, "world" ),
          RuntimeObjectFactory.Types.INTERFACES );
      CharSequence out = factory.get( CharSequence.class, PentahoSessionHolder.getSession() );
      assertEquals( "world", out );
    } {
      RuntimeObjectFactory factory = new RuntimeObjectFactory();
      factory.registerObject( "world" );
      String out = factory.get( String.class, PentahoSessionHolder.getSession() );
      assertEquals( "world", out );
      CharSequence out2 = factory.get( CharSequence.class, PentahoSessionHolder.getSession() );
      assertEquals( "world", out2 );
    }

  }

  @Test
  public void testRegistrationWithSpecificClasses() throws Exception {
    {
      RuntimeObjectFactory factory = new RuntimeObjectFactory();
      factory.registerObject( "hello", String.class );
      String out = factory.get( String.class, PentahoSessionHolder.getSession() );
      assertEquals( "hello", out );
      CharSequence out2 = factory.get( CharSequence.class, PentahoSessionHolder.getSession() );
      assertNull( out2 );
    } {
      RuntimeObjectFactory factory = new RuntimeObjectFactory();
      factory
        .registerReference( new SingletonPentahoObjectReference<String>( String.class, "world" ),
          CharSequence.class );
      CharSequence out = factory.get( CharSequence.class, PentahoSessionHolder.getSession() );
      assertEquals( "world", out );
    } {
      RuntimeObjectFactory factory = new RuntimeObjectFactory();
      factory.registerObject( "world" );
      String out = factory.get( String.class, PentahoSessionHolder.getSession() );
      assertEquals( "world", out );
      CharSequence out2 = factory.get( CharSequence.class, PentahoSessionHolder.getSession() );
      assertEquals( "world", out2 );
    } {
      RuntimeObjectFactory factory = new RuntimeObjectFactory();
      factory
        .registerReference( new SingletonPentahoObjectReference<String>( String.class, "world" ),
          CharSequence.class, String.class );
      CharSequence out = factory.get( CharSequence.class, PentahoSessionHolder.getSession() );
      assertEquals( "world", out );
      String out2 = factory.get( String.class, PentahoSessionHolder.getSession() );
      assertSame( out, out2 );

    }
  }

  @Test
  public void testWithQuery() throws Exception {
    {
      RuntimeObjectFactory factory = new RuntimeObjectFactory();
      factory
        .registerReference(
          new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "world" ).attributes(
            Collections.<String, Object>singletonMap( "foo", "bar" ) ).build(), RuntimeObjectFactory.Types.CLASSES );
      String out =
        factory.get( String.class, PentahoSessionHolder.getSession(), Collections.singletonMap( "foo", "bar" ) );
      assertEquals( "world", out );
      out = factory.get( String.class, PentahoSessionHolder.getSession(), Collections.singletonMap( "foo", "baz" ) );
      assertNull( out );
    } {
      // check the old fallback
      RuntimeObjectFactory factory = new RuntimeObjectFactory();
      factory.registerReference(
        new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "world" ).attributes(
          Collections.<String, Object>singletonMap( "id", "abcd" ) ).build(), RuntimeObjectFactory.Types.CLASSES );
      factory
        .registerReference(
          new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "beater" ).attributes(
            Collections.<String, Object>singletonMap( "id", "abcdef" ) ).build(), RuntimeObjectFactory.Types.CLASSES );

      String out = factory.get( String.class, "abcd", PentahoSessionHolder.getSession() );
      assertEquals( "world", out );
      out = factory.get( String.class, "abcdef", PentahoSessionHolder.getSession() );
      assertEquals( "beater", out );

      // be sure that it falls-back to class if key not found
      out = factory.get( String.class, "NOT THERE", PentahoSessionHolder.getSession() );
      assertNotNull( out );

      // and it should respond with null if nothing is there
      assertNull( factory.get( Integer.class, "NOT THERE", PentahoSessionHolder.getSession() ) );
    }

  }

  @Test
  public void testNotImplementedMethods() throws Exception {

    RuntimeObjectFactory factory = new RuntimeObjectFactory();
    factory.init( "", null );
    assertNull( factory.getImplementingClass( "aKey" ) );
    assertFalse( factory.objectDefined( "aKey" ) );
  }

  @Test
  public void testGetName() throws Exception {
    RuntimeObjectFactory factory = new RuntimeObjectFactory();

    assertEquals( "Runtime Object Factory", factory.getName() );
  }

  @Test
  public void testPriority() throws Exception {

    RuntimeObjectFactory factory = new RuntimeObjectFactory();
    factory
      .registerReference(
        new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "higher" ).priority( 2 ).build(),
        RuntimeObjectFactory.Types.CLASSES );
    factory
      .registerReference(
        new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "lower" ).priority( 1 ).build(),
        RuntimeObjectFactory.Types.CLASSES );
    String out = factory.get( String.class, PentahoSessionHolder.getSession() );
    assertEquals( "higher", out );
    factory
      .registerReference(
        new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "highest" ).priority( 5 ).build(),
        RuntimeObjectFactory.Types.CLASSES );
    out = factory.get( String.class, PentahoSessionHolder.getSession() );
    assertEquals( "highest", out );
  }


  @Test
  public void testObjectDefined() throws Exception {

    RuntimeObjectFactory factory = new RuntimeObjectFactory();
    factory.registerObject( "hello", RuntimeObjectFactory.Types.CLASSES );
    assertTrue( factory.objectDefined( String.class ) );
    assertFalse( factory.objectDefined( Integer.class ) );
  }

  @Test
  public void testGetAll() throws Exception {

    RuntimeObjectFactory factory = new RuntimeObjectFactory();
    factory
      .registerReference(
        new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "higher" ).priority( 2 ).build(),
        RuntimeObjectFactory.Types.CLASSES );
    factory
      .registerReference(
        new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "lower" ).priority( 1 ).build(),
        RuntimeObjectFactory.Types.CLASSES );

    List<String> out = factory.getAll( String.class, PentahoSessionHolder.getSession() );
    assertEquals( 2, out.size() );
    assertEquals( "higher", out.get( 0 ) );
    assertEquals( "lower", out.get( 1 ) );


    factory
      .registerReference(
        new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "highest" ).priority( 5 ).build(),
        RuntimeObjectFactory.Types.CLASSES );
    out = factory.getAll( String.class, PentahoSessionHolder.getSession() );
    assertEquals( 3, out.size() );
    assertEquals( "highest", out.get( 0 ) );
    assertEquals( "higher", out.get( 1 ) );
    assertEquals( "lower", out.get( 2 ) );

  }

  @Test
  public void testGetAllWithQuery() throws Exception {

    RuntimeObjectFactory factory = new RuntimeObjectFactory();
    SingletonPentahoObjectReference.Builder<String> builder =
      new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "world" )
        .attributes( Collections.<String, Object>singletonMap( "foo", "bar" ) );

    SingletonPentahoObjectReference<String> ref = builder.build();
    SingletonPentahoObjectReference<String> ref2 = builder.build();

    builder.attributes( Collections.<String, Object>singletonMap( "foo", "baz" ) );
    SingletonPentahoObjectReference<String> ref3 = builder.build();

    factory.registerReference( ref );
    factory.registerReference( ref2 );
    factory.registerReference( ref3 );

    List<String> out = factory.getAll( String.class, PentahoSessionHolder.getSession() );
    assertEquals( 3, out.size() );

    out = factory.getAll( String.class, PentahoSessionHolder.getSession(), Collections.singletonMap( "foo", "bar" ) );
    assertEquals( 2, out.size() );

  }

  @Test
  public void testGetObjectReference() throws Exception {
    IPentahoSession session = new StandaloneSession( "joe" );
    RuntimeObjectFactory factory = new RuntimeObjectFactory();
    final SingletonPentahoObjectReference<String>
      something = new SingletonPentahoObjectReference<String>( String.class, "Something" );
    factory.registerReference( something );
    assertSame( something, factory.getObjectReference( String.class, session ) );
  }

  @Test
  public void testGetObjectReferences() throws Exception {

    IPentahoSession session = new StandaloneSession( "joe" );
    RuntimeObjectFactory factory = new RuntimeObjectFactory();
    final SingletonPentahoObjectReference<String> something1 =
      new SingletonPentahoObjectReference<String>( String.class, "Something1", Collections.<String, Object>emptyMap(),
        0 );
    final SingletonPentahoObjectReference<String> something2 =
      new SingletonPentahoObjectReference<String>( String.class, "Something2", Collections.<String, Object>emptyMap(),
        1 );
    factory.registerReference( something1 );
    factory.registerReference( something2 );
    List<String> out = factory.getAll( String.class, PentahoSessionHolder.getSession() );
    assertEquals( 2, out.size() );
    List<IPentahoObjectReference<String>> refs = factory.getObjectReferences( String.class, session );

    assertSame( something1, refs.get( 1 ) );
    assertSame( something2, refs.get( 0 ) );
  }

  @Test
  public void testBadFactory() throws Exception {
    PrototypePentahoObjectReference.Builder<String> builder =
      new PrototypePentahoObjectReference.Builder<String>( String.class );

    builder.creator( new IObjectCreator<String>() {
      @Override public String create( IPentahoSession session ) {
        throw new RuntimeException( "I am bad" );
      }
    } );

    PrototypePentahoObjectReference<String> factoryReference = builder.build();

    assertNull( factoryReference.getObject() );
  }

  @Test
  public void testFactoriedReferencePriorities() throws Exception {
    SingletonPentahoObjectReference.Builder<String> builder =
      new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "something" );

    SingletonPentahoObjectReference<String> r1 = builder.priority( 1 ).build();
    SingletonPentahoObjectReference<String> r2 = builder.priority( 2 ).build();
    SingletonPentahoObjectReference<String> r3 = builder.priority( 2 ).build();

    assertTrue( r1.compareTo( r2 ) == -1 );
    assertTrue( r2.compareTo( r1 ) == 1 );
    assertTrue( r2.compareTo( r3 ) == 0 );

    assertTrue( r1.compareTo( null ) == 1 );
    assertTrue( r1.compareTo( r1 ) == 0 );

    SingletonPentahoObjectReference.Builder<String> stringBuilder =
      new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "Something" );
    SingletonPentahoObjectReference<String> s1 = stringBuilder.priority( 1 ).build();
    SingletonPentahoObjectReference<String> s2 = stringBuilder.priority( 2 ).build();
    SingletonPentahoObjectReference<String> s3 = stringBuilder.priority( 2 ).build();


    assertTrue( s1.compareTo( s2 ) == -1 );
    assertTrue( s2.compareTo( s1 ) == 1 );
    assertTrue( s2.compareTo( s3 ) == 0 );

    assertTrue( s1.compareTo( null ) == 1 );
    assertTrue( s1.compareTo( s1 ) == 0 );

  }

  @Test
  public void testPentahoSystem() throws Exception {
    PentahoSystem.registerObject( "Testing" );
    assertEquals( "Testing", PentahoSystem.get( String.class ) );

    PentahoSystem.clearObjectFactory();
    PentahoSystem.registerObject( "Testing", IPentahoRegistrableObjectFactory.Types.INTERFACES );
    assertNull( PentahoSystem.get( String.class ) );
    assertEquals( "Testing", PentahoSystem.get( CharSequence.class ) );

    PentahoSystem.clearObjectFactory();
    PentahoSystem
      .registerReference( new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "foo" ).build() );
    assertEquals( "foo", PentahoSystem.get( String.class ) );

    PentahoSystem.clearObjectFactory();
    PentahoSystem
      .registerReference( new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "foo" ).build(),
        IPentahoRegistrableObjectFactory.Types.INTERFACES );
    assertNull( PentahoSystem.get( String.class ) );
    assertEquals( "foo", PentahoSystem.get( CharSequence.class ) );

    PentahoSystem.clearObjectFactory();
    PentahoSystem
      .registerReference( new SingletonPentahoObjectReference.Builder<String>( String.class ).object( "foo" ).build(),
        CharSequence.class );
    assertNull( PentahoSystem.get( String.class ) );
    assertEquals( "foo", PentahoSystem.get( CharSequence.class ) );


    PentahoSystem.clearObjectFactory();
    PentahoSystem.registerObject( "Testing", String.class );
    assertEquals( "Testing", PentahoSystem.get( String.class ) );


  }

  @Test
  public void testDeregistration() throws Exception {
    PentahoSystem.clearObjectFactory();
    final IPentahoObjectRegistration handle = PentahoSystem.registerObject( "Testing" );
    assertEquals( "Testing", PentahoSystem.get( String.class ) );
    handle.remove();
    assertNull( PentahoSystem.get( String.class ) );
  }
}
