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


package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.util.IWadlDocumentResource;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import org.glassfish.jersey.server.wadl.config.WadlGeneratorConfig;
import org.glassfish.jersey.server.wadl.config.WadlGeneratorDescription;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.WadlGeneratorResourceDocSupport;

/**
 * The wadl configurator class that extends in run time the wadl pointing to a file that adds pre-computed information
 */
public class PentahoWadlGeneratorConfig extends WadlGeneratorConfig {

  @Override
  public List<WadlGeneratorDescription> configure() {
    String originalRequest = getOriginalRequest();

    Pattern pluginPattern = Pattern.compile( ".*\\/plugin\\/([^/]+)\\/api\\/application.wadl" );
    Matcher pluginMatcher = pluginPattern.matcher( originalRequest );

    String plugin = null;
    if ( pluginMatcher.matches() ) {
      plugin = pluginMatcher.group( 1 );
    }

    WadlGeneratorConfigDescriptionBuilder builder = getBuilder( plugin );

    if ( builder != null ) {
      return builder.descriptions();
    } else {
      return new ArrayList<WadlGeneratorDescription>();
    }
  }

  /**
   * Gets the original request used to obtain the wadl.
   * The wadl request is for plugins is changed so we need to retrieve its original form to be able to get the plugin
   * and point to the right wadl extension file.
   *
   * @return String with the original url request
   */
  protected String getOriginalRequest() {
    JAXRSPluginServlet jaxrsPluginServlet = getJAXRSPluginServlet();
    String originalRequest = "";
    if ( jaxrsPluginServlet != null ) {
      originalRequest = (String) jaxrsPluginServlet.requestThread.get();
    }
    if ( originalRequest == null || originalRequest.isEmpty() ) {
      return "/api/application.wadl"; // global api isn't filled
    }
    return originalRequest;
  }

  /**
   * Gets the WadlGeneratorConfigDescriptionBuilder that is used to extend the wadl.
   * It was a property pointing to the InputStream of the file
   *
   * @param plugin String with the plugin that is requesting the wadl extension
   * @return WadlGeneratorConfigDescriptionBuilder that is used to extend the wadl.
   */
  protected WadlGeneratorConfigDescriptionBuilder getBuilder( String plugin ) {
    List<IWadlDocumentResource> resourceReferences = getWadlDocumentResources();

    InputStream is = null;
    try {
      for ( IWadlDocumentResource wadlDocumentResource : resourceReferences ) {
        if ( plugin == null && !wadlDocumentResource.isFromPlugin() ) {
          is = wadlDocumentResource.getResourceAsStream();
          break;
        } else if ( wadlDocumentResource.isFromPlugin() && wadlDocumentResource.getPluginId().equals( plugin ) ) {
          is = wadlDocumentResource.getResourceAsStream();
          break;
        }
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    if ( is != null ) {
      return generator( WadlGeneratorResourceDocSupport.class ).prop( "resourceDocStream", is );
    }

    return null;
  }

  /**
   * Gets the JAXRSPluginServlet for api that stores the the original request for the wadl
   *
   * @return JAXRSPluginServlet with the stored original request
   */
  protected JAXRSPluginServlet getJAXRSPluginServlet() {
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
    JAXRSPluginServlet jaxrsPluginServlet;

    try {
      jaxrsPluginServlet = (JAXRSPluginServlet) pluginManager.getBean( "api" );
    } catch ( Exception e ) {
      return null;
    }
    return jaxrsPluginServlet;
  }

  /**
   * Returns the list of the wadl extension declared beans
   *
   * @return List<IWadlDocumentResource> with the declared beans
   */
  protected List<IWadlDocumentResource> getWadlDocumentResources() {
    return PentahoSystem.getAll( IWadlDocumentResource.class );
  }
}
