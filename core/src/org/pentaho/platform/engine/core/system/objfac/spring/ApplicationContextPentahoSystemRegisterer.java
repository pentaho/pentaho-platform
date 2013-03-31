package org.pentaho.platform.engine.core.system.objfac.spring;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * User: nbaker
 * Date: 3/31/13
 */
public class ApplicationContextPentahoSystemRegisterer implements ApplicationContextAware {
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    StandaloneSpringPentahoObjectFactory objFact = new StandaloneSpringPentahoObjectFactory();
    objFact.init(null, applicationContext);
    PentahoSystem.registerObjectFactory(objFact);
  }
}
