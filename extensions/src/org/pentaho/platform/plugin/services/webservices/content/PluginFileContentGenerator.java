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

package org.pentaho.platform.plugin.services.webservices.content;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;

import java.io.InputStream;
import java.io.OutputStream;

public class PluginFileContentGenerator extends SimpleContentGenerator {

  private static final long serialVersionUID = 1L;

  String mimeType;
  String relativeFilePath;
  String pluginId;

  @Override
  public Log getLogger() {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void createContent( OutputStream outputStream ) throws Exception {
    IPluginResourceLoader pluginResourceLoader = PentahoSystem.get( IPluginResourceLoader.class );
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
    ClassLoader classLoader = pluginManager.getClassLoader( pluginId );
    String filePath = !relativeFilePath.startsWith( "/" ) ? "/" + relativeFilePath : relativeFilePath;
    InputStream inputStream = pluginResourceLoader.getResourceAsStream( classLoader, filePath );
    int val;
    while ( ( val = inputStream.read() ) != -1 ) {
      outputStream.write( val );
    }
    outputStream.flush();
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }

  public String getRelativeFilePath() {
    return relativeFilePath;
  }

  public void setRelativeFilePath( String relativeFilePath ) {
    this.relativeFilePath = relativeFilePath;
  }

  public void setMimeType( String mimeType ) {
    this.mimeType = mimeType;
  }

  public String getPluginId() {
    return pluginId;
  }

  public void setPluginId( String pluginId ) {
    this.pluginId = pluginId;
  }
}
