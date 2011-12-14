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
 */
package org.pentaho.platform.repository2.unified.importexport;

import com.sun.xml.ws.developer.JAXWSProperties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.importexport.legacy.DbSolutionRepositoryImportSource;
import org.pentaho.platform.repository2.unified.importexport.legacy.FileSolutionRepositoryImportSource;
import org.pentaho.platform.repository2.unified.webservices.jaxws.IUnifiedRepositoryJaxwsWebService;
import org.pentaho.platform.repository2.unified.webservices.jaxws.UnifiedRepositoryToWebServiceAdapter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Main {

  private static Options options;

  static {
    // create the Options
    options = new Options();
    options.addOption("h", "help", false, "print this message");

    options.addOption(OptionBuilder.withLongOpt("import").withDescription("import").create("i"));
    options.addOption(OptionBuilder.withLongOpt("export").withDescription("export").create("e"));

    options.addOption(OptionBuilder.withLongOpt("username").withDescription("repository username").hasArg()
        .withArgName("username").create("u"));
    options.addOption(OptionBuilder.withLongOpt("password").withDescription("repository password").hasArg()
        .withArgName("password").create("p"));
    options.addOption(OptionBuilder.withLongOpt("url").withDescription(
        "url of repository (e.g. http://localhost:8080/pentaho)").hasArg().withArgName("url").create("a"));
    options.addOption(OptionBuilder.withLongOpt("type").withDescription("external system type (e.g. legacy-db or file-system)")
        .hasArg().withArgName("type").create("x"));
    options.addOption(OptionBuilder.withLongOpt("file-path").withDescription("Path to directory of files").hasArg()
        .withArgName("file-path").create("f"));
    options.addOption(OptionBuilder.withLongOpt("charset").withDescription(
        "charset to use for the repository (characters from external systems converted to this charset)").hasArg()
        .withArgName("charset").create("c"));

    // import only options
    options.addOption(OptionBuilder.withLongOpt("comment").withDescription("version comment (import only)").hasArg()
        .withArgName("comment").create("m"));
    options.addOption(OptionBuilder.withLongOpt("path").withDescription(
        "repository path to which to add imported files (e.g. /public) (import only)").hasArg().withArgName("path")
        .create("f"));

    // external
    options.addOption(OptionBuilder.withArgName("property=value").hasArgs(2).withValueSeparator().withDescription(
        "name value pairs used by external systems").create("E"));

  }

  public static void main(final String[] args) {
    // create the command line parser
    CommandLineParser parser = new PosixParser();

    CommandLine line = null;
    try {
      // parse the command line arguments
      line = parser.parse(options, args);

      checkArgs(line);

      if (isImport(line)) {
        ImportSource importSource = getImportSource(line);
        IUnifiedRepository unifiedRepository = getUnifiedRepository(line);
        Map<String, Converter> converters = createConverters();

        Importer importer = new Importer(unifiedRepository, converters);
        addContentHandlers(importer, unifiedRepository);

        final String versionMessage = (line.hasOption("comment") ? line.getOptionValue("comment") : null);
        importer.doImport(importSource, line.getOptionValue("path"), versionMessage);
      } else if (isExport(line)) {
        IUnifiedRepository unifiedRepository = getUnifiedRepository(line);
        Exporter exporter = new Exporter(unifiedRepository, line.getOptionValue("path"), line.getOptionValue("file-path"));
        exporter.doExport();
      } else {
        throw new UnsupportedOperationException("not implemented");
      }
    } catch (ParseException e) {
      handleException(e);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private static void addContentHandlers(final Importer importer, final IUnifiedRepository repository) {
//    // Add the Pentaho Metadata Import Content Handlers
//    final PentahoMetadataImportContentHandler metadataHandler = new PentahoMetadataImportContentHandler();
//    final IMetadataDomainRepository metadataDomainRepository = new PentahoMetadataDomainRepositoryTest(repository);
//    metadataHandler.setDomainRepository(metadataDomainRepository);
//    metadataHandler.setXmiParser(new XmiParser());
//    importer.addImportContentHandler(100, metadataHandler);
//
//    // Add the default handler (it will go last) - it just copies files into the repository
//    importer.addImportContentHandler(Integer.MAX_VALUE, new DefaultImportContentHandler());
  }

  protected static Map<String, Converter> createConverters() {
    Map<String, Converter> converters = new HashMap<String, Converter>();
    StreamConverter streamConverter = new StreamConverter();
    converters.put("prpt", streamConverter);
    converters.put("mondrian.xml", streamConverter);
    converters.put("kjb", streamConverter);
    converters.put("ktr", streamConverter);
    converters.put("report", streamConverter);
    converters.put("rptdesign", streamConverter);
    converters.put("svg", streamConverter);
    converters.put("url", streamConverter);
    converters.put("xaction", streamConverter);
    converters.put("xanalyzer", streamConverter);
    converters.put("xcdf", streamConverter);
    converters.put("xdash", streamConverter);
    converters.put("xreportspec", streamConverter);
    converters.put("waqr.xaction", streamConverter);
    converters.put("xwaqr", streamConverter);
    converters.put("gif", streamConverter);
    converters.put("css", streamConverter);
    converters.put("html", streamConverter);
    converters.put("htm", streamConverter);
    converters.put("jpg", streamConverter);
    converters.put("jpeg", streamConverter);
    converters.put("js", streamConverter);
    converters.put("cfg.xml", streamConverter);
    converters.put("jrxml", streamConverter);
    converters.put("png", streamConverter);
    converters.put("properties", streamConverter);
    converters.put("sql", streamConverter);
    converters.put("xmi", streamConverter);
    converters.put("xml", streamConverter);
    return converters;
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
  private static IUnifiedRepository getUnifiedRepository(final CommandLine line) {
    final String NAMESPACE_URI = "http://www.pentaho.org/ws/1.0"; //$NON-NLS-1$
    final String SERVICE_NAME = "unifiedRepository"; //$NON-NLS-1$

    String urlString = line.getOptionValue("url").trim();
    if (urlString.endsWith("/")) {
      urlString = urlString.substring(0, urlString.length() - 1);
    }
    urlString = urlString + "/webservices/" + SERVICE_NAME + "?wsdl";

    URL url = null;
    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }

    Service service = Service.create(url, new QName(NAMESPACE_URI, SERVICE_NAME));
    IUnifiedRepositoryJaxwsWebService port = service.getPort(IUnifiedRepositoryJaxwsWebService.class);
    // http basic authentication
    ((BindingProvider) port).getRequestContext()
        .put(BindingProvider.USERNAME_PROPERTY, line.getOptionValue("username"));
    ((BindingProvider) port).getRequestContext()
        .put(BindingProvider.PASSWORD_PROPERTY, line.getOptionValue("password"));
    // accept cookies to maintain session on server
    ((BindingProvider) port).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
    // support streaming binary data
    // TODO mlowery this is not portable between JAX-WS implementations (uses com.sun)
    ((BindingProvider) port).getRequestContext().put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
    SOAPBinding binding = (SOAPBinding) ((BindingProvider) port).getBinding();
    binding.setMTOMEnabled(true);
    return new UnifiedRepositoryToWebServiceAdapter(port);
  }

  private static ImportSource getImportSource(final CommandLine line) throws ParseException {
    String type = line.getOptionValue("type");
    if ("legacy-db".equals(type.trim())) {
      Properties props = line.getOptionProperties("E");
      String driver = props.getProperty("legacy-db-driver");
      if (!StringUtils.hasLength(driver)) {
        throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG",
            "-E" + "legacy-db-driver"));
      }
      String url = props.getProperty("legacy-db-url");
      if (!StringUtils.hasLength(url)) {
        throw new ParseException(Messages.getInstance()
            .getString("Main.ERROR_0001_MISSING_ARG", "-E" + "legacy-db-url"));
      }
      String username = props.getProperty("legacy-db-username");
      if (!StringUtils.hasLength(username)) {
        throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG",
            "-E" + "legacy-db-username"));
      }
      String password = props.getProperty("legacy-db-password");
      // empty string is ok here
      if (password == null) {
        throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG",
            "-E" + "legacy-db-password"));
      }
      String charset = props.getProperty("legacy-db-charset");
      if (!StringUtils.hasLength(charset)) {
        throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG",
            "-E" + "legacy-db-charset"));
      }
      DataSource dataSource = new DriverManagerDataSource(driver, url, username, password);

      ImportSource importSource = new DbSolutionRepositoryImportSource(dataSource, charset);
      importSource.setRequiredCharset(line.getOptionValue("charset"));
      importSource.setOwnerName(line.getOptionValue("username"));
      return importSource;
    } else if ("file-system".equals(type.trim())) {
      File file = new File(line.getOptionValue("file-path"));
      ImportSource importSource = new FileSolutionRepositoryImportSource(file, line.getOptionValue("charset"));
      importSource.setOwnerName(line.getOptionValue("username"));
      return importSource;
    }
    throw new ParseException("unknown type");
  }

  private static boolean isImport(final CommandLine line) {
    return line.hasOption("import");
  }

  private static boolean isExport(final CommandLine line) {
    return line.hasOption("export");
  }

  private static void checkArgs(final CommandLine line) throws ParseException {

    if (line.hasOption("help")) {
      printHelp();
    }

    // either import or export but not both
    if ((line.hasOption("import") && line.hasOption("export"))
        || (!line.hasOption("import") && !line.hasOption("export"))) {
      throw new ParseException("exactly one of --import or --export is required");
    }

    // options that are always required
    if (!(line.hasOption("username"))) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", "username"));
    }
    if (!(line.hasOption("password"))) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", "password"));
    }
    if (!(line.hasOption("url"))) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", "url"));
    }
    if (!(line.hasOption("type"))) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", "type"));
    }
    if (!(line.hasOption("charset"))) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", "charset"));
    }
    if (!(line.hasOption("path"))) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", "path"));
    }

    // is it known type?
    String type = line.getOptionValue("type");
    if ("legacy-db".equals(type.trim()) || "file-system".equals(type.trim())) {
      return;
    }
    throw new ParseException("unknown type {0}");

  }

  private static void handleException(final ParseException e) {
    System.err.println(e.getLocalizedMessage());
    printHelp();
    System.exit(1);
  }

  private static void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("importexport", "Unified repository command line import/export tool", options,
        "Common options for legacy-db:\n" + "-Elegacy-db-driver=value\n" + "-Elegacy-db-url=value\n"
            + "-Elegacy-db-username=value\n" + "-Elegacy-db-password=value\n " + "-Elegacy-db-charset=value\n\n"
            + "Example arguments for legacy-db:\n"
            + "--import --url=http://localhost:8080/pentaho --username=joe --password=password "
            + "--type=legacy-db --charset=UTF-8 --path=/public -Elegacy-db-driver=com.mysql.jdbc.Driver "
            + "-Elegacy-db-url=jdbc:mysql://localhost/hibernate -Elegacy-db-username=hibuser "
            + "-Elegacy-db-password=password -Elegacy-db-charset=ISO-8859-1\n\n"
            + "Example arguments for File System import:\n"
            + "--import --url=http://localhost:8080/pentaho --username=joe\n"
            + "--password=password --type=file-system --charset=UTF-8 --path=/public\n"
            + "--file-path=/Users/wseyler/Desktop/steel-wheels\n");
  }

}
