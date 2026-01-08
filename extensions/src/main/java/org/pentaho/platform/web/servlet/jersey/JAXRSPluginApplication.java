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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.pentaho.platform.web.servlet.PluginDispatchServlet;

/**
 * JAXRSPluginApplication is a class that extends ResourceConfig.
 * This can be used to configure the JAX-RS application of the Pentaho-platform Plugins.
 * PluginDispatchServlet will create JAX-RS application of each plugin.
 * This class will be configured as an initialization parameter for the Pentaho-platform's {@link PluginDispatchServlet} in the web.xml file.
 * <br/><br/>
 * This class can be defined as bean with packages as constructor arguments in plugin.spring.xml of a plugin if there are any resources package to be scanned for jersey resources.
 * <br/>
 * For Example:
 * <code><pre>
 *     &lt;bean class&equals;&quot;org&period;pentaho&period;platform&period;web&period;servlet&period;jersey&period;JAXRSPluginApplication&quot;&gt;
 *        &lt;constructor-arg&gt;
 *            &lt;list&gt;
 *                &lt;value&gt;org&period;pentaho&lt;&sol;value&gt;
 *            &lt;&sol;list&gt;
 *        &lt;&sol;constructor-arg&gt;
 *     &lt;&sol;bean&gt;
 * </pre></code>
 */
public class JAXRSPluginApplication extends ResourceConfig {

  private static final Log logger = LogFactory.getLog( JAXRSPluginApplication.class );


  /**
   * If a plugin has a use-case of customizing JAX-RS application, it can use this bean name to refer to the JAX-RS application to create bean.
   */
  public static final String APP_BEAN_NAME = "pluginApp";
  /**
   * <p>The constructor for the JAXRSPluginApplication class.
   * This constructor can be used for setting up the necessary configurations for the JAX-RS application.
   * It does this by registering the necessary packages and features.</p><br/>
   * <p> Jersey application will scan packages automatically based on the beans defined in the plugin.spring.xml file for the jersey resources.
   * In case if any package missed, then the plugin has to do the bean definition for the app </p>
   * <p> Following features are registered as part of Platform Jersey application initialization.
   * <li> MultiPartFeature : Provides support for handling multipart requests.</li>
   */
  public JAXRSPluginApplication( String[] packages ) {
    packages( packages );
    register( MultiPartFeature.class );

    property( "jersey.config.server.response.setStatusOverSendError", true );

    if ( logger.isDebugEnabled() ) {
      property( "jersey.config.server.tracing.type", "ALL" );
      property( "jersey.config.server.tracing.threshold", "VERBOSE" );
    }
  }
}
