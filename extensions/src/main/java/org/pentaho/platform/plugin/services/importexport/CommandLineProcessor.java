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


package org.pentaho.platform.plugin.services.importexport;

import com.google.common.annotations.VisibleForTesting;
import com.hitachivantara.security.web.impl.client.csrf.jaxrsv3.CsrfTokenFilter;
import com.hitachivantara.security.web.impl.client.csrf.jaxrsv3.util.SessionCookiesFilter;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
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
import java.nio.file.NoSuchFileException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.util.RepositoryPathEncoder;

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
  private static final String MULTIVALUE_FIELD_LOG_FILE = "logFile";
  private static final String MULTIVALUE_FIELD_LOG_LEVEL = "logLevel";
  private static final String MULTIVALUE_FIELD_BACKUP_BUNDLE_PATH = "backupBundlePath";
  private static final String MULTIVALUE_FIELD_OUTPUT_FILE_NAME_LEVEL = "outputFile";

  private static final String METADATA_DATASOURCE_EXT = "xmi";

  private static final String ZIP_EXT = "zip";

  private static final Log log = LogFactory.getLog( CommandLineProcessor.class );

  private static final Options options = new Options();

  private static Exception exception;

  private static String errorMessage;

  private final CommandLine commandLine;

  private final RequestType requestType;
  private static final String DEFAULT_LOG_LEVEL = "INFO";
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
  private static final String INFO_OPTION_LOGLEVEL_NAME = "logLevel";
  private static final String INFO_OPTION_LOGLEVEL_KEY = "lL";
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
    clientConfig = new ClientConfig();
    clientConfig.register( MultiPartFeature.class );

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

    options.addOption( INFO_OPTION_LOGLEVEL_KEY, INFO_OPTION_LOGLEVEL_NAME, true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_LOGLEVEL_DESCRIPTION" ) );

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
    WebTarget webTarget = client.target( exportURL );

    // Response response
    Invocation.Builder builder = webTarget.request( MediaType.APPLICATION_JSON ).accept( MediaType.TEXT_XML_TYPE );
    Response response = builder.get( Response.class );
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

    client = ClientBuilder.newClient( clientConfig );
    client.register( HttpAuthenticationFeature.basic( getUsername(), getPassword() ) );
    client.register( new SessionCookiesFilter( new CookieManager() ) );
    client.register( new CsrfTokenFilter( new URI( contextURL + API_CSRF_TOKEN ) ) );
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
   * @param args the command line arguments
   * @throws ParseException indicates that neither (or both) an import and/or export have been request
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
    ZipInputStream zipInputStream = null;

    String metadataImportURL = contextURL + API_METADATA_POST_IMPORT;

    String domainId = getOptionValue( INFO_OPTION_METADATA_DOMAIN_ID_NAME, true, false );

    WebTarget webTarget = client.target( metadataImportURL );

    FormDataMultiPart part = new FormDataMultiPart();

    final String name = RepositoryFilenameUtils.separatorsToRepository( metadataDatasourceFile.getName() );
    final String ext = RepositoryFilenameUtils.getExtension( name );

    try {
      if ( ext.equals( ZIP_EXT ) ) {
        zipInputStream = new ZipInputStream( new FileInputStream( metadataDatasourceFile ) );
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
        zipInputStream = null;

        part.field( MULTIPART_FIELD_OVERWRITE, "true".equals( overwrite ) ? "true" : "false",
            MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( MULTIPART_FIELD_DOMAIN_ID, domainId, MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( MULTIPART_FIELD_METADATA_FILE, metadataFileInZipInputStream, MediaType.MULTIPART_FORM_DATA_TYPE );

        // If the import service needs the file name do the following.
        part.getField( MULTIPART_FIELD_METADATA_FILE )
            .setContentDisposition( FormDataContentDisposition.name( MULTIPART_FIELD_METADATA_FILE )
                .fileName( metadataFileInZip.getName() ).build() );

        // Response response
        Response response = webTarget.request( MediaType.TEXT_HTML_TYPE ).post( Entity.entity( part, MediaType.MULTIPART_FORM_DATA_TYPE ) );
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
        Response response = webTarget.request( MediaType.TEXT_HTML_TYPE ).post( Entity.entity( part, MediaType.MULTIPART_FORM_DATA_TYPE ) );
        if ( response != null ) {
          logResponseMessage( logFile, path, response, RequestType.IMPORT );
          response.close();
        }
        metadataDatasourceInputStream.close();
      }
    } finally {
      if ( metadataFileInZipInputStream != null ) {
        metadataFileInZipInputStream.close();
      }
      if ( zipInputStream != null ) {
        zipInputStream.close();
      }
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

    WebTarget webTarget = client.target( analysisImportURL );
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

    Response response = webTarget.request( MediaType.TEXT_HTML_TYPE ).post( Entity.entity( part, MediaType.MULTIPART_FORM_DATA_TYPE ) );

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

        WebTarget webTarget = client.target( importURL );

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

        Response response = webTarget.request( MediaType.MULTIPART_FORM_DATA_TYPE )
          .accept( MediaType.TEXT_HTML_TYPE )
          .post( Entity.entity( part, MediaType.MULTIPART_FORM_DATA_TYPE ) );
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
        if ( client != null ) {
          client.close();
        }
        if ( part != null ) {
          part.cleanup();
        }
        if ( in != null ) {
          in.close();
        }
      }
    }
  }

  protected void logResponseMessage( String logFile, String path, Response response, RequestType requestType ) {
    boolean badLogFilePath = false;
    String responseBody = null;
    
    if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
      errorMessage =
        Messages.getInstance().getString( "CommandLineProcessor.INFO_" + requestType.toString() + "_SUCCESSFUL" );
    } else if ( response.getStatus() == Response.Status.FORBIDDEN.getStatusCode() ) {
      errorMessage = Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0007_FORBIDDEN", path );
    } else if ( response.getStatus() == Response.Status.NOT_FOUND.getStatusCode() ) {
      errorMessage =
        Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0004_UNKNOWN_SOURCE", path );
    } else if ( response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode() ) {
      errorMessage =
        Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0009_INVALID_LOG_FILE_PATH", logFile );
      badLogFilePath = true;
    } else if ( response.getStatus() == 406 ) {
      // HTTP 406 Not Acceptable - content-type mismatch
      errorMessage = "HTTP 406 Not Acceptable - The server cannot process the request. Verify the metadata file format (.xmi) and ensure the endpoint expects the correct content type.";
    }

    StringBuilder message = new StringBuilder( errorMessage );
    if ( !badLogFilePath ) {
      message.append( System.getProperty( "line.separator" ) );
      if ( response.hasEntity() ) {
        responseBody = response.readEntity( String.class );
        
        // Parse datasource import response codes for better user feedback
        if ( requestType == RequestType.IMPORT && StringUtils.isNotBlank( responseBody ) ) {
          String trimmedResponse = responseBody.trim();
          String importDetails = parseDatasourceImportResponse( trimmedResponse );
          if ( importDetails != null ) {
            message.append( importDetails );
          } else {
            message.append( Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_RESPONSE_RECEIVED",
              responseBody ) );
          }
        } else {
          message.append( Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_RESPONSE_RECEIVED",
            responseBody ) );
        }
      }
      System.out.println( message );
      if ( StringUtils.isNotBlank( logFile ) ) {
        writeToFile( message.toString(), logFile );
      }
    } else {
      System.out.println( message );
    }
  }

  /**
   * Parse datasource import response codes and return user-friendly messages based on PlatformImportException status codes.
   * These codes come from the data-access REST API endpoints for metadata and analysis datasource imports.
   * 
   * Status codes from org.pentaho.platform.plugin.services.importer.PlatformImportException:
   * - 1: PUBLISH_GENERAL_ERROR - General server-side failure
   * - 2: PUBLISH_UNSPECIFIED_ERROR - General unspecified error
   * - 5: PUBLISH_USERNAME_PASSWORD_FAIL - Authentication failure (username or password error)
   * - 6: PUBLISH_CONNECTION_ERROR - Data source/connection problem
   * - 7: PUBLISH_XMLA_ALREADY_EXISTS - XMLA Catalog name already exists
   * - 8: PUBLISH_SCHEMA_EXISTS - Schema already exists
   * - 9: PUBLISH_CONTENT_EXISTS - Content already exists
   * - 10: PUBLISH_PROHIBITED_SYMBOLS_ERROR - Prohibited characters in name/content
   * - 11: PUBLISH_PLUGIN_ERROR - Job or transformation has missing plugins
   * - 12: PUBLISH_PARTIAL_ERROR - Partial upload (incomplete transfer)
   * - 13: PUBLISH_NAME_ERROR - Name validation error
   * - 3: SUCCESS (non-standard, used by the platform)
   * 
   * @param responseBody the response body string
   * @return user-friendly message for the response code, or null if not recognized
   */
  protected String parseDatasourceImportResponse( String responseBody ) {
    try {
      int responseCode = Integer.parseInt( responseBody.trim() );
      switch ( responseCode ) {
        case 1:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_GENERAL_SERVER_ERROR" );
        case 2:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_UNSPECIFIED_ERROR" );
        case 3:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_SUCCESS" );
        case 5:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_AUTH_FAILURE" );
        case 6:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_CONNECTION_ERROR" );
        case 7:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_XMLA_EXISTS" );
        case 8:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_SCHEMA_EXISTS" );
        case 9:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_CONTENT_EXISTS" );
        case 10:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_PROHIBITED_CHARS" );
        case 11:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_PLUGIN_ERROR" );
        case 12:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_PARTIAL_ERROR" );
        case 13:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_NAME_ERROR" );
        default:
          return Messages.getInstance().getString( "CommandLineProcessor.DATASOURCE_IMPORT_UNKNOWN_CODE", 
            String.valueOf( responseCode ) );
      }
    } catch ( NumberFormatException e ) {
      // If it's not a numeric response code, return null to use default handling
      return null;
    }
  }

  /**
   * REST Service Backup
   *
   * @throws ParseException --backup --url=http://localhost:8080/pentaho --username=admin --password=password
   *                        --logfile=c:/temp/steel-wheels.log --file-path=c:/temp/backup.zip
   */
  private void performBackup() throws ParseException, KettleException, URISyntaxException {
    String contextURL = getOptionValue( INFO_OPTION_URL_NAME, true, false );
    String logFile = getOptionValue( INFO_OPTION_LOGFILE_NAME, false, true );
    String logLevel = getOptionValue( INFO_OPTION_LOGLEVEL_NAME, false, true );
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
    WebTarget webTarget = client.target( backupURL );

    // Response response
    MultivaluedMap<String, String> postBody = new MultivaluedHashMap<>();
    postBody.add( MULTIVALUE_FIELD_LOG_FILE, logFile );
    postBody.add( MULTIVALUE_FIELD_LOG_LEVEL, logLevel != null && logLevel.length() > 0 ? logLevel : DEFAULT_LOG_LEVEL );
    postBody.add( MULTIVALUE_FIELD_OUTPUT_FILE_NAME_LEVEL, outputFile );

    Response response = webTarget.request( MediaType.APPLICATION_FORM_URLENCODED ).post( Entity.entity( postBody, MediaType.APPLICATION_FORM_URLENCODED), Response.class );
    if ( response != null && response.getStatus() == 200 ) {
      writeEntityToFile( response, outputFile );

      String message = Messages.getInstance().getString( "CommandLineProcessor.INFO_BACKUP_COMPLETED" ).concat( "\n" );
      message += Messages.getInstance().getString( "CommandLineProcessor.INFO_RESPONSE_STATUS", response.getStatus() );
      message += "\n";
      message += Messages.getInstance().getString( "CommandLineProcessor.INFO_BACKUP_WRITTEN_TO", outputFile );
      if ( StringUtils.isNotBlank( logFile ) ) {
        System.out.println( message );
        writeToFile( message, logFile );
      }
    } else if ( response != null && response.getStatus() == 400 ) {
      System.out.println( Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0009_INVALID_LOG_FILE_PATH", logFile ) );
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
    String logLevel = getOptionValue( INFO_OPTION_LOGLEVEL_NAME, false, true );

    String importURL = contextURL + API_REPO_FILES_SYSTEM_RESTORE;
    File fileIS = new File( filePath );
    try ( InputStream in = Files.newInputStream( fileIS.toPath() );
          FormDataMultiPart part = new FormDataMultiPart() ) {
      initRestService( contextURL );
      // check if the user has permissions to upload/download data
      if ( !checkUserAuthorization( contextURL, AdministerSecurityAction.NAME ) ) {
        return;
      }
      WebTarget webTarget = client.target( importURL );

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
      part.field( MULTIVALUE_FIELD_LOG_FILE, logFile, MediaType.MULTIPART_FORM_DATA_TYPE );
      part.field( MULTIVALUE_FIELD_LOG_LEVEL, logLevel != null && logLevel.length() > 0 ? logLevel : DEFAULT_LOG_LEVEL, MediaType.MULTIPART_FORM_DATA_TYPE );
      part.field( MULTIVALUE_FIELD_BACKUP_BUNDLE_PATH, filePath, MediaType.MULTIPART_FORM_DATA_TYPE );
      // Response response
      Response response = webTarget.request( MediaType.MULTIPART_FORM_DATA ).post( Entity.entity( part, MediaType.MULTIPART_FORM_DATA_TYPE ) );
      if ( response != null && response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode() ) {
        errorMessage = Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0009_INVALID_LOG_FILE_PATH", logFile );
        System.out.println( errorMessage );
      } else if ( response != null ) {
        logResponseMessage( logFile, filePath, response, RequestType.RESTORE );
        response.close();
      }
    } catch ( NoSuchFileException nsfe ) {
      String message = Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0010_FILE_DOES_NOT_EXIST", nsfe.getMessage() );
      System.err.println( message );
      log.error( message );
      writeToFile( message, logFile );
    } catch ( Exception e ) {
      System.err.println( e.getMessage() );
      log.error( e.getMessage() );
      writeToFile( e.getMessage(), logFile );
    } finally {
      // cleanup the jersey resources
      if( client != null ) {
        client.close();
      }
    }
  }

  /**
   * REST Service Export
   * <p>
   * --export --url=http://localhost:8080/pentaho --username=admin --password=password
   * --file-path=c:/temp/export.zip --charset=UTF-8 --path=public/pentaho-solutions/steel-wheels
   * --logfile=c:/temp/steel-wheels.log --withManifest=true
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
    WebTarget webTarget = client.target( exportURL );

    // Response response
    Invocation.Builder builder = webTarget.request( MediaType.MULTIPART_FORM_DATA ).accept( MediaType.TEXT_HTML_TYPE );
    Response response = builder.get( Response.class );
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
    } else {
      errorMessage = Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0002_INVALID_RESPONSE" );
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
   * @param option   the option whose value should be returned (NOTE: {@code null} will be returned if the option was not
   *                 provided)
   * @param required indicates if the option is required
   * @param emptyOk  indicates if a blank value is acceptable
   * @return the value provided from the command line, or {@code null} if none was provided
   * @throws ParseException indicates the required or non-blank value was not provided
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
  private void writeEntityToFile( Response response, String pathName ) {
    try ( InputStream input = ( InputStream ) response.getEntity() ) {
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
    try ( FileOutputStream fos = new FileOutputStream( file, true ) ) {
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
    WebTarget authTarget =
        client.target( contextURL + API_AUTHORIZATION_ACTION_IS_AUTHORIZED + "?authAction="
            + securityAction );
    boolean isAuthorized = Boolean.parseBoolean( authTarget.request().get().readEntity( String.class ) );
    if ( !isAuthorized ) {
      System.err.println( Messages.getInstance().getString(
          "CommandLineProcessor.ERROR_0006_NON_ADMIN_CREDENTIALS" ) );
    }
    return isAuthorized;
  }
}
