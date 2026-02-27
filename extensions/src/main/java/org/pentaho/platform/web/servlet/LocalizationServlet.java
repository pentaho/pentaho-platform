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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.servlet.messages.Messages;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;

/**
 * This class makes a message bundle available as a JSON hash. This is designed to be used as a web service to allow
 * thin-clients to retrieve message bundles from the server.
 */
public class LocalizationServlet extends ServletBase {

  private static final Log logger = LogFactory.getLog( LocalizationServlet.class );

  private static final String DEFAULT_CACHE_MESSAGES_SETTING = "false"; //$NON-NLS-1$

  @Override
  public Log getLogger() {
    return logger;
  }

  @Override
  public void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
    doPost( req, resp );
  }

  @Override
  public void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
    String pluginId = req.getParameter( "plugin" ); //$NON-NLS-1$
    String name = req.getParameter( "name" ); //$NON-NLS-1$

    try {
      String json = getJSONBundle( pluginId, name );
      this.setCorsHeaders( req, resp );

      resp.setContentType( "text/plain" ); //$NON-NLS-1$
      resp.setStatus( HttpServletResponse.SC_OK );
      resp.setCharacterEncoding( LocaleHelper.getSystemEncoding() );
      PrintWriter writer = resp.getWriter();
      try {
        writer.write( json );
      } finally {
        writer.close();
      }
    } catch ( Exception ex ) {
      error( Messages.getInstance().getErrorString( "LocalizationServlet.ERROR_0000_ERROR" ), ex ); //$NON-NLS-1$
      resp.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * Retrieve a {@link java.util.ResourceBundle} from a plugin.
   * 
   * @param pluginId
   *          ID of the plugin to load the resource bundle from
   * @param name
   *          Resource bundle name that resides in the plugin
   * @return Resource bundle for the name provided in the plugin referenced by {@code pluginId}
   * @throws IllegalArgumentException
   *           Invalid plugin Id
   * @throws java.util.MissingResourceException
   *           Invalid resource bundle name
   */
  protected ResourceBundle getBundle( String pluginId, String name ) {
    IPluginManager pm = PentahoSystem.get( IPluginManager.class );
    ClassLoader pluginClassLoader = pm.getClassLoader( pluginId );

    if ( pluginClassLoader == null ) {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "LocalizationServlet.ERROR_0001_INVALID_PLUGIN_ID", pluginId ) ); //$NON-NLS-1$
    }
    if ( name == null || name.length() == 0 ) {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "LocalizationServlet.ERROR_0002_INVALID_RESOURCE_NAME", name ) ); //$NON-NLS-1$
    }

    ResourceBundle bundle = ResourceBundle.getBundle( name, LocaleHelper.getLocale(), pluginClassLoader );
    // Clear the bundle's cached messages if we shouldn't be caching them
    if ( !isMessageCachingEnabled( pm, pluginId ) ) {
      ResourceBundle.clearCache();
    }
    return bundle;
  }

  /**
   * Should the messages in the Resource Bundle be cached?
   * 
   * @param pm
   *          Plugin manager
   * @param pluginId
   *          ID of plugin whose "cache-messages" setting should be checked
   * @return {@code true} if the localization messages loaded from this plugin should be cached
   */
  public boolean isMessageCachingEnabled( IPluginManager pm, String pluginId ) {
    Object cache = pm.getPluginSetting( pluginId, "cache-messages", DEFAULT_CACHE_MESSAGES_SETTING ); //$NON-NLS-1$
    // Check whether we want to clear the bundle cache which is useful to test resource file changes
    return !DEFAULT_CACHE_MESSAGES_SETTING.equals( cache );
  }

  /**
   * Load the resource bundle for the plugin provided and return the resulting properties map as JSON. This is intended
   * to be used with Dojo's i18n system
   * (http://dojotoolkit.org/reference-guide/1.7/quickstart/internationalization/index
   * .html#quickstart-internationalization-index)
   * 
   * @param pluginId
   *          ID of plugin to load the resource from
   * @param name
   *          Name of the resource to load
   * @return JSON String with a hash of key/value pairs representing properties from the requested resource bundle
   */
  public String getJSONBundle( String pluginId, String name ) {
    try {
      return getJsonForBundle( getBundle( pluginId, name ) );
    } catch ( Exception e ) {
      throw new RuntimeException( e.toString(), e );
    }
  }

  /**
   * Convert a {@see ResourceBundle} into a JSON string.
   * 
   * @param bundle
   *          Resource bundle to convert
   * @return Bundle with all key/value pairs as entries in a hash, returned as a JSON string.
   * @throws org.json.JSONException
   */
  protected String getJsonForBundle( ResourceBundle bundle ) throws JSONException {
    JSONObject cat = new JSONObject();
    for ( String key : bundle.keySet() ) {
      cat.put( key, bundle.getString( key ) );
    }
    return cat.toString();
  }
}
