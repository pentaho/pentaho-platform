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
 *
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.plugin.services.pluginmgr;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentGeneratorInvoker;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginManagerListener;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginBeanDefinition;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.api.engine.PluginServiceDefinition;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.api.engine.ServiceInitializationException;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.config.PropertiesFileConfiguration;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.spring.PentahoBeanScopeValidatorPostProcessor;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.references.PrototypePentahoObjectReference;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.ServiceConfig;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.ui.xul.XulOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * An IPluginManager implementation based on registering objects to the PentahoSystem and querying for them there. This
 * supports finding types registered through other systems, allowing for the transition away from IPluginManager to
 * plain PentahoSystem.getXXX calls.
 * <p/>
 * Created by nbaker on 4/18/14.
 */
public class PentahoSystemPluginManager implements IPluginManager {

  private static final String DEFAULT_PERSPECTIVE = "generatedContent";
  private static final String METAPROVIDER_KEY_PREFIX = "METAPROVIDER-";
  public static final String CONTENT_TYPE = "content-type";
  public static final String PLUGIN_ID = "plugin-id";
  public static final String SETTINGS_PREFIX = "settings/";

  private final Multimap<String, IPentahoObjectRegistration> handleRegistry =
    Multimaps.synchronizedMultimap( ArrayListMultimap
      .<String, IPentahoObjectRegistration>create() );
  private ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );
  private Logger logger = LoggerFactory.getLogger( getClass() );
  private Set<IPluginManagerListener> listeners = new HashSet<IPluginManagerListener>();

  private static void createAndRegisterLifecycleListeners( IPlatformPlugin plugin, ClassLoader loader )
    throws PlatformPluginRegistrationException {
    try {
      if ( plugin.getLifecycleListenerClassnames() != null ) {
        for ( String pluginLifecycleListener : plugin.getLifecycleListenerClassnames() ) {
          Object listener = loader.loadClass( pluginLifecycleListener ).getDeclaredConstructor().newInstance();
          if ( !IPluginLifecycleListener.class.isAssignableFrom( listener.getClass() ) ) {
            throw new PlatformPluginRegistrationException(
              Messages
                .getInstance()
                .getErrorString(
                  "PluginManager.ERROR_0016_PLUGIN_LIFECYCLE_LISTENER_WRONG_TYPE", plugin.getId(),
                  plugin.getLifecycleListenerClassnames() )
            ); //$NON-NLS-1$
          }
          plugin.addLifecycleListener( (IPluginLifecycleListener) listener );
        }
      }
    } catch ( Throwable t ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0017_COULD_NOT_LOAD_PLUGIN_LIFECYCLE_LISTENER", plugin.getId(), plugin
          .getLifecycleListenerClassnames()
      ), t );
    }
  }

  @Override
  public Set<String> getContentTypes() {
    final HashSet<String> types = new HashSet<String>();
    final List<IContentInfo> contentInfos = PentahoSystem.getAll( IContentInfo.class, null );
    for ( IContentInfo contentInfo : contentInfos ) {
      types.add( contentInfo.getExtension() );
    }
    return types;
  }

  @Override
  public IContentInfo getContentTypeInfo( String type ) {
    if ( type.contains( "." ) ) {
      type = type.substring( type.lastIndexOf( "." ) + 1 );
    }
    return PentahoSystem.get( IContentInfo.class, PentahoSessionHolder.getSession(), Collections
      .singletonMap( "extension", type ) );
  }

  @Override
  public IContentGenerator getContentGeneratorForType( String type, IPentahoSession session )
    throws ObjectFactoryException {

    // first we check: is there any IContentGeneratorInvoker implementation on the bean registry? If so: use it;
    final IContentGeneratorInvoker cgInvoker = PentahoSystem.get( IContentGeneratorInvoker.class );
    if ( cgInvoker != null && cgInvoker.isSupportedContent( type ) ) {
      logger.info(
        "Located IContentGeneratorInvoker that supports content of type '" + type + "':" + cgInvoker.getClass()
          .getName() );
      return cgInvoker.getContentGenerator();
    }

    // otherwise, gracefully fallback to PentahoSystemPluginManager's internal IContentGenerator discovery logic;

    return PentahoSystem.get( IContentGenerator.class, session, Collections.singletonMap( CONTENT_TYPE, type ) );
  }

  @Override
  public boolean reload() {
    return reload( PentahoSessionHolder.getSession() );
  }

  private void unloadPlugins() {


    // we do not need to synchronize here since unloadPlugins
    // is called within the synchronized block in reload
    for ( IPlatformPlugin plugin : PentahoSystem.getAll( IPlatformPlugin.class ) ) {
      try {
        plugin.unLoaded();

        // if a spring app context was registered for this plugin, remove and close it
        final GenericApplicationContext appContext = PentahoSystem
          .get( GenericApplicationContext.class, null, Collections.singletonMap( PLUGIN_ID, plugin.getId() ) );

        if ( appContext != null ) {
          final StandaloneSpringPentahoObjectFactory pentahoObjectFactory =
            StandaloneSpringPentahoObjectFactory.getInstance( appContext );

          if ( pentahoObjectFactory != null ) {
            PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
          }
          appContext.close();
        }

      } catch ( Throwable t ) {
        // we do not want any type of exception to leak out and cause a problem here
        // A plugin unload should not adversely affect anything downstream, it should
        // log an error and otherwise fail silently
        String msg =
          Messages.getInstance().getErrorString(
            "PluginManager.ERROR_0014_PLUGIN_FAILED_TO_PROPERLY_UNLOAD", plugin.getId() ); //$NON-NLS-1$
        logger.error( getClass().toString(), msg, t );
        PluginMessageLogger.add( msg );
      }
      final ClassLoader classLoader =
        PentahoSystem.get( ClassLoader.class, null, Collections.singletonMap( PLUGIN_ID, plugin.getId() ) );
      if ( classLoader != null ) {
        try {
          ( (PluginClassLoader) classLoader ).close();
        } catch ( IOException e ) {
          logger.error( "errror closing plugin clasloader", e );
        }
      }
    }


    for ( Map.Entry<String, IPentahoObjectRegistration> entry : handleRegistry.entries() ) {
      entry.getValue().remove();
    }
    handleRegistry.clear();

  }

  @Override
  public boolean reload( IPentahoSession session ) {

    boolean anyErrors = false;
    IPluginProvider pluginProvider = PentahoSystem.get( IPluginProvider.class, "IPluginProvider", session );
    List<IPlatformPlugin> providedPlugins = Collections.emptyList();
    try {
      this.unloadPlugins();

      // the plugin may fail to load during getPlugins without an exception thrown if the provider
      // is capable of discovering the plugin fine but there are structural problems with the plugin
      // itself. In this case a warning should be logged by the provider, but, again, no exception
      // is expected.
      providedPlugins = pluginProvider.getPlugins( session );

    } catch ( PlatformPluginRegistrationException e1 ) {
      String msg =
        Messages.getInstance().getErrorString( "PluginManager.ERROR_0012_PLUGIN_DISCOVERY_FAILED" );
      org.pentaho.platform.util.logging.Logger.error( getClass().toString(), msg, e1 );
      PluginMessageLogger.add( msg );
      anyErrors = true;
    }

    for ( IPlatformPlugin plugin : providedPlugins ) {
      try {
        IPlatformPlugin existingPlugin =
          PentahoSystem.get( IPlatformPlugin.class, null, Collections.singletonMap( PLUGIN_ID, plugin.getId() ) );
        if ( existingPlugin != null ) {
          throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
            "PluginManager.ERROR_0024_PLUGIN_ALREADY_LOADED_BY_SAME_NAME", plugin.getId() ) );
        }

        final ClassLoader classloader = createClassloader( plugin );

        // Register the classloader, Spring App Context and Object Factory with PentahoSystem
        IPentahoObjectRegistration handle = PentahoSystem.registerReference(
          new SingletonPentahoObjectReference.Builder<IPlatformPlugin>( IPlatformPlugin.class )
            .object( plugin )
            .attributes( Collections.<String, Object>singletonMap( PLUGIN_ID, plugin.getId() ) ).build(),
          IPlatformPlugin.class
        );
        registerReference( plugin.getId(), handle );
        handle =
          PentahoSystem.registerReference(
            new SingletonPentahoObjectReference.Builder<ClassLoader>( ClassLoader.class ).object( classloader )
              .attributes( Collections.<String, Object>singletonMap( PLUGIN_ID, plugin.getId() ) ).build(),
            ClassLoader.class
          );
        registerReference( plugin.getId(), handle );

        final GenericApplicationContext beanFactory = createBeanFactory( plugin, classloader );

        final StandaloneSpringPentahoObjectFactory pentahoFactory =
          new StandaloneSpringPentahoObjectFactory( "Plugin Factory ( " + plugin.getId() + " )" );
        pentahoFactory.init( null, beanFactory );
        beanFactory.refresh();

        handle =
          PentahoSystem.registerReference(
            new SingletonPentahoObjectReference.Builder<GenericApplicationContext>(
              GenericApplicationContext.class )
              .object( beanFactory )
              .attributes( Collections.<String, Object>singletonMap( PLUGIN_ID, plugin.getId() ) ).build(),
            IPentahoRegistrableObjectFactory.Types.ALL
          );
        registerReference( plugin.getId(), handle );

        handle =
          PentahoSystem.registerReference(
            new SingletonPentahoObjectReference.Builder<IPentahoObjectFactory>( IPentahoObjectFactory.class )
              .object( pentahoFactory )
              .attributes( Collections.<String, Object>singletonMap( PLUGIN_ID, plugin.getId() ) ).build(),
            IPentahoObjectFactory.class
          );
        registerReference( plugin.getId(), handle );


      } catch ( Throwable t ) {
        // this has been logged already
        anyErrors = true;
        String msg =
          Messages.getInstance().getErrorString(
            "PluginManager.ERROR_0011_FAILED_TO_REGISTER_PLUGIN", plugin.getId() );
        org.pentaho.platform.util.logging.Logger.error( getClass().toString(), msg, t );
        PluginMessageLogger.add( msg );
      }
    }


    for ( IPlatformPlugin plugin : providedPlugins ) {
      try {
        registerPlugin( plugin );
      } catch ( Throwable t ) {
        // this has been logged already
        anyErrors = true;
        String msg =
          Messages.getInstance().getErrorString(
            "PluginManager.ERROR_0011_FAILED_TO_REGISTER_PLUGIN", plugin.getId() );
        org.pentaho.platform.util.logging.Logger.error( getClass().toString(), msg, t );
        PluginMessageLogger.add( msg );
      }

    }

    IServiceManager svcManager = PentahoSystem.get( IServiceManager.class, null );
    if ( svcManager != null ) {
      try {
        svcManager.initServices();
      } catch ( ServiceInitializationException e ) {
        String msg = Messages.getInstance()
          .getErrorString( "PluginManager.ERROR_0022_SERVICE_INITIALIZATION_FAILED" );
        org.pentaho.platform.util.logging.Logger.error( getClass().toString(), msg, e );
        PluginMessageLogger.add( msg );
      }
    }

    for ( IPluginManagerListener listener : listeners ) {
      listener.onReload();
    }

    return !anyErrors;
  }

  @SuppressWarnings( "unchecked" )
  private void registerPlugin( final IPlatformPlugin plugin ) throws PlatformPluginRegistrationException,
    PluginLifecycleException {
    // TODO: we should treat the registration of a plugin as an atomic operation
    // with rollback if something is broken

    if ( StringUtils.isEmpty( plugin.getId() ) ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0026_PLUGIN_INVALID", plugin.getSourceDescription() ) );
    }

    ClassLoader loader = PentahoSystem.get( ClassLoader.class, null,
      Collections.singletonMap( PLUGIN_ID, plugin.getId() ) );
    GenericApplicationContext beanFactory = PentahoSystem
      .get( GenericApplicationContext.class, null, Collections.singletonMap( PLUGIN_ID, plugin.getId() ) );
    createAndRegisterLifecycleListeners( plugin, loader );

    plugin.init();

    registerContentTypes( plugin, loader, beanFactory );

    registerContentGenerators( plugin, loader, beanFactory );

    registerPerspectives( plugin, loader );

    registerOverlays( plugin );

    registerSettings( plugin, loader );

    // service registry must take place after bean registry since
    // a service class may be configured as a plugin bean
    registerServices( plugin, loader, beanFactory );

    PluginMessageLogger
      .add( Messages.getInstance().getString( "PluginManager.PLUGIN_REGISTERED", plugin.getId() ) );
    try {
      plugin.loaded();
    } catch ( Throwable t ) {
      // The plugin has already been loaded, so there is really no logical response to any type
      // of failure here except to log an error and otherwise fail silently
      String msg =
        Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0015_PLUGIN_LOADED_HANDLING_FAILED", plugin.getId() );
      org.pentaho.platform.util.logging.Logger.error( getClass().toString(), msg, t );
      PluginMessageLogger.add( msg );
    }
  }

  private void registerOverlays( IPlatformPlugin plugin ) {
    int priority = plugin.getOverlays().size();
    for ( XulOverlay overlay : plugin.getOverlays() ) {
      // preserve ordering as it may be significant
      final IPentahoObjectRegistration referenceHandle = PentahoSystem.registerReference(
        new SingletonPentahoObjectReference.Builder<XulOverlay>( XulOverlay.class )
          .object( overlay ).attributes(
            Collections.<String, Object>singletonMap( PLUGIN_ID, plugin.getId() ) ).priority( priority ).build(),
        XulOverlay.class
      );
      priority--;
      registerReference( plugin.getId(), referenceHandle );
    }
  }

  private void registerServices( IPlatformPlugin plugin, ClassLoader loader, GenericApplicationContext beanFactory )
    throws PlatformPluginRegistrationException {
    IServiceManager svcManager = PentahoSystem.get( IServiceManager.class, null );

    for ( PluginServiceDefinition pws : plugin.getServices() ) {
      for ( ServiceConfig ws : createServiceConfigs( pws, plugin, loader, beanFactory ) ) {
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
                                                          ClassLoader loader, GenericApplicationContext beanFactory )
    throws PlatformPluginRegistrationException {
    Collection<ServiceConfig> services = new ArrayList<ServiceConfig>();

    // Set the service type (one service config instance created per service type)
    //
    if ( pws.getTypes() == null || pws.getTypes().length < 1 ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0023_SERVICE_TYPE_UNSPECIFIED", pws.getId() ) );
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
        ws.getServiceType() + "-" + ws.getId() + "/" + serviceClassName;
      assertUnique( beanFactory, plugin.getId(), serviceClassKey );
      // defining plugin beans the old way through the plugin provider ifc supports only prototype scope
      BeanDefinition beanDef =
        BeanDefinitionBuilder.rootBeanDefinition( serviceClassName ).setScope( BeanDefinition.SCOPE_PROTOTYPE )
          .getBeanDefinition();

      beanFactory.registerBeanDefinition( serviceClassKey, beanDef );

      if ( !this.isBeanRegistered( serviceClassKey ) ) {
        throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0020_NO_SERVICE_CLASS_REGISTERED", serviceClassKey ) );
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
          "PluginManager.ERROR_0021_SERVICE_CLASS_LOAD_FAILED", serviceClassKey ), e );
      }
      services.add( ws );
    }

    return services;
  }

  private void registerSettings( IPlatformPlugin plugin, ClassLoader loader ) {

    IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );


    InputStream stream = resLoader.getResourceAsStream( loader, "settings.xml" );
    if ( stream == null ) {
      // No settings.xml is fine
      return;
    }
    Properties properties = new Properties();
    try {
      Document docFromStream = XmlDom4JHelper.getDocFromStream( stream );
      for ( Object element : docFromStream.getRootElement().elements() ) {
        Element ele = (Element) element;
        String name = ele.getName();
        String value = ele.getText();
        properties.put( "settings/" + name, value );
      }
    } catch ( DocumentException | IOException e ) {
      logger.error( "Error parsing settings.xml for plugin: " + plugin.getId(), e );
    }
    try {
      systemConfig.registerConfiguration( new PropertiesFileConfiguration( plugin.getId(), properties ) );
    } catch ( IOException e ) {
      logger.error( "Error registering settings.xml for plugin: " + plugin.getId(), e );
    }

  }

  private void registerPerspectives( IPlatformPlugin plugin, ClassLoader loader ) {
    for ( IPluginPerspective pluginPerspective : plugin.getPluginPerspectives() ) {

      //      PentahoSystem.get( IPluginPerspectiveManager.class ).addPluginPerspective( pluginPerspective );

      final IPentahoObjectRegistration referenceHandle = PentahoSystem.registerReference(
        new SingletonPentahoObjectReference.Builder<IPluginPerspective>( IPluginPerspective.class )
          .object( pluginPerspective )
          .attributes( Collections.<String, Object>singletonMap( PLUGIN_ID, plugin.getId() ) ).build(),
        IPluginPerspective.class
      );

      registerReference( plugin.getId(), referenceHandle );
    }
  }

  private void registerContentGenerators( IPlatformPlugin plugin, ClassLoader loader,
                                          final GenericApplicationContext beanFactory )
    throws PlatformPluginRegistrationException {

    // register the content generators
    for ( final IContentGeneratorInfo cgInfo : plugin.getContentGenerators() ) {
      // define the bean in the factory
      BeanDefinition beanDef =
        BeanDefinitionBuilder.rootBeanDefinition( cgInfo.getClassname() ).setScope( BeanDefinition.SCOPE_PROTOTYPE )
          .getBeanDefinition();

      // register bean with alias of content generator id (old way)
      beanFactory.registerBeanDefinition( cgInfo.getId(), beanDef );
      // register bean with alias of type (with default perspective) as well (new way)
      beanFactory.registerAlias( cgInfo.getId(), cgInfo.getType() );

      PluginMessageLogger.add( Messages.getInstance().getString(
        "PluginManager.USER_CONTENT_GENERATOR_REGISTERED", cgInfo.getId(), plugin.getId() ) );

      final HashMap<String, Object> attributes = new HashMap<String, Object>();
      attributes.put( PLUGIN_ID, plugin.getId() );
      attributes.put( CONTENT_TYPE, cgInfo.getType() );
      final IPentahoObjectRegistration referenceHandle = PentahoSystem.registerReference(
        new PrototypePentahoObjectReference.Builder<IContentGenerator>( IContentGenerator.class )
          .creator( new IObjectCreator<IContentGenerator>() {
            @Override
            public IContentGenerator create( IPentahoSession session ) {
              return (IContentGenerator) beanFactory.getBean( cgInfo.getId() );
            }
          } ).attributes( attributes ).build(), IContentGenerator.class
      );

      registerReference( plugin.getId(), referenceHandle );
    }

    // The remaining operations require a beanFactory
    if ( beanFactory == null ) {
      return;
    }

    String[] names =
      BeanFactoryUtils.beanNamesForTypeIncludingAncestors( beanFactory.getBeanFactory(), IContentGenerator.class );
    ArrayList<String> ids = new ArrayList<String>();

    for ( String beanName : names ) {
      ids.add( beanName );
      Collections.addAll( ids, beanFactory.getAliases( beanName ) );
    }


    for ( final String beanName : ids ) {
      final HashMap<String, Object> attributes = new HashMap<String, Object>();
      attributes.put( PLUGIN_ID, plugin.getId() );
      attributes.put( CONTENT_TYPE, beanName );

      final IPentahoObjectRegistration referenceHandle = PentahoSystem.registerReference(
        new PrototypePentahoObjectReference.Builder<IContentGenerator>( IContentGenerator.class )
          .creator( new IObjectCreator<IContentGenerator>() {
            @Override
            public IContentGenerator create( IPentahoSession session ) {
              return (IContentGenerator) beanFactory.getBean( beanName );
            }
          } ).attributes( attributes ).build(),
        IContentGenerator.class
      );

      registerReference( plugin.getId(), referenceHandle );
    }

  }

  protected void registerContentTypes( IPlatformPlugin plugin, ClassLoader loader,
                                       GenericApplicationContext beanFactory )
    throws PlatformPluginRegistrationException {
    // index content types and define any file meta providersIContentGeneratorInfo
    for ( IContentInfo info : plugin.getContentInfos() ) {
      final HashMap<String, Object> attributes = new HashMap<String, Object>();
      attributes.put( PLUGIN_ID, plugin.getId() );
      attributes.put( "extension", info.getExtension() );

      IPentahoObjectRegistration handle = PentahoSystem.registerReference(
        new SingletonPentahoObjectReference.Builder<IContentInfo>( IContentInfo.class ).object( info )
          .attributes( attributes ).build(),
        IContentInfo.class
      );

      registerReference( plugin.getId(), handle );
    }
  }

  private GenericApplicationContext createBeanFactory( IPlatformPlugin plugin, ClassLoader classloader )
    throws PlatformPluginRegistrationException {
    if ( !( classloader instanceof PluginClassLoader ) ) {
      throw new PlatformPluginRegistrationException(
        "Can't determine plugin dir to load spring file because classloader is not of type PluginClassLoader.  "
          + "This is since we are probably in a unit test"
      );

    }

    //
    // Get the native factory (the factory that comes preconfigured via either Spring bean files or via JUnit test
    //
    BeanFactory nativeBeanFactory = getNativeBeanFactory( plugin, classloader );

    //
    // Now create the definable factory for accepting old style bean definitions from IPluginProvider
    //

    GenericApplicationContext beanFactory = null;
    if ( nativeBeanFactory != null && nativeBeanFactory instanceof GenericApplicationContext ) {
      beanFactory = (GenericApplicationContext) nativeBeanFactory;
    } else {
      beanFactory = new GenericApplicationContext();
      beanFactory.setClassLoader( classloader );
      beanFactory.getBeanFactory().setBeanClassLoader( classloader );

      if ( nativeBeanFactory != null ) {
        beanFactory.getBeanFactory().setParentBeanFactory( nativeBeanFactory );
      }
    }

    beanFactory.addBeanFactoryPostProcessor( new PentahoBeanScopeValidatorPostProcessor() );

    //
    // Register any beans defined via the pluginProvider
    //

    // we do not have to synchronize on the bean set here because the
    // map that backs the set is never modified after the plugin has
    // been made available to the plugin manager
    for ( PluginBeanDefinition def : plugin.getBeans() ) {
      // register by classname if id is null
      def.setBeanId( ( def.getBeanId() == null ) ? def.getClassname() : def.getBeanId() );
      try {
        assertUnique( beanFactory, plugin.getId(), def.getBeanId() );
      } catch ( PlatformPluginRegistrationException e ) {
        logger.error( MessageFormat
          .format( "Unable to register plugin bean, a bean by the id {0} is already defined in plugin: {1}",
            def.getBeanId(), plugin.getId() ) );
        continue;
      }
      // defining plugin beans the old way through the plugin provider ifc supports only prototype scope
      BeanDefinition beanDef =
        BeanDefinitionBuilder.rootBeanDefinition( def.getClassname() ).setScope( BeanDefinition.SCOPE_PROTOTYPE )
          .getBeanDefinition();
      beanFactory.registerBeanDefinition( def.getBeanId(), beanDef );
    }

    return beanFactory;
  }

  /**
   * A utility method that throws an exception if a bean with the id is already defined for this plugin
   */
  protected void assertUnique( GenericApplicationContext applicationContext, String pluginId, String beanId )
    throws PlatformPluginRegistrationException {
    if ( applicationContext.containsBean( beanId ) ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0018_BEAN_ALREADY_REGISTERED", beanId, pluginId ) );
    }
  }

  protected BeanFactory getNativeBeanFactory( final IPlatformPlugin plugin, final ClassLoader loader ) {
    BeanFactory nativeFactory = null;
    if ( plugin.getBeanFactory() != null ) {
      // then we are probably in a unit test so just use the preconfigured one
      BeanFactory testFactory = plugin.getBeanFactory();
      if ( testFactory instanceof ConfigurableBeanFactory ) {
        ( (ConfigurableBeanFactory) testFactory ).setBeanClassLoader( loader );
      } else {
        logger.warn( Messages.getInstance().getString( "PluginManager.WARN_WRONG_BEAN_FACTORY_TYPE" ) );
      }
      nativeFactory = testFactory;
    } else {
      File f = new File( ( (PluginClassLoader) loader ).getPluginDir(), "plugin.spring.xml" );
      if ( f.exists() ) {
        logger.debug( "Found plugin spring file @ " + f.getAbsolutePath() );

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

  private void registerReference( String id, IPentahoObjectRegistration handle ) {
    this.handleRegistry.get( id ).add( handle );
  }

  private ClassLoader createClassloader( IPlatformPlugin plugin ) throws PlatformPluginRegistrationException {
    String pluginDirPath =
      PentahoSystem.getApplicationContext().getSolutionPath( "system/" + plugin.getSourceDescription() );
    // need to scrub out duplicate file delimeters otherwise we will
    // not be able to locate resources in jars. This classloader ultimately
    // needs to be made less fragile
    pluginDirPath = pluginDirPath.replace( "//", "/" );
    org.pentaho.platform.util.logging.Logger
      .debug( this,
        "plugin dir for " + plugin.getId() + " is [" + pluginDirPath + "]" );
    File pluginDir = new File( pluginDirPath );
    if ( !pluginDir.exists() || !pluginDir.isDirectory() || !pluginDir.canRead() ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0027_PLUGIN_DIR_UNAVAILABLE", pluginDir.getAbsolutePath() ) );
    }
    PluginClassLoader loader = new PluginClassLoader( pluginDir, this.getClass().getClassLoader() );
    if ( plugin.getLoaderType() == IPlatformPlugin.ClassLoaderType.OVERRIDING ) {
      loader.setOverrideLoad( true );
    }
    return loader;
  }

  @Override
  public List<XulOverlay> getOverlays() {
    return PentahoSystem.getAll( XulOverlay.class );
  }

  @Override
  public Object getBean( String beanId ) throws PluginBeanException {
    if ( beanId == null ) {
      throw new IllegalArgumentException( "beanId cannot be null" );
    }

    for ( GenericApplicationContext beanFactory : PentahoSystem.getAll( GenericApplicationContext.class ) ) {
      if ( beanFactory.containsBean( beanId ) ) {
        try {
          return beanFactory.getBean( beanId );
        } catch ( Throwable ex ) { // Catching throwable on purpose
          throw new PluginBeanException( ex );
        }
      }
    }

    throw new PluginBeanException( Messages.getInstance().getString(
      "PluginManager.WARN_CLASS_NOT_REGISTERED", beanId ) );

  }

  @Override
  public IContentGenerator getContentGenerator( String type, String perspectiveName ) {
    IContentGenerator cg = null;
    String beanId;
    if ( perspectiveName == null || perspectiveName.equals( DEFAULT_PERSPECTIVE ) ) {
      beanId = type;
    } else {
      beanId = type + "." + perspectiveName;
    }

    // first we check: is there any IContentGeneratorInvoker implementation on the bean registry? If so: use it;
    final IContentGeneratorInvoker cgInvoker = PentahoSystem.get( IContentGeneratorInvoker.class );
    if ( cgInvoker != null && cgInvoker.isSupportedContent( beanId ) ) {
      logger.info(
        "Located IContentGeneratorInvoker that supports content of type '" + beanId + "':" + cgInvoker.getClass()
          .getName() );
      return cgInvoker.getContentGenerator();
    }

    // otherwise, gracefully fallback to PentahoSystemPluginManager's internal IContentGenerator discovery logic;

    IContentGenerator contentGenerator = PentahoSystem
      .get( IContentGenerator.class, PentahoSessionHolder.getSession(),
        Collections.singletonMap( CONTENT_TYPE, beanId ) );
    if ( contentGenerator == null ) {
      contentGenerator = PentahoSystem
        .get( IContentGenerator.class, PentahoSessionHolder.getSession(),
          Collections.singletonMap( CONTENT_TYPE, perspectiveName ) );
    }

    return contentGenerator;
  }

  @Override
  public Class<?> loadClass( String beanId ) throws PluginBeanException {
    if ( beanId == null ) {
      throw new IllegalArgumentException( "beanId cannot be null" );
    }
    Class<?> type = null;
    for ( IPentahoObjectReference<GenericApplicationContext> reference : PentahoSystem.getObjectReferences(
      GenericApplicationContext.class, null ) ) {
      if ( !reference.getAttributes().containsKey( PLUGIN_ID ) ) {
        // This GenericApplicationContext was not registered from the plugin manager as it lacks plugin-id
        continue;
      }
      final GenericApplicationContext beanFactory = reference.getObject();
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
        "PluginManager.WARN_CLASS_NOT_REGISTERED", beanId ) );
    }
    return type;
  }

  @Override
  public boolean isBeanRegistered( String beanId ) {
    if ( beanId == null ) {
      throw new IllegalArgumentException( "beanId cannot be null" );
    }

    for ( GenericApplicationContext beanFactory : PentahoSystem.getAll( GenericApplicationContext.class ) ) {
      if ( beanFactory.containsBean( beanId ) ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void unloadAllPlugins() {
    unloadPlugins();
  }

  @Override
  public String getPluginIdForType( String contentType ) {

    final IPentahoObjectReference<IContentGenerator> objectReference = PentahoSystem
      .getObjectReference( IContentGenerator.class, PentahoSessionHolder.getSession(),
        Collections.singletonMap( CONTENT_TYPE, contentType ) );
    if ( objectReference != null && objectReference.getAttributes().containsKey( PLUGIN_ID ) ) {
      return objectReference.getAttributes().get( PLUGIN_ID ).toString();
    } else {
      // fallback for the case where everything is registered in the new form [contentType].[method]
      final List<IPentahoObjectReference<IContentGenerator>> objectReferences =
        PentahoSystem.getObjectReferences( IContentGenerator.class, PentahoSessionHolder.getSession() );

      for ( IPentahoObjectReference<IContentGenerator> reference : objectReferences ) {
        if ( reference.getAttributes().containsKey( CONTENT_TYPE ) ) {
          final String o = (String) reference.getAttributes().get( CONTENT_TYPE );
          if ( o.contains( "." ) && o.substring( 0, o.lastIndexOf( "." ) ).equals( contentType ) ) {
            return (String) reference.getAttributes().get( PLUGIN_ID );
          }
        }
      }
      return null;
    }
  }

  @Override
  public List<String> getPluginRESTPerspectivesForType( String contentType ) {
    List<String> retList = new ArrayList<String>();
    final List<IPentahoObjectReference<IContentGenerator>> objectReferences = PentahoSystem
      .getObjectReferences( IContentGenerator.class, PentahoSessionHolder.getSession(),
        Collections.singletonMap( CONTENT_TYPE, contentType ) );

    for ( IPentahoObjectReference<IContentGenerator> objectReference : objectReferences ) {
      if ( objectReference.getAttributes().containsKey( "id" ) ) {
        final String id = (String) objectReference.getAttributes().get( "id" );
        if ( id != null && id.contains( "." ) ) {
          retList.add( id.substring( id.lastIndexOf( "." ) + 1 ) );
        }
      }
    }
    return retList;
  }

  @Override
  public List<String> getPluginRESTPerspectivesForId( String id ) {
    List<String> retList = new ArrayList<String>();
    final List<IPentahoObjectReference<IContentGenerator>> objectReferences = PentahoSystem
      .getObjectReferences( IContentGenerator.class, PentahoSessionHolder.getSession(),
        Collections.singletonMap( PLUGIN_ID, id ) );
    for ( IPentahoObjectReference<IContentGenerator> objectReference : objectReferences ) {
      if ( objectReference.getAttributes().containsKey( "id" ) ) {
        final String beanId = (String) objectReference.getAttributes().get( "id" );
        if ( beanId != null && beanId.contains( "." ) ) {
          retList.add( beanId.substring( beanId.lastIndexOf( "." ) + 1 ) );
        }
      }
    }
    return retList;
  }

  @Override
  public String getPluginIdForClassLoader( ClassLoader classLoader ) {
    if ( classLoader == null ) {
      return null;
    }
    final List<IPentahoObjectReference<ClassLoader>> objectReferences =
      PentahoSystem.getObjectReferences( ClassLoader.class, null );
    for ( IPentahoObjectReference<ClassLoader> objectReference : objectReferences ) {
      if ( objectReference.getObject().equals( classLoader ) ) {
        return objectReference.getAttributes().get( PLUGIN_ID ).toString();
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
    String[] requestPathElements = trimLeadingSlash( requestPath ).split( "/" );
    String[] servicePathElements = trimLeadingSlash( servicePath ).split( "/" );

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

  @Override
  public Object getPluginSetting( String pluginId, String key, String defaultValue ) {
    final IConfiguration pluginConfig = systemConfig.getConfiguration( pluginId );
    if ( pluginConfig != null ) {
      try {
        // key can be the plain setting name or "settings/" + key. The old system was flexible in this regard so we need
        // to be as well
        if ( pluginConfig.getProperties().containsKey( key ) ) {
          return pluginConfig.getProperties().getProperty( key );
        }
        if ( key.startsWith( SETTINGS_PREFIX ) ) {
          return defaultValue;
        }

        // try it with settings on the front
        String compositeKey = SETTINGS_PREFIX + key;
        if ( pluginConfig.getProperties().containsKey( compositeKey ) ) {
          return pluginConfig.getProperties().getProperty( compositeKey );
        }

        // fall-down to the default
        return defaultValue;

      } catch ( IOException e ) {
        logger.error( "unable to access plugin settings", e );
      }
    }
    return defaultValue;
  }

  @Deprecated
  public String getServicePlugin( String path ) {
    for ( IPlatformPlugin plugin : PentahoSystem.getAll( IPlatformPlugin.class ) ) {
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

  @Override
  public ClassLoader getClassLoader( String pluginId ) {
    return PentahoSystem.get( ClassLoader.class, null, Collections.singletonMap( PLUGIN_ID, pluginId ) );
  }

  @Override
  public ListableBeanFactory getBeanFactory( String pluginId ) {
    return PentahoSystem.get( ApplicationContext.class, null, Collections.singletonMap( PLUGIN_ID, pluginId ) );
  }

  @Override
  public boolean isStaticResource( String path ) {

    for ( IPlatformPlugin plugin : PentahoSystem.getAll( IPlatformPlugin.class ) ) {
      String pluginId = getStaticResourcePluginId( plugin, path );
      if ( pluginId != null ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isPublic( String pluginId, String path ) {
    IPlatformPlugin plugin = PentahoSystem.get( IPlatformPlugin.class, PentahoSessionHolder.getSession(),
      Collections.singletonMap( PLUGIN_ID, pluginId ) );
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

  @Override
  public InputStream getStaticResource( String path ) {
    for ( IPlatformPlugin plugin : PentahoSystem.getAll( IPlatformPlugin.class ) ) {
      Map<String, String> resourceMap = plugin.getStaticResourceMap();
      for ( String url : resourceMap.keySet() ) {
        if ( isRequested( url, path ) ) {
          IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
          ClassLoader classLoader =
            PentahoSystem.get( ClassLoader.class, null, Collections.singletonMap( PLUGIN_ID, plugin.getId() ) );
          String resourcePath = path.replace( url, resourceMap.get( url ) );
          return resLoader.getResourceAsStream( classLoader, resourcePath );
        }
      }
    }
    return null;
  }

  @Override
  public List<String> getRegisteredPlugins() {
    List<String> retList = new ArrayList<String>();
    final List<IPlatformPlugin> plugins = PentahoSystem.getAll( IPlatformPlugin.class );
    for ( IPlatformPlugin plugin : plugins ) {
      retList.add( plugin.getId() );
    }
    return retList;
  }

  @Override
  public List<String> getExternalResourcesForContext( String context ) {

    List<String> resources = new ArrayList<String>();
    for ( IPlatformPlugin plugin : PentahoSystem.getAll( IPlatformPlugin.class ) ) {
      List<String> pluginRes = plugin.getExternalResourcesForContext( context );
      if ( pluginRes != null ) {
        resources.addAll( pluginRes );
      }
    }
    return resources;
  }

  @Override
  public void addPluginManagerListener( IPluginManagerListener listener ) {
    this.listeners.add( listener );
  }
}
