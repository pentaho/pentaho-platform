/*
 * ! ******************************************************************************
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


package org.pentaho.platform.plugin.services.pluginmgr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.LoggerFactory;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginBeanDefinition;
import org.pentaho.platform.api.engine.PluginServiceDefinition;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.solution.ContentGeneratorInfo;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.core.solution.PluginOperation;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;

import com.cronutils.utils.VisibleForTesting;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of {@link IPluginProvider} that searches for plugin.xml files in the Pentaho system path and
 * instantiates {@link IPlatformPlugin}s from the information in those files.
 *
 * @author aphillips
 */
public class SystemPathXmlPluginProvider implements IPluginProvider {

  public static final String CLASS_PROPERRTY = "class";
  private static final Pattern PLUGIN_DATE_STAMP_REGEX = Pattern.compile( "([\\w\\-]+)-2\\d{3}-[\\d\\-]+" );
  private static final org.slf4j.Logger log = LoggerFactory.getLogger( SystemPathXmlPluginProvider.class );

  /**
   * Gets the list of plugins that this provider class has discovered.
   *
   * @return an read-only list of plugins
   * @throws PlatformPluginRegistrationException if there is a problem preventing the impl from looking for plugins
   * @see IPluginProvider#getPlugins(IPentahoSession)
   */
  public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
    List<IPlatformPlugin> plugins = new ArrayList<>();

    // look in each of the system setting folders looking for plugin.xml files
    String systemPath = PentahoSystem.getApplicationContext().getSolutionPath( "system" ); //$NON-NLS-1$
    File systemDir = new File( systemPath );
    if ( !systemDir.exists() || !systemDir.isDirectory() ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0004_CANNOT_FIND_SYSTEM_FOLDER" ) ); //$NON-NLS-1$
    }
    File[] kids = systemDir.listFiles();
    Arrays.sort( kids );  // default sort is by name, which is what we want
    // look at each child to see if it is a folder
    for ( File kid : kids ) {
      if ( kid.isDirectory() ) {
        try {
          processDirectory( plugins, kid, session );
        } catch ( Throwable t ) {
          // don't throw an exception. we need to continue to process any remaining good plugins
          String msg =
            Messages.getInstance().getErrorString(
              "SystemPathXmlPluginProvider.ERROR_0001_FAILED_TO_PROCESS_PLUGIN", kid.getAbsolutePath() ); //$NON-NLS-1$
          Logger.error( getClass().toString(), msg, t );
          PluginMessageLogger.add( msg );
        }
      }
    }

    return Collections.unmodifiableList( plugins );
  }

  @SuppressWarnings( "squid:S3776" ) // cannot break down into smaller methods without reducing readability
  protected void processDirectory( List<IPlatformPlugin> plugins, File folder, IPentahoSession session )
    throws PlatformPluginRegistrationException {

    log.debug( "Processing plugin directory: {}", folder.getAbsolutePath() );
    // see if there is a plugin.xml file
    FilenameFilter filter = new NameFileFilter( "plugin.xml", IOCase.SENSITIVE ); //$NON-NLS-1$
    File[] kids = folder.listFiles( filter );
    if ( kids == null || kids.length == 0 ) {
      return;
    }
    // if the folder is marked for deletion, then delete it 
    FilenameFilter deleteFilter = new NameFileFilter( ".plugin-manager-delete", IOCase.SENSITIVE ); //$NON-NLS-1$
    kids = folder.listFiles( deleteFilter );
    if ( kids != null && kids.length > 0 ) {
      log.debug( "Deleting plugin directory marked for deletion: {}", folder.getAbsolutePath() );
      deleteFolder( folder );
      return;
    }
    // see if we should ignore this plugin because it is marked to be ignored
    FilenameFilter ignoreFilter = new NameFileFilter( ".kettle-ignore", IOCase.SENSITIVE ); //$NON-NLS-1$
    kids = folder.listFiles( ignoreFilter );
    if ( kids != null && kids.length > 0 ) {
      log.debug( "Ignoring plugin directory marked to be ignored: {}", folder.getAbsolutePath() );
      return;
    }

    folder = cleanUpUninstalledPlugins( folder, deleteFilter, ignoreFilter );

    boolean hasLib = false;
    filter = new NameFileFilter( "lib", IOCase.SENSITIVE ); //$NON-NLS-1$
    kids = folder.listFiles( filter );
    if ( kids != null && kids.length > 0 ) {
      hasLib = kids[ 0 ].exists() && kids[ 0 ].isDirectory();
    }
    // we have found a plugin.xml file
    // get the file from the repository
    String path = "system" + RepositoryFile.SEPARATOR + folder.getName() + RepositoryFile.SEPARATOR + "plugin.xml"; //$NON-NLS-1$ //$NON-NLS-2$
    Document doc = null;
    try {
      try {
        org.dom4j.io.SAXReader reader = XMLParserFactoryProducer.getSAXReader( new SolutionURIResolver() );
        doc = reader.read( ActionSequenceResource.getInputStream( path, LocaleHelper.getLocale() ) );
      } catch ( Throwable t ) {
        // XML document can't be read. We'll just return a null document.
      }
      if ( doc != null ) {
        PlatformPlugin newPlugin = createPlugin( doc, session, folder.getName(), hasLib );
        // make sure we don't already have a plugin with this id
        if ( plugins.stream().anyMatch( p -> p.getId().equals( newPlugin.getId() ) ) ) {
          String msg =
            Messages.getInstance().getErrorString(
              "PluginManager.ERROR_0024_PLUGIN_ALREADY_LOADED_BY_SAME_NAME", newPlugin.getId(), folder //$NON-NLS-1$
                .getAbsolutePath() );
          Logger.error( getClass().toString(), msg );
          PluginMessageLogger.add( msg );
          log.warn( "Plugin with id {} already loaded, skipping plugin in folder {}", newPlugin.getId(), path );
        } else {
          plugins.add( newPlugin );
        }
      }
    } catch ( Exception e ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0005_CANNOT_PROCESS_PLUGIN_XML", path ), e ); //$NON-NLS-1$
    }
    if ( doc == null ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0005_CANNOT_PROCESS_PLUGIN_XML", path ) ); //$NON-NLS-1$
    }
  }

  /*
   * This method ensures that if a plugin folder was uninstalled by the plugin manager, we delete it.
   * If a plugin was newly installed by the plugin manager (still has a date stamp on the end of the name),
   * we rename it to the base folder name without the date stamp.
   */
  private File cleanUpUninstalledPlugins( File folder, FilenameFilter deleteFilter, FilenameFilter ignoreFilter ) {
    // strip off any datestamp left from the plugin manager to leave only the base folder name
    log.debug( "Checking for datestamp on plugin folder: {}", folder.getAbsolutePath() );
    String newFolderName = stripDateStampFromFolderName( folder.getName() );
    if ( !folder.getName().equals( newFolderName ) ) {
      // The plugin folder had a date stamp on the end of the name which was removed.
      // Check if a folder with the new name already exists
      File parent = folder.getParentFile();
      FilenameFilter newFolderFilter = new NameFileFilter( newFolderName, IOCase.SENSITIVE );
      File[] matchingFolders = parent.listFiles( newFolderFilter );
      boolean canRenameFolder = false;
      if ( matchingFolders != null && matchingFolders.length == 1 ) { //it either matches or it doesn't
        // folder already exists; check whether it is marked to be ignored and deleted by the plugin manager
        File oldPluginFolder = matchingFolders[ 0 ];
        File[] ignoreFiles = oldPluginFolder.listFiles( ignoreFilter );
        File[] deleteFiles = oldPluginFolder.listFiles( deleteFilter );
        if ( null != ignoreFiles && ignoreFiles.length == 1 && null != deleteFiles && deleteFiles.length == 1 ) {
          // we can and should delete this folder before we rename the other one
          log.debug( "Deleting old plugin folder marked for deletion: {}", oldPluginFolder.getAbsolutePath() );
          deleteFolder( oldPluginFolder );
          canRenameFolder = true;
        } else {
          // log an error since we can't get rid of the old plugin to allow the new one to load correctly
          String msg = Messages.getInstance().getErrorString(
            "PluginManager.ERROR_0030_CANNOT_DELETE_CONFLICTING_PLUGIN_FOLDER", oldPluginFolder.getName(), folder
              .getName() );
          Logger.error( getClass().toString(), msg );
          canRenameFolder = false;
        }
      } else {
        canRenameFolder = true;
      }
      if ( !canRenameFolder || !folder.renameTo( new File( parent, newFolderName ) ) ) {
        String msg = Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0028_CANNOT_RENAME_PLUGIN_FOLDER", folder.getName(), newFolderName );
        Logger.error( getClass().toString(), msg );
        PluginMessageLogger.add( msg );
      } else {
        // rename was successful, so we can continue processing the plugin
        folder = new File( parent, newFolderName );
      }
    }
    return folder;
  }

  private void deleteFolder( File folder ) {
    // delete the folder and its contents
    try {
      FileUtils.deleteDirectory( folder );
      log.debug( "Deleted plugin directory: {}", folder.getAbsolutePath() );
      String msg = Messages.getInstance().getString(
        "PluginManager.PLUGIN_FOLDER_DELETED", folder.getAbsolutePath() );
      Logger.info( getClass().toString(), msg );
      PluginMessageLogger.add( msg );
    } catch ( Exception e ) {
      String msg = Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0029_CANNOT_DELETE_PLUGIN_FOLDER", folder.getAbsolutePath() );
      Logger.error( getClass().toString(), msg, e );
      PluginMessageLogger.add( msg );
    }
  }

  @VisibleForTesting
  protected String stripDateStampFromFolderName( String folderName ) {
    if ( null == folderName ) {
      return null;
    }
    Matcher matcher = PLUGIN_DATE_STAMP_REGEX.matcher( folderName );

    return matcher.matches() ? matcher.group( 1 ) : folderName;
  }

  protected PlatformPlugin createPlugin( Document doc, IPentahoSession session, String folder, boolean hasLib ) {
    PlatformPlugin plugin = new PlatformPlugin();

    processStaticResourcePaths( plugin, doc, session );
    processPluginInfo( plugin, doc, folder, session );
    processContentTypes( plugin, doc, session );
    processContentGenerators( plugin, doc, session, folder, hasLib );
    processOverlays( plugin, doc, session );
    processLifecycleListeners( plugin, doc );
    processBeans( plugin, doc );
    processWebservices( plugin, doc );
    processExternalResources( plugin, doc );
    processPerspectives( plugin, doc );

    int listenerCount = plugin.getLifecycleListenerClassnames() != null ? plugin.getLifecycleListenerClassnames()
      .size() : 0;
    String msg =
      Messages.getInstance().getString(
        "SystemPathXmlPluginProvider.PLUGIN_PROVIDES", //$NON-NLS-1$
        Integer.toString( plugin.getContentInfos().size() ),
        Integer.toString( plugin.getContentGenerators().size() ),
        Integer.toString( plugin.getOverlays().size() ),
        Integer.toString( listenerCount ) );
    PluginMessageLogger.add( msg );

    plugin.setSourceDescription( folder );

    return plugin;
  }

  /**
   * @param plugin
   * @param doc
   */
  protected void processPerspectives( PlatformPlugin plugin, Document doc ) {
    // TODO Auto-generated method stub
    List<?> nodes = doc.selectNodes( "/*/perspective" ); //$NON-NLS-1$
    for ( Object obj : nodes ) {
      Element node = (Element) obj;
      if ( node != null ) {
        IPluginPerspective perspective = PerspectiveUtil.createPerspective( node );
        plugin.addPluginPerspective( perspective );
      }
    }
  }

  protected void processStaticResourcePaths( PlatformPlugin plugin, Document doc, IPentahoSession session ) {
    List<?> nodes = doc.selectNodes( "//static-path" ); //$NON-NLS-1$
    for ( Object obj : nodes ) {
      Element node = (Element) obj;
      if ( node != null ) {
        String url = node.attributeValue( "url" ); //$NON-NLS-1$
        String localFolder = node.attributeValue( "localFolder" ); //$NON-NLS-1$
        plugin.addStaticResourcePath( url, localFolder );
      }
    }
  }

  protected void processExternalResources( PlatformPlugin plugin, Document doc ) {
    Node parentNode = doc.selectSingleNode( "//external-resources" ); //$NON-NLS-1$
    if ( parentNode == null ) {
      return;
    }
    for ( Object obj : parentNode.selectNodes( "file" ) ) {
      Element node = (Element) obj;
      if ( node != null ) {
        String context = node.attributeValue( "context" ); //$NON-NLS-1$
        String resource = node.getStringValue();
        plugin.addExternalResource( context, resource );
      }
    }
  }

  protected void processLifecycleListeners( PlatformPlugin plugin, Document doc ) {
    List<Node> nodes = doc.selectNodes( "//lifecycle-listener" ); //$NON-NLS-1$
    if ( nodes != null && !nodes.isEmpty() ) {
      for ( Node node : nodes ) {
        String classname = ( (Element) node ).attributeValue( CLASS_PROPERRTY );
        plugin.addLifecycleListenerClassname( classname );
      }
    }
  }

  protected void processBeans( PlatformPlugin plugin, Document doc ) {
    List<?> nodes = doc.selectNodes( "//bean" ); //$NON-NLS-1$
    for ( Object obj : nodes ) {
      Element node = (Element) obj;
      if ( node != null ) {
        plugin.addBean( new PluginBeanDefinition( node.attributeValue( "id" ), node.attributeValue( //$NON-NLS-1$
          CLASS_PROPERRTY ) ) );
      }
    }
  }

  protected void processWebservices( PlatformPlugin plugin, Document doc ) {
    List<?> nodes = doc.selectNodes( "//webservice" ); //$NON-NLS-1$
    for ( Object obj : nodes ) {
      Element node = (Element) obj;

      PluginServiceDefinition pws = new PluginServiceDefinition();

      pws.setId( getProperty( node, "id" ) ); //$NON-NLS-1$
      String type = getProperty( node, "type" ); //$NON-NLS-1$
      if ( !StringUtils.isEmpty( type ) ) {
        pws.setTypes( type.split( "," ) ); //$NON-NLS-1$
      }
      pws.setTitle( getProperty( node, "title" ) ); //$NON-NLS-1$
      pws.setDescription( getProperty( node, "description" ) ); //$NON-NLS-1$

      // TODO: add support for inline service class definition
      pws.setServiceBeanId( getProperty( node, "ref" ) ); //$NON-NLS-1$
      pws.setServiceClass( getProperty( node, CLASS_PROPERRTY ) );

      Collection<String> extraClasses = new ArrayList<>();
      List<?> extraNodes = node.selectNodes( "extra" ); //$NON-NLS-1$
      for ( Object extra : extraNodes ) {
        Element extraElement = (Element) extra;
        String extraClass = getProperty( extraElement, CLASS_PROPERRTY );
        if ( extraClasses != null ) {
          extraClasses.add( extraClass );
        }
      }
      pws.setExtraClasses( extraClasses );

      if ( pws.getServiceBeanId() == null && pws.getServiceClass() == null ) {
        PluginMessageLogger.add( Messages.getInstance().getString( "PluginManager.NO_SERVICE_CLASS_FOUND" ) ); //$NON-NLS-1$
      } else {
        plugin.addWebservice( pws );
      }
    }
  }

  protected void processPluginInfo( PlatformPlugin plugin, Document doc, String folder, IPentahoSession session ) {
    Element node = (Element) doc.selectSingleNode( "/plugin" ); //$NON-NLS-1$

    if ( node == null ) {
      return;
    }

    String title = node.attributeValue( "title" );
    plugin.setTitle( title );

    // "name" is the attribute that unique identifies a plugin. It acts as the plugin ID. For backwards compatibility,
    // if name is not provided, name is set to the value of the "title" attribute
    //
    String name = ( node.attributeValue( "name" ) != null ) ? node.attributeValue( "name" ) : title;
    if ( StringUtils.isEmpty( name ) ) {
      String msg =
        Messages.getInstance().getErrorString( "SystemPathXmlPluginProvider.ERROR_0002_PLUGIN_INVALID", folder );
      PluginMessageLogger.add( msg );
      Logger.error( getClass().toString(), msg );
    }

    plugin.setId( name );
    PluginMessageLogger.add( Messages.getInstance().getString(
      "SystemPathXmlPluginProvider.DISCOVERED_PLUGIN", name, folder ) );

    plugin.setDescription( node.attributeValue( "description" ) );

    plugin.setResourceBundleClassName( node.attributeValue( "resourcebundle" ) );

    IPlatformPlugin.ClassLoaderType loaderType = IPlatformPlugin.ClassLoaderType.DEFAULT;
    String loader = node.attributeValue( "loader" );
    if ( !StringUtils.isEmpty( loader ) ) {
      loaderType = IPlatformPlugin.ClassLoaderType.valueOf( loader.toUpperCase() );
    }
    plugin.setLoadertype( loaderType );
  }

  protected void processOverlays( PlatformPlugin plugin, Document doc, IPentahoSession session ) {
    // look for content types
    List<?> nodes = doc.selectNodes( "//overlays/overlay" ); //$NON-NLS-1$
    for ( Object obj : nodes ) {
      Element node = (Element) obj;
      DefaultXulOverlay overlay = processOverlay( node );

      if ( overlay != null ) {
        plugin.addOverlay( overlay );
      }
    }
  }

  public static DefaultXulOverlay processOverlay( Element node ) {
    DefaultXulOverlay overlay = null;

    String id = node.attributeValue( "id" ); //$NON-NLS-1$
    String resourceBundleUri = node.attributeValue( "resourcebundle" ); //$NON-NLS-1$
    String priority = node.attributeValue( "priority" );

    String xml = node.asXML();

    if ( StringUtils.isNotEmpty( id ) && StringUtils.isNotEmpty( xml ) ) {
      // check for overlay priority attribute. if not present, do not provide one
      // so default will be used
      if ( StringUtils.isNotEmpty( priority ) ) {
        try {
          overlay = new DefaultXulOverlay( id, null, xml, resourceBundleUri, Integer.parseInt( priority ) );
        } catch ( NumberFormatException e ) {
          // don't fail if attribute value is invalid. just use alt constructor without priority
          overlay = new DefaultXulOverlay( id, null, xml, resourceBundleUri );
        }
      } else {
        overlay = new DefaultXulOverlay( id, null, xml, resourceBundleUri );
      }
    }

    return overlay;
  }

  protected void processContentTypes( PlatformPlugin plugin, Document doc, IPentahoSession session ) {
    // look for content types
    List<?> nodes = doc.selectNodes( "//content-type" ); //$NON-NLS-1$
    for ( Object obj : nodes ) {
      Element node = (Element) obj;

      String title = XmlDom4JHelper.getNodeText( "title", node ); //$NON-NLS-1$
      String extension = node.attributeValue( "type" ); //$NON-NLS-1$

      if ( title != null && extension != null ) {
        String description = XmlDom4JHelper.getNodeText( "description", node, "" ); //$NON-NLS-1$ //$NON-NLS-2$
        String mimeType = node.attributeValue( "mime-type", "" ); //$NON-NLS-1$ //$NON-NLS-2$
        String iconUrl = XmlDom4JHelper.getNodeText( "icon-url", node, "" ); //$NON-NLS-1$ //$NON-NLS-2$

        ContentInfo contentInfo = new ContentInfo();
        contentInfo.setDescription( description );
        contentInfo.setTitle( title );
        contentInfo.setExtension( extension );
        contentInfo.setMimeType( mimeType );
        contentInfo.setIconUrl( iconUrl );

        List<?> operationNodes = node.selectNodes( "operations/operation" ); //$NON-NLS-1$
        for ( Object operationObj : operationNodes ) {
          Element operationNode = (Element) operationObj;
          String id = XmlDom4JHelper.getNodeText( "id", operationNode, "" ); //$NON-NLS-1$ //$NON-NLS-2$
          String perspective = XmlDom4JHelper.getNodeText( "perspective", operationNode, "" ); //$NON-NLS-1$ //$NON-NLS-2$
          if ( StringUtils.isNotEmpty( id ) ) {
            PluginOperation operation = new PluginOperation( id );
            if ( StringUtils.isNotEmpty( perspective ) ) {
              operation.setPerspective( perspective );
            }

            contentInfo.addOperation( operation );
          }
        }

        plugin.addContentInfo( contentInfo );
        PluginMessageLogger.add( Messages.getInstance().getString(
          "PluginManager.USER_CONTENT_TYPE_REGISTERED", extension, title ) ); //$NON-NLS-1$
      } else {
        PluginMessageLogger.add( Messages.getInstance().getString(
          "PluginManager.USER_CONTENT_TYPE_NOT_REGISTERED", extension, title ) ); //$NON-NLS-1$
      }
    }
  }

  /*
   * Finds propName as either an attribute of the given node or the text element of a child element called propName
   */
  private static String getProperty( Element node, String propName ) {
    String propValue = null;
    propValue = node.attributeValue( propName );
    if ( propValue == null ) {
      propValue = XmlDom4JHelper.getNodeText( propName, node, null );
    }
    return propValue;
  }

  protected void processContentGenerators( PlatformPlugin plugin, Document doc, IPentahoSession session, String folder,
                                           boolean hasLib ) {
    // look for content generators
    List<?> nodes = doc.selectNodes( "//content-generator" ); //$NON-NLS-1$
    for ( Object obj : nodes ) {
      Element node = (Element) obj;

      String className = getProperty( node, CLASS_PROPERRTY );
      if ( className == null ) {
        className = XmlDom4JHelper.getNodeText( "classname", node, null ); //$NON-NLS-1$
      }
      String id = node.attributeValue( "id" ); //$NON-NLS-1$
      String type = node.attributeValue( "type" ); //$NON-NLS-1$
      String url = node.attributeValue( "url" ); //$NON-NLS-1$
      String title = getProperty( node, "title" ); //$NON-NLS-1$
      String description = getProperty( node, "description" ); //$NON-NLS-1$
      try {
        if ( id != null && type != null && className != null && title != null ) {
          try {
            IContentGeneratorInfo info =
              createContentGenerator( plugin, id, title, description, type, url, className, session, folder );
            plugin.addContentGenerator( info );
          } catch ( Exception e ) {
            PluginMessageLogger.add( Messages.getInstance().getString(
              "PluginManager.USER_CONTENT_GENERATOR_NOT_REGISTERED", id, folder ) ); //$NON-NLS-1$
          }
        } else {
          PluginMessageLogger.add( Messages.getInstance().getString(
            "PluginManager.USER_CONTENT_GENERATOR_NOT_REGISTERED", id, folder ) ); //$NON-NLS-1$
        }
      } catch ( Exception e ) {
        PluginMessageLogger.add( Messages.getInstance().getString(
          "PluginManager.USER_CONTENT_GENERATOR_NOT_REGISTERED", id, folder ) ); //$NON-NLS-1$
        Logger.error( getClass().toString(), Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0006_CANNOT_CREATE_CONTENT_GENERATOR_FACTORY", folder ), e ); //$NON-NLS-1$
      }
    }
  }

  private static IContentGeneratorInfo createContentGenerator( PlatformPlugin plugin, String id, String title,
                                                               String description, String type, String url,
                                                               String className, IPentahoSession session,
                                                               String location ) {
    ContentGeneratorInfo info = new ContentGeneratorInfo();
    info.setId( id );
    info.setTitle( title );
    info.setDescription( description );
    info.setUrl( ( url != null ) ? url : "" ); //$NON-NLS-1$
    info.setType( type );
    info.setClassname( className );

    return info;
  }
}
