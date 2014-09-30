/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importexport;

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
import com.sun.xml.ws.developer.JAXWSProperties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.webservices.jaxws.IUnifiedRepositoryJaxwsWebService;
import org.pentaho.platform.repository2.unified.webservices.jaxws.UnifiedRepositoryToWebServiceAdapter;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.util.RepositoryPathEncoder;

import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handles the parsing of command line arguments and creates an import process based upon them
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class CommandLineProcessor {
  private static final String API_REPO_FILES_IMPORT = "/api/repo/files/import";

  private static final String ANALYSIS_DATASOURCE_IMPORT = "/plugin/data-access/api/mondrian/postAnalysis";

  private static final String METADATA_DATASOURCE_IMPORT = "/plugin/data-access/api/metadata/postimport";

  private static final String METADATA_DATASOURCE_EXT = "xmi";

  private static final String ZIP_EXT = "zip";

  private static final Log log = LogFactory.getLog( CommandLineProcessor.class );

  private static final Options options = new Options();

  private static Exception exception;

  private CommandLine commandLine;

  private RequestType requestType;

  private IUnifiedRepository repository;

  private static enum RequestType {
    HELP, IMPORT, EXPORT, REST
  }

  private static enum DatasourceType {
    JDBC, METADATA, ANALYSIS
  }

  private static enum ResourceType {
    SOLUTIONS, DATASOURCE
  }

  private static Client client = null;

  static {
    // create the Options
    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_HELP_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_HELP_NAME" ), false, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_HELP_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_IMPORT_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_IMPORT_NAME" ), false, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_IMPORT_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_EXPORT_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_EXPORT_NAME" ), false, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_EXPORT_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_USERNAME_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_USERNAME_NAME" ), true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_USERNAME_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PASSWORD_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_PASSWORD_NAME" ), true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_PASSWORD_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_NAME" ), true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_URL_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_NAME" ), true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_CHARSET_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_CHARSET_NAME" ), true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_CHARSET_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_LOGFILE_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_LOGFILE_NAME" ), true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_LOGFILE_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PATH_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_PATH_NAME" ), true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_PATH_DESCRIPTION" ) );

    // import only ACL additions
    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_OVERWRITE_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_OVERWRITE_NAME" ), true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_OVERWRITE_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PERMISSION_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_PERMISSION_NAME" ), true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_PERMISSION_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_RETAIN_OWNERSHIP_KEY" ),
        Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_RETAIN_OWNERSHIP_NAME" ), true, Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_RETAIN_OWNERSHIP_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_WITH_MANIFEST_KEY" ),
        Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_WITH_MANIFEST_NAME" ), true, Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_WITH_MANIFEST_DESCRIPTION" ) );

    // rest services
    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_REST_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_REST_NAME" ), false, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_REST_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_SERVICE_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_SERVICE_NAME" ), true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_SERVICE_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PARAMS_KEY" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_OPTION_PARAMS_NAME" ), true, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_OPTION_PARAMS_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_RESOURCE_TYPE_KEY" ),
        Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_RESOURCE_TYPE_NAME" ), true, Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_RESOURCE_TYPE_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_TYPE_KEY" ),
        Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_TYPE_NAME" ), true, Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_TYPE_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_TYPE_KEY" ),
        Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_TYPE_NAME" ), true, Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_RESOURCE_TYPE_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_TYPE_KEY" ),
        Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_TYPE_NAME" ), true, Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_TYPE_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_CATALOG_KEY" ),
        Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_CATALOG_NAME" ), true, Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_CATALOG_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_DATASOURCE_KEY" ),
        Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_DATASOURCE_NAME" ), true, Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_DATASOURCE_DESCRIPTION" ) );

    options.addOption(
        Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_XMLA_ENABLED_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_XMLA_ENABLED_NAME" ), true, Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_XMLA_ENABLED_DESCRIPTION" ) );

    options.addOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_METADATA_DOMAIN_ID_KEY" ),
        Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_METADATA_DOMAIN_ID_NAME" ), true, Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_METADATA_DOMAIN_ID_DESCRIPTION" ) );
  }

  /**
   * How this class is executed from the command line.
   * 
   * @param args
   */
  public static void main( String[] args ) throws Exception {

    try {
      // reset the exception information
      exception = null;

      final CommandLineProcessor commandLineProcessor = new CommandLineProcessor( args );

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
      }
    } catch ( ParseException parseException ) {
      exception = parseException;
      System.err.println( parseException.getLocalizedMessage() );
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
   * 
   * @throws ParseException
   */
  private void performREST() throws ParseException, InitializationException {

    String contextURL =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_NAME" ), true, false );
    String path =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PATH_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_PATH_NAME" ), true, false );
    String logFile =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_LOGFILE_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_LOGFILE_NAME" ), false, true );
    String exportURL = contextURL + "/api/repo/files/";
    if ( path != null ) {
      String effPath = RepositoryPathEncoder.encodeRepositoryPath( path );
      exportURL += effPath;
    }
    String service =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_SERVICE_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_SERVICE_NAME" ), true, false );
    String params =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PARAMS_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_PARAMS_NAME" ), false, true );
    exportURL += "/" + service;
    if ( params != null ) {
      exportURL += "?params=" + params;
    }

    initRestService();
    WebResource resource = client.resource( exportURL );

    // Response response
    Builder builder = resource.type( MediaType.APPLICATION_JSON ).type( MediaType.TEXT_XML_TYPE );
    ClientResponse response = builder.put( ClientResponse.class );
    if ( response != null && response.getStatus() == 200 ) {

      String message = Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_COMPLETED" ).concat( "\n" );
      message +=
          Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_RESPONSE_STATUS", response.getStatus() );
      message += "\n";

      if ( logFile != null && !"".equals( logFile ) ) {
        message += Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_FILE_WRITTEN", logFile );
        System.out.println( message );
        writeFile( message, logFile );
      }
    } else {
      System.out.println( Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0002_INVALID_RESPONSE" ) );
    }
  }

  /**
   * Used only for REST Jersey calls
   * 
   * @throws ParseException
   */
  private void initRestService() throws ParseException, InitializationException {
    // get information about the remote connection
    String username =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_USERNAME_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_USERNAME_NAME" ), true, false );
    String password =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PASSWORD_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_PASSWORD_NAME" ), true, false );
    ClientConfig clientConfig = new DefaultClientConfig();
    clientConfig.getFeatures().put( JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE );
    client = Client.create( clientConfig );
    client.addFilter( new HTTPBasicAuthFilter( username, password ) );

    // check if the user has permissions to upload/download data
    String contextURL =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_NAME" ), true, false );
    WebResource resource = client.resource( contextURL + "/api/authorization/action/isauthorized?authAction="
        + AdministerSecurityAction.NAME );
    String response = resource.get( String.class );
    if ( !response.equals( "true" ) ) {
      throw new InitializationException( Messages.getInstance().getString(
          "CommandLineProcessor.ERROR_0006_NON_ADMIN_CREDENTIALS" ) );
    }
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
    commandLine = new PosixParser().parse( options, args );
    if ( commandLine.hasOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_HELP_NAME" ) )
        || commandLine.hasOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_HELP_KEY" ) ) ) {
      requestType = RequestType.HELP;
    } else {
      if ( commandLine.hasOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_REST_NAME" ) )
          || commandLine.hasOption( Messages.getInstance().
            getString( "CommandLineProcessor.INFO_OPTION_REST_KEY" ) ) ) {
        requestType = RequestType.REST;
      } else {
        final boolean importRequest =
            commandLine.hasOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_IMPORT_KEY" ) );
        final boolean exportRequest =
            commandLine.hasOption( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_EXPORT_KEY" ) );

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
  private void performMetadataDatasourceImport( String contextURL, File metadataDatasourceFile,
      String overwrite ) throws ParseException, IOException {
    File metadataFileInZip = null;
    InputStream metadataFileInZipInputStream = null;

    String metadataImportURL = contextURL + METADATA_DATASOURCE_IMPORT;

    String domainId =
        getOptionValue( Messages.getInstance().getString(
          "CommandLineProcessor.INFO_OPTION_METADATA_DOMAIN_ID_KEY" ),
            Messages.getInstance().getString(
              "CommandLineProcessor.INFO_OPTION_METADATA_DOMAIN_ID_NAME" ), true, false );

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
          File tempFile = null;
          boolean isDir = entry.getSize() == 0;
          if ( !isDir ) {
            tempFile = File.createTempFile( "zip", null );
            tempFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream( tempFile );
            IOUtils.copy( zipInputStream, fos );
            fos.close();
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

        part.field( "overwrite", "true".equals( overwrite ) ? "true" : "false", MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( "domainId", domainId, MediaType.MULTIPART_FORM_DATA_TYPE ).field( "metadataFile",
            metadataFileInZipInputStream, MediaType.MULTIPART_FORM_DATA_TYPE );

        // If the import service needs the file name do the following.
        part.getField( "metadataFile" ).setContentDisposition(
            FormDataContentDisposition.name( "metadataFile" ).fileName( metadataFileInZip.getName() ).build() );

        // Response response
        ClientResponse response = resource.type( MediaType.MULTIPART_FORM_DATA ).post( ClientResponse.class, part );
        if ( response != null ) {
          String message = response.getEntity( String.class );
          System.out.println( Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_RESPONSE_RECEIVED",
              message ) );
        }

      } else {
        FileInputStream metadataDatasourceInputStream = new FileInputStream( metadataDatasourceFile );

        part.field( "overwrite", "true".equals( overwrite ) ? "true" : "false", MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( "domainId", domainId, MediaType.MULTIPART_FORM_DATA_TYPE ).field( "metadataFile",
            metadataDatasourceInputStream, MediaType.MULTIPART_FORM_DATA_TYPE );

        // If the import service needs the file name do the following.
        part.getField( "metadataFile" ).setContentDisposition(
            FormDataContentDisposition.name( "metadataFile" ).fileName( metadataDatasourceFile.getName() ).build() );

        // Response response
        ClientResponse response = resource.type( MediaType.MULTIPART_FORM_DATA ).post( ClientResponse.class, part );
        if ( response != null ) {
          String message = response.getEntity( String.class );
          System.out.println( Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_RESPONSE_RECEIVED",
              message ) );
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
  private void performAnalysisDatasourceImport( String contextURL, File analysisDatasourceFile,
      String overwrite ) throws ParseException, IOException {
    String analysisImportURL = contextURL + ANALYSIS_DATASOURCE_IMPORT;

    String catalogName =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_CATALOG_Key" ),
            Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_CATALOG_NAME" ), false, true );
    String datasourceName =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_DATASOURCE_KEY" ),
            Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_DATASOURCE_NAME" ), false,
            true );
    String xmlaEnabledFlag =
        getOptionValue(
            Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_ANALYSIS_XMLA_ENABLED_KEY" ), Messages
                .getInstance().getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_ANALYSIS_XMLA_ENABLED_NAME" ),
            false, true );

    WebResource resource = client.resource( analysisImportURL );
    FileInputStream inputStream = new FileInputStream( analysisDatasourceFile );
    String parms = "Datasource=" + datasourceName + ";overwrite=" + overwrite;

    FormDataMultiPart part = new FormDataMultiPart();
    part.field( "overwrite", "true".equals( overwrite ) ? "true" : "false", MediaType.MULTIPART_FORM_DATA_TYPE );

    if ( catalogName != null ) {
      part.field( "catalogName", catalogName, MediaType.MULTIPART_FORM_DATA_TYPE );
    }
    if ( datasourceName != null ) {
      part.field( "datasourceName", datasourceName, MediaType.MULTIPART_FORM_DATA_TYPE );
    }
    part.field( "parameters", parms, MediaType.MULTIPART_FORM_DATA_TYPE );

    part.field( "xmlaEnabledFlag", "true".equals( xmlaEnabledFlag ) ? "true" : "false",
        MediaType.MULTIPART_FORM_DATA_TYPE );
    part.field( "uploadAnalysis", inputStream, MediaType.MULTIPART_FORM_DATA_TYPE );

    // If the import service needs the file name do the following.
    part.getField( "uploadAnalysis" ).setContentDisposition(
        FormDataContentDisposition.name( "uploadAnalysis" ).fileName( analysisDatasourceFile.getName() ).build() );

    // Response response
    ClientResponse response = resource.type( MediaType.MULTIPART_FORM_DATA ).post( ClientResponse.class, part );
    if ( response != null ) {
      String message = response.getEntity( String.class );
      response.close();
      System.out.println( Messages.getInstance()
          .getString( "CommandLineProcessor.INFO_REST_RESPONSE_RECEIVED", message ) );
    }
    inputStream.close();
    part.cleanup();
  }

  /**
   * @throws ParseException
   * @throws IOException
   */
  private void performDatasourceImport() throws ParseException, IOException {
    String contextURL =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_Key" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_NAME" ), true, false );
    String filePath =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_NAME" ), true, false );
    String datasourceType =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_TYPE_KEY" ),
            Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_DATASOURCE_TYPE_NAME" ), true, false );
    String overwrite =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_OVERWRITE_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_OVERWRITE_NAME" ), false, true );

    /*
     * wrap in a try/finally to ensure input stream is closed properly
     */
    try {
      initRestService();

      File file = new File( filePath );
      if ( datasourceType != null ) {
        if ( datasourceType.equals( DatasourceType.ANALYSIS.name() ) ) {
          performAnalysisDatasourceImport( contextURL, file, overwrite );
        } else if ( datasourceType.equals( DatasourceType.METADATA.name() ) ) {
          performMetadataDatasourceImport( contextURL, file, overwrite );
        }
      }

    } catch ( Exception e ) {
      System.err.println( e.getMessage() );
      log.error( e.getMessage() );
    }

  }

  /*
   * --import --url=http://localhost:8080/pentaho - -username=admin --password=password
   * --charset=UTF-8 --path=:public --file-path=C:/Users/tband/Downloads/pentaho-solutions.zip
   * --logfile=c:/Users/tband/Desktop/logfile.log --permission=true --overwrite=true --retainOwnership=true (required
   * fields- default is false)
   */

  private void performImport() throws ParseException, IOException {
    String contextURL =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_Key" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_NAME" ), true, false );
    String filePath =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_NAME" ), true, false );
    String resourceType =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_RESOURCE_TYPE_KEY" ),
            Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_RESOURCE_TYPE_NAME" ), false, true );
    // We are importing datasources
    if ( resourceType != null && resourceType.equals( ResourceType.DATASOURCE.name() ) ) {
      performDatasourceImport();
    } else {
      String charSet =
          getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_CHARSET_KEY" ), Messages
              .getInstance().getString( "CommandLineProcessor.INFO_OPTION_CHARSET_NAME" ), false, true );
      String logFile =
          getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_LOGFILE_KEY" ), Messages
              .getInstance().getString( "CommandLineProcessor.INFO_OPTION_LOGFILE_NAME" ), false, true );
      String path =
          getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PATH_KEY" ), Messages
              .getInstance().getString( "CommandLineProcessor.INFO_OPTION_PATH_NAME" ), true, false );

      String importURL = contextURL + API_REPO_FILES_IMPORT;
      File fileIS = new File( filePath );
      InputStream in = new FileInputStream( fileIS );
      FormDataMultiPart part = new FormDataMultiPart();

      /*
       * wrap in a try/finally to ensure input stream is closed properly
       */
      try {
        initRestService();
        WebResource resource = client.resource( importURL );

        String overwrite =
            getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_OVERWRITE_KEY" ),
                Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_OVERWRITE_NAME" ), true, false );
        String retainOwnership =
            getOptionValue(
                Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_RETAIN_OWNERSHIP_KEY" ), Messages
                    .getInstance().getString( "CommandLineProcessor.INFO_OPTION_RETAIN_OWNERSHIP_NAME" ), true, false );
        String permission =
            getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PERMISSION_KEY" ),
                Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PERMISSION_NAME" ), true, false );

        part.field( "importDir", path, MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( "overwriteAclPermissions", "true".equals( overwrite ) ? "true" : "false",
            MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( "retainOwnership", "true".equals( retainOwnership ) ? "true" : "false",
            MediaType.MULTIPART_FORM_DATA_TYPE );
        part.field( "charSet", charSet == null ? "UTF-8" : charSet );
        part.field( "applyAclPermissions", "true".equals( permission ) ? "true" : "false",
            MediaType.MULTIPART_FORM_DATA_TYPE ).field( "fileUpload", in, MediaType.MULTIPART_FORM_DATA_TYPE );

        // If the import service needs the file name do the following.
        part.getField( "fileUpload" ).setContentDisposition(
            FormDataContentDisposition.name( "fileUpload" ).fileName( fileIS.getName() ).build() );

        // Response response
        ClientResponse response = resource.type( MediaType.MULTIPART_FORM_DATA ).post( ClientResponse.class, part );
        if ( response != null ) {
          String message = response.getEntity( String.class );
          System.out.println( Messages.getInstance().getString( "CommandLineProcessor.INFO_REST_RESPONSE_RECEIVED",
              message ) );
          if ( logFile != null && !"".equals( logFile ) ) {
            writeFile( message, logFile );
          }
          response.close();
        }
      } catch ( Exception e ) {
        System.err.println( e.getMessage() );
        log.error( e.getMessage() );
        writeFile( e.getMessage(), logFile );
      } finally {
        // close input stream and cleanup the jersey resources
        client.destroy();
        part.cleanup();
        in.close();
      }
    }
  }

  /**
   * REST Service Export
   * 
   * @throws ParseException
   *           --export --url=http://localhost:8080/pentaho --username=admin --password=password
   *           --file-path=c:/temp/export.zip --charset=UTF-8 --path=public/pentaho-solutions/steel-wheels
   *           --logfile=c:/temp/steel-wheels.log --withManifest=true
   * @throws java.io.IOException
   */
  private void performExport() throws ParseException, IOException, InitializationException {
    String contextURL =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_NAME" ), true, false );
    String path =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PATH_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_PATH_NAME" ), true, false );
    String withManifest =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_WITH_MANIFEST_KEY" ),
            Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_WITH_MANIFEST_NAME" ), false, true );
    String effPath = RepositoryPathEncoder.encodeRepositoryPath( path );
    if ( effPath.lastIndexOf( ":" ) == effPath.length() - 1 // remove trailing slash
        && effPath.length() > 1  ) { // allow user to enter "--path=/"
      effPath = effPath.substring( 0, effPath.length() - 1 );
    }
    String logFile =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_LOGFILE_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_LOGFILE_NAME" ), false, true );
    String exportURL =
        contextURL + "/api/repo/files/" + effPath + "/download?withManifest="
            + ( "false".equals( withManifest ) ? "false" : "true" );

    // path is validated before executing
    String filepath =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_NAME" ), true, false );

    if ( !isValidExportPath( filepath, logFile ) ) {
      throw new ParseException( Messages.getInstance().getString( "CommandLineProcessor.ERROR_0005_INVALID_FILE_PATH"
          , filepath ) );
    }

    initRestService();
    WebResource resource = client.resource( exportURL );

    // Response response
    Builder builder = resource.type( MediaType.MULTIPART_FORM_DATA ).accept( MediaType.TEXT_HTML_TYPE );
    ClientResponse response = builder.get( ClientResponse.class );
    if ( response != null && response.getStatus() == 200 ) {
      String filename =
          getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_KEY" ), Messages
              .getInstance().getString( "CommandLineProcessor.INFO_OPTION_FILEPATH_NAME" ), true, false );
      final InputStream input = response.getEntityInputStream();
      createZipFile( filename, input );
      input.close();
      String message = Messages.getInstance().getString( "CommandLineProcessor.INFO_EXPORT_COMPLETED" ).concat( "\n" );
      message += Messages.getInstance().getString( "CommandLineProcessor.INFO_RESPONSE_STATUS", response.getStatus() );
      message += "\n";
      message += Messages.getInstance().getString( "CommandLineProcessor.INFO_EXPORT_WRITTEN_TO", filename );
      if ( logFile != null && !"".equals( logFile ) ) {
        System.out.println( message );
        writeFile( message, logFile );
      }
    } else {
      System.out.println( Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0002_INVALID_RESPONSE" ) );
      if ( response != null && response.getStatus() == 404 ) {
        throw new ParseException( Messages.getInstance().getErrorString(
            "CommandLineProcessor.ERROR_0004_UNKNOWN_SOURCE", path ) );
      }
    }
  }

  private boolean isValidExportPath( String filePath, String logFile ) {

    boolean isValid = false;

    if ( filePath != null && filePath.toLowerCase().endsWith( ".zip" ) ) {

      int fileNameIdx = filePath.replace( "\\", "/" ).lastIndexOf( "/" );
      if ( fileNameIdx >= 0 ) {
        String directoryPath = filePath.substring( 0, fileNameIdx );

        File f = new File( directoryPath );
        if ( f != null && f.exists() && f.isDirectory() ) {
          isValid = true;
        }
      }
    }

    if ( !isValid && logFile != null && !"".equals( logFile ) ) {
      writeFile( "Invalid file-path:" + filePath, logFile );
    }

    return isValid;
  }

  /**
   * create the zip file from the input stream
   * 
   * @param filename
   * @param input
   *          InputStream
   */
  private void createZipFile( String filename, final InputStream input ) {
    OutputStream output = null;
    try {
      output = new FileOutputStream( filename );
      byte[] buffer = new byte[8 * 1024];
      int bytesRead;
      while ( ( bytesRead = input.read( buffer ) ) != -1 ) {
        output.write( buffer, 0, bytesRead );
      }

      buffer = null;
    } catch ( IOException e ) {
      e.printStackTrace();
    } finally {
      if ( output != null ) {
        try {
          output.close();
        } catch ( Exception e ) {
          e.printStackTrace();
        }
      }
    }
  }

  public synchronized void setRepository( final IUnifiedRepository repository ) {
    if ( repository == null ) {
      throw new IllegalArgumentException();
    }
    this.repository = repository;
  }

  /**
   * Why does this return a web service? Going directly to the IUnifiedRepository requires the following:
   * <p/>
   * <ul>
   * <li>PentahoSessionHolder setup including password and tenant ID. (The server doesn't even process passwords today--
   * it assumes that Spring Security processed it. This would require code changes.)</li>
   * <li>User must specify path to Jackrabbit files (i.e. system/jackrabbit).</li>
   * </ul>
   */
  protected synchronized IUnifiedRepository getRepository() throws ParseException {
    if ( repository != null ) {
      return repository;
    }

    final String NAMESPACE_URI = "http://www.pentaho.org/ws/1.0"; //$NON-NLS-1$
    final String SERVICE_NAME = "unifiedRepository"; //$NON-NLS-1$

    String urlString =
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_KEY" ),
            Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_URL_NAME" ), true, false ).trim();
    if ( urlString.endsWith( "/" ) ) {
      urlString = urlString.substring( 0, urlString.length() - 1 );
    }
    urlString = urlString + "/webservices/" + SERVICE_NAME + "?wsdl";

    URL url;
    try {
      url = new URL( urlString );
    } catch ( MalformedURLException e ) {
      throw new IllegalArgumentException( e );
    }

    Service service = Service.create( url, new QName( NAMESPACE_URI, SERVICE_NAME ) );
    IUnifiedRepositoryJaxwsWebService port = service.getPort( IUnifiedRepositoryJaxwsWebService.class );
    // http basic authentication
    ( (BindingProvider) port ).getRequestContext().put(
        BindingProvider.USERNAME_PROPERTY,
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_USERNAME_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_USERNAME_NAME" ), true, false ) );
    ( (BindingProvider) port ).getRequestContext().put(
        BindingProvider.PASSWORD_PROPERTY,
        getOptionValue( Messages.getInstance().getString( "CommandLineProcessor.INFO_OPTION_PASSWORD_KEY" ), Messages
            .getInstance().getString( "CommandLineProcessor.INFO_OPTION_PASSWORD_NAME" ), true, true ) );
    // accept cookies to maintain session on server
    ( (BindingProvider) port ).getRequestContext().put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );
    // support streaming binary data
    // TODO mlowery this is not portable between JAX-WS implementations
    // (uses com.sun)
    ( (BindingProvider) port ).getRequestContext().put( JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192 );
    SOAPBinding binding = (SOAPBinding) ( (BindingProvider) port ).getBinding();
    binding.setMTOMEnabled( true );
    final UnifiedRepositoryToWebServiceAdapter unifiedRepositoryToWebServiceAdapter =
        new UnifiedRepositoryToWebServiceAdapter( port );
    repository = unifiedRepositoryToWebServiceAdapter;
    return unifiedRepositoryToWebServiceAdapter;
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
  protected String getOptionValue( final String option, final boolean required, final boolean emptyOk ) throws
      ParseException {
    final String value = StringUtils.trim( commandLine.getOptionValue( option ) );
    if ( required && StringUtils.isEmpty( value ) ) {
      throw new ParseException( Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0001_MISSING_ARG",
          option ) );
    }
    if ( !emptyOk && StringUtils.isEmpty( value ) ) {
      throw new ParseException( Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0001_MISSING_ARG",
          option ) );
    }
    return value;
  }

  /**
   * Returns the option value from the command line
   * 
   * @param shortOption
   *          the single character option whose value should be returned (NOTE: {@code null} will be returned if the
   *          option was not provided)
   * @param longOption
   *          the string option whose value should be returned (NOTE: {@code null} will be returned if the option was
   *          not provided)
   * @param required
   *          indicates if the option is required
   * @param emptyOk
   *          indicates if a blank value is acceptable
   * @return the value provided from the command line, or {@code null} if none was provided
   * @throws ParseException
   *           indicates the required or non-blank value was not provided
   */
  protected String getOptionValue( final String shortOption, final String longOption, final boolean required,
      final boolean emptyOk ) throws ParseException {
    // first try the short option parameter
    String value = StringUtils.trim( commandLine.getOptionValue( shortOption ) );
    if ( StringUtils.isEmpty( value ) ) {
      // if its empty, try the long option
      value = StringUtils.trim( commandLine.getOptionValue( longOption ) );
    }
    if ( required && StringUtils.isEmpty( value ) ) {
      throw new ParseException( Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0001_MISSING_ARG",
          longOption ) );
    }
    if ( !emptyOk && StringUtils.isEmpty( value ) ) {
      throw new ParseException( Messages.getInstance().getErrorString( "CommandLineProcessor.ERROR_0001_MISSING_ARG",
          longOption ) );
    }
    return value;
  }

  /**
   * internal helper to write output file
   * 
   * @param message
   * @param logFile
   */
  private void writeFile( String message, String logFile ) {
    try {
      File file = new File( logFile );
      FileOutputStream fout = FileUtils.openOutputStream( file );
      IOUtils.copy( IOUtils.toInputStream( message ), fout );
      fout.close();
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }

  }

  /**
   *
   */
  protected static void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( Messages.getInstance().getString( "CommandLineProcessor.INFO_PRINTHELP_CMDLINE" ), Messages
        .getInstance().getString( "CommandLineProcessor.INFO_PRINTHELP_HEADER" ), options, Messages.getInstance()
        .getString( "CommandLineProcessor.INFO_PRINTHELP_FOOTER" ) );
  }
}
