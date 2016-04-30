/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.enunciate.Facet;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.services.FileService;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path ( "/repo/files/import" )
public class RepositoryImportResource {

  private static final Logger LOGGER = Logger.getLogger( RepositoryImportResource.class );

  private static final String DEFAULT_CHAR_SET = "UTF-8";

  /**
   * Attempts to import all files from the zip archive or single file. A log file is produced at the end of import.
   *
   * <p><b>Example Request:</b><br />
   *    POST pentaho/api/repo/files/import
   *    <br /><b>POST data:</b>
   *    <pre function="syntax.xml">
   *      ------WebKitFormBoundaryB9hzsGp4wR5SGAZD
   *      Content-Disposition: form-data; name="importDir"
   *
   *      /public
   *      ------WebKitFormBoundaryB9hzsGp4wR5SGAZD
   *      Content-Disposition: form-data; name="fileUpload"; filename="test.csv"
   *      Content-Type: application/vnd.ms-excel
   *
   *      ------WebKitFormBoundaryB9hzsGp4wR5SGAZD
   *      Content-Disposition: form-data; name="overwriteFile"
   *
   *      true
   *      ------WebKitFormBoundaryB9hzsGp4wR5SGAZD
   *      Content-Disposition: form-data; name="overwriteAclPermissions"
   *
   *      true
   *      ------WebKitFormBoundaryB9hzsGp4wR5SGAZD
   *      Content-Disposition: form-data; name="applyAclPermissions"
   *
   *      true
   *      ------WebKitFormBoundaryB9hzsGp4wR5SGAZD
   *      Content-Disposition: form-data; name="retainOwnership"
   *
   *      true
   *      ------WebKitFormBoundaryB9hzsGp4wR5SGAZD
   *      Content-Disposition: form-data; name="charSet"
   *
   *      UTF-8
   *      ------WebKitFormBoundaryB9hzsGp4wR5SGAZD
   *      Content-Disposition: form-data; name="logLevel"
   *
   *      INFO
   *      ------WebKitFormBoundaryd1z6iZhXyx12RYxV
   *      Content-Disposition: form-data; name="fileNameOverride"
   *
   *      fileNameOverriden.csv
   *      ------WebKitFormBoundaryd1z6iZhXyx12RYxV--
   *    </pre>
   * </p>
   *
   * @param importDir               JCR Directory to which the zip structure or single file will be uploaded to.
   * @param fileUpload              Input stream for the file.
   * @param overwriteFile           The flag indicates ability to overwrite existing file.
   * @param overwriteAclPermissions The flag indicates ability to overwrite Acl permissions.
   * @param applyAclPermissions     The flag indicates ability to apply Acl permissions.
   * @param retainOwnership         The flag indicates ability to retain ownership.
   * @param charSet                 The charset for imported file.
   * @param logLevel                The level of logging.
   * @param fileNameOverride        If present and the content represents a single file, this parameter contains the filename to use
   *                                when storing the file in the repository. If not present, the fileInfo.getFileName will be used.
   *                                Note that the later cannot reliably handle foreign character sets.
   *
   * @return A jax-rs Response object with the appropriate header and body.
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *   &lt;html&gt;
   *   &lt;head&gt;
   *   &lt;title&gt;Repository Import Log&lt;/title&gt;
   *   &lt;/head&gt;
   *   &lt;body bgcolor="#FFFFFF" topmargin="6" leftmargin="6" style="font-family: arial,sans-serif; font-size: x-small"&gt;
   *   &lt;hr size="1" noshade&gt;
   *   Log session start time Thu Feb 26 11:04:19 BRT 2015&lt;br&gt;
   *   &lt;br&gt;
   *   &lt;table cellspacing="0" cellpadding="4" border="1" bordercolor="#224466" width="100%"&gt;
   *   &lt;tr style="background: #336699; color: #FFFFFF; text-align: left"&gt;
   *   &lt;th&gt;Import File&lt;/th&gt;
   *   &lt;th&gt;Level&lt;/th&gt;
   *   &lt;th&gt;Message&lt;/th&gt;
   *   &lt;/tr&gt;
   *   &lt;td title="importFile"&gt;/public&lt;/td&gt;
   *   &lt;td title="Level"&gt;INFO&lt;/td&gt;
   *   &lt;td title="Message"&gt;Start Import Job&lt;/td&gt;
   *   &lt;/tr&gt;
   *   &lt;td title="importFile"&gt;/public/fileNameOverriden.csv&lt;/td&gt;
   *   &lt;td title="Level"&gt;INFO&lt;/td&gt;
   *   &lt;td title="Message"&gt;Start File Import&lt;/td&gt;
   *   &lt;/tr&gt;
   *   &lt;td title="importFile"&gt;/public/fileNameOverriden.csv&lt;/td&gt;
   *   &lt;td title="Level"&gt;&lt;font color="#993300"&gt;&lt;strong&gt;WARN&lt;/strong&gt;&lt;/font&gt;&lt;/td&gt;
   *   &lt;td title="Message"&gt;fileNameOverriden.csv&lt;/td&gt;
   *   &lt;/tr&gt;
   *   &lt;td title="importFile"&gt;/public&lt;/td&gt;
   *   &lt;td title="Level"&gt;INFO&lt;/td&gt;
   *   &lt;td title="Message"&gt;End Import Job&lt;/td&gt;
   *   &lt;/tr&gt;
   *   &lt;/table&gt;
   *   &lt;br&gt;
   *   &lt;/body&gt;&lt;/html&gt;
   * </pre>
   */
  @POST
  @Consumes ( MediaType.MULTIPART_FORM_DATA )
  @Produces ( MediaType.TEXT_HTML )
  @Facet( name = "Unsupported" )
  public Response doPostImport( @FormDataParam ( "importDir" ) String importDir,
                                @FormDataParam ( "fileUpload" ) InputStream fileUpload,
                                @FormDataParam ( "overwriteFile" ) String overwriteFile,
                                @FormDataParam ( "overwriteAclPermissions" ) String overwriteAclPermissions,
                                @FormDataParam ( "applyAclPermissions" ) String applyAclPermission,
                                @FormDataParam ( "retainOwnership" ) String retainOwnership,
                                @FormDataParam ( "charSet" ) String charSet,
                                @FormDataParam ( "logLevel" ) String logLevel,
                                @FormDataParam ( "fileUpload" ) FormDataContentDisposition fileInfo,
                                @FormDataParam ( "fileNameOverride" ) String fileNameOverride ) {
    IRepositoryImportLogger importLogger = null;
    ByteArrayOutputStream importLoggerStream = new ByteArrayOutputStream();
    boolean logJobStarted = false;

    if ( StringUtils.isBlank( charSet ) ) {
      charSet =  DEFAULT_CHAR_SET;
    }

    try {
      validateAccess( importDir );

      boolean overwriteFileFlag = ( "false".equals( overwriteFile ) ? false : true );
      boolean overwriteAclSettingsFlag = ( "true".equals( overwriteAclPermissions ) ? true : false );
      boolean applyAclSettingsFlag = ( "true".equals( applyAclPermission ) ? true : false );
      boolean retainOwnershipFlag = ( "true".equals( retainOwnership ) ? true : false );
      // If logLevel is null then we will default to ERROR
      if ( logLevel == null || logLevel.length() <= 0 ) {
        logLevel = "ERROR";
      }

      //Non-admins cannot process a manifest
      FileService fileService = new FileService();
      if ( !fileService.doCanAdminister() ) {
        applyAclSettingsFlag = false;
        retainOwnershipFlag = true;
      }

      Level level = Level.toLevel( logLevel );
      ImportSession.getSession().setAclProperties( applyAclSettingsFlag,
          retainOwnershipFlag, overwriteAclSettingsFlag );

      //The fileNameOverride was added because the formDataContentDispositionfile object cannot reliable
      //contain non US-ASCII characters.  See RFC283 section 2.3 for details
      String fileName = fileNameOverride != null ? fileNameOverride : fileInfo.getFileName();

      RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
      bundleBuilder.input( fileUpload );
      bundleBuilder.charSet( charSet );
      bundleBuilder.hidden( false );
      bundleBuilder.path( importDir );
      bundleBuilder.overwriteFile( overwriteFileFlag );
      bundleBuilder.applyAclSettings( applyAclSettingsFlag );
      bundleBuilder.overwriteAclSettings( overwriteAclSettingsFlag );
      bundleBuilder.retainOwnership( retainOwnershipFlag );
      bundleBuilder.name( fileName );
      IPlatformImportBundle bundle = bundleBuilder.build();

      IPlatformMimeResolver mimeResolver = PentahoSystem.get( IPlatformMimeResolver.class );
      String mimeTypeFromFile = mimeResolver.resolveMimeForFileName( fileName );
      if ( mimeTypeFromFile == null ) {
        return Response.ok( "INVALID_MIME_TYPE", MediaType.TEXT_HTML ).build();
      }
      bundleBuilder.mime( mimeTypeFromFile );

      IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );
      importLogger = importer.getRepositoryImportLogger();

      final String mimeType =
          bundle.getMimeType() != null ? bundle.getMimeType() : mimeResolver.resolveMimeForBundle( bundle );
      if ( mimeType == null ) {
        return Response.ok( "INVALID_MIME_TYPE", MediaType.TEXT_HTML ).build();
      }

      logJobStarted = true;
      importLogger.startJob( importLoggerStream, importDir, level );
      importer.importFile( bundle );

      // Flush the Mondrian cache to show imported data-sources.
      IMondrianCatalogService mondrianCatalogService =
          PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService", PentahoSessionHolder
              .getSession() );
      mondrianCatalogService.reInit( PentahoSessionHolder.getSession() );
    } catch ( PentahoAccessControlException e ) {
      return Response.serverError().entity( e.toString() ).build();
    } catch ( Exception e ) {
      return Response.serverError().entity( e.toString() ).build();
    } finally {
      if ( logJobStarted == true ) {
        importLogger.endJob();
      }
    }
    String responseBody;
    try {
      responseBody = importLoggerStream.toString( charSet );
    } catch ( UnsupportedEncodingException e ) {
      LOGGER.error( "Encoding of response body is failed. (charSet=" + charSet + ")", e );
      responseBody = importLoggerStream.toString();
    }
    return Response.ok( responseBody, MediaType.TEXT_HTML ).build();
  }

  protected void validateAccess( String importDir ) throws PentahoAccessControlException {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    //check if we are admin or have publish permisson
    boolean isAdmin = policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
                    && ( policy.isAllowed( AdministerSecurityAction.NAME ) || policy.isAllowed( PublishAction.NAME ) );
    if ( !isAdmin ) {
      //the user does not have admin or publish permisson, so we will check if the user imports to their home folder
      boolean importingToHomeFolder = false;
      String tenatedUserName = PentahoSessionHolder.getSession().getName();
      //get user home home folder path
      String userHomeFolderPath = ServerRepositoryPaths.getUserHomeFolderPath(
          JcrTenantUtils.getUserNameUtils().getTenant( tenatedUserName ),
            JcrTenantUtils.getUserNameUtils().getPrincipleName( tenatedUserName ) );
      if ( userHomeFolderPath != null && userHomeFolderPath.length() > 0 ) {
        //we pass the relative path so add serverside root folder for every home folder
        importingToHomeFolder = ( ServerRepositoryPaths.getTenantRootFolderPath() + importDir )
            .contains( userHomeFolderPath );
      }
      if ( !( importingToHomeFolder && policy.isAllowed( RepositoryCreateAction.NAME )
          && policy.isAllowed( RepositoryReadAction.NAME ) ) ) {
        throw new PentahoAccessControlException( "User is not authorized to perform this operation" );
      }
    }
  }
}
