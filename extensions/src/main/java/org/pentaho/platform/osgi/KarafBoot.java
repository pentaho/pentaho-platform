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
 * Copyright (c) 2002 - 2024 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.platform.osgi;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.karaf.main.Main;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * This Pentaho SystemListener starts the Embedded Karaf framework to support OSGI in the platform.
 */
public class KarafBoot implements IPentahoSystemListener {
  public static final String CLEAN_KARAF_CACHE = "org.pentaho.clean.karaf.cache";

  private Main main;
  private KarafInstance karafInstance;
  private Properties karafCustomProperties;
  private static FileLock karafBootFileLock;
  private static File lockFile;

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

  private static final String KARAF_LOGS_PATH = "karaf.log";

  protected static final String KARAF_DIR = "/system/karaf";
  private static FileFilter directoryFilter = new FileFilter() {
    @Override
    public boolean accept( File pathname ) {
      return pathname.isDirectory();
    }
  };

  @Override
  public boolean startup( IPentahoSession session ) {

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

      // Use getAbsolutePath to prevent invalid path in Windows
      String root = karafDir.getAbsolutePath();

      // See if user specified a karaf folder they would like to use
      String rootCopyFolderString = System.getProperty( PENTAHO_KARAF_ROOT_COPY_DEST_FOLDER );

      // Use a transient folder (will be deleted on exit) if user says to or cannot open config.properties
      boolean transientRoot = Boolean.parseBoolean( System.getProperty( PENTAHO_KARAF_ROOT_TRANSIENT, "false" ) );
      if ( rootCopyFolderString == null && !canOpenConfigPropertiesForEdit( root ) ) {
        transientRoot = true;
      }

      boolean shouldWaitForLock = Boolean.parseBoolean( System.getProperty( Const.KARAF_WAIT_FOR_BOOT_LOCK_FILE, "false" ) );
      if ( shouldWaitForLock ) {
        waitForBootLock();
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
          @Override
          public void run() {
            try {
              if ( main != null ) {
                main.destroy();
              }

              //release lock
              if ( karafInstance != null ) {
                karafInstance.close();
              }

              deleteRecursiveIfExists( destDir );
            } catch ( IOException e ) {
              logger.error( "Unable to delete karaf directory " + destDir, e );
            } catch ( Exception e ) {
              logger.error( "Error stopping Karaf", e );
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
          @Override
          public boolean accept( File file ) {
            Path filePath = Paths.get( file.toURI() );

            String relativePath = karafDirPath.relativize( filePath ).toString();
            if ( excludes.contains( relativePath ) ) {
              return false;
            } else if ( symlinks.contains( relativePath ) ) {
              File linkFile = new File( destDir, relativePath );

              try {
                boolean linkFileDirCreated = linkFile.getParentFile().mkdirs();
                logger.info(
                  "link file " + linkFile.getParentFile().getAbsolutePath() + ( linkFileDirCreated ? "created" : "already existed" ) );
              } catch ( SecurityException exception ) {
                logger.error( linkFile.getParentFile().getAbsolutePath() + " Access denied." );
                throw exception;
              }

              Path link = Paths.get( linkFile.toURI() );

              try {
                // Try to create a symlink and skip the copy if successful
                if ( Files.createSymbolicLink( link, filePath ) != null ) {
                  return false;
                }
              } catch ( IOException e ) {
                logger.warn( "Unable to create symlink " + linkFile.getAbsolutePath() + " -> " + file.getAbsolutePath() );
              }
            }

            return true;
          }
        } );
      }

      if ( destDir != null ) {
        root = destDir.toURI().getPath();
      }

      this.karafCustomProperties = this.readCustomProperties( root );

      configureSystemProperties( solutionRootPath, root );
      expandSystemPackages();
      cleanCachesIfFlagSet( root );

      // Setup karaf instance configuration
      karafInstance = createAndProcessKarafInstance( root );

      // Wrap the startup of Karaf in a child thread which has explicitly set a bogus authentication. This is
      // work-around and issue with Karaf inheriting the Authenticaiton set on the main system thread due to the
      // InheritableThreadLocal backing the SecurityContext. By setting a fake authentication, calls to the
      // org.pentaho.platform.osgi.SpringSecurityLoginModule always challenge the user.
      Thread karafThread = new Thread( new Runnable() {
        @Override
        public void run() {
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

    // release memory reserved for karaf custom properties, as they are only needed in the startup phase.
    this.karafCustomProperties = null;

    return main != null;
  }

  void cleanCachesIfFlagSet( String root ) throws IOException {
    // Check to see if the clean cache property is set. If so delete data and recreate before launching.
    logger.info( "Checking to see if " + CLEAN_KARAF_CACHE + " is enabled" );

    String cleanCache = this.karafCustomProperties.getProperty( CLEAN_KARAF_CACHE, "false" );
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

      this.karafCustomProperties.setProperty( CLEAN_KARAF_CACHE, "false" );

      FileOutputStream out = null;
      try {
        final String customLocation = root + "/etc/custom.properties";

        out = new FileOutputStream( customLocation );
        logger.info( "Setting " + CLEAN_KARAF_CACHE + " back to false as this is a one-time action" );

        this.karafCustomProperties.store( out, "Turning of one-time cache clean setting" );
      } finally {
        if ( out != null ) {
          IOUtils.closeQuietly( out );
        }
      }
    }
  }

  protected void deleteRecursiveIfExists( File item ) {
    if ( !item.exists() ) {
      return;
    }

    if ( !Files.isSymbolicLink( item.toPath() ) && item.isDirectory() ) {
      File[] subitems = item.listFiles();
      for ( File subitem : subitems ) {
        deleteRecursiveIfExists( subitem );
      }
    }

    try {
      if ( !item.delete() ) {
        logger.warn( item.toURI().toString() + " could not be deleted." );
      }
    } catch ( SecurityException exception ) {
      logger.warn( item.toURI().toString() + " cannot delete file. Access denied." );
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

    final String karafLogsPath = this.karafCustomProperties.getProperty( KARAF_LOGS_PATH, solutionRootPath + "/logs" );
    fillMissedSystemProperty( KARAF_LOGS_PATH, karafLogsPath );

    // When running in the PDI-Clients there are separate etc directories so that features can be customized for
    // the particular execution needs (Carte, Spoon, Pan, Kitchen)
    KettleClientEnvironment.ClientType clientType = getClientType();
    String extraKettleEtc = translateToExtraKettleEtc( clientType );

    // If clientType is null, the extraKettleEtc will return 'etc-default'
    // Added a check to see if the folder exist before setting the system property
    if ( extraKettleEtc != null && new File( root + extraKettleEtc ).exists() ) {
      System.setProperty( "felix.fileinstall.dir", root + "/etc" + "," + root + extraKettleEtc );
    } else {
      System.setProperty( "felix.fileinstall.dir", root + "/etc" );
    }

    // Tell others like the pdi-osgi-bridge that there's already a karaf instance running so they don't start
    // their own.
    System.setProperty( "embedded.karaf.mode", "true" );

    // set the location of the log4j config file, since OSGI won't pick up the one in webapp
    File file = new File( solutionRootPath + "/system/osgi/log4j2.xml" );
    if ( file.exists() ) {
      System.setProperty( "log4j2.configurationFile", file.toURI().toString() );
    } else {
      logger.warn( file.toURI().toString() + " file not exist" );
    }
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

  @VisibleForTesting
  void setCustomProperties( Properties properties ) {
    this.karafCustomProperties = properties;
  }

  @VisibleForTesting
  Properties readCustomProperties( String root ) {
    Properties properties = new Properties();

    FileInputStream inputStream = null;
    try {
      final String customLocation = root + "/etc/custom.properties";
      final File customFile = new File( customLocation );

      inputStream = new FileInputStream( customFile );
      properties.load( inputStream );
    } catch ( IOException ioe ) {
      logger.error( "Not able to load properties due to an error loading custom.properties", ioe );
    } finally {
      IOUtils.closeQuietly( inputStream );
    }

    return properties;
  }

  protected String translateToExtraKettleEtc( KettleClientEnvironment.ClientType clientType ) {
    if ( clientType != null ) {
      switch ( clientType ) {
        case SPOON:
        case PAN:
        case KITCHEN:
        case CARTE:
        case SCALE:
        case OTHER:
          return "/etc-" + clientType.getID().toLowerCase();
      }
    }
    return "/etc-default";
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

  void expandSystemPackages() {
    if ( this.karafCustomProperties.isEmpty() ) {
      logger.warn( "No custom.properties file found in karaf distribution." );

      return;
    }

    final Properties customProperties = new SystemPackageExtrapolator().expandProperties( this.karafCustomProperties );
    final String systemPackageExtra = customProperties.getProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA );

    System.setProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA, systemPackageExtra );
  }

  @Override
  public void shutdown() {
    try {
      if ( main != null ) {
        main.destroy();
      }
    } catch ( Exception e ) {
      logger.error( "Error stopping Karaf", e );
    }
  }

  /**
   * Ensures no other karaf instances are starting at the same time.  This can cause problems when using an alternative
   * karaf root and several instances of kitchen/pan are starting at once.
   * @throws InterruptedException
   */
  @VisibleForTesting
  boolean waitForBootLock() throws InterruptedException {
    boolean success = false;
    try {
      String tempDir = System.getProperty( "java.io.tmpdir" );
      long bootWaitTime = 300000;
      try {
        bootWaitTime = Long.parseLong( System.getProperty( Const.KARAF_BOOT_LOCK_WAIT_TIME, "300000" ) );
      } catch ( NumberFormatException e ) {
        logger.warn( String.format( "Error parsing value of %s, using default 5 minutes", Const.KARAF_BOOT_LOCK_WAIT_TIME ), e );
      }
      long waitTimeUp = System.currentTimeMillis() + bootWaitTime;
      setLockFile( new File( Paths.get( tempDir, Const.KARAF_BOOT_LOCK_FILE ).toUri() ) );

      // wait until the lock file is gone or we run out of time
      logger.debug( "Waiting for karaf boot lock..." );
      while ( lockFile.exists() && System.currentTimeMillis() < waitTimeUp ) {
        Thread.sleep( 1000 );
      }

      if ( lockFile.exists() ) {
        logger.warn( "Karaf boot lock timed out, but lock file {} still present.  Proceeding anyway. Please remove any stale lock file.", lockFile.getAbsolutePath() );
      } else if ( lockFile.createNewFile() ) {
        // createNewFile should be atomic, so we should be safe
        // deleteOnExit only works if the JVM shuts down normally and we shouldn't need it, but we might as well set it anyway
        lockFile.deleteOnExit();
        // lock the file to prevent other processes from stepping on it
        try ( FileChannel fileChannel = FileChannel.open( lockFile.toPath(), Set.of( StandardOpenOption.WRITE ) ) ) {
          setLockFileLock( fileChannel.lock() );
          logger.info( "Karaf boot lock secured." );
          fileChannel.write( ByteBuffer.wrap( getCurrentPid().getBytes() ) );
        }
        success = true;
      } else {
        // another process got here first and created the file before we could; start waiting again because we know
        logger.debug( "Karaf boot lock claimed by another process, waiting again..." );
        waitForBootLock();
      }
    } catch ( InterruptedException e ) {
      logger.error( "Caught interrupted exception waiting for karaf boot file lock", e );
      if ( Thread.interrupted() ) {
        throw new InterruptedException();
      }
    } catch ( IOException e ) {
      logger.warn( "Exception trying to get karaf boot file lock; proceeding anyway", e );
    }
    return success;
  }

  @VisibleForTesting
  String getCurrentPid() {
    return Long.toString( ProcessHandle.current().pid() );
  }

  public static File getLockFile() {
    return lockFile;
  }

  public static void setLockFile( File lockFile ) {
    KarafBoot.lockFile = lockFile;
  }

  public static FileLock getLockFileLock() {
    return karafBootFileLock;
  }

  public static void setLockFileLock( FileLock fileLock ) {
    KarafBoot.karafBootFileLock = fileLock;
  }
}
