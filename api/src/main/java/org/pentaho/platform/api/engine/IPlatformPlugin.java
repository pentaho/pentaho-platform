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


package org.pentaho.platform.api.engine;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.api.locale.IResourceBundleProvider;
import org.pentaho.ui.xul.XulOverlay;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This interface represents the contract for the specification of a plugin. A {@link IPluginProvider} is
 * responsible for serving these to requesting clients, such as the {@link IPluginManager}. The presence of an
 * instance of an {@link IPlatformPlugin} does not necessarily mean that the plugin is loaded. An implementations
 * of this interface represents merely a plugin configuration.
 *
 * @author jdixon
 */
public interface IPlatformPlugin extends IPluginLifecycleListener {

  public enum ClassLoaderType {
    DEFAULT, OVERRIDING
  }

  /**
   * Returns the unique ID of this plugin
   *
   * @return the plugin id
   */
  public String getId();

  /**
   * Gets the localized title of this plugin for the current locale
   * or the default value if no translation is found.
   *
   * @return the plugin title
   */
  @Nullable
  default String getTitle() {
    return getTitle( null );
  }

  /**
   * Gets the localized title of this plugin for a specific locale
   * or the default value if no translation is found.
   * If the locale is invalid, then the current locale will be used.
   *
   * @param locale the locale to use
   * @return the plugin title
   */
  @Nullable
  String getTitle( @Nullable Locale locale );

  /**
   * Gets the localized description of this plugin for the current locale
   * or the default value if no translation is found.
   *
   * @return the plugin description
   */
  @Nullable
  default String getDescription() {
    return getDescription( null );
  }

  /**
   * Gets the localized description of this plugin for a specific locale
   * or the default value if no translation is found.
   * If the locale is invalid, then the current locale will be used.
   *
   * @param locale the locale to use
   * @return the plugin description
   */
  @Nullable
  String getDescription( @Nullable Locale locale );

  /**
   * Gets the fully qualified class name used as the resource bundle base name.
   */
  @Nullable
  String getResourceBundleClassName();

  /**
   * Sets the resource bundle provider for this plugin. This is used to
   * provide the resource bundle for this plugin for localization.
   *
   * @param provider the resource bundle provider
   */
  void setResourceBundleProvider( @Nullable IResourceBundleProvider provider );

  /**
   * A short description of where this plugin came from, e.g. "biserver/solutions/pluginA"
   *
   * @return
   */
  public String getSourceDescription();

  /**
   * Returns the list of content generators for this plug-in
   *
   * @return
   */
  public List<IContentGeneratorInfo> getContentGenerators();

  /**
   * Returns a list of overlays for this plug-in
   *
   * @return
   */
  public List<XulOverlay> getOverlays();

  /**
   * Returns a list of content info objects for this plug-in
   *
   * @return
   */
  public List<IContentInfo> getContentInfos();

  /**
   * Returns a list of perspective objects for this plug-in
   *
   * @return plugin perspectives
   */
  public List<IPluginPerspective> getPluginPerspectives();

  /**
   * Returns a list of bean configurations for this plugin-in
   */
  public Collection<PluginBeanDefinition> getBeans();

  /**
   * Returns the Spring application context for this plugin
   */
  public ListableBeanFactory getBeanFactory();

  /**
   * Returns a list of static resource paths for this plugin-in
   */
  public Map<String, String> getStaticResourceMap();

  /**
   * Returns the list of fully qualified name of the lifecycle listener class defined by this plugin. The class must be a
   * {@link IPluginLifecycleListener}.
   *
   * @return lifecycle listener class name
   */
  public List<String> getLifecycleListenerClassnames();

  /**
   * Registers a lifecycle listener with this plugin. This listener will be notified when lifecycle events occur on
   * this plugin.
   *
   * @param listener a lifecycle listener
   */
  public void addLifecycleListener( IPluginLifecycleListener listener );

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.platform.api.engine.IPluginLifecycleListener#init()
   */
  public void init() throws PluginLifecycleException;

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.platform.api.engine.IPluginLifecycleListener#loaded()
   */
  public void loaded() throws PluginLifecycleException;

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.platform.api.engine.IPluginLifecycleListener#unLoaded()
   */
  public void unLoaded() throws PluginLifecycleException;

  /**
   * The storage mechanism for a plugin to know what ISolutionFileMetaProvider class should be used for a
   * particular content type.
   *
   * @return a map of content types (extensions) keys and ISolutionFileMetaProvider (or deprecated
   * IFileInfoGenerator) classnames for values
   */
  public Map<String, String> getMetaProviderMap();

  /**
   * Returns the list of the webservices defined by this plugin.
   *
   * @return the definitions of the webservices for this plugin
   */
  public Collection<PluginServiceDefinition> getServices();

  /**
   * Indicates what kind of classloader should be used to load classes and resources from this plugin. The default
   * classloader type is no more than an extension of {@link java.net.URLClassLoader}.
   *
   * @return the type of classloader to use for this plugin
   * @see IPlatformPlugin.ClassLoaderType
   */
  public ClassLoaderType getLoaderType();

  /**
   * Return a List of scripts registered for a given context.
   *
   * @param context named area in the platform
   * @return list of registered scripts
   */
  List<String> getExternalResourcesForContext( String context );
}
