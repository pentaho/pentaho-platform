/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import org.pentaho.platform.api.util.IWadlDocumentResource;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;


public class DefaultSpringWadlResourceDocument implements IWadlDocumentResource {
  ClassPathResource springResource;
  String pluginId = "";
  boolean isPlugin = false;

  private static String WADL_NAME = "META-INF/wadl/wadlExtension.xml";

  public DefaultSpringWadlResourceDocument( Resource resource ) {
    this.springResource = (ClassPathResource) resource;

    //identify if is plugin
    if ( springResource.getClassLoader() instanceof PluginClassLoader ) {
      isPlugin = true;
      pluginId = ( (PluginClassLoader) springResource.getClassLoader() ).getPluginDir().getName();
    }
  }

  @Override
  public InputStream getResourceAsStream() throws IOException {
    Enumeration<URL> urls;

    urls = springResource.getClassLoader().getResources( WADL_NAME );

    InputStream is = null;
    URL url = null;

    String systemPath = getSystemPath();

    while ( urls.hasMoreElements() ) {
      url = urls.nextElement();
      if ( !isPlugin ) {
        break;
      } else {
        String urlString = url.getPath();
        if ( urlString.contains( systemPath + "/" + pluginId ) ) {
          break;
        }
      }
    }

    if ( url != null ) {
      try {
        is = getInputStream( url );
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }

    return is;
  }

  protected String getSystemPath() {
    return PentahoSystem.getApplicationContext().getSolutionPath( "system" );
  }

  protected InputStream getInputStream( URL url ) throws IOException {
    return url.openConnection().getInputStream();
  }

  @Override
  public boolean isFromPlugin() {
    return isPlugin;
  }

  @Override
  public String getPluginId() {
    return pluginId;
  }
}
