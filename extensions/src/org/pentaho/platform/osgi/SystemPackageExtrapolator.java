package org.pentaho.platform.osgi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

/**
 * Created by nbaker on 2/4/15.
 */
public class SystemPackageExtrapolator {

  public static final String ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA = "org.osgi.framework.system.packages.extra";
  Logger logger = LoggerFactory.getLogger( getClass() );

  public Properties expandProperties( Properties properties ){

    Field f;
    try {
      f = ClassLoader.class.getDeclaredField("packages");
    } catch ( NoSuchFieldException e ) {
      logger.warn( "Not able to expand system.packages.extra properties as the classloader does not support accessing "
          + "the classes field" );
      return properties;
    }
    f.setAccessible(true);
    Map<String, Package> packageMap = null;
    ClassLoader classLoader = getClass().getClassLoader();
    Set<String> packages = new HashSet<String>();
    do {
      try {
        packageMap = (Map<String, Package>) f.get( getClass().getClassLoader() );
      } catch ( IllegalAccessException e ) {
        logger
            .warn( "Not able to expand system.packages.extra properties due to an error accessing the classes field" );
        return properties;
      }
      packages.addAll( packageMap.keySet() );

    } while ((classLoader = classLoader.getParent()) != null);

    String packagesImports = properties.getProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA );
    Set<String> imports = new HashSet<String>();
    String[] split = packagesImports.split( "," );

    Set<String> qualifiedPackages = new HashSet<String>();
    Set<String> qualifiedPackagesWhole = new HashSet<String>();

    for ( String pack : split ) {
      pack = pack.trim();
      if(pack.indexOf( ";" ) > 0){
        qualifiedPackages.add(pack.substring( pack.indexOf( ";" ) ));
        qualifiedPackagesWhole.add(pack);
      }
    }
    for ( String pack : split ) {
      if( StringUtils.isNotEmpty( pack ) && pack.contains( ".*" ) ){
        // expand out
        String basePackage = pack.substring( 0, pack.indexOf( "*" ) ).trim(); // including "."
        String predicate = pack.substring( pack.indexOf( "*" ) + 1 );
        for ( String aPackage : packages ) {
          if(aPackage.startsWith( basePackage ) && ! qualifiedPackages.contains( aPackage )){
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
