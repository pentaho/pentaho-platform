/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.util;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.jar.JarFile;
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

  private ManifestUtil() {
  }
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
  public static Manifest getManifest( final Class<?> clazz ) {
    final URL codeBase = clazz.getProtectionDomain().getCodeSource().getLocation();
    if ( codeBase.getPath().endsWith( ".jar" ) ) { //$NON-NLS-1$
      try ( JarInputStream jin = new JarInputStream( codeBase.openStream() ) ) {
        Manifest manifest = jin.getManifest();
        if ( null != manifest ) {
          return manifest;
        }
      } catch ( Exception e ) {
        // TODO handle this exception
      }

      try {
        // couldn't get the manifest; might be on JBoss
        Class<?> virtualFile = Class.forName( "org.jboss.vfs.VirtualFile" );
        Method getChild = virtualFile.getMethod( "getChild", String.class );
        Object manifestVFile = getChild.invoke( codeBase.getContent(), JarFile.MANIFEST_NAME );

        Method openStream = virtualFile.getMethod( "openStream" );

        try ( InputStream manifestStream = (InputStream) openStream.invoke( manifestVFile ) ) {
          return new Manifest( manifestStream );
        }
      } catch ( Exception e ) {
        // TODO handle this exception
      }
    }
    return null;
  }
}
