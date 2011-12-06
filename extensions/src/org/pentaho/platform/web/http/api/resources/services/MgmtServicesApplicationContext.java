package org.pentaho.platform.web.http.api.resources.services;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MgmtServicesApplicationContext {
  private static final String SPRING_FILE_LOCATION = "com/pentaho/mgmt/console/spring.xml"; //$NON-NLS-1$  

  private static ClassPathXmlApplicationContext context;

  private MgmtServicesApplicationContext() {
    
  }
  
  public static synchronized ApplicationContext getContext() {
    if(context == null) {
      context = new ClassPathXmlApplicationContext(SPRING_FILE_LOCATION);
    }
    return context;
  }
  
  public static synchronized void initialize() {
    if (context != null) {
      context.close();
    }
    context = null;
  }
}
