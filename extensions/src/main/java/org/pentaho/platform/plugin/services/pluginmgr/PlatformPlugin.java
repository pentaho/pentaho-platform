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


package org.pentaho.platform.plugin.services.pluginmgr;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginBeanDefinition;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.api.engine.PluginServiceDefinition;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.api.locale.IResourceBundleProvider;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.ui.xul.XulOverlay;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Default bean implementation of {@link IPlatformPlugin}
 */
public class PlatformPlugin implements IPlatformPlugin, IPentahoInitializer {

  private List<IContentGeneratorInfo> contentGenerators = new ArrayList<IContentGeneratorInfo>();

  private List<IContentInfo> contentInfos = new ArrayList<IContentInfo>();

  private List<XulOverlay> overlays = new ArrayList<XulOverlay>();

  private List<IPluginPerspective> perspectives = new ArrayList<IPluginPerspective>();

  private Collection<PluginBeanDefinition> beanDefinitions = new ArrayList<PluginBeanDefinition>();

  private DefaultListableBeanFactory beanFactory;

  private Collection<PluginServiceDefinition> webserviceDefinitions = new ArrayList<PluginServiceDefinition>();

  private List<IPentahoInitializer> initializers = new ArrayList<IPentahoInitializer>();

  private Map<String, String> staticResourceMap = new HashMap<String, String>();

  private Map<String, String> metaProviderMap = new HashMap<String, String>();

  private String id;

  // this value needs to default to an empty string so the plugin dir will not say "null" in the path if the path is not
  // explicitly set
  private String sourceDescription = ""; //$NON-NLS-1$

  private List<String> lifecycleListenerClassnames = new ArrayList<>();

  private List<IPluginLifecycleListener> lifecycleListeners = new ArrayList<>();

  private ClassLoaderType loaderType;

  private Map<String, List<String>> externalResources = new HashMap<String, List<String>>();

  /**
   * The regular expression pattern used to match resource bundle keys in the format "${key}".
   */
  private static final Pattern RESOURCE_BUNDLE_KEY_PATTERN = Pattern.compile( "\\$\\{([^}]+)\\}" );

  /**
   * A fully qualified class name used as the resource bundle base name.
   * The default value is "messages" and the resource bundle is loaded from the plugin root folder.
   */
  private String resourceBundleClassName = null;

  /**
   * The resource bundle provider for this plugin. Allows loading resource bundles for distinct locales.
   */
  private IResourceBundleProvider resourceBundleProvider = null;

  private String title = null;

  private String description = null;

  public PlatformPlugin() {
  }

  /* for testing only */
  public PlatformPlugin( DefaultListableBeanFactory defaultListableBeanFactory ) {
    beanFactory = defaultListableBeanFactory;
  }

  public void init( IPentahoSession session ) {
    for ( IPentahoInitializer initializer : initializers ) {
      initializer.init( session );
    }
  }

  public void addLifecycleListenerClassname( String lifecycleListenerClassname ) {
    this.lifecycleListenerClassnames.add( lifecycleListenerClassname );
  }

  public void setLifecycleListenerClassname( String lifecycleListenerClassnames ) {
    this.lifecycleListenerClassnames.add( lifecycleListenerClassnames );
  }

  public List<String> getLifecycleListenerClassnames() {
    return lifecycleListenerClassnames;
  }

  public List<IContentGeneratorInfo> getContentGenerators() {
    return contentGenerators;
  }

  public List<IContentInfo> getContentInfos() {
    return contentInfos;
  }

  public String getId() {
    return id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nullable
  public String getTitle( @Nullable Locale locale ) {
    return localizeInterpolatedString( title, locale );
  }

  /**
   * Sets the title of this plugin. It can be a default localized value
   * or contain keys in the format "${resource-id}" for localization.
   *
   * @param title the title
   */
  public void setTitle( @Nullable String title ) {
    this.title = title;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nullable
  public String getDescription( @Nullable Locale locale ) {
    return localizeInterpolatedString( description, locale );
  }

  /**
   * Sets the description of this plugin. It can be a default localized value
   * or contain keys in the format "${key}" for localization.
   *
   * @param description the description
   */
  public void setDescription( @Nullable String description ) {
    this.description = description;
  }

  /**
   * Returns a localized string for the given key and locale.
   * If the key is not found or no resource bundle is found for the specified locale,
   * then `value` is returned.
   *
   * @param value  the value string to be localized
   * @param locale the locale to use for localization
   * @return the localized string or `value` if not found
   */
  @Nullable
  @VisibleForTesting
  protected String localizeInterpolatedString( @Nullable String value, @Nullable Locale locale ) {
    if ( resourceBundleProvider == null || StringUtil.isEmpty( value ) ) {
      return value;
    }

    // extract key pattern "${key}" from value input string
    Set<String> keys = getKeys( value );

    if ( keys.isEmpty() ) {
      return value;
    }

    try {
      ResourceBundle bundle = resourceBundleProvider.getResourceBundle( locale );

      String localizedValue = value;

      // replace all keys with their values
      for ( String key : keys ) {
        String keyValue = bundle.getString( key );

        if ( !StringUtil.isEmpty( keyValue ) ) {
          localizedValue = localizedValue.replace( "${" + key + "}", keyValue );
        }
      }

      return localizedValue;
    } catch ( Exception e ) {
      return value;
    }
  }

  /**
   * Extracts keys from the given value string, which are in the format "${key}".
   *
   * @param value the value string to extract keys from
   * @return a set of keys found in the value string
   */
  @NonNull
  private Set<String> getKeys( @NonNull String value ) {
    return RESOURCE_BUNDLE_KEY_PATTERN.matcher( value ).results()
      .map( match -> match.group( 1 ) )
      .collect( Collectors.toSet() );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nullable
  public String getResourceBundleClassName() {
    return resourceBundleClassName;
  }

  /**
   * Sets the fully qualified class name used as the resource bundle base name.
   *
   * @param className the fully qualified class name
   */
  public void setResourceBundleClassName( @Nullable String className ) {
    this.resourceBundleClassName = className;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setResourceBundleProvider( @Nullable IResourceBundleProvider provider ) {
    this.resourceBundleProvider = provider;
  }

  public List<XulOverlay> getOverlays() {
    return overlays;
  }

  public List<IPluginPerspective> getPluginPerspectives() {
    return Collections.unmodifiableList( perspectives );
  }

  /**
   * Sets the unique id for this plug-in
   *
   * @param id
   */
  public void setId( String id ) {
    this.id = id;
  }

  /**
   * Adds an initializer to this plug-in
   *
   * @param initializer
   */
  public void addInitializer( IPentahoInitializer initializer ) {
    initializers.add( initializer );
  }

  /**
   * Adds a content generator to this plug-in
   *
   * @param contentGenerator
   */
  public void addContentGenerator( IContentGeneratorInfo contentGenerator ) {
    contentGenerators.add( contentGenerator );
  }

  /**
   * Adds a content info type to this plug-in
   *
   * @param contentInfo
   */
  public void addContentInfo( IContentInfo contentInfo ) {
    contentInfos.add( contentInfo );
  }

  /**
   * Adds an overlay to this plug-in
   *
   * @param overlay
   */
  public void addOverlay( XulOverlay overlay ) {
    overlays.add( overlay );
  }

  public String getSourceDescription() {
    return sourceDescription;
  }

  public void setSourceDescription( String sourceDescription ) {
    this.sourceDescription = sourceDescription;
  }

  public void addStaticResourcePath( String url, String localFolder ) {
    staticResourceMap.put( url, localFolder );
  }

  public Map<String, String> getStaticResourceMap() {
    return staticResourceMap;
  }

  public Collection<PluginBeanDefinition> getBeans() {
    return Collections.unmodifiableCollection( beanDefinitions );
  }

  public DefaultListableBeanFactory getBeanFactory() {
    return beanFactory;
  }

  public Collection<PluginServiceDefinition> getServices() {
    return Collections.unmodifiableCollection( webserviceDefinitions );
  }

  public void addBean( PluginBeanDefinition beanDefinition ) {
    beanDefinitions.add( beanDefinition );
  }

  public void addWebservice( PluginServiceDefinition serviceDefinition ) {
    webserviceDefinitions.add( serviceDefinition );
  }

  public void init() throws PluginLifecycleException {
    if ( lifecycleListeners != null && !lifecycleListeners.isEmpty() ) {
      for ( IPluginLifecycleListener lifecycleListener : lifecycleListeners ) {
        lifecycleListener.init();
      }
    }
  }

  public void loaded() throws PluginLifecycleException {
    if ( lifecycleListeners != null && !lifecycleListeners.isEmpty() ) {
      for ( IPluginLifecycleListener lifecycleListener : lifecycleListeners ) {
        lifecycleListener.loaded();
      }
    }
  }

  public void unLoaded() throws PluginLifecycleException {
    if ( lifecycleListeners != null && !lifecycleListeners.isEmpty() ) {
      for ( IPluginLifecycleListener lifecycleListener : lifecycleListeners ) {
        lifecycleListener.unLoaded();
      }
    }
  }

  public void addLifecycleListener( IPluginLifecycleListener listener ) {
    this.lifecycleListeners.add( listener );
  }

  public Map<String, String> getMetaProviderMap() {
    return metaProviderMap;
  }

  public ClassLoaderType getLoaderType() {
    return loaderType;
  }

  public void setLoadertype( ClassLoaderType loaderType ) {
    this.loaderType = loaderType;
  }

  public void addExternalResource( String context, String resource ) {
    List<String> res = externalResources.get( context );
    if ( res == null ) {
      res = new ArrayList<String>();
      externalResources.put( context, res );
    }
    res.add( resource );
  }

  public List<String> getExternalResourcesForContext( String context ) {
    List<String> res = externalResources.get( context );
    if ( res == null ) {
      res = new ArrayList<String>();
      externalResources.put( context, res );
    }
    return Collections.unmodifiableList( res );
  }

  public void addPluginPerspective( IPluginPerspective perspective ) {
    perspectives.add( perspective );
  }
}
