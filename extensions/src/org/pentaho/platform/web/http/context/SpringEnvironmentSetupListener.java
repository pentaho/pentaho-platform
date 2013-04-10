package org.pentaho.platform.web.http.context;

import org.pentaho.platform.web.http.PentahoHttpSessionHelper;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

/**
 * Spring needs to know the location of the pentaho "system" folder before startup. This class
 * computes and stores this path as a System property.
 *
 * User: nbaker
 * Date: 4/8/13
 */
public class SpringEnvironmentSetupListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {

    ServletContext context = servletContextEvent.getServletContext();

    String solutionPath = PentahoHttpSessionHelper.getSolutionPath(context);
    if (solutionPath != null) {
      String systemPath  = solutionPath + File.separator + "system";
      System.setProperty("PentahoSystemPath", systemPath);
    }

  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
  }

}
