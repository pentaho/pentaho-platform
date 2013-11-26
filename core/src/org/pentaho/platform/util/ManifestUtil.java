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

package org.pentaho.platform.util;

import java.net.URL;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Set of utility methods related to the manifest file. <br/>
 * NOTE: if the manifest file can not be retrieved, these methods will not work and will return <code>null</code>.
 * The most common case for this is the the code is being run outside of a jar file.
 * 
 * @author dkincade
 */
public class ManifestUtil {
  /**
   * Retrieves the manifest information for the jar file which contains this utility class.
   * 
   * @return The Manifest file for the jar file which contains this utility class, or <code>null</code> if the code
   *         is not in a jar file.
   */
  public static Manifest getManifest() {
    return ManifestUtil.getManifest( ManifestUtil.class );
  }

  /**
   * Retrieves the manifest information for the jar file which contains the specified class.
   * 
   * @return The Manifest file for the jar file which contains the specified class, or <code>null</code> if the
   *         code is not in a jar file.
   */
  public static Manifest getManifest( final Class clazz ) {
    JarInputStream jin = null;
    try {
      final URL codeBase = clazz.getProtectionDomain().getCodeSource().getLocation();
      if ( codeBase.getPath().endsWith( ".jar" ) ) { //$NON-NLS-1$
        jin = new JarInputStream( codeBase.openStream() );
        Manifest manifest = jin.getManifest();
        return manifest;
      }
    } catch ( Exception e ) {
      // TODO handle this exception
    } finally {
      if ( jin != null ) {
        try {
          jin.close();
        } catch ( Exception ex ) {
          // TODO determine what to do if the close failed. Most likely nothing since we would have probably failed
          // earlier!
        }
      }
    }
    return null;
  }
}
