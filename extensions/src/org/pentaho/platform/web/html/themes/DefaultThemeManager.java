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

package org.pentaho.platform.web.html.themes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.ui.IThemeManager;
import org.pentaho.platform.api.ui.IThemeResolver;
import org.pentaho.platform.api.ui.ModuleThemeInfo;
import org.pentaho.platform.api.ui.Theme;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default IThemeResolver implementation. DefaultThemeManager collects information about available system and local
 * themes.
 * 
 * User: nbaker Date: 5/15/11
 */
public class DefaultThemeManager implements IThemeManager {

  private static final String THEME_CACHE_REGION = "Themes";
  private static final String SYSTEM_THEMES = "System_Themes";
  private static final String MODULE_THEMES = "Local_Themes";

  private static ICacheManager cache = PentahoSystem.getCacheManager( null );
  private static final Log logger = LogFactory.getLog( DefaultThemeManager.class );

  static {
    cache.addCacheRegion( THEME_CACHE_REGION );
  }

  private List<IThemeResolver> resolvers = new ArrayList<IThemeResolver>();
  {
    resolvers.add( new PluginThemeResolver() );
    resolvers.add( new ServletContextThemeResolver() );
  }

  public List<String> getSystemThemeIds() {
    List<String> allThemes = new ArrayList<String>();

    boolean themesFoundInCache = false;
    Set<String> regionKeys = cache.getAllKeysFromRegionCache( THEME_CACHE_REGION );
    if ( regionKeys != null && !regionKeys.isEmpty() ) {
      for ( String key : regionKeys ) {
        if ( key.startsWith( SYSTEM_THEMES ) ) {
          themesFoundInCache = true;
          allThemes.add( key.substring( SYSTEM_THEMES.length() + 1 ) );
        }
      }
    }
    if ( !themesFoundInCache ) {
      Map<String, Theme> themes = collectAllSystemThemes();
      for ( String name : themes.keySet() ) {
        allThemes.add( name );
      }
    }
    return allThemes;
  }

  public Theme getSystemTheme( String themeId ) {
    Theme theme = (Theme) cache.getFromRegionCache( THEME_CACHE_REGION, SYSTEM_THEMES + "-" + themeId );
    if ( theme == null ) { // may have been flushed, try to fetch it
      theme = collectAllSystemThemes().get( themeId );
      if ( theme == null ) {
        logger.error( "Unable to find requested system theme: " + themeId );
      }
    }
    return theme;
  }

  public Theme getModuleTheme( String moduleName, String themeId ) {
    if ( themeId == null ) {
      return null;
    }
    Theme theme = null;
    ModuleThemeInfo moduleThemeInfo =
        (ModuleThemeInfo) cache.getFromRegionCache( THEME_CACHE_REGION, MODULE_THEMES + "-" + moduleName );
    if ( moduleThemeInfo == null ) { // may have been flushed, try to fetch it
      moduleThemeInfo = collectAllModuleThemes().get( moduleName );
      if ( moduleThemeInfo == null ) {
        logger.debug( "Unable to retrieve module theme for (" + moduleName
            + ") as the module theme definition was not found" );
        return null;
      }
    }
    for ( Theme t : moduleThemeInfo.getModuleThemes() ) {
      if ( t.getId().equals( themeId ) ) {
        theme = t;
        break;
      }
    }
    if ( theme == null ) {
      logger
          .error( MessageFormat.format( "Unable to find requested module theme: %s module: %s", themeId, moduleName ) );
    }
    return theme;
  }

  public ModuleThemeInfo getModuleThemeInfo( String moduleName ) {
    ModuleThemeInfo moduleThemeInfo =
        (ModuleThemeInfo) cache.getFromRegionCache( THEME_CACHE_REGION, MODULE_THEMES + "-" + moduleName );
    if ( moduleThemeInfo == null ) { // may have been flushed, try to fetch it
      moduleThemeInfo = collectAllModuleThemes().get( moduleName );
      if ( moduleThemeInfo == null ) {
        logger.debug( "Unable to retrieve module theme for (" + moduleName
            + ") as the module theme definition was not found" );
        return null;
      }
    }
    return moduleThemeInfo;
  }

  public Map<String, ModuleThemeInfo> collectAllModuleThemes() {
    Map<String, ModuleThemeInfo> moduleThemes = new HashMap<String, ModuleThemeInfo>();
    for ( IThemeResolver resolver : resolvers ) {
      Map<String, ModuleThemeInfo> moduleInfo = resolver.getModuleThemes();
      for ( String moduleName : moduleInfo.keySet() ) {
        // populate the cache
        cache.putInRegionCache( THEME_CACHE_REGION, MODULE_THEMES + "-" + moduleName, moduleInfo.get( moduleName ) );
      }
      moduleThemes.putAll( moduleInfo );
    }
    return moduleThemes;
  }

  public Map<String, Theme> collectAllSystemThemes() {
    Map<String, Theme> systemThemes = new HashMap<String, Theme>();
    for ( IThemeResolver resolver : resolvers ) {
      Map<String, Theme> themes = resolver.getSystemThemes();
      for ( String themeId : themes.keySet() ) {
        // populate the cache
        cache.putInRegionCache( THEME_CACHE_REGION, SYSTEM_THEMES + "-" + themeId, themes.get( themeId ) );
      }
      systemThemes.putAll( themes );
    }
    return systemThemes;
  }

  public void refresh() {
    cache.clearRegionCache( THEME_CACHE_REGION );
    collectAllSystemThemes();
    collectAllModuleThemes();
  }

  public void setThemeResolvers( List<IThemeResolver> resolvers ) {
    this.resolvers = resolvers;
  }

  public List<IThemeResolver> getThemeResolvers() {
    return resolvers;
  }

}
