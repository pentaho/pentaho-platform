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


package org.pentaho.platform.osgi;

import org.junit.AfterClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.AggregateObjectFactory;

import static junit.framework.Assert.assertEquals;

/**
 * User: nbaker Date: 11/19/13 Time: 3:01 PM
 */
public class PentahoOSGIActivatorTest {

  @Test
  public void testActivation() throws Exception {
    AggregateObjectFactory agg = (AggregateObjectFactory) PentahoSystem.getObjectFactory();
    int originalSize = agg.getFactories().size();
    BundleContext context = Mockito.mock( BundleContext.class );
    PentahoOSGIActivator activator = new PentahoOSGIActivator();
    activator.setBundleContext( context );
    assertEquals( 1, agg.getFactories().size() - originalSize );
//    assertTrue( agg.getFactories()
//      .toArray( new IPentahoObjectFactory[ agg.getFactories().size() ] )[ 1 ] instanceof OSGIObjectFactory );
  }


  @AfterClass
  public static void after() {
    ( (AggregateObjectFactory) PentahoSystem.getObjectFactory() ).clear();
  }
}
