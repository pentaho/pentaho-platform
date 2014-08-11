package org.pentaho.platform.osgi;

import org.osgi.framework.BundleContext;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.context.WebApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

/**
 * Standard OSGI Activator class which is called when the OSGI environment is started. Work to integrate the OSGI
 * container with the PentahoSystem is started from this class
 */
public class PentahoOSGIActivator {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private OSGIObjectFactory objectFactory;

  public void setBundleContext( BundleContext bundleContext ) throws Exception {
    IApplicationContext applicationContext = PentahoSystem.getApplicationContext();
    if( applicationContext instanceof WebApplicationContext) {

      WebApplicationContext webApplicationContext = (WebApplicationContext) applicationContext;
      ServletContext servletContext = (ServletContext) webApplicationContext.getContext();
      servletContext.setAttribute(BundleContext.class.getName(), bundleContext);
    }

    logger.debug("Registering OSGIObjectFactory");

    objectFactory = new OSGIObjectFactory( bundleContext );
    PentahoSystem.registerObjectFactory( objectFactory );
    logger.debug("OSGIObjectFactory installed");

  }

  public void shutdown() {
    if ( objectFactory != null ) {
      PentahoSystem.deregisterObjectFactory( objectFactory );
    }
  }

}
