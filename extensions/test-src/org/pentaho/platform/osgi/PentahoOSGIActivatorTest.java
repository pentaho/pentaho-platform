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
