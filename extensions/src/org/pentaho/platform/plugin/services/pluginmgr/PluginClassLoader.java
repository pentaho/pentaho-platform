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

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.services.messages.Messages;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom implementation of {@link URLClassLoader} for Pentaho Platform Plugins. It is used to load plugin jars and
 * classes and aids in retrieving resources by providing a root directory to search for resources related to plugins.
 * <p>
 * Note: {@link PluginClassLoader} will search for jar files in a 'lib' subdirectory under the pluginDir provided in the
 * constructor. Class and other resources will be visible to this classloader in either the root directory of the plugin
 * or in the lib folder.
 * 
 * @author aphillips
 */
public class PluginClassLoader extends URLClassLoader {
  private static Log log = LogFactory.getLog( PluginClassLoader.class );

  private File pluginDir;

  private boolean overrideLoad = false;

  /**
   * Creates a class loader for loading plugin classes and discovering resources. Jars must be located in
   * [pluginDir]/lib.
   * 
   * @param pluginDir
   *          the root directory of the plugin
   * @param parent
   *          the parent classloader
   */
  public PluginClassLoader( final File pluginDir, ClassLoader parent ) {
    super( getPluginUrls( pluginDir ), parent );
    this.pluginDir = pluginDir;
    if ( log.isDebugEnabled() ) {
      log.debug( "URLs for this classloader:" ); //$NON-NLS-1$
      for ( URL url : getURLs() ) {
        log.debug( url );
      }
    }
  }

  /**
   * Convenience method that creates a {@link PluginClassLoader} with the current classloader as it's parent.
   * 
   * @param pluginDir
   *          the root directory of the plugin
   * @param o
   *          the object from which the parent classloader will be derived
   */
  public PluginClassLoader( final File pluginDir, Object o ) {
    this( pluginDir, o.getClass().getClassLoader() );
  }

  /**
   * Controls whether or not this classloader will eagerly load a class requested by {@link #loadClass(String, boolean)}
   * ahead of the parent classloader. or delegate the load to the parent. If this method is not called, the default
   * behavior will apply which is to *not* override classloading.
   * 
   * @param b
   *          if true, loadClass method will look to this loader first to load a class, otherwise, the parent
   *          classloader will be queried first.
   */
  public void setOverrideLoad( boolean b ) {
    overrideLoad = b;
    if ( b ) {
      log.debug( "classloader " + this + " is set to override mode.  loadClass will now attempt to load " //$NON-NLS-1$ //$NON-NLS-2$
          + "the class from the classpath known to this classloader before delegating to the parent classloader" ); //$NON-NLS-1$
    }
  }

  protected static URL[] getPluginUrls( File pluginDir ) {
    List<URL> urls = new ArrayList<URL>();
    File libDir = new File( pluginDir, "lib" ); //$NON-NLS-1$
    try {
      urls.add( pluginDir.toURI().toURL() );
      urls.add( libDir.toURI().toURL() );
    } catch ( MalformedURLException e ) {
      log.warn( Messages.getInstance().getString(
          "PluginClassLoader.WARN_FAILED_TO_ADD_PLUGIN_DIR_TO_CLASSPATH", pluginDir //$NON-NLS-1$
              .getAbsolutePath(), libDir.getAbsolutePath() ), e );
    }
    addJars( urls, libDir );
    return urls.toArray( new URL[urls.size()] );
  }

  protected static void addJars( List<URL> urls, File folder ) {
    if ( folder.exists() && folder.isDirectory() ) {
      // get a list of all the JAR files
      FilenameFilter filter = new WildcardFileFilter( "*.jar" ); //$NON-NLS-1$
      File[] jarFiles = folder.listFiles( filter );
      if ( jarFiles != null && jarFiles.length > 0 ) {

        for ( File file : jarFiles ) {
          URL url = null;
          try {
            url = file.toURI().toURL();
            if ( log.isDebugEnabled() ) {
              log.debug( "adding jar to plugin classloader: " + url.toString() ); //$NON-NLS-1$
            }
            urls.add( url );
          } catch ( MalformedURLException e ) {
            log.warn( MessageFormat.format( "PluginClassLoader.WARN_FAILED_TO_ADD_JAR_TO_CLASSPATH", file //$NON-NLS-1$
                .getAbsolutePath() ), e );
          }
        }
      }
    }
  }

  public File getPluginDir() {
    return pluginDir;
  }

  @Override
  public Class<?> loadClass( String name, boolean resolve ) throws ClassNotFoundException {
    if ( log.isDebugEnabled() ) {
      log.debug( "loadClass(" + name + ")... " ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    Class<?> t = null;
    /*
     * Here we check the cache to see if the class has been loaded before by either this or the parent classloader. If
     * it has, then there is no need to load the class again, just return it.
     */
    t = findLoadedClass( name );
    if ( t != null ) {
      if ( log.isDebugEnabled() ) {
        log.debug( MessageFormat.format( "{0} loaded by {1}", name, t.getClassLoader() ) ); //$NON-NLS-1$
      }
      return t;
    }

    /*
     * If we are overriding the parent classloader we will first try to load this class from this classloader object in
     * isolation (no awareness of the parent classloader). If this classloader does not have the class, we will proceed
     * to attempt to load the class from the parent.
     */
    if ( overrideLoad ) {
      try {
        t = findClass( name );
        if ( t != null ) {
          if ( log.isDebugEnabled() ) {
            log.debug( MessageFormat.format( "{0} loaded by {1}", name, this ) ); //$NON-NLS-1$
          }
          if ( resolve ) {
            resolveClass( t );
          }
          return t;
        }
      } catch ( ClassNotFoundException e ) {
        if ( log.isTraceEnabled() ) {
          log.trace( MessageFormat.format( "class {0} not found in loader {1}. Trying parent loader", name, this ) ); //$NON-NLS-1$
        }
      }
    }

    /*
     * At this point we have not found the class either in the cache or in this classloader, so we need to ask the
     * parent.
     */
    t = super.loadClass( name, resolve );
    if ( log.isDebugEnabled() ) {
      log.debug( MessageFormat.format( "{0} loaded by {1}", name, t.getClassLoader() ) ); //$NON-NLS-1$
    }
    return t;
  }

  @Override
  public String toString() {
    return super.toString() + ( ( pluginDir != null ) ? " at " + pluginDir.getAbsolutePath() : "" ); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
