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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.ui.IThemeResolver;
import org.pentaho.platform.api.ui.ModuleThemeInfo;
import org.pentaho.platform.api.ui.Theme;
import org.pentaho.platform.api.ui.ThemeResource;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Theme resolver for platform plugins.
 * <p>
 * Plugins that specify a "root_theme_folder" will have this folder's subfolders added as themes. If the themes are also
 * in the "system_themes' plugin setting they will be added as system-level themes. If there's already a system theme by
 * that name loaded it will be overridden.
 * 
 * User: nbaker Date: 5/15/11
 */
public class PluginThemeResolver implements IThemeResolver {

  private static IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
  private static IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class );
  private Map<String, ModuleThemeInfo> moduleThemes = new HashMap<String, ModuleThemeInfo>();
  private Log logger = LogFactory.getLog( PluginThemeResolver.class );

  public PluginThemeResolver() {
    super();

    for ( String pluginId : pluginManager.getRegisteredPlugins() ) {
      findPluginThemes( pluginId );
    }
  }

  public Map<String, ModuleThemeInfo> getModuleThemes() {
    return moduleThemes;
  }

  public Map<String, Theme> getSystemThemes() {
    Map<String, Theme> systemThemes = new HashMap<String, Theme>();

    // Find the declared default plugin for themes. Add it's themes to the map first. Any that come in later will
    // overwrite it.
    String defaultThemePlugin = PentahoSystem.getSystemSetting( "default-theme-plugin", "userconsole" );
    ModuleThemeInfo defaultThemePluginInfo = moduleThemes.get( defaultThemePlugin );
    if ( defaultThemePluginInfo == null ) {
      logger.debug( "Unable to find the default theme plugin: " + defaultThemePlugin );
    } else {
      // Add all system themes from the default plugin in now.
      for ( Theme theme : defaultThemePluginInfo.getSystemThemes() ) {
        systemThemes.put( theme.getId(), theme );
      }
    }

    // Loop through other modules and add their system themes. Any which have the same name as one supplied by the
    // default
    // theme plugin will override the default.
    for ( String module : moduleThemes.keySet() ) {
      if ( module.equals( defaultThemePlugin ) ) {
        continue;
      }
      for ( Theme theme : moduleThemes.get( module ).getSystemThemes() ) {
        systemThemes.put( theme.getId(), theme );
      }
    }
    return systemThemes;
  }

  private void findPluginThemes( String pluginId ) {
    try {
      InputStream pluginManifestStream =
          resLoader.getResourceAsStream( pluginManager.getClassLoader( pluginId ), "themes.xml" );
      if ( pluginManifestStream == null ) {
        return;
      }
      ResourceBundle resourceBundle = null;
      try {
        resourceBundle =
            ResourceBundle.getBundle( "themes", LocaleHelper.getDefaultLocale(), pluginManager
              .getClassLoader( pluginId ) );
      } catch ( Exception ignored ) { /* optional bundle */
      }

      SAXReader rdr = new SAXReader();
      Document pluginManifest = rdr.read( pluginManifestStream );

      String rootThemeFolder = pluginManifest.getRootElement().attributeValue( "root-folder" );
      // Plugins supplying styles whether local or system must have a root_theme_folder
      if ( rootThemeFolder == null ) {
        return;
      }

      // Things look good. Build-up theme information for this plugin.
      ModuleThemeInfo moduleThemeInfo = new ModuleThemeInfo( pluginId );

      List<Element> pluginNodes = pluginManifest.getRootElement().elements();
      for ( Element themeNode : pluginNodes ) {

        String themeId = themeNode.getName();
        String themeName = StringUtils.defaultIfEmpty( themeNode.attributeValue( "display-name" ), themeId );
        if ( themeName.startsWith( "${" ) ) {
          themeName = resourceBundle.getString( themeName );
        }
        Theme theme =
            new Theme( themeId, themeName, "content/" + pluginId + "/" + rootThemeFolder + "/" + themeId + "/" );
        theme.setHidden( "true".equals( themeNode.attributeValue( "hidden" ) ) );

        if ( "true".equals( themeNode.attributeValue( "system" ) ) ) {
          moduleThemeInfo.getSystemThemes().add( theme );
        } else {
          moduleThemeInfo.getModuleThemes().add( theme );
        }

        List<Element> resourceList = themeNode.elements();
        for ( Element res : resourceList ) {
          theme.addResource( new ThemeResource( theme, res.getText() ) );
        }

      }
      moduleThemes.put( pluginId, moduleThemeInfo );
    } catch ( Exception e ) {
      logger.debug( "Error parsing plugin themes", e );
    }
  }
}
