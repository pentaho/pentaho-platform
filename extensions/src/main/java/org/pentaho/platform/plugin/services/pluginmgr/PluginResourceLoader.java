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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The default implementation of the {@link IPluginResourceLoader}. Obtains resources by searching the root directory of
 * a {@link PluginClassLoader}.
 * 
 * <h3>Resource discovery</h3> {@link PluginResourceLoader} will search the following places for plugin classes:
 * <ul>
 * <li>the /lib folder under the plugin's root directory, e.g. "myplugin/lib"
 * </ul>
 * {@link PluginResourceLoader} will search for non-class resources in several locations:
 * <ul>
 * <li>inside jar files located in the lib directory
 * <li>from the filesystem relative to the root directory of the plugin
 * </ul>
 * 
 * <h3>resourcePath</h3> This class requires resource paths to be the relative paths to plugin resources, relative the
 * root directory of the plugin. A resource path can be specified either using '/' or '.' (or both) in the path,
 * depending on the particular method you are using. It is usually best to specify the path using '/' since both the
 * filesystem and the classloader can handle this delimiter, whereas '.' will not be handled correctly if you are trying
 * to load a resource from the filesystem.
 * 
 * <h3>Plugin Settings</h3>This class backs the plugin settings APIs with the PentahoSystem settings service. See
 * {@link PentahoSystem#getSystemSetting(String, String)} and {@link ISystemSettings}. System settings are expected in a
 * file named settings.xml in the root of the plugin directory.
 * 
 * @author aphillips
 * 
 */
public class PluginResourceLoader implements IPluginResourceLoader {

  private File rootDir = null;

  private PluginClassLoader overrideClassloader;

  private String settingsPath = RepositoryFile.SEPARATOR + "settings.xml"; //$NON-NLS-1$

  public void setSettingsPath( String settingsPath ) {
    this.settingsPath = settingsPath;
  }

  @Deprecated
  public void setOverrideClassloader( PluginClassLoader pluginClassloader ) {
    this.overrideClassloader = pluginClassloader;
  }

  protected PluginClassLoader getOverrideClassloader() {
    return overrideClassloader;
  }

  /**
   * Force the resource loader to look for resources in this root directory. If null, the resource loader will consult
   * the {@link PluginClassLoader} for the root directory.
   * 
   * @param rootDir
   *          the root directory in which to search for resources
   * @deprecated instead of setting the root dir, have your application use a subclass of PluginResourceLoader that
   *             returns an appropriately pathed PluginClassLoader from an overridden {@link #getClassLoader(Class)}.
   */
  public void setRootDir( File rootDir ) {
    this.rootDir = rootDir;
  }

  public byte[] getResourceAsBytes( Class<? extends Object> clazz, String resourcePath ) {
    InputStream in = getResourceAsStream( clazz, resourcePath );
    if ( in == null ) {
      return null;
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      out.write( in );
    } catch ( IOException e ) {
      Logger.debug( this, "Cannot open stream to resource", e ); //$NON-NLS-1$
      return null;
    } finally {
      IOUtils.closeQuietly( in );
    }
    return out.toByteArray();
  }

  public String getResourceAsString( Class<? extends Object> clazz, String resourcePath )
    throws UnsupportedEncodingException {
    return getResourceAsString( clazz, resourcePath, LocaleHelper.getSystemEncoding() );
  }

  public String getResourceAsString( Class<? extends Object> clazz, String resourcePath, String charsetName )
    throws UnsupportedEncodingException {
    byte[] bytes = getResourceAsBytes( clazz, resourcePath );
    if ( bytes == null ) {
      return null;
    }
    return new String( bytes, charsetName );
  }

  public String getSystemRelativePluginPath( ClassLoader classLoader ) {
    File dir = getPluginDir( classLoader );
    if ( dir == null ) {
      return null;
    }
    // get the full path with \ converted to /
    String path = dir.getAbsolutePath().replace( "\\", RepositoryFile.SEPARATOR );
    int pos = path.lastIndexOf( RepositoryFile.SEPARATOR + "system" + RepositoryFile.SEPARATOR ); //$NON-NLS-1$
    if ( pos != -1 ) {
      path = path.substring( pos + 8 );
    }
    return path;
  }

  protected File getPluginDir( ClassLoader classLoader ) {
    if ( rootDir != null ) {
      return rootDir;
    }
    if ( classLoader instanceof PluginClassLoader ) {
      return ( (PluginClassLoader) classLoader ).getPluginDir();
    }
    return null;
  }

  /*
   * It is important for this method to exist since it provides a way to override the classloader which is particularly
   * useful in test cases
   */
  protected ClassLoader getClassLoader( Class<?> clazz ) {
    PluginClassLoader _overrideClassloader = getOverrideClassloader();
    ClassLoader classLoader = ( _overrideClassloader != null ) ? _overrideClassloader : clazz.getClassLoader();

    if ( !PluginClassLoader.class.isAssignableFrom( classLoader.getClass() ) ) {
      Logger
          .warn(
            this,
            Messages
              .getInstance()
              .getString(
                "PluginResourceLoader.WARN_CLASS_LOADED_OUTSIDE_OF_PLUGIN_ENV", clazz.getName(),
                PluginClassLoader.class.getSimpleName(), this.getClass().getSimpleName() ) ); //$NON-NLS-1$
    }
    return classLoader;
  }

  public InputStream getResourceAsStream( Class<?> clazz, String resourcePath ) {
    ClassLoader classLoader = getClassLoader( clazz );
    return getResourceAsStream( classLoader, resourcePath );
  }

  public InputStream getResourceAsStream( ClassLoader classLoader, String resourcePath ) {
    if ( getOverrideClassloader() != null ) {
      classLoader = getOverrideClassloader();
    }

    InputStream in = null;

    File root = getPluginDir( classLoader );
    if ( root != null ) {

      // can we find it on the filesystem?
      File f = new File( root, resourcePath );
      if ( f.canRead() ) {
        try {
          in = new BufferedInputStream( new FileInputStream( f ) );
        } catch ( FileNotFoundException e ) {
          Logger.debug( this, "Cannot open stream to resource", e ); //$NON-NLS-1$
        }
      } else { //if not in filesystem ask the classloader
        in = classLoader.getResourceAsStream( resourcePath );
        if ( in == null ) {
          Logger.debug( this, "Cannot find resource defined by path [" + resourcePath + "]" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }
    return in;
  }

  public List<URL> findResources( Class<?> clazz, String namePattern ) {
    ClassLoader classLoader = getClassLoader( clazz );
    return findResources( classLoader, namePattern );
  }

  public List<URL> findResources( ClassLoader classLoader, String namePattern ) {

    String dirPattern = "", filePattern = "*"; //$NON-NLS-1$ //$NON-NLS-2$

    if ( namePattern.contains( "/" ) ) { //$NON-NLS-1$
      String pattern = namePattern.substring( 0, namePattern.lastIndexOf( '/' ) );
      if ( pattern.length() > 0 ) {
        dirPattern = pattern;
      }
      pattern = namePattern.substring( namePattern.lastIndexOf( '/' ) + 1, namePattern.length() );
      if ( pattern.length() > 0 ) {
        filePattern = pattern;
      }
    } else {
      filePattern = namePattern;
    }

    IOFileFilter fileFilter = new WildcardFileFilter( filePattern );
    IOFileFilter dirFilter = TrueFileFilter.INSTANCE;

    Collection<?> files =
        FileUtils.listFiles( new File( getPluginDir( classLoader ), dirPattern ), fileFilter, dirFilter );
    Iterator<?> fileIter = files.iterator();
    List<URL> urls = new ArrayList<URL>( files.size() );
    while ( fileIter.hasNext() ) {
      try {
        urls.add( ( (File) fileIter.next() ).toURI().toURL() );
      } catch ( MalformedURLException e ) {
        Logger.warn( this, "Could not create url", e ); //$NON-NLS-1$
      }
    }
    return urls;
  }

  public ResourceBundle getResourceBundle( Class<?> clazz, String resourcePath ) {
    ResourceBundle bundle = ResourceBundle.getBundle( resourcePath, LocaleHelper.getLocale(), getClassLoader( clazz ) );
    return bundle;
  }

  public String getPluginSetting( Class<?> pluginClass, String key ) {
    return getPluginSetting( pluginClass, key, null );
  }

  public String getPluginSetting( Class<?> pluginClass, String key, String defaultVal ) {
    ClassLoader classLoader = getClassLoader( pluginClass );
    String pluginPath = getSystemRelativePluginPath( classLoader );
    File absPluginPath = getPluginDir( classLoader );
    if ( pluginPath == null ) {
      Logger.debug( this, Messages.getInstance().getString(
          "PluginResourceLoader.WARN_PLUGIN_PATH_BAD", "" + pluginClass, settingsPath, key ) ); //$NON-NLS-1$
      return defaultVal;
    }
    File settingsFile = new File( absPluginPath, settingsPath );
    if ( !settingsFile.exists() ) {
      Logger.debug( this, Messages.getInstance().getErrorString(
          "SYSTEMSETTINGS.ERROR_0002_FILE_NOT_IN_SOLUTION", settingsFile.getAbsolutePath() ) ); //$NON-NLS-1$
      return defaultVal;
    }
    return PentahoSystem.getSystemSetting( pluginPath + settingsPath, key, defaultVal );
  }

  public String getPluginSetting( ClassLoader classLoader, String key, String defaultVal ) {
    if ( getOverrideClassloader() != null ) {
      classLoader = getOverrideClassloader();
    }
    String pluginPath = getSystemRelativePluginPath( classLoader );
    if ( pluginPath == null ) {
      Logger.debug( this, Messages.getInstance().getString(
          "PluginResourceLoader.WARN_PLUGIN_PATH_BAD", "" + classLoader, settingsPath, key ) ); //$NON-NLS-1$
      return defaultVal;
    }

    File absPluginPath = getPluginDir( classLoader );
    File settingsFile = new File( absPluginPath, settingsPath );
    if ( !settingsFile.exists() ) {
      Logger.debug( this, Messages.getInstance().getErrorString(
          "SYSTEMSETTINGS.ERROR_0002_FILE_NOT_IN_SOLUTION", settingsFile.getAbsolutePath() ) ); //$NON-NLS-1$
      return defaultVal;
    }

    return PentahoSystem.getSystemSetting( pluginPath + settingsPath, key, defaultVal );
  }

}
