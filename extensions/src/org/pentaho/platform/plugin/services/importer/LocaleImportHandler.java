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

package org.pentaho.platform.plugin.services.importer;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.w3c.dom.Document;

public class LocaleImportHandler extends RepositoryFileImportFileHandler implements IPlatformImportHandler {

  private static final String FILE_DESCRIPTION = "file.description";
  private static final String FILE_TITLE = "file.title";

  private static final String DIRECTORY_NAME = "directory_name";
  private static final String DIRECTORY_DESCRIPTION = "directory_description";

  private static final String LOCALE_FOLDER = "index";

  private static final String LOCALE_EXT = ".locale";
  private static final String OLD_LOCALE_EXT = ".properties";
  private static final String XML_LOCALE_EXT = ".xml";

  private static final String VARIABLE_SYMBOL_FROM_INDEX = "%";
  private static final String TITLE_PROPERTY_NAME = "name";
  private static final String DESC_PROPERTY_NAME = "description";

  private List<String> artifacts; // spring injected file extensions

  private IUnifiedRepository unifiedRepository;

  public LocaleImportHandler( List<MimeType> mimeTypes, List<String> artifacts ) {
    super( mimeTypes );
    this.unifiedRepository = PentahoSystem.get( IUnifiedRepository.class );
    this.artifacts = artifacts;
  }

  public void importFile( IPlatformImportBundle bundle ) throws PlatformImportException {
    RepositoryFileImportBundle localeBundle = (RepositoryFileImportBundle) bundle;

    Properties localePropertiesFromIndex = loadPropertiesByXml( localeBundle );
    RepositoryFile localeParent = getLocaleParent( localeBundle, localePropertiesFromIndex );

    Properties localeProperties = buildLocaleProperties( localeBundle, localePropertiesFromIndex );

    String bundleFileName = localeBundle.getFile() != null ? localeBundle.getFile().getName() : localeBundle.getName();
    if ( localeParent != null && unifiedRepository != null && bundleFileName != null ) {
      // If the parent file (content) got skipped because it existed then we will not import the locale information
      String fullPath = RepositoryFilenameUtils.concat( localeBundle.getPath(), localeParent.getName() );
      if ( ImportSession.getSession().getSkippedFiles().contains( fullPath ) ) {
        getLogger().trace(
            "Not importing Locale [" + bundleFileName + "] since parent file not written " );
      } else {
        getLogger().trace( "Processing Locale [" + bundleFileName + "]" );
        unifiedRepository
            .setLocalePropertiesForFile( localeParent, extractLocaleCode( localeBundle ), localeProperties );
      }
    }
  }

  /**
   * return locale specific properties from resource bundle
   *
   * @param locale
   * @return
   */
  private Properties buildLocaleProperties( RepositoryFileImportBundle locale, Properties localePropertiesFromIndex ) {
    Properties localeProperties = new Properties();
    PropertyResourceBundle rb = null;
    String comment = locale.getComment();
    String fileTitle = locale.getName();

    if ( !localePropertiesFromIndex.isEmpty() ) {
      // for old style index.xml as locale
      comment = localePropertiesFromIndex.getProperty( DESC_PROPERTY_NAME );
      fileTitle = localePropertiesFromIndex.getProperty( TITLE_PROPERTY_NAME );
    } else {
      try {
        byte[] bytes = IOUtils.toByteArray( locale.getInputStream() );
        java.io.InputStream bundleInputStream = new ByteArrayInputStream( bytes );
        rb = new PropertyResourceBundle( bundleInputStream );
      } catch ( Exception returnEmptyIfError ) {
        getLogger().error( returnEmptyIfError.getMessage() );
        return localeProperties;
      }

      if ( rb != null ) {
        // this is the 4.8 style - name and description
        // First try file desc. If no file desc, try for a directory desc, else try fallback
        comment =
            rb.containsKey( DESC_PROPERTY_NAME ) ? rb.getString( DESC_PROPERTY_NAME ) : rb
                .containsKey( FILE_DESCRIPTION ) ? rb.getString( FILE_DESCRIPTION ) : rb
                .containsKey( DIRECTORY_DESCRIPTION ) ? rb.getString( DIRECTORY_DESCRIPTION ) : comment;

        // First try name. If no name, try title. If no title, try for a directory name, else use filename.
        fileTitle =
            rb.containsKey( TITLE_PROPERTY_NAME ) ? rb.getString( TITLE_PROPERTY_NAME ) : rb.containsKey( "title" )
                ? rb.getString( "title" ) : rb.containsKey( FILE_TITLE ) ? rb.getString( FILE_TITLE ) : rb
                .containsKey( DIRECTORY_NAME ) ? rb.getString( DIRECTORY_NAME ) : fileTitle;

      }
    }
    // this is the new .locale Jcr property names
    localeProperties.setProperty( FILE_DESCRIPTION, comment != null ? comment : StringUtils.EMPTY );
    localeProperties.setProperty( FILE_TITLE, fileTitle != null ? fileTitle : StringUtils.EMPTY );
    return localeProperties;
  }

  /**
   * returns default of the name of the locale e.g. JA, FR, EN, ... or DEFAULT for root
   *
   * @param localeBundle
   * @return
   */
  private String extractLocaleCode( RepositoryFileImportBundle localeBundle ) {
    String localeCode = "default";
    String localeFileName = localeBundle.getName();
    if ( localeBundle.getFile() != null ) {
      localeFileName = localeBundle.getFile().getName();
    }
    for ( Locale locale : Locale.getAvailableLocales() ) {
      if ( localeFileName.endsWith( "_" + locale + LOCALE_EXT )
          || localeFileName.endsWith( "_" + locale + OLD_LOCALE_EXT ) ) {
        localeCode = locale.toString();
        break;
      }
    }
    return localeCode;
  }

  private RepositoryFile getLocaleParent( RepositoryFileImportBundle locale, Properties localePropertiesFromIndex ) {
    if ( unifiedRepository == null ) {
      return null;
    }

    RepositoryFile localeParent = null;
    String localeFileName = locale.getName();
    if ( locale.getFile() != null ) {
      localeFileName = locale.getFile().getName();
    }
    RepositoryFile localeFolder = unifiedRepository.getFile( locale.getPath() );

    if ( isLocaleFolder( localeFileName, localePropertiesFromIndex ) ) {
      localeParent = localeFolder;
    } else if ( localeFolder != null ) {
      List<RepositoryFile> localeFolderChildren = unifiedRepository.getChildren( localeFolder.getId() );
      for ( RepositoryFile localeChild : localeFolderChildren ) {

        String localeChildName = extractFileName( localeChild.getName() );
        String localeChildExtension = extractExtension( localeChild.getName() );

        // [BISERVER-10444] locale is not being applied to correct file extension (report1.prpt.locale vs.
        // report1.xaction.locale)
        boolean localeFileNameAlsoContainsFileExtension =
            checkIfLocaleFileNameAlsoContainsAFileExtension( localeFileName );

        if ( localeFileNameAlsoContainsFileExtension ) {
          // FilenameUtils.getBaseName() returns name of file or empty string if none exists (NPE safe)
          // FilenameUtils.getExtension() returns extension of file or empty string if none exists (NPE safe)
          String localeFileExtension = FilenameUtils.getExtension( FilenameUtils.getBaseName( localeFileName ) );
          String localeFileNameWithoutExtensions =
              FilenameUtils.getBaseName( FilenameUtils.getBaseName( localeFileName ) );

          if ( localeFileExtension.contains( "_" ) ) {
            localeFileExtension = localeFileExtension.substring( 0, localeFileExtension.indexOf( "_" ) );
          }

          if ( localeFileNameWithoutExtensions.equals( localeChildName )
              && localeFileExtension.equalsIgnoreCase( localeChildExtension )
              && artifacts.contains( localeChildExtension ) ) {
            localeParent = localeChild;
            break;
          }

        } else if ( extractFileName( localeFileName ).equals( localeChildName )
            && artifacts.contains( localeChildExtension ) ) {
          localeParent = localeChild;
          break;
        }
      }
    }
    return localeParent;
  }

  private boolean isLocaleFolder( String localeFileName, Properties localePropertiesFromIndex ) {
    return ( localeFileName.startsWith( LOCALE_FOLDER ) && localeFileName.endsWith( LOCALE_EXT ) )
        || ( localeFileName.startsWith( LOCALE_FOLDER ) && localeFileName.endsWith( OLD_LOCALE_EXT ) )
        || ( localeFileName.equals( LOCALE_FOLDER + XML_LOCALE_EXT ) && !localePropertiesFromIndex.isEmpty() );
  }

  private String extractExtension( String name ) {
    int idx = name.lastIndexOf( "." );
    if ( idx == -1 || idx == name.length() ) {
      return name;
    }
    return name.substring( idx + 1 );
  }

  private String extractFileName( String name ) {
    int idx = name.lastIndexOf( "." );
    if ( idx == -1 || idx == name.length() ) {
      return name;
    }
    return name.substring( 0, idx );
  }

  private boolean checkIfLocaleFileNameAlsoContainsAFileExtension( String locale ) {

    // ex: report1.prpt.locale
    if ( StringUtils.isNotEmpty( locale ) ) {
      // FilenameUtils.getBaseName() returns name of file or empty string if none exists (NPE safe)
      // FilenameUtils.getExtension() returns extension of file or empty string if none exists (NPE safe)
      return StringUtils.isNotEmpty( FilenameUtils.getExtension( FilenameUtils.getBaseName( locale ) ) );
    }
    return false;
  }

  private Properties loadPropertiesByXml( RepositoryFileImportBundle localeBundle ) {
    final Properties properties = new Properties();
    String fileTitle = localeBundle.getName();

    if ( ( LOCALE_FOLDER + XML_LOCALE_EXT ).equals( fileTitle ) ) {
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = null;
      try {
        builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse( localeBundle.getInputStream() );
        XPath xPath = XPathFactory.newInstance().newXPath();

        String name = xPath.compile( "/index/name" ).evaluate( document );
        String desc = xPath.compile( "/index/description" ).evaluate( document );

        if ( StringUtils.isNotBlank( name ) && !name.equals( VARIABLE_SYMBOL_FROM_INDEX + TITLE_PROPERTY_NAME ) ) {
          properties.put( TITLE_PROPERTY_NAME, name );
        }
        if ( StringUtils.isNotBlank( desc ) && !desc.equals( VARIABLE_SYMBOL_FROM_INDEX + DESC_PROPERTY_NAME ) ) {
          properties.put( DESC_PROPERTY_NAME, desc );
        }
      } catch ( Exception e ) {
        getLogger().error( e.getMessage() );
      }
    }
    return properties;
  }

}
