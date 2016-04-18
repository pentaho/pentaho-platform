/*
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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.osgi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.karaf.main.Main;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * This Pentaho SystemListener starts the Embedded Karaf framework to support OSGI in the platform.
 * <p/>
 * Created by nbaker on 7/29/14.
 */
public class KarafBoot implements IPentahoSystemListener {
  public static final String CLEAN_KARAF_CACHE = "org.pentaho.clean.karaf.cache";
  private Main main;
  Logger logger = LoggerFactory.getLogger( getClass() );

  public static final String PENTAHO_KARAF_ROOT_COPY_DEST_FOLDER = "pentaho.karaf.root.copy.dest.folder";

  public static final String PENTAHO_KARAF_ROOT_TRANSIENT = "pentaho.karaf.root.transient";

  public static final String PENTAHO_KARAF_ROOT_TRANSIENT_DIRECTORY_ATTEMPTS =
      "pentaho.karaf.root.transient.directory.attempts";

  public static final String PENTAHO_KARAF_ROOT_COPY_FOLDER_SYMLINK_FILES =
      "pentaho.karaf.root.copy.folder.symlink.files";

  public static final String PENTAHO_KARAF_ROOT_COPY_FOLDER_EXCLUDE_FILES =
    "pentaho.karaf.root.copy.folder.exclude.files";

  public static final String ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA = "org.osgi.framework.system.packages.extra";

  public static final String PENTAHO_KARAF_INSTANCE_RESOLVER_CLASS = "pentaho.karaf.instance.resolver.class";

  private static final String SYSTEM_PROP_OSX_APP_ROOT_DIR = "osx.app.root.dir";

  protected static final String KARAF_DIR = "/system/karaf";
  private static FileFilter directoryFilter = new FileFilter() {
    @Override public boolean accept( File pathname ) {
      return pathname.isDirectory();
    }
  };;

  @Override public boolean startup( IPentahoSession session ) {

    try {
      String solutionRootPath = PentahoSystem.getApplicationContext().getSolutionRootPath();

      File karafDir = new File( solutionRootPath + KARAF_DIR );

      if ( !karafDir.exists() ) {

        logger.warn( "Karaf not found in standard dir of '" + ( solutionRootPath + KARAF_DIR ) + "' " );

        String osxAppRootDir = System.getProperty( SYSTEM_PROP_OSX_APP_ROOT_DIR );

        if ( !StringUtils.isEmpty( osxAppRootDir ) ) {

          logger.warn( "Given that the system property '" + SYSTEM_PROP_OSX_APP_ROOT_DIR + "' is set, we are in "
              + "a OSX .app context; we'll try looking for Karaf in the app's root dir '" + osxAppRootDir + "' " );

          File osxAppKarafDir = new File( osxAppRootDir + KARAF_DIR );

          if ( osxAppKarafDir.exists() ) {
            karafDir = osxAppKarafDir; // karaf found in the app's root dir
          }
        }
      }

      String root = karafDir.toURI().getPath();

      // See if user specified a karaf folder they would like to use
      String rootCopyFolderString = System.getProperty( PENTAHO_KARAF_ROOT_COPY_DEST_FOLDER );

      // Use a transient folder (will be deleted on exit) if user says to or cannot open config.properties
      boolean transientRoot = Boolean.parseBoolean( System.getProperty( PENTAHO_KARAF_ROOT_TRANSIENT, "false" ) );
      if ( rootCopyFolderString == null && !canOpenConfigPropertiesForEdit( root ) ) {
        transientRoot = true;
      }

      final File destDir;
      if ( transientRoot ) {
        if ( rootCopyFolderString == null ) {
          destDir = Files.createTempDirectory( "karaf" ).toFile();
        } else {
          int directoryAttempts =
              Integer.parseInt( System.getProperty( PENTAHO_KARAF_ROOT_TRANSIENT_DIRECTORY_ATTEMPTS, "250" ) );
          File candidate = new File( rootCopyFolderString );
          int i = 1;
          while ( candidate.exists() || !candidate.mkdirs() ) {
            if ( i > directoryAttempts ) {
              candidate = Files.createTempDirectory( "karaf" ).toFile();
              logger.warn( "Unable to create " + rootCopyFolderString + " after " + i + " attempts, using temp dir "
                  + candidate );
              break;
            }
            candidate = new File( rootCopyFolderString + ( i++ ) );
          }

          destDir = candidate;
        }

        Runtime.getRuntime().addShutdownHook( new Thread( new Runnable() {
          @Override public void run() {
            try {
              FileUtils.deleteDirectory( destDir );
            } catch ( IOException e ) {
              logger.error( "Unable to delete karaf directory " + destDir, e );
            }
          }
        } ) );
      } else if ( rootCopyFolderString != null ) {
        destDir = new File( rootCopyFolderString );
      } else {
        destDir = null;
      }

      // Copy karaf (symlinking allowed files/folders if possible)
      if ( destDir != null && ( transientRoot || !destDir.exists() ) ) {
        final Set<String> symlinks = new HashSet<>();
        String symlinkFiles = System.getProperty( PENTAHO_KARAF_ROOT_COPY_FOLDER_SYMLINK_FILES, "lib,system" );
        if ( symlinkFiles != null ) {
          for ( String symlink : symlinkFiles.split( "," ) ) {
            symlinks.add( symlink.trim() );
          }
        }
        final Set<String> excludes = new HashSet<>();
        String excludeFiles = System.getProperty( PENTAHO_KARAF_ROOT_COPY_FOLDER_EXCLUDE_FILES, "caches" );
        if ( excludeFiles != null ) {
          for ( String exclude : excludeFiles.split( "," ) ) {
            excludes.add( exclude.trim() );
          }
        }
        final Path karafDirPath = Paths.get( karafDir.toURI() );
        FileUtils.copyDirectory( karafDir, destDir, new AbstractFileFilter() {
          @Override public boolean accept( File file ) {
            Path filePath = Paths.get( file.toURI() );
            String relativePath = karafDirPath.relativize( filePath ).toString();
            if ( excludes.contains( relativePath ) ) {
              return false;
            } else if ( symlinks.contains( relativePath ) ) {
              File linkFile = new File( destDir, relativePath );
              linkFile.getParentFile().mkdirs();
              Path link = Paths.get( linkFile.toURI() );
              try {
                // Try to create a symlink and skip the copy if successful
                if ( Files.createSymbolicLink( link, filePath ) != null ) {
                  return false;
                }
              } catch ( IOException e ) {
                logger
                    .error(
                        "Unable to create symlink " + linkFile.getAbsolutePath() + " -> " + file.getAbsolutePath(), e );
              }
            }
            return true;
          }
        } );
      }

      if ( destDir != null ) {
        root = destDir.toURI().getPath();
      }

      configureSystemProperties( solutionRootPath, root );

      String customLocation = root + "/etc/custom.properties";
      expandSystemPackages( customLocation );

      cleanCachesIfFlagSet( root );

      // Setup karaf instance configuration
      KarafInstance karafInstance = createAndProcessKarafInstance( root );


      // Wrap the startup of Karaf in a child thread which has explicitly set a bogus authentication. This is
      // work-around and issue with Karaf inheriting the Authenticaiton set on the main system thread due to the
      // InheritableThreadLocal backing the SecurityContext. By setting a fake authentication, calls to the
      // org.pentaho.platform.osgi.SpringSecurityLoginModule always challenge the user.
      Thread karafThread = new Thread( new Runnable() {

        @Override public void run() {
          // Bogus authentication
          SecurityContextHolder.getContext().setAuthentication( new UsernamePasswordAuthenticationToken(
              UUID.randomUUID().toString(), "" ) );
          main = new Main( new String[ 0 ] );
          try {
            main.launch();
          } catch ( Exception e ) {
            main = null;
            logger.error( "Error starting Karaf", e );
          }
        }
      } );
      karafThread.setDaemon( true );
      karafThread.run();
      karafThread.join();
    } catch ( Exception e ) {
      main = null;
      logger.error( "Error starting Karaf", e );
    }
    return main != null;
  }

  void cleanCachesIfFlagSet( String root ) throws IOException {
    String customLocation = root + "/etc/custom.properties";

    // Check to see if the clean cache property is set. If so delete data and recreate before launching.
    logger.info( "Checking to see if " + CLEAN_KARAF_CACHE + " is enabled" );
    Properties customProps = new Properties();
    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream( new File( customLocation ) );
      customProps.load( fileInputStream );
    } finally {
      if ( fileInputStream != null ) {
        IOUtils.closeQuietly( fileInputStream );
      }
    }
    String cleanCache = customProps.getProperty( CLEAN_KARAF_CACHE, "false" );
    if ( "true".equals( cleanCache ) ) {
      logger.info( CLEAN_KARAF_CACHE + " is enabled. Karaf data directories will be deleted" );

      File cacheParent = new File( root + "/caches" );

      File[] clientCacheFolders = cacheParent.listFiles( directoryFilter );
      for ( File clientCacheFolder : clientCacheFolders ) {
        File[] cacheDirs = clientCacheFolder.listFiles( directoryFilter );
        for ( File cacheDir : cacheDirs ) {
          File lockFile = new File( cacheDir, ".lock" );
          FileOutputStream fileOutputStream = new FileOutputStream( lockFile );
          try {
            FileLock fileLock = fileOutputStream.getChannel().tryLock();
            fileLock.release();
            IOUtils.closeQuietly( fileOutputStream );
            FileUtils.deleteDirectory( cacheDir );
          } catch ( Exception ignored ) {
            // lock active by another program
          } finally {
            IOUtils.closeQuietly( fileOutputStream );
          }
        }
      }

      customProps.setProperty( CLEAN_KARAF_CACHE, "false" );
      FileOutputStream out = null;
      try {
        out = new FileOutputStream( customLocation );
        logger.info( "Setting " + CLEAN_KARAF_CACHE + " back to false as this is a one-time action" );
        customProps.store( out, "Turning of one-time cache clean setting" );
      } finally {
        if ( out != null ) {
          IOUtils.closeQuietly( out );
        }
      }
    }

  }

  protected KarafInstance createAndProcessKarafInstance( String root )
      throws FileNotFoundException, KarafInstanceResolverException {
    String clientType = new ExceptionBasedClientTypeProvider().getClientType();
    KarafInstance karafInstance = new KarafInstance( root, clientType );
    karafInstance.assignPortsAndCreateCache();
    return karafInstance;
  }


  protected void configureSystemProperties( String solutionRootPath, String root ) {
    fillMissedSystemProperty( "karaf.home", root );
    fillMissedSystemProperty( "karaf.base", root );
    fillMissedSystemProperty( "karaf.history", root + "/data/history.txt" );
    fillMissedSystemProperty( "karaf.instances", root + "/instances" );
    fillMissedSystemProperty( "karaf.startLocalConsole", "false" );
    fillMissedSystemProperty( "karaf.startRemoteShell", "true" );
    fillMissedSystemProperty( "karaf.lock", "false" );
    fillMissedSystemProperty( "karaf.etc", root + "/etc" );

    // When running in the PDI-Clients there are separate etc directories so that features can be customized for
    // the particular execution needs (Carte, Spoon, Pan, Kitchen)
    KettleClientEnvironment.ClientType clientType = getClientType();
    String extraKettleEtc = translateToExtraKettleEtc( clientType );

    if ( extraKettleEtc != null ) {
      System.setProperty( "felix.fileinstall.dir", root + "/etc" + "," + root + extraKettleEtc );
    } else {
      System.setProperty( "felix.fileinstall.dir", root + "/etc" );
    }

    // Tell others like the pdi-osgi-bridge that there's already a karaf instance running so they don't start
    // their own.
    System.setProperty( "embedded.karaf.mode", "true" );

    // set the location of the log4j config file, since OSGI won't pick up the one in webapp
    File file = new File( solutionRootPath + "/system/osgi/log4j.xml" );
    if ( file.exists() ) {
      System.setProperty( "log4j.configuration", file.toURI().toString() );
    } else {
      logger.warn( file.toURI().toString() + " file not exist" );
    }
    // Setting ignoreTCL to true such that the OSGI classloader used to initialize log4j will be the
    // same one used when instatiating appenders.
    System.setProperty( "log4j.ignoreTCL", "true" );
  }

  /**
   * If property with propertyName does not exist, than set property with value propertyValue
   *
   * @param propertyName  - property for check
   * @param propertyValue - value to set if property null
   */
  protected void fillMissedSystemProperty( String propertyName, String propertyValue ) {
    if ( System.getProperty( propertyName ) == null ) {
      System.setProperty( propertyName, propertyValue );
    }
  }

  protected String translateToExtraKettleEtc( KettleClientEnvironment.ClientType clientType ) {
    String extraKettleEtc = null;
    if ( clientType != null ) {
      switch ( clientType ) {
        case SPOON:
          extraKettleEtc = "/etc-spoon";
          break;
        case PAN:
          extraKettleEtc = "/etc-pan";
          break;
        case KITCHEN:
          extraKettleEtc = "/etc-kitchen";
          break;
        case CARTE:
          extraKettleEtc = "/etc-carte";
          break;
        default:
          extraKettleEtc = "/etc-default";
          break;
      }
    }
    return extraKettleEtc;
  }

  protected KettleClientEnvironment.ClientType getClientType() {
    return KettleClientEnvironment.getInstance().getClient();
  }

  boolean canOpenConfigPropertiesForEdit( String directory ) {
    String testFile = directory + "/etc/config.properties";
    FileOutputStream fileOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream( testFile, true );
    } catch ( IOException e ) {
      return false;
    } finally {
      if ( fileOutputStream != null ) {
        try {
          fileOutputStream.close();
        } catch ( IOException e ) {
          // Ignore
        }
      }
    }
    return true;
  }

  void expandSystemPackages( String s ) {

    File customFile = new File( s );
    if ( !customFile.exists() ) {
      logger.warn( "No custom.properties file for in karaf distribution." );
      return;
    }
    Properties properties = new Properties();
    FileInputStream inStream = null;
    try {
      inStream = new FileInputStream( customFile );
      properties.load( inStream );
    } catch ( IOException e ) {
      logger
          .error( "Not able to expand system.packages.extra properties due to an error loading custom.properties", e );
      return;
    } finally {
      IOUtils.closeQuietly( inStream );
    }

    properties = new SystemPackageExtrapolator().expandProperties( properties );
    System.setProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA,
        properties.getProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA ) );

  }

  @Override public void shutdown() {
    try {
      if ( main != null ) {
        main.destroy();
      }
    } catch ( Exception e ) {
      logger.error( "Error stopping Karaf", e );
    }
  }
}
