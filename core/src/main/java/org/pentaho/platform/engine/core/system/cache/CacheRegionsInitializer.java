/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License, version 2 as published by the Free Software Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, you can obtain
 * a copy at http://www.gnu.org/licenses/gpl-2.0.html or from the Free Software Foundation, Inc.,  51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2006 - 2018 Hitachi Vantara.  All rights reserved.
 *
 */

package org.pentaho.platform.engine.core.system.cache;

import org.pentaho.platform.api.cache.CacheRegionRequired;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginManagerListener;
import org.pentaho.platform.util.StringUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by Dmitriy Stepanov on 02.02.18.
 */
@Component
public class CacheRegionsInitializer implements IPentahoSystemListener, IPluginManagerListener, BeanPostProcessor {

  public static final String CACHE_REGION_NAME = "CacheRegionName";

  @Autowired
  private ICacheManager cacheManager;
  @Autowired
  private IPluginManager pluginManager;

  private Set<String> systemRegions = new HashSet<>();
  private Set<String> pluginRegions = new HashSet<>();
  private Set<String> sessionRegions = new HashSet<>();

  public CacheRegionsInitializer() {
  }

  public CacheRegionsInitializer( ICacheManager cacheManager, IPluginManager pluginManager ) {
    this.cacheManager = cacheManager;
    this.pluginManager = pluginManager;
    this.pluginManager.addPluginManagerListener( this );
  }

  public CacheRegionsInitializer( ICacheManager cacheManager, IPluginManager pluginManager, List<String> systemRegions,
                                  List<String> pluginRegions ) {
    this( cacheManager, pluginManager );
    this.systemRegions.addAll( systemRegions );
    this.pluginRegions.addAll( pluginRegions );
  }

  @Override public boolean startup( IPentahoSession session ) {
    if ( cacheManager != null ) {
      systemRegions.forEach( this::addCacheRegion );
      pluginRegions.forEach( this::addCacheRegion );
    }
    return true;
  }

  private void addCacheRegion( String region ) {
    if ( cacheManager != null && !cacheManager.cacheEnabled( region ) ) {
      cacheManager.addCacheRegion( region );
    }
  }

  @Override public void shutdown() {
    if ( cacheManager != null ) {
      systemRegions.forEach( this::removeRegionCache );
      pluginRegions.forEach( this::removeRegionCache );
    }

  }

  private void removeRegionCache( String region ) {
    if ( cacheManager.cacheEnabled( region ) ) {
      cacheManager.removeRegionCache( region );
    }
  }

  @Override public void onReload() {
    for ( String pluginId : pluginManager.getRegisteredPlugins() ) {
      String regionName = (String) pluginManager.getPluginSetting( pluginId,
        CACHE_REGION_NAME, "" );
      if ( !StringUtil.isEmpty( regionName ) ) {
        pluginRegions.add( regionName );
      }
    }
    if ( cacheManager != null ) {
      pluginRegions.forEach( this::removeRegionCache );
      pluginRegions.forEach( this::addCacheRegion );
    }

  }

  @Override public Object postProcessBeforeInitialization( Object bean, String beanName ) throws BeansException {
    CacheRegionRequired[] annotations;
    if ( ( annotations = bean.getClass().getAnnotationsByType( CacheRegionRequired.class ) ).length != 0 ) {
      for ( CacheRegionRequired annotation : annotations ) {
        processAnnotation( annotation );
      }
    }
    return bean;
  }

  private void processAnnotation( CacheRegionRequired annotation ) {
    switch ( annotation.phase() ) {
      case SYSTEM: {
        systemRegions.add( annotation.region() );
        break;
      }
      case PLUGIN: {
        pluginRegions.add( annotation.region() );
        break;
      }
      case SESSION: {
        String region = annotation.region();
        sessionRegions.add( region );
        addCacheRegion( region );
        break;
      }
    }
  }

  @Override public Object postProcessAfterInitialization( Object bean, String beanName ) throws BeansException {
    if ( ICacheManager.class.isAssignableFrom( bean.getClass() ) ) {
      sessionRegions.forEach( this::addCacheRegion );
    }
    return bean;
  }
}
