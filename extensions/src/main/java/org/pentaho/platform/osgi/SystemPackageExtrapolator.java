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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.osgi;

import org.apache.commons.lang.StringUtils;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by nbaker on 2/4/15.
 */
public class SystemPackageExtrapolator {

  public static final String ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA = "org.osgi.framework.system.packages.extra";
  Logger logger = LoggerFactory.getLogger( getClass() );

  private List<PackageProvider> providers = new ArrayList<>();

  public SystemPackageExtrapolator() {
    providers.add( new JBossModulePackageProvider() );
    providers.add( new UrlClassLoaderPackageProvider() );
  }

  public SystemPackageExtrapolator(
      List<PackageProvider> providers ) {
    this.providers = providers;
  }

  interface PackageProvider {
    Set<String> getPackages();
  }

  static class JBossModulePackageProvider implements PackageProvider {
    private Module module;

    @Override public Set<String> getPackages() {
      Set<String> exportedPaths = null;
      if ( module == null ) { // assume we're in the module
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // String comparison as we may not have the JBoss Modules classes.
        if ( classLoader.getClass().getName().equals( "org.jboss.modules.ModuleClassLoader" ) ) {
          exportedPaths = ( (ModuleClassLoader) classLoader ).getModule().getExportedPaths();
        }
      } else {
        exportedPaths = module.getExportedPaths();
      }

      Set<String> packages = new HashSet<>();
      if ( exportedPaths != null ) {
        for ( String exportedPath : exportedPaths ) {
          packages.add( exportedPath.replace( "/", "." ) );
        }
      }
      return packages;
    }

    public void setModule( Module module ) {
      this.module = module;
    }
  }

  class UrlClassLoaderPackageProvider implements PackageProvider {
    @Override public Set<String> getPackages() {
      Set<String> packages = new HashSet<>();
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      do {
        if ( !URLClassLoader.class.isAssignableFrom( classLoader.getClass() ) ) {
          continue;
        }
        URL[] urLs = ( (URLClassLoader) classLoader ).getURLs();
        for ( URL url : urLs ) {
          String fileName = URLDecoder.decode( url.getFile() );
          extractPackagesFromPath( packages, fileName );
        }
      } while ( ( classLoader = classLoader.getParent() ) != null );

      // needed if kettle is being loaded in the application classloader (e.g. PMR on JDK9+) instead of a URLClassLoader
      if ( packages.isEmpty() ) {
        String[] classPath = System.getProperty( "java.class.path" ).split( File.pathSeparator );
        for ( int i = 0; i < classPath.length; i++ ) {
          extractPackagesFromPath( packages, classPath[ i ] );
        }
      }

      return packages;
    }

    private void extractPackagesFromPath( Set<String> packages, String pathname ) {
      try {
        File file = new File( pathname );
        if ( !file.exists() || file.isDirectory() ) {
          return;
        }
        try ( JarFile jarFile = new JarFile( file ) ) {
          Enumeration<JarEntry> entries = jarFile.entries();
          while ( entries.hasMoreElements() ) {
            packages.add( getPackageName( entries.nextElement() ) );
          }
        }
      } catch ( IOException e ) {
        logger.debug( "Error procesing jar for packages", e );
      }
    }
  }

  static String getPackageName( JarEntry jarEntry ) {
    String name = jarEntry.getName();

    int lastSlash = name.lastIndexOf( '/' );
    if ( lastSlash >= 0 ) {
      return name.substring( 0, lastSlash ).replace( "/", "." );
    }
    return "";
  }

  public Properties expandProperties( Properties properties ) {

    Set<String> packages = new HashSet<>();
    for ( PackageProvider provider : providers ) {
      packages.addAll( provider.getPackages() );
    }

    String packagesImports = properties.getProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA );
    String[] split = packagesImports.split( "," );

    String[] expanded = expandPackages( split, packages.toArray( new String[packages.size()] ) );

    properties.setProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA, StringUtils.join( expanded, "," ) );
    return properties;
  }

  private String[] expandPackages( String[] packages, String[] availablePackages ) {
    Set<String> qualifiedPackages = new HashSet<>();
    Set<String> qualifiedPackagesWhole = new HashSet<>();
    Set<String> imports = new HashSet<>();

    for ( String pack : packages ) {
      pack = pack.trim();
      if ( pack.indexOf( ";" ) > 0 && pack.indexOf( "*" ) == -1 ) {
        qualifiedPackages.add( pack.substring( 0, pack.indexOf( ";" ) ) );
        qualifiedPackagesWhole.add( pack );
      }
    }
    for ( String pack : packages ) {
      if ( StringUtils.isNotEmpty( pack ) && pack.contains( ".*" ) ) {
        // expand out
        String basePackage = pack.substring( 0, pack.indexOf( "*" ) ).trim(); // including "."
        String predicate = pack.substring( pack.indexOf( "*" ) + 1 );
        for ( String aPackage : availablePackages ) {
          if ( aPackage.startsWith( basePackage ) && !qualifiedPackages.contains( aPackage ) ) {
            imports.add( aPackage + predicate );
          }
        }
        imports.add( basePackage.substring( 0, basePackage.length() - 1 ) + predicate );
      } else {
        imports.add( pack );
      }
    }
    imports.addAll( qualifiedPackagesWhole );
    return imports.toArray( new String[ imports.size() ] );
  }
}
