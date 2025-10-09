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

import java.util.Set;

import org.pentaho.platform.servicecoordination.api.IServiceBarrier;
import org.pentaho.platform.servicecoordination.api.IServiceBarrierManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * This class is registered with spring. Spring calls the implemented methods just before, and just after, instantiation
 * of a bean. 
 * 
 * @author tkafalas
 */
public class BarrierBeanPostProcessor implements BeanPostProcessor {
  static IServiceBarrierManager serviceBarrierManager;
  
  private static Logger logger = LoggerFactory.getLogger( BarrierBeanPostProcessor.class );
  
  private static BarrierBeanPostProcessor barrierBeanPostProcessor;

  private BarrierBeanPostProcessor() {
    serviceBarrierManager = IServiceBarrierManager.LOCATOR.getManager();
  }

  public static BarrierBeanPostProcessor getInstance() {
    if ( barrierBeanPostProcessor == null ) {
      barrierBeanPostProcessor = new BarrierBeanPostProcessor();
    }
    return barrierBeanPostProcessor;
  }

  @Override
  public Object postProcessBeforeInitialization( Object bean, String beanName ) throws BeansException {
    logger.debug( "beforeInitialization: " + beanName );
    Set<String> barriers = BarrierBeanProcessor.getInstance().getBeanBarriers().get( beanName );
    if ( barriers != null ) {
      for ( String barrierName : barriers ) {
        IServiceBarrier barrier = serviceBarrierManager.getServiceBarrier( barrierName );
        barrier.hold();
      }
    }
    return bean;
  }

  @Override
  /**
   * If a bean is initialized that is registered with the {{@link #BarrierBeanProcessor}, then one hold on that
   * barrier will be released.
   */
  public Object postProcessAfterInitialization( Object bean, String beanName ) throws BeansException {
    logger.debug( "AfterInitialization: " + beanName );
    Set<String> barriers = BarrierBeanProcessor.getInstance().getBeanBarriers().get( beanName );
    if ( barriers != null ) {
      for ( String barrierName : barriers ) {
        IServiceBarrier barrier = serviceBarrierManager.getServiceBarrier( barrierName );
        barrier.release();
      }
    }
    return bean;
  }

}
