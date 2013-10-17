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

package org.pentaho.platform.plugin.services.pluginmgr;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
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
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An implmentation of {@link IPluginProvider} that searches for plugin.xml files in the Pentaho system path and
 * instantiates {@link IPlatformPlugin}s from the information in those files.
 * 
 * @author aphillips
 */
public class SystemPathXmlPluginProvider implements IPluginProvider {

  /**
   * Gets the list of plugins that this provider class has discovered.
   * 
   * @return an read-only list of plugins
   * @see IPluginProvider#getPlugins()
   * @throws PlatformPluginRegistrationException
   *           if there is a problem preventing the impl from looking for plugins
   */
  public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
    List<IPlatformPlugin> plugins = new ArrayList<IPlatformPlugin>();

    // look in each of the system setting folders looking for plugin.xml files
    String systemPath = PentahoSystem.getApplicationContext().getSolutionPath( "system" ); //$NON-NLS-1$
    File systemDir = new File( systemPath );
    if ( !systemDir.exists() || !systemDir.isDirectory() ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0004_CANNOT_FIND_SYSTEM_FOLDER" ) ); //$NON-NLS-1$
    }
    File[] kids = systemDir.listFiles();
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

  protected void processDirectory( List<IPlatformPlugin> plugins, File folder, IPentahoSession session )
    throws PlatformPluginRegistrationException {
    // see if there is a plugin.xml file
    FilenameFilter filter = new NameFileFilter( "plugin.xml", IOCase.SENSITIVE ); //$NON-NLS-1$
    File[] kids = folder.listFiles( filter );
    if ( kids == null || kids.length == 0 ) {
      return;
    }
    boolean hasLib = false;
    filter = new NameFileFilter( "lib", IOCase.SENSITIVE ); //$NON-NLS-1$
    kids = folder.listFiles( filter );
    if ( kids != null && kids.length > 0 ) {
      hasLib = kids[0].exists() && kids[0].isDirectory();
    }
    // we have found a plugin.xml file
    // get the file from the repository
    String path = "system" + RepositoryFile.SEPARATOR + folder.getName() + RepositoryFile.SEPARATOR + "plugin.xml"; //$NON-NLS-1$ //$NON-NLS-2$
    Document doc = null;
    try {
      try {
        org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
        reader.setEntityResolver( new SolutionURIResolver() );
        doc = reader.read( ActionSequenceResource.getInputStream( path, LocaleHelper.getLocale() ) );
      } catch ( Throwable t ) {
        // XML document can't be read. We'll just return a null document.
      }
      if ( doc != null ) {
        plugins.add( createPlugin( doc, session, folder.getName(), hasLib ) );
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

    String listenerCount = ( StringUtils.isEmpty( plugin.getLifecycleListenerClassname() ) ) ? "0" : "1"; //$NON-NLS-1$//$NON-NLS-2$

    String msg =
        Messages.getInstance().getString(
            "SystemPathXmlPluginProvider.PLUGIN_PROVIDES", //$NON-NLS-1$
            Integer.toString( plugin.getContentInfos().size() ),
            Integer.toString( plugin.getContentGenerators().size() ), Integer.toString( plugin.getOverlays().size() ),
            listenerCount );
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
    Element node = (Element) doc.selectSingleNode( "//lifecycle-listener" ); //$NON-NLS-1$
    if ( node != null ) {
      String classname = node.attributeValue( "class" ); //$NON-NLS-1$
      plugin.setLifecycleListenerClassname( classname );
    }
  }

  protected void processBeans( PlatformPlugin plugin, Document doc ) {
    List<?> nodes = doc.selectNodes( "//bean" ); //$NON-NLS-1$
    for ( Object obj : nodes ) {
      Element node = (Element) obj;
      if ( node != null ) {
        plugin.addBean( new PluginBeanDefinition( node.attributeValue( "id" ), node.attributeValue( "class" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
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
      pws.setServiceClass( getProperty( node, "class" ) ); //$NON-NLS-1$

      Collection<String> extraClasses = new ArrayList<String>();
      List<?> extraNodes = node.selectNodes( "extra" ); //$NON-NLS-1$
      for ( Object extra : extraNodes ) {
        Element extraElement = (Element) extra;
        String extraClass = getProperty( extraElement, "class" ); //$NON-NLS-1$
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

    // "name" is the attribute that unique identifies a plugin. It acts as the plugin ID. For backwards compatibility,
    // if name is not provided, name is set to the value of the "title" attribute
    //
    if ( node != null ) {
      String name =
          ( node.attributeValue( "name" ) != null ) ? node.attributeValue( "name" ) : node.attributeValue( "title" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if ( StringUtils.isEmpty( name ) ) {
        String msg =
            Messages.getInstance().getErrorString( "SystemPathXmlPluginProvider.ERROR_0002_PLUGIN_INVALID", folder ); //$NON-NLS-1$
        PluginMessageLogger.add( msg );
        Logger.error( getClass().toString(), msg );
      }

      plugin.setId( name );
      PluginMessageLogger.add( Messages.getInstance().getString(
        "SystemPathXmlPluginProvider.DISCOVERED_PLUGIN", name, folder ) ); //$NON-NLS-1$

      IPlatformPlugin.ClassLoaderType loaderType = IPlatformPlugin.ClassLoaderType.DEFAULT;
      String loader = node.attributeValue( "loader" ); //$NON-NLS-1$
      if ( !StringUtils.isEmpty( loader ) ) {
        loaderType = IPlatformPlugin.ClassLoaderType.valueOf( loader.toUpperCase() );
      }
      plugin.setLoadertype( loaderType );
    }
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
        String metaProviderClass = XmlDom4JHelper.getNodeText( "meta-provider", node, "" ); //$NON-NLS-1$ //$NON-NLS-2$

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
        if ( !StringUtils.isEmpty( metaProviderClass ) ) {
          plugin.getMetaProviderMap().put( contentInfo.getExtension(), metaProviderClass );
        }
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

      String className = getProperty( node, "class" ); //$NON-NLS-1$
      if ( className == null ) {
        className = XmlDom4JHelper.getNodeText( "classname", node, null ); //$NON-NLS-1$
      }
      String fileInfoClassName = XmlDom4JHelper.getNodeText( "fileinfo-classname", node, null ); //$NON-NLS-1$
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
          if ( !StringUtils.isEmpty( fileInfoClassName ) ) {
            plugin.getMetaProviderMap().put( type, fileInfoClassName );
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
      String description, String type, String url, String className, IPentahoSession session, String location )
    throws ClassNotFoundException, InstantiationException, IllegalAccessException {

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
