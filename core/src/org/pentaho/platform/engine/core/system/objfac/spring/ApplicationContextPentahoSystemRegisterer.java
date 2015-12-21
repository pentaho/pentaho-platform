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

package org.pentaho.platform.engine.core.system.objfac.spring;

import java.io.File;

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

/**
 * When added as a bean to a Spring context this class will register the ApplicationContext as a
 * StandalongSpringPentahoObjectFactory with the PentahoSystem
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
    // if (configurableListableBeanFactory.containsBeanDefinition("unifiedRepository")){
    configurableListableBeanFactory.addBeanPostProcessor( BarrierBeanPostProcessor.getInstance() );
    // Make sure barrierbean.xml is processed before any spring processing.
    if ( !barrierBeanFileProcessed ) {
      // Put ServiceBarrier holds in place
      String barrierBeanFilePath =
          System.getProperty( "PentahoSystemPath" ) + File.separator + "barrierbean.properties";
      BarrierBeanProcessor.registerBarrierBeans( barrierBeanFilePath );
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
