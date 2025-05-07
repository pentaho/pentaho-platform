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

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.io.File;

/**
 * When added as a bean to a Spring context this class will register the ApplicationContext as a
 * StandaloneSpringPentahoObjectFactory with the PentahoSystem.
 * <p/>
 * User: nbaker Date: 3/31/13
 */
public class ApplicationContextPentahoSystemRegisterer implements ApplicationContextAware, BeanFactoryPostProcessor,
  PriorityOrdered {

  private static boolean barrierBeanFileProcessed;
  
  @Override
  public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
    safeAddBeanScopePostProcessors( applicationContext );
    StandaloneSpringPentahoObjectFactory objFact = StandaloneSpringPentahoObjectFactory.getInstance(
      applicationContext );

    PentahoSystem.registerObjectFactory( objFact );
    PublishedBeanRegistry.registerFactory( applicationContext );
  }

  @Override
  public void postProcessBeanFactory( ConfigurableListableBeanFactory configurableListableBeanFactory )
    throws BeansException {
    configurableListableBeanFactory.addBeanPostProcessor( BarrierBeanPostProcessor.getInstance() );
    // Make sure barrierbean.xml is processed before any spring processing.
    if ( !barrierBeanFileProcessed ) {
      // Put ServiceBarrier holds in place
      String barrierBeanFilePath =
          System.getProperty( "PentahoSystemPath" ) + File.separator + "barrierbean.properties";
      BarrierBeanProcessor.getInstance().registerBarrierBeans( barrierBeanFilePath );
      barrierBeanFileProcessed = true;
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  protected void safeAddBeanScopePostProcessors( ApplicationContext appCtx ) {

    if( appCtx != null && appCtx instanceof AbstractApplicationContext ) {

      boolean beanScopePostProcessorExists = false;

      for( BeanFactoryPostProcessor bfpp : ( ( AbstractApplicationContext ) appCtx ).getBeanFactoryPostProcessors() ) {
        beanScopePostProcessorExists |= ( bfpp != null && bfpp instanceof PentahoBeanScopeValidatorPostProcessor );
      }

      if( !beanScopePostProcessorExists ){

        ( ( AbstractApplicationContext ) appCtx ).addBeanFactoryPostProcessor(
            new PentahoBeanScopeValidatorPostProcessor() );
      }
    }
  }
}
