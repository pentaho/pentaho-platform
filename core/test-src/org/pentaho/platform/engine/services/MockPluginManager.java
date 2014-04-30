/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockPluginManager implements IPluginManager {

  public static final Map<String, IContentGenerator> contentGeneratorByType = new HashMap<String, IContentGenerator>();

  public static final Map<String, IContentInfo> contentInfoByType = new HashMap<String, IContentInfo>();

  public Object getBean( String arg0 ) throws PluginBeanException {
    // TODO Auto-generated method stub
    return null;
  }

  public ClassLoader getClassLoader( IPlatformPlugin arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public ClassLoader getClassLoader( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentGenerator getContentGenerator( String arg0, IPentahoSession arg1 ) throws ObjectFactoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentGenerator getContentGeneratorForType( String arg0, IPentahoSession arg1 )
    throws ObjectFactoryException {
    return contentGeneratorByType.get( arg0 );
  }

  public String getContentGeneratorIdForType( String arg0, IPentahoSession arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentGeneratorInfo getContentGeneratorInfo( String arg0, IPentahoSession arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<IContentGeneratorInfo> getContentGeneratorInfoForType( String arg0, IPentahoSession arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getContentGeneratorTitleForType( String arg0, IPentahoSession arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getContentGeneratorUrlForType( String arg0, IPentahoSession arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentInfo getContentInfoFromExtension( String arg0, IPentahoSession arg1 ) {
    return contentInfoByType.get( arg0 );
  }

  public Set<String> getContentTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentGeneratorInfo getDefaultContentGeneratorInfoForType( String arg0, IPentahoSession arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public IFileInfo getFileInfo( String arg0, IPentahoSession arg1, ISolutionFile arg2, InputStream arg3 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getPluginSetting( IPlatformPlugin arg0, String arg1, String arg2 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getPluginSetting( String arg0, String arg1, String arg2 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getServicePlugin( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public InputStream getStaticResource( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isBeanRegistered( String arg0 ) {
    // TODO Auto-generated method stub
    return false;
  }

  public IPlatformPlugin isResourceLoadable( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isStaticResource( String arg0 ) {
    // TODO Auto-generated method stub
    return false;
  }

  public Class<?> loadClass( String arg0 ) throws PluginBeanException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean reload( IPentahoSession arg0 ) {
    // TODO Auto-generated method stub
    return false;
  }

  public void unloadAllPlugins() {
    // TODO Auto-generated method stub

  }

  public String getPluginIdForClassLoader( ClassLoader classLoader ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getPluginIdForContentGeneratorInfo( IContentGeneratorInfo contentGeneratorInfo ) {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getApplicationContext( String pluginId ) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<XulOverlay> getOverlays() {
    // TODO Auto-generated method stub
    return null;
  }

  public ListableBeanFactory getBeanFactory( String pluginId ) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getRegisteredPlugins() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getExternalResourcesForContext( String context ) {
    return Collections.emptyList();
  }

  @Override
  public IContentGenerator getContentGenerator( String arg0, String arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IContentInfo getContentTypeInfo( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPluginIdForType( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isPublic( String arg0, String arg1 ) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean reload() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public List<String> getPluginRESTPerspectivesForId( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getPluginRESTPerspectivesForType( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override public void addPluginManagerListener( IPluginManagerListener listener ) {

  }
}
