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

package org.pentaho.platform.engine.core.system.objfac.spring;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;
import org.pentaho.platform.servicecoordination.api.IServiceBarrierManager;

/**
 * 
 * @author tkafalas
 *
 */
public class BarrierBeanPostProcessorTest {
  private static final String BEAN_NAME = "beanName";
  private static final String BARRIER_NAME = "barrierName";
  private static final Object theBean = new String("theBean");

  @Test
  public void testPostProcess() throws Exception {
    Properties barrierBeanProperties = new Properties();
    barrierBeanProperties.setProperty( BARRIER_NAME, BEAN_NAME );
    BarrierBeanProcessor.getInstance().registerBarrierBeans( barrierBeanProperties );

    BarrierBeanPostProcessor postProcessor = BarrierBeanPostProcessor.getInstance();
    postProcessor.postProcessBeforeInitialization( theBean, BEAN_NAME );
    postProcessor.postProcessAfterInitialization( theBean, BEAN_NAME );

    // Await the barrier in a new thread so we can continue if something goes wrong
    Thread t = new Thread( new Runnable() {
      @Override
      public void run() {
        BarrierBeanProcessor.getInstance().awaitBarrier( BARRIER_NAME );
      }
    }

    );

    t.start();
    boolean success = false;
    for ( int i = 0; i < 10; i++ ) {
      success = IServiceBarrierManager.LOCATOR.getManager().getServiceBarrier( BARRIER_NAME ).isAvailable();
      if ( success ) {
        break;
      }
      System.out.println(i);
      Thread.sleep( 100 );
    }
    assertTrue( "Did not release the barrier bean", success );
  }

}
