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

package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.ui.xul.XulOverlay;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.Collection;
import java.util.List;
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
   * Returns the fully qualified name of the lifecycle listener class defined by this plugin. The class must be a
   * {@link IPluginLifecycleListener}.
   * 
   * @return lifecycle listener class name
   */
  public String getLifecycleListenerClassname();

  /**
   * Registers a lifecycle listener with this plugin. This listener will be notified when lifecycle events occur on
   * this plugin.
   * 
   * @param listener
   *          a lifecycle listener
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
   *         IFileInfoGenerator) classnames for values
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
   * @see IPlatformPlugin.ClassLoaderType
   * @return the type of classloader to use for this plugin
   */
  public ClassLoaderType getLoaderType();

  /**
   * Return a List of scripts registered for a given context.
   * 
   * @param context
   *          named area in the platform
   * @return list of registered scripts
   */
  List<String> getExternalResourcesForContext( String context );
}
