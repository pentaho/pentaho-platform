/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.engine.core;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.AggregateObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.spring.PublishedBeanRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * User: nbaker Date: 3/3/13
 */
public class AggregateObjectFactoryTest {

  @Before
  public void setup() {
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testByKey() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );

    AggregateObjectFactory aggFactory = new AggregateObjectFactory();
    aggFactory.registerObjectFactory( factory );

    GoodObject info = aggFactory.get( GoodObject.class, "GoodObject", session );
    assertNotNull( info );
  }

  @Test
  public void testCombined() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();

    ConfigurableApplicationContext context =
        new FileSystemXmlApplicationContext( "src/test/resources/solution/system/pentahoObjects.spring.xml" );

    factory.init( null, context );

    StandaloneSpringPentahoObjectFactory factory2 = new StandaloneSpringPentahoObjectFactory();
    factory2.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );

    StandaloneObjectFactory factory3 = new StandaloneObjectFactory();
    factory3.init( null, null );
    factory3.defineObject( "MimeTypeListener", MimeTypeListener.class.getName(),
        IPentahoDefinableObjectFactory.Scope.GLOBAL );

    AggregateObjectFactory aggFactory = (AggregateObjectFactory) PentahoSystem.getObjectFactory();
    aggFactory.registerObjectFactory( factory3 );

    List<MimeTypeListener> mimes = aggFactory.getAll( MimeTypeListener.class, session );
    assertEquals( 6, mimes.size() );

  }

  @Test
  public void testRePublish() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/republish.spring.xml", null );

    PentahoSystem.registerObjectFactory( factory );

    MimeTypeListener republished =
        PentahoSystem.get( MimeTypeListener.class, session, Collections.singletonMap( "republished", "true" ) );
    assertNotNull( republished );

    assertEquals( "Higher Priority MimeTypeListener", republished.name );

    IMimeTypeListener republishedAsInterface =
        PentahoSystem.get( IMimeTypeListener.class, session, Collections.singletonMap( "republishedAsInterface",
            "true" ) );
    assertNotNull( republishedAsInterface );
    assertEquals( "Higher Priority MimeTypeListener", ( (MimeTypeListener) republishedAsInterface ).name );

  }

  @Test
  public void testRePublishAttributes() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/republish.spring.xml", null );

    PentahoSystem.registerObjectFactory( factory );

    MimeTypeListener republished =
        PentahoSystem.get( MimeTypeListener.class, session, Collections.singletonMap( "someKey", "someValue" ) );
    assertNotNull( republished );

    assertEquals( "Higher Priority MimeTypeListener", republished.name );

  }

  @Test
  public void testGetById() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );

    AggregateObjectFactory aggFactory = (AggregateObjectFactory) PentahoSystem.getObjectFactory();

    MimeTypeListener info =
        aggFactory.get( MimeTypeListener.class, session, Collections.singletonMap( "id", "someID" ) );
    assertNotNull( info );

  }

  /**
   * Two Spring PentahoObjectFactories with the same underlying applicationContext should not be registered twice. This
   * case tests that the AggregateObjectFactory's set implementation is working properly.
   *
   * @throws Exception
   */
  @Test
  public void testRegistration() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();

    ConfigurableApplicationContext context =
        new FileSystemXmlApplicationContext( "src/test/resources/solution/system/pentahoObjects.spring.xml" );

    factory.init( null, context );

    StandaloneSpringPentahoObjectFactory factory2 = new StandaloneSpringPentahoObjectFactory();
    factory2.init( null, context );

    AggregateObjectFactory aggFactory = (AggregateObjectFactory) PentahoSystem.getObjectFactory();
    aggFactory.registerObjectFactory( factory );
    aggFactory.registerObjectFactory( factory2 );

    List<MimeTypeListener> mimes = aggFactory.getAll( MimeTypeListener.class, session );
    assertEquals( 5, mimes.size() );
  }

  /**
   * Two Spring PentahoObjectFactories with the same underlying applicationContext should not be registered twice. This
   * case tests that the AggregateObjectFactory's set implementation is working properly.
   *
   * @throws Exception
   */
  @Test
  public void testRegisteredButNotPublishingAnythingApplicationContext() throws Exception {

    PublishedBeanRegistry.reset();

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();

    ConfigurableApplicationContext context =
        new FileSystemXmlApplicationContext(
            "src/test/resources/solution/system/registeredButNotPublishing.spring.xml" );

    // this was causing an exception.
    factory.init( null, context );

    AggregateObjectFactory aggFactory = (AggregateObjectFactory) PentahoSystem.getObjectFactory();
    aggFactory.registerObjectFactory( factory );
    assertEquals( 0, PublishedBeanRegistry.getRegisteredFactories().size() );

  }
}
