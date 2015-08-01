package org.pentaho.platform.osgi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
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

  public Properties expandProperties( Properties properties ) {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    Set<String> packages = new HashSet<String>();

    do {
      if ( !URLClassLoader.class.isAssignableFrom( classLoader.getClass() ) ) {
        continue;
      }
      URL[] urLs = ( (URLClassLoader) classLoader ).getURLs();
      for ( URL url : urLs ) {
        try {
          String fileName = URLDecoder.decode(url.getFile());
          File file = new File( fileName );
          if ( !file.exists() || file.isDirectory() ) {
            continue;
          }
          JarFile jarFile = new JarFile( file );
          Enumeration<JarEntry> entries = jarFile.entries();
          while ( entries.hasMoreElements() ) {
            JarEntry jarEntry = entries.nextElement();
            String name = jarEntry.getName();

            if ( jarEntry.isDirectory() ) {
              packages.add( name.replaceAll( "\\/", "." ).substring( 0, name.length() - 1 ) );
            }
          }
        } catch ( IOException e ) {
          logger.debug( "Error procesing jar for packages", e );
        }
      }
    } while ( ( classLoader = classLoader.getParent() ) != null );

    String packagesImports = properties.getProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA );
    Set<String> imports = new HashSet<String>();
    String[] split = packagesImports.split( "," );

    Set<String> qualifiedPackages = new HashSet<String>();
    Set<String> qualifiedPackagesWhole = new HashSet<String>();

    for ( String pack : split ) {
      pack = pack.trim();
      if ( pack.indexOf( ";" ) > 0 ) {
        qualifiedPackages.add( pack.substring( pack.indexOf( ";" ) ) );
        qualifiedPackagesWhole.add( pack );
      }
    }
    for ( String pack : split ) {
      if ( StringUtils.isNotEmpty( pack ) && pack.contains( ".*" ) ) {
        // expand out
        String basePackage = pack.substring( 0, pack.indexOf( "*" ) ).trim(); // including "."
        String predicate = pack.substring( pack.indexOf( "*" ) + 1 );
        for ( String aPackage : packages ) {
          if ( aPackage.startsWith( basePackage ) && !qualifiedPackages.contains( aPackage ) ) {
            imports.add( aPackage + predicate );
          }
        }
        imports.add( basePackage.substring( 0, basePackage.length() - 1 ) );
      } else {
        imports.add( pack );
      }
    }
    imports.addAll( qualifiedPackagesWhole );

    properties.setProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA, StringUtils.join( imports, "," ) );
    return properties;
  }
}
