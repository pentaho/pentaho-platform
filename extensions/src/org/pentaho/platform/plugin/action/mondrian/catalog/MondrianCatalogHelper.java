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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.action.mondrian.catalog;

import mondrian.i18n.LocalizingDynamicSchemaProcessor;
import mondrian.olap.MondrianDef;
import mondrian.olap.Util;
import mondrian.olap.Util.PropertyList;
import mondrian.rolap.agg.AggregationManager;
import mondrian.xmla.DataSourcesConfig;
import mondrian.xmla.DataSourcesConfig.DataSources;
import org.apache.commons.collections.list.SetUniqueList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.eigenbase.xom.DOMWrapper;
import org.eigenbase.xom.Parser;
import org.eigenbase.xom.XMLOutput;
import org.eigenbase.xom.XOMException;
import org.eigenbase.xom.XOMUtil;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException.Reason;
import org.pentaho.platform.repository.solution.filebased.MondrianVfs;
import org.pentaho.platform.repository.solution.filebased.SolutionRepositoryVfsFileObject;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reads in file containing Mondrian data sources and catalogs. (Contains code copied from <code>XmlaServlet</code>.)
 *
 *
 * @author mlowery
 */
public class MondrianCatalogHelper implements IMondrianCatalogService {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog( MondrianCatalogHelper.class );

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

  public static final String MONDRIAN_DATASOURCE_FOLDER = "mondrian"; //$NON-NLS-1$

  // ~ Constructors ====================================================================================================

  @SuppressWarnings ( "unchecked" )
  private List<MondrianCatalog> getCatalogs( IPentahoSession pentahoSession ) {

    Map<String, MondrianCatalog> catalogsMap =
      (Map<String, MondrianCatalog>) PentahoSystem.getCacheManager( pentahoSession ).getFromRegionCache(
        MONDRIAN_CATALOG_CACHE_REGION, getLocale().toString() );

    List<MondrianCatalog> catalogs = new ArrayList<MondrianCatalog>();
    // Analyzer cache is also placed in the MONDRIAN_CATALOG_CACHE_REGION
    // so we need to filter the list to only keep MondrianCatalog objects
    for ( Object o : catalogsMap.values() ) {
      if ( o instanceof MondrianCatalog ) {
        catalogs.add( (MondrianCatalog) o );
      }
    }
    // Sort
    Collections.sort( catalogs, new Comparator<MondrianCatalog>() {
      public int compare( MondrianCatalog o1, MondrianCatalog o2 ) {
        return o1.getName().compareTo( o2.getName() );
      }
    } );
    // remove duplicates
    SetUniqueList uniqueCatalogs = SetUniqueList.decorate( catalogs );
    return uniqueCatalogs;
  }

  /**
   * Performs a search for an existing catalog based on the datasource info and catalog definition.
   *
   * @param catalog        The catalog to compare against
   * @param pentahoSession The session with which this request is associated (Used to locate the cache)
   * @return True if an existing match has been found for the catalog
   */
  private boolean catalogExists( MondrianCatalog catalog, IPentahoSession pentahoSession ) {
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

        if ( definitionEquals( foundDefinition, newDefinition ) ) {
          return true;
        }
      }
    }
    return false;
  }

  private static final MondrianCatalog getCatalogFromCache( String context, IPentahoSession pentahoSession ) {
    // NOTE that the context can be the catalog name or the definition string for the catalog. If you are using the
    // definition string to
    // retrieve the catalog form the cache, you cannot be guaranteed what datasource is in play; so under these
    // circumstances, this catalog's
    // definition is the only part of the catalog that can be trusted. As this feature was added to enable looking up
    // Mondrian
    // roles from the schema, we don't much care which datasource is in play.
    Map<String, MondrianCatalog> catalogs =
      (Map<String, MondrianCatalog>) PentahoSystem.getCacheManager( pentahoSession ).getFromRegionCache(
        MONDRIAN_CATALOG_CACHE_REGION, getLocale().toString() );
    return catalogs.get( context );
  }

  /**
   * TODO Delete this method. Calling this method ties you to this implementation.
   *
   * @deprecated Please use PentahoSystem.get to get the Mondrian Catalog Service
   */
  public static MondrianCatalogHelper getInstance() {
    // IMondrianCatalogService is a singleton; IPentahoSession not required
    return (MondrianCatalogHelper) PentahoSystem
      .get( IMondrianCatalogService.class, "IMondrianCatalogService", null ); //$NON-NLS-1$
  }

  public MondrianCatalogHelper() {
    super();

    try {
      DefaultFileSystemManager dfsm = (DefaultFileSystemManager) VFS.getManager();
      dfsm.addProvider( "mondrian", new MondrianVfs() ); //$NON-NLS-1$
    } catch ( FileSystemException e ) {
      logger.error( e.getMessage() );
    }
  }

  public static String MONDRIAN_CATALOG_CACHE_REGION = "mondrian-catalog-cache"; //$NON-NLS-1$

  // ~ Methods =========================================================================================================

  protected synchronized void init( final IPentahoSession pentahoSession ) {
    // First check if the catalogs are initialized for the current locale
    final ICacheManager cacheMgr = PentahoSystem.getCacheManager( pentahoSession );
    if ( cacheMgr.cacheEnabled( MONDRIAN_CATALOG_CACHE_REGION )
      && cacheMgr.getFromRegionCache( MONDRIAN_CATALOG_CACHE_REGION, getLocale().toString() ) != null ) {
      return;
    }
    if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
      MondrianCatalogHelper.logger.debug( "init" ); //$NON-NLS-1$
    }
    // By default, we will use the system to load all schemas into the cache.
    // access to these schemas is controlled later via the hasAccess() method
    loadCatalogsIntoCache( makeDataSources(), PentahoSessionHolder.getSession() );
    AggregationManager.instance().getCacheControl( null, null ).flushSchemaCache();
  }

  public synchronized void reInit( final IPentahoSession pentahoSession ) {
    final ICacheManager cacheMgr = PentahoSystem.getCacheManager( pentahoSession );
    if ( cacheMgr.cacheEnabled( MONDRIAN_CATALOG_CACHE_REGION ) ) {
      cacheMgr.clearRegionCache( MONDRIAN_CATALOG_CACHE_REGION );
    }
    init( pentahoSession );
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
        return ( dataSourcesConfigUrl == null ) ? null : parseDataSourcesUrl( dataSourcesConfigUrl );
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

  public String generateInMemoryDatasourcesXml( IUnifiedRepository unifiedRepository ) {

    StringBuffer datasourcesXML = new StringBuffer();
    datasourcesXML.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<DataSources>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<DataSource>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<DataSourceName>Pentaho</DataSourceName>\n" ); //$NON-NLS-1$
    datasourcesXML
      .append( "<DataSourceDescription>Pentaho BI Platform Datasources</DataSourceDescription>\n" ); //$NON-NLS-1$
    datasourcesXML.append(
      "<URL>" + PentahoRequestContextHolder.getRequestContext().getContextPath() + "Xmla</URL>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<DataSourceInfo>Provider=mondrian</DataSourceInfo>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<ProviderName>PentahoXMLA</ProviderName>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<ProviderType>MDP</ProviderType>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<AuthenticationMode>Unauthenticated</AuthenticationMode>\n" ); //$NON-NLS-1$
    datasourcesXML.append( "<Catalogs>\n" ); //$NON-NLS-1$

    Encoder encoder = ESAPI.encoder();

    // Creates <Catalogs> from the "/etc/mondrian/<catalog>/metadata" nodes.
    /*
     * IPentahoSession pentahoSession = PentahoSessionHolder.getSession(); String tenantEtcFolder = null;
     * if(pentahoSession != null) { String tenantId = (String)
     * pentahoSession.getAttribute(IPentahoSession.TENANT_ID_KEY); tenantEtcFolder =
     * ServerRepositoryPaths.getTenantEtcFolderPath(tenantId); } else { tenantEtcFolder =
     * ServerRepositoryPaths.getTenantEtcFolderPath(); }
     */

    String etcMondrian =
      ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + MONDRIAN_DATASOURCE_FOLDER;

    RepositoryFile etcMondrianFolder = unifiedRepository.getFile( etcMondrian );
    if ( etcMondrianFolder != null ) {
      List<RepositoryFile> mondrianCatalogs = unifiedRepository.getChildren( etcMondrianFolder.getId() );

      for ( RepositoryFile catalog : mondrianCatalogs ) {

        String catalogName = catalog.getName();
        RepositoryFile metadata =
          unifiedRepository.getFile( etcMondrian + RepositoryFile.SEPARATOR + catalogName + RepositoryFile.SEPARATOR
            + "metadata" ); //$NON-NLS-1$

        if ( metadata != null ) {
          DataNode metadataNode =
            unifiedRepository.getDataForRead( metadata.getId(), NodeRepositoryFileData.class ).getNode();
          String datasourceInfo = metadataNode.getProperty( "datasourceInfo" ).getString(); //$NON-NLS-1$
          String definition = metadataNode.getProperty( "definition" ).getString(); //$NON-NLS-1$

          datasourcesXML.append( "<Catalog name=\"" + encoder.encodeForXML( catalogName ) + "\">\n" ); //$NON-NLS-1$ //$NON-NLS-2$
          datasourcesXML
            .append( "<DataSourceInfo>" + encoder.encodeForXML( datasourceInfo ) + "</DataSourceInfo>\n" ); //$NON-NLS-1$ //$NON-NLS-2$
          datasourcesXML.append( "<Definition>" + encoder.encodeForXML( definition ) + "</Definition>\n" ); //$NON-NLS-1$ //$NON-NLS-2$
          datasourcesXML.append( "</Catalog>\n" ); //$NON-NLS-1$
        } else {
          logger
            .warn( Messages.getInstance().getString( "MondrianCatalogHelper.WARN_META_DATA_IS_NULL" ) ); //$NON-NLS-1$
        }
      }

      datasourcesXML.append( "</Catalogs>\n" ); //$NON-NLS-1$
      datasourcesXML.append( "</DataSource>\n" ); //$NON-NLS-1$
      datasourcesXML.append( "</DataSources>\n" ); //$NON-NLS-1$
      return datasourcesXML.toString();
    } else {
      return null;
    }
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
              "MondrianCatalogHelper.ERROR_0002_FAILED_TO_PARSE_DATASOURCE_CONFIG",
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
      throw Util.newError( e, Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0002_FAILED_TO_PARSE_DATASOURCE_CONFIG", dataSourcesConfigString ) ); //$NON-NLS-1$
    }
  }

  protected Map<String, MondrianCatalogComplementInfo> makeCatalogComplementInfoMap( final DOMWrapper doc ) {

    HashMap<String, MondrianCatalogComplementInfo> map = new HashMap<String, MondrianCatalogComplementInfo>();

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
    Map<String, MondrianCatalog> map = new HashMap<String, MondrianCatalog>();
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
      def1.startsWith( "solution:/" ) ? "solution:" + def1.substring( 10 )
        : "solution:/" + def1.substring( 9 ); //$NON-NLS-1$
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

  public String getDataSourcesConfig() {
    return dataSourcesConfig;
  }

  public void setDataSourcesConfig( final String dataSourcesConfig ) {
    this.dataSourcesConfig = dataSourcesConfig;
  }

  public List<MondrianCatalog> listCatalogs( final IPentahoSession pentahoSession, final boolean jndiOnly ) {
    if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
      MondrianCatalogHelper.logger.debug( "listCatalogs" ); //$NON-NLS-1$
    }
    init( pentahoSession );

    // defensive copy
    return Collections.unmodifiableList( filter( getCatalogs( pentahoSession ), pentahoSession, jndiOnly ) );
  }

  /**
   * use the in memory session value of input stream (used by test harness)
   */
  public synchronized void addCatalog( final MondrianCatalog catalog, final boolean overwrite,
                                       final IPentahoSession pentahoSession ) throws MondrianCatalogServiceException {
    String mondrianSchema = (String) pentahoSession.getAttribute( "MONDRIAN_SCHEMA_XML_CONTENT" ); //$NON-NLS-1$
    InputStream schemaInputStream = IOUtils.toInputStream( mondrianSchema );
    addCatalog( schemaInputStream, catalog, overwrite, pentahoSession );
  }

  /**
   * new method to pass the input stream directly from data access put and post schema
   *
   * @param schemaInputStream
   * @param catalog
   * @param overwrite
   * @param pentahoSession
   * @throws MondrianCatalogServiceException
   *
   */
  public synchronized void addCatalog( InputStream schemaInputStream, final MondrianCatalog catalog,
                                       final boolean overwrite, final IPentahoSession pentahoSession )
    throws MondrianCatalogServiceException {
    if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
      MondrianCatalogHelper.logger.debug( "addCatalog" ); //$NON-NLS-1$
    }

    init( pentahoSession );

    // do an access check first
    if ( !hasAccess( catalog, CatalogPermission.WRITE, pentahoSession ) ) {
      if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
        MondrianCatalogHelper.logger.debug( "user does not have access; throwing exception" ); //$NON-NLS-1$
      }
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0003_INSUFFICIENT_PERMISSION" ), Reason.ACCESS_DENIED ); //$NON-NLS-1$
    }

    // check for existing dataSourceInfo+catalog
    if ( catalogExists( catalog, pentahoSession ) && !overwrite ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0004_ALREADY_EXISTS" ), Reason.ALREADY_EXISTS ); //$NON-NLS-1$
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
    //compare the catalog names and throw exception if same and NOT ovewrite
    if ( fileLocationCatalogTest != null
      && definitionEquals( fileLocationCatalogTest.getDefinition(),
        "mondrian:/" + catalog.getName() )
        && !overwrite ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0004_ALREADY_EXISTS" ), //$NON-NLS-1$
        Reason.XMLA_SCHEMA_NAME_EXISTS );
    }
    // check if the file is a valid schema
    try {
      StringWriter writer = new StringWriter();
      IOUtils.copy( schemaInputStream, writer, "UTF8" );
      String catalogStr = writer.toString();
      makeSchema( catalogStr );
      schemaInputStream = new ByteArrayInputStream( catalogStr.getBytes() );
    } catch ( Exception ex ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0008_ERROR_OCCURRED" ), //$NON-NLS-1$
        Reason.valueOf( ex.getMessage() ) );
    }
    try {
      org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper helper =
        new org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper( PentahoSystem
          .get( IUnifiedRepository.class ) );
      helper.addSchema( schemaInputStream, catalog.getName(), catalog.getDataSourceInfo() );
    } catch ( Exception e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0008_ERROR_OCCURRED" ), //$NON-NLS-1$
        Reason.valueOf( e.getMessage() ) );
    }

    if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
      MondrianCatalogHelper.logger
        .debug( "refreshing from dataSourcesConfig (" + dataSourcesConfig + ")" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    reInit( pentahoSession );
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
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      org.w3c.dom.Document document = builder.parse( parsingInputStream );
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
        new org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper( PentahoSystem
          .get( IUnifiedRepository.class ) );
      helper.addSchema( schemaInputStream, catalogName, datasourceInfo );

      reInit( PentahoSessionHolder.getSession() );

    } catch ( SAXParseException e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getString(
        "MondrianCatalogHelper.ERROR_0018_IMPORT_SCHEMA_ERROR" ) ); //$NON-NLS-1$
    } catch ( Exception e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getString(
        "MondrianCatalogHelper.ERROR_0008_ERROR_OCCURRED" ), //$NON-NLS-1$
        Reason.valueOf( e.getMessage() ) );
    }
  }

  @Deprecated
  protected void writeDataSources( DataSources dataSources ) {

    File dataSourcesFile;
    try {
      dataSourcesFile = new File( new URL( dataSourcesConfig ).getFile() ); // dataSourcesConfigResource.getFile();
    } catch ( IOException e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0005_RESOURCE_NOT_AVAILABLE" ), e, Reason.GENERAL ); //$NON-NLS-1$
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

  public MondrianCatalog getCatalog( final String context, final IPentahoSession pentahoSession ) {
    if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
      MondrianCatalogHelper.logger.debug( "getCatalog" ); //$NON-NLS-1$
    }
    init( pentahoSession );

    MondrianCatalog cat = getCatalogFromCache( context, pentahoSession );
    if ( null != cat ) {
      if ( hasAccess( cat, CatalogPermission.READ, pentahoSession ) ) {
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
    ICacheManager cacheMgr = PentahoSystem.getCacheManager( pentahoSession );
    if ( !cacheMgr.cacheEnabled( MONDRIAN_CATALOG_CACHE_REGION ) ) {
      // Create the region
      cacheMgr.addCacheRegion( MONDRIAN_CATALOG_CACHE_REGION );
    }
    if ( cacheMgr.getFromRegionCache( MONDRIAN_CATALOG_CACHE_REGION, getLocale().toString() ) == null ) {
      // Put the map in the region
      cacheMgr.putInRegionCache( MONDRIAN_CATALOG_CACHE_REGION, getLocale().toString(),
        new HashMap<String, List<MondrianCatalog>>() );
    }

    for ( DataSourcesConfig.DataSource dataSource : dataSources.dataSources ) {
      List<String> catalogNames = new ArrayList<String>();
      for ( DataSourcesConfig.Catalog catalog : dataSource.catalogs.catalogs ) {
        catalogNames.add( catalog.name );
      }

      Map<String, MondrianCatalog> catalogs =
        (Map<String, MondrianCatalog>) cacheMgr.getFromRegionCache( MONDRIAN_CATALOG_CACHE_REGION, getLocale()
          .toString() );

      for ( DataSourcesConfig.Catalog catalog : dataSource.catalogs.catalogs ) {
        if ( catalog.definition.startsWith( "mondrian:" ) || catalog.definition
          .startsWith( "solution:" ) ) { //$NON-NLS-1$

          // try catch here so the whole thing doesn't blow up if one datasource is configured incorrectly.
          try {
            MondrianSchema schema = makeSchema( docAtUrlToString( catalog.definition, pentahoSession ) );

            MondrianCatalog mondrianCatalog =
              new MondrianCatalog( useSchemaNameAsCatalogName ? schema.getName() : catalog.name,
                catalog.dataSourceInfo, catalog.definition, schema );

            catalogs.put( mondrianCatalog.getName(), mondrianCatalog );
            catalogs.put( mondrianCatalog.getDefinition(), mondrianCatalog );

          } catch ( Exception e ) {
            MondrianCatalogHelper.logger.error( Messages.getInstance().getErrorString(
              "MondrianCatalogHelper.ERROR_0013_FAILED_TO_LOAD_SCHEMA", catalog.definition ), e ); //$NON-NLS-1$
          }
        } else {
          MondrianCatalogHelper.logger.warn( Messages.getInstance().getString(
            "MondrianCatalogHelper.WARN_SKIPPING_DATASOURCE_DEF", catalog.definition ) ); //$NON-NLS-1$
        }
      }
    }
  }

  @Deprecated
  protected List<MondrianCatalog> transformIntoCatalogList( final DataSourcesConfig.DataSources dataSources,
                                                            final IPentahoSession pentahoSession ) {
    List<MondrianCatalog> localCatalogs = new ArrayList<MondrianCatalog>();
    for ( DataSourcesConfig.DataSource dataSource : dataSources.dataSources ) {
      List<String> catalogNames = new ArrayList<String>();
      for ( DataSourcesConfig.Catalog catalog : dataSource.catalogs.catalogs ) {
        catalogNames.add( catalog.name );
      }

      for ( DataSourcesConfig.Catalog catalog : dataSource.catalogs.catalogs ) {
        if ( catalog.definition.startsWith( "solution:" ) ) { //$NON-NLS-1$
          // try catch here so the whole thing doesn't blow up if one datasource is configured incorrectly.
          try {
            MondrianSchema schema = makeSchema( docAtUrlToString( catalog.definition, pentahoSession ) );

            MondrianCatalogComplementInfo catalogComplementInfo = getCatalogComplementInfoMap( catalog.definition );

            MondrianCatalog mondrianCatalog =
              new MondrianCatalog( useSchemaNameAsCatalogName ? schema.getName() : catalog.name,
                catalog.dataSourceInfo, catalog.definition, schema, catalogComplementInfo );

            localCatalogs.add( mondrianCatalog );
          } catch ( Exception e ) {
            MondrianCatalogHelper.logger.error( Messages.getInstance().getErrorString(
              "MondrianCatalogHelper.ERROR_0013_FAILED_TO_LOAD_SCHEMA", catalog.definition ), e ); //$NON-NLS-1$

          }
        } else {
          MondrianCatalogHelper.logger.warn( Messages.getInstance().getString(
            "MondrianCatalogHelper.WARN_SKIPPING_DATASOURCE_DEF", catalog.definition ) ); //$NON-NLS-1$
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
  public MondrianSchema loadMondrianSchema( final String solutionLocation, final IPentahoSession pentahoSession ) {
    return makeSchema( docAtUrlToString( solutionLocation, pentahoSession ) );
  }

  protected String docAtUrlToString( final String urlStr, final IPentahoSession pentahoSession ) {
    // String relPath = getSolutionRepositoryRelativePath(urlStr, pentahoSession);

    String res = null;
    InputStream in = null;
    try {
      LocalizingDynamicSchemaProcessor schemaProcessor = new LocalizingDynamicSchemaProcessor();
      PropertyList localeInfo = new PropertyList();
      localeInfo.put( "Locale", getLocale().toString() ); //$NON-NLS-1$

      FileSystemManager fsManager = VFS.getManager();


      SolutionRepositoryVfsFileObject mondrianDS =
        (SolutionRepositoryVfsFileObject) fsManager.resolveFile( urlStr );

      in = mondrianDS.getInputStream();
      res = schemaProcessor.filter( null, localeInfo, in );
    } catch ( FileNotFoundException fnfe ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0007_FILE_NOT_FOUND" ), fnfe ); //$NON-NLS-1$
    } catch ( Exception e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0006_IO_PROBLEM" ), e ); //$NON-NLS-1$
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
        .debug( "makeSchema (catalogStr=" + catalogStr.substring( 0, Math.min( 40, catalogStr.length() ) ) + "...)"
      );
    }
    MondrianSchema schema = null;
    try {
      final Parser xmlParser = XOMUtil.createDefaultParser();
      final DOMWrapper def = xmlParser.parse( catalogStr );
      MondrianDef.Schema schemaFromXml = new MondrianDef.Schema( def );
      String schemaName = schemaFromXml.name;
      List<MondrianCube> mondrianCubes = new ArrayList<MondrianCube>();
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
          "MondrianCatalogHelper.ERROR_0008_ERROR_OCCURRED" ), e ); //$NON-NLS-1$
      }
      throw Util.newError( e, Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0009_WHILE_PARSING_CATALOG", catalogStr ) ); //$NON-NLS-1$
    }
    return schema;
  }

  protected static enum CatalogPermission {
    READ, WRITE
  }

  protected List<MondrianCatalog> filter( final List<MondrianCatalog> origList, final IPentahoSession pentahoSession,
                                          final boolean jndiOnly ) {
    List<MondrianCatalog> filtered = new ArrayList<MondrianCatalog>();
    for ( MondrianCatalog orig : origList ) {
      if ( hasAccess( orig, CatalogPermission.READ, pentahoSession ) && ( !jndiOnly || orig.isJndi() ) ) {
        filtered.add( orig );
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
  protected boolean hasAccess( final MondrianCatalog cat, final CatalogPermission perm,
                               final IPentahoSession pentahoSession ) {
    // IUnifiedRepository unifiedRepository = PentahoSystem.get(IUnifiedRepository.class);
    // unifiedRepository.hasAccess(null, null);
    return true;
  }

  protected String getSolutionRepositoryRelativePath( final String path, final IPentahoSession pentahoSession ) {

    try {
      FileSystemManager fsManager = VFS.getManager();
      return fsManager.resolveFile( path ).getName().getPath();
    } catch ( FileSystemException e ) {
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0012_FILESYSTEM_PROBLEM" ), e ); //$NON-NLS-1$
    }

  }

  public boolean isUseSchemaNameAsCatalogName() {
    return useSchemaNameAsCatalogName;
  }

  public void setUseSchemaNameAsCatalogName( final boolean useSchemaNameAsCatalogName ) {
    this.useSchemaNameAsCatalogName = useSchemaNameAsCatalogName;
  }

  public static int addToCatalog( String baseUrl, boolean enableXmla, String schemaSolutionPath,
                                  IPentahoSession session, String jndiName, boolean overwrite ) {

    IMondrianCatalogService mondrianCatalogService =
      PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService", session ); //$NON-NLS-1$

    String dsUrl = baseUrl;
    if ( !dsUrl.endsWith( "/" ) ) { //$NON-NLS-1$
      dsUrl += "/"; //$NON-NLS-1$
    }
    dsUrl += "Xmla"; //$NON-NLS-1$

    String catDef = "solution:" + schemaSolutionPath; //$NON-NLS-1$

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
          Messages.getInstance().getErrorString( "MondrianCatalogPublisher.ERROR_0001_JNDI_NAMING_ERROR", jndiName ),
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
        new ArrayList<MondrianCube>(), roleNames ) );

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
   * This method removes a mondrian catalog from the JCR repository.
   */
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
        "MondrianCatalogHelper.ERROR_0015_CATALOG_NOT_FOUND", catalogName ) ); //$NON-NLS-1$
    }

    //
    // do an access check first
    //

    if ( !hasAccess( catalog, CatalogPermission.WRITE, pentahoSession ) ) {
      if ( MondrianCatalogHelper.logger.isDebugEnabled() ) {
        MondrianCatalogHelper.logger.debug( "user does not have access; throwing exception" ); //$NON-NLS-1$
      }
      throw new MondrianCatalogServiceException( Messages.getInstance().getErrorString(
        "MondrianCatalogHelper.ERROR_0003_INSUFFICIENT_PERMISSION" ), Reason.ACCESS_DENIED ); //$NON-NLS-1$
    }

    IUnifiedRepository solutionRepository = PentahoSystem.get( IUnifiedRepository.class );
    RepositoryFile deletingFile = solutionRepository.getFile( RepositoryFile.SEPARATOR + "etc" //$NON-NLS-1$
      + RepositoryFile.SEPARATOR + "mondrian" + RepositoryFile.SEPARATOR + catalog.getName() ); //$NON-NLS-1$
    solutionRepository.deleteFile( deletingFile.getId(), true, "" ); //$NON-NLS-1$
    reInit( pentahoSession );
  }
}
