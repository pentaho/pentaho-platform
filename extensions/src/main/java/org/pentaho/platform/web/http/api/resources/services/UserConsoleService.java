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

package org.pentaho.platform.web.http.api.resources.services;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.config.PropertiesFileConfiguration;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UserConsoleService {
  public static final String DEFAULT_SCHEDULER_FOLDER = "scheduler_folder";
  public static final String DEFAULT_SHOW_OVERRIDE_DIALOG = "showOverrideDialog";

  private static final Log logger = LogFactory.getLog( UserConsoleService.class );
  private static final String SET_SESSION_VAR_WHITELIST_PROPERTY = "rest.userConsoleResource.setSessionVarWhiteList";
  private static final String GET_SESSION_VAR_WHITELIST_PROPERTY = "rest.userConsoleResource.getSessionVarWhiteList";
  private static final String REST_CONFIG_NAME = "rest";
  private static final String REST_CONFIG_PATH = "/system/restConfig.properties";

  private final List<String> setSessionVarWhiteList;
  private final List<String> getSessionVarWhiteList;

  public UserConsoleService() {
    this.setSessionVarWhiteList = loadWhiteList( SET_SESSION_VAR_WHITELIST_PROPERTY );
    this.getSessionVarWhiteList = loadWhiteList( GET_SESSION_VAR_WHITELIST_PROPERTY );
  }

  @VisibleForTesting
  protected List<String> loadWhiteList( String property ) {
    ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );
    String pathname = PentahoSystem.getApplicationContext().getSolutionRootPath() + REST_CONFIG_PATH;
    PropertiesFileConfiguration config = new PropertiesFileConfiguration(
      REST_CONFIG_NAME,
      new File( pathname ) );

    try {
      systemConfig.registerConfiguration( config );

      String value = systemConfig.getProperty( property );
      return Arrays.asList( value.split( "," ) );
    } catch ( IOException e ) {
      logger.warn( String.format( "Failed to load whitelist for property: %s", property ), e );
      return Arrays.asList( DEFAULT_SCHEDULER_FOLDER, DEFAULT_SHOW_OVERRIDE_DIALOG );
    }
  }

  public IPentahoSession getPentahoSession() {
    return PentahoSessionHolder.getSession();
  }

  /**
   * Returns whether the current user is an administrator
   *
   * @return boolean value depending on the current user being the administrator
   */
  public boolean isAdministrator() {
    return SystemUtils.canAdminister();
  }

  /**
   * Returns whether the user is authenticated or not
   *
   * @return boolean value depending on the current user being authenticated
   */
  public boolean isAuthenticated() {
    return getPentahoSession() != null && getPentahoSession().isAuthenticated();
  }

  /**
   * Returns a List of plugins registered to the pentaho system
   *
   * @return List of registered plugins
   */
  public List<String> getRegisteredPlugins() {
    return PentahoSystem.get( IPluginManager.class, getPentahoSession() ).getRegisteredPlugins();
  }

  /**
   * Retrieves a session variable from the current user session if it exists in the configured
   * {@link #getSessionVarWhiteList}.
   *
   * @param key the session variable key to retrieve.
   * @return the value associated with the key in the current session, or null if the key is not found
   * @throws ForbiddenSessionVariableException if the key is not in the {@link #getSessionVarWhiteList}
   */
  public Object getSessionVariable( String key ) {
    if ( !getSessionVarWhiteList.contains( key ) ) {
      logger.error( "Session variable '" + key + "' is not whitelisted to get." );
      throw new ForbiddenSessionVariableException( String.format( "Getting session variable not allowed: %s", key ) );
    }
    return getPentahoSession().getAttribute( key );
  }

  /**
   * Sets a session variable for the current user session if it exists in the configured
   * {@link #setSessionVarWhiteList}.
   * If value is null, this has the same effect as calling {@link #clearSessionVariable}.
   *
   * @param key   the session variable key to set.
   * @param value the value to set for the session variable. If null, the attribute is removed.
   * @return the value that was actually set in the session, or null if the attribute was removed or the key is not
   * found
   * @throws ForbiddenSessionVariableException if the key is not in the {@link #setSessionVarWhiteList}
   */
  public Object setSessionVariable( String key, String value ) {
    if ( !setSessionVarWhiteList.contains( key ) ) {
      logger.error( "Session variable '" + key + "' is not whitelisted to set." );
      throw new ForbiddenSessionVariableException( String.format( "Setting session variable not allowed: %s", key ) );
    }

    final var session = getPentahoSession();

    if ( value == null ) {
      return session.removeAttribute( key );
    } else {
      session.setAttribute( key, value );
      return value;
    }
  }

  /**
   * Removes a session variable from the current user session if it exists in the configured
   * {@link #setSessionVarWhiteList}.
   *
   * @param key the session variable key to remove.
   * @return the previous value associated with the key, or null if the key is not found
   * @throws ForbiddenSessionVariableException if the key is not in the {@link #setSessionVarWhiteList}
   */
  public Object clearSessionVariable( String key ) {
    if ( !setSessionVarWhiteList.contains( key ) ) {
      logger.error( "Session variable '" + key + "' is not whitelisted to set." );
      throw new ForbiddenSessionVariableException( String.format( "Clearing session variable not allowed: %s", key ) );
    }
    return getPentahoSession().removeAttribute( key );
  }
}
