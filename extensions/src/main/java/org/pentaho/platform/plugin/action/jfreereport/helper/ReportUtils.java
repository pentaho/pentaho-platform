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

package org.pentaho.platform.plugin.action.jfreereport.helper;

import org.jfree.io.IOUtils;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.util.messages.LocaleHelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Creation-Date: 07.07.2006, 15:49:35
 * 
 * @author Thomas Morgner
 */
public class ReportUtils {
  private static class ClassLoaderEntry implements Serializable {
    private static final long serialVersionUID = 8925334939030498948L;

    private transient ClassLoader entry;

    public ClassLoaderEntry( final ClassLoader entry ) {
      this.entry = entry;
    }

    public ClassLoader getEntry() {
      return entry;
    }
  }

  private ReportUtils() {
  }

  public static synchronized File getTempDirectory( final IPentahoSession session ) {
    IApplicationContext ctx = PentahoSystem.getApplicationContext();
    if ( ctx != null ) {
      final String fileOutputPath = ctx.getFileOutputPath( "system/tmp/" ); //$NON-NLS-1$
      final File tempDir = new File( fileOutputPath );
      final String id = session.getId();

      final File userTempDir;
      if ( id == null ) {
        // typical sloopy programming! Someone forgot to check null values.
        userTempDir = tempDir;
      } else {
        userTempDir = new File( tempDir, id );
      }

      // this operation silently fails if the dir already exists.
      userTempDir.mkdir();
      return userTempDir;
    }
    throw new IllegalStateException( Messages.getInstance().getString( "ReportUtils.ERROR_0036_PENTAHO_SYSTEM_NOT_OK" ) ); //$NON-NLS-1$
  }

  public static ClassLoader createJarLoader( final IPentahoSession session, final IActionSequenceResource resource ) {

    // todo: We cant clean the temp directory ...
    // session.addListener (,..) is needed
    synchronized ( session ) {
      try {
        final URL url = ReportUtils.getURL( session, resource, true );
        if ( url == null ) {
          return null;
        }

        final Map cache = ReportUtils.getClassLoaderCache( session );
        ClassLoaderEntry entry = (ClassLoaderEntry) cache.get( url );
        if ( entry != null ) {
          if ( entry.getEntry() != null ) {
            return entry.getEntry();
          }
        }

        // now wrap the beast into a jar URL ...
        final URL jarURL = new URL( "jar:" + url.toExternalForm() + "!/" ); //$NON-NLS-1$ //$NON-NLS-2$
        final URLClassLoader urlClassLoader = URLClassLoader.newInstance( new URL[] { jarURL } );
        cache.put( url, new ClassLoaderEntry( urlClassLoader ) );
        return urlClassLoader;
      } catch ( IOException e ) {
        // something went wrong ..
        return null;
      }
    }
  }

  private static Map getClassLoaderCache( final IPentahoSession session ) {
    synchronized ( session ) {
      Object maybeMap = session.getAttribute( "-x-pentaho-classloaders" ); //$NON-NLS-1$
      if ( maybeMap instanceof Map ) {
        return (Map) maybeMap;
      }
      Map map = new HashMap();
      session.setAttribute( "-x-pentaho-classloaders", map ); //$NON-NLS-1$
      return map;
    }
  }

  private static URL
  getURL( final IPentahoSession session, final IActionSequenceResource resource, final boolean create )
    throws IOException {
    if ( resource.getSourceType() == IActionSequenceResource.URL_RESOURCE ) {
      return new URL( resource.getAddress() );
    }
    if ( resource.getSourceType() == IActionSequenceResource.FILE_RESOURCE ) {
      File file = new File( resource.getAddress() );
      if ( file.exists() && file.canRead() ) {
        return file.toURI().toURL();
      }
    }
    if ( resource.getSourceType() == IActionSequenceResource.SOLUTION_FILE_RESOURCE ) {
      String reportJarPath = PentahoSystem.getApplicationContext().getSolutionPath( resource.getAddress() );
      File file = new File( reportJarPath );
      if ( file.exists() && file.canRead() ) {
        return file.toURI().toURL();
      }
    }

    if ( create ) {
      // ok, fall back to copy the file into the temp dir and to load it from
      // there...
      File temp = ReportUtils.getTempDirectory( session );
      File tempFile = PentahoSystem.getApplicationContext().createTempFile( session, "loaded-jar-", ".jar", temp, true ); //$NON-NLS-1$ //$NON-NLS-2$
      // if that fails, we dont have to waste our time on copying the stuff ..
      final URL url = tempFile.toURI().toURL();

      final InputStream in = resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
      final OutputStream out = new BufferedOutputStream( new FileOutputStream( tempFile ) );
      try {
        IOUtils.getInstance().copyStreams( in, out );
      } finally {
        in.close();
        out.close();
      }
      return url;
    } else {
      return null;
    }
  }

  public static URL getURL( final IPentahoSession session, final IActionSequenceResource resource ) throws IOException {
    return ReportUtils.getURL( session, resource, false );
  }
}
