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
 * Copyright 2011 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.plugin.services.importexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

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
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository2.unified.webservices.jaxws.IUnifiedRepositoryJaxwsWebService;
import org.pentaho.platform.repository2.unified.webservices.jaxws.UnifiedRepositoryToWebServiceAdapter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

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

/**
 * Handles the parsing of command line arguments and creates an import process
 * based upon them
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class CommandLineProcessor {
  private static final String API_REPO_FILES_IMPORT = "/api/repo/files/import";

  private static final Log log = LogFactory.getLog(CommandLineProcessor.class);

  private static final Options options = new Options();

  private static Exception exception;

  private CommandLine commandLine;

  private RequestType requestType;

  private IUnifiedRepository repository;

  private static enum RequestType {
    HELP, IMPORT, EXPORT, REST
  }

  private static boolean useRestService = true;

  private static Client client = null;
  static {
    // create the Options
    options.addOption("h", "help", false, "print this message");

    options.addOption("i", "import", false, "import");
    options.addOption("e", "export", false, "export");

    options.addOption("u", "username", true, "repository username");
    options.addOption("p", "password", true, "repository password");
    options.addOption("a", "url", true, "url of repository (e.g. http://localhost:8080/pentaho)");
    options.addOption("x", "source", true, "external system type (e.g. legacy-db, file-system)");
    options.addOption("type", true, "The type of content being imported\nfiles (default), metadata");
    options.addOption("f", "file-path", true, "Path to directory of files");
    options.addOption("c", "charset", true,
        "charset to use for the repository (characters from external systems converted to this charset)");
    options.addOption("l", "logfile", true, "full path and filename of logfile messages");

    // import only options
    options.addOption("m", "comment", true, "version comment (import only)");
    options.addOption("f", "path", true, "repository path to which to add imported files (e.g. /public) (import only)");
    // import only ACL additions
    options.addOption("o", "overwrite", true, "overwrite files (import only)");
    options.addOption("p", "permission", true, "apply ACL manifest permissions to files and folders  (import only)");
    options.addOption("r", "retainOwnership", true, "Retain ownership information  (import only)");
    options.addOption("w", "withManifest", true, "Export Manifest ACL file included  (export only)");

    // external
    options.addOption("ldrvr", "legacy-db-driver", true, "legacy database repository driver");
    options.addOption("lurl", "legacy-db-url", true, "legacy database repository url");
    options.addOption("luser", "legacy-db-username", true, "legacy database repository username");
    options.addOption("lpass", "legacy-db-password", true, "legacy database repository password");
    options.addOption("lchar", "legacy-db-charset", true, "legacy database repository character-set");

  
    // Legacy Service
    options.addOption("s", "legacy", true, "If True - Use the legacy import/export on local to BI Server)");
   
    //rest services
    options.addOption("r", "rest", false, "Use the REST (Default) version (not local to BI Server)");
    options.addOption("v", "service", true, "this is the REST service call e.g. acl, children, properties");

  }

  /**
   * How this class is executed from the command line. It will create an
   * instance of an {@link ImportProcessor} and initialize it base on the
   * options provided on the command line.
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception {
    String logFile = null;
    try {
      // reset the exception information
      exception = null;

      final CommandLineProcessor commandLineProcessor = new CommandLineProcessor(args);
      String legacy = commandLineProcessor.getOptionValue("legacy", false, true);
      logFile = commandLineProcessor.getOptionValue("logfile",false,true);
      useRestService = "false".equals(legacy) ? false : true;// default to new REST version if not provided
      // new service only
      switch (commandLineProcessor.getRequestType()) {
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
    } catch (ParseException parseException) {
      exception = parseException;
      System.err.println(parseException.getLocalizedMessage());
      printHelp();
    } catch (Exception e) {
      exception = e;
      e.printStackTrace();  
      log.error(e.getMessage(),e);
    }
  }

  /**
   * call FileResource REST service 
   * example: {path+}/children 
   * example: {path+}/parameterizable
   * example: {path+}/properties
   * example: /delete?params={fileid1, fileid2}
   * @throws ParseException
   */
  private void performREST() throws ParseException {
  
    String contextURL = getOptionValue("url", true, false);
    String path = getOptionValue("path", true, false);    
    String logFile = getOptionValue("logfile",false,true);    
    String exportURL = contextURL + "/api/repo/files/";
    if(path != null){
      String effPath = path.replaceAll("/", ":");
      exportURL += effPath;
    }
    String service = getOptionValue("service",true,false);
    String params = getOptionValue("params",false,true);
    exportURL +=  "/"+service;
    if(params != null){
      exportURL += "?params="+params;
    }
   
    initRestService();
    WebResource resource = client.resource(exportURL);
   
    // Response response
    Builder builder = resource.type(MediaType.APPLICATION_JSON).type(MediaType.TEXT_XML_TYPE);
    ClientResponse response = builder.put(ClientResponse.class);
    if (response != null && response.getStatus() == 200) {
      String filename = getOptionValue("file-path", true, false);
      
      String message = "REST Completed \n";
      message += "Response Status: "+response.getStatus(); 
      message += "\n";
    
        if(logFile !=null && !"".equals(logFile)){
          message +=  "REST File written to "+logFile;
          System.out.println(message);
          writeFile(message, logFile);
        } 
      }else {
        System.out.println("Response: "+response+ " status=" + response.getStatus());
      }
  }

  /**
   * Used only for REST Jersey calls
   * @throws ParseException
   */
  private void initRestService() throws ParseException {
    // get information about the remote connection
    String username = getOptionValue("username", true, false);
    String password = getOptionValue("password", true, false);
    ClientConfig clientConfig = new DefaultClientConfig();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = Client.create(clientConfig);
    client.addFilter(new HTTPBasicAuthFilter(username, password));
  }

  /**
   * Returns information about any exception encountered (if one was
   * generated)
   * 
   * @return the {@link Exception} that was generated, or {@code null} if none
   *         was generated
   */
  public static Exception getException() {
    return exception;
  }

  /**
   * Parses the command line and handles the situation where it isn't a valid
   * import or export or rest request
   * 
   * @param args
   *            the command line arguments
   * @throws ParseException
   *             indicates that neither (or both) an import and/or export have
   *             been request
   */
  protected CommandLineProcessor(String[] args) throws ParseException {
    // parse the command line arguments
    commandLine = new PosixParser().parse(options, args);
    if (commandLine.hasOption("help")) {
      requestType = RequestType.HELP;
    } else {
      if(commandLine.hasOption("rest")){
        requestType = RequestType.REST;
      } else {
        final boolean importRequest = commandLine.hasOption("import");
        final boolean exportRequest = commandLine.hasOption("export");
        if (importRequest == exportRequest) {
          throw new ParseException("exactly one of --import or --export is required");
        }
        requestType = (importRequest ? RequestType.IMPORT : RequestType.EXPORT);
      }
    }
  }

  // ========================== Instance Members / Methods
  // ==========================

  protected RequestType getRequestType() {
    return requestType;
  }

  protected void performImport() throws Exception, ImportException {
    if (!useRestService) {
      this.performImportLegacy();
    } else {
      performImportREST();
    }
  }

  /*
   * --import --url=http://localhost:8080/pentaho -
   *  -username=joe
   *  --password=password 
   *  --source=file-system --charset=UTF-8
   *  --path=:public 
   *  --file-path=C:/Users/tband/Downloads/pentaho-solutions.zip
   *  --logfile=c:/Users/tband/Desktop/logfile.log
   *  --permission=true 
   *  --overwrite=true 
   *  --retainOwnership=true
   *  (required fields- default is false)
   */

  private void performImportREST() throws ParseException, FileNotFoundException {
    String contextURL = getOptionValue("url", true, false);
    String path = getOptionValue("path", true, false);
    String filePath = getOptionValue("file-path", true, false);
    String charSet = getOptionValue("charset", false, true);
    String logFile = getOptionValue("logfile",false,true);
    String importURL = contextURL + API_REPO_FILES_IMPORT;
    File fileIS = new File(filePath);
    InputStream in = new FileInputStream(fileIS);
    initRestService();
    WebResource resource = client.resource(importURL);

    String overwrite = getOptionValue("overwrite", true, false);
    String retainOwnership = getOptionValue("retainOwnership", true, false);
    String permission = getOptionValue("permission", true, false);

    FormDataMultiPart part = new FormDataMultiPart();
    part.field("importDir", path, MediaType.MULTIPART_FORM_DATA_TYPE);
    part.field("overwriteAclPermissions", "true".equals(overwrite) ? "true" : "false", MediaType.MULTIPART_FORM_DATA_TYPE);
    part.field("retainOwnership", "true".equals(retainOwnership) ? "true" : "false", MediaType.MULTIPART_FORM_DATA_TYPE);
    part.field("charSet", charSet == null ? "UTF-8" : charSet);
    part.field("applyAclPermissions", "true".equals(permission) ? "true" : "false", MediaType.MULTIPART_FORM_DATA_TYPE).field(
        "fileUpload", in, MediaType.MULTIPART_FORM_DATA_TYPE);

    // If the import service needs the file name do the following.
    part.getField("fileUpload").setContentDisposition(
        FormDataContentDisposition.name("fileUpload").fileName(fileIS.getName()).build());

    // Response response
    ClientResponse  response = resource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class,part);
    if(response != null){
      String message = response.getEntity(String.class);
      System.out.println("done response=" + message);      
      if(logFile != null && !"".equals(logFile)){
          writeFile(message,logFile);
      }
    }
  }


  /**
   * this process must run on the same box as the JCR repository does not use
   * REST
   * 
   * @throws Exception
   *             ;
   * @throws ImportException
   */
  protected void performImportLegacy() throws Exception, ImportException {
    final ImportProcessor importProcessor = getImportProcessor();
    importProcessor.setImportSource(createImportSource());
    addImportHandlers(importProcessor);
    String overwrite = getOptionValue("overwrite", false, true);
    importProcessor.performImport(Boolean.valueOf(overwrite == null ? "true" : overwrite).booleanValue());
  }

  protected void performExport() throws ParseException, ExportException, IOException {
    if (!useRestService) {
      performExportLegacy();
    } else {
      performExportREST();
    }
  }

  /**
   * REST Service Export
   * 
   * @throws ParseException
   *             --export --url=http://localhost:8080/pentaho --username=joe
   *             --password=password --file-path=c:/temp/export.zip
   *             --charset=UTF-8 --path=public/pentaho-solutions/steel-wheels
   *             --logfile=c:/temp/steel-wheels.log --withManifest=true
   * @throws IOException
   */
  private void performExportREST() throws ParseException, IOException {
    String contextURL = getOptionValue("url", true, false);
    String path = getOptionValue("path", true, false);
    String withManifest = getOptionValue("withManifest", false, true);
    String effPath = path.replaceAll("/", ":");
    String logFile = getOptionValue("logfile",false,true);
    String exportURL = contextURL + "/api/repo/files/" + effPath + "/download?withManifest="
        + ("false".equals(withManifest) ? "false" : "true");
    initRestService();
    WebResource resource = client.resource(exportURL);
   
    // Response response
    Builder builder = resource.type(MediaType.MULTIPART_FORM_DATA).accept(MediaType.TEXT_HTML_TYPE);
    ClientResponse response = builder.get(ClientResponse.class);
    if (response != null && response.getStatus() == 200) {
      String filename = getOptionValue("file-path", true, false);
      final InputStream input = response.getEntityInputStream();
      createZipFile(filename, input);
      input.close();
      String message = "Export Completed \n";
      message += "Response Status: "+response.getStatus(); 
      message += "\n";
      message +=  "Export File written to "+filename;
      if(logFile !=null && !"".equals(logFile)){
        System.out.println(message);
        writeFile(message, logFile);
      }
    }
  }

  /**
   * create the zip file from the input stream
   * @param filename
   * @param input InputStream
   */
  private void createZipFile(String filename, final InputStream input) {
    OutputStream output = null;
    try {
      output = new FileOutputStream(filename);
      byte[] buffer = new byte[8 * 1024];
      int bytesRead;
      while ((bytesRead = input.read(buffer)) != -1) {
        output.write(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * 
   * @throws ParseException
   * @throws ExportException
   * @throws IOException
   */
  protected void performExportLegacy() throws ParseException, ExportException, IOException {
    final Exporter exportProcessor = getExportProcessor();
    exportProcessor.doExport();
    // throw new UnsupportedOperationException(); // TODO implement
  }

  private Exporter getExportProcessor() throws ParseException {
    final IUnifiedRepository repository = getRepository();
    String path = getOptionValue("path", true, false);
    String filepath = getOptionValue("file-path", true, false);
    final Exporter exportProcess = new Exporter(repository, path, filepath);
    return exportProcess;
  }

  /**
   * Determines the {@link ImportProcessor} to be used by evaluating the
   * command line
   * 
   * @return the @{link ImportProcessor} to be used for importing - or the
   *         {@link SimpleImportProcessor} if one can not be determined
   */
  protected ImportProcessor getImportProcessor() throws ParseException {
    final String comment = getOptionValue("comment", false, true);
    final String destinationPath = getOptionValue("path", true, false);
    return new SimpleImportProcessor(destinationPath, comment);
  }

  /**
   * Creates an instance of an {@link ImportSource} based off the command line
   * options
   */
  protected ImportSource createImportSource() throws ParseException, InitializationException {
    final String source = getOptionValue("source", true, false);
    final String requiredCharset = getOptionValue("charset", true, false);
    if (StringUtils.equals(source, "legacy-db")) {
      final String driver = getOptionValue("legacy-db-driver", true, false);
      final String url = getOptionValue("legacy-db-url", true, false);
      final String dbUsername = getOptionValue("legacy-db-username", true, false);
      final String dbPassword = getOptionValue("legacy-db-password", true, true);
      final String charset = getOptionValue("legacy-db-charset", true, true);
      final DataSource dataSource = new DriverManagerDataSource(driver, url, dbUsername, dbPassword);

      final String username = getOptionValue("username", true, false);
      return new org.pentaho.platform.plugin.services.importexport.legacy.DbSolutionRepositoryImportSource(dataSource,
          charset, requiredCharset, username);
    }

    if (StringUtils.equals(source, "file-system")) {
      final String filePath = getOptionValue("file-path", true, false);
      final File file = new File(filePath);
      return new org.pentaho.platform.plugin.services.importexport.legacy.FileSolutionRepositoryImportSource(file,
          requiredCharset);
    }

    // Source is not understood
    throw new ParseException("unknown source: " + source);
  }

  /**
   * Creates and adds the set of {@link ImportHandler}s to be used with this
   * import process
   * 
   * @param importProcessor
   *            the import processor in which the import handlers should be
   *            created
   */
  protected void addImportHandlers(final ImportProcessor importProcessor) throws ParseException {
    // TODO - Need a way to either (a) have all ImportProcessors use the
    // same set or (b) use spring to initialize this
    final IUnifiedRepository repository = getRepository();

    String username = getOptionValue("username", true, false);
    String password = getOptionValue("password", true, false);
    String url = getOptionValue("url", true, false);

    importProcessor.addImportHandler(new MondrianImportHandler(username, password, url));
    importProcessor.addImportHandler(new MetadataImportHandler(username, password, url));
    importProcessor.addImportHandler(new DefaultImportHandler(repository));
  }

  public void setRepository(final IUnifiedRepository repository) {
    if (repository == null) {
      throw new IllegalArgumentException();
    }
    this.repository = repository;
  }

  /**
   * Why does this return a web service? Going directly to the
   * IUnifiedRepository requires the following:
   * <p/>
   * <ul>
   * <li>PentahoSessionHolder setup including password and tenant ID. (The
   * server doesn't even process passwords today-- it assumes that Spring
   * Security processed it. This would require code changes.)</li>
   * <li>User must specify path to Jackrabbit files (i.e. system/jackrabbit).</li>
   * </ul>
   */
  protected synchronized IUnifiedRepository getRepository() throws ParseException {
    if (repository != null) {
      return repository;
    }

    final String NAMESPACE_URI = "http://www.pentaho.org/ws/1.0"; //$NON-NLS-1$
    final String SERVICE_NAME = "unifiedRepository"; //$NON-NLS-1$

    String urlString = getOptionValue("url", true, false).trim();
    if (urlString.endsWith("/")) {
      urlString = urlString.substring(0, urlString.length() - 1);
    }
    urlString = urlString + "/webservices/" + SERVICE_NAME + "?wsdl";

    URL url;
    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }

    Service service = Service.create(url, new QName(NAMESPACE_URI, SERVICE_NAME));
    IUnifiedRepositoryJaxwsWebService port = service.getPort(IUnifiedRepositoryJaxwsWebService.class);
    // http basic authentication
    ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY,
        getOptionValue("username", true, false));
    ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,
        getOptionValue("password", true, true));
    // accept cookies to maintain session on server
    ((BindingProvider) port).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
    // support streaming binary data
    // TODO mlowery this is not portable between JAX-WS implementations
    // (uses com.sun)
    ((BindingProvider) port).getRequestContext().put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
    SOAPBinding binding = (SOAPBinding) ((BindingProvider) port).getBinding();
    binding.setMTOMEnabled(true);
    final UnifiedRepositoryToWebServiceAdapter unifiedRepositoryToWebServiceAdapter = new UnifiedRepositoryToWebServiceAdapter(
        port);
    repository = unifiedRepositoryToWebServiceAdapter;
    return unifiedRepositoryToWebServiceAdapter;
  }

  /**
   * Returns the option value from the command line
   * 
   * @param option
   *            the option whose value should be returned (NOTE: {@code null}
   *            will be returned if the option was not provided)
   * @param required
   *            indicates if the option is required
   * @param emptyOk
   *            indicates if a blank value is acceptable
   * @return the value provided from the command line, or {@code null} if none
   *         was provided
   * @throws ParseException
   *             indicates the required or non-blank value was not provided
   */
  protected String getOptionValue(final String option, final boolean required, final boolean emptyOk)
      throws ParseException {
    final String value = StringUtils.trim(commandLine.getOptionValue(option));
    if (required && StringUtils.isEmpty(value)) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", option));
    }
    if (!emptyOk && StringUtils.isEmpty(value)) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", option));
    }
    return value;
  }

  /**
   * internal helper to write output file
   * @param message
   * @param logFile
   */
  private void writeFile(String message, String logFile) {
    try{
      File file = new File(logFile);
      FileOutputStream fout = FileUtils.openOutputStream(file);
      IOUtils.copy(IOUtils.toInputStream(message), fout);
      fout.close();
    } catch(Exception ex){
      ex.printStackTrace();
    }
   
  }
  /**
   * 
   */
  protected static void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("importexport", "Unified repository command line import/export tool", options,
        "Common options for import & export using REST Services"
            + "Example arguments for import:\n"
            + "--import --url=http://localhost:8080/pentaho --username=joe\n"
            + "--password=password --source=file-system --type=files --charset=UTF-8 --path=/public\n"
            + "--file-path=c:/temp/steel-wheels\n"
            + "--logfile=c:/temp/logfile.log\n" 
            + "--permission=true\n" + "--overwrite=true\n"
            + "--retainOwnership=true\n\n"      
             + "\n\n"
            + "Example arguments for export:\n"
            + "--export --url=http://localhost:8080/pentaho --username=joe --password=password \n"
            + "--file-path=c:/temp/export.zip --charset=UTF-8 --path=/public\n --withManifest=true"
            + "--logfile=c:/temp/logfile.log\n"
            + "Example arguments for running REST services:\n"
            + "--rest --url=http://localhost:8080/pentaho --username=joe --password=password \n"
            + " -path=/public/pentaho-solutions/steel-wheels/reports\n "
            + " --logfile=c:/temp/logfile.log --service=acl\n"
            + "Using the legacy import/export (on BI Server) - default uses REST\n"
            +" --legacy=true\n"
            + "");
  }
}