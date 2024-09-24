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
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifestFormatException;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.api.repository2.unified.webservices.LocaleMapDto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class ZipExportProcessor extends BaseExportProcessor {
  private static final Log log = LogFactory.getLog( ZipExportProcessor.class );

  protected String path;

  protected ExportManifest exportManifest;

  protected boolean withManifest = true;

  protected List<String> localeExportList;

  private static final int SAFETY_TMP_FILE_SIZE = 50;

  /**
   * Encapsulates the logic of registering import handlers, generating the manifest, and performing the export
   */
  public ZipExportProcessor( String path, IUnifiedRepository repository, boolean withManifest ) {
    this.withManifest = withManifest;

    // set a default path at root if missing
    if ( StringUtils.isEmpty( path ) ) {
      this.path = "/";
    } else {
      this.path = path;
    }

    setUnifiedRepository( repository );

    this.exportHandlerList = new ArrayList<>();

    initManifest();
  }

  protected void initManifest() {
    this.exportManifest = new ExportManifest();

    // set created by and create date in manifest information
    IPentahoSession session = getSession();

    Date todaysDate = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat( EXPORT_INFO_DATE_FORMAT );
    SimpleDateFormat timeFormat = new SimpleDateFormat( EXPORT_INFO_TIME_FORMAT );

    exportManifest.getManifestInformation().setExportBy( session.getName() );
    exportManifest.getManifestInformation().setExportDate(
            dateFormat.format( todaysDate ) + " " + timeFormat.format( todaysDate ) );
    exportManifest.getManifestInformation().setManifestVersion( "2" );
  }

  protected IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  /**
   * Performs the export process, returns a zip File object
   *
   * @throws ExportException indicates an error in import processing
   */
  public File performExport( RepositoryFile exportRepositoryFile ) throws ExportException, IOException {
    File exportFile = null;

    // create temp file
    exportFile = File.createTempFile( EXPORT_TEMP_FILENAME_PREFIX, EXPORT_TEMP_FILENAME_EXT );
    exportFile.deleteOnExit();

    // get the file path
    String filePath = new File( this.path ).getParent();
    if ( filePath == null ) {
      filePath = "/";
    }

    // send a response right away if not found
    if ( exportRepositoryFile == null ) {
      // todo: add to messages.properties
      throw new FileNotFoundException( "JCR file not found: " + this.path );
    }

    try ( ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( exportFile ) ) ) {
      if ( exportRepositoryFile.isFolder() ) { // Handle recursive export
        exportManifest.getManifestInformation().setRootFolder( path.substring( 0, path.lastIndexOf( "/" ) + 1 ) );

        // don't zip root folder without name
        if ( !ClientRepositoryPaths.getRootFolderPath().equals( exportRepositoryFile.getPath() ) ) {
          zos.putNextEntry( new ZipEntry( getFixedZipEntryName( exportRepositoryFile, filePath ) ) );
        }
        exportDirectory( exportRepositoryFile, zos, filePath );

      } else {
        exportManifest.getManifestInformation().setRootFolder( path.substring( 0, path.lastIndexOf( "/" ) + 1 ) );
        exportFile( exportRepositoryFile, zos, filePath );
      }

      if ( this.withManifest ) {
        // write manifest to zip output stream
        ZipEntry entry = new ZipEntry( EXPORT_MANIFEST_FILENAME );
        zos.putNextEntry( entry );

        // pass output stream to manifest class for writing
        try {
          exportManifest.toXml( zos );
        } catch ( Exception e ) {
          // todo: add to messages.properties
          log.error( "Error generating export XML" );
        }

        zos.closeEntry();
      }
    }

    // clean up
    exportManifest = null;

    return exportFile;
  }

  /**
   * @param repositoryFile
   * @param outputStream
   * @param filePath
   * @throws ExportManifestFormatException
   */
  public void exportFile( RepositoryFile repositoryFile, OutputStream outputStream, String filePath ) throws
      ExportException, IOException {

    // we need a zip
    ZipOutputStream zos = (ZipOutputStream) outputStream;

    // iterate through handlers to perform export
    for ( ExportHandler exportHandler : exportHandlerList ) {
      try ( InputStream is = exportHandler.doExport( repositoryFile, filePath ) ) {
        // if we don't get a valid input stream back, skip it
        if ( is != null ) {
          addToManifest( repositoryFile );
          String zipEntryName = getFixedZipEntryName( repositoryFile, filePath );
          ZipEntry entry = new ZipEntry( zipEntryName );
          zos.putNextEntry( entry );
          IOUtils.copy( is, outputStream );
          zos.closeEntry();
          createLocales( repositoryFile, filePath, repositoryFile.isFolder(), outputStream );
        }
      }
    }
  }

  /**
   * create an entry in the export manifest for this file or folder
   *
   * @param repositoryFile
   * @throws ExportException
   */
  protected void addToManifest( RepositoryFile repositoryFile ) throws ExportException {
    if ( this.withManifest ) {
      // add this entity to the manifest
      RepositoryFileAcl fileAcl = getUnifiedRepository().getAcl( repositoryFile.getId() );
      try {
        getExportManifest().add( repositoryFile, fileAcl );
      } catch ( ExportManifestFormatException e ) {
        throw new ExportException( e.getMessage() );
      }
    }
  }

  /**
   * @param repositoryDir
   * @param outputStream
   */
  @Override
  public void exportDirectory( RepositoryFile repositoryDir, OutputStream outputStream, String filePath ) throws
      ExportException, IOException {
    addToManifest( repositoryDir );
    List<RepositoryFile> children = getUnifiedRepository().getChildren( new RepositoryRequest(
        String.valueOf( repositoryDir.getId() ), true, 1, null ) );
    for ( RepositoryFile repositoryFile : children ) {
      // exclude 'etc' folder - datasources and etc.
      if ( isExportCandidate( repositoryFile.getPath() ) ) {
        if ( repositoryFile.isFolder() ) {
          if ( outputStream.getClass().isAssignableFrom( ZipOutputStream.class ) ) {
            ZipOutputStream zos = (ZipOutputStream) outputStream;
            String zipEntryName = getFixedZipEntryName( repositoryFile, filePath );
            ZipEntry entry = new ZipEntry( zipEntryName );
            zos.putNextEntry( entry );
          }
          exportDirectory( repositoryFile, outputStream, filePath );
        } else {
          try {
            exportFile( repositoryFile, outputStream, filePath );
          } catch ( ZipException e ) {
            // possible duplicate entry, log it and continue on with the other files in the directory
            log.debug( e.getMessage(), e );
          }
        }
      }
    }
    createLocales( repositoryDir, filePath, repositoryDir.isFolder(), outputStream );
  }

  protected boolean isExportCandidate( String path ) {
    return !ClientRepositoryPaths.getEtcFolderPath().equals( path );
  }

  /**
   * Take repository file path and local file path and return computed zip entry path
   *
   * @param repositoryFile
   * @param filePath
   * @return
   */
  protected String getFixedZipEntryName( RepositoryFile repositoryFile, String filePath ) {
    String result = getZipEntryName( repositoryFile, filePath );
    if ( this.withManifest ) {
      result = ExportFileNameEncoder.encodeZipPathName( result );
    }
    return result;
  }

  protected String getZipEntryName( RepositoryFile repositoryFile, String filePath ) {
    String result = "";

    // if we are at the root, get substring differently
    int filePathLength = 0;

    if ( filePath.equals( "/" ) || filePath.equals( "\\" ) ) {
      filePathLength = filePath.length();
    } else {
      filePathLength = filePath.length() + 1;
    }

    result = repositoryFile.getPath().substring( filePathLength );

    // add trailing slash for folders
    if ( repositoryFile.isFolder() ) {
      result += "/";
    }
    return FilenameUtils.normalize( result, true );
  }

  /**
   * for each locale stored in in Jcr create a .locale file with the stored node properties
   *
   * @param repositoryFile
   * @param filePath
   * @param isFolder
   * @param outputStream
   * @throws IOException
   */
  protected void createLocales( RepositoryFile repositoryFile, String filePath, boolean isFolder,
                              OutputStream outputStream ) throws IOException {
    ZipEntry entry;
    String zipEntryName;
    String name;
    String localeName;
    Properties properties;
    ZipOutputStream zos = (ZipOutputStream) outputStream;
    // only process files and folders that we know will have locale settings
    if ( supportedLocaleFileExt( repositoryFile ) ) {
      List<LocaleMapDto> locales = getAvailableLocales( repositoryFile.getId() );
      zipEntryName = getFixedZipEntryName( repositoryFile, filePath );
      name = repositoryFile.getName();
      for ( LocaleMapDto locale : locales ) {
        localeName = locale.getLocale().equalsIgnoreCase( "default" ) ? "" : "_" + locale.getLocale();
        if ( isFolder ) {
          zipEntryName = getFixedZipEntryName( repositoryFile, filePath ) + "index";
          name = "index";
        }

        properties = getUnifiedRepository().getLocalePropertiesForFileById( repositoryFile.getId(), locale.getLocale() );
        if ( properties != null ) {
          properties.remove( "jcr:primaryType" ); // Pentaho Type

          try ( InputStream is = createLocaleFile( name + localeName, properties, locale.getLocale() ) ) {
            if ( is != null ) {
              entry = new ZipEntry( zipEntryName + localeName + LOCALE_EXT );
              zos.putNextEntry( entry );
              IOUtils.copy( is, outputStream );
              zos.closeEntry();
            }
          }
        }
      }
    }
  }

  /**
   * there are certain extensions that get imported with locale maps (incorrectly?) like .png only export locale maps
   * for the list from importexport.xml
   *
   * @param repositoryFile
   * @return true if supported
   */
  private boolean supportedLocaleFileExt( RepositoryFile repositoryFile ) {
    boolean ans = true;
    String ext = repositoryFile.getName();
    if ( !repositoryFile.isFolder() ) {
      int idx = ext.lastIndexOf( "." );
      if ( idx > 0 ) {
        ext = ext.substring( idx, ext.length() );
      }
      List<String> exportList = getLocaleExportList();
      if ( exportList != null ) {
        ans = exportList.contains( ext );
      }

    }
    return ans;
  }

  /**
   * lookup the list of available locale values
   *
   * @param fileId
   * @return
   */
  private List<LocaleMapDto> getAvailableLocales( Serializable fileId ) {
    List<LocaleMapDto> availableLocales = new ArrayList<LocaleMapDto>();
    List<Locale> locales = getUnifiedRepository().getAvailableLocalesForFileById( fileId );
    if ( locales != null && !locales.isEmpty() ) {
      for ( Locale locale : locales ) {
        availableLocales.add( new LocaleMapDto( locale.toString(), null ) );
      }
    }
    return availableLocales;
  }

  /**
   * need to create the locale file
   *
   * @param name
   * @param properties
   * @param locale
   * @throws IOException
   * @returns inputStream pointer to created file or null
   */
  private InputStream createLocaleFile( String name, Properties properties, String locale ) throws IOException {
    if ( properties != null ) {
      File localeFile = null;

      try {
        localeFile = PentahoSystem
          .getApplicationContext().createTempFile( getSession(), ExportFileNameEncoder.encodeZipFileName( name ), LOCALE_EXT, true );
      } catch ( IOException e ) {
        // BISERVER-14140 - Retry when temp file name exceeds the limit of OS
        // Retry inside a catch because there isn't an accurate mechanism to determine the effective temp file max length
        // This is the local temp file only, inside the zip, the final file will have the original name
        String smallerName = ExportFileNameEncoder.encodeZipFileName( name ).substring( 0, SAFETY_TMP_FILE_SIZE );
        log.debug( "Error with original name file. Retrying with a smaller temp file name - " + smallerName );
        localeFile = PentahoSystem
          .getApplicationContext().createTempFile( getSession(), smallerName, LOCALE_EXT, true );
      } finally {
        if ( localeFile != null ) {
          localeFile.deleteOnExit();
        }
      }

      try ( FileOutputStream fileOut = new FileOutputStream( localeFile ) ) {
        properties.store( fileOut, "Locale = " + locale );
      }
      return new FileInputStream( localeFile );
    }
    return null;
  }

  /**
   * get the list of files we are interested in supporting locale from Spring
   *
   * @return
   */
  public List<String> getLocaleExportList() {
    if ( this.localeExportList == null || this.localeExportList.isEmpty() ) {
      for ( ExportHandler exportHandler : exportHandlerList ) {
        this.localeExportList = ( (DefaultExportHandler) exportHandler ).getLocaleExportList();
        break;
      }
    }
    return localeExportList;
  }

  public void setLocaleExportList( List<String> localeExportList ) {
    this.localeExportList = localeExportList;
  }

  public ExportManifest getExportManifest() {
    return exportManifest;
  }

  public void setExportManifest( ExportManifest exportManifest ) {
    this.exportManifest = exportManifest;
  }
}
