/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.engine.core.system.objfac.spring;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * To maintain legacy compatibility, upon BeanFactory init we'll traverse through all beans in its containing
 * factoryMap and for each that holds an empty scope definition we'll set it with a BeanDefinition.SCOPE_SINGLETON
 * <p />
 * Contextualization:
 * <p />
 * in previous spring framework 2.5.6 any non-explicit bean scope declaration would be set with default a value of
 * BeanDefinition.SCOPE_SINGLETON;
 * <p />
 * in latest spring framework 3.2.14 the scope is set with an empty string; the scope checking should now be done using
 * the isSingleton() predicate, whose value is set to 'true' when scope is either set as 'singleton' or empty string
 * <p />
 * @see https://github.com/spring-projects/spring-framework/blob/v3.2.14.RELEASE/spring-beans/src/main/java/org/springframework/beans/factory/support/AbstractBeanDefinition.java#L437
 * <p />
 * Jersey libs, however, use the getScope() as the means for validating if a spring is singleton scoped or not;
 * And while in spring 2.5.6 that was alright, with the new spring 3.2.14 that's not quite the case
 * <p />
 * @see https://github.com/jersey/jersey-1.x/blob/master/jersey-core/src/main/java/com/sun/jersey/core/spi/component/ioc/IoCProviderFactory.java#L122-L126
 *
 */
public class PentahoBeanScopeValidatorPostProcessor implements BeanFactoryPostProcessor {

  private static Log logger = LogFactory.getLog( PentahoBeanScopeValidatorPostProcessor.class );

  @Override
  public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException {

    if( beanFactory != null && beanFactory.getBeanDefinitionCount() > 0 ){

      for( String beanName : beanFactory.getBeanDefinitionNames() ){

        validateBeanScope( beanName, beanFactory.getBeanDefinition( beanName ) );

      }
    }
  }

  protected void validateBeanScope( String beanName , BeanDefinition bd ) {

    if( bd != null && StringUtils.isEmpty( bd.getScope() ) ){
      logger.info( "Setting '" + beanName + "' with 'singleton' scope" );
      bd.setScope( BeanDefinition.SCOPE_SINGLETON );
    }
  }
}
