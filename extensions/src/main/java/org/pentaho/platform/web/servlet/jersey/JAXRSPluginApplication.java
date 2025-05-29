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

package org.pentaho.platform.web.servlet.jersey;

import com.fasterxml.jackson.core.util.JacksonFeature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.pentaho.platform.web.servlet.PluginDispatchServlet;

/**
 * JAXRSPluginApplication is a class that extends ResourceConfig.
 * This can be used to configure the JAX-RS application of the Pentaho-platform Plugins.
 * PluginDispatchServlet will create JAR-Rs application of each plugin.
 * This class will be configured as an initialization parameter for the Pentaho-platform's {@link PluginDispatchServlet} in the web.xml file.
 */
public class JAXRSPluginApplication extends ResourceConfig {

  private static final Log logger = LogFactory.getLog( JAXRSPluginApplication.class );

  /**
   * <p>The constructor for the JAXRSPluginApplication class.
   * This constructor can be used for setting up the necessary configurations for the JAX-RS application.
   * It does this by registering the necessary packages and features.</p><br/>
   * <p> Jersey application will scan packages automatically based on the beans defined in the plugin.spring.xml file for the jersey resources.</p>
   * <p> Following features are registered as part of Platform Jersey application initialization.
   * <li> MultiPartFeature : Provides support for handling multipart requests.</li>
   * <li> JacksonFeature : Provides support for JSON serialization and deserialization along with JAXB support.</li></p>
   */
  public JAXRSPluginApplication( String[] packages ) {
    packages( packages );
    register( MultiPartFeature.class );
    register( JacksonFeature.class );
    property( "jersey.config.server.response.setStatusOverSendError", true );

    if ( logger.isDebugEnabled() ) {
      property( "jersey.config.server.tracing", "ALL" );
      property( "jersey.config.server.tracing.threshold", "VERBOSE" );
    }
  }
}
