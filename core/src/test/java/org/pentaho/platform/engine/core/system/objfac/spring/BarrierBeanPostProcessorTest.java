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
