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


package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.config.PropertiesFileConfiguration;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.services.pluginmgr.IAdminContentConditionalLogic;
import org.pentaho.platform.web.http.api.resources.services.UserConsoleService;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;

/**
 * The {@code UserConsoleResource} service provides both shared and user-specific state or settings related with the use
 * of the Pentaho User Console.
 * <p>
 * The following operations provide access to User Console system settings (shared by all users):
 * <ul>
 *   <li>{@link UserConsoleResource#getAdminContent()}</li>
 *   <li>{@link UserConsoleResource#getMantleSettings()}</li>
 *   <li>{@link UserConsoleResource#getMondrianCatalogs()}</li>
 *   <li>{@link UserConsoleResource#registeredPlugins()}</li>
 * </ul>
 * <p>
 * The {@code UserConsoleResource} exposes operations to access and manage <i>user session variables</i>.
 * User session variables are unique per user session and reset to their default values at every user session creation.
 * Contrast session variables with <i>user settings</i>, accessed via {@link UserSettingsResource},
 * which are persisted across user sessions and shared by all active sessions of a user.
 * <p>
 * One last characteristic of session variables is that only those declared in the properties
 * {@code userConsoleResource.getSessionVarWhiteList} and {@code userConsoleResource.setSessionVarWhiteList}
 * of the system file {@code system/restConfig.properties}, can be read or modified via this class.
 * By default, these are {@code scheduler_folder} and {@code showOverrideDialog}.
 * <p>
 * Generic user session variables are accessed and managed via the operations:
 * <ul>
 *   <li>{@link UserConsoleResource#setSessionVariable(String, String)}</li>
 *   <li>{@link UserConsoleResource#getSessionVariable(String)}</li>
 *   <li>{@link UserConsoleResource#clearSessionVariable(String)}</li>
 * </ul>
 * <p>
 * The following operations expose specific (non-generic) user session information:
 * <ul>
 *   <li>{@link UserConsoleResource#isAuthenticated()}</li>
 *   <li>{@link UserConsoleResource#isAdministrator()}</li>
 *   <li>{@link UserConsoleResource#getLocale()} - gets the effective locale of the user session</li>
 *   <li>
 *     {@link UserConsoleResource#setLocaleOverride(String)} -
 *     sets the locale of the user session,
 *     via {@link org.pentaho.platform.util.messages.LocaleHelper#setSessionLocaleOverride(Locale)}
 *     and {@link org.pentaho.platform.util.messages.LocaleHelper#setThreadLocaleOverride(Locale)}
 *   </li>
 * </ul>
 * <p>
 * The state changing operations of this service,
 * with the notable exception of {@link UserConsoleResource#setLocaleOverride(String)},
 * are protected against CSRF attacks, and thus require a CSRF token to be called.
 * This resolves to:
 * <ul>
 *   <li>{@link UserConsoleResource#setSessionVariable(String, String)}</li>
 *   <li>{@link UserConsoleResource#clearSessionVariable(String)}</li>
 * </ul>
 */
@Path ( "/mantle/" )
public class UserConsoleResource extends AbstractJaxRSResource {

  private static final Log logger = LogFactory.getLog( UserConsoleResource.class );
  protected static UserConsoleService userConsoleService;
  private static ISystemConfig systemConfig;
  private static List<String> setSessionVarWhiteList;
  private static List<String> getSessionVarWhiteList;
  private static PropertiesFileConfiguration config;
  private static boolean isInitialized = false;

  public UserConsoleResource() {

    userConsoleService = new UserConsoleService();

    if ( !isInitialized ) {
      systemConfig = PentahoSystem.get( ISystemConfig.class );
      String solutionRootPath = PentahoSystem.getApplicationContext().getSolutionRootPath();
      config =
          new PropertiesFileConfiguration( "rest", new File( solutionRootPath + "/system/restConfig.properties" ) );
      try {
        systemConfig.registerConfiguration( config );
        setSessionVarWhiteList = Arrays
            .asList( systemConfig.getProperty( "rest.userConsoleResource.setSessionVarWhiteList" ).split( "," ) );
        getSessionVarWhiteList = Arrays
            .asList( systemConfig.getProperty( "rest.userConsoleResource.getSessionVarWhiteList" ).split( "," ) );
      } catch ( IOException e ) {
        //Default to hard coded white list
        setSessionVarWhiteList.add( "scheduler_folder" );
        setSessionVarWhiteList.add( "showOverrideDialog" );
        getSessionVarWhiteList.add( "scheduler_folder" );
        getSessionVarWhiteList.add( "showOverrideDialog" );
      }
      isInitialized = true;
    }

  }

  /**
   * Returns whether the current user is an administrator.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/mantle/isAdministrator
   * </p>
   *
   * @return String true if the user is an administrator, or false otherwise.
   *
   * <p><b>Example Response:</b></p>
   *  <pre function="syntax.xml">
   *     true
   *  </pre>
   */
  @GET
  @Path ( "/isAdministrator" )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Returns the boolean response" )
  } )
  public Response isAdministrator() {
    return buildOkResponse( String.valueOf( userConsoleService.isAdministrator() ) );
  }

  /**
   * Returns whether the user is sn authenticated user or not.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/mantle/isAuthenticated
   * </p>
   *
   * @return String true if the user is an administrator, or false otherwise.
   *
   * <p><b>Example Response:</b></p>
   *  <pre function="syntax.xml">
   *     true
   *  </pre>
   */
  @GET
  @Path ( "/isAuthenticated" )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Returns the boolean response" ),
      @ResponseCode ( code = 401, condition = "User is not authenticated" )
  } )
  public Response isAuthenticated() {
    return buildOkResponse( String.valueOf( userConsoleService.isAuthenticated() ) );
  }

  /**
   * Returns the list of admin related settings
   *
   * @return list of settings
   */
  @GET
  @Path ( "/getAdminContent" )
  @Facet ( name = "Unsupported" )
  @Produces ( { APPLICATION_JSON, APPLICATION_XML } )
  public SettingsWrapper getAdminContent() {

    ArrayList<Setting> settings = new ArrayList<Setting>();
    try {
      IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, UserConsoleService.getPentahoSession() );
      List<String> pluginIds = pluginManager.getRegisteredPlugins();
    nextPlugin:
      for ( String pluginId : pluginIds ) {
        String adminContentInfo = (String) pluginManager.getPluginSetting( pluginId, "admin-content-info", null );
        String exceptionMessage = (String) pluginManager.getPluginSetting( pluginId, "exception-message", null );
        if ( adminContentInfo != null ) {
          StringTokenizer nameValuePairs = new StringTokenizer( adminContentInfo, ";" );
          while ( nameValuePairs.hasMoreTokens() ) {
            String currentToken = nameValuePairs.nextToken().trim();
            if ( currentToken.startsWith( "conditional-logic-validator=" ) ) {
              String validatorName = currentToken.substring( "conditional-logic-validator=".length() );
              Class<?> validatorClass = pluginManager.getClassLoader( pluginId ).loadClass( validatorName );
              IAdminContentConditionalLogic validator = (IAdminContentConditionalLogic) validatorClass.newInstance();
              int status = validator.validate();
              if ( status == IAdminContentConditionalLogic.DISPLAY_ADMIN_CONTENT ) {
                settings.add( new Setting( "admin-content-info", adminContentInfo ) );
              }
              if ( status == IAdminContentConditionalLogic.DISPLAY_EXCEPTION_MESSAGE && exceptionMessage != null ) {
                settings.add( new Setting( "exception-message", exceptionMessage ) );
              }
              if ( status == IAdminContentConditionalLogic.AVOID_ADMIN_CONTENT ) {
                continue nextPlugin;
              }
            }
          }
        }
      }
    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
    }
    return new SettingsWrapper( settings );
  }

  /**
   * Return the current user console settings
   *
   * @return current settings
   */
  @GET
  @Path ( "/settings" )
  @Produces ( { APPLICATION_JSON, APPLICATION_XML } )
  @Facet ( name = "Unsupported" )
  public SettingsWrapper getMantleSettings() {
    ArrayList<Setting> settings = new ArrayList<Setting>();
    settings
        .add( new Setting( "login-show-users-list", PentahoSystem.getSystemSetting( "login-show-users-list", "" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    settings.add( new Setting( "documentation-url", PentahoSystem.getSystemSetting( "documentation-url", "" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    settings.add( new Setting( "submit-on-enter-key", PentahoSystem.getSystemSetting( "submit-on-enter-key", "true" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    settings.add( new Setting( "user-console-revision", PentahoSystem.getSystemSetting( "user-console-revision", "" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    settings.add( new Setting( "startupPerspective", PentahoSystem.getSystemSetting( "startup-perspective", "" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    settings.add( new Setting( "showOnlyPerspective", PentahoSystem.getSystemSetting( "show-only-perspective", "" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    int startupUrls = Integer.parseInt( PentahoSystem.getSystemSetting( "num-startup-urls", "0" ) );
    settings.add( new Setting( "num-startup-urls", PentahoSystem.getSystemSetting( "num-startup-urls", "0" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    for ( int i = 1; i <= startupUrls; i++ ) {
      settings.add( new Setting( "startup-url-" + i, PentahoSystem.getSystemSetting( "startup-url-" + i, "" ) ) ); //$NON-NLS-1$
      settings.add( new Setting( "startup-name-" + i, PentahoSystem.getSystemSetting( "startup-name-" + i, "" ) ) ); //$NON-NLS-1$
    }

    // Check for override of New Analysis View via pentaho.xml
    // Poked in via pentaho.xml entries
    // <new-analysis-view>
    // <command-url>http://www.google.com</command-url>
    // <command-title>Marc Analysis View</command-title>
    // </new-analysis-view>
    // <new-report>
    // <command-url>http://www.yahoo.com</command-url>
    // <command-title>Marc New Report</command-title>
    // </new-report>
    //
    String overrideNewAnalysisViewCommmand = PentahoSystem.getSystemSetting( "new-analysis-view/command-url", null ); //$NON-NLS-1$
    String overrideNewAnalysisViewTitle = PentahoSystem.getSystemSetting( "new-analysis-view/command-title", null ); //$NON-NLS-1$
    if ( ( overrideNewAnalysisViewCommmand != null ) && ( overrideNewAnalysisViewTitle != null ) ) {
      settings.add( new Setting( "new-analysis-view-command-url", overrideNewAnalysisViewCommmand ) ); //$NON-NLS-1$
      settings.add( new Setting( "new-analysis-view-command-title", overrideNewAnalysisViewTitle ) ); //$NON-NLS-1$
    }
    String overrideNewReportCommmand = PentahoSystem.getSystemSetting( "new-report/command-url", null ); //$NON-NLS-1$
    String overrideNewReportTitle = PentahoSystem.getSystemSetting( "new-report/command-title", null ); //$NON-NLS-1$
    if ( ( overrideNewReportCommmand != null ) && ( overrideNewReportTitle != null ) ) {
      settings.add( new Setting( "new-report-command-url", overrideNewReportCommmand ) ); //$NON-NLS-1$
      settings.add( new Setting( "new-report-command-title", overrideNewReportTitle ) ); //$NON-NLS-1$
    }

    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, UserConsoleService.getPentahoSession() ); //$NON-NLS-1$
    if ( pluginManager != null ) {
      // load content types from IPluginSettings
      int i = 0;
      for ( String contentType : pluginManager.getContentTypes() ) {
        IContentInfo info = pluginManager.getContentTypeInfo( contentType );
        if ( info != null ) {
          settings.add( new Setting( "plugin-content-type-" + i, "." + contentType ) ); //$NON-NLS-1$ //$NON-NLS-2$
          settings.add( new Setting( "plugin-content-type-icon-" + i, info.getIconUrl() ) ); //$NON-NLS-1$
          int j = 0;
          for ( IPluginOperation operation : info.getOperations() ) {
            settings.add( new Setting( "plugin-content-type-" + i + "-command-" + j, operation.getId() ) ); //$NON-NLS-1$
            settings.add( new Setting(
                "plugin-content-type-" + i + "-command-perspective-" + j, operation.getPerspective() ) ); //$NON-NLS-1$
            j++;
          }
          i++;
        }
      }
    }

    return new SettingsWrapper( settings );
  }

  /**
   * Return the list of mondrian cubes in the platform
   *
   * @return list of cubes
   */
  @GET
  @Path ( "/cubes" )
  @Facet ( name = "Unsupported" )
  @Produces ( { APPLICATION_JSON, APPLICATION_XML } )
  public CubesWrapper getMondrianCatalogs() {
    ArrayList<Cube> cubes = new ArrayList<Cube>();

    IMondrianCatalogService catalogService =
        PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService", UserConsoleService
            .getPentahoSession() ); //$NON-NLS-1$
    List<MondrianCatalog> catalogs = catalogService.listCatalogs( UserConsoleService.getPentahoSession(), true );

    for ( MondrianCatalog cat : catalogs ) {
      for ( MondrianCube cube : cat.getSchema().getCubes() ) {
        cubes.add( new Cube( cat.getName(), cube.getName(), cube.getId() ) );
      }
    }
    return new CubesWrapper( cubes );
  }

  /**
   * Apply the selected locale to the user console.
   *
   * @param locale (user console's locale)
   * @return
   */
  @POST
  @Path ( "/locale" )
  @Facet ( name = "Unsupported" )
  public Response setLocaleOverride( String locale ) {
    return new SystemResource().setLocaleOverride( locale );
  }

  /**
   * Return the server side locale
   *
   * @return server's locale
   */
  @GET
  @Path ( "/locale" )
  @Facet ( name = "Unsupported" )
  public Response getLocale() {
    return new SystemResource().getLocale();
  }

  /**
   * Sets the value of a session variable.
   *
   * @param key The name of the session variable
   * @param value The value of the session variable
   * @return
   */
  @POST
  @Path ( "/session-variable" )
  @Facet ( name = "Unsupported" )
  @Produces( TEXT_PLAIN )
  public Response setSessionVariable( @QueryParam ( "key" ) String key, @QueryParam ( "value" ) String value ) {
    if ( setSessionVarWhiteList.contains( key ) ) {
      IPentahoSession session = UserConsoleService.getPentahoSession();
      session.setAttribute( key, value );
      return Response.ok( session.getAttribute( key ) ).build();
    }
    return Response.status( FORBIDDEN ).build();
  }

  /**
   * Gets the value of a session variable.
   *
   * @param key The name of the session variable
   * @return
   */
  @GET
  @Path ( "/session-variable" )
  @Facet ( name = "Unsupported" )
  @Produces( {TEXT_PLAIN, APPLICATION_JSON, APPLICATION_XML} )
  public Response getSessionVariable( @QueryParam ( "key" ) String key ) {
    if ( getSessionVarWhiteList.contains( key ) ) {
      Object value = UserConsoleService.getPentahoSession().getAttribute( key );

      if ( value != null ) {
        return Response.ok( value ).build();
      } else {
        return Response.noContent().build();
      }
    }
    return Response.status( FORBIDDEN ).build();
  }

  /**
   * Deletes the value of a session variable.
   *
   * @param key The name of the session variable
   * @return
   */
  @DELETE
  @Path ( "/session-variable" )
  @Facet ( name = "Unsupported" )
  public Response clearSessionVariable( @QueryParam ( "key" ) String key ) {
    return Response.ok( UserConsoleService.getPentahoSession().removeAttribute( key ) ).build();
  }

  @GET
  @Path ( "/registeredPlugins" )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Returns a list of registered plugins as a list string" )
  } )
  @Facet ( name = "Unsupported" )
  public Response registeredPlugins() {
    List<String> registeredPlugins = userConsoleService.getRegisteredPlugins();
    return buildOkResponse( registeredPlugins.toString() );
  }

  protected Response buildOkResponse( Object entity ) {
    return Response.ok( entity ).build();
  }
}
