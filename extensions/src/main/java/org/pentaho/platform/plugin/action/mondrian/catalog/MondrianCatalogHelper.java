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


package org.pentaho.platform.plugin.action.mondrian.catalog;

import com.google.common.annotations.VisibleForTesting;
import mondrian.i18n.LocalizingDynamicSchemaProcessor;
import mondrian.olap.Connection;
import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianException;
import mondrian.olap.Util;
import mondrian.olap.Util.PropertyList;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.spi.DynamicSchemaProcessor;
import mondrian.util.ClassResolver;
import mondrian.xmla.DataSourcesConfig;
import mondrian.xmla.DataSourcesConfig.DataSources;
import org.apache.commons.collections.list.SetUniqueList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.FileProvider;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.eigenbase.xom.DOMWrapper;
import org.eigenbase.xom.Parser;
import org.eigenbase.xom.XMLOutput;
import org.eigenbase.xom.XOMException;
import org.eigenbase.xom.XOMUtil;
import org.olap4j.OlapConnection;
import org.owasp.encoder.Encode;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException.Reason;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.repository.solution.filebased.MondrianVfs;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrAclNodeHelper;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.ANNOTATED_SCHEMA_FILE;
import static org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.ANNOTATIONS_FILE;
import static org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.SCHEMA_FILE;

/**
 * Reads in file containing Mondrian data sources and catalogs. (Contains code copied from <code>XmlaServlet</code>.)
 *
 * @author mlowery
 */
public class MondrianCatalogHelper implements IAclAwareMondrianCatalogService {

  public static final String MONDRIAN_DATASOURCE_FOLDER = "mondrian"; //$NON-NLS-1$
  public static final String SOLUTION_PREFIX = "solution:"; //$NON-NLS-1$
  public static final String MONDRIAN_CATALOG_CACHE_REGION = "mondrian-catalog-cache"; //$NON-NLS-1$
  private static final String MONDRIAN_FILE_PROVIDER_IDENTIFIER = "mondrian"; //$NON-NLS-1$
  private static final String MONDRIAN_URI_START = "mondrian:/";

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog( MondrianCatalogHelper.class );


  // ~ Error and warning messages ======================================================================================

  private static final String ERROR_MESSAGE_INVALID_DATASOURCE_CONFIG =
    "MondrianCatalogHelper.ERROR_0001_INVALID_DATASOURCE_CONFIG";
  private static final String ERROR_MESSAGE_FAILED_TO_PARSE_DATASOURCE_CONFIG =
    "MondrianCatalogHelper.ERROR_0002_FAILED_TO_PARSE_DATASOURCE_CONFIG";
  private static final String ERROR_MESSAGE_INSUFFICIENT_PERMISSIONS =
    "MondrianCatalogHelper.ERROR_0003_INSUFFICIENT_PERMISSION";
  private static final String ERROR_MESSAGE_ALREADY_EXISTS = "MondrianCatalogHelper.ERROR_0004_ALREADY_EXISTS";
  private static final String ERROR_MESSAGE_RESOURCE_NOT_AVAILABLE =
    "MondrianCatalogHelper.ERROR_0005_RESOURCE_NOT_AVAILABLE";
  private static final String ERROR_MESSAGE_IO_PROBLEM = "MondrianCatalogHelper.ERROR_0006_IO_PROBLEM";
  private static final String ERROR_MESSAGE_FILE_NOT_FOUND = "MondrianCatalogHelper.ERROR_0007_FILE_NOT_FOUND";
  private static final String ERROR_MESSAGE_ERROR_OCCURRED = "MondrianCatalogHelper.ERROR_0008_ERROR_OCCURRED";
  private static final String ERROR_MESSAGE_PARSING_CATALOG = "MondrianCatalogHelper.ERROR_0009_WHILE_PARSING_CATALOG";
  private static final String ERROR_MESSAGE_FILE_SYSTEM_PROBLEM = "MondrianCatalogHelper.ERROR_0012_FILESYSTEM_PROBLEM";
  private static final String ERROR_MESSAGE_FAILED_TO_LOAD_SCHEMA =
    "MondrianCatalogHelper.ERROR_0013_FAILED_TO_LOAD_SCHEMA";
  private static final String ERROR_MESSAGE_CATALOG_NOT_FOUND = "MondrianCatalogHelper.ERROR_0015_CATALOG_NOT_FOUND";
  private static final String ERROR_MESSAGE_IMPORT_SCHEMA = "MondrianCatalogHelper.ERROR_0018_IMPORT_SCHEMA_ERROR";
  private static final String ERROR_MESSAGE_FAILED_TO_FLUSH = "MondrianCatalogHelper.ERROR_0019_FAILED_TO_FLUSH";

  private static final String WARNING_MESSAGE_SKIPPING_DATASOURCE =
    "MondrianCatalogHelper.WARN_SKIPPING_DATASOURCE_DEF";

  // ~ Instance fields =================================================================================================

  private String dataSourcesConfig;

  /**
   * true to use schema name from catalog definition (aka schema file) as catalog name.
   */
  private boolean useSchemaNameAsCatalogName = true;

  /**
   * Holds the additional catalog information
   */
  private Map<String, MondrianCatalogComplementInfo> catalogComplementInfoMap;

  /**
   * Tells the MCH to generate datasources.xml with a DB name equals to that of the 4.X servers. This is necessary until
   * the next release of olap4j-xmla. Using the legacy DB name allows requests of MDSCHEMA_MEMBERS to be batched. See
   * MONDRIAN-2229.
   */
  private final boolean useLegacyDbName;

  private IAclNodeHelper aclHelper;

  @VisibleForTesting
  MondrianCatalogRepositoryHelper catalogRepositoryHelper;
  private final LocalizingDynamicSchemaProcessor localizingDynamicSchemaProcessor;
  private final IUnifiedRepository unifiedRepository;
  private final IOlapService olapService;

  // ~ Constructors ====================================================================================================

  public MondrianCatalogHelper( boolean useLegacyDbName ) {

    this( useLegacyDbName, null, null );
  }

  @VisibleForTesting
  @Deprecated
  public MondrianCatalogHelper( IAclNodeHelper aclHelper ) {
    this();
    this.aclHelper = aclHelper;
  }

  public MondrianCatalogHelper() {
    this( false );
  }

  public MondrianCatalogHelper( boolean useLegacyDbName, FileProvider mondrianFileProvider,
                                LocalizingDynamicSchemaProcessor localizingDynamicSchemaProcessor,
                                IUnifiedRepository unifiedRepository, IOlapService olapService ) {
    super();
    this.useLegacyDbName = useLegacyDbName;

    try {
      DefaultFileSystemManager dfsm = (DefaultFileSystemManager) VFS.getManager();
      var finalMondrianFileProvider = mondrianFileProvider;
      if ( mondrianFileProvider == null ) {
        finalMondrianFileProvider = new MondrianVfs();
      }

      if ( !dfsm.hasProvider( MONDRIAN_FILE_PROVIDER_IDENTIFIER ) ) {
        dfsm.addProvider( MONDRIAN_FILE_PROVIDER_IDENTIFIER, finalMondrianFileProvider ); //$NON-NLS-1$
      } else {
        logger.info( "The mondrian provider is already registered. Skipping registration." );
      }
    } catch ( FileSystemException e ) {
      logger.error( e.getMessage() );
    }

    this.localizingDynamicSchemaProcessor =
      localizingDynamicSchemaProcessor != null ? localizingDynamicSchemaProcessor
        : new LocalizingDynamicSchemaProcessor();

    if ( unifiedRepository != null ) {
      this.unifiedRepository = unifiedRepository;
    } else {
      this.unifiedRepository = PentahoSystem.get( IUnifiedRepository.class );
    }

    if ( olapService != null ) {
      this.olapService = olapService;
    } else {
      this.olapService = PentahoSystem.get( IOlapService.class );
    }
  }

  public MondrianCatalogHelper( boolean useLegacyDbName, FileProvider mondrianFileProvider,
                                LocalizingDynamicSchemaProcessor localizingDynamicSchemaProcessor ) {
    this( useLegacyDbName, mondrianFileProvider, localizingDynamicSchemaProcessor, null, null );
  }
  // ~ Methods =========================================================================================================

  /**
   * TODO Delete this method. Calling this method ties you to this implementation.
   *
   * @deprecated Please use PentahoSystem.get to get the Mondrian Catalog Service
   */
  @Deprecated
  public static MondrianCatalogHelper getInstance() {
    // IMondrianCatalogService is a singleton; IPentahoSession not required
    return (MondrianCatalogHelper) PentahoSystem
      .get( IMondrianCatalogService.class, "IMondrianCatalogService", null ); //$NON-NLS-1$
  }

  protected MondrianCatalog getCatalogFromCache( String context, IPentahoSession pentahoSession ) {
    // NOTE that the context can be the catalog name or the definition string for the catalog. If you are using the
    // definition string to
    // retrieve the catalog form the cache, you cannot be guaranteed what datasource is in play; so under these
    // circumstances, this catalog's
    // definition is the only part of the catalog that can be trusted. As this feature was added to enable looking up
    // Mondrian
    // roles from the schema, we don't much care which datasource is in play.

    MondrianCatalogCache mondrianCatalogCache =
      (MondrianCatalogCache) PentahoSystem.getCacheManager( pentahoSession ).getFromRegionCache(
        MONDRIAN_CATALOG_CACHE_REGION, getLocale().toString() );
    return mondrianCatalogCache.getCatalog( context );
  }

  @SuppressWarnings( "unchecked" )
  protected List<MondrianCatalog> getCatalogs( IPentahoSession pentahoSession ) {

    MondrianCatalogCache mondrianCatalogCache =
      (MondrianCatalogCache) PentahoSystem.getCacheManager( pentahoSession ).getFromRegionCache(
        MONDRIAN_CATALOG_CACHE_REGION, getLocale().toString() );

    List<MondrianCatalog> catalogs = new ArrayList<>();
    if ( mondrianCatalogCache != null ) {
      catalogs.addAll( mondrianCatalogCache.getCatalogs().values() );
    }
    // Sort
    Collections.sort( catalogs, ( o1, o2 ) -> o1.getName().compareTo( o2.getName() ) );
    // remove duplicates
    return SetUniqueList.decorate( catalogs );
  }

  /**
   * Add a catalog to the Mondrian Catalog Service.
   *
   * @param schemaSolutionPath the solution path to the schema definition, e.g. /etc/mondrian/XXX/schema.xml
   * @param session            Pentaho session
   * @param jndiName           the JNDI name of the datasource to use for this catalog, e.g. java:comp/env/jdbc/XXX
   * @param overwrite          flag to indicate if the catalog should be overwritten if it already exists
   * @return 0 is success, -1 if an error occurred
   */
  public static int addToCatalog( String schemaSolutionPath,
                                  IPentahoSession session, String jndiName, boolean overwrite ) {
    IMondrianCatalogService mondrianCatalogService =
      PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService", session ); //$NON-NLS-1$

    String catDef = SOLUTION_PREFIX + schemaSolutionPath; //$NON-NLS-1$

    MondrianSchema mondrianSchema = mondrianCatalogService.loadMondrianSchema( catDef, session );

    String catName = mondrianSchema.getName();
    String[] roleNames = mondrianSchema.getRoleNames();

    // verify JNDI
    try {
      IDBDatasourceService datasourceService = PentahoSystem.getObjectFactory().get( IDBDatasourceService.class, null );
      datasourceService.getDSBoundName( jndiName );
    } catch ( ObjectFactoryException objface ) {
      Logger
        .error(
          "MondrianCatalogHelper", Messages.getInstance()
            .getErrorString( "MondrianCatalogPublisher.ERROR_0006_UNABLE_TO_FACTORY_OBJECT", jndiName ),
          objface );
    } catch ( DBDatasourceServiceException dse ) {
      Logger
        .error(
          "MondrianCatalogHelper",
          Messages.getInstance()
            .getErrorString( "MondrianCatalogPublisher.ERROR_0001_JNDI_NAMING_ERROR", jndiName ),
          dse );
      return -1;
    }

    // used in both the catalog and the catalog datasource
    // Note: we use the unbound JNDI name here, the PentahoXmlaServlet resolves the JNDI name

    String catConnectStr = "Provider=mondrian;DataSource=" + jndiName; //$NON-NLS-1$

    // MB - 12/2009 - TODO: Figure out the empty list
    // Curious why we aren't adding the cubes from the read schema into the created schema.
    MondrianCatalog cat =
      new MondrianCatalog( catName, catConnectStr, catDef, new MondrianSchema( catName,
        new ArrayList<>(), roleNames ) );

    try {
      mondrianCatalogService.addCatalog( cat, overwrite, session );
    } catch ( MondrianCatalogServiceException e ) {
      Logger
        .error(
          "MondrianCatalogHelper",
          Messages.getInstance().getErrorString( "MondrianCatalogPublisher.ERROR_0002_EXCEPTION_OCCURRED" ),
          e );
      return -1;
    }

    return 0;
  }


  /**
   * Add a catalog to the Mondrian Catalog Service.
   *
   * @param baseUrl            the base URL for the XMLA service, e.g. http://localhost:8080/pentaho/
   * @param enableXmla         Deprecated, use the baseUrl to determine if XMLA is enabled
   * @param schemaSolutionPath the solution path to the schema definition, e.g. /etc/mondrian/XXX/schema.xml
   * @param session            Pentaho session
   * @param jndiName           the JNDI name of the datasource to use for this catalog, e.g. java:comp/env/jdbc/XXX
   * @param overwrite          flag to indicate if the catalog should be overwritten if it already exists
   * @return 0 is success, -1 if an error occurred
   * @deprecated Use {@link #addToCatalog(String, IPentahoSession, String, boolean)} instead.
   */
  @Deprecated( forRemoval = true )
  public static int addToCatalog( String baseUrl, boolean enableXmla, String schemaSolutionPath,
                                  IPentahoSession session, String jndiName, boolean overwrite ) {
    return addToCatalog( schemaSolutionPath, session, jndiName, overwrite );
  }

  /**
   * This method will conditionally load the catalogs into the cache, depending on the internal flag to check if it
   * is fully loaded,
   * It is synchronized to prevent multiple threads from trying to load the catalogs
   *
   * @param pentahoSession the pentaho session where the cache is stored
   */
  synchronized void initIfNotFullyLoaded( final IPentahoSession pentahoSession ) {
    // First check if the catalogs are initialized and fully loaded for the current locale
    MondrianCatalogCache mondrianCatalogCache = getCacheForRegion( pentahoSession );
    if ( mondrianCatalogCache.getMondrianCatalogCacheState().isFullyLoaded() ) {
      return;
    }

    if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
      MondrianCatalogHelper.logger.debug( "init" ); //$NON-NLS-1$
    }
    // By default, we will use the system to load all schemas into the cache.
    // access to these schemas is controlled later via the hasAccess() method
    loadCatalogsIntoCache( makeDataSources(), PentahoSessionHolder.getSession() );
  }

  @Override
  public synchronized void reInit( final IPentahoSession pentahoSession ) {
    final ICacheManager cacheMgr = PentahoSystem.getCacheManager( pentahoSession );
    if ( cacheMgr.cacheEnabled( MONDRIAN_CATALOG_CACHE_REGION ) ) {
      cacheMgr.clearRegionCache( MONDRIAN_CATALOG_CACHE_REGION );
    }
    initIfNotFullyLoaded( pentahoSession );
  }

  private static Locale getLocale() {
    final Locale locale = LocaleHelper.getLocale();
    if ( locale != null ) {
      return locale;
    } else {
      return Locale.getDefault();
    }
  }

  /**
   * Performs a search for an existing catalog based on the datasource info and catalog definition.
   *
   * @param catalog        The catalog to compare against
   * @param pentahoSession The session with which this request is associated (Used to locate the cache)
   * @return True if an existing match has been found for the catalog
   */
  protected boolean catalogExists( MondrianCatalog catalog, IPentahoSession pentahoSession ) {
    if ( catalog != null ) {
      MondrianCatalog foundCatalog = getCatalogFromCache( catalog.getName(), pentahoSession );
      // Was the catalog found by name?
      if ( foundCatalog != null ) {
        // first check dataSourceInfo
        String foundDataSourceInfo = cleanseDataSourceInfo( foundCatalog.getDataSourceInfo() );
        String newDataSourceInfo = cleanseDataSourceInfo( catalog.getDataSourceInfo() );

        if ( !foundDataSourceInfo.equals( newDataSourceInfo ) ) {
          return false;
        }

        // now check definition
        String foundDefinition = foundCatalog.getDefinition();
        String newDefinition = catalog.getDefinition();

        return definitionEquals( foundDefinition, newDefinition );
      }
    }
    return false;
  }

  /**
   * Same as implemented in <code>XmlaServlet</code> except takes advantage of Spring's Resource framework.
   */
  protected DataSourcesConfig.DataSources makeDataSources() {
    try {
      URL dataSourcesConfigUrl = null;

      if ( dataSourcesConfig == null ) {
        String datasourcesXML =
          generateInMemoryDatasourcesXml( PentahoSystem.get( IUnifiedRepository.class, PentahoSessionHolder
            .getSession() ) );
        return parseDataSources( datasourcesXML );
      } else if ( dataSourcesConfig.startsWith( "file:" ) ) { //$NON-NLS-1$
        dataSourcesConfigUrl = new URL( dataSourcesConfig ); // dataSourcesConfigResource.getURL();
        return parseDataSourcesUrl( dataSourcesConfigUrl );
      } else if ( dataSourcesConfig.startsWith( "classpath:" ) ) { //$NON-NLS-1$
        dataSourcesConfigUrl = getClass().getResource( dataSourcesConfig.substring( 10 ) );
        return ( dataSourcesConfigUrl == null ) ? null : parseDataSourcesUrl( dataSourcesConfigUrl );
      } else {
        throw new MondrianCatalogServiceException( "dataSourcesConfig is not a valid URL or does not exist",
          //$NON-NLS-1$
          Reason.GENERAL );
      }
    } catch ( IOException e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0001_INVALID_DATASOURCE_CONFIG", dataSourcesConfig ), //$NON-NLS-1$
        e, Reason.GENERAL );
    }
  }

  @VisibleForTesting
  protected String contextPathFromRequestContextHolder() {
    return PentahoRequestContextHolder.getRequestContext().getContextPath();
  }

  public String generateInMemoryDatasourcesXml( IUnifiedRepository unifiedRepository ) {
    String etcMondrian =
      ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + MONDRIAN_DATASOURCE_FOLDER;
    RepositoryFile etcMondrianFolder = unifiedRepository.getFile( etcMondrian );

    if ( etcMondrianFolder == null ) {
      return null;
    }
    StringBuffer datasourcesXML = new StringBuffer();
    datasourcesXML.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<DataSources>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<DataSource>\n" ); //$NON-NLS-1$
    if ( useLegacyDbName ) {
      datasourcesXML.append( "<DataSourceName>Provider=Mondrian</DataSourceName>\n" ); //$NON-NLS-1$
    } else {
      datasourcesXML.append( "<DataSourceName>Pentaho Mondrian</DataSourceName>\n" ); //$NON-NLS-1$
    }
    datasourcesXML
      .append( "<DataSourceDescription>Pentaho BI Platform Datasources</DataSourceDescription>\n" ); //$NON-NLS-1$
    datasourcesXML.append(
      "<URL>" + contextPathFromRequestContextHolder() + "Xmla</URL>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<DataSourceInfo>Provider=Mondrian</DataSourceInfo>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<ProviderName>PentahoXMLA</ProviderName>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<ProviderType>MDP</ProviderType>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<AuthenticationMode>Unauthenticated</AuthenticationMode>\n" ); //$NON-NLS-1$

    appendCatalogsSection( unifiedRepository, etcMondrianFolder, datasourcesXML );
    datasourcesXML.append( "</DataSource>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "</DataSources>\n" ); //$NON-NLS-1$

    return datasourcesXML.toString();
  }

  protected DataSourcesConfig.DataSources parseDataSourcesUrl( final URL dataSourcesConfigUrl ) {

    try {
      String dataSourcesConfigString = readDataSourcesContent( dataSourcesConfigUrl );
      return parseDataSources( dataSourcesConfigString );

    } catch ( Exception e ) {
      throw Util
        .newError(
          e,
          Messages
            .getInstance()
            .getErrorString(
              ERROR_MESSAGE_FAILED_TO_PARSE_DATASOURCE_CONFIG,
              dataSourcesConfigUrl.toExternalForm() ) );
    }
  }

  protected String readDataSourcesContent( final URL dataSourcesConfigUrl ) throws IOException {
    return Util.readURL( dataSourcesConfigUrl, Util.toMap( System.getProperties() ) );
  }

  protected DataSourcesConfig.DataSources parseDataSources( final String dataSourcesConfigString ) {

    try {
      if ( dataSourcesConfigString == null ) {
        MondrianCatalogHelper.logger.warn( Messages.getInstance().getString(
          "MondrianCatalogHelper.WARN_PARSE_NULL_INPUT" ) ); //$NON-NLS-1$
        return null;
      }
      String replacedConfigString =
        Util.replaceProperties( dataSourcesConfigString, Util.toMap( System.getProperties() ) );

      if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
        String msg = "parseDataSources: dataSources=" + replacedConfigString; //$NON-NLS-1$
        MondrianCatalogHelper.logger.debug( msg );
      }
      final Parser parser = XOMUtil.createDefaultParser();
      final DOMWrapper doc = parser.parse( replacedConfigString );
      catalogComplementInfoMap = makeCatalogComplementInfoMap( doc );
      return new DataSourcesConfig.DataSources( doc );

    } catch ( XOMException e ) {
      throw Util.newError( e, Messages.getInstance()
        .getErrorString( ERROR_MESSAGE_FAILED_TO_PARSE_DATASOURCE_CONFIG,
          dataSourcesConfigString ) );
    }
  }

  protected Map<String, MondrianCatalogComplementInfo> makeCatalogComplementInfoMap( final DOMWrapper doc ) {

    HashMap<String, MondrianCatalogComplementInfo> map = new HashMap<>();

    if ( doc.getElementChildren().length > 0 ) {

      DOMWrapper dataSource = doc.getElementChildren()[ 0 ];
      DOMWrapper catalogs = null;

      // Search Catalogs
      for ( int i = 0; i < dataSource.getElementChildren().length; i++ ) {
        DOMWrapper element = dataSource.getElementChildren()[ i ];
        if ( element.getTagName().equals( "Catalogs" ) ) { //$NON-NLS-1$
          catalogs = element;
          break;
        }
      }

      // Generate the map. We need the name and the variables
      for ( int i = 0; i < catalogs.getElementChildren().length; i++ ) {
        final DOMWrapper catalog = catalogs.getElementChildren()[ i ];
        if ( !"Catalog".equals( catalog.getTagName() ) ) { //$NON-NLS-1$
          continue;
        }

        MondrianCatalogComplementInfo complementInfo = new MondrianCatalogComplementInfo();

        // Iterate through whereConditions
        for ( int j = 0; j < catalog.getElementChildren().length; j++ ) {
          final DOMWrapper whereNode = catalog.getElementChildren()[ j ];
          if ( "WhereCondition".equals( whereNode.getTagName() ) ) { //$NON-NLS-1$
            complementInfo.addWhereCondition( whereNode.getAttribute( "cube" ), whereNode.getText() ); //$NON-NLS-1$
          }
        }

        map.put( getDOMWrapperElementText( catalog, "Definition" ), complementInfo ); //$NON-NLS-1$
      }

    }

    return map;
  }

  private String getDOMWrapperElementText( final DOMWrapper element, final String name ) {

    for ( int i = 0; i < element.getElementChildren().length; i++ ) {
      DOMWrapper child = element.getElementChildren()[ i ];
      if ( child.getTagName().equals( name ) ) {
        return child.getText();
      }
    }

    return null;
  }

  protected Map<String, MondrianCatalog> makeCatalogMap( final List<MondrianCatalog> cats ) {
    Map<String, MondrianCatalog> map = new HashMap<>();
    for ( MondrianCatalog catalog : cats ) {
      map.put( catalog.getName(), catalog );
    }
    return map;
  }

  /**
   * Equals that is tolerant of inconsistencies in solution path (leading slash vs. no leading slash).
   */
  private boolean definitionEquals( final String def1, final String def2 ) {
    if ( def1.equals( def2 ) ) {
      return true;
    }
    String tmp =
      def1.startsWith( SOLUTION_PREFIX + "/" ) ? SOLUTION_PREFIX + def1.substring( 10 )
        : SOLUTION_PREFIX + "/" + def1.substring( 9 ); //$NON-NLS-1$
    return tmp.equals( def2 );
  }

  protected String cleanseDataSourceInfo( String dataSourceInfo ) {
    if ( dataSourceInfo == null ) {
      return null;
    }
    // remove EnableXmla if necessary before building the key
    PropertyList propertyList = Util.parseConnectString( dataSourceInfo );
    if ( propertyList.get( "EnableXmla" ) != null ) { //$NON-NLS-1$
      propertyList.remove( "EnableXmla" ); //$NON-NLS-1$
    }
    if ( propertyList.get( "overwrite" ) != null ) { //$NON-NLS-1$
      propertyList.remove( "overwrite" ); //$NON-NLS-1$
    }
    return propertyList.toString();
  }

  public synchronized String getDataSourcesConfig() {
    return dataSourcesConfig;
  }

  public synchronized void setDataSourcesConfig( final String dataSourcesConfig ) {
    this.dataSourcesConfig = dataSourcesConfig;
  }

  @Override
  public List<MondrianCatalog> listCatalogs( final IPentahoSession pentahoSession, final boolean jndiOnly ) {
    if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
      MondrianCatalogHelper.logger.debug( "listCatalogs" ); //$NON-NLS-1$
    }
    initIfNotFullyLoaded( pentahoSession );

    // defensive copy
    return Collections.unmodifiableList( filter( getCatalogs( pentahoSession ), jndiOnly ) );
  }

  /**
   * use the in memory session value of input stream (used by test harness)
   */
  @Override
  public synchronized void addCatalog( final MondrianCatalog catalog, final boolean overwrite,
                                       final IPentahoSession pentahoSession ) throws MondrianCatalogServiceException {
    String mondrianSchema = (String) pentahoSession.getAttribute( "MONDRIAN_SCHEMA_XML_CONTENT" ); //$NON-NLS-1$
    InputStream schemaInputStream = IOUtils.toInputStream( mondrianSchema );
    addCatalog( schemaInputStream, catalog, overwrite, pentahoSession );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addCatalog( InputStream inputStream, MondrianCatalog catalog, boolean overwriteInRepository,
                          IPentahoSession session ) {
    addCatalog( inputStream, catalog, overwriteInRepository, null, session );
  }

  /**
   * new method to pass the input stream directly from data access put and post schema
   *
   * @param schemaInputStream
   * @param catalog
   * @param overwrite
   * @param acl               catalog ACL
   * @param pentahoSession
   * @throws MondrianCatalogServiceException
   */
  @Override
  public synchronized void addCatalog( InputStream schemaInputStream, final MondrianCatalog catalog,
                                       final boolean overwrite, RepositoryFileAcl acl,
                                       final IPentahoSession pentahoSession )
    throws MondrianCatalogServiceException {
    if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
      MondrianCatalogHelper.logger.debug( "addCatalog" ); //$NON-NLS-1$
    }

    // if cache was not initialized, do so now
    initIfNotFullyLoaded( pentahoSession );

    // check for existing dataSourceInfo+catalog
    final boolean catalogExistsWithSameDatasource = catalogExists( catalog, pentahoSession );
    if ( catalogExistsWithSameDatasource && !overwrite ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        ERROR_MESSAGE_ALREADY_EXISTS ), Reason.ALREADY_EXISTS ); //$NON-NLS-1$
    }


    // Checks if a catalog of the same name but with a different file
    // path exists.
    MondrianCatalog fileLocationCatalogTest = null;
    for ( MondrianCatalog currentCatalogCheck : getCatalogs( pentahoSession ) ) {
      if ( currentCatalogCheck.getName().equals( catalog.getName() ) ) {
        fileLocationCatalogTest = currentCatalogCheck;
        break;
      }
    }
    //compare the catalog names and throw exception if same and NOT overwrite
    final boolean catalogExistsWithDifferentDatasource;
    try {
      catalogExistsWithDifferentDatasource =
        fileLocationCatalogTest != null
          && definitionEquals(
          fileLocationCatalogTest.getDefinition(),
          MONDRIAN_URI_START
            + URLEncoder.encode( catalog.getName(), "UTF-8" ) );
    } catch ( UnsupportedEncodingException e ) {
      throw new MondrianCatalogServiceException( e );
    }

    if ( catalogExistsWithDifferentDatasource && !overwrite ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        ERROR_MESSAGE_ALREADY_EXISTS ), //$NON-NLS-1$
        Reason.XMLA_SCHEMA_NAME_EXISTS );
    }

    // Save the schema definition to the repository
    MondrianCatalogRepositoryHelper helper = getMondrianCatalogRepositoryHelper();
    try {
      helper.addHostedCatalog( schemaInputStream, catalog.getName(), catalog.getDataSourceInfo() );
    } catch ( Exception e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        ERROR_MESSAGE_ERROR_OCCURRED ), //$NON-NLS-1$
        Reason.valueOf( e.getMessage() ) );
    }

    // Load the catalog into the cache. The overload with the 'overwrite' flag is used here
    // to ensure that existing cache entries are replaced if 'overwrite' is true.
    loadCatalogIntoCache( catalog.getName(), pentahoSession, overwrite );

    try {
      setAclFor( catalog.getName(), acl );

      if ( catalogExistsWithSameDatasource || catalogExistsWithDifferentDatasource ) {
        flushCacheForCatalog( catalog.getName(), pentahoSession );
      }
    } catch ( MondrianException e ) {
      helper.deleteHostedCatalog( catalog.getName() );
      reInit( pentahoSession );
      throw e;
    }
  }

  protected void flushCacheForCatalog( String catalogName, IPentahoSession pentahoSession ) {
    Connection unwrap = null;
    try {
      OlapConnection connection = olapService.getConnection( catalogName, pentahoSession );
      unwrap = connection.unwrap( Connection.class );
      unwrap.getCacheControl( null ).flushSchema( unwrap.getSchema() );
    } catch ( Throwable e ) {
      MondrianCatalogHelper.logger.warn(
        Messages.getInstance().getErrorString(
            ERROR_MESSAGE_FAILED_TO_FLUSH, catalogName ), e );
    } finally {
      if ( unwrap != null ) {
        unwrap.close();
      }
    }
    removeHelpGeneratorCache( catalogName, pentahoSession );
  }

  @VisibleForTesting
  protected void appendCatalogsSection( IUnifiedRepository unifiedRepository,
                                        RepositoryFile etcMondrianFolder, StringBuffer datasourcesXML ) {
    datasourcesXML.append( "<Catalogs>\n" ); //$NON-NLS-1$
    // Creates <Catalogs> from the "/etc/mondrian/<catalog>/metadata" nodes.
    /*
     * IPentahoSession pentahoSession = PentahoSessionHolder.getSession(); String tenantEtcFolder = null;
     * if(pentahoSession != null) { String tenantId = (String)
     * pentahoSession.getAttribute(IPentahoSession.TENANT_ID_KEY); tenantEtcFolder =
     * ServerRepositoryPaths.getTenantEtcFolderPath(tenantId); } else { tenantEtcFolder =
     * ServerRepositoryPaths.getTenantEtcFolderPath(); }
     */
    List<RepositoryFile> mondrianCatalogs = unifiedRepository.getChildren( etcMondrianFolder.getId() );

    for ( RepositoryFile catalog : mondrianCatalogs ) {
      String catalogName = catalog.getName();

      var catalogFromRepo = getMondrianCatalogRepositoryHelper().getHostedCatalogInfo( catalogName );
      if ( catalogFromRepo != null ) {
        datasourcesXML
          .append( "<Catalog name=\"" + Encode.forXml( catalogName ) + "\">\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        datasourcesXML
          .append( "<DataSourceInfo>" + Encode.forXml( catalogFromRepo.dataSourceInfo )
            + "</DataSourceInfo>\n" ); //$NON-NLS-1$
        datasourcesXML.append(
          "<Definition>" + Encode.forXml( catalogFromRepo.definition )
            + "</Definition>\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        datasourcesXML.append( "</Catalog>\n" ); //$NON-NLS-1$
      } else {
        logger
          .warn( Messages.getInstance().getString( "MondrianCatalogHelper.WARN_META_DATA_IS_NULL" ) ); //$NON-NLS-1$
      }
    }

    datasourcesXML.append( "</Catalogs>\n" ); //$NON-NLS-1$
  }

  protected synchronized IAclNodeHelper getAclHelper() {
    if ( aclHelper == null ) {
      aclHelper = new JcrAclNodeHelper( unifiedRepository );
    }
    return aclHelper;
  }

  public synchronized void setAclHelper( IAclNodeHelper helper ) {
    aclHelper = helper;
  }

  @Override
  public void setAclFor( String catalogName, RepositoryFileAcl acl ) {
    getAclHelper().setAclFor( getMondrianCatalogRepositoryHelper().getMondrianCatalogFile( catalogName ), acl );
  }

  @Override
  public RepositoryFileAcl getAclFor( String catalogName ) {
    return getAclHelper().getAclFor( getMondrianCatalogRepositoryHelper().getMondrianCatalogFile( catalogName ) );
  }

  protected synchronized MondrianCatalogRepositoryHelper getMondrianCatalogRepositoryHelper() {
    if ( catalogRepositoryHelper == null ) {
      catalogRepositoryHelper =
        new MondrianCatalogRepositoryHelper( unifiedRepository );
    }
    return catalogRepositoryHelper;
  }

  @Deprecated
  protected synchronized void writeDataSources( DataSources dataSources ) {

    File dataSourcesFile;
    try {
      dataSourcesFile = new File( new URL( dataSourcesConfig ).getFile() ); // dataSourcesConfigResource.getFile();
    } catch ( IOException e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        ERROR_MESSAGE_RESOURCE_NOT_AVAILABLE ), e, Reason.GENERAL ); //$NON-NLS-1$
    }

    Writer sxml;
    try {
      sxml = new FileWriter( dataSourcesFile );
    } catch ( IOException e ) {
      throw new MondrianCatalogServiceException( e );
    }
    StringWriter sw = new StringWriter();
    XMLOutput pxml = new XMLOutput( sw );
    pxml.print( "<?xml version=\"1.0\"?>\n" ); //$NON-NLS-1$
    dataSources.displayXML( pxml, 0 );
    Document doc = null;
    try {
      doc = XmlDom4JHelper.getDocFromString( sw.toString(), new PentahoEntityResolver() );
    } catch ( XmlParseException e ) {
      throw new MondrianCatalogServiceException( e );
    }

    // pretty print
    try {
      OutputFormat format = OutputFormat.createPrettyPrint();
      format.setEncoding( doc.getXMLEncoding() );
      XMLWriter writer = new XMLWriter( sxml, format );
      writer.write( doc );
      writer.close();
      // CleanXmlHelper.saveDomToWriter(doc, sxml);
    } catch ( IOException e ) {
      throw new MondrianCatalogServiceException( e );
    }

    IOUtils.closeQuietly( sxml );
  }

  @Override
  public MondrianCatalog getCatalog( final String context, final IPentahoSession pentahoSession ) {
    if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
      MondrianCatalogHelper.logger.debug( "getCatalog" ); //$NON-NLS-1$
    }

    loadCatalogIntoCache( context, pentahoSession, false );
    MondrianCatalog cat = getCatalogFromCache( context, pentahoSession );
    if ( null != cat ) {
      if ( hasAccess( cat, RepositoryFilePermission.READ ) ) {
        return cat;
      } else {
        if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
          MondrianCatalogHelper.logger.debug( "user requested catalog with name \"" + context //$NON-NLS-1$
            + "\", but had insufficient privileges; returning null" ); //$NON-NLS-1$
        }
        return null;
      }
    } else {
      if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
        MondrianCatalogHelper.logger
          .debug( "user requested catalog with name\"" + context
            + "\", but catalog doesn't exist" ); //$NON-NLS-1$
      }
      return null;
    }

  }

  protected void loadCatalogsIntoCache( final DataSourcesConfig.DataSources dataSources,
                                        final IPentahoSession pentahoSession ) {

    // Create the cache region if necessary.
    MondrianCatalogCache mondrianCatalogCache = getCacheForRegion( pentahoSession );
    if ( mondrianCatalogCache.getMondrianCatalogCacheState().isFullyLoaded() ) {
      return;
    }

    for ( DataSourcesConfig.DataSource dataSource : dataSources.dataSources ) {
      for ( DataSourcesConfig.Catalog catalog : dataSource.catalogs.catalogs ) {
        cacheCatalog( catalog, mondrianCatalogCache );
      }
    }

    //set cache to fully loaded state
    mondrianCatalogCache.getMondrianCatalogCacheState().setFullyLoaded();
  }

  @Override
  public InputStream getCatalogSchemaAsStream( String catalogName, boolean applyAnnotations ) {

    var helper = getMondrianCatalogRepositoryHelper();
    var catalogFiles = helper.getMondrianSchemaFiles( catalogName );
    InputStream schemaStream;
    if ( applyAnnotations && catalogFiles.containsKey( ANNOTATIONS_FILE ) ) {
      schemaStream = catalogFiles.get( ANNOTATED_SCHEMA_FILE );
    } else {
      schemaStream = catalogFiles.get( SCHEMA_FILE );
    }

    return schemaStream;
  }

  @Override
  public InputStream getCatalogAnnotationsAsStream( String catalogName ) {
    var helper = getMondrianCatalogRepositoryHelper();
    var catalogFiles = helper.getMondrianSchemaFiles( catalogName );
    if ( catalogFiles.containsKey( ANNOTATIONS_FILE ) ) {
      return catalogFiles.get( ANNOTATIONS_FILE );
    }
    return null;
  }

  protected String applyDSP( String catalogDsInfo, String catalogDefinition ) throws Exception {

    PropertyList pl = Util.parseConnectString( catalogDsInfo );
    String dsp = pl.get( RolapConnectionProperties.DynamicSchemaProcessor.name() );
    if ( dsp != null ) {
      if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
        MondrianCatalogHelper.logger.debug( "applyDSP: " + dsp ); //$NON-NLS-1$
      }
      DynamicSchemaProcessor dynProc = ClassResolver.INSTANCE.instantiateSafe( dsp );
      pl.put( "Locale", getLocale().toString() );
      return dynProc.processSchema( catalogDefinition, pl );
    } else {
      return docAtUrlToString( catalogDefinition );
    }
  }

  protected String getCatalogAsString( DataSourcesConfig.Catalog catalog ) throws Exception {
    if ( catalog.dataSourceInfo != null ) {
      return applyDSP( catalog.dataSourceInfo, catalog.definition );
    } else {
      MondrianCatalogHelper.logger.warn( Messages.getInstance().getString(
        "MondrianCatalogHelper.WARN_NO_CATALOG_DATASOURCE_INFO", catalog.name ) );
      return docAtUrlToString( catalog.definition );
    }
  }

  @Deprecated
  protected List<MondrianCatalog> transformIntoCatalogList( final DataSourcesConfig.DataSources dataSources ) {
    List<MondrianCatalog> localCatalogs = new ArrayList<>();
    for ( DataSourcesConfig.DataSource dataSource : dataSources.dataSources ) {
      for ( DataSourcesConfig.Catalog catalog : dataSource.catalogs.catalogs ) {
        if ( catalog.definition.startsWith( SOLUTION_PREFIX ) ) { //$NON-NLS-1$
          // try catch here so the whole thing doesn't blow up if one datasource is configured incorrectly.
          try {
            MondrianSchema schema = makeSchema( docAtUrlToString( catalog.definition ) );

            MondrianCatalogComplementInfo catalogComplementInfo = getCatalogComplementInfoMap( catalog.definition );

            MondrianCatalog mondrianCatalog =
              new MondrianCatalog( useSchemaNameAsCatalogName ? schema.getName() : catalog.name,
                catalog.dataSourceInfo, catalog.definition, schema, catalogComplementInfo );

            localCatalogs.add( mondrianCatalog );
          } catch ( Exception e ) {
            MondrianCatalogHelper.logger.error( Messages.getInstance().getErrorString(
              ERROR_MESSAGE_FAILED_TO_LOAD_SCHEMA, catalog.definition ), e ); //$NON-NLS-1$

          }
        } else {
          MondrianCatalogHelper.logger.warn( Messages.getInstance().getString(
            WARNING_MESSAGE_SKIPPING_DATASOURCE, catalog.definition ) ); //$NON-NLS-1$
        }
      }
    }
    return localCatalogs;
  }

  /**
   * Method to access the MondrianCatalogComplementInfo taken a catalog name.
   *
   * @param name Catalog schema location
   * @return MondrianCatalogComplementInfo object
   */
  public MondrianCatalogComplementInfo getCatalogComplementInfoMap( String name ) {
    return catalogComplementInfoMap.get( name );
  }

  /**
   * this method loads a mondrian schema
   *
   * @param solutionLocation location of the schema
   * @param pentahoSession   current session object
   * @return Mondrian Schema object.
   */
  @Override
  public MondrianSchema loadMondrianSchema( final String solutionLocation, final IPentahoSession pentahoSession ) {
    return makeSchema( docAtUrlToString( solutionLocation ) );
  }

  protected String docAtUrlToString( final String urlStr ) {
    String res;
    InputStream in = null;
    try {
      PropertyList localeInfo = new PropertyList();
      localeInfo.put( "Locale", getLocale().toString() ); //$NON-NLS-1$

      FileSystemManager fsManager = VFS.getManager();

      FileObject mondrianDS = fsManager.resolveFile( urlStr );

      in = mondrianDS.getContent().getInputStream();
      res = localizingDynamicSchemaProcessor.filter( null, localeInfo, in );
    } catch ( FileNotFoundException fnfe ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        ERROR_MESSAGE_FILE_NOT_FOUND ), fnfe ); //$NON-NLS-1$
    } catch ( Exception e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        ERROR_MESSAGE_IO_PROBLEM ), e ); //$NON-NLS-1$
    } finally {
      IOUtils.closeQuietly( in );
    }
    return res;
  }

  protected MondrianSchema getSchema( final String catalogName, final IPentahoSession pentahoSession ) {
    MondrianCatalog catalog = getCatalogFromCache( catalogName, pentahoSession );
    if ( null == catalog ) {
      return null;
    } else {
      return catalog.getSchema();
    }
  }

  protected MondrianSchema makeSchema( final String catalogStr ) {
    if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
      MondrianCatalogHelper.logger
        .debug( "makeSchema (catalogStr=" + catalogStr.substring( 0, Math.min( 40, catalogStr.length() ) ) + "...)" );
    }
    MondrianSchema schema = null;
    try {
      final Parser xmlParser = XOMUtil.createDefaultParser();
      final DOMWrapper def = xmlParser.parse( catalogStr );
      MondrianDef.Schema schemaFromXml = new MondrianDef.Schema( def );
      String schemaName = schemaFromXml.name;
      List<MondrianCube> mondrianCubes = new ArrayList<>();
      for ( MondrianDef.Cube cube : schemaFromXml.cubes ) {
        if ( cube.enabled == null || cube.enabled.booleanValue() ) {
          String name = cube.caption;
          if ( StringUtils.isBlank( name ) ) {
            name = cube.name;
          }
          mondrianCubes.add( new MondrianCube( name, cube.name ) );
        }
      }
      for ( MondrianDef.VirtualCube cube : schemaFromXml.virtualCubes ) {
        String name = cube.caption;
        if ( StringUtils.isBlank( name ) ) {
          name = cube.name;
        }
        if ( cube.enabled == null || cube.enabled.booleanValue() ) {
          mondrianCubes.add( new MondrianCube( name, cube.name ) );
        }
      }

      // Interpret the role names
      MondrianDef.Role[] roles = schemaFromXml.roles;
      String[] roleNames = null;

      if ( ( roles != null ) && ( roles.length > 0 ) ) {
        roleNames = new String[ roles.length ];
        for ( int i = 0; i < roles.length; i++ ) {
          roleNames[ i ] = roles[ i ].name; // Note - getName() doesn't return the role name, it returns the word Role
        }
      }

      schema = new MondrianSchema( schemaName, mondrianCubes, roleNames );

    } catch ( XOMException e ) {
      if ( MondrianCatalogHelper.logger.isErrorEnabled() ) {
        MondrianCatalogHelper.logger.error( Messages.getInstance().getErrorString(
          ERROR_MESSAGE_ERROR_OCCURRED ), e ); //$NON-NLS-1$
      }
      throw Util.newError( e, Messages.getInstance().getErrorString(
        ERROR_MESSAGE_PARSING_CATALOG, catalogStr ) ); //$NON-NLS-1$
    }
    return schema;
  }

  protected static enum CatalogPermission {
    READ, WRITE
  }

  protected List<MondrianCatalog> filter( final List<MondrianCatalog> origList,
                                          final boolean jndiOnly ) {
    List<MondrianCatalog> filtered = new ArrayList<>();
    for ( MondrianCatalog orig : origList ) {
      try {
        if ( hasAccess( orig, RepositoryFilePermission.READ ) && ( !jndiOnly || orig.isJndi() ) ) {
          filtered.add( orig );
        }
      } catch ( UnifiedRepositoryException e ) {
        // in case of catalog doesn't exist anymore
        continue;
      }
    }
    return filtered;
  }

  /**
   * This (hacky) implementation bases its decision on whether or not the user has the permission (indicated by
   * <code>CatalogPermission</code>) based on whether the user has permission on the file in the solution repository
   * indicated by <code>catalog.getDefinition()</code>.
   * <p/>
   * Why is this class even enforcing security anyway!?
   */
  protected boolean hasAccess( MondrianCatalog cat, RepositoryFilePermission permission ) {
    return getAclHelper().canAccess( getMondrianCatalogRepositoryHelper().getMondrianCatalogFile( cat.getName() ),
      EnumSet.of( permission ) );
  }

  public void importSchema( File mondrianFile, String databaseConnection, String parameters ) {

    try {
      String datasourceInfo = "Provider=mondrian;DataSource=" + databaseConnection; //$NON-NLS-1$
      if ( !StringUtils.isEmpty( parameters ) ) {
        datasourceInfo = parameters;
      }

      // Note: Mondrian parameters could be validated here and throw subsequent exception if they do not conform to
      // spec.

      FileInputStream parsingInputStream = new FileInputStream( mondrianFile );
      org.w3c.dom.Document document = getMondrianXmlDocument( parsingInputStream );
      NodeList schemas = document.getElementsByTagName( "Schema" ); //$NON-NLS-1$
      Node schema = schemas.item( 0 );
      if ( schema == null ) {
        throw new SAXParseException( "",
          null ); // Generic schema error message will be provided at catch statement.
      }
      Node name = schema.getAttributes().getNamedItem( "name" ); //$NON-NLS-1$
      String catalogName = name.getTextContent();
      parsingInputStream.close();

      FileInputStream schemaInputStream = new FileInputStream( mondrianFile );
      org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper helper =
        new org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper(
          unifiedRepository );
      helper.addSchema( schemaInputStream, catalogName, datasourceInfo );

      reInit( PentahoSessionHolder.getSession() );

      flushCacheForCatalog( catalogName, PentahoSessionHolder.getSession() );
    } catch ( SAXParseException e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getString(
        ERROR_MESSAGE_IMPORT_SCHEMA ) ); //$NON-NLS-1$
    } catch ( Exception e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getString(
        ERROR_MESSAGE_ERROR_OCCURRED ), //$NON-NLS-1$
        Reason.valueOf( e.getMessage() ) );
    }
  }

  public boolean isUseSchemaNameAsCatalogName() {
    return useSchemaNameAsCatalogName;
  }

  public void setUseSchemaNameAsCatalogName( final boolean useSchemaNameAsCatalogName ) {
    this.useSchemaNameAsCatalogName = useSchemaNameAsCatalogName;
  }

  /**
   * This method removes a mondrian catalog from the JCR repository.
   */
  @Override
  public void removeCatalog( final String catalogName, final IPentahoSession pentahoSession )
    throws MondrianCatalogServiceException {

    //
    // find the catalog to be removed
    //

    if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
      MondrianCatalogHelper.logger.debug( "removeCatalog" ); //$NON-NLS-1$
    }
    MondrianCatalog catalog = getCatalog( catalogName, pentahoSession );

    if ( catalog == null ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        ERROR_MESSAGE_CATALOG_NOT_FOUND, catalogName ) ); //$NON-NLS-1$
    }

    //
    // do an access check first
    //

    if ( !hasAccess( catalog, RepositoryFilePermission.WRITE ) ) {
      if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
        MondrianCatalogHelper.logger.debug( "user does not have access; throwing exception" ); //$NON-NLS-1$
      }
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        ERROR_MESSAGE_INSUFFICIENT_PERMISSIONS ), Reason.ACCESS_DENIED ); //$NON-NLS-1$
    }
    flushCacheForCatalog( catalog.getName(), pentahoSession );

    getAclHelper().removeAclFor( getMondrianCatalogRepositoryHelper().getMondrianCatalogFile( catalog.getName() ) );

    RepositoryFile deletingFile = unifiedRepository.getFile( RepositoryFile.SEPARATOR + "etc" //$NON-NLS-1$
        + RepositoryFile.SEPARATOR + "mondrian" + RepositoryFile.SEPARATOR + catalog.getName() ); //$NON-NLS-1$
    unifiedRepository.deleteFile( deletingFile.getId(), true, "" ); //$NON-NLS-1$
    MondrianCatalogCache mondrianCatalogCache =
      (MondrianCatalogCache) PentahoSystem.getCacheManager( pentahoSession ).getFromRegionCache(
        MONDRIAN_CATALOG_CACHE_REGION, getLocale().toString() );
    mondrianCatalogCache.getCatalogs().remove( catalog.getDefinition() );
    mondrianCatalogCache.getCatalogs().remove( catalog.getName() );

  }

  @VisibleForTesting
  org.w3c.dom.Document getMondrianXmlDocument( InputStream is )
    throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = XMLParserFactoryProducer.createSecureDocBuilderFactory();
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse( is );
  }


  /**
   * Load catalog into cache. If not found or if overwrite is true, load the catalog into cache.
   *
   * @param catalogName    the catalog name
   * @param pentahoSession the pentaho session where the cache is stored
   * @param overwrite      flag to indicate if the catalog should be overwritten
   */
  private synchronized void loadCatalogIntoCache( String catalogName, final IPentahoSession pentahoSession,
                                                  boolean overwrite ) {
    ICacheManager cacheMgr = PentahoSystem.getCacheManager( pentahoSession );
    MondrianCatalogCache mondrianCatalogCache = getCacheForRegion( cacheMgr );
    if ( mondrianCatalogCache.getCatalog( catalogName ) != null && !overwrite ) {
      return;  //We already have it
    }

    findDatasourceWithCatalog( catalogName, mondrianCatalogCache );
    cacheMgr.putInRegionCache( MONDRIAN_CATALOG_CACHE_REGION, getLocale().toString(), mondrianCatalogCache );

    if ( mondrianCatalogCache.getCatalog( catalogName ) == null && !mondrianCatalogCache.getMondrianCatalogCacheState()
      .isFullyLoaded() && isCatalogDefinitionString( catalogName ) ) {
      //We could not find quickly by name, but if its a definition string perhaps we can find it with a full load
      initIfNotFullyLoaded( pentahoSession );
    }
  }

  private MondrianCatalogCache getCacheForRegion( IPentahoSession pentahoSession ) {
    final ICacheManager cacheMgr = PentahoSystem.getCacheManager( pentahoSession );
    return getCacheForRegion( cacheMgr );
  }

  private MondrianCatalogCache getCacheForRegion( final ICacheManager cacheMgr ) {
    // Create the cache region if necessary.
    if ( !cacheMgr.cacheEnabled( MONDRIAN_CATALOG_CACHE_REGION ) ) {
      // Create the region
      cacheMgr.addCacheRegion( MONDRIAN_CATALOG_CACHE_REGION );
    }

    MondrianCatalogCache mondrianCatalogCache =
      (MondrianCatalogCache) cacheMgr.getFromRegionCache( MONDRIAN_CATALOG_CACHE_REGION,
        getLocale().toString() );
    if ( mondrianCatalogCache == null ) {
      mondrianCatalogCache = new MondrianCatalogCache();
      cacheMgr.putInRegionCache( MONDRIAN_CATALOG_CACHE_REGION, getLocale().toString(), mondrianCatalogCache );
    }

    return mondrianCatalogCache;
  }

  private void findDatasourceWithCatalog( String catalogName, MondrianCatalogCache mondrianCatalogCache ) {

    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    try {
      URL dataSourcesConfigUrl = null;

      if ( dataSourcesConfig == null ) {
        // Using the repo here, so we don't need dataSourcesConfig
        generateInMemoryCatalog( catalogName, mondrianCatalogCache, pentahoSession );

      } else if ( dataSourcesConfig.startsWith( "file:" ) ) { //$NON-NLS-1$
        dataSourcesConfigUrl = new URL( dataSourcesConfig );
        DataSourcesConfig.DataSources dataSources = parseDataSourcesUrl( dataSourcesConfigUrl );
        cacheCatalogsInDataSources( dataSources, mondrianCatalogCache );

      } else if ( dataSourcesConfig.startsWith( "classpath:" ) ) { //$NON-NLS-1$
        dataSourcesConfigUrl = getClass().getResource( dataSourcesConfig.substring( 10 ) );
        if ( dataSourcesConfigUrl != null ) {
          DataSourcesConfig.DataSources dataSources = parseDataSourcesUrl( dataSourcesConfigUrl );
          cacheCatalogsInDataSources( dataSources, mondrianCatalogCache );
        }
      } else {
        throw new MondrianCatalogServiceException( "dataSourcesConfig is not a valid URL or does not exist",
          //$NON-NLS-1$
          Reason.GENERAL );
      }
    } catch ( IOException e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        ERROR_MESSAGE_INVALID_DATASOURCE_CONFIG, dataSourcesConfig ), //$NON-NLS-1$
        e, Reason.GENERAL );
    }
  }

  private void generateInMemoryCatalog( String catalogName, MondrianCatalogCache mondrianCatalogCache,
                                        IPentahoSession pentahoSession ) {
    String etcMondrian =
      ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + MONDRIAN_DATASOURCE_FOLDER;
    RepositoryFile etcMondrianFolder = unifiedRepository.getFile( etcMondrian );

    if ( etcMondrianFolder == null ) {
      return;
    }

    DataSourcesConfig.Catalog catalog =
      getCatalogFromRepo( catalogName );

    cacheCatalog( catalog, mondrianCatalogCache );
  }

  private void cacheCatalog( DataSourcesConfig.Catalog catalog, MondrianCatalogCache mondrianCatalogCache ) {
    if ( catalog != null ) {
      if ( isCatalogDefinitionString( catalog.definition ) ) {

        // try catch here so the whole thing doesn't blow up if one datasource is configured incorrectly.
        MondrianSchema schema = null;
        try {
          schema = makeSchema( getCatalogAsString( catalog ) );

        } catch ( Exception e ) {
          MondrianCatalogHelper.logger.error( Messages.getInstance().getErrorString(
            ERROR_MESSAGE_FAILED_TO_LOAD_SCHEMA, catalog.definition ), e ); //$NON-NLS-1$
          if ( e instanceof MondrianException ) {
            throw (MondrianException) e;
          }
        }

        MondrianCatalog mondrianCatalog = createMondrianCatalog( catalog, schema );
        mondrianCatalogCache.putCatalog( mondrianCatalog.getName(), mondrianCatalog );
        mondrianCatalogCache.putCatalog( mondrianCatalog.getDefinition(), mondrianCatalog );

      } else {
        MondrianCatalogHelper.logger.warn( Messages.getInstance().getString(
          WARNING_MESSAGE_SKIPPING_DATASOURCE, catalog.definition ) ); //$NON-NLS-1$
      }
    }
  }

  private MondrianCatalog createMondrianCatalog( DataSourcesConfig.Catalog catalog, MondrianSchema schema ) {
    if ( schema == null ) {
      return new MondrianCatalog( catalog.name, catalog.dataSourceInfo, catalog.definition, schema );
    } else {
      return
        new MondrianCatalog( useSchemaNameAsCatalogName ? schema.getName() : catalog.name,
          catalog.dataSourceInfo, catalog.definition, schema );
    }
  }

  private DataSourcesConfig.Catalog getCatalogFromRepo( String desiredCatalog ) {
    var catalogInfo = getMondrianCatalogRepositoryHelper().getHostedCatalogInfo( desiredCatalog );
    if ( catalogInfo != null ) {
      DataSourcesConfig.Catalog configCatalog = new DataSourcesConfig.Catalog();
      configCatalog.dataSourceInfo = catalogInfo.dataSourceInfo; //$NON-NLS-1$
      configCatalog.definition = catalogInfo.definition; //$NON-NLS-1$
      configCatalog.name = desiredCatalog;
      return configCatalog;
    }
    return null;
  }

  private void cacheCatalogsInDataSources( DataSourcesConfig.DataSources dataSources,
                                           MondrianCatalogCache mondrianCatalogCache ) {
    for ( DataSourcesConfig.DataSource dataSource : dataSources.dataSources ) {
      for ( DataSourcesConfig.Catalog catalog : dataSource.catalogs.catalogs ) {
        cacheCatalog( catalog, mondrianCatalogCache );
      }
    }
    mondrianCatalogCache.setFullLoad();
  }

  /**
   * Is the value of this string in the format of a definition string?
   *
   * @return true if format it appropriate for a definition string
   */
  private boolean isCatalogDefinitionString( String context ) {
    return context != null && ( context.startsWith( "mondrian:" ) || context.startsWith( SOLUTION_PREFIX ) );
  }

  private void removeHelpGeneratorCache( String catalogName, IPentahoSession pentahoSession ) {
    final ICacheManager cacheMgr = PentahoSystem.getCacheManager( pentahoSession );
    final Set<String> keys = cacheMgr.getAllKeysFromRegionCache( MONDRIAN_CATALOG_CACHE_REGION );
    if ( keys == null ) {
      MondrianCatalogHelper.logger
        .debug( "No cache entries for " + MONDRIAN_CATALOG_CACHE_REGION );
      return;
    }
    String startsWith = "ANALYZER~" + catalogName + "~"; //faster than a split
    for ( String key : keys ) {
      if ( key.startsWith( startsWith ) ) {
        if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
          MondrianCatalogHelper.logger
            .debug( "Removing cache entry " + key + " from " + MONDRIAN_CATALOG_CACHE_REGION );
        }
        cacheMgr.removeFromRegionCache( MONDRIAN_CATALOG_CACHE_REGION, key );
      }
    }
  }

}
