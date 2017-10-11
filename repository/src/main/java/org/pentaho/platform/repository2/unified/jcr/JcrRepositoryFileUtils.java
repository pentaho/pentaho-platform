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
 * Copyright 2006 - 2017 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.Lock;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.VersionManagerImpl;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.exception.RepositoryFileDaoMalformedNameException;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategySessionFactory;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Class of static methods where the real JCR work takes place.
 * 
 * @author mlowery
 */
public class JcrRepositoryFileUtils {

  private static final Log logger = LogFactory.getLog( JcrRepositoryFileUtils.class );

  /**
   * See section 4.6 "Path Syntax" of JCR 1.0 spec. Note that this list is only characters that can never appear in a
   * "simplename". It does not include '.' because, while "." and ".." are illegal, any other string containing '.' is
   * legal. It is up to this implementation to prohibit permutations of legal characters.
   */
  // private static final List<Character> reservedChars = Collections.unmodifiableList( Arrays.asList( new Character[] {
  // '/', ':', '[', ']', '*', '\'', '"', '|', '\t', '\r', '\n' } ) );

  // This list will drive what characters are not allowed on the client as well as the server
  private static List<Character> reservedChars =
      Collections.unmodifiableList( Arrays.asList( new Character[] { '/', '\\', '\t', '\r', '\n' } ) );

  private static IRepositoryVersionManager repositoryVersionManager = null;

  /**
   * Try to get parameters from PentahoSystem, otherwise use default
   */
  static {
    List<Character> newOverrideReservedChars =
        PentahoSystem.get( ArrayList.class, "reservedChars", PentahoSessionHolder.getSession() );

    if ( newOverrideReservedChars != null ) {
      reservedChars = newOverrideReservedChars;
    }
  }

  private static Pattern makePattern( List<Character> list ) {
    // escape all reserved characters as they may have special meaning to regex engine
    StringBuilder buf = new StringBuilder();
    buf.append( ".*" ); //$NON-NLS-1$
    buf.append( "[" ); //$NON-NLS-1$
    for ( Character ch : list ) {
      buf.append( "\\" ); //$NON-NLS-1$
      buf.append( ch );
    }
    buf.append( "]" ); //$NON-NLS-1$
    buf.append( "+" ); //$NON-NLS-1$
    buf.append( ".*" ); //$NON-NLS-1$
    return Pattern.compile( buf.toString() );
  }

  public static RepositoryFile getFileById( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Serializable fileId )
    throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    Assert.notNull( fileNode );
    return nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, fileNode );
  }

  public static RepositoryFile nodeToFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Node node )
    throws RepositoryException {
    return nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, node, false, null );
  }

  private static RepositoryFile getRootFolder( final Session session ) throws RepositoryException {
    Node node = session.getRootNode();
    RepositoryFile file = new RepositoryFile.Builder( node.getIdentifier(), "" ).folder( true ).versioned( false ).path( //$NON-NLS-1$
        JcrStringHelper.pathDecode( node.getPath() ) ).build();
    return file;
  }

  public static RepositoryFile nodeToFileOld( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Node node,
      final boolean loadMaps, IPentahoLocale pentahoLocale ) throws RepositoryException {

    if ( session.getRootNode().isSame( node ) ) {
      return getRootFolder( session );
    }

    Serializable id = null;
    String name = null;
    String path = null;
    long fileSize = 0;
    Date created = null;
    String creatorId = null;
    Boolean hidden = RepositoryFile.HIDDEN_BY_DEFAULT;
    Boolean schedulable = RepositoryFile.SCHEDULABLE_BY_DEFAULT;
    Date lastModified = null;
    boolean folder = false;
    boolean versioned = false;
    Serializable versionId = null;
    boolean locked = false;
    String lockOwner = null;
    Date lockDate = null;
    String lockMessage = null;
    String title = null;
    String description = null;
    Boolean aclNode = false;
    Map<String, Properties> localePropertiesMap = null;

    id = getNodeId( session, pentahoJcrConstants, node );

    if ( logger.isDebugEnabled() ) {
      logger.debug( String.format( "reading file with id '%s' and path '%s'", id, node.getPath() ) ); //$NON-NLS-1$
    }

    path = pathConversionHelper.absToRel( ( getAbsolutePath( session, pentahoJcrConstants, node ) ) );
    // if the rel path is / then name the folder empty string instead of its true name (this hides the tenant name)
    name = RepositoryFile.SEPARATOR.equals( path ) ? "" : getNodeName( session, pentahoJcrConstants, node ); //$NON-NLS-1$

    if ( isPentahoFolder( pentahoJcrConstants, node ) ) {
      folder = true;
    }

    // jcr:created nodes have OnParentVersion values of INITIALIZE
    if ( node.hasProperty( pentahoJcrConstants.getJCR_CREATED() ) ) {
      Calendar tmpCal = node.getProperty( pentahoJcrConstants.getJCR_CREATED() ).getDate();
      if ( tmpCal != null ) {
        created = tmpCal.getTime();
      }
    }

    // Expensive
    Map<String, Serializable> metadata = getFileMetadata( session, id );
    if ( metadata != null ) {
      creatorId = (String) metadata.get( PentahoJcrConstants.PHO_CONTENTCREATOR );
      Serializable schedulableValue = metadata.get( RepositoryFile.SCHEDULABLE_KEY );
      if ( schedulableValue instanceof String ) {
        schedulable = BooleanUtils.toBoolean( (String) schedulableValue );
      }
    }
    if ( node.hasProperty( pentahoJcrConstants.getPHO_HIDDEN() ) ) {
      hidden = node.getProperty( pentahoJcrConstants.getPHO_HIDDEN() ).getBoolean();
    }
    if ( node.hasProperty( pentahoJcrConstants.getPHO_FILESIZE() ) ) {
      fileSize = node.getProperty( pentahoJcrConstants.getPHO_FILESIZE() ).getLong();
    }
    if ( node.hasProperty( pentahoJcrConstants.getPHO_ACLNODE() ) ) {
      aclNode = node.getProperty( pentahoJcrConstants.getPHO_ACLNODE() ).getBoolean();
    }
    if ( isPentahoFile( pentahoJcrConstants, node ) ) {
      // pho:lastModified nodes have OnParentVersion values of IGNORE; i.e. they don't exist in frozen nodes
      if ( !node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
        Calendar tmpCal = node.getProperty( pentahoJcrConstants.getPHO_LASTMODIFIED() ).getDate();
        if ( tmpCal != null ) {
          lastModified = tmpCal.getTime();
        }
      }
    }

    // Get default locale if null
    if ( pentahoLocale == null ) {
      Locale currentLocale = LocaleHelper.getLocale();
      if ( currentLocale != null ) {
        pentahoLocale = new PentahoLocale( currentLocale );
      } else {
        pentahoLocale = new PentahoLocale();
      }
    }

    // Not needed for content generators and the like
    if ( isPentahoHierarchyNode( session, pentahoJcrConstants, node ) ) {
      if ( node.hasNode( pentahoJcrConstants.getPHO_LOCALES() ) ) {
        // Expensive
        localePropertiesMap =
            getLocalePropertiesMap( session, pentahoJcrConstants, node.getNode( pentahoJcrConstants
                .getPHO_LOCALES() ) );

        // [BISERVER-8337] localize title and description
        LocalePropertyResolver lpr = new LocalePropertyResolver( name );
        LocalizationUtil localizationUtil = new LocalizationUtil( localePropertiesMap, pentahoLocale.getLocale() );
        title = localizationUtil.resolveLocalizedString( lpr.resolveDefaultTitleKey(), null );
        if ( org.apache.commons.lang.StringUtils.isBlank( title ) ) {
          title = localizationUtil.resolveLocalizedString( lpr.resolveTitleKey(), null );
          if ( org.apache.commons.lang.StringUtils.isBlank( title ) ) {
            title = localizationUtil.resolveLocalizedString( lpr.resolveNameKey(), title );
          }
        }
        description = localizationUtil.resolveLocalizedString( lpr.resolveDefaultDescriptionKey(), null );
        if ( org.apache.commons.lang.StringUtils.isBlank( description ) ) {
          description = localizationUtil.resolveLocalizedString( lpr.resolveDescriptionKey(), description );
        }
      }

      // BISERVER-8609 - Backwards compatibility. Fallback to the old data structure if title/description are not
      // found
      if ( title == null && node.hasNode( pentahoJcrConstants.getPHO_TITLE() ) ) {
        title =
            getLocalizedString( session, pentahoJcrConstants, node.getNode( pentahoJcrConstants.getPHO_TITLE() ),
                pentahoLocale );
      }
      if ( description == null && node.hasNode( pentahoJcrConstants.getPHO_DESCRIPTION() ) ) {
        description =
            getLocalizedString( session, pentahoJcrConstants, node.getNode( pentahoJcrConstants.getPHO_DESCRIPTION() ),
                pentahoLocale );
      }

    }

    if ( !loadMaps ) {
      localePropertiesMap = null; // remove reference, allow garbage collection
    }

    versioned = isVersioned( session, pentahoJcrConstants, node );
    if ( versioned ) {
      versionId = getVersionId( session, pentahoJcrConstants, node );
    }

    locked = isLocked( pentahoJcrConstants, node );
    if ( locked ) {
      Lock lock = session.getWorkspace().getLockManager().getLock( node.getPath() );
      lockOwner = lockHelper.getLockOwner( session, pentahoJcrConstants, lock );
      lockDate = lockHelper.getLockDate( session, pentahoJcrConstants, lock );
      lockMessage = lockHelper.getLockMessage( session, pentahoJcrConstants, lock );
    }

    RepositoryFile file =
        new RepositoryFile.Builder( id, name ).createdDate( created ).creatorId( creatorId ).lastModificationDate(
            lastModified ).folder( folder ).versioned( versioned ).path( path ).versionId( versionId ).fileSize(
                fileSize ).locked( locked ).lockDate( lockDate ).hidden( hidden ).schedulable( schedulable )
            .lockMessage( lockMessage ).lockOwner( lockOwner ).title( title ).description( description ).locale(
                pentahoLocale.toString() ).localePropertiesMap( localePropertiesMap ).aclNode( aclNode ).build();

    return file;
  }

  public static RepositoryFile nodeToFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Node node,
      final boolean loadMaps, IPentahoLocale pentahoLocale ) throws RepositoryException {

    if ( session.getRootNode().isSame( node ) ) {
      return getRootFolder( session );
    }
    // Get default locale if null
    if ( pentahoLocale == null ) {
      Locale currentLocale = LocaleHelper.getLocale();
      if ( currentLocale != null ) {
        pentahoLocale = new PentahoLocale( currentLocale );
      } else {
        pentahoLocale = new PentahoLocale();
      }
    }
    return getRepositoryFileProxyFactory().getProxy( node, pentahoLocale );
  }

  private static RepositoryFileProxyFactory fileProxyFactory;

  private static RepositoryFileProxyFactory getRepositoryFileProxyFactory() {
    if ( fileProxyFactory == null ) {
      fileProxyFactory = PentahoSystem.get( RepositoryFileProxyFactory.class );
    }
    return fileProxyFactory;
  }

  public static String getLocalizedString( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node localizedStringNode, IPentahoLocale pentahoLocale ) throws RepositoryException {
    Assert.isTrue( isLocalizedString( session, pentahoJcrConstants, localizedStringNode ) );

    boolean isLocaleNull = pentahoLocale == null;

    if ( pentahoLocale == null ) {
      pentahoLocale = new PentahoLocale();
    }

    Locale locale = pentahoLocale.getLocale();

    final String UNDERSCORE = "_"; //$NON-NLS-1$
    final String COLON = ":"; //$NON-NLS-1$
    boolean hasLanguage = StringUtils.hasText( locale.getLanguage() );
    boolean hasCountry = StringUtils.hasText( locale.getCountry() );
    boolean hasVariant = StringUtils.hasText( locale.getVariant() );

    List<String> candidatePropertyNames = new ArrayList<String>( 3 );

    if ( hasVariant ) {
      candidatePropertyNames.add( locale.getLanguage() + UNDERSCORE + locale.getCountry() + UNDERSCORE + locale
          .getVariant() );
    }
    if ( hasCountry ) {
      candidatePropertyNames.add( locale.getLanguage() + UNDERSCORE + locale.getCountry() );
    }
    if ( hasLanguage ) {
      candidatePropertyNames.add( locale.getLanguage() );
    }

    for ( String propertyName : candidatePropertyNames ) {
      if ( localizedStringNode.hasProperty( propertyName ) ) {
        return localizedStringNode.getProperty( propertyName ).getString();
      }
    }

    String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS );
    Assert.hasText( prefix );

    String propertyStr = isLocaleNull ? pentahoJcrConstants.getPHO_ROOTLOCALE() : prefix + COLON + locale.getLanguage();

    return localizedStringNode.getProperty( propertyStr ).getString();
  }

  public static Map<String, Properties> getLocalePropertiesMap( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node localesNode ) throws RepositoryException {

    String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS );
    Assert.hasText( prefix );

    Map<String, Properties> localePropertiesMap = new HashMap<String, Properties>();

    NodeIterator nodeItr = localesNode.getNodes();
    while ( nodeItr.hasNext() ) {
      Node node = nodeItr.nextNode();

      String locale = node.getName();
      Properties properties = new Properties();
      PropertyIterator propertyIterator = node.getProperties();
      while ( propertyIterator.hasNext() ) {
        Property property = propertyIterator.nextProperty();
        if ( !property.isMultiple() ) {
          properties.put( property.getName(), property.getValue().getString() );
        }
      }
      localePropertiesMap.put( locale, properties );
    }
    return localePropertiesMap;
  }

  private static void setLocalePropertiesMap( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node localeRootNode, final Map<String, Properties> localePropertiesMap ) throws RepositoryException {
    String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS );
    Assert.hasText( prefix );

    if ( localePropertiesMap != null && !localePropertiesMap.isEmpty() ) {
      for ( String locale : localePropertiesMap.keySet() ) {
        Properties properties = localePropertiesMap.get( locale );
        if ( properties != null ) {
          // create node and set properties for each locale
          Node localeNode;
          if ( !NodeHelper.checkHasNode( localeRootNode, locale ) ) {
            localeNode = localeRootNode.addNode( locale, pentahoJcrConstants.getNT_UNSTRUCTURED() );
          } else {
            localeNode = NodeHelper.checkGetNode( localeRootNode, locale );
          }
          for ( String propertyName : properties.stringPropertyNames() ) {
            try {
              localeNode.setProperty( propertyName, properties.getProperty( propertyName ) );
            } catch ( Throwable th ) {
              // Continue setting other properties
              continue;
            }
          }
        }
      }
    }
  }

  private static Map<String, String> getLocalizedStringMap( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node localizedStringNode ) throws RepositoryException {
    Assert.isTrue( isLocalizedString( session, pentahoJcrConstants, localizedStringNode ) );

    String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS );
    Assert.hasText( prefix );

    Map<String, String> localizedStringMap = new HashMap<String, String>();
    PropertyIterator propertyIter = localizedStringNode.getProperties();

    // Loop through properties and append the appropriate values in the map
    while ( propertyIter.hasNext() ) {
      Property property = propertyIter.nextProperty();
      String propertyKey = property.getName().substring( prefix.length() + 1 );

      localizedStringMap.put( propertyKey, property.getString() );
    }

    return localizedStringMap;
  }

  /**
   * Sets localized string.
   */
  private static void setLocalizedStringMap( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node localizedStringNode, final Map<String, String> map ) throws RepositoryException {
    Assert.isTrue( isLocalizedString( session, pentahoJcrConstants, localizedStringNode ) );

    String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS );
    Assert.hasText( prefix );
    PropertyIterator propertyIter = localizedStringNode.getProperties();
    while ( propertyIter.hasNext() ) {
      Property prop = propertyIter.nextProperty();
      if ( prop.getName().startsWith( prefix ) ) {
        prop.remove();
      }
    }

    for ( Map.Entry<String, String> entry : map.entrySet() ) {
      localizedStringNode.setProperty( prefix + ":" + entry.getKey(), entry.getValue() ); //$NON-NLS-1$
    }
  }

  public static String getAbsolutePath( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node ) throws RepositoryException {
    if ( node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
      return JcrStringHelper.pathDecode( session.getNodeByIdentifier( node.getProperty( pentahoJcrConstants
          .getJCR_FROZENUUID() ).getString() ).getPath() );
    }

    return JcrStringHelper.pathDecode( node.getPath() );
  }

  public static Serializable getNodeId( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node ) throws RepositoryException {
    if ( node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
      return node.getProperty( pentahoJcrConstants.getJCR_FROZENUUID() ).getString();
    }

    return node.getIdentifier();
  }

  public static String getNodeName( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node ) throws RepositoryException {
    if ( node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
      return JcrStringHelper.fileNameDecode( session.getNodeByIdentifier( node.getProperty( pentahoJcrConstants
          .getJCR_FROZENUUID() ).getString() ).getName() );
    }

    return JcrStringHelper.fileNameDecode( node.getName() );
  }

  public static String getVersionId( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node ) throws RepositoryException {
    if ( node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
      return JcrStringHelper.fileNameDecode( node.getParent().getName() );
    }
    Version version = getBaseVersion( session, node );
    return version != null ? JcrStringHelper.fileNameDecode( version.getName() ) : null;
  }

  /**
   * Getting base version of the node. In the case of getting NullPointerException from Jackrabbit Content Repository
   * (see https://issues.apache.org/jira/browse/JCR-2382), catching it, logging the error and returning null
   * 
   * @param node
   * @return version of the node or null
   * @throws UnsupportedRepositoryOperationException
   * @throws RepositoryException
   */
  private static Version getBaseVersion( final Session session, final Node node )
    throws UnsupportedRepositoryOperationException, RepositoryException {
    Version version = null;
    VersionManager versionManager = session.getWorkspace().getVersionManager();
    try {
      version = versionManager.getBaseVersion( node.getPath() );
    } catch ( NullPointerException ex ) {
      logger.warn( Messages.getInstance().getString( "JcrRepositoryFileUtils.WARN_0001_NPE_FROM_CR" ), ex );
    }
    return version;
  }

  public static Node createFolderNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable parentFolderId, final RepositoryFile folder ) throws RepositoryException {
    // Not need to check the name if we encoded it
    // checkName( folder.getName() );
    Node parentFolderNode;
    if ( parentFolderId != null ) {
      parentFolderNode = session.getNodeByIdentifier( parentFolderId.toString() );
    } else {
      parentFolderNode = session.getRootNode();
    }

    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull( parentFolderNode );

    String encodedfolderName = JcrStringHelper.fileNameEncode( folder.getName() );
    Node folderNode = parentFolderNode.addNode( encodedfolderName, pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER() );
    folderNode.setProperty( pentahoJcrConstants.getPHO_HIDDEN(), folder.isHidden() );
    folderNode.setProperty( pentahoJcrConstants.getPHO_ACLNODE(), folder.isAclNode() );
    // folderNode.setProperty(pentahoJcrConstants.getPHO_TITLE(), folder.getTitle());
    Node localeNodes = null;

    // TODO localization of files and folders must be identical
    if ( !folder.getTitle().equals( folder.getName() ) ) { // Title is different from the name
      localeNodes = folderNode.addNode( pentahoJcrConstants.getPHO_LOCALES(), pentahoJcrConstants.getPHO_NT_LOCALE() );
      Map<String, Properties> localPropertiesMap = new HashMap<String, Properties>();
      Properties titleProps = new Properties();
      titleProps.put( "file.title", folder.getTitle() );
      localPropertiesMap.put( LocalizationUtil.DEFAULT, titleProps );
      setLocalePropertiesMap( session, pentahoJcrConstants, localeNodes, localPropertiesMap );
    }

    if ( folder.isVersioned() ) {
      // folderNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_VERSIONED), true);
      folderNode.addMixin( pentahoJcrConstants.getPHO_MIX_VERSIONABLE() );
    }

    folderNode.addNode( pentahoJcrConstants.getPHO_METADATA(), JcrConstants.NT_UNSTRUCTURED );
    folderNode.addMixin( pentahoJcrConstants.getMIX_REFERENCEABLE() );
    return folderNode;
  }

  public static Node createFileNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable parentFolderId, final RepositoryFile file, final IRepositoryFileData content,
      final ITransformer<IRepositoryFileData> transformer ) throws RepositoryException {
    // Not need to check the name if we encoded it
    // checkName( file.getName() );
    String encodedFileName = JcrStringHelper.fileNameEncode( file.getName() );
    Node parentFolderNode;
    if ( parentFolderId != null ) {
      parentFolderNode = session.getNodeByIdentifier( parentFolderId.toString() );
    } else {
      parentFolderNode = session.getRootNode();
    }

    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull( parentFolderNode );

    Node fileNode = parentFolderNode.addNode( encodedFileName, pentahoJcrConstants.getPHO_NT_PENTAHOFILE() );
    fileNode.setProperty( pentahoJcrConstants.getPHO_CONTENTTYPE(), transformer.getContentType() );
    fileNode.setProperty( pentahoJcrConstants.getPHO_LASTMODIFIED(), Calendar.getInstance() );
    fileNode.setProperty( pentahoJcrConstants.getPHO_HIDDEN(), file.isHidden() );
    fileNode.setProperty( pentahoJcrConstants.getPHO_FILESIZE(), content.getDataSize() );
    fileNode.setProperty( pentahoJcrConstants.getPHO_ACLNODE(), file.isAclNode() );

    // TODO localization of files and folders must be identical
    if ( file.getLocalePropertiesMap() != null && !file.getLocalePropertiesMap().isEmpty() ) {
      Node localeNodes =
          fileNode.addNode( pentahoJcrConstants.getPHO_LOCALES(), pentahoJcrConstants.getPHO_NT_LOCALE() );
      setLocalePropertiesMap( session, pentahoJcrConstants, localeNodes, file.getLocalePropertiesMap() );
    }

    Node metaNode = fileNode.addNode( pentahoJcrConstants.getPHO_METADATA(), JcrConstants.NT_UNSTRUCTURED );
    setMetadataItemForFile( session, PentahoJcrConstants.PHO_CONTENTCREATOR, file.getCreatorId(), metaNode );
    setMetadataItemForFile( session, RepositoryFile.SCHEDULABLE_KEY, Boolean.toString( file.isSchedulable() ),
        metaNode );
    fileNode.addMixin( pentahoJcrConstants.getMIX_LOCKABLE() );
    fileNode.addMixin( pentahoJcrConstants.getMIX_REFERENCEABLE() );

    if ( file.isVersioned() ) {
      // fileNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_VERSIONED), true);
      fileNode.addMixin( pentahoJcrConstants.getPHO_MIX_VERSIONABLE() );
    }

    transformer.createContentNode( session, pentahoJcrConstants, content, fileNode );
    return fileNode;
  }

  private static void preventLostUpdate( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final RepositoryFile file ) throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier( file.getId().toString() );
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull( fileNode );
    if ( isVersioned( session, pentahoJcrConstants, fileNode ) ) {
      Assert.notNull( file.getVersionId(), "updating a versioned file requires a non-null version id" ); //$NON-NLS-1$
      Assert.state( session.getWorkspace().getVersionManager().getBaseVersion( fileNode.getPath() ).getName().equals(
          file.getVersionId().toString() ), "update to this file has occurred since its last read" ); //$NON-NLS-1$
    }
  }

  public static Node updateFileNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final RepositoryFile file, final IRepositoryFileData content,
      final ITransformer<IRepositoryFileData> transformer ) throws RepositoryException {

    Node fileNode = session.getNodeByIdentifier( file.getId().toString() );
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull( fileNode );

    preventLostUpdate( session, pentahoJcrConstants, file );

    fileNode.setProperty( pentahoJcrConstants.getPHO_CONTENTTYPE(), transformer.getContentType() );
    fileNode.setProperty( pentahoJcrConstants.getPHO_LASTMODIFIED(), Calendar.getInstance() );
    fileNode.setProperty( pentahoJcrConstants.getPHO_HIDDEN(), file.isHidden() );
    fileNode.setProperty( pentahoJcrConstants.getPHO_FILESIZE(), content.getDataSize() );
    fileNode.setProperty( pentahoJcrConstants.getPHO_ACLNODE(), file.isAclNode() );
    if ( file.getLocalePropertiesMap() != null && !file.getLocalePropertiesMap().isEmpty() ) {
      Node localePropertiesMapNode = null;
      if ( !fileNode.hasNode( pentahoJcrConstants.getPHO_LOCALES() ) ) {
        localePropertiesMapNode =
            fileNode.addNode( pentahoJcrConstants.getPHO_LOCALES(), pentahoJcrConstants.getPHO_NT_LOCALE() );
      } else {
        localePropertiesMapNode = fileNode.getNode( pentahoJcrConstants.getPHO_LOCALES() );
      }
      setLocalePropertiesMap( session, pentahoJcrConstants, localePropertiesMapNode, file.getLocalePropertiesMap() );
    }

    Node metadataNode = null;
    if ( !fileNode.hasNode( pentahoJcrConstants.getPHO_METADATA() ) ) {
      metadataNode = fileNode.addNode( pentahoJcrConstants.getPHO_METADATA(), JcrConstants.NT_UNSTRUCTURED );
    } else {
      metadataNode = fileNode.getNode( pentahoJcrConstants.getPHO_METADATA() );
    }
    if ( file.getCreatorId() != null ) {
      setMetadataItemForFile( session, PentahoJcrConstants.PHO_CONTENTCREATOR, file.getCreatorId(), metadataNode );
    }
    setMetadataItemForFile( session, RepositoryFile.SCHEDULABLE_KEY, Boolean.toString( file.isSchedulable() ),
        metadataNode );

    transformer.updateContentNode( session, pentahoJcrConstants, content, fileNode );
    return fileNode;
  }

  public static Node updateFolderNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final RepositoryFile folder ) throws RepositoryException {

    Node folderNode = session.getNodeByIdentifier( folder.getId().toString() );
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull( folderNode );

    preventLostUpdate( session, pentahoJcrConstants, folder );

    folderNode.setProperty( pentahoJcrConstants.getPHO_HIDDEN(), folder.isHidden() );
    folderNode.setProperty( pentahoJcrConstants.getPHO_ACLNODE(), folder.isAclNode() );
    if ( folder.getLocalePropertiesMap() != null && !folder.getLocalePropertiesMap().isEmpty() ) {
      Node localePropertiesMapNode = null;
      if ( !folderNode.hasNode( pentahoJcrConstants.getPHO_LOCALES() ) ) {
        localePropertiesMapNode =
            folderNode.addNode( pentahoJcrConstants.getPHO_LOCALES(), pentahoJcrConstants.getPHO_NT_LOCALE() );
      } else {
        localePropertiesMapNode = folderNode.getNode( pentahoJcrConstants.getPHO_LOCALES() );
      }
      setLocalePropertiesMap( session, pentahoJcrConstants, localePropertiesMapNode, folder.getLocalePropertiesMap() );
    }
    return folderNode;
  }

  public static IRepositoryFileData getContent( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final Serializable versionId, final ITransformer<IRepositoryFileData> transformer )
    throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    if ( isVersioned( session, pentahoJcrConstants, fileNode ) ) {
      VersionManager vMgr = session.getWorkspace().getVersionManager();
      Version version = null;
      if ( versionId != null ) {
        version = vMgr.getVersionHistory( fileNode.getPath() ).getVersion( versionId.toString() );
      } else {
        version = vMgr.getBaseVersion( fileNode.getPath() );
      }
      fileNode = getNodeAtVersion( pentahoJcrConstants, version );
    }
    Assert.isTrue( !isPentahoFolder( pentahoJcrConstants, fileNode ) );

    return transformer.fromContentNode( session, pentahoJcrConstants, fileNode );
  }

  public static List<RepositoryFile> getChildren( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper,
      final RepositoryRequest repositoryRequest ) throws RepositoryException {
    Node folderNode = session.getNodeByIdentifier( JcrStringHelper.idEncode( repositoryRequest.getPath() ) );

    Assert.isTrue( isPentahoFolder( pentahoJcrConstants, folderNode ) );

    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    // get all immediate child nodes that are of type PHO_NT_PENTAHOFOLDER or PHO_NT_PENTAHOFILE
    NodeIterator nodeIterator = null;
    if ( repositoryRequest.getChildNodeFilter() != null ) {
      nodeIterator = folderNode.getNodes( repositoryRequest.getChildNodeFilter() );
    } else {
      nodeIterator = folderNode.getNodes();
    }

    while ( nodeIterator.hasNext() ) {
      Node node = nodeIterator.nextNode();
      if ( isSupportedNodeType( pentahoJcrConstants, node ) ) {
        RepositoryFile file = nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, node );
        if ( !file.isAclNode() && ( !file.isHidden() || repositoryRequest.isShowHidden() ) ) {
          children.add( file );
        }
      }
    }

    children.removeIf( Objects::isNull );
    Collections.sort( children );
    return children;

  }

  @Deprecated
  public static List<RepositoryFile> getChildren( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Serializable folderId,
      final String filter ) throws RepositoryException {
    return getChildren( session, pentahoJcrConstants, pathConversionHelper, lockHelper, folderId, filter, null );
  }

  @Deprecated
  public static List<RepositoryFile> getChildren( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Serializable folderId,
      final String filter, Boolean showHiddenFiles ) throws RepositoryException {
    RepositoryRequest repositoryRequest = new RepositoryRequest( folderId.toString(), showHiddenFiles, 0, filter );
    return getChildren( session, pentahoJcrConstants, pathConversionHelper, lockHelper, repositoryRequest );
  }

  public static boolean isPentahoFolder( final PentahoJcrConstants pentahoJcrConstants, final Node node )
    throws RepositoryException {
    Assert.notNull( node );
    if ( node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
      String nodeTypeName = node.getProperty( pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE() ).getString();
      return pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER().equals( nodeTypeName );
    }

    return node.isNodeType( pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER() );
  }

  public static boolean isPentahoHierarchyNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node ) throws RepositoryException {
    Assert.notNull( node );
    if ( node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
      String nodeTypeName = node.getProperty( pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE() ).getString();
      // TODO mlowery add PENTAHOLINKEDFILE here when it is available
      return pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER().equals( nodeTypeName ) || pentahoJcrConstants
          .getPHO_NT_PENTAHOFILE().equals( nodeTypeName );
    }

    return node.isNodeType( pentahoJcrConstants.getPHO_NT_PENTAHOHIERARCHYNODE() );
  }

  public static boolean isLocked( final PentahoJcrConstants pentahoJcrConstants, final Node node )
    throws RepositoryException {
    Assert.notNull( node );
    if ( node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
      // frozen nodes are never locked
      return false;
    }
    boolean locked = node.isLocked();
    if ( locked ) {
      Assert.isTrue( node.isNodeType( pentahoJcrConstants.getMIX_LOCKABLE() ) );
    }
    return locked;
  }

  public static boolean isPentahoFile( final PentahoJcrConstants pentahoJcrConstants, final Node node )
    throws RepositoryException {
    Assert.notNull( node );
    if ( node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
      String primaryTypeName = node.getProperty( pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE() ).getString();
      if ( pentahoJcrConstants.getPHO_NT_PENTAHOFILE().equals( primaryTypeName ) ) {
        return true;
      }
      return false;
    }

    return node.isNodeType( pentahoJcrConstants.getPHO_NT_PENTAHOFILE() );
  }

  private static boolean isLocalizedString( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node ) throws RepositoryException {
    Assert.notNull( node );
    if ( node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
      String frozenPrimaryType = node.getProperty( pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE() ).getString();
      if ( pentahoJcrConstants.getPHO_NT_LOCALIZEDSTRING().equals( frozenPrimaryType ) ) {
        return true;
      }
      return false;
    }

    return node.isNodeType( pentahoJcrConstants.getPHO_NT_LOCALIZEDSTRING() );
  }

  public static boolean isVersioned( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node ) throws RepositoryException {
    Assert.notNull( node );
    if ( node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
      // frozen nodes represent the nodes at a particular version; so yes, they are versioned!
      return true;
    }

    return node.isNodeType( pentahoJcrConstants.getPHO_MIX_VERSIONABLE() );
  }

  public static boolean isSupportedNodeType( final PentahoJcrConstants pentahoJcrConstants, final Node node )
    throws RepositoryException {
    Assert.notNull( node );
    if ( node.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ) {
      String nodeTypeName = node.getProperty( pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE() ).getString();
      return pentahoJcrConstants.getPHO_NT_PENTAHOFILE().equals( nodeTypeName ) || pentahoJcrConstants
          .getPHO_NT_PENTAHOFOLDER().equals( nodeTypeName );
    }

    return node.isNodeType( pentahoJcrConstants.getPHO_NT_PENTAHOFILE() ) || node.isNodeType( pentahoJcrConstants
        .getPHO_NT_PENTAHOFOLDER() );
  }

  /**
   * Conditionally checks out node representing file if node is versionable.
   */
  public static void checkoutNearestVersionableFileIfNecessary( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId ) throws RepositoryException {
    // file could be null meaning the caller is using null as the parent folder; that's OK; in this case the node
    // in
    // question would be the repository root node and that is never versioned
    if ( fileId != null ) {
      Node node = session.getNodeByIdentifier( fileId.toString() );
      checkoutNearestVersionableNodeIfNecessary( session, pentahoJcrConstants, node );
    }
  }

  /**
   * Conditionally checks out node if node is versionable.
   */
  public static void checkoutNearestVersionableNodeIfNecessary( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node node ) throws RepositoryException {
    Assert.notNull( node );

    Node versionableNode = findNearestVersionableNode( session, pentahoJcrConstants, node );

    if ( versionableNode != null ) {
      session.getWorkspace().getVersionManager().checkout( versionableNode.getPath() );
    }
  }

  public static void checkinNearestVersionableFileIfNecessary( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId, final String versionMessage )
    throws RepositoryException {
    checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, fileId, versionMessage, null, false );
  }

  /**
   * Conditionally checks in node representing file if node is versionable.
   */
  public static void checkinNearestVersionableFileIfNecessary( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId, final String versionMessage,
      final Date versionDate, final boolean aclOnlyChange ) throws RepositoryException {
    // file could be null meaning the caller is using null as the parent folder; that's OK; in this case the node
    // in
    // question would be the repository root node and that is never versioned
    if ( fileId != null ) {
      Node node = session.getNodeByIdentifier( fileId.toString() );
      checkinNearestVersionableNodeIfNecessary( session, pentahoJcrConstants, node, versionMessage, versionDate,
          aclOnlyChange );
    }
  }

  public static void checkinNearestVersionableNodeIfNecessary( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node node, final String versionMessage )
    throws RepositoryException {
    checkinNearestVersionableNodeIfNecessary( session, pentahoJcrConstants, node, versionMessage, null, false );
  }

  /**
   * Conditionally checks in node if node is versionable.
   * <p/>
   * TODO mlowery move commented out version labeling to its own method
   */
  public static void checkinNearestVersionableNodeIfNecessary( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node node, final String versionMessage, Date versionDate,
      final boolean aclOnlyChange ) throws RepositoryException {
    Assert.notNull( node );
    session.save();

    /*
     * session.save must be called inside the versionable node block and outside to ensure user changes are made when a
     * file is not versioned.
     */
    Node versionableNode = findNearestVersionableNode( session, pentahoJcrConstants, node );

    if ( versionableNode != null ) {
      versionableNode.setProperty( pentahoJcrConstants.getPHO_VERSIONAUTHOR(), getUsername() );
      if ( StringUtils.hasText( versionMessage ) ) {
        versionableNode.setProperty( pentahoJcrConstants.getPHO_VERSIONMESSAGE(), versionMessage );
      } else {
        // TODO mlowery why do I need to check for hasProperty here? in JR 1.6, I didn't need to
        if ( versionableNode.hasProperty( pentahoJcrConstants.getPHO_VERSIONMESSAGE() ) ) {
          versionableNode.setProperty( pentahoJcrConstants.getPHO_VERSIONMESSAGE(), (String) null );
        }
      }
      if ( aclOnlyChange ) {
        versionableNode.setProperty( pentahoJcrConstants.getPHO_ACLONLYCHANGE(), true );
      } else {
        // TODO mlowery why do I need to check for hasProperty here? in JR 1.6, I didn't need to
        if ( versionableNode.hasProperty( pentahoJcrConstants.getPHO_ACLONLYCHANGE() ) ) {
          versionableNode.getProperty( pentahoJcrConstants.getPHO_ACLONLYCHANGE() ).remove();
        }
      }
      session.save(); // required before checkin since we set some properties above

      Calendar cal = Calendar.getInstance();
      if ( versionDate != null ) {
        cal.setTime( versionDate );
      } else {
        cal.setTime( new Date() );
      }
      ( (VersionManagerImpl) session.getWorkspace().getVersionManager() ).checkin( versionableNode.getPath(), cal );

      // if we're not versioning, delete only the previous version to
      // prevent the number of versions from increasing. We still need a versioned node
      if ( !getRepositoryVersionManager().isVersioningEnabled( versionableNode.getPath() ) ) {

        List<VersionSummary> versionSummaries =
            (List<VersionSummary>) getVersionSummaries( session, pentahoJcrConstants, versionableNode.getIdentifier(),
                Boolean.TRUE );

        if ( ( versionSummaries != null ) && ( versionSummaries.size() > 1 ) ) {
          VersionSummary versionSummary = (VersionSummary) versionSummaries.toArray()[versionSummaries.size() - 2];

          if ( versionSummary != null ) {
            String versionId = (String) versionSummary.getId();
            session.getWorkspace().getVersionManager().getVersionHistory( versionableNode.getPath() ).removeVersion(
                versionId );
            session.save();
          }
        }
      }
    }
  }

  private static String getUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state( pentahoSession != null );
    return pentahoSession.getName();
  }

  /**
   * Returns the nearest versionable node (possibly the node itself) or {@code null} if the root is reached.
   */
  private static Node findNearestVersionableNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node ) throws RepositoryException {
    Node currentNode = node;
    while ( !currentNode.isNodeType( pentahoJcrConstants.getPHO_MIX_VERSIONABLE() ) ) {
      try {
        currentNode = currentNode.getParent();
      } catch ( ItemNotFoundException e ) {
        // at the root
        return null;
      }
    }
    return currentNode;
  }

  public static void deleteFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final ILockHelper lockTokenHelper ) throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull( fileNode );
    // technically, the node can be locked when it is deleted; however, we want to avoid an orphaned lock token;
    // delete
    // it first
    if ( fileNode.isLocked() ) {
      Lock lock = session.getWorkspace().getLockManager().getLock( fileNode.getPath() );
      // don't need lock token anymore
      lockTokenHelper.removeLockToken( session, pentahoJcrConstants, lock );
    }
    fileNode.remove();
  }

  public static RepositoryFile nodeIdToFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Serializable fileId )
    throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    return nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, fileNode );
  }

  public static Object getVersionSummaries( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final boolean includeAclOnlyChanges ) throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    VersionHistory versionHistory = session.getWorkspace().getVersionManager().getVersionHistory( fileNode.getPath() );
    // get root version but don't include it in version summaries; from JSR-170 specification section 8.2.5:
    // [root version] is a dummy version that serves as the starting point of the version graph. Like all version
    // nodes,
    // it has a subnode called jcr:frozenNode. But, in this case that frozen node does not contain any state
    // information
    // about N
    Version version = versionHistory.getRootVersion();
    Version[] successors = version.getSuccessors();
    List<VersionSummary> versionSummaries = new ArrayList<VersionSummary>();
    while ( successors != null && successors.length > 0 ) {
      version = successors[0]; // branching not supported
      VersionSummary sum = toVersionSummary( pentahoJcrConstants, versionHistory, version );
      if ( !sum.isAclOnlyChange() || ( includeAclOnlyChanges && sum.isAclOnlyChange() ) ) {
        versionSummaries.add( sum );
      }
      successors = version.getSuccessors();
    }
    return versionSummaries;
  }

  private static VersionSummary toVersionSummary( final PentahoJcrConstants pentahoJcrConstants,
      final VersionHistory versionHistory, final Version version ) throws RepositoryException {
    List<String> labels = Arrays.asList( versionHistory.getVersionLabels( version ) );
    // get custom Hitachi Vantara properties (i.e. author and message)
    Node nodeAtVersion = getNodeAtVersion( pentahoJcrConstants, version );
    String author = "BASE_VERSION";
    if ( nodeAtVersion.hasProperty( pentahoJcrConstants.getPHO_VERSIONAUTHOR() ) ) {
      author = nodeAtVersion.getProperty( pentahoJcrConstants.getPHO_VERSIONAUTHOR() ).getString();
    }
    String message = null;
    if ( nodeAtVersion.hasProperty( pentahoJcrConstants.getPHO_VERSIONMESSAGE() ) ) {
      message = nodeAtVersion.getProperty( pentahoJcrConstants.getPHO_VERSIONMESSAGE() ).getString();
    }
    boolean aclOnlyChange = false;
    if ( nodeAtVersion.hasProperty( pentahoJcrConstants.getPHO_ACLONLYCHANGE() ) && nodeAtVersion.getProperty(
        pentahoJcrConstants.getPHO_ACLONLYCHANGE() ).getBoolean() ) {
      aclOnlyChange = true;
    }
    return new VersionSummary( version.getName(), versionHistory.getVersionableIdentifier(), aclOnlyChange, version
        .getCreated().getTime(), author, message, labels );
  }

  /**
   * Returns the node as it was at the given version.
   * 
   * @param version
   *          version to get
   * @return node at version
   */
  private static Node getNodeAtVersion( final PentahoJcrConstants pentahoJcrConstants, final Version version )
    throws RepositoryException {
    return version.getNode( pentahoJcrConstants.getJCR_FROZENNODE() );
  }

  public static RepositoryFile getFileAtVersion( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Serializable fileId,
      final Serializable versionId ) throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    Version version =
        session.getWorkspace().getVersionManager().getVersionHistory( fileNode.getPath() ).getVersion( versionId
            .toString() );
    return nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, getNodeAtVersion(
        pentahoJcrConstants, version ) );
  }

  /**
   * Returns the metadata regarding that identifies what transformer wrote this file's data.
   */
  public static String getFileContentType( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final Serializable versionId ) throws RepositoryException {
    Assert.notNull( fileId );
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    if ( versionId != null ) {
      Version version =
          session.getWorkspace().getVersionManager().getVersionHistory( fileNode.getPath() ).getVersion( versionId
              .toString() );
      Node nodeAtVersion = getNodeAtVersion( pentahoJcrConstants, version );
      return nodeAtVersion.getProperty( pentahoJcrConstants.getPHO_CONTENTTYPE() ).getString();
    }

    return fileNode.getProperty( pentahoJcrConstants.getPHO_CONTENTTYPE() ).getString();
  }

  public static Serializable getParentId( final Session session, final Serializable fileId )
    throws RepositoryException {
    Node node = session.getNodeByIdentifier( fileId.toString() );
    return node.getParent().getIdentifier();
  }

  public static Serializable getBaseVersionId( final Session session, final Serializable fileId )
    throws RepositoryException {
    Node node = session.getNodeByIdentifier( fileId.toString() );
    return session.getWorkspace().getVersionManager().getBaseVersion( node.getPath() ).getName();
  }

  public static Object getVersionSummary( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final Serializable versionId ) throws RepositoryException {
    VersionManager vMgr = session.getWorkspace().getVersionManager();
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    VersionHistory versionHistory = vMgr.getVersionHistory( fileNode.getPath() );
    Version version = null;
    if ( versionId != null ) {
      version = versionHistory.getVersion( versionId.toString() );
    } else {
      version = vMgr.getBaseVersion( fileNode.getPath() );
    }
    return toVersionSummary( pentahoJcrConstants, versionHistory, version );
  }

  public static RepositoryFileTree getTree( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final String absPath,
      final RepositoryRequest repositoryRequest, IRepositoryAccessVoterManager accessVoterManager )
    throws RepositoryException {

    Item fileItem = session.getItem( JcrStringHelper.pathEncode( absPath ) );
    // items are nodes or properties; this must be a node
    Assert.isTrue( fileItem.isNode() );
    Node fileNode = (Node) fileItem;

    return getTreeByNode( session, pentahoJcrConstants, pathConversionHelper, lockHelper, fileNode, repositoryRequest
        .getDepth(), repositoryRequest.getChildNodeFilter(), repositoryRequest.isShowHidden(), accessVoterManager,
        repositoryRequest.getTypes(), new MutableBoolean( false ), repositoryRequest.isIncludeSystemFolders(),
        absPath );

  }

  /**
   * Returns a RepositoryFileTree for a given node. This method will be called recursively for each folder it processes.
   * The childNodeFilter is a filter used directly by the JCR jar to filter node names. Since JCR does not know a folder
   * from a file (that is our construct), it is not capable of filtering out filenames but not folder names. Therefore,
   * this logic will create two sets of children nodes. The <code>filteredChildrenSet</code> keeps the child nodes that
   * satisfied the childNodeFilter. The <code> childrenFolderSet</code> keeps a set of all child folder nodes regardless
   * of the value of the filter. We use the <code>childrenFolderSet</code> to know what folders to traverse, but we use
   * the <code>filteredChildrenSet </code> to determine what actual files to include in the returned tree.
   * <p>
   * Just because we process a folder node does not necessarily mean the folder will be reported in the tree. It must
   * first find a file that satisfies the criteria of the <code>childNodeFilter</code> mask. A file meeting the criteria
   * may be any number of folders down the repository structure, so the <code>foundFiltered</code> MutableBoolean tells
   * the caller if a file was found, at any level, meeting that criteria.
   * 
   * @param session
   *          The current session in progress
   * @param pentahoJcrConstants
   * @param pathConversionHelper
   * @param lockHelper
   * @param fileNode
   *          The node which will serve as the root of the tree
   * @param depth
   *          how many levels do we go down.
   * @param childNodeFilter
   *          The filter sent to JCR to retrieve defining which files are in scope
   * @param showHidden
   *          Whether to return hidden files
   * @param accessVoterManager
   *          See IRepositoryAccessVoterManager
   * @param types
   *          <code>FILE_TYPE_FILTERS</code> Types of files to return including FILES, FOLDERS, FILES_FOLDERS
   * @param foundFiltered
   *          This <code>MutableBoolean</code> will tell the caller if there was a file encountered, (at any level up to
   *          the depth), that was compliant with the childNodeFilter. This will determine if this node, (and its
   *          children), should be discarded because there are no relevant files.
   * @return A RepositoryFileTree representing the entire tree at and below the given node that complies with file
   *         filtering and other parameters of the tree request.
   * @throws RepositoryException
   */
  private static RepositoryFileTree getTreeByNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Node fileNode,
      final int depth, final String childNodeFilter, final boolean showHidden,
      IRepositoryAccessVoterManager accessVoterManager, RepositoryRequest.FILES_TYPE_FILTER types,
      MutableBoolean foundFiltered, final boolean includeSystemFolders, final String rootPath )
      throws RepositoryException {

    RepositoryFile rootFile =
        nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, fileNode, false, null );
    if ( ( !showHidden && rootFile.isHidden() ) || rootFile.isAclNode() || ( !accessVoterManager.hasAccess( rootFile,
        RepositoryFilePermission.READ, JcrRepositoryFileAclUtils.getAcl( session, pentahoJcrConstants, rootFile
            .getId() ), PentahoSessionHolder.getSession() ) ) ) {
      return null;
    }
    List<RepositoryFileTree> children;
    HashSet<Node> childrenFolderSet;
    // if depth is neither negative (indicating unlimited depth) nor positive (indicating at least one more level
    // to go)
    if ( depth != 0 ) {
      children = new ArrayList<RepositoryFileTree>();
      int numberOfPasses = childNodeFilter != null && !childNodeFilter.equals( "*" ) ? 2 : 1;

      // get Filtered Children set
      HashSet<Node> filteredChildrenSet;
      filteredChildrenSet = new HashSet<Node>();
      NodeIterator childNodes = fileNode.getNodes( childNodeFilter );
      while ( childNodes.hasNext() ) {
        Node childNode = childNodes.nextNode();

        boolean pentahoFolder = isPentahoFolder( pentahoJcrConstants, childNode );
        if ( !( !pentahoFolder && types == RepositoryRequest.FILES_TYPE_FILTER.FOLDERS || pentahoFolder
            && types == RepositoryRequest.FILES_TYPE_FILTER.FILES ) ) {
          // do not to include (skip) system_folder children that are at root level if includeSystemFolders is false
          if ( !( !includeSystemFolders && ( rootPath.equals( childNode.getParent().getPath() ) && isSystemFolder(
            session, childNode ) ) ) ) {
            filteredChildrenSet.add( childNode );
          }
        }
      }

      // Now get the unfiltered folder set not already in Filtered Set
      childrenFolderSet = new HashSet<Node>();
      if ( numberOfPasses == 2 ) {
        if ( isPentahoFolder( pentahoJcrConstants, fileNode ) ) {
          childNodes = fileNode.getNodes();
          while ( childNodes.hasNext() ) {
            Node childNode = childNodes.nextNode();
            boolean pentahoFolder = isPentahoFolder( pentahoJcrConstants, childNode );
            if ( pentahoFolder ) {
              childrenFolderSet.add( childNode );
            }
          }
        }
      }

      // Now work on the unfiltered set of folders, if any, add them only if file have been found somewhere down the
      // tree
      for ( Node childNode : childrenFolderSet ) {
        checkNodeForTree( childNode, children, session, pentahoJcrConstants, pathConversionHelper, childNodeFilter,
            lockHelper, depth, showHidden, accessVoterManager, types, foundFiltered, false, includeSystemFolders, rootPath );
      }

      // And finally, add Children in filtered
      for ( Node childNode : filteredChildrenSet ) {
        foundFiltered.setValue( true );
        checkNodeForTree( childNode, children, session, pentahoJcrConstants, pathConversionHelper, childNodeFilter,
            lockHelper, depth, showHidden, accessVoterManager, types, foundFiltered, true, includeSystemFolders, rootPath );
      }

      children.removeIf( Objects::isNull );
      Collections.sort( children );
    } else {
      children = null;
    }
    return new RepositoryFileTree( rootFile, children );
  }

  private static boolean isSystemFolder( Session session, Node childNode ) throws RepositoryException {
    Map<String, Serializable> fileMeta = getFileMetadata( session, ( (NodeImpl) childNode ).getNodeId() );
    boolean isSystemFolder = fileMeta.containsKey( IUnifiedRepository.SYSTEM_FOLDER ) ? (Boolean) fileMeta
        .get( IUnifiedRepository.SYSTEM_FOLDER ) : false;
    return isSystemFolder;
  }

  /**
   * This method is called twice by <code>getTreeNode</code>. It's job is to determine whether the current child node
   * should be added to the list of children for the node being processed. It is a separate method simply because it is
   * too much code to appear twice in the above <code>getTreeNode</code> method. It also makes the recursive call back
   * to getTreeByNode to process the next lower level of folder node (it must process the lower levels to know if the
   * folder should be added). Finally, it returns the foundFiltered boolean to let the caller know if a file was found
   * that satisfied the childNodeFilter.
   */
  private static void checkNodeForTree( final Node childNode, List<RepositoryFileTree> children, final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final IPathConversionHelper pathConversionHelper,
      final String childNodeFilter, final ILockHelper lockHelper, final int depth, final boolean showHidden,
      final IRepositoryAccessVoterManager accessVoterManager, RepositoryRequest.FILES_TYPE_FILTER types,
      MutableBoolean foundFiltered, boolean isRootFiltered, final boolean includeSystemFolders,
      final String rootPath ) throws RepositoryException {

    RepositoryFile file = nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, childNode );
    if ( isSupportedNodeType( pentahoJcrConstants, childNode ) && ( accessVoterManager.hasAccess( file,
        RepositoryFilePermission.READ, JcrRepositoryFileAclUtils.getAcl( session, pentahoJcrConstants, file.getId() ),
        PentahoSessionHolder.getSession() ) ) ) {
      MutableBoolean foundFilteredAtomic = new MutableBoolean( !isPentahoFolder( pentahoJcrConstants, childNode ) );
      RepositoryFileTree repositoryFileTree =
          getTreeByNode( session, pentahoJcrConstants, pathConversionHelper, lockHelper, childNode, depth - 1,
              childNodeFilter, showHidden, accessVoterManager, types, foundFilteredAtomic, includeSystemFolders, rootPath );
      if ( repositoryFileTree != null && ( foundFilteredAtomic.booleanValue() || isRootFiltered ) ) {
        foundFiltered.setValue( true );
        children.add( repositoryFileTree );
      }
    }
  }

  public static Node updateFileLocaleProperties( final Session session, final Serializable fileId, String locale,
      Properties properties ) throws RepositoryException {

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS );
    Assert.hasText( prefix );

    Node localesNode = null;
    if ( !fileNode.hasNode( pentahoJcrConstants.getPHO_LOCALES() ) ) {
      // Auto-create pho:locales node if doesn't exist
      localesNode = fileNode.addNode( pentahoJcrConstants.getPHO_LOCALES(), pentahoJcrConstants.getPHO_NT_LOCALE() );
    } else {
      localesNode = fileNode.getNode( pentahoJcrConstants.getPHO_LOCALES() );
    }

    try {
      Node localeNode = NodeHelper.checkGetNode( localesNode, locale );
      for ( String propertyName : properties.stringPropertyNames() ) {
        localeNode.setProperty( propertyName, properties.getProperty( propertyName ) );
      }
    } catch ( PathNotFoundException pnfe ) {
      // locale doesn't exist, create a new locale node
      Map<String, Properties> propertiesMap = new HashMap<String, Properties>();
      propertiesMap.put( locale, properties );
      setLocalePropertiesMap( session, pentahoJcrConstants, localesNode, propertiesMap );
    }

    return fileNode;
  }

  public static Node deleteFileLocaleProperties( final Session session, final Serializable fileId, String locale )
    throws RepositoryException {

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS );
    Assert.hasText( prefix );

    Node localesNode = fileNode.getNode( pentahoJcrConstants.getPHO_LOCALES() );
    Assert.notNull( localesNode );

    try {
      // remove locale node
      Node localeNode = NodeHelper.checkGetNode( localesNode, locale );
      localeNode.remove();
    } catch ( PathNotFoundException pnfe ) {
      // nothing to delete
    }

    return fileNode;
  }

  public static void setFileMetadata( final Session session, final Serializable fileId,
      Map<String, Serializable> metadataMap ) throws ItemNotFoundException, RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );

    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS );
    Assert.hasText( prefix );
    Node metadataNode = fileNode.getNode( pentahoJcrConstants.getPHO_METADATA() );
    checkoutNearestVersionableNodeIfNecessary( session, pentahoJcrConstants, metadataNode );

    PropertyIterator propertyIter = metadataNode.getProperties( prefix + ":*" ); //$NON-NLS-1$
    while ( propertyIter.hasNext() ) {
      propertyIter.nextProperty().remove();
    }

    for ( String key : metadataMap.keySet() ) {
      setMetadataItemForFile( session, key, metadataMap.get( key ), metadataNode );
    }

    checkinNearestVersionableNodeIfNecessary( session, pentahoJcrConstants, metadataNode, null );
  }

  private static void setMetadataItemForFile( final Session session, final String metadataKey,
      final Serializable metadataObj, final Node metadataNode ) throws ItemNotFoundException, RepositoryException {
    checkName( metadataKey );
    Assert.notNull( metadataNode );
    String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS );
    Assert.hasText( prefix );
    if ( metadataObj instanceof String ) {
      metadataNode.setProperty( prefix + ":" + metadataKey, (String) metadataObj ); //$NON-NLS-1$
    } else if ( metadataObj instanceof Calendar ) {
      metadataNode.setProperty( prefix + ":" + metadataKey, (Calendar) metadataObj ); //$NON-NLS-1$
    } else if ( metadataObj instanceof Double ) {
      metadataNode.setProperty( prefix + ":" + metadataKey, (Double) metadataObj ); //$NON-NLS-1$
    } else if ( metadataObj instanceof Long ) {
      metadataNode.setProperty( prefix + ":" + metadataKey, (Long) metadataObj ); //$NON-NLS-1$
    } else if ( metadataObj instanceof Boolean ) {
      metadataNode.setProperty( prefix + ":" + metadataKey, (Boolean) metadataObj ); //$NON-NLS-1$
    }
  }

  public static Map<String, Serializable> getFileMetadata( final Session session, final Serializable fileId )
    throws ItemNotFoundException, RepositoryException {
    Map<String, Serializable> values = new HashMap<String, Serializable>();
    String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS );
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    String metadataNodeName = pentahoJcrConstants.getPHO_METADATA();
    Node metadataNode = null;
    try {
      metadataNode = NodeHelper.checkGetNode( fileNode, metadataNodeName );
    } catch ( PathNotFoundException pathNotFound ) { // No meta on this return an empty Map
      return values;
    }
    PropertyIterator iter = metadataNode.getProperties( prefix + ":*" ); //$NON-NLS-1$
    while ( iter.hasNext() ) {
      Property property = iter.nextProperty();
      String key = property.getName().substring( property.getName().indexOf( ':' ) + 1 );
      Serializable value = null;
      switch ( property.getType() ) {
        case PropertyType.STRING:
          value = property.getString();
          break;
        case PropertyType.DATE:
          value = property.getDate();
          break;
        case PropertyType.DOUBLE:
          value = property.getDouble();
          break;
        case PropertyType.LONG:
          value = property.getLong();
          break;
        case PropertyType.BOOLEAN:
          value = property.getBoolean();
          break;
      }
      if ( value != null ) {
        values.put( key, value );
      }
    }

    return values;
  }

  /**
   * Use override list from PentahoSystem if it exists
   * 
   * @return
   */
  public static List<Character> getReservedChars() {
    return reservedChars;
  }

  /**
   * Checks for presence of black listed chars as well as illegal permutations of legal chars.
   */
  public static void checkName( final String name ) {
    Pattern containsReservedCharsPattern = makePattern( getReservedChars() );
    if ( !StringUtils.hasLength( name ) || // not null, not empty, and not all whitespace
        !name.trim().equals( name ) || // no leading or trailing whitespace
        containsReservedCharsPattern.matcher( name ).matches() || // no reserved characters
        ".".equals( name ) || // no . //$NON-NLS-1$
        "..".equals( name ) ) { // no .. //$NON-NLS-1$
      throw new RepositoryFileDaoMalformedNameException( name );
    }
  }

  public static RepositoryFile createFolder( final Session session,
      final CredentialsStrategySessionFactory sessionFactory, final RepositoryFile parentFolder,
      final RepositoryFile folder, final boolean inheritAces, final RepositoryFileSid ownerSid,
      final IPathConversionHelper pathConversionHelper, final String versionMessage ) throws RepositoryException {
    Serializable parentFolderId = parentFolder == null ? null : parentFolder.getId();
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, parentFolderId );
    Node folderNode = createFolderNode( session, pentahoJcrConstants, parentFolderId, folder );
    session.save();
    JcrRepositoryFileAclUtils.createAcl( session, pentahoJcrConstants, folderNode.getIdentifier(),
        new RepositoryFileAcl.Builder( ownerSid ).entriesInheriting( inheritAces ).build() );
    session.save();
    if ( folder.isVersioned() ) {
      JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary( session, pentahoJcrConstants, folderNode,
          versionMessage );
    }
    JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, parentFolderId,
        Messages.getInstance().getString( "JcrRepositoryFileDao.USER_0001_VER_COMMENT_ADD_FOLDER", folder.getName(),
            ( parentFolderId == null ? "root" : parentFolderId.toString() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    return JcrRepositoryFileUtils.getFileById( session, pentahoJcrConstants, pathConversionHelper, null, folderNode
        .getIdentifier() );
  }

  public static RepositoryFile getFileByAbsolutePath( final Session session, final String absPath,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final boolean loadMaps,
      final IPentahoLocale locale ) throws RepositoryException {

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    Item fileNode;
    try {
      fileNode = session.getItem( JcrStringHelper.pathEncode( absPath ) );
      // items are nodes or properties; this must be a node
      Assert.isTrue( fileNode.isNode() );
    } catch ( PathNotFoundException e ) {
      fileNode = null;
    }
    return fileNode != null ? nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
        (Node) fileNode, loadMaps, locale ) : null;
  }

  public static IRepositoryVersionManager getRepositoryVersionManager() {
    if ( repositoryVersionManager == null ) {
      repositoryVersionManager = PentahoSystem.get( IRepositoryVersionManager.class );
    }
    return repositoryVersionManager;
  }

  // User for unit tests
  public static void setRepositoryVersionManager( IRepositoryVersionManager repositoryVersionManager ) {
    JcrRepositoryFileUtils.repositoryVersionManager = repositoryVersionManager;
  }
}
