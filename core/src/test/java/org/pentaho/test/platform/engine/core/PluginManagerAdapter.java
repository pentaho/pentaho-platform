/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.engine.core;

import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginManagerListener;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.ui.xul.XulOverlay;
import org.springframework.beans.factory.ListableBeanFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 3 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2009 - 2019 Hitachi Vantara.  All rights reserved.
 * 
 */
public class PluginManagerAdapter implements IPluginManager {

  public Object getBean( String beanId ) throws PluginBeanException {
    // Auto-generated method stub
    return null;
  }

  public ClassLoader getClassLoader( IPlatformPlugin plugin ) {
    // Auto-generated method stub
    return null;
  }

  public ClassLoader getClassLoader( String pluginId ) {
    // Auto-generated method stub
    return null;
  }

  public IContentGenerator getContentGenerator( String id, IPentahoSession session ) throws ObjectFactoryException {
    // Auto-generated method stub
    return null;
  }

  public IContentGenerator getContentGeneratorForType( String type, IPentahoSession session )
    throws ObjectFactoryException {
    // Auto-generated method stub
    return null;
  }

  public String getContentGeneratorIdForType( String type, IPentahoSession session ) {
    // Auto-generated method stub
    return null;
  }

  public IContentGeneratorInfo getContentGeneratorInfo( String id, IPentahoSession session ) {
    // Auto-generated method stub
    return null;
  }

  public List<IContentGeneratorInfo> getContentGeneratorInfoForType( String type, IPentahoSession session ) {
    // Auto-generated method stub
    return null;
  }

  public String getContentGeneratorTitleForType( String type, IPentahoSession session ) {
    // Auto-generated method stub
    return null;
  }

  public String getContentGeneratorUrlForType( String type, IPentahoSession session ) {
    // Auto-generated method stub
    return null;
  }

  public IContentInfo getContentInfoFromExtension( String extension, IPentahoSession session ) {
    // Auto-generated method stub
    return null;
  }

  public Set<String> getContentTypes() {
    // Auto-generated method stub
    return null;
  }

  public IContentGeneratorInfo getDefaultContentGeneratorInfoForType( String type, IPentahoSession session ) {
    // Auto-generated method stub
    return null;
  }

  public IFileInfo getFileInfo( String extension, IPentahoSession session, ISolutionFile file, InputStream in ) {
    // Auto-generated method stub
    return null;
  }

  public Object getPluginSetting( IPlatformPlugin plugin, String key, String defaultValue ) {
    // Auto-generated method stub
    return null;
  }

  public Object getPluginSetting( String pluginId, String key, String defaultValue ) {
    // Auto-generated method stub
    return null;
  }

  public String getServicePlugin( String path ) {
    // Auto-generated method stub
    return null;
  }

  public InputStream getStaticResource( String path ) {
    // Auto-generated method stub
    return null;
  }

  public boolean isBeanRegistered( String beanId ) {
    // Auto-generated method stub
    return false;
  }

  public IPlatformPlugin isResourceLoadable( String path ) {
    // Auto-generated method stub
    return null;
  }

  public boolean isStaticResource( String path ) {
    // Auto-generated method stub
    return false;
  }

  public Class<?> loadClass( String beanId ) throws PluginBeanException {
    // Auto-generated method stub
    return null;
  }

  public boolean reload( IPentahoSession session ) {
    // Auto-generated method stub
    return false;
  }

  public void unloadAllPlugins() {
    // Auto-generated method stub

  }

  public List<XulOverlay> getOverlays() {
    // Auto-generated method stub
    return null;
  }

  public String getPluginIdForClassLoader( ClassLoader classLoader ) {
    // Auto-generated method stub
    return null;
  }

  public String getPluginIdForContentGeneratorInfo( IContentGeneratorInfo contentGeneratorInfo ) {
    // Auto-generated method stub
    return null;
  }

  public Object getApplicationContext( String pluginId ) {
    // Auto-generated method stub
    return null;
  }

  public ListableBeanFactory getBeanFactory( String pluginId ) {
    // Auto-generated method stub
    return null;
  }

  public List<String> getRegisteredPlugins() {
    // Auto-generated method stub
    return null;
  }

  public List<String> getExternalResourcesForContext( String context ) {
    return Collections.emptyList();
  }

  @Override
  public String getPluginIdForType( String contentType ) {
    // Auto-generated method stub
    return null;
  }

  @Override
  public boolean isPublic( String pluginId, String path ) {
    // Auto-generated method stub
    return false;
  }

  @Override
  public IContentGenerator getContentGenerator( String type, String perspectiveName ) {
    // Auto-generated method stub
    return null;
  }

  @Override
  public boolean reload() {
    // Auto-generated method stub
    return false;
  }

  @Override
  public IContentInfo getContentTypeInfo( String type ) {
    // Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getPluginRESTPerspectivesForId( String arg0 ) {
    // Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getPluginRESTPerspectivesForType( String arg0 ) {
    // Auto-generated method stub
    return null;
  }

  @Override public void addPluginManagerListener( IPluginManagerListener listener ) {

  }
}
