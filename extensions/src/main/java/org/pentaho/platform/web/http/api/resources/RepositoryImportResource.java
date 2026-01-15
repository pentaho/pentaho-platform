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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.enunciate.Facet;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.web.http.api.resources.services.FileService;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Path( "/repo/files/import" )
public class RepositoryImportResource {

  private static final Logger LOGGER = LogManager.getLogger( RepositoryImportResource.class );

  private static final String DEFAULT_CHAR_SET = "UTF-8";

  /**
   * Attempts to import all files from the zip archive or single file. A log file is produced at the end of import.
   *
   * <p><b>Example Request:</b><br />
   * POST pentaho/api/repo/files/import
   * <br /><b>POST data:</b>
   * <pre function="syntax.xml">
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
   * @param applyAclPermission      The flag indicates ability to apply ACL permissions.
   * @param retainOwnership         The flag indicates ability to retain ownership.
   * @param charSet                 The charset for imported file.
   * @param logLevel                The level of logging.
   * @param fileNameOverride        If present and the content represents a single file, this parameter contains the filename to use
   *                                when storing the file in the repository. If not present, the fileInfo.getFileName will be used.
   *                                Note that the later cannot reliably handle foreign character sets.
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
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( MediaType.TEXT_HTML )
  @Facet( name = "Unsupported" )
  public Response doPostImport( @FormDataParam( "importDir" ) String importDir,
                                @FormDataParam( "fileUpload" ) InputStream fileUpload,
                                @FormDataParam( "overwriteFile" ) String overwriteFile,
                                @FormDataParam( "overwriteAclPermissions" ) String overwriteAclPermissions,
                                @FormDataParam( "applyAclPermissions" ) String applyAclPermission,
                                @FormDataParam( "retainOwnership" ) String retainOwnership,
                                @FormDataParam( "charSet" ) String charSet,
                                @FormDataParam( "logLevel" ) String logLevel,
                                @FormDataParam( "fileUpload" ) FormDataContentDisposition fileInfo,
                                @FormDataParam( "fileNameOverride" ) String fileNameOverride ) {
    return doPostImportCommon( importDir, Arrays.asList( fileUpload ), overwriteFile, overwriteAclPermissions, applyAclPermission,
        retainOwnership, charSet, logLevel, fileInfo, Arrays.asList( fileNameOverride ) );
  }

  protected void validateImportAccess( String importDir ) throws PentahoAccessControlException {
    // upload directory might be created or its permissions changed so skip existence and write permission checks
    boolean canUpload = SystemUtils.canUpload( importDir, true );
    if ( !canUpload ) {
      throw new PentahoAccessControlException( "User is not authorized to perform this operation" );
    }
  }

  /**
   * Attempts to import all files from the zip archive or multiple files. A log file is produced at the end of import.
   * this has been written as the above api was called in multiple places like reporting module...
   *
   * @param importDir
   * @param fileParts
   * @param overwriteFile
   * @param overwriteAclPermissions
   * @param applyAclPermission
   * @param retainOwnership
   * @param charSet
   * @param logLevel
   * @param fileInfo
   * @param fileNameOverride
   * @return
   */
  @POST()
  @Path( "/multiple" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( MediaType.TEXT_HTML )
  @Facet( name = "Unsupported" )
  public Response doPostImport( @FormDataParam( "importDir" ) String importDir,
                                @FormDataParam( "fileUpload" ) List<FormDataBodyPart> fileParts,
                                @FormDataParam( "overwriteFile" ) String overwriteFile,
                                @FormDataParam( "overwriteAclPermissions" ) String overwriteAclPermissions,
                                @FormDataParam( "applyAclPermissions" ) String applyAclPermission,
                                @FormDataParam( "retainOwnership" ) String retainOwnership,
                                @FormDataParam( "charSet" ) String charSet,
                                @FormDataParam( "logLevel" ) String logLevel,
                                @FormDataParam( "fileUpload" ) FormDataContentDisposition fileInfo,
                                @FormDataParam( "fileNameOverride" ) List<String> fileNameOverrides ) {
    List<InputStream> fileUploads = fileParts.stream()
        .map( part -> part.getValueAs( InputStream.class ) )
        .collect( Collectors.toList() );
    return doPostImportCommon( importDir, fileUploads, overwriteFile, overwriteAclPermissions, applyAclPermission,
        retainOwnership, charSet, logLevel, fileInfo, fileNameOverrides );
  }

  /**
   * common code extracted to this new method
   *
   * @param importDir
   * @param fileUploads
   * @param overwriteFile
   * @param overwriteAclPermissions
   * @param applyAclPermission
   * @param retainOwnership
   * @param charSet
   * @param logLevel
   * @param fileInfo
   * @param fileNameOverride
   * @return
   */
  private Response doPostImportCommon( String importDir, List<InputStream> fileUploads, String overwriteFile,
                                       String overwriteAclPermissions, String applyAclPermission, String retainOwnership,
                                       String charSet, String logLevel, FormDataContentDisposition fileInfo,
                                       List<String> fileNameOverrides ) {
    IRepositoryImportLogger importLogger = null;
    ByteArrayOutputStream importLoggerStream = new ByteArrayOutputStream();
    boolean logJobStarted = false;

    if ( StringUtils.isBlank( charSet ) ) {
      charSet = DEFAULT_CHAR_SET;
    }

    try {
      validateImportAccess( importDir );

      boolean overwriteFileFlag = ( "false".equals( overwriteFile ) ? false : true );
      boolean overwriteAclSettingsFlag = ( "true".equals( overwriteAclPermissions ) ? true : false );
      boolean applyAclSettingsFlag = ( "true".equals( applyAclPermission ) ? true : false );
      boolean retainOwnershipFlag = ( "true".equals( retainOwnership ) ? true : false );
      // If logLevel is null then we will default to ERROR
      if ( logLevel == null || logLevel.length() <= 0 ) {
        logLevel = "ERROR";
      }

      // Non-admins cannot process a manifest
      FileService fileService = new FileService();
      if ( !fileService.doCanAdminister() ) {
        applyAclSettingsFlag = false;
        retainOwnershipFlag = true;
      }

      Level level = Level.toLevel( logLevel );
      ImportSession.getSession().setAclProperties( applyAclSettingsFlag, retainOwnershipFlag, overwriteAclSettingsFlag );

      IPlatformMimeResolver mimeResolver = PentahoSystem.get( IPlatformMimeResolver.class );
      IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );
      importLogger = importer.getRepositoryImportLogger();
      importLogger.startJob( importLoggerStream, importDir, level );

      for ( int i = 0; i < fileNameOverrides.size(); i++ ) {
        InputStream fileUpload = fileUploads.get( i );

        String fileName = fileNameOverrides.get( i );

        RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
        bundleBuilder.input( fileUpload );
        bundleBuilder.charSet( charSet );
        bundleBuilder.path( importDir );
        bundleBuilder.overwriteFile( overwriteFileFlag );
        bundleBuilder.applyAclSettings( applyAclSettingsFlag );
        bundleBuilder.overwriteAclSettings( overwriteAclSettingsFlag );
        bundleBuilder.retainOwnership( retainOwnershipFlag );
        bundleBuilder.name( fileName );

        IPlatformImportBundle bundle = bundleBuilder.build();
        String mimeTypeFromFile = mimeResolver.resolveMimeForFileName( fileName );

        if ( mimeTypeFromFile == null ) {
          return Response.ok( "INVALID_MIME_TYPE", MediaType.TEXT_HTML ).build();
        }

        bundleBuilder.mime( mimeTypeFromFile );
        importer.getRepositoryImportLogger().setPerformingRestore( false );
        try {
          importer.importFile( bundle );
        } catch ( PlatformImportException ex ) {
          if ( ex.getErrorStatus() == PlatformImportException.PUBLISH_PARTIAL_UPLOAD ) {
            return Response.ok( "INVALID_MIME_TYPE_PARTIAL", MediaType.TEXT_HTML ).build();
          } else {
            throw ex;
          }
        }
      }

      // Flush the Mondrian cache to show imported data-sources.
      IMondrianCatalogService mondrianCatalogService = PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService",
          PentahoSessionHolder.getSession() );
      mondrianCatalogService.reInit( PentahoSessionHolder.getSession() );
      logJobStarted = true;
    } catch ( Exception e ) {
      return Response.serverError().entity( e.toString() ).build();
    } finally {
      ImportSession.clearSession();
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
}
