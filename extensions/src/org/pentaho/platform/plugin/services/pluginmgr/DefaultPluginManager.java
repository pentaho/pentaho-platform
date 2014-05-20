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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginManagerListener;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionFileMetaProvider;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginBeanDefinition;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.api.engine.PluginServiceDefinition;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.api.engine.ServiceInitializationException;
import org.pentaho.platform.api.engine.perspective.IPluginPerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.ServiceConfig;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.ui.xul.XulOverlay;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultPluginManager implements IPluginManager {

  private static final Log logger = LogFactory.getLog( DefaultPluginManager.class );

  private static final String DEFAULT_PERSPECTIVE = "generatedContent";

  // A namespacing prefix is added when registering meta provider objects in the object factory
  private static final String METAPROVIDER_KEY_PREFIX = "METAPROVIDER-"; //$NON-NLS-1$

  protected Map<String, ClassLoader> classLoaderMap = Collections.synchronizedMap( new HashMap<String, ClassLoader>() );

  protected Map<String, GenericApplicationContext> beanFactoryMap = Collections
    .synchronizedMap( new HashMap<String, GenericApplicationContext>() );

  protected Map<String, IPlatformPlugin> registeredPlugins = new Hashtable<String, IPlatformPlugin>();

  protected Map<String, IContentInfo> contentTypeByExtension = Collections
    .synchronizedMap( new HashMap<String, IContentInfo>() );

  protected List<XulOverlay> overlaysCache = Collections.synchronizedList( new ArrayList<XulOverlay>() );

  @Override
  public Set<String> getContentTypes() {
    // map.keySet returns a set backed by the map, so we cannot allow modification of the set
    return Collections.unmodifiableSet( contentTypeByExtension.keySet() );
  }

  @Override
  public List<XulOverlay> getOverlays() {
    return Collections.unmodifiableList( overlaysCache );
  }

  @Override
  public IContentInfo getContentTypeInfo( String type ) {
    return contentTypeByExtension.get( type );
  }

  /**
   * Clears all the lists and maps in preparation for reloading the state from the plugin provider. Fires the plugin
   * unloaded event for each known plugin.
   */
  private void unloadPlugins() {
    overlaysCache.clear();
    classLoaderMap.clear();

    // TODO: can we reset/reload the spring bean factory here?

    contentTypeByExtension.clear();
    // we do not need to synchronize here since unloadPlugins
    // is called within the synchronized block in reload
    for ( IPlatformPlugin plugin : registeredPlugins.values() ) {
      try {
        plugin.unLoaded();
      } catch ( Throwable t ) {
        // we do not want any type of exception to leak out and cause a problem here
        // A plugin unload should not adversely affect anything downstream, it should
        // log an error and otherwise fail silently
        String msg =
          Messages.getInstance().getErrorString(
            "PluginManager.ERROR_0014_PLUGIN_FAILED_TO_PROPERLY_UNLOAD", plugin.getId() ); //$NON-NLS-1$
        Logger.error( getClass().toString(), msg, t );
        PluginMessageLogger.add( msg );
      }
    }
    registeredPlugins.clear();
  }

  @Override
  public List<String> getRegisteredPlugins() {
    List<String> pluginIds = new ArrayList<String>( registeredPlugins.size() );
    for ( IPlatformPlugin plugin : registeredPlugins.values() ) {
      pluginIds.add( plugin.getId() );
    }
    return pluginIds;
  }

  @Deprecated
  public final boolean reload( IPentahoSession session ) {
    return reload();
  }

  @Override
  public final boolean reload() {
    IPentahoSession session = PentahoSessionHolder.getSession();
    boolean anyErrors = false;
    IPluginProvider pluginProvider = PentahoSystem.get( IPluginProvider.class, "IPluginProvider", session );
    List<IPlatformPlugin> providedPlugins = null;
    try {
      synchronized ( registeredPlugins ) {
        this.unloadPlugins();
      }
      // the plugin may fail to load during getPlugins without an exception thrown if the provider
      // is capable of discovering the plugin fine but there are structural problems with the plugin
      // itself. In this case a warning should be logged by the provider, but, again, no exception
      // is expected.
      providedPlugins = pluginProvider.getPlugins( session );

    } catch ( PlatformPluginRegistrationException e1 ) {
      String msg =
        Messages.getInstance().getErrorString( "PluginManager.ERROR_0012_PLUGIN_DISCOVERY_FAILED" ); //$NON-NLS-1$
      Logger.error( getClass().toString(), msg, e1 );
      PluginMessageLogger.add( msg );
      anyErrors = true;
    }

    // TODO: refresh appc context here?

    synchronized ( providedPlugins ) {

      for ( IPlatformPlugin plugin : providedPlugins ) {
        try {
          registeredPlugins.put( plugin.getId(), plugin );
          ClassLoader loader = setPluginClassLoader( plugin );
          initializeBeanFactory( plugin, loader );
        } catch ( Throwable t ) {
          // this has been logged already
          anyErrors = true;
          String msg =
            Messages.getInstance().getErrorString(
              "PluginManager.ERROR_0011_FAILED_TO_REGISTER_PLUGIN", plugin.getId() ); //$NON-NLS-1$
          Logger.error( getClass().toString(), msg, t );
          PluginMessageLogger.add( msg );
        }
      }

      registeredPlugins.clear();
      for ( IPlatformPlugin plugin : providedPlugins ) {
        try {
          GenericApplicationContext beanFactory = beanFactoryMap.get( plugin.getId() );
          if ( beanFactory != null ) {
            beanFactory.refresh();
          }
          registerPlugin( plugin );
          registeredPlugins.put( plugin.getId(), plugin );
        } catch ( Throwable t ) {
          // this has been logged already
          anyErrors = true;
          String msg =
            Messages.getInstance().getErrorString(
              "PluginManager.ERROR_0011_FAILED_TO_REGISTER_PLUGIN", plugin.getId() ); //$NON-NLS-1$
          Logger.error( getClass().toString(), msg, t );
          PluginMessageLogger.add( msg );
        }
      }
    }

    IServiceManager svcManager = PentahoSystem.get( IServiceManager.class, null );
    if ( svcManager != null ) {
      try {
        svcManager.initServices();
      } catch ( ServiceInitializationException e ) {
        String msg = Messages.getInstance()
          .getErrorString( "PluginManager.ERROR_0022_SERVICE_INITIALIZATION_FAILED" ); //$NON-NLS-1$
        Logger.error( getClass().toString(), msg, e );
        PluginMessageLogger.add( msg );
      }
    }

    return !anyErrors;
  }

  /**
   * Gets the plugin ready to handle lifecycle events.
   */
  private static void bootStrapPlugin( IPlatformPlugin plugin, ClassLoader loader )
    throws PlatformPluginRegistrationException {
    Object listener = null;
    try {
      if ( !StringUtils.isEmpty( plugin.getLifecycleListenerClassname() ) ) {
        listener = loader.loadClass( plugin.getLifecycleListenerClassname() ).newInstance();
      }
    } catch ( Throwable t ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0017_COULD_NOT_LOAD_PLUGIN_LIFECYCLE_LISTENER", plugin.getId(), plugin //$NON-NLS-1$
        .getLifecycleListenerClassname() ), t );
    }

    if ( listener != null ) {
      if ( !IPluginLifecycleListener.class.isAssignableFrom( listener.getClass() ) ) {
        throw new PlatformPluginRegistrationException(
          Messages
            .getInstance()
            .getErrorString(
              "PluginManager.ERROR_0016_PLUGIN_LIFECYCLE_LISTENER_WRONG_TYPE", plugin.getId(),
              plugin.getLifecycleListenerClassname() ) ); //$NON-NLS-1$
      }
      plugin.addLifecycleListener( (IPluginLifecycleListener) listener );
    }
  }

  @SuppressWarnings( "unchecked" )
  private void registerPlugin( final IPlatformPlugin plugin ) throws PlatformPluginRegistrationException,
    PluginLifecycleException {
    // TODO: we should treat the registration of a plugin as an atomic operation
    // with rollback if something is broken

    if ( StringUtils.isEmpty( plugin.getId() ) ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0026_PLUGIN_INVALID", plugin.getSourceDescription() ) ); //$NON-NLS-1$
    }

    if ( registeredPlugins.containsKey( plugin.getId() ) ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0024_PLUGIN_ALREADY_LOADED_BY_SAME_NAME", plugin.getId() ) ); //$NON-NLS-1$
    }

    ClassLoader loader = setPluginClassLoader( plugin );

    bootStrapPlugin( plugin, loader );

    plugin.init();

    registerContentTypes( plugin, loader );

    registerContentGenerators( plugin, loader );

    registerPerspectives( plugin, loader );

    // cache overlays
    overlaysCache.addAll( plugin.getOverlays() );

    // service registry must take place after bean registry since
    // a service class may be configured as a plugin bean
    registerServices( plugin, loader );

    PluginMessageLogger
      .add( Messages.getInstance().getString( "PluginManager.PLUGIN_REGISTERED", plugin.getId() ) ); //$NON-NLS-1$
    try {
      plugin.loaded();
    } catch ( Throwable t ) {
      // The plugin has already been loaded, so there is really no logical response to any type
      // of failure here except to log an error and otherwise fail silently
      String msg =
        Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0015_PLUGIN_LOADED_HANDLING_FAILED", plugin.getId() ); //$NON-NLS-1$
      Logger.error( getClass().toString(), msg, t );
      PluginMessageLogger.add( msg );
    }
  }

  private void registerPerspectives( IPlatformPlugin plugin, ClassLoader loader ) {
    for ( IPluginPerspective pluginPerspective : plugin.getPluginPerspectives() ) {
      PentahoSystem.get( IPluginPerspectiveManager.class ).addPluginPerspective( pluginPerspective );
    }
  }

  protected void registerContentTypes( IPlatformPlugin plugin, ClassLoader loader )
    throws PlatformPluginRegistrationException {
    // index content types and define any file meta providers
    for ( IContentInfo info : plugin.getContentInfos() ) {
      contentTypeByExtension.put( info.getExtension(), info );

      String metaProviderClass = plugin.getMetaProviderMap().get( info.getExtension() );

      // if a meta-provider is defined for this content type, then register it...
      if ( !StringUtils.isEmpty( metaProviderClass ) ) {
        Class<?> clazz = null;
        String defaultErrMsg =
          Messages
            .getInstance()
            .getErrorString(
              "PluginManager.ERROR_0013_FAILED_TO_SET_CONTENT_TYPE_META_PROVIDER", metaProviderClass,
              info.getExtension() ); //$NON-NLS-1$

        try {
          // do a test load to fail early if class not found
          clazz = loader.loadClass( metaProviderClass );
        } catch ( Exception e ) {
          throw new PlatformPluginRegistrationException( defaultErrMsg, e );
        }

        // check that the class is an accepted type
        if ( !( ISolutionFileMetaProvider.class.isAssignableFrom( clazz ) ) ) {
          throw new PlatformPluginRegistrationException(
            Messages
              .getInstance()
              .getErrorString(
                "PluginManager.ERROR_0019_WRONG_TYPE_FOR_CONTENT_TYPE_META_PROVIDER", metaProviderClass,
                info.getExtension() ) ); //$NON-NLS-1$
        }

        // the class is ok, so register it with the factory
        assertUnique( plugin.getId(), METAPROVIDER_KEY_PREFIX + info.getExtension() );
        BeanDefinition beanDef =
          BeanDefinitionBuilder.rootBeanDefinition( metaProviderClass ).setScope( BeanDefinition.SCOPE_PROTOTYPE )
            .getBeanDefinition();
        beanFactoryMap.get( plugin.getId() ).registerBeanDefinition( METAPROVIDER_KEY_PREFIX + info.getExtension(),
          beanDef );
      }
    }
  }

  /**
   * The native bean factory is the bean factory that has had all of its bean definitions loaded natively. In other
   * words, the plugin manager will not add any further bean definitions (i.e. from a plugin.xml file) into this
   * factory. This factory represents the one responsible for holding bean definitions for plugin.spring.xml or, if in a
   * unit test environment, the unit test pre-loaded bean factory.
   *
   * @return a bean factory will preconfigured bean definitions or <code>null</code> if no bean definition source is
   * available
   */
  protected BeanFactory getNativeBeanFactory( final IPlatformPlugin plugin, final ClassLoader loader ) {
    BeanFactory nativeFactory = null;
    if ( plugin.getBeanFactory() != null ) {
      // then we are probably in a unit test so just use the preconfigured one
      BeanFactory testFactory = plugin.getBeanFactory();
      if ( testFactory instanceof ConfigurableBeanFactory ) {
        ( (ConfigurableBeanFactory) testFactory ).setBeanClassLoader( loader );
      } else {
        logger.warn( Messages.getInstance().getString( "PluginManager.WARN_WRONG_BEAN_FACTORY_TYPE" ) ); //$NON-NLS-1$
      }
      nativeFactory = testFactory;
    } else {
      File f = new File( ( (PluginClassLoader) loader ).getPluginDir(), "plugin.spring.xml" ); //$NON-NLS-1$
      if ( f.exists() ) {
        logger.debug( "Found plugin spring file @ " + f.getAbsolutePath() ); //$NON-NLS-1$

        FileSystemResource fsr = new FileSystemResource( f );
        GenericApplicationContext appCtx = new GenericApplicationContext() {

          @Override
          protected void prepareBeanFactory( ConfigurableListableBeanFactory clBeanFactory ) {
            super.prepareBeanFactory( clBeanFactory );
            clBeanFactory.setBeanClassLoader( loader );
          }

          @Override
          public ClassLoader getClassLoader() {
            return loader;
          }

        };

        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader( appCtx );
        xmlReader.setBeanClassLoader( loader );
        xmlReader.loadBeanDefinitions( fsr );

        nativeFactory = appCtx;
      }
    }
    return nativeFactory;
  }

  /**
   * Initializes a bean factory for serving up instance of plugin classes.
   *
   * @return an instance of the factory that allows callers to continue to define more beans on it programmatically
   */
  protected void initializeBeanFactory( final IPlatformPlugin plugin, final ClassLoader loader )
    throws PlatformPluginRegistrationException {

    if ( !( loader instanceof PluginClassLoader ) ) {
      logger
        .warn(
          "Can't determine plugin dir to load spring file because classloader is not of type PluginClassLoader.  "
            //$NON-NLS-1$
            + "This is since we are probably in a unit test" ); //$NON-NLS-1$
      return;
    }

    //
    // Get the native factory (the factory that comes preconfigured via either Spring bean files or via JUnit test
    //
    BeanFactory nativeBeanFactory = getNativeBeanFactory( plugin, loader );

    //
    // Now create the definable factory for accepting old style bean definitions from IPluginProvider
    //

    GenericApplicationContext beanFactory = null;
    if ( nativeBeanFactory != null && nativeBeanFactory instanceof GenericApplicationContext ) {
      beanFactory = (GenericApplicationContext) nativeBeanFactory;
    } else {
      beanFactory = new GenericApplicationContext();
      beanFactory.setClassLoader( loader );
      beanFactory.getBeanFactory().setBeanClassLoader( loader );

      if ( nativeBeanFactory != null ) {
        beanFactory.getBeanFactory().setParentBeanFactory( nativeBeanFactory );
      }
    }

    beanFactoryMap.put( plugin.getId(), beanFactory );

    //
    // Register any beans defined via the pluginProvider
    //

    // we do not have to synchronize on the bean set here because the
    // map that backs the set is never modified after the plugin has
    // been made available to the plugin manager
    for ( PluginBeanDefinition def : plugin.getBeans() ) {
      // register by classname if id is null
      def.setBeanId( ( def.getBeanId() == null ) ? def.getClassname() : def.getBeanId() );
      assertUnique( plugin.getId(), def.getBeanId() );
      // defining plugin beans the old way through the plugin provider ifc supports only prototype scope
      BeanDefinition beanDef =
        BeanDefinitionBuilder.rootBeanDefinition( def.getClassname() ).setScope( BeanDefinition.SCOPE_PROTOTYPE )
          .getBeanDefinition();
      beanFactory.registerBeanDefinition( def.getBeanId(), beanDef );
    }

    StandaloneSpringPentahoObjectFactory pentahoFactory =
      new StandaloneSpringPentahoObjectFactory( "Plugin Factory ( " + plugin.getId() + " )" );
    pentahoFactory.init( null, beanFactory );

  }

  /**
   * A utility method that throws an exception if a bean with the id is already defined for this plugin
   */
  protected void assertUnique( String pluginId, String beanId ) throws PlatformPluginRegistrationException {
    if ( beanFactoryMap.get( pluginId ).containsBean( beanId ) ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0018_BEAN_ALREADY_REGISTERED", beanId, pluginId ) ); //$NON-NLS-1$
    }
  }

  private void registerServices( IPlatformPlugin plugin, ClassLoader loader )
    throws PlatformPluginRegistrationException {
    IServiceManager svcManager = PentahoSystem.get( IServiceManager.class, null );

    for ( PluginServiceDefinition pws : plugin.getServices() ) {
      for ( ServiceConfig ws : createServiceConfigs( pws, plugin, loader ) ) {
        try {
          svcManager.registerService( ws );
        } catch ( ServiceException e ) {
          throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
            "PluginManager.ERROR_0025_SERVICE_REGISTRATION_FAILED", ws.getId(), plugin.getId() ), e ); //$NON-NLS-1$
        }
      }
    }
  }

  /*
   * A utility method to convert plugin version of webservice definition to the official engine version consumable by an
   * IServiceManager
   */
  private Collection<ServiceConfig> createServiceConfigs( PluginServiceDefinition pws, IPlatformPlugin plugin,
                                                          ClassLoader loader )
    throws PlatformPluginRegistrationException {
    Collection<ServiceConfig> services = new ArrayList<ServiceConfig>();

    // Set the service type (one service config instance created per service type)
    //
    if ( pws.getTypes() == null || pws.getTypes().length < 1 ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0023_SERVICE_TYPE_UNSPECIFIED", pws.getId() ) ); //$NON-NLS-1$
    }
    for ( String type : pws.getTypes() ) {
      ServiceConfig ws = new ServiceConfig();

      ws.setServiceType( type );
      ws.setTitle( pws.getTitle() );
      ws.setDescription( pws.getDescription() );
      String serviceClassName =
        ( StringUtils.isEmpty( pws.getServiceClass() ) ) ? pws.getServiceBeanId() : pws.getServiceClass();

      String serviceId;
      if ( !StringUtils.isEmpty( pws.getId() ) ) {
        serviceId = pws.getId();
      } else {
        serviceId = serviceClassName;
        if ( serviceClassName.indexOf( '.' ) > 0 ) {
          serviceId = serviceClassName.substring( serviceClassName.lastIndexOf( '.' ) + 1 );
        }
      }
      ws.setId( serviceId );

      // Register the service class
      //
      final String serviceClassKey =
        ws.getServiceType() + "-" + ws.getId() + "/" + serviceClassName; //$NON-NLS-1$ //$NON-NLS-2$
      assertUnique( plugin.getId(), serviceClassKey );
      // defining plugin beans the old way through the plugin provider ifc supports only prototype scope
      BeanDefinition beanDef =
        BeanDefinitionBuilder.rootBeanDefinition( serviceClassName ).setScope( BeanDefinition.SCOPE_PROTOTYPE )
          .getBeanDefinition();
      beanFactoryMap.get( plugin.getId() ).registerBeanDefinition( serviceClassKey, beanDef );

      if ( !this.isBeanRegistered( serviceClassKey ) ) {
        throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0020_NO_SERVICE_CLASS_REGISTERED", serviceClassKey ) ); //$NON-NLS-1$
      }

      // Load/set the service class and supporting types
      //
      try {
        ws.setServiceClass( loadClass( serviceClassKey ) );

        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        if ( pws.getExtraClasses() != null ) {
          for ( String extraClass : pws.getExtraClasses() ) {
            classes.add( loadClass( extraClass ) );
          }
        }
        ws.setExtraClasses( classes );
      } catch ( PluginBeanException e ) {
        throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0021_SERVICE_CLASS_LOAD_FAILED", serviceClassKey ), e ); //$NON-NLS-1$
      }
      services.add( ws );
    }

    return services;
  }

  private ClassLoader setPluginClassLoader( IPlatformPlugin plugin ) throws PlatformPluginRegistrationException {
    ClassLoader loader = classLoaderMap.get( plugin.getId() );
    if ( loader == null ) {
      String pluginDirPath =
        PentahoSystem.getApplicationContext()
          .getSolutionPath( "system/" + plugin.getSourceDescription() ); //$NON-NLS-1$
      // need to scrub out duplicate file delimeters otherwise we will
      // not be able to locate resources in jars. This classloader ultimately
      // needs to be made less fragile
      pluginDirPath = pluginDirPath.replace( "//", "/" ); //$NON-NLS-1$ //$NON-NLS-2$
      Logger.debug( this,
        "plugin dir for " + plugin.getId() + " is [" + pluginDirPath + "]" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      File pluginDir = new File( pluginDirPath );
      if ( !pluginDir.exists() || !pluginDir.isDirectory() || !pluginDir.canRead() ) {
        throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0027_PLUGIN_DIR_UNAVAILABLE", pluginDir.getAbsolutePath() ) ); //$NON-NLS-1$
      }
      loader = new PluginClassLoader( pluginDir, this.getClass().getClassLoader() );
      if ( plugin.getLoaderType() == IPlatformPlugin.ClassLoaderType.OVERRIDING ) {
        ( (PluginClassLoader) loader ).setOverrideLoad( true );
      }
      classLoaderMap.put( plugin.getId(), loader );
    }
    return loader;
  }

  public ClassLoader getClassLoader( IPlatformPlugin plugin ) {
    return getClassLoader( plugin.getId() );
  }

  @Override
  public ClassLoader getClassLoader( String pluginId ) {
    return classLoaderMap.get( pluginId );
  }

  // public ListableBeanFactory asBeanFactory() {
  // return beanFactoryMap.get(pluginId);
  // }

  @Override
  public ListableBeanFactory getBeanFactory( String pluginId ) {
    return beanFactoryMap.get( pluginId ).getBeanFactory();
  }

  private void registerContentGenerators( IPlatformPlugin plugin, ClassLoader loader )
    throws PlatformPluginRegistrationException {
    // register the content generators
    for ( IContentGeneratorInfo cgInfo : plugin.getContentGenerators() ) {
      // define the bean in the factory
      BeanDefinition beanDef =
        BeanDefinitionBuilder.rootBeanDefinition( cgInfo.getClassname() ).setScope( BeanDefinition.SCOPE_PROTOTYPE )
          .getBeanDefinition();
      GenericApplicationContext factory = beanFactoryMap.get( plugin.getId() );
      // register bean with alias of content generator id (old way)
      factory.registerBeanDefinition( cgInfo.getId(), beanDef );
      // register bean with alias of type (with default perspective) as well (new way)
      factory.registerAlias( cgInfo.getId(), cgInfo.getType() );

      PluginMessageLogger.add( Messages.getInstance().getString(
        "PluginManager.USER_CONTENT_GENERATOR_REGISTERED", cgInfo.getId(), plugin.getId() ) ); //$NON-NLS-1$
    }
  }

  public Object getBean( String beanId, Class<?> requiredType ) {
    if ( beanId == null ) {
      throw new IllegalArgumentException( "beanId cannot be null" ); //$NON-NLS-1$
    }

    Object bean = null;
    for ( GenericApplicationContext beanFactory : beanFactoryMap.values() ) {
      if ( beanFactory.containsBean( beanId ) ) {
        if ( requiredType == null ) {
          bean = beanFactory.getBean( beanId );
        } else {
          bean = beanFactory.getBean( beanId, requiredType );
        }
      }
    }
    if ( bean == null ) {
      throw new NoSuchBeanDefinitionException( "Could not find bean with id " + beanId );
    }
    return bean;
  }

  @Override
  public Object getBean( String beanId ) throws PluginBeanException {
    if ( beanId == null ) {
      throw new IllegalArgumentException( "beanId cannot be null" ); //$NON-NLS-1$
    }

    Object bean = null;
    for ( GenericApplicationContext beanFactory : beanFactoryMap.values() ) {
      if ( beanFactory.containsBean( beanId ) ) {
        try {
          bean = beanFactory.getBean( beanId );
        } catch ( Throwable ex ) { // Catching throwable on purpose
          throw new PluginBeanException( ex );
        }
      }
    }
    if ( bean == null ) {
      throw new PluginBeanException( Messages.getInstance().getString(
        "PluginManager.WARN_CLASS_NOT_REGISTERED", beanId ) ); //$NON-NLS-1$
    }
    return bean;
  }

  @Override
  public IContentGenerator getContentGenerator( String type, String perspectiveName ) {
    IContentGenerator cg = null;
    if ( perspectiveName == null || perspectiveName.equals( DEFAULT_PERSPECTIVE ) ) {
      cg = (IContentGenerator) getBean( type, IContentGenerator.class );
    } else {
      String beanId = ( perspectiveName == null ) ? type : type + "." + perspectiveName; //$NON-NLS-1$
      try {
        cg = (IContentGenerator) getBean( beanId, IContentGenerator.class );
      } catch ( NoSuchBeanDefinitionException e ) {
        // fallback condition, look for a type agnostic content generator
        try {
          cg = (IContentGenerator) getBean( perspectiveName, IContentGenerator.class );
        } catch ( NoSuchBeanDefinitionException e2 ) {
          throw new NoSuchBeanDefinitionException( "Failed to find bean: " + e.getMessage() + " : " + e2.getMessage() );
        }
      }
    }
    return cg;
  }

  public IAction getAction( String type, String perspectiveName ) {
    IAction action = null;
    String beanId = ( perspectiveName == null ) ? type : type + "." + perspectiveName; //$NON-NLS-1$
    try {
      action = (IAction) getBean( beanId, IAction.class );
    } catch ( NoSuchBeanDefinitionException e ) {
      // fallback condition, look for a type agnostic content generator
      try {
        action = (IAction) getBean( perspectiveName, IAction.class );
      } catch ( NoSuchBeanDefinitionException e2 ) {
        throw new NoSuchBeanDefinitionException( "Failed to find bean: " + e.getMessage() + " : " + e2.getMessage() );
      }
    }
    return action;
  }

  @Override
  public Class<?> loadClass( String beanId ) throws PluginBeanException {
    if ( beanId == null ) {
      throw new IllegalArgumentException( "beanId cannot be null" ); //$NON-NLS-1$
    }
    Class<?> type = null;
    for ( GenericApplicationContext beanFactory : beanFactoryMap.values() ) {
      if ( beanFactory.containsBean( beanId ) ) {
        try {
          type = beanFactory.getType( beanId );
          break;
        } catch ( Throwable ex ) { // Catching throwable on purpose
          throw new PluginBeanException( ex );
        }
      }
    }

    if ( type == null ) {
      throw new PluginBeanException( Messages.getInstance().getString(
        "PluginManager.WARN_CLASS_NOT_REGISTERED", beanId ) ); //$NON-NLS-1$
    }
    return type;
  }

  @Override
  public boolean isBeanRegistered( String beanId ) {
    if ( beanId == null ) {
      throw new IllegalArgumentException( "beanId cannot be null" ); //$NON-NLS-1$
    }

    boolean registered = false;
    for ( GenericApplicationContext beanFactory : beanFactoryMap.values() ) {
      if ( beanFactory.containsBean( beanId ) ) {
        registered = true;
      }
    }

    return registered;
  }

  @Override
  public void unloadAllPlugins() {
    synchronized ( registeredPlugins ) {
      this.unloadPlugins();
    }
  }

  public Object getPluginSetting( IPlatformPlugin plugin, String key, String defaultValue ) {
    return getPluginSetting( plugin.getId(), key, defaultValue );
  }

  @Override
  public Object getPluginSetting( String pluginId, String key, String defaultValue ) {
    IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
    ClassLoader classLoader = classLoaderMap.get( pluginId );
    return resLoader.getPluginSetting( classLoader, key, defaultValue );
  }

  private Collection<String> getBeanIdsForType( String pluginId, Class<?> clazz ) {
    ArrayList<String> ids = new ArrayList<String>();

    ListableBeanFactory fac = beanFactoryMap.get( pluginId ).getBeanFactory();

    String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors( fac, clazz );
    for ( String beanName : names ) {
      ids.add( beanName );
      for ( String beanAlias : fac.getAliases( beanName ) ) {
        ids.add( beanAlias );
      }
    }
    return ids;
  }

  @Override
  public String getPluginIdForType( String contentType ) {
    for ( String pluginId : getRegisteredPlugins() ) {
      for ( String beanId : getBeanIdsForType( pluginId, IContentGenerator.class ) ) {
        String serviceContentType = beanId;
        if ( beanId.contains( "." ) ) { //$NON-NLS-1$
          serviceContentType = beanId.substring( 0, beanId.indexOf( '.' ) );
        }
        if ( contentType.equals( serviceContentType ) ) {
          return pluginId;
        }
      }
    }

    // if no content generator was found in any of the plugins that can service contentType, return null
    return null;
  }

  @Override
  @Deprecated
  public IContentGenerator getContentGeneratorForType( String type, IPentahoSession session )
    throws ObjectFactoryException {
    try {
      return getContentGenerator( type, (String) null );
    } catch ( NoSuchBeanDefinitionException e ) {
      throw new ObjectFactoryException( e );
    }
  }

  @Override
  public String getPluginIdForClassLoader( ClassLoader classLoader ) {
    if ( classLoader == null ) {
      return null;
    }
    for ( String pluginId : classLoaderMap.keySet() ) {
      ClassLoader maybeClassLoader = classLoaderMap.get( pluginId );
      if ( maybeClassLoader.equals( classLoader ) ) {
        return pluginId;
      }
    }
    return null;
  }

  private String trimLeadingSlash( String path ) {
    return ( path.startsWith( "/" ) ) ? path.substring( 1 ) : path; //$NON-NLS-1$
  }

  /**
   * Return <code>true</code> if the servicePath is being addressed by the requestPath. The request path is said to
   * request the service if it contains at least ALL of the elements of the servicePath, in order. It may include more
   * than these elements but it must contain at least the servicePath.
   *
   * @param servicePath
   * @param requestPath
   * @return <code>true</code> if the servicePath is being addressed by the requestPath
   */
  protected boolean isRequested( String servicePath, String requestPath ) {
    String[] requestPathElements = trimLeadingSlash( requestPath ).split( "/" ); //$NON-NLS-1$
    String[] servicePathElements = trimLeadingSlash( servicePath ).split( "/" ); //$NON-NLS-1$

    if ( requestPathElements.length < servicePathElements.length ) {
      return false;
    }

    for ( int i = 0; i < servicePathElements.length; i++ ) {
      if ( !requestPathElements[ i ].equals( servicePathElements[ i ] ) ) {
        return false;
      }
    }
    return true;
  }

  @Deprecated
  public String getServicePlugin( String path ) {
    for ( IPlatformPlugin plugin : registeredPlugins.values() ) {
      String pluginId = getStaticResourcePluginId( plugin, path );
      if ( pluginId != null ) {
        return pluginId;
      }

      for ( IContentGeneratorInfo contentGenerator : plugin.getContentGenerators() ) {
        String cgId = contentGenerator.getId();
        if ( isRequested( cgId, path ) ) {
          return plugin.getId();
        }
      }
    }

    return null;
  }

  private String getStaticResourcePluginId( IPlatformPlugin plugin, String path ) {
    Map<String, String> resourceMap = plugin.getStaticResourceMap();
    for ( String url : resourceMap.keySet() ) {
      if ( isRequested( url, path ) ) {
        return plugin.getId();
      }
    }
    return null;
  }

  @Deprecated
  public boolean isStaticResource( String path ) {
    for ( IPlatformPlugin plugin : registeredPlugins.values() ) {
      String pluginId = getStaticResourcePluginId( plugin, path );
      if ( pluginId != null ) {
        return true;
      }
    }
    return false;
  }

  public boolean isPublic( String pluginId, String path ) {
    IPlatformPlugin plugin = registeredPlugins.get( pluginId );
    if ( plugin == null ) {
      return false;
    }
    Map<String, String> resourceMap = plugin.getStaticResourceMap();
    if ( path.startsWith( "/" ) ) { //$NON-NLS-1$
      path = path.substring( 1 );
    }
    for ( String pluginRelativeDir : resourceMap.values() ) {
      if ( path.startsWith( pluginRelativeDir ) ) {
        return true;
      }
    }
    return false;
  }

  @Deprecated
  public InputStream getStaticResource( String path ) {
    for ( IPlatformPlugin plugin : registeredPlugins.values() ) {
      Map<String, String> resourceMap = plugin.getStaticResourceMap();
      for ( String url : resourceMap.keySet() ) {
        if ( isRequested( url, path ) ) {
          IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
          ClassLoader classLoader = classLoaderMap.get( plugin.getId() );
          String resourcePath = path.replace( url, resourceMap.get( url ) );
          return resLoader.getResourceAsStream( classLoader, resourcePath );
        }
      }
    }
    return null;
  }

  public List<String> getExternalResourcesForContext( String context ) {
    List<String> resources = new ArrayList<String>();
    for ( IPlatformPlugin plugin : registeredPlugins.values() ) {
      List<String> pluginRes = plugin.getExternalResourcesForContext( context );
      if ( pluginRes != null ) {
        resources.addAll( pluginRes );
      }
    }
    return resources;
  }

  @Override
  public List<String> getPluginRESTPerspectivesForType( String contentType ) {
    List<String> pluginPerspectives = new ArrayList<String>();
    for ( String pluginId : getRegisteredPlugins() ) {
      for ( String beanId : getBeanIdsForType( pluginId, IContentGenerator.class ) ) {
        String serviceContentType = beanId;
        if ( beanId.contains( "." ) ) { //$NON-NLS-1$
          serviceContentType = beanId.substring( 0, beanId.indexOf( '.' ) );
        }
        if ( serviceContentType != null && serviceContentType.equals( contentType ) ) {
          if ( beanId.contains( "." ) ) { //$NON-NLS-1$
            pluginPerspectives.add( beanId.substring( beanId.lastIndexOf( '.' ), beanId.length() ) );
          }
        }
      }
    }
    return pluginPerspectives;
  }

  @Override
  public List<String> getPluginRESTPerspectivesForId( String id ) {
    List<String> pluginPerspectives = new ArrayList<String>();
    for ( String pluginId : getRegisteredPlugins() ) {
      if ( id.equals( pluginId ) ) {
        for ( String beanId : getBeanIdsForType( pluginId, IContentGenerator.class ) ) {
          if ( beanId.contains( "." ) ) { //$NON-NLS-1$
            pluginPerspectives.add( beanId.substring( beanId.lastIndexOf( '.' ) + 1, beanId.length() ) );
          }
        }
      }
    }
    return pluginPerspectives;
  }

  @Override public void addPluginManagerListener( IPluginManagerListener listener ) {

  }
}
