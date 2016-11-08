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
import org.pentaho.ui.xul.XulOverlay;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  private String lifecycleListenerClassname;

  private IPluginLifecycleListener lifecycleListener;

  private ClassLoaderType loaderType;

  private Map<String, List<String>> externalResources = new HashMap<String, List<String>>();

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

  public String getLifecycleListenerClassname() {
    return lifecycleListenerClassname;
  }

  public void setLifecycleListenerClassname( String lifecycleListenerClassname ) {
    this.lifecycleListenerClassname = lifecycleListenerClassname;
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
    if ( lifecycleListener != null ) {
      lifecycleListener.init();
    }
  }

  public void loaded() throws PluginLifecycleException {
    if ( lifecycleListener != null ) {
      lifecycleListener.loaded();
    }
  }

  public void unLoaded() throws PluginLifecycleException {
    if ( lifecycleListener != null ) {
      lifecycleListener.unLoaded();
    }
  }

  public void addLifecycleListener( IPluginLifecycleListener listener ) {
    this.lifecycleListener = listener;
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
