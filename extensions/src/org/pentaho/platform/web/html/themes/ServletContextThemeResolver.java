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
import org.pentaho.platform.api.ui.IThemeResolver;
import org.pentaho.platform.api.ui.ModuleThemeInfo;
import org.pentaho.platform.api.ui.Theme;
import org.pentaho.platform.api.ui.ThemeResource;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * User: nbaker Date: 5/15/11
 */
public class ServletContextThemeResolver implements IThemeResolver {

  private Map<String, ModuleThemeInfo> moduleThemes = new HashMap<String, ModuleThemeInfo>();
  private ServletContext context;
  private Log logger = LogFactory.getLog( ServletContextThemeResolver.class );

  public ServletContextThemeResolver() {
  }

  public void resolveThemes() {
    moduleThemes.clear();

    // check to see if we're running outside of a servlet context, if so there's nothing for us to do

    context = (ServletContext) PentahoSystem.getApplicationContext().getContext();
    if ( context == null ) {
      logger.debug( "No ServletContext found" );
      return;
    }
    for ( Object rootFolderObj : context.getResourcePaths( "/" ) ) {
      String rootFolder = (String) rootFolderObj;
      findPluginThemes( rootFolder );
    }
  }

  public Map<String, ModuleThemeInfo> getModuleThemes() {
    resolveThemes();
    return moduleThemes;
  }

  public Map<String, Theme> getSystemThemes() {
    resolveThemes();
    Map<String, Theme> systemThemes = new HashMap<String, Theme>();

    // Loop through other modules and add their system themes.
    for ( String module : moduleThemes.keySet() ) {
      for ( Theme theme : moduleThemes.get( module ).getSystemThemes() ) {
        systemThemes.put( theme.getId(), theme );
      }
    }
    return systemThemes;
  }

  private void findPluginThemes( String pluginId ) {

    // some implementations return folders without a leading slash, fix it now
    if ( pluginId.startsWith( "/" ) == false ) {
      pluginId = "/" + pluginId;
    }
    try {
      InputStream pluginManifestStream = context.getResourceAsStream( pluginId + "themes.xml" );
      if ( pluginManifestStream == null ) {
        return;
      }

      ResourceBundle resourceBundle = null;
      try {
        resourceBundle = ResourceBundle.getBundle( "themes", LocaleHelper.getDefaultLocale() );
      } catch ( MissingResourceException ignored ) {
        // resource bundles are optional
      }

      // getResourcePaths returns directories with slashes, remove those now
      pluginId = pluginId.replace( "/", "" );

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
        if ( themeName.startsWith( "${" ) && resourceBundle != null ) {
          themeName = resourceBundle.getString( themeName );
        }
        Theme theme = new Theme( themeId, themeName, pluginId + "/" + rootThemeFolder + "/" + themeId + "/" );
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
