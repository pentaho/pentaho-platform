package org.pentaho.platform.engine.core;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.AggregateObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * User: nbaker
 * Date: 3/3/13
 */
public class AggregateObjectFactoryTest {

  @Test
  public void testByKey() throws Exception{


    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory( );
    factory.init("test-res/solution/system/pentahoObjects.spring.xml", null);


    AggregateObjectFactory aggFactory = new AggregateObjectFactory();
    aggFactory.registerObjectFactory(factory);

    GoodObject info = aggFactory.get(GoodObject.class, "GoodObject", session);
    assertNotNull(info);
  }

  @Test
  public void testCombined() throws Exception{

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory( );

    ConfigurableApplicationContext context = new FileSystemXmlApplicationContext("test-res/solution/system/pentahoObjects.spring.xml");

    factory.init(null, context);


    StandaloneSpringPentahoObjectFactory factory2 = new StandaloneSpringPentahoObjectFactory( );
    factory2.init("test-res/solution/system/pentahoObjects.spring.xml", null );

    StandaloneObjectFactory factory3 = new StandaloneObjectFactory( );
    factory3.init(null, null );
    factory3.defineObject("MimeTypeListener", MimeTypeListener.class.getName(), IPentahoDefinableObjectFactory.Scope.GLOBAL);


    AggregateObjectFactory aggFactory = new AggregateObjectFactory();
    aggFactory.registerObjectFactory(factory3);
    aggFactory.registerObjectFactory(factory2);
    aggFactory.registerObjectFactory(factory);

    List<MimeTypeListener> mimes = aggFactory.getAll(MimeTypeListener.class, session);
    assertEquals(11, mimes.size());

  }


  @Test
  public void testRePublish() throws Exception{

    StandaloneSession session = new StandaloneSession();
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory( );
    factory.init("test-res/solution/system/republish.spring.xml", null);

    PentahoSystem.registerObjectFactory(factory);

    MimeTypeListener republished = PentahoSystem.get(MimeTypeListener.class, session, Collections.singletonMap("republished", "true"));
    assertNotNull(republished);

    assertEquals("Higher Priority MimeTypeListener", republished.name);


    IMimeTypeListener republishedAsInterface = PentahoSystem.get(IMimeTypeListener.class, session, Collections.singletonMap("republishedAsInterface", "true"));
    assertNotNull(republishedAsInterface);
    assertEquals("Higher Priority MimeTypeListener", ((MimeTypeListener)republishedAsInterface).name);

  }
}
