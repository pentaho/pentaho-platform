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

import org.pentaho.ui.xul.XulOverlay;
import org.springframework.beans.factory.ListableBeanFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * The contract API between the platform and BIServer plugins. The plugin manager provides the ability to load and
 * register plugins as well as utility methods for getting particular extension implementations from the set of
 * registered plugins. It's object creation and management features are backed by a {@link ListableBeanFactory} For more
 * information on platform plugins, visit the wiki link below.
 *
 * @author jamesdixon
 * @author aphillips
 * @see <a href="http://wiki.pentaho.com/display/ServerDoc2x/BI+Platform+Plugins+in+V2">BI Platform Plugins</a>
 */
public interface IPluginManager {

  /**
   * Returns a set of the content types that the registered plugins can process. If the plugin is intended to handle the
   * processing of a document in the solution repository the types need to match with the filename extension.
   *
   * @return list of all the content types provided by plugins
   */
  public Set<String> getContentTypes();

  /**
   * Gets metadata about a particular content type
   *
   * @param type a content type provided by a plugin
   * @return info object for a content type
   */
  public IContentInfo getContentTypeInfo( String type );

  /**
   * @deprecated Use {@link #getContentGenerator(String, String) instead
   */
  @Deprecated
  public IContentGenerator getContentGeneratorForType( String type, IPentahoSession session )
    throws ObjectFactoryException;

  /**
   * Causes the plug-in manager object to re-register all of the plug-ins that it knows about. A {@link IPluginProvider}
   * may be invoked to discover plugins from various sources.
   *
   * @return true if no errors were encountered
   */
  public boolean reload();

  /**
   * @deprecated Use {@link #reload()} instead
   */
  @Deprecated
  public boolean reload( IPentahoSession session );

  /**
   * Returns a list of the XUL overlays that are defined by all the plug-ins. The overlays are XML fragments.
   *
   * @return List of XML XUL overlays
   */
  public List<XulOverlay> getOverlays();

  /**
   * If any plugins have registered a bean by id beanId, this method will return a new instance of the object. The
   * correct classloader must be used to instantiate the object.
   *
   * @param beanId a unique identifier for a particular bean (cannot be null)
   * @return an instance of the bean registered under beanId may generate an unchecked PluginBeanException if there was
   * a problem retrieving the bean instance
   */
  public Object getBean( String beanId ) throws PluginBeanException;

  /**
   * Returns a {@link IContentGenerator} that can render the named perspective (e.g. 'viewer') of a resource of type
   * <code>type</code> (e.g. 'prpt'). Essentially the same as <code> (IContentGenerator)getBean
   * (type+"/"+perspectiveName)</code>
   *
   * @param type            the type of resource to render
   * @param perspectiveName specifies a distinct view of the resource. Can be <code>null</code> in which case the
   *                        default perspective 'generatedContent' is implied.
   * @return a list of suitable {@link IContentGenerator}s may generate an unchecked NoSuchBeanDefinitionException - if
   * there is no bean definition with the specified name may generate an unchecked BeansException - if the bean could
   * not be obtained
   */
  public IContentGenerator getContentGenerator( String type, String perspectiveName );

  /**
   * Returns a loaded class for the bean registered as beanId. The class will have been loaded by the proper
   * classloader, so it indirectly provides the caller with it's classloader by class.getClassLoader(). This is often
   * helpful since plugin bean classes are often not available through the caller's classloader.
   *
   * @param beanId a unique identifier for a particular bean (cannot be null)
   * @return a loaded class for the registered bean
   * @throws PluginBeanException if there was a problem loading the class
   */
  public Class<?> loadClass( String beanId ) throws PluginBeanException;

  /**
   * Returns true if a bean with id beanId has been registered with the plugin manager, i.e. you can get a bean instance
   * by calling {@link #getBean(String)}
   *
   * @param beanId Cannot be null
   * @return true if the bean is registered
   */
  public boolean isBeanRegistered( String beanId );

  /**
   * Unloads all the plugins. Called when the context shuts down.
   */
  public void unloadAllPlugins();

  /**
   * Returns the plugin (IPlatformPlugin) that handles requests for files of type <code>contentType</code>
   *
   * @param contentType a user-defined file-extension like suffix denoting a type of content
   * @return the id of the plugin that handles requests to files of type <code>contentType</code> or <code>null</code>
   * if none were found
   */
  public String getPluginIdForType( String contentType );

  /**
   * Returns the Plugin perspectives for files of type <code>contentType</code>
   *
   * @param contentType a user-defined file-extension like suffix denoting a type of content. for example xaction, prpt
   * @return the list of REST perspectives for files of type <code>contentType</code> or <code>null</code> if none were
   * found
   */
  public List<String> getPluginRESTPerspectivesForType( String contentType );

  /**
   * Returns the Plugin perspectives for a plugin of specific id
   *
   * @param the id of the plugin
   * @return the list of REST perspectives for a plugin of specific id or <code>null</code> if none were found
   */
  public List<String> getPluginRESTPerspectivesForId( String id );

  /**
   * Each plugin is loaded on its own classloader, we can use this to find the plugin for which the classloader is
   * serving
   *
   * @param classLoader The classloader for the plugin, also used to load all plugin resources
   * @return the plugin id which was loaded on the given classloader
   */
  public String getPluginIdForClassLoader( ClassLoader classLoader );

  /**
   * Retrieves a plugin setting for a given plugin and key.
   *
   * @param pluginId     the ID of the plugin to find settings for
   * @param key          the setting name to lookup
   * @param defaultValue the default to use if the setting key is not found
   * @return the plugin setting
   */
  public Object getPluginSetting( String pluginId, String key, String defaultValue );

  /**
   * Returns the plugin that can handle a request for the resource at "path". A plugin is determined to be able to serve
   * the request if it either a content generator or a static resource of the plugin is configured to handle the path.
   * In other words, if a plugin has a static resource of "/my-plugin/resources", then a request to
   * "/my-plugin/resources/images/file.png" can be handled by the plugin. If ultimately, no plugin can handle the
   * resource path, <code>null</code> is returned.
   *
   * @param path the path to the plugin resource
   * @return the ID of the plugin which owns the resource or <code>null</code> if one cannot be found
   * @deprecated This method may not work correctly for plugins in Sugar or later releases, in which a plugin does not
   * provide the URL by which to access the resource. Use {@link #isPublic(String, String)} if you want to determine if
   * a particular plugin dir is accessable through the plugin resources REST service.
   */
  @Deprecated
  public String getServicePlugin( String path );

  /**
   * Returns the classloader instance that was assigned by the plugin manager to load all classes for the specified
   * plugin. Used in combination with {@link #getServicePlugin(String)}, this method can provide you with a way to load
   * resources from a plugin when all you have is a request URL/path, such as in a servlet environment.
   *
   * @param pluginId the plugin for which we want to get the assigned classloader
   * @return the classloader assigned to this plugin, or <code>null</code> if the plugin is not known by the plugin
   * manager, or for some reason a classloader was not assigned to the plugin (an error condition).
   */
  public ClassLoader getClassLoader( String pluginId );

  /**
   * Returns a (Spring) bean factory for retrieving plugin-bound objects
   *
   * @param pluginId
   * @return a bean factory for retrieving instances of plugin-provided objects
   */
  public ListableBeanFactory getBeanFactory( String pluginId );

  /**
   * returns true if the path is a reference to a potential static resource. Note that this does not guarantee that the
   * resource exists, just that it maps to a static resource location.
   *
   * @param path static resource path
   * @return true if path begins as a static resource
   * @deprecated This method may not work correctly for plugins in Sugar or later releases, in which a plugin does not
   * provide the URL by which to access the resource. Use {@link #isPublic(String, String)} if you want to determine if
   * a particular plugin dir is accessable through the plugin resources REST service.
   */
  @Deprecated
  public boolean isStaticResource( String path );

  /**
   * Returns true if the resource specified by <code>path</code> is publicly available, meaning this resource can be
   * accessed by HTTP GET requests
   *
   * @param pluginId the id of the plugin to search for the dir
   * @param path     a path relative to the plugin folder to check for public availability
   * @return <code>true</code> if publicly available or <code>false</code> if not available for any reason
   */
  public boolean isPublic( String pluginId, String path );

  /**
   * Returns and InputStream to the specified resource path.
   *
   * @param path the path to the plugin resource
   * @return the InputStream which may be used to read the plugin resource
   * @deprecated This method may not work correctly for plugins in Sugar or later releases, in which a plugin does not
   * provide the URL by which to access the resource. Use {@link #isPublic(String, String)} if you want to determine if
   * a particular plugin dir is accessable through the plugin resources REST service.
   */
  @Deprecated
  public InputStream getStaticResource( String path );

  /**
   * Lists the ids of available plugins. From the id, you can get lots of information about a plugin, such as the bean
   * factory which gives you access to information on all specified beans for that plugin.
   *
   * @return list of plugin ids
   */
  public List<String> getRegisteredPlugins();

  /**
   * Return a List of scripts registered for a given context.
   *
   * @param context named area in the platform
   * @return list of registered scripts
   */
  List<String> getExternalResourcesForContext( String context );

  /**
   * Add a {@link IPluginManagerListener} to be notified of IPluginManager events.
   *
   * @param listener
   */
  void addPluginManagerListener( IPluginManagerListener listener );
}
