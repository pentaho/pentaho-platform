package org.pentaho.platform.engine.core.system.objfac.spring;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * When added as a bean to a Spring context this class will register the ApplicationContext as a
 * StandalongSpringPentahoObjectFactory with the PentahoSystem
 *
 * User: nbaker
 * Date: 3/31/13
 */
public class ApplicationContextPentahoSystemRegisterer implements ApplicationContextAware, BeanFactoryPostProcessor, PriorityOrdered {

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    StandaloneSpringPentahoObjectFactory objFact = new StandaloneSpringPentahoObjectFactory();
    objFact.init(null, applicationContext);
    PentahoSystem.registerObjectFactory(objFact);
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
