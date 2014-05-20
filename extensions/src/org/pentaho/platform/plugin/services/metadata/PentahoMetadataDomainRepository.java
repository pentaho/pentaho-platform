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

package org.pentaho.platform.plugin.services.metadata;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.LocalizationUtil;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository2.unified.RepositoryUtils;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Handles the storage and retrieval of Pentaho Metada Domain objects in a repository. It does this by using a
 * pre-defined system location (defined by {@link PentahoMetadataDomainRepositoryInfo}) as the storage location for the
 * Domain files and associated locale files. </p> Since Domain IDs are the unique identifier for Pentaho Metadata
 * domains and may contain any character (including repository folder separator character(s) like '/', a {@link UUID}
 * will be created to store each file. The metadata for the file will be used to store the information (such as the
 * Domain ID). </p>
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class PentahoMetadataDomainRepository implements IMetadataDomainRepository,
    IPentahoMetadataDomainRepositoryImporter, IPentahoMetadataDomainRepositoryExporter {
  // The logger for this class
  private static final Log logger = LogFactory.getLog( PentahoMetadataDomainRepository.class );

  // The messages object used in generating messages that may be seen by the user
  private static final Messages messages = Messages.getInstance();

  private static final Map<IUnifiedRepository, PentahoMetadataInformationMap> metaMapStore =
      new HashMap<IUnifiedRepository, PentahoMetadataInformationMap>();

  // The type of repository file (domain, locale)
  private static final String PROPERTY_NAME_TYPE = "file-type";

  private static final String TYPE_DOMAIN = "domain";
  private static final String TYPE_LOCALE = "locale";

  // The repository file metadata key used to store the file's domain id
  private static final String PROPERTY_NAME_DOMAIN_ID = "domain-id";

  // The repository file metadata key used to store the file's locale (properties files)
  private static final String PROPERTY_NAME_LOCALE = "locale";

  // The default encoding for file storage
  private static final String DEFAULT_ENCODING = "UTF-8";

  // The default mime-type for the Pentaho Domain files
  private static final String DOMAIN_MIME_TYPE = "text/xml";

  // The default mime-type for locale files
  private static final String LOCALE_MIME_TYPE = "text/plain";

  // The repository used to store / retrieve objects
  private IUnifiedRepository repository;

  // Mapping between the Pentaho Metadata Domain ID and the repository files for that Domain
  private final PentahoMetadataInformationMap metadataMapping;

  // The parser used to serialize / deserialize metadata files
  private XmiParser xmiParser;

  // The repository utility class
  private RepositoryUtils repositoryUtils;

  // The localization utility class (used to load side-car properties files into a Domain object)
  private LocalizationUtil localizationUtil;

  /**
   * Creates an instance of this class providing the {@link IUnifiedRepository} repository backend.
   * 
   * @param repository
   *          the {@link IUnifiedRepository} in which data will be stored / retrieved
   */
  public PentahoMetadataDomainRepository( final IUnifiedRepository repository ) {
    this( repository, null, null, null );
  }

  /**
   * Helper constructor used for setting other objects in this class
   * 
   * @param repository
   *          the {@link IUnifiedRepository} in which data will be stored / retrieved
   * @param repositoryUtils
   *          utility class for working inside the repository </br>(NOTE: {@code null} is acceptable and will create a
   *          default instance)
   * @param xmiParser
   *          the parser class for serializing / de-serializing Domain objects </br>(NOTE: {@code null} is acceptable
   *          and will create a default instance)
   * @param localizationUtil
   *          the object used to add locale bundles into a Pentaho Metadata Domain object </br>(NOTE: {@code null} is
   *          acceptable and will create a default instance)
   */
  protected PentahoMetadataDomainRepository( final IUnifiedRepository repository,
      final RepositoryUtils repositoryUtils, final XmiParser xmiParser, final LocalizationUtil localizationUtil ) {
    if ( null == repository ) {
      throw new IllegalArgumentException();
    }
    this.metadataMapping = getMetadataMapping( repository );
    setRepository( repository );
    setRepositoryUtils( repositoryUtils );
    setLocalizationUtil( localizationUtil );
    setXmiParser( xmiParser );
  }

  /**
   * Store a domain to the repository. The domain should persist between JVM restarts.
   * 
   * @param domain
   *          domain object to store
   * @param overwrite
   *          if true, overwrite existing domain
   * @throws DomainIdNullException
   *           if domain id is null or empty
   * @throws DomainAlreadyExistsException
   *           if a domain with the same Domain ID already exists in the repository and {@code overwrite == false}
   * @throws DomainStorageException
   *           if there is a problem storing the domain
   */
  @Override
  public void storeDomain( final Domain domain, final boolean overwrite ) throws DomainIdNullException,
    DomainAlreadyExistsException, DomainStorageException {
    logger.debug( "storeDomain(domain(id=" + ( domain != null ? domain.getId() : "" ) + ", " + overwrite + ")" );
    if ( null == domain || StringUtils.isEmpty( domain.getId() ) ) {
      throw new DomainIdNullException( messages
          .getErrorString( "PentahoMetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL" ) );
    }

    String xmi = "";
    try {
      // NOTE - a ByteArrayInputStream doesn't need to be closed ...
      // ... so this is safe AS LONG AS we use a ByteArrayInputStream
      xmi = xmiParser.generateXmi( domain );
      //final InputStream inputStream = new ByteArrayInputStream( xmi.getBytes( DEFAULT_ENCODING ) );
      final InputStream inputStream = new ByteArrayInputStream( xmi.getBytes( "UTF8" ) );
      storeDomain( inputStream, domain.getId(), overwrite );
    } catch ( DomainStorageException dse ) {
      throw dse;
    } catch ( DomainAlreadyExistsException dae ) {
      throw dae;
    } catch ( Exception e ) {
      final String errorMessage =
          messages.getErrorString( "PentahoMetadataDomainRepository.ERROR_0003_ERROR_STORING_DOMAIN", domain.getId(), e
              .getLocalizedMessage() );
      logger.error( errorMessage, e );
      throw new DomainStorageException( xmi + errorMessage, e );
    }
  }

  /**
   * Stores a domain to the repository directly as an Input Stream
   * 
   * @param inputStream
   * @param domainId
   * @param overwrite
   */
  @Override
  public void storeDomain( final InputStream inputStream, final String domainId, final boolean overwrite )
    throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
    logger.debug( "storeDomain(inputStream, " + domainId + ", " + overwrite + ")" );
    if ( null == inputStream ) {
      throw new IllegalArgumentException();
    }
    if ( StringUtils.isEmpty( domainId ) ) {
      throw new DomainIdNullException( messages
          .getErrorString( "PentahoMetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL" ) );
    }

    // Check to see if the domain already exists
    final RepositoryFile domainFile = getMetadataRepositoryFile( domainId );
    if ( !overwrite && domainFile != null ) {
      final String errorString =
          messages.getErrorString( "PentahoMetadataDomainRepository.ERROR_0002_DOMAIN_ALREADY_EXISTS", domainId );
      logger.error( errorString );
      throw new DomainAlreadyExistsException( errorString );
    }

    // Check if this is valid xml
    InputStream inputStream2 = null;
    String xmi = null;
    try {
      // try to see if the xmi can be parsed (ie, check if it's valid xmi)
      // first, convert our input stream to a string
      BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream, DEFAULT_ENCODING ) );
      StringBuilder stringBuilder = new StringBuilder();
      while ( ( xmi = reader.readLine() ) != null ) {
        stringBuilder.append( xmi );
      }
      inputStream.close();
      xmi = stringBuilder.toString();
      // now, try to see if the xmi can be parsed (ie, check if it's valid xmi)
      Domain domain = xmiParser.parseXmi( new java.io.ByteArrayInputStream( xmi.getBytes( DEFAULT_ENCODING ) ) );
      // xmi is valid. Create a new inputstream for the actual import action.
      inputStream2 = new java.io.ByteArrayInputStream( xmi.getBytes( DEFAULT_ENCODING ) );
    } catch ( Exception ex ) {
      logger.error( ex.getMessage() );
      // throw new
      // DomainStorageException(messages.getErrorString("PentahoMetadataDomainRepository.ERROR_0010_ERROR_PARSING_XMI"),
      // ex);
      java.io.ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ex.printStackTrace( new java.io.PrintStream( byteArrayOutputStream ) );
      throw new DomainStorageException( byteArrayOutputStream.toString(), ex );
    }

    final SimpleRepositoryFileData data =
        new SimpleRepositoryFileData( inputStream2, DEFAULT_ENCODING, DOMAIN_MIME_TYPE );
    if ( domainFile == null ) {
      final RepositoryFile newDomainFile = createUniqueFile( domainId, null, data );
    } else {
      repository.updateFile( domainFile, data, null );
    }

    // This invalidates any caching
    flushDomains();

  }

  /*
   * retrieves the data streams for the metadata referenced by domainId. This could be a single .xmi file or an .xmi
   * file and multiple .properties files.
   */
  public Map<String, InputStream> getDomainFilesData( final String domainId ) {
    Map<String, InputStream> values = new HashMap<String, InputStream>();
    Set<RepositoryFile> metadataFiles;
    synchronized ( metadataMapping ) {
      metadataFiles = metadataMapping.getFiles( domainId );
    }
    for ( RepositoryFile repoFile : metadataFiles ) {
      RepositoryFileInputStream is;
      try {
        is = new RepositoryFileInputStream( repoFile );
      } catch ( Exception e ) {
        return null; // This pretty much ensures an exception will be thrown later and passed to the client
      }
      String fileName = repoFile.getName().endsWith( ".properties" ) ? repoFile.getName()
          : domainId + ( domainId.endsWith( ".xmi" ) ? "" : ".xmi" );
      values.put( fileName, is );
    }
    return values;
  }

  /**
   * retrieve a domain from the repo. This does lazy loading of the repo, so it calls reloadDomains() if not already
   * loaded.
   * 
   * @param domainId
   *          domain to get from the repository
   * @return domain object
   */
  @Override
  public Domain getDomain( final String domainId ) {
    logger.debug( "getDomain(" + domainId + ")" );
    if ( StringUtils.isEmpty( domainId ) ) {
      throw new IllegalArgumentException( messages.getErrorString(
          "PentahoMetadataDomainRepository.ERROR_0004_DOMAIN_ID_INVALID", domainId ) );
    }

    Domain domain = null;
    try {
      // Load the domain file
      final RepositoryFile file = getMetadataRepositoryFile( domainId );
      if ( file != null ) {
        SimpleRepositoryFileData data = repository.getDataForRead( file.getId(), SimpleRepositoryFileData.class );
        if ( data != null ) {
          domain = xmiParser.parseXmi( data.getStream() );
          domain.setId( domainId );
          logger.debug( "loaded domain" );

          // Load any I18N bundles
          loadLocaleStrings( domainId, domain );
          logger.debug( "loaded I18N bundles" );
        } else {
          throw new UnifiedRepositoryException( messages.getErrorString(
              "PentahoMetadataDomainRepository.ERROR_0005_ERROR_RETRIEVING_DOMAIN", domainId, "not found" ) );

        }
      }
    } catch ( Exception e ) {
      throw new UnifiedRepositoryException( messages.getErrorString(
          "PentahoMetadataDomainRepository.ERROR_0005_ERROR_RETRIEVING_DOMAIN",
        domainId, e.getLocalizedMessage() ), e );
    }

    // Return
    return domain;
  }

  /**
   * return a list of all the domain ids in the repository. triggers a call to reloadDomains if necessary.
   * 
   * @return the domain Ids.
   */
  @Override
  public Set<String> getDomainIds() {
    logger.debug( "getDomainIds()" );
    internalReloadDomains();
    final Set<String> domainIds = new HashSet<String>();
    synchronized ( metadataMapping ) {
      domainIds.addAll( metadataMapping.getDomainIds() );
      return domainIds;
    }
  }

  /**
   * remove a domain from disk and memory.
   * 
   * @param domainId
   */
  @Override
  public void removeDomain( final String domainId ) {
    logger.debug( "removeDomain(" + domainId + ")" );
    if ( StringUtils.isEmpty( domainId ) ) {
      throw new IllegalArgumentException( messages.getErrorString(
          "PentahoMetadataDomainRepository.ERROR_0004_DOMAIN_ID_INVALID", domainId ) );
    }

    // Get the metadata domain file
    Set<RepositoryFile> domainFiles;
    synchronized ( metadataMapping ) {
      domainFiles = metadataMapping.getFiles( domainId );
      metadataMapping.deleteDomain( domainId );
    }

    for ( final RepositoryFile file : domainFiles ) {
      if ( logger.isTraceEnabled() ) {
        logger.trace( "Deleting repository file " + toString( file ) );
      }
      repository.deleteFile( file.getId(), true, null );
    }

    // This invalidates any caching
    if ( !domainFiles.isEmpty() ) {
      flushDomains();
    }

  }

  /**
   * remove a model from a domain which is stored either on a disk or memory.
   * 
   * @param domainId
   * @param modelId
   */
  @Override
  public void removeModel( final String domainId, final String modelId ) throws DomainIdNullException,
    DomainStorageException {
    logger.debug( "removeModel(" + domainId + ", " + modelId + ")" );
    if ( StringUtils.isEmpty( domainId ) ) {
      throw new IllegalArgumentException( messages.getErrorString(
          "PentahoMetadataDomainRepository.ERROR_0004_DOMAIN_ID_INVALID", domainId ) );
    }
    if ( StringUtils.isEmpty( modelId ) ) {
      throw new IllegalArgumentException( messages
          .getErrorString( "PentahoMetadataDomainRepository.ERROR_0006_MODEL_ID_INVALID" ) );
    }

    // Get the domain and remove the model
    final Domain domain = getDomain( domainId );
    if ( null != domain ) {
      boolean found = false;
      final Iterator<LogicalModel> iter = domain.getLogicalModels().iterator();
      while ( iter.hasNext() ) {
        LogicalModel model = iter.next();
        if ( modelId.equals( model.getId() ) ) {
          iter.remove();
          found = true;
          break;
        }
      }

      // Update the domain if we change it
      if ( found ) {
        try {
          storeDomain( domain, true );
          flushDomains();
        } catch ( DomainAlreadyExistsException ignored ) {
          // This can't happen since we have setup overwrite to true
        }
      }
    }
  }

  /**
   * reload domains from disk
   */
  @Override
  public void reloadDomains() {
    logger.debug( "reloadDomains()" );
    internalReloadDomains();
  }

  /**
   * Performs the process of reloading the domain information from the repository
   */
  private void internalReloadDomains() {
    synchronized ( metadataMapping ) {
      metadataMapping.reset();

      // Reload the metadata about the metadata (that was fun to say)
      final List<RepositoryFile> children = repository.getChildren( getMetadataDir().getId(), "*" );
      logger.trace( "\tFound " + children.size() + " files in the repository" );
      for ( final RepositoryFile child : children ) {
        // Get the metadata for this file
        final Map<String, Serializable> fileMetadata = repository.getFileMetadata( child.getId() );
        if ( fileMetadata == null || StringUtils.isEmpty( (String) fileMetadata.get( PROPERTY_NAME_DOMAIN_ID ) ) ) {
          logger.warn( messages.getString( "PentahoMetadataDomainRepository.WARN_0001_FILE_WITHOUT_METADATA", child
              .getName() ) );
          continue;
        }
        final String domainId = (String) fileMetadata.get( PROPERTY_NAME_DOMAIN_ID );
        final String type = (String) fileMetadata.get( PROPERTY_NAME_TYPE );
        final String locale = (String) fileMetadata.get( PROPERTY_NAME_LOCALE );
        if ( logger.isTraceEnabled() ) {
          logger.trace( "\tprocessing file [type=" + type + " : domainId=" + domainId + " : locale=" + locale + "]" );
        }

        // Save the data in the map
        if ( StringUtils.equals( type, TYPE_DOMAIN ) ) {
          metadataMapping.addDomain( domainId, child );
        } else if ( StringUtils.equals( type, TYPE_LOCALE ) ) {
          metadataMapping.addLocale( domainId, locale, child );
        }
      }
    }
  }

  /**
   * flush the domains from memory
   */
  @Override
  public void flushDomains() {
    logger.debug( "flushDomains()" );
    internalReloadDomains();
  }

  @Override
  public String generateRowLevelSecurityConstraint( final LogicalModel model ) {
    // We will let subclasses handle this issue
    return null;
  }

  /**
   * The aclHolder cannot be null unless the access type requested is ACCESS_TYPE_SCHEMA_ADMIN.
   */
  @Override
  public boolean hasAccess( final int accessType, final IConcept aclHolder ) {
    // We will let subclasses handle this computation
    return true;
  }

  /**
   * Adds a set of properties as a locale properties file for the specified Domain ID
   * 
   * @param domainId
   *          the domain ID for which this properties file will be added
   * @param locale
   *          the locale for which this properties file will be added
   * @param properties
   *          the properties to be added
   */
  public void addLocalizationFile( final String domainId, final String locale, final Properties properties )
    throws DomainStorageException {
    // This is safe since ByteArray streams don't have to be closed
    if ( null != properties ) {
      try {
        final OutputStream out = new ByteArrayOutputStream();
        properties.store( out, null );
        addLocalizationFile( domainId, locale, new ByteArrayInputStream( out.toString().getBytes() ), true );
      } catch ( IOException e ) {
        throw new DomainStorageException( messages.getErrorString(
            "PentahoMetadataDomainRepository.ERROR_0008_ERROR_IN_REPOSITORY", e.getLocalizedMessage() ), e );
      }
    }
  }

  @Override
  public void addLocalizationFile( final String domainId, final String locale, final InputStream inputStream,
      final boolean overwrite ) throws DomainStorageException {
    logger.debug( "addLocalizationFile(" + domainId + ", " + locale + ", inputStream)" );
    if ( null != inputStream ) {
      if ( StringUtils.isEmpty( domainId ) || StringUtils.isEmpty( locale ) ) {
        throw new IllegalArgumentException( messages.getErrorString(
            "PentahoMetadataDomainRepository.ERROR_0004_DOMAIN_ID_INVALID", domainId ) );
      }

      synchronized ( metadataMapping ) {
        // Check for duplicates
        final RepositoryFile localeFile = metadataMapping.getLocaleFile( domainId, locale );
        if ( !overwrite && localeFile != null ) {
          throw new DomainStorageException( messages.getErrorString(
              "PentahoMetadataDomainRepository.ERROR_0009_LOCALE_ALREADY_EXISTS", domainId, locale ), null );
        }

        final SimpleRepositoryFileData data =
            new SimpleRepositoryFileData( inputStream, DEFAULT_ENCODING, LOCALE_MIME_TYPE );
        if ( localeFile == null ) {
          final RepositoryFile newLocaleFile = createUniqueFile( domainId, locale, data );
          metadataMapping.addLocale( domainId, locale, newLocaleFile );
        } else {
          repository.updateFile( localeFile, data, null );
        }
      }

      // This invalidates any cached information
      flushDomains();
    }
  }

  protected RepositoryFile getMetadataDir() {
    final String metadataDirName = PentahoMetadataDomainRepositoryInfo.getMetadataFolderPath();
    return repository.getFile( metadataDirName );
  }

  protected void loadLocaleStrings( final String domainId, final Domain domain ) {
    final Map<String, RepositoryFile> localeFiles = metadataMapping.getLocaleFiles( domainId );
    if ( localeFiles != null ) {
      for ( final String locale : localeFiles.keySet() ) {
        final RepositoryFile localeFile = localeFiles.get( locale );
        final Properties properties = loadProperties( localeFile );
        logger.trace( "\tLoading properties [" + domain + " : " + locale + "]" );
        localizationUtil.importLocalizedProperties( domain, properties, locale );
      }
    }
  }

  protected Properties loadProperties( final RepositoryFile bundle ) {
    try {
      Properties properties = null;
      final SimpleRepositoryFileData bundleData =
          repository.getDataForRead( bundle.getId(), SimpleRepositoryFileData.class );
      if ( bundleData != null ) {
        properties = new Properties();
        properties.load( bundleData.getStream() );
      } else {
        logger.warn( "Could not load properties from repository file: " + bundle.getName() );
      }
      return properties;
    } catch ( IOException e ) {
      throw new UnifiedRepositoryException( messages.getErrorString(
          "PentahoMetadataDomainRepository.ERROR_0008_ERROR_IN_REPOSITORY", e.getLocalizedMessage() ), e );
    }
  }

  /**
   * Creates a new repository file (with the supplied data) and applies the proper metadata to this file.
   * 
   * @param domainId
   *          the Domain id associated with this file
   * @param locale
   *          the locale associated with this file (or null for a domain file)
   * @param data
   *          the data to put in the file
   * @return the repository file created
   */
  protected RepositoryFile createUniqueFile( final String domainId, final String locale,
      final SimpleRepositoryFileData data ) {
    // Generate a "unique" filename
    final String filename = UUID.randomUUID().toString();

    // Create the new file
    final RepositoryFile file =
        repository.createFile( getMetadataDir().getId(), new RepositoryFile.Builder( filename ).build(), data, null );

    // Add metadata to the file
    final Map<String, Serializable> metadataMap = new HashMap<String, Serializable>();
    metadataMap.put( PROPERTY_NAME_DOMAIN_ID, domainId );
    if ( StringUtils.isEmpty( locale ) ) {
      // This is a domain file
      metadataMap.put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
    } else {
      // This is a locale property file
      metadataMap.put( PROPERTY_NAME_TYPE, TYPE_LOCALE );
      metadataMap.put( PROPERTY_NAME_LOCALE, locale );
    }

    // Update the metadata
    repository.setFileMetadata( file.getId(), metadataMap );
    return file;
  }

  protected IUnifiedRepository getRepository() {
    return repository;
  }

  protected XmiParser getXmiParser() {
    return xmiParser;
  }

  protected RepositoryUtils getRepositoryUtils() {
    return repositoryUtils;
  }

  protected LocalizationUtil getLocalizationUtil() {
    return localizationUtil;
  }

  protected void setRepository( final IUnifiedRepository repository ) {
    this.repository = repository;
  }

  protected void setXmiParser( final XmiParser xmiParser ) {
    this.xmiParser = ( xmiParser != null ? xmiParser : new XmiParser() );
  }

  protected void setRepositoryUtils( final RepositoryUtils repositoryUtils ) {
    this.repositoryUtils = ( repositoryUtils != null ? repositoryUtils : new RepositoryUtils( repository ) );
  }

  protected void setLocalizationUtil( final LocalizationUtil localizationUtil ) {
    this.localizationUtil = ( localizationUtil != null ? localizationUtil : new LocalizationUtil() );
  }

  protected String toString( final RepositoryFile file ) {
    try {
      final Map<String, Serializable> fileMetadata = repository.getFileMetadata( file.getId() );
      return "[type=" + fileMetadata.get( PROPERTY_NAME_TYPE ) + " : domain="
          + fileMetadata.get( PROPERTY_NAME_DOMAIN_ID ) + " : locale=" + fileMetadata.get( PROPERTY_NAME_LOCALE )
          + " : filename=" + file.getName() + "]";
    } catch ( Throwable ignore ) {
      //ignore
    }
    return "null";
  }

  /**
   * Accesses the metadata mapping (with 1 retry) to find the metadata file for the specified domainId
   */
  protected RepositoryFile getMetadataRepositoryFile( final String domainId ) {
    RepositoryFile domainFile = metadataMapping.getDomainFile( domainId );
    if ( null == domainFile ) {
      reloadDomains();
      domainFile = metadataMapping.getDomainFile( domainId );
    }
    return domainFile;
  }

  /**
   * Returns the MatadataInformationMap for the specified IUnifiedRepository
   * 
   * @param repository
   *          the repository for which a map is specified
   * @return the MatadataInformationMap for the repository
   */
  private static synchronized PentahoMetadataInformationMap getMetadataMapping( final IUnifiedRepository repository ) {
    PentahoMetadataInformationMap metaMap = metaMapStore.get( repository );
    if ( null == metaMap ) {
      metaMap = new PentahoMetadataInformationMap();
      metaMapStore.put( repository, metaMap );
    }
    return metaMap;
  }
}
