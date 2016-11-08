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

import org.pentaho.platform.api.util.IVersionHelper;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class VersionHelper implements IVersionHelper {

  public String getVersionInformation() {
    return getVersionInformation( VersionHelper.class );
  }

  public String getVersionInformation( final Class clazz ) {
    ResourceBundle assemblyBundle = null;
    try {
      assemblyBundle = ResourceBundle.getBundle("server-assembly");
    } catch (MissingResourceException ignored) {
      
    }
    if (assemblyBundle != null) {
      return getVersionInformationFromProperties(assemblyBundle);
    }
    // The following two lines read from the MANIFEST.MF
    String implTitle = clazz.getPackage().getImplementationTitle();
    String implVersion = clazz.getPackage().getImplementationVersion();
    if ( implVersion != null ) {
      // If we're in a .jar file, then we can return the version information
      // from the .jar file.
      return implTitle + " " + implVersion; //$NON-NLS-1$
    } else {
      // We're not in a .jar file - try to find the build-res/version file and
      // read the version information from that.
      try {
        ResourceBundle bundle = ResourceBundle.getBundle( "build-res.version" ); //$NON-NLS-1$
        StringBuffer buff = new StringBuffer();
        buff.append( bundle.getString( "impl.title" ) ).append( ' ' ).append( bundle.getString( "release.major.number" ) ).append( '.' ).append( bundle.getString( "release.minor.number" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        buff.append( '.' )
            .append( bundle.getString( "release.milestone.number" ) ).append( '.' ).append( bundle.getString( "release.build.number" ) ).append( " (class)" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return buff.toString();
      } catch ( Exception ex ) {
        return "Pentaho BI Platform - No Version Information Available"; //$NON-NLS-1$
      }
    }
  }
  
  public String getVersionInformationFromProperties(ResourceBundle assemblyBundle) {
    StringBuffer buff = new StringBuffer();
    buff.append( assemblyBundle.getString( "assembly.title" ) ).append( ' ' ).append( assemblyBundle.getString( "assembly.version" ) );
    return buff.toString();
  }

  public static VersionInfo getVersionInfo() {
    return VersionHelper.getVersionInfo( VersionHelper.class );
  }

  public static VersionInfo getVersionInfo( final Class clazz ) {
    // Check if server-assembly.properties exists
    ResourceBundle assemblyBundle = null;
    try {
      assemblyBundle = ResourceBundle.getBundle("server-assembly");
    } catch (MissingResourceException ignored) {
      
    }
    if (assemblyBundle != null) {
      return VersionHelper.getVersionInfoFromProperties(assemblyBundle);
    }
    // Try to get the manifest for the class
    final Manifest manifest = ManifestUtil.getManifest( clazz );
    if ( manifest != null ) {
      // Return the version info from the manifest
      return VersionHelper.createVersionInfo( manifest );
    }
    // Return the default version info (from properties file)
    return VersionHelper.createVersionInfo();
  }

  /**
   * Extracts the version information data from the manifest's attributes and puts them into a VersionInfo
   * instance.
   * 
   * @param manifest
   *          the manifest information
   * @return the version information from the manifest
   */
  protected static VersionInfo createVersionInfo( final Manifest manifest ) {
    final VersionInfo versionInfo = new VersionInfo();
    final Attributes mainAttributes = manifest.getMainAttributes();
    versionInfo.setFromManifest( true );
    versionInfo.setProductID( mainAttributes.getValue( "Implementation-ProductID" ) ); //$NON-NLS-1$
    versionInfo.setTitle( mainAttributes.getValue( "Implementation-Title" ) ); //$NON-NLS-1$
    versionInfo.setVersion( mainAttributes.getValue( "Implementation-Version" ) ); //$NON-NLS-1$
    return versionInfo;
  }

  /**
   * Extracts the version information data from the <code>build-res/version.properties</code> file found in the
   * source directory.
   * 
   * @return the version information from the <code>build-res/version.properties</code> file
   */
  protected static VersionInfo createVersionInfo() {
    // We're not in a .jar file - try to find the build-res/version file and
    // read the version information from that.
    final VersionInfo versionInfo = new VersionInfo();
    try {
      final ResourceBundle bundle = ResourceBundle.getBundle( "build-res.version" ); //$NON-NLS-1$
      versionInfo.setFromManifest( false );
      versionInfo.setProductID( bundle.getString( "impl.productID" ) ); //$NON-NLS-1$
      versionInfo.setTitle( bundle.getString( "impl.title" ) ); //$NON-NLS-1$
      versionInfo.setVersionMajor( bundle.getString( "release.major.number" ) ); //$NON-NLS-1$
      versionInfo.setVersionMinor( bundle.getString( "release.minor.number" ) ); //$NON-NLS-1$
      versionInfo.setVersionBuild( bundle.getString( "release.build.number" ) ); //$NON-NLS-1$

      // The release milestone number has both the release number and the milestone number
      final String releaseMilestoneNumber = bundle.getString( "release.milestone.number" ); //$NON-NLS-1$
      if ( releaseMilestoneNumber != null ) {
        String[] parts = releaseMilestoneNumber.replace( '-', '.' ).split( "\\." ); //$NON-NLS-1$
        if ( parts.length > 0 ) {
          versionInfo.setVersionRelease( parts[0] );
          if ( parts.length > 1 ) {
            versionInfo.setVersionMilestone( parts[1] );
          }
        }
      }
    } catch ( Exception e ) {
      // TODO log this error
    }
    return versionInfo;
  }

  protected static VersionInfo getVersionInfoFromProperties(ResourceBundle assemblyBundle) {
    final VersionInfo versionInfo = new VersionInfo();
    try {
      versionInfo.setFromManifest( false );
      versionInfo.setTitle( assemblyBundle.getString( "assembly.title" ) );
      versionInfo.setProductID( assemblyBundle.getString( "assembly.productid" ) );
      versionInfo.setVersion( assemblyBundle.getString("assembly.version") );
    } catch (Exception ignored) {
      // ex.printStackTrace();
      versionInfo.setVersionRelease( "-error-" );
    }
    return versionInfo;
  }
  
  
}
