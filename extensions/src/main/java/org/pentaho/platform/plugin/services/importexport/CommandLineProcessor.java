/*!
 *
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
 *
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport;

import com.google.common.annotations.VisibleForTesting;
import com.hitachivantara.security.web.impl.client.csrf.jaxrsv1.CsrfTokenFilter;
import com.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.SessionCookiesFilter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.util.RepositoryPathEncoder;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handles the parsing of command line arguments and creates an import process based upon them
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class CommandLineProcessor {

  private static final String API_CSRF_TOKEN = "/api/csrf/token";
  private static final String API_REPO_FILES_IMPORT = "/api/repo/files/import";
  private static final String API_MONDRIAN_POST_ANALYSIS = "/plugin/data-access/api/mondrian/postAnalysis";
  private static final String API_METADATA_POST_IMPORT = "/plugin/data-access/api/metadata/postimport";
  private static final String API_REPO_FILES = "/api/repo/files/";
  private static final String API_REPO_FILES_BACKUP = "/api/repo/files/backup";
  private static final String API_AUTHORIZATION_ACTION_IS_AUTHORIZED = "/api/authorization/action/isauthorized";
  private static final String API_REPO_FILES_SYSTEM_RESTORE = "/api/repo/files/systemRestore";

  private static final String MULTIPART_FIELD_OVERWRITE = "overwrite";
  private static final String MULTIPART_FIELD_DOMAIN_ID = "domainId";
  private static final String MULTIPART_FIELD_METADATA_FILE = "metadataFile";
  private static final String MULTIPART_FIELD_CATALOG_NAME = "catalogName";
  private static final String MULTIPART_FIELD_DATASOURCE_NAME = "datasourceName";
  private static final String MULTIPART_FIELD_PARAMETERS = "parameters";
  private static final String MULTIPART_FIELD_XMLA_ENABLED_FLAG = "xmlaEnabledFlag";
  private static final String MULTIPART_FIELD_UPLOAD_ANALYSIS = "uploadAnalysis";
  private static final String MULTIPART_FIELD_IMPORT_DIR = "importDir";
  private static final String MULTIPART_FIELD_OVERWRITE_ACL_PERMISSIONS = "overwriteAclPermissions";
  private static final String MULTIPART_FIELD_RETAIN_OWNERSHIP = "retainOwnership";
  private static final String MULTIPART_FIELD_CHAR_SET = "charSet";
  private static final String MULTIPART_FIELD_APPLY_ACL_PERMISSIONS = "applyAclPermissions";
  private static final String MULTIPART_FIELD_FILE_UPLOAD = "fileUpload";
  private static final String MULTIPART_FIELD_FILE_NAME_OVERRIDE = "fileNameOverride";
  private static final String MULTIPART_FIELD_OVERWRITE_FILE = "overwriteFile";
  private static final String MULTIPART_FIELD_APPLY_ACL_SETTINGS = "applyAclSettings";
  private static final String MULTIPART_FIELD_OVERWRITE_ACL_SETTINGS = "overwriteAclSettings";

  private static final String METADATA_DATASOURCE_EXT = "xmi";

  private static final String ZIP_EXT = "zip";

  private static final Log log = LogFactory.getLog( CommandLineProcessor.class );

  private static final Options options = new Options();

  private static Exception exception;

  private static String errorMessage;

  private final CommandLine commandLine;

  private final RequestType requestType;

  private static final String INFO_OPTION_HELP_KEY = "h";
  private static final String INFO_OPTION_HELP_NAME = "help";
  private static final String INFO_OPTION_IMPORT_KEY = "i";
  private static final String INFO_OPTION_IMPORT_NAME = "import";
  private static final String INFO_OPTION_EXPORT_KEY = "e";
  private static final String INFO_OPTION_EXPORT_NAME = "export";
  private static final String INFO_OPTION_BACKUP_KEY = "backup";
  private static final String INFO_OPTION_BACKUP_NAME = "backup";
  private static final String INFO_OPTION_RESTORE_KEY = "restore";
  private static final String INFO_OPTION_RESTORE_NAME = "restore";
  private static final String INFO_OPTION_USERNAME_KEY = "u";
  private static final String INFO_OPTION_USERNAME_NAME = "username";
  private static final String INFO_OPTION_PASSWORD_KEY = "p";
  private static final String INFO_OPTION_PASSWORD_NAME = "password";
  private static final String INFO_OPTION_URL_KEY = "a";
  private static final String INFO_OPTION_URL_NAME = "url";
  private static final String INFO_OPTION_FILEPATH_KEY = "fp";
  private static final String INFO_OPTION_FILEPATH_NAME = "file-path";
  private static final String INFO_OPTION_CHARSET_KEY = "c";
  private static final String INFO_OPTION_CHARSET_NAME = "charset";
  private static final String INFO_OPTION_LOGFILE_KEY = "l";
  private static final String INFO_OPTION_LOGFILE_NAME = "logfile";
  private static final String INFO_OPTION_PATH_KEY = "f";
  private static final String INFO_OPTION_PATH_NAME = "path";
  private static final String INFO_OPTION_OVERWRITE_KEY = "o";
  private static final String INFO_OPTION_OVERWRITE_NAME = "overwrite";
  private static final String INFO_OPTION_PERMISSION_KEY = "m";
  private static final String INFO_OPTION_PERMISSION_NAME = "permission";
  private static final String INFO_OPTION_RETAIN_OWNERSHIP_KEY = "r";
  private static final String INFO_OPTION_RETAIN_OWNERSHIP_NAME = "retainOwnership";
  private static final String INFO_OPTION_WITH_MANIFEST_KEY = "w";
  private static final String INFO_OPTION_WITH_MANIFEST_NAME = "withManifest";
  private static final String INFO_OPTION_REST_KEY = "rest";
  private static final String INFO_OPTION_REST_NAME = "rest";
  private static final String INFO_OPTION_SERVICE_KEY = "v";
  private static final String INFO_OPTION_SERVICE_NAME = "service";
  private static final String INFO_OPTION_PARAMS_KEY = "params";
  private static final String INFO_OPTION_PARAMS_NAME = "params";
  private static final String INFO_OPTION_RESOURCE_TYPE_KEY = "res";
  private static final String INFO_OPTION_RESOURCE_TYPE_NAME = "resource-type";
  private static final String INFO_OPTION_DATASOURCE_TYPE_KEY = "ds";
  private static final String INFO_OPTION_DATASOURCE_TYPE_NAME = "datasource-type";
  private static final String INFO_OPTION_ANALYSIS_CATALOG_KEY = "cat";
  private static final String INFO_OPTION_ANALYSIS_CATALOG_NAME = "catalog";
  private static final String INFO_OPTION_ANALYSIS_DATASOURCE_KEY = "a_ds";
  private static final String INFO_OPTION_ANALYSIS_DATASOURCE_NAME = "analysis-datasource";
  private static final String INFO_OPTION_ANALYSIS_XMLA_ENABLED_KEY = "a_xmla";
  private static final String INFO_OPTION_ANALYSIS_XMLA_ENABLED_NAME = "xmla-enabled";
  private static final String INFO_OPTION_METADATA_DOMAIN_ID_KEY = "m_id";
  private static final String INFO_OPTION_METADATA_DOMAIN_ID_NAME = "metadata-domain-id";
  private static final String INFO_OPTION_APPLY_ACL_SETTINGS_KEY = "a_acl";
  private static final String INFO_OPTION_APPLY_ACL_SETTINGS_NAME = "applyAclSettings";
  private static final String INFO_OPTION_OVERWRITE_ACL_SETTINGS_KEY = "o_acl";
  private static final String INFO_OPTION_OVERWRITE_ACL_SETTINGS_NAME = "overwriteAclSettings";

  public enum RequestType {
    HELP, IMPORT, EXPORT, REST, BACKUP, RESTORE
  }

  public enum DatasourceType {
    JDBC, METADATA, ANALYSIS
  }

  public enum ResourceType {
    SOLUTIONS, DATASOURCE
  }

  private static Client client = null;
  private static final ClientConfig clientConfig;


  static {
    // For REST Jersey calls
    clientConfig = new DefaultClientConfig();
    clientConfig.getFeatures().put( JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE );

    // create the Options
    options.addOption( INFO_OPTION_HELP_KEY, INFO_OPTION_HELP_NAME, false, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_HELP_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_IMPORT_KEY, INFO_OPTION_IMPORT_NAME, false, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_IMPORT_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_EXPORT_KEY, INFO_OPTION_EXPORT_NAME, false, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_EXPORT_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_USERNAME_KEY, INFO_OPTION_USERNAME_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_USERNAME_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_PASSWORD_KEY, INFO_OPTION_PASSWORD_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_PASSWORD_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_URL_KEY, INFO_OPTION_URL_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_URL_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_FILEPATH_KEY, INFO_OPTION_FILEPATH_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_CHARSET_KEY, INFO_OPTION_CHARSET_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_CHARSET_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_LOGFILE_KEY, INFO_OPTION_LOGFILE_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_LOGFILE_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_PATH_KEY, INFO_OPTION_PATH_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_PATH_DESCRIPTION" ) );

    // import only ACL additions
    options.addOption( INFO_OPTION_OVERWRITE_KEY, INFO_OPTION_OVERWRITE_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_OVERWRITE_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_PERMISSION_KEY, INFO_OPTION_PERMISSION_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_PERMISSION_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_RETAIN_OWNERSHIP_KEY, INFO_OPTION_RETAIN_OWNERSHIP_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_RETAIN_OWNERSHIP_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_WITH_MANIFEST_KEY, INFO_OPTION_WITH_MANIFEST_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_WITH_MANIFEST_DESCRIPTION" ) );

    // rest services
    options.addOption( INFO_OPTION_REST_KEY, INFO_OPTION_REST_NAME, false, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_REST_DESCRIPTION" ) );

    // backup
    options.addOption( INFO_OPTION_BACKUP_KEY, INFO_OPTION_BACKUP_NAME, false, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_BACKUP_DESCRIPTION" ) );

    // restore
    options.addOption( INFO_OPTION_RESTORE_KEY, INFO_OPTION_RESTORE_NAME, false, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_RESTORE_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_SERVICE_KEY, INFO_OPTION_SERVICE_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_SERVICE_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_PARAMS_KEY, INFO_OPTION_PARAMS_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_PARAMS_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_RESOURCE_TYPE_KEY, INFO_OPTION_RESOURCE_TYPE_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_RESOURCE_TYPE_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_DATASOURCE_TYPE_KEY, INFO_OPTION_DATASOURCE_TYPE_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_TYPE_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_ANALYSIS_CATALOG_KEY, INFO_OPTION_ANALYSIS_CATALOG_NAME, true, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_CATALOG_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_ANALYSIS_DATASOURCE_KEY, INFO_OPTION_ANALYSIS_DATASOURCE_NAME, true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_DATASOURCE_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_ANALYSIS_XMLA_ENABLED_KEY, INFO_OPTION_ANALYSIS_XMLA_ENABLED_NAME, true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_XMLA_ENABLED_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_METADATA_DOMAIN_ID_KEY, INFO_OPTION_METADATA_DOMAIN_ID_NAME, true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_METADATA_DOMAIN_ID_DESCRIPTION" ) );

    options.addOption( INFO_OPTION_APPLY_ACL_SETTINGS_KEY, INFO_OPTION_APPLY_ACL_SETTINGS_NAME, true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_APPLY_ACL_SETTINGS" ) );

    options.addOption( INFO_OPTION_OVERWRITE_ACL_SETTINGS_KEY, INFO_OPTION_OVERWRITE_ACL_SETTINGS_NAME, true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_OVERWRITE_ACL_SETTINGS" ) );
  }

  /**
   * How this class is executed from the command line.
   *
   * @param args
   */
  public static void main( String[] args ) throws Exception {
    try {
      CommandLineProcessor commandLineProcessor = new CommandLineProcessor( args );

      // reset the exception information
      exception = null;

      // new service only
      switch ( commandLineProcessor.getRequestType() ) {
        case HELP:
          printHelp();
          break;

        case IMPORT:
          commandLineProcessor.performImport();
          break;

        case EXPORT:
          commandLineProcessor.performExport();
          break;

        case REST:
          commandLineProcessor.performREST();
          break;

        case BACKUP:
          commandLineProcessor.performBackup();
          break;

        case RESTORE:
          commandLineProcessor.performRestore();
          break;
      }
    } catch ( ParseException parseException ) {
      exception = parseException;
      System.out.println( parseException.getLocalizedMessage() );
      printHelp();
      log.error( parseException.getMessage(), parseException );
    } catch ( Exception e ) {
      exception = e;
      e.printStackTrace();
      log.error( e.getMessage(), e );
    }
  }

  /**
   * call FileResource REST service example: {path+}/children example: {path+}/parameterizable example:
   * {path+}/properties example: /delete?params={fileid1, fileid2}
   */
  private void performREST() throws ParseException, KettleException, URISyntaxException {

    String contextURL = getOptionValue( INFO_OPTION_URL_NAME, true, false );
    String path = getOptionValue( INFO_OPTION_PATH_NAME, true, false );
    String logFile = getOptionValue( INFO_OPTION_LOGFILE_NAME, false, true );
    String exportURL = contextURL + API_REPO_FILES;
    if ( path != null ) {
      String effPath = RepositoryPathEncoder.encodeRepositoryPath( path );
      exportURL += effPath;
    }
    String service = getOptionValue( INFO_OPTION_SERVICE_NAME, true, false );
    String params = getOptionValue( INFO_OPTION_PARAMS_NAME, false, true );
    exportURL += "/" + service;
    if ( params != null ) {
      exportURL += "?params=" + params;
    }

    initRestService( contextURL );
    WebResource resource = client.resource( exportURL );

    // Response response
    Builder builder = resource.type( MediaType.APPLICATION_JSON ).type( MediaType.TEXT_XML_TYPE );
    ClientResponse response = builder.put( ClientResponse.class );
    if ( response != null && response.getStatus() == 200 ) {

      String message = Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_COMPLETED" ).concat( "\n" );
      message +=
        Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_RESPONSE_STATUS", response.getStatus() );
      message += "\n";

      if ( StringUtils.isNotBlank( logFile ) ) {
        message += Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_FILE_WRITTEN", logFile );
        System.out.println( message );
        writeToFile( message, logFile );
      }
    } else {
      System.out.println( Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0002_INVALID_RESPONSE" ) );
    }
  }

  @VisibleForTesting
  String getUsername() throws ParseException {
    return getOptionValue( INFO_OPTION_USERNAME_NAME, true, false );
  }

  @VisibleForTesting
  String getPassword() throws ParseException, KettleException {
    if ( !KettleClientEnvironment.isInitialized() ) {
      KettleClientEnvironment.init();
    }

    return Encr.decryptPasswordOptionallyEncrypted( getOptionValue( INFO_OPTION_PASSWORD_NAME, true, false ) );
  }

  /**
   * Used only for REST Jersey calls
   *
   * @param contextURL The Pentaho server web application base URL.
   */
  private void initRestService( String contextURL ) throws ParseException, KettleException, URISyntaxException {

    client = Client.create( clientConfig );
    client.addFilter( new HTTPBasicAuthFilter( getUsername(), getPassword() ) );
    client.addFilter( new SessionCookiesFilter( new CookieManager() ) );
    client.addFilter( new CsrfTokenFilter( new URI( contextURL + API_CSRF_TOKEN ) ) );
  }

  /**
   * Returns information about any exception encountered (if one was generated)
   *
   * @return the {@link Exception} that was generated, or {@code null} if none was generated
   */
  public static Exception getException() {
    return exception;
  }

  /**
   * Parses the command line and handles the situation where it isn't a valid import or export or rest request
   *
   * @param args
   *          the command line arguments
   * @throws ParseException
   *           indicates that neither (or both) an import and/or export have been request
   */
  protected CommandLineProcessor( String[] args ) throws ParseException {
    // parse the command line arguments
    commandLine = new CmdParser().parse( options, args );
    if ( commandLine.hasOption( INFO_OPTION_HELP_NAME ) || commandLine.hasOption( INFO_OPTION_HELP_KEY ) ) {
      requestType = RequestType.HELP;
    } else {
      if ( commandLine.hasOption( INFO_OPTION_REST_NAME ) || commandLine.hasOption( INFO_OPTION_REST_KEY ) ) {
        requestType = RequestType.REST;
      } else if ( commandLine.hasOption( INFO_OPTION_BACKUP_KEY ) ) {
        requestType = RequestType.BACKUP;
      } else if ( commandLine.hasOption( INFO_OPTION_RESTORE_KEY ) ) {
        requestType = RequestType.RESTORE;
      } else {
        final boolean importRequest =
          commandLine.hasOption( INFO_OPTION_IMPORT_KEY );
        final boolean exportRequest =
          commandLine.hasOption( INFO_OPTION_EXPORT_KEY );

        if ( importRequest == exportRequest ) {
          throw new ParseException( Messages.getInstance().getErrorString(
            "CommandLineProcessor.ERROR_0003_PARSE_EXCEPTION" ) );
        }
        requestType = ( importRequest ? RequestType.IMPORT : RequestType.EXPORT );
      }
    }
  }

  // ========================== Instance Members / Methods
  // ==========================

  protected RequestType getRequestType() {
    return requestType;
  }

  /**
   * --import --url=http://localhost:8080/pentaho --username=admin --password=password --file-path=metadata.xmi
   * --resource-type=DATASOURCE --datasource-type=METADATA --overwrite=true --metadata-domain-id=steel-wheels
   *
   * @param contextURL
   * @param metadataDatasourceFile
   * @param overwrite
   * @throws ParseException
   * @throws IOException
   */
  private void performMetadataDatasourceImport( String contextURL, File metadataDatasourceFile, String overwrite,
                                                String logFile, String path )
    throws ParseException, IOException {
    File metadataFileInZip = null;
    InputStream metadataFileInZipInputStream = null;

    String metadataImportURL = contextURL + API_METADATA_POST_IMPORT;

    String domainId = getOptionValue( INFO_OPTION_METADATA_DOMAIN_ID_NAME, true, false );

    WebResource resource = client.resource( metadataImportURL );

    FormDataMultiPart part = new FormDataMultiPart();

    final String name = RepositoryFilenameUtils.separatorsToRepository( metadataDatasourceFile.getName() );
    final String ext = RepositoryFilenameUtils.getExtension( name );

    try {
      if ( ext.equals( ZIP_EXT ) ) {
        ZipInputStream zipInputStream = new ZipInputStream( new FileInputStream( metadataDatasourceFile ) );
        ZipEntry entry = zipInputStream.getNextEntry();
        while ( entry != null ) {
          final String entryName = RepositoryFilenameUtils.separatorsToRepository( entry.getName() );
          final String extension = RepositoryFilenameUtils.getExtension( entryName );

          boolean isDir = entry.getSize() == 0;
          if ( !isDir ) {
            // TODO Why on Earth are we creating these temporary files if they are set to be deleted on exit and not
            //  being read anywhere? Note that, for what I saw, this code is as it was created in 2013... Could it only
            //  be to make sure the zip is valid?
            File tempFile = File.createTempFile( "zip", null );
            tempFile.deleteOnExit();
            writeToFile( zipInputStream, tempFile );
          }
          if ( extension.equals( METADATA_DATASOURCE_EXT ) ) {
            if ( metadataFileInZip == null ) {
              metadataFileInZip = new File( entryName );
              metadataFileInZipInputStream = new FileInputStream( metadataFileInZip );
            }
          }

          zipInputStream.closeEntry();
          entry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();

        part.field( MULTIPART_FIELD_OVERWRITE, "true".equals( overwrite ) ? "true" : "false",
          MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( MULTIPART_FIELD_DOMAIN_ID, domainId, MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( MULTIPART_FIELD_METADATA_FILE, metadataFileInZipInputStream, MediaType.MULTIPART_FORM_DATA_TYPE );

        // If the import service needs the file name do the following.
        part.getField( MULTIPART_FIELD_METADATA_FILE )
          .setContentDisposition( FormDataContentDisposition.name( MULTIPART_FIELD_METADATA_FILE )
            .fileName( metadataFileInZip.getName() ).build() );

        // Response response
        ClientResponse response = resource.type( MediaType.MULTIPART_FORM_DATA ).post( ClientResponse.class, part );
        if ( response != null ) {
          logResponseMessage( logFile, path, response, RequestType.IMPORT );
          response.close();
        }

      } else {
        FileInputStream metadataDatasourceInputStream = new FileInputStream( metadataDatasourceFile );

        part.field( MULTIPART_FIELD_OVERWRITE, "true".equals( overwrite ) ? "true" : "false",
          MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( MULTIPART_FIELD_DOMAIN_ID, domainId, MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( MULTIPART_FIELD_METADATA_FILE, metadataDatasourceInputStream, MediaType.MULTIPART_FORM_DATA_TYPE );

        // If the import service needs the file name do the following.
        part.getField( MULTIPART_FIELD_METADATA_FILE ).setContentDisposition( FormDataContentDisposition.name( MULTIPART_FIELD_METADATA_FILE )
          .fileName( metadataDatasourceFile.getName() ).build() );

        // Response response
        ClientResponse response = resource.type( MediaType.MULTIPART_FORM_DATA ).post( ClientResponse.class, part );
        if ( response != null ) {
          logResponseMessage( logFile, path, response, RequestType.IMPORT );
          response.close();
        }
        metadataDatasourceInputStream.close();
      }
    } finally {
      metadataFileInZipInputStream.close();
      part.cleanup();
    }
  }

  /**
   * --import --url=http://localhost:8080/pentaho --username=admin --password=password
   * --file-path=analysis/steelwheels.mondrian.xml --resource-type=DATASOURCE --datasource-type=ANALYSIS
   * --overwrite=true --analysis-datasource=steelwheels
   *
   * @param contextURL
   * @param analysisDatasourceFile
   * @param overwrite
   * @throws ParseException
   * @throws IOException
   */
  private void performAnalysisDatasourceImport( String contextURL, File analysisDatasourceFile, String overwrite,
                                                String logFile, String path )
    throws ParseException, IOException {

    String analysisImportURL = contextURL + API_MONDRIAN_POST_ANALYSIS;

    String catalogName = getOptionValue( INFO_OPTION_ANALYSIS_CATALOG_NAME, false, true );
    String datasourceName = getOptionValue( INFO_OPTION_ANALYSIS_DATASOURCE_NAME, false, true );
    String xmlaEnabledFlag = getOptionValue( INFO_OPTION_ANALYSIS_XMLA_ENABLED_NAME, false, true );

    WebResource resource = client.resource( analysisImportURL );
    FileInputStream inputStream = new FileInputStream( analysisDatasourceFile );
    String parms = "Datasource=" + datasourceName + ";overwrite=" + overwrite;

    FormDataMultiPart part = new FormDataMultiPart();
    part.field( MULTIPART_FIELD_OVERWRITE, "true".equals( overwrite ) ? "true" : "false", MediaType.MULTIPART_FORM_DATA_TYPE );

    if ( catalogName != null ) {
      part.field( MULTIPART_FIELD_CATALOG_NAME, catalogName, MediaType.MULTIPART_FORM_DATA_TYPE );
    }
    if ( datasourceName != null ) {
      part.field( MULTIPART_FIELD_DATASOURCE_NAME, datasourceName, MediaType.MULTIPART_FORM_DATA_TYPE );
    }
    part.field( MULTIPART_FIELD_PARAMETERS, parms, MediaType.MULTIPART_FORM_DATA_TYPE );

    part.field( MULTIPART_FIELD_XMLA_ENABLED_FLAG, "true".equals( xmlaEnabledFlag ) ? "true" : "false",
      MediaType.MULTIPART_FORM_DATA_TYPE );
    part.field( MULTIPART_FIELD_UPLOAD_ANALYSIS, inputStream, MediaType.MULTIPART_FORM_DATA_TYPE );

    // If the import service needs the file name do the following.
    part.getField( MULTIPART_FIELD_UPLOAD_ANALYSIS )
      .setContentDisposition( FormDataContentDisposition.name( MULTIPART_FIELD_UPLOAD_ANALYSIS )
        .fileName( analysisDatasourceFile.getName() ).build() );

    WebResource.Builder resourceBuilder = resource.type( MediaType.MULTIPART_FORM_DATA );
    ClientResponse response = resourceBuilder.post( ClientResponse.class, part );

    if ( response != null ) {
      logResponseMessage( logFile, path, response, RequestType.IMPORT );
      response.close();
    }
    inputStream.close();
    part.cleanup();
  }

  /**
   * @throws ParseException
   */
  private void performDatasourceImport() throws ParseException {
    String contextURL = getOptionValue( INFO_OPTION_URL_NAME, true, false );
    String filePath = getOptionValue( INFO_OPTION_FILEPATH_NAME, true, false );
    String datasourceType = getOptionValue( INFO_OPTION_DATASOURCE_TYPE_NAME, true, false );
    String overwrite = getOptionValue( INFO_OPTION_OVERWRITE_NAME, false, true );
    String logFile = getOptionValue( INFO_OPTION_LOGFILE_NAME, false, true );

    /*
     * wrap in a try/finally to ensure input stream is closed properly
     */
    try {
      initRestService( contextURL );

      File file = new File( filePath );
      if ( datasourceType != null ) {
        if ( datasourceType.equals( DatasourceType.ANALYSIS.name() ) ) {
          performAnalysisDatasourceImport( contextURL, file, overwrite, logFile, filePath );
        } else if ( datasourceType.equals( DatasourceType.METADATA.name() ) ) {
          performMetadataDatasourceImport( contextURL, file, overwrite, logFile, filePath );
        }
      }

    } catch ( Exception e ) {
      System.err.println( e.getMessage() );
      log.error( e.getMessage() );
    }
  }

  /*
   * --import --url=http://localhost:8080/pentaho --username=admin --password=password --charset=UTF-8 --path=/public
   * --file-path=C:/Users/tband/Downloads/pentaho-solutions.zip --logfile=c:/Users/tband/Desktop/logfile.log
   * --permission=true --overwrite=true --retainOwnership=true (required fields- default is false)
   */

  private void performImport() throws ParseException, IOException {
    String contextURL = getOptionValue( INFO_OPTION_URL_NAME, true, false );
    String filePath = getOptionValue( INFO_OPTION_FILEPATH_NAME, true, false );
    String resourceType = getOptionValue( INFO_OPTION_RESOURCE_TYPE_NAME, false, true );
    // We are importing datasources
    if ( resourceType != null && resourceType.equals( ResourceType.DATASOURCE.name() ) ) {
      performDatasourceImport();
    } else {
      String charSet = getOptionValue( INFO_OPTION_CHARSET_NAME, false, true );
      String logFile = getOptionValue( INFO_OPTION_LOGFILE_NAME, false, true );
      String path = getOptionValue( INFO_OPTION_PATH_NAME, true, false );

      String importURL = contextURL + API_REPO_FILES_IMPORT;
      File fileIS = new File( filePath );
      InputStream in = new FileInputStream( fileIS );
      FormDataMultiPart part = new FormDataMultiPart();

      /*
       * wrap in a try/finally to ensure input stream is closed properly
       */
      try {
        initRestService( contextURL );

        WebResource resource = client.resource( importURL );

        String overwrite = getOptionValue( INFO_OPTION_OVERWRITE_NAME, false, true );
        String retainOwnership = getOptionValue( INFO_OPTION_RETAIN_OWNERSHIP_NAME, false, true );
        String permission = getOptionValue( INFO_OPTION_PERMISSION_NAME, false, true );

        part.field( MULTIPART_FIELD_IMPORT_DIR, path, MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( MULTIPART_FIELD_OVERWRITE_ACL_PERMISSIONS, "true".equals( overwrite ) ? "true" : "false",
          MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( MULTIPART_FIELD_RETAIN_OWNERSHIP, "true".equals( retainOwnership ) ? "true" : "false",
          MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( MULTIPART_FIELD_CHAR_SET, charSet == null ? StandardCharsets.UTF_8.name() : charSet );
        part.field( MULTIPART_FIELD_APPLY_ACL_PERMISSIONS, "true".equals( permission ) ? "true" : "false",
          MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( MULTIPART_FIELD_FILE_UPLOAD, in, MediaType.MULTIPART_FORM_DATA_TYPE );

        // If the import service needs the file name do the following.
        part.field( MULTIPART_FIELD_FILE_NAME_OVERRIDE, fileIS.getName(), MediaType.MULTIPART_FORM_DATA_TYPE );
        part.getField( MULTIPART_FIELD_FILE_UPLOAD )
          .setContentDisposition( FormDataContentDisposition.name( MULTIPART_FIELD_FILE_UPLOAD ).fileName(
            fileIS.getName() ).build() );

        WebResource.Builder resourceBuilder = resource.type( MediaType.MULTIPART_FORM_DATA );
        ClientResponse response = resourceBuilder.post( ClientResponse.class, part );
        if ( response != null ) {
          logResponseMessage( logFile, path, response, RequestType.IMPORT );
          response.close();
        }
      } catch ( Exception e ) {
        System.err.println( e.getMessage() );
        log.error( e.getMessage() );
        writeToFile( e.getMessage(), logFile );
      } finally {
        // close input stream and cleanup the jersey resources
        client.destroy();
        part.cleanup();
        in.close();
      }
    }
  }

  private void logResponseMessage( String logFile, String path, ClientResponse response, RequestType requestType ) {
    if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ) {
      errorMessage = Messages.getInstance().getString( "CommandLineProcessor.INFO_" + requestType.toString() + "_SUCCESSFUL" );
    } else if ( response.getStatus() == ClientResponse.Status.FORBIDDEN.getStatusCode() ) {
      errorMessage = Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0007_FORBIDDEN", path );
    } else if ( response.getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode() ) {
      errorMessage =
        Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0004_UNKNOWN_SOURCE", path );
    }
    StringBuilder message = new StringBuilder( errorMessage );
    message.append( System.getProperty( "line.separator" ) );
    if ( response.hasEntity() ) {
      message.append( Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_RESPONSE_RECEIVED",
        response.getEntity( String.class ) ) );
    }
    System.out.println( message );
    if ( StringUtils.isNotBlank( logFile ) ) {
      writeToFile( message.toString(), logFile );
    }
  }

  /**
   * REST Service Backup
   *
   * @throws ParseException
   *           --backup --url=http://localhost:8080/pentaho --username=admin --password=password
   *                             --logfile=c:/temp/steel-wheels.log --file-path=c:/temp/backup.zip
   */
  private void performBackup() throws ParseException, KettleException, URISyntaxException {
    String contextURL = getOptionValue( INFO_OPTION_URL_NAME, true, false );
    String logFile = getOptionValue( INFO_OPTION_LOGFILE_NAME, false, true );

    // Output file is validated before executing
    String outputFile = getOptionValue( INFO_OPTION_FILEPATH_NAME, true, false );

    if ( !isValidExportPath( outputFile, logFile ) ) {
      throw new ParseException( Messages.getInstance().getString( "CommandLineProcessor.ERROR_0005_INVALID_FILE_PATH",
        outputFile ) );
    }

    initRestService( contextURL );

    // check if the user has permissions to upload/download data
    if ( !checkUserAuthorization( contextURL, AdministerSecurityAction.NAME ) ) {
      return;
    }

    // Build the complete URL to use
    String backupURL = buildURL( contextURL, API_REPO_FILES_BACKUP );

    WebResource resource = client.resource( backupURL );

    // Response response
    Builder builder = resource.type( MediaType.APPLICATION_FORM_URLENCODED ).accept( MediaType.TEXT_HTML_TYPE );
    ClientResponse response = builder.get( ClientResponse.class );
    if ( response != null && response.getStatus() == 200 ) {
      writeEntityToFile( response, outputFile );

      String message = Messages.getInstance().getString( "CommandLineProcessor.INFO_EXPORT_COMPLETED" ).concat( "\n" );
      message += Messages.getInstance().getString( "CommandLineProcessor.INFO_RESPONSE_STATUS", response.getStatus() );
      message += "\n";
      message += Messages.getInstance().getString( "CommandLineProcessor.INFO_EXPORT_WRITTEN_TO", outputFile );
      if ( StringUtils.isNotBlank( logFile ) ) {
        System.out.println( message );
        writeToFile( message, logFile );
      }
    } else {
      System.out.println( Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0002_INVALID_RESPONSE" ) );
    }
  }

  /**
   * Given the Context URL and an API, this method returns the corresponding complete URL adding the proper
   * parameters for when the request Parameter Authentication is enabled.
   *
   * @param contextURL the context URL
   * @param apiPath    the API path
   * @return Complete URL for the given API and already prepared for when the request Parameter Authentication is
   * enabled
   * @throws ParseException
   * @throws KettleException
   */
  private String buildURL( String contextURL, String apiPath ) throws ParseException, KettleException {
    StringBuilder sb = new StringBuilder();
    sb.append( contextURL ).append( apiPath );

    return sb.toString();
  }

  /**
   * REST Service Restore
   * --restore --url=http://localhost:8080/pentaho --username=admin --password=password --overwrite=true
   * --logfile=c:/temp/steel-wheels.log --file-path=c:/temp/backup.zip
   *
   * @throws ParseException
   */
  private void performRestore() throws ParseException {
    String contextURL = getOptionValue( INFO_OPTION_URL_NAME, true, false );
    String filePath = getOptionValue( INFO_OPTION_FILEPATH_NAME, true, false );
    String logFile = getOptionValue( INFO_OPTION_LOGFILE_NAME, false, true );

    String importURL = contextURL + API_REPO_FILES_SYSTEM_RESTORE;
    File fileIS = new File( filePath );
    try ( InputStream in = Files.newInputStream( fileIS.toPath() );
          FormDataMultiPart part = new FormDataMultiPart() ) {
      initRestService( contextURL );
      // check if the user has permissions to upload/download data
      if ( !checkUserAuthorization( contextURL, AdministerSecurityAction.NAME ) ) {
        return;
      }
      WebResource resource = client.resource( importURL );

      part.field( MULTIPART_FIELD_FILE_UPLOAD, in, MediaType.MULTIPART_FORM_DATA_TYPE );
      String overwrite = getOptionValue( INFO_OPTION_OVERWRITE_NAME, true, false );
      part.field( MULTIPART_FIELD_OVERWRITE_FILE, "true".equals( overwrite ) ? "true" : "false",
        MediaType.MULTIPART_FORM_DATA_TYPE );
      String applyAclSettings = getOptionValue( INFO_OPTION_APPLY_ACL_SETTINGS_NAME, false, true );
      part.field( MULTIPART_FIELD_APPLY_ACL_SETTINGS, !"false".equals( applyAclSettings ) ? "true" : "false",
        MediaType.MULTIPART_FORM_DATA_TYPE );
      String overwriteAclSettings = getOptionValue( INFO_OPTION_OVERWRITE_ACL_SETTINGS_NAME, false, true );
      part.field( MULTIPART_FIELD_OVERWRITE_ACL_SETTINGS, "true".equals( overwriteAclSettings ) ? "true" : "false",
        MediaType.MULTIPART_FORM_DATA_TYPE );

      // Response response
      ClientResponse response = resource.type( MediaType.MULTIPART_FORM_DATA ).post( ClientResponse.class, part );
      if ( response != null ) {
        logResponseMessage( logFile, filePath, response, RequestType.RESTORE );
        response.close();
      }
    } catch ( Exception e ) {
      System.err.println( e.getMessage() );
      log.error( e.getMessage() );
      writeToFile( e.getMessage(), logFile );
    } finally {
      // cleanup the jersey resources
      client.destroy();
    }
  }

  /**
   * REST Service Export
   *
   *           --export --url=http://localhost:8080/pentaho --username=admin --password=password
   *           --file-path=c:/temp/export.zip --charset=UTF-8 --path=public/pentaho-solutions/steel-wheels
   *                             --logfile=c:/temp/steel-wheels.log --withManifest=true
   */
  private void performExport() throws ParseException, KettleException, URISyntaxException {
    String contextURL = getOptionValue( INFO_OPTION_URL_NAME, true, false );
    String path = getOptionValue( INFO_OPTION_PATH_NAME, true, false );
    String withManifest = getOptionValue( INFO_OPTION_WITH_MANIFEST_NAME, false, true );
    String effPath = RepositoryPathEncoder.encodeURIComponent( RepositoryPathEncoder.encodeRepositoryPath( path ) );
    if ( effPath.lastIndexOf( ':' ) == effPath.length() - 1 // remove trailing slash
      && effPath.length() > 1 ) { // allow user to enter "--path=/"
      effPath = effPath.substring( 0, effPath.length() - 1 );
    }
    String logFile = getOptionValue( INFO_OPTION_LOGFILE_NAME, false, true );
    String exportURL =
      contextURL + API_REPO_FILES + effPath + "/download?withManifest=" + ( "false".equals( withManifest )
        ? "false" : "true" );

    // Output file is validated before executing
    String outputFile = getOptionValue( INFO_OPTION_FILEPATH_NAME, true, false );

    if ( !isValidExportPath( outputFile, logFile ) ) {
      throw new ParseException( Messages.getInstance().getString( "CommandLineProcessor.ERROR_0005_INVALID_FILE_PATH",
        outputFile ) );
    }

    initRestService( contextURL );
    WebResource resource = client.resource( exportURL );

    // Response response
    Builder builder = resource.type( MediaType.MULTIPART_FORM_DATA ).accept( MediaType.TEXT_HTML_TYPE );
    ClientResponse response = builder.get( ClientResponse.class );
    if ( response != null && response.getStatus() == 200 ) {
      writeEntityToFile( response, outputFile );
      String message = Messages.getInstance().getString( "CommandLineProcessor.INFO_EXPORT_COMPLETED" ).concat( "\n" );
      message += Messages.getInstance().getString( "CommandLineProcessor.INFO_RESPONSE_STATUS", response.getStatus() );
      message += "\n";
      message += Messages.getInstance().getString( "CommandLineProcessor.INFO_EXPORT_WRITTEN_TO", outputFile );
      if ( StringUtils.isNotBlank( logFile ) ) {
        System.out.println( message );
        writeToFile( message, logFile );
      }
      System.out.println( Messages.getInstance().getString( "CommandLineProcessor.INFO_EXPORT_SUCCESSFUL" ) );
    } else if ( response != null && response.getStatus() == 403 ) {
      errorMessage = Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0007_FORBIDDEN", path );
      System.out.println( errorMessage );
    } else if ( response != null && response.getStatus() == 404 ) {
      errorMessage = Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0004_UNKNOWN_SOURCE", path );
      System.out.println( errorMessage );
    }
  }

  private boolean isValidExportPath( String filePath, String logFile ) {

    boolean isValid = false;

    if ( filePath != null && filePath.toLowerCase().endsWith( ".zip" ) ) {

      int fileNameIdx = filePath.replace( '\\', '/' ).lastIndexOf( "/" );
      if ( fileNameIdx >= 0 ) {
        File f = new File( filePath.substring( 0, fileNameIdx ) );

        isValid = f.exists() && f.isDirectory();
      }
    }

    if ( !isValid && StringUtils.isNotBlank( logFile ) ) {
      writeToFile( "Invalid file-path:" + filePath, logFile );
    }

    return isValid;
  }

  /**
   * Returns the option value from the command line
   *
   * @param option
   *          the option whose value should be returned (NOTE: {@code null} will be returned if the option was not
   *          provided)
   * @param required
   *          indicates if the option is required
   * @param emptyOk
   *          indicates if a blank value is acceptable
   * @return the value provided from the command line, or {@code null} if none was provided
   * @throws ParseException
   *           indicates the required or non-blank value was not provided
   */
  protected String getOptionValue( final String option, final boolean required, final boolean emptyOk )
    throws ParseException {
    final String value = StringUtils.trim( commandLine.getOptionValue( option ) );

    if ( StringUtils.isEmpty( value ) && ( required || !emptyOk ) ) {
      throw new ParseException( Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0001_MISSING_ARG",
        option ) );
    }

    return StringUtils.removeStart( value, "=" );
  }

  public static String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Writes the entity on the given {@link ClientResponse} to an output file
   *
   * @param response the response instance
   * @param pathName the path of the output file
   */
  private void writeEntityToFile( ClientResponse response, String pathName ) {
    try ( InputStream input = response.getEntityInputStream() ) {
      writeToFile( input, new File( pathName ) );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Writes the given string to an output file
   *
   * @param str      the string
   * @param pathName the path of the output file
   * @see #writeToFile(InputStream, File)
   */
  private void writeToFile( String str, String pathName ) {
    try ( InputStream inputStream = IOUtils.toInputStream( str, Charset.defaultCharset() ) ) {
      writeToFile( inputStream, new File( pathName ) );
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
  }

  /**
   * Writes the contents of the given input string to an output file
   *
   * @param inputStream the input stream to persist
   * @param file        the output file
   * @see #writeToFile(String, String)
   */
  private static void writeToFile( InputStream inputStream, File file ) throws IOException {
    try ( FileOutputStream fos = new FileOutputStream( file ) ) {
      IOUtils.copy( inputStream, fos );
    }
  }

  protected static void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( Messages.getInstance().getString( "CommandLineProcessor.INFO_PRINTHELP_CMDLINE" ), Messages
      .getInstance().getString( "CommandLineProcessor.INFO_PRINTHELP_HEADER" ), options, Messages.getInstance()
      .getString( "CommandLineProcessor.INFO_PRINTHELP_FOOTER" ) );
  }

  /**
   * Check if the current user has a given authorization.
   *
   * @param contextURL     the base URL
   * @param securityAction the Action to check
   * @return <code>true</code> if the user has the given authorization, <code>false</code> if it does not
   */
  private static boolean checkUserAuthorization( String contextURL, String securityAction ) {
    WebResource authResource =
      client.resource( contextURL + API_AUTHORIZATION_ACTION_IS_AUTHORIZED + "?authAction="
        + securityAction );
    boolean isAuthorized = Boolean.parseBoolean( authResource.get( String.class ) );
    if ( !isAuthorized ) {
      System.err.println( Messages.getInstance().getString(
        "CommandLineProcessor.ERROR_0006_NON_ADMIN_CREDENTIALS" ) );
    }
    return isAuthorized;
  }
}
