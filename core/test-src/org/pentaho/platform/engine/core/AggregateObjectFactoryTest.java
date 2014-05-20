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

import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * User: nbaker Date: 3/3/13
 */
public class AggregateObjectFactoryTest {

  @Test
  public void testByKey() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "test-res/solution/system/pentahoObjects.spring.xml", null );

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
      new FileSystemXmlApplicationContext( "test-res/solution/system/pentahoObjects.spring.xml" );

    factory.init( null, context );

    StandaloneSpringPentahoObjectFactory factory2 = new StandaloneSpringPentahoObjectFactory();
    factory2.init( "test-res/solution/system/pentahoObjects.spring.xml", null );

    StandaloneObjectFactory factory3 = new StandaloneObjectFactory();
    factory3.init( null, null );
    factory3.defineObject( "MimeTypeListener", MimeTypeListener.class.getName(),
      IPentahoDefinableObjectFactory.Scope.GLOBAL );

    AggregateObjectFactory aggFactory = new AggregateObjectFactory();
    aggFactory.registerObjectFactory( factory3 );
    aggFactory.registerObjectFactory( factory2 );
    aggFactory.registerObjectFactory( factory );

    List<MimeTypeListener> mimes = aggFactory.getAll( MimeTypeListener.class, session );
    assertEquals( 11, mimes.size() );

  }

  @Test
  public void testRePublish() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "test-res/solution/system/republish.spring.xml", null );

    PentahoSystem.registerObjectFactory( factory );

    MimeTypeListener republished =
      PentahoSystem.get( MimeTypeListener.class, session, Collections.singletonMap( "republished", "true" ) );
    assertNotNull( republished );

    assertEquals( "Higher Priority MimeTypeListener", republished.name );

    IMimeTypeListener republishedAsInterface =
      PentahoSystem.get( IMimeTypeListener.class, session, Collections
        .singletonMap( "republishedAsInterface", "true" ) );
    assertNotNull( republishedAsInterface );
    assertEquals( "Higher Priority MimeTypeListener", ( (MimeTypeListener) republishedAsInterface ).name );

  }


  @Test
  public void testRePublishAttributes() throws Exception {

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "test-res/solution/system/republish.spring.xml", null );

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
    factory.init( "test-res/solution/system/pentahoObjects.spring.xml", null );

    AggregateObjectFactory aggFactory = new AggregateObjectFactory();
    aggFactory.registerObjectFactory( factory );

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
      new FileSystemXmlApplicationContext( "test-res/solution/system/pentahoObjects.spring.xml" );

    factory.init( null, context );

    StandaloneSpringPentahoObjectFactory factory2 = new StandaloneSpringPentahoObjectFactory();
    factory2.init( null, context );

    AggregateObjectFactory aggFactory = new AggregateObjectFactory();
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

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();

    ConfigurableApplicationContext context =
      new FileSystemXmlApplicationContext( "test-res/solution/system/registeredButNotPublishing.spring.xml" );

    // this was causing an exception.
    factory.init( null, context );

    AggregateObjectFactory aggFactory = new AggregateObjectFactory();
    aggFactory.registerObjectFactory( factory );
    assertEquals( 0, PublishedBeanRegistry.getRegisteredFactories().size() );

  }
}
