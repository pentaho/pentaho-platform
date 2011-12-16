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
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.repository.pmd.PentahoMetadataDomainRepository;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.importexport.legacy.DbSolutionRepositoryImportSource;
import org.pentaho.platform.repository2.unified.importexport.legacy.FileSolutionRepositoryImportSource;
import org.pentaho.platform.repository2.unified.webservices.jaxws.IUnifiedRepositoryJaxwsWebService;
import org.pentaho.platform.repository2.unified.webservices.jaxws.UnifiedRepositoryToWebServiceAdapter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

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
    options.addOption(OptionBuilder.withLongOpt("source")
        .withDescription("external system type (e.g. legacy-db, file-system)")
        .hasArg().withArgName("source").create("x"));
    options.addOption("type", true, "The type of content being imported\nfiles (default), metadata");
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

    // options for metadata import
    options.addOption("domain", "domain", true,
        "The Pentaho Metadata Domain ID for this import item (--import --source=metadata)");
    options.addOption("locale", "locale", true,
        "The locale of the properties file being imported (overrides locale computed from filename, " +
            "--import --source=metadata only)");

    // external
    options.addOption(OptionBuilder.withArgName("property=value").hasArgs(2).withValueSeparator().withDescription(
        "name value pairs used by external systems").create("E"));

  }

  public static void main(final String[] args) {
    try {
      // parse the command line arguments
      final CommandLineParser parser = new PosixParser();
      final CommandLine line = parser.parse(options, args);

      if (line.hasOption("help")) {

        printHelp();

      } else if (line.hasOption("import")) {

        final ImportSource importSource = getImportSource(line);
        final IUnifiedRepository unifiedRepository = getUnifiedRepository(line);
        final String comment = getOptionValue(line, "comment", false, true);
        final Importer importer = getImporter(line, unifiedRepository);
        importer.doImport(importSource, comment, true);

      } else if (line.hasOption("export")) {

        IUnifiedRepository unifiedRepository = getUnifiedRepository(line);
        final String path = line.getOptionValue("path");
        final String filePath = line.getOptionValue("file-path");
        Exporter exporter = new Exporter(unifiedRepository, path, filePath);
        exporter.doExport();

      } else {
        throw new ParseException("exactly one of --import or --export is required");
      }
    } catch (ParseException e) {
      handleException(e);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static Importer getImporter(final CommandLine line, final IUnifiedRepository repository) throws ParseException {
    Importer importer = null;
    final String importType = getOptionValue(line, "type", "files");
    if (StringUtils.equals(importType, "files")) {
      final String path = getOptionValue(line, "path", true, false);
      importer = new FileImporter(repository, path, createConverters());
    } else if (StringUtils.equals(importType, "metadata")) {
      importer = new MetadataImporter(repository, new PentahoMetadataDomainRepository(repository));
    } else {
      throw new ParseException("Unknown type: " + importType);
    }
    return importer;
  }

  private static void addContentHandlers(final FileImporter fileImporter, final IUnifiedRepository repository) {
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
    final String source = getOptionValue(line, "source", true, false);

    if (StringUtils.equals(source, "legacy-db")) {
      Properties props = line.getOptionProperties("E");
      String driver = getProperty(props, "legacy-db-driver", "-E", true, false);
      String url = getProperty(props, "legacy-db-url", "-E", true, false);
      String username = getProperty(props, "legacy-db-username", "-E", true, false);
      String password = getProperty(props, "legacy-db-password", "-E", true, true);
      String charset = getProperty(props, "legacy-db-charset", "-E", true, true);
      DataSource dataSource = new DriverManagerDataSource(driver, url, username, password);

      ImportSource importSource = new DbSolutionRepositoryImportSource(dataSource, charset);
      importSource.setRequiredCharset(line.getOptionValue("charset"));
      importSource.setOwnerName(line.getOptionValue("username"));
      return importSource;
    }

    if (StringUtils.equals(source, "file-system")) {
      final String filePath = getOptionValue(line, "file-path", true, false);
      final String username = getOptionValue(line, "username", true, false);
      final String charset = getOptionValue(line, "charset", true, false);

      File file = new File(filePath);
      ImportSource importSource = new FileSolutionRepositoryImportSource(file, charset);
      importSource.setOwnerName(username);
      return importSource;
    }

    // Source is not understood
    throw new ParseException("unknown source: " + source);
  }

  private static String getProperty(final Properties props, final String key, final String prefix,
                                    final boolean required, final boolean emptyOk) throws ParseException {
    final String value = StringUtils.trim(props.getProperty(key));
    if (required && value == null) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", prefix + key));
    } else if (!emptyOk && StringUtils.isEmpty(value)) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", prefix + key));
    }
    return value.trim();
  }

  private static String getOptionValue(final CommandLine line, final String option,
                                       final boolean required, final boolean emptyOk) throws ParseException {
    final String value = StringUtils.trim(line.getOptionValue(option));
    if (required && StringUtils.isEmpty(value)) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", option));
    }
    if (!emptyOk && StringUtils.isEmpty(value)) {
      throw new ParseException(Messages.getInstance().getString("Main.ERROR_0001_MISSING_ARG", option));
    }
    return value;
  }

  private static String getOptionValue(final CommandLine line, final String option, final String defaultValue) {
    final String value = StringUtils.trim(line.getOptionValue(option));
    return (!StringUtils.isEmpty(value) ? value : defaultValue);
  }

  private static void handleException(final ParseException e) {
    System.err.println(e.getLocalizedMessage());
    printHelp();
  }

  private static void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("importexport", "Unified repository command line import/export tool", options,
        "Common options for legacy-db:\n" + "-Elegacy-db-driver=value\n" + "-Elegacy-db-url=value\n"
            + "-Elegacy-db-username=value\n" + "-Elegacy-db-password=value\n " + "-Elegacy-db-charset=value\n\n"
            + "Example arguments for legacy-db:\n"
            + "--import --url=http://localhost:8080/pentaho --username=joe --password=password "
            + "--source=legacy-db --charset=UTF-8 --path=/public -Elegacy-db-driver=com.mysql.jdbc.Driver "
            + "-Elegacy-db-url=jdbc:mysql://localhost/hibernate -Elegacy-db-username=hibuser "
            + "-Elegacy-db-password=password -Elegacy-db-charset=ISO-8859-1\n\n"
            + "Example arguments for File System import:\n"
            + "--import --url=http://localhost:8080/pentaho --username=joe\n"
            + "--password=password --source=file-system --type=files --charset=UTF-8 --path=/public\n"
            + "--file-path=/Users/wseyler/Desktop/steel-wheels\n\n"
            + "Example arguments for Metadata import:\n"
            + "--import --url=http://localhost:8080/pentaho --username=joe\n"
            + "--password=password --source=file-system --type=metadata --charset=UTF-8\n"
            + "--file-path=./pentaho-solutions/metadata\n");
  }

}
