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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;

import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.webservices.ExecutableFileTypeDtoWrapper;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.api.repository2.unified.webservices.ExecutableFileTypeDto;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.api.resources.services.SystemService;
import org.pentaho.platform.web.http.messages.Messages;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * This api provides methods for discovering information about the system
 * 
 * @author pminutillo
 */
@Path( "/system/" )
@ExternallyManagedLifecycle
@Facet( name = "Unsupported" )
public class SystemResource extends AbstractJaxRSResource {

  private static final Log logger = LogFactory.getLog( SystemResource.class );
  private ISystemConfig systemConfig;
  private IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );


  public SystemResource() {
    this( PentahoSystem.get( ISystemConfig.class ) );
  }

  public SystemResource( ISystemConfig systemConfig ) {
    this.systemConfig = systemConfig;
  }

  /**
   * Returns all users, roles, and ACLs in an XML document. Moved here from now removed SystemAllResource class
   * 
   * Response Sample: <content> <users> <user>joe</user> </users> <roles> <role>Admin</role> </roles> <acls> <acl>
   * <name>Update</name> <mask>8</mask> </acl> </acls> </content>
   * 
   * @return Response containing roles, users, and acls
   * @throws Exception
   */
  @GET
  @Produces( { MediaType.APPLICATION_XML } )
  @Facet ( name = "Unsupported" )
  public Response getAll() throws Exception {
    try {
      if ( canAdminister() ) {
        return Response.ok( SystemService.getSystemService().getAll().asXML() ).type( MediaType.APPLICATION_XML ).build();
      } else {
        return Response.status( UNAUTHORIZED ).build();
      }
    } catch ( Throwable t ) {
      throw new WebApplicationException( t );
    }
  }

  /**
   * Return JSON string reporting which authentication provider is currently in use
   * 
   * Response sample: { "authenticationType": "JCR_BASED_AUTHENTICATION" }
   * 
   * @return AuthenticationProvider represented as JSON response
   * @throws Exception
   */
  @GET
  @Path( "/authentication-provider" )
  @Produces( { MediaType.APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public Response getAuthenticationProvider() throws Exception {
    try {
      IConfiguration config = this.systemConfig.getConfiguration( "security" );
      String provider = config.getProperties().getProperty( "provider" );
      return Response.ok( new AuthenticationProvider( provider ) ).type( MediaType.APPLICATION_JSON ).build();
    } catch ( Throwable t ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), t ); //$NON-NLS-1$
      throw new Exception( t );
    }
  }

  /**
   * Returns a list of TimeZones ensuring that the server (default) timezone is at the top of the list (0th element)
   * 
   * @return a list of TimeZones ensuring that the server (default) timezone is at the top of the list (0th element)
   */
  @GET
  @Path( "/timezones" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  @Facet ( name = "Unsupported" )
  public TimeZoneWrapper getTimeZones() {
    Map<String, String> timeZones = new HashMap<>();
    for ( String tzId : TimeZone.getAvailableIDs() ) {
      if ( !tzId.toLowerCase().contains( "gmt" ) ) {
        timeZones.put( tzId, formatTimezoneIdToDisplayName( tzId ) );
      }
    }
    List<TimeZoneEntry> entries = new ArrayList<>();
    timeZones.keySet().stream().forEach( tzId -> {
      entries.add( new TimeZoneEntry( tzId, timeZones.get( tzId ) ) );
    } );

    return new TimeZoneWrapper( new TimeZoneWrapper.TimeZonesEntries( entries ), TimeZone.getDefault().getID() );
  }

  private String formatTimezoneIdToDisplayName( String tzId ) {
    int offset = TimeZone.getTimeZone( tzId ).getOffset( System.currentTimeMillis() );
    String sign = offset >= 0 ? "+" : "-";
    offset = Math.abs( offset );
    String text = String.format( "%s%02d%02d", sign, offset / 3600000, ( offset / 60000 ) % 60 );
    return tzId + " - " + TimeZone.getTimeZone( tzId ).getDisplayName( true, TimeZone.LONG ) + " (UTC" + text + ")";
  }

  /**
   * Return the server side locale
   *
   * @return server's locale
   */
  @GET
  @Path( "/locale" )
  @Facet ( name = "Unsupported" )
  public Response getLocale() {
    return Response.ok( LocaleHelper.getLocale().toString() ).build();
  }

  /**
   * Apply the selected locale to the user console
   *
   * @param locale (user console's locale)
   *
   * @return
   */
  @POST
  @Path( "/locale" )
  @Facet ( name = "Unsupported" )
  public Response setLocaleOverride( String locale ) {
    Locale sessionLocale = null;

    if ( !StringUtils.isEmpty( locale ) ) {
      // Clean up "en-US" and "en/GB".
      String localeNormalized = locale.replaceAll( "[-/]", "_" );
      try {
        sessionLocale = LocaleUtils.toLocale( localeNormalized );
      } catch ( IllegalArgumentException ex ) {
        return Response.serverError().entity( ex.getMessage() ).build();
      }
    }

    IUserSettingService settingsService =  PentahoSystem.get( IUserSettingService.class, PentahoSessionHolder.getSession() );
    settingsService.setUserSetting( LocaleHelper.USER_LOCALE_SETTING, locale );
    logger.debug( "Defining language to : " + locale  );

    LocaleHelper.setSessionLocaleOverride( sessionLocale );
    LocaleHelper.setThreadLocaleOverride( sessionLocale );

    return getLocale();
  }

  /**
   * Retrieves the list of supported content type in the platform
   *
   * @return list of <code> ExecutableFileTypeDto </code>
   */
  @Path( "/executableTypes" )
  @GET
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public ExecutableFileTypeDtoWrapper getExecutableTypes() {
    ArrayList<ExecutableFileTypeDto> executableTypes = new ArrayList<ExecutableFileTypeDto>();
    for ( String contentType : pluginManager.getContentTypes() ) {
      IContentInfo contentInfo = pluginManager.getContentTypeInfo( contentType );
      ExecutableFileTypeDto executableFileType = new ExecutableFileTypeDto();
      executableFileType.setDescription( contentInfo.getDescription() );
      executableFileType.setExtension( contentInfo.getExtension() );
      executableFileType.setTitle( contentInfo.getTitle() );
      executableFileType.setCanSchedule( hasOperationId( contentInfo.getOperations(), "SCHEDULE_NEW" ) );
      executableFileType.setCanEdit( hasOperationId( contentInfo.getOperations(), "EDIT" ) );
      executableTypes.add( executableFileType );
    }

    final GenericEntity<List<ExecutableFileTypeDto>> entity =
      new GenericEntity<List<ExecutableFileTypeDto>>( executableTypes ) {
      };
    return new ExecutableFileTypeDtoWrapper( executableTypes );
  }

  private boolean hasOperationId( final List<IPluginOperation> operations, final String operationId ) {
    if ( operations != null && StringUtils.isNotBlank( operationId ) ) {
      for ( IPluginOperation operation : operations ) {
        if ( operation != null && StringUtils.isNotBlank( operation.getId() ) ) {
          if ( operation.getId().equals( operationId ) && StringUtils.isNotBlank( operation.getPerspective() ) ) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
        && ( policy.isAllowed( AdministerSecurityAction.NAME ) );
  }
}
