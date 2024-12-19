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


package org.pentaho.platform.plugin.services.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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

  public LocaleImportHandler( List<IMimeType> mimeTypes, List<String> artifacts ) {
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

  @VisibleForTesting
  RepositoryFile getLocaleParent( RepositoryFileImportBundle locale, Properties localePropertiesFromIndex ) {
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
      // The localeFolderChildren are files that are not locale/properties file based and are actual files/reports. So
      // we can do a quick check to make sure we are looking at potential locale files first before we need to go deeper
      RepositoryFile foundLocaleFolderChild = getLocaleFolderChild( localeFileName, localeFolderChildren );
      if ( foundLocaleFolderChild == null ) {
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
          } else if ( ( extractFileName( localeFileName ).equals( localeChildName )  // matches default properties files
              || ( extractFileNameWithCountryLocalization( localeFileName ).equals( localeChildName ) ) // matches file_en.properties files
              || ( extractFileNameWithLanguageLocalization( localeFileName ).equals( localeChildName ) ) ) // matches file_en_US.properties files
              && artifacts.contains( localeChildExtension ) ) {
            localeParent = localeChild;
            break;
          }
        }
      } else {
        localeParent = foundLocaleFolderChild;
      }
    }
    return localeParent;
  }

  private boolean isLocaleFolder( String localeFileName, Properties localePropertiesFromIndex ) {
    return ( localeFileName.startsWith( LOCALE_FOLDER ) && localeFileName.endsWith( LOCALE_EXT ) )
        || ( localeFileName.startsWith( LOCALE_FOLDER ) && localeFileName.endsWith( OLD_LOCALE_EXT ) )
        || ( localeFileName.equals( LOCALE_FOLDER + XML_LOCALE_EXT ) && !localePropertiesFromIndex.isEmpty() );
  }

  private RepositoryFile getLocaleFolderChild( String localeFileName, List<RepositoryFile> localeFolderChildren ) {
    RepositoryFile localeFolderChild = null;
    for ( RepositoryFile localeChild : localeFolderChildren ) {
      if ( localeFileName.equals( localeChild.getName() ) ) {
        localeFolderChild = localeChild;
        break;
      }
    }
    return localeFolderChild;
  }

  private String extractExtension( String name ) {
    int idx = name.lastIndexOf( "." );
    if ( idx == -1 || idx == name.length() ) {
      return name;
    }
    return name.substring( idx + 1 );
  }

  private String extractFileName( String name, char separator ) {
    int idx = name.lastIndexOf( separator );
    if ( idx == -1 || idx == name.length() ) {
      return name;
    }
    return name.substring( 0, idx );
  }

  private String extractFileName( String name ) {
    return extractFileName( name, '.' );
  }

  private String extractFileNameWithCountryLocalization( String name ) {
    return extractFileName( name, '_' );
  }

  private String extractFileNameWithLanguageLocalization( String name ) {
    String aux = extractFileName( name, '_' );
    return extractFileName( aux, '_' );
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

  @VisibleForTesting
  Properties loadPropertiesByXml( RepositoryFileImportBundle localeBundle ) {
    final Properties properties = new Properties();
    RepositoryFile file = localeBundle.getFile();
    if ( file == null ) {
      return properties;
    }
    String fileTitle = localeBundle.getFile().getName();

    if ( ( LOCALE_FOLDER + XML_LOCALE_EXT ).equals( fileTitle ) ) {
      try {
        InputStream is = localeBundle.getInputStream();
        is.reset();
        Document document = getLocalBundleDocument( is );
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

  @VisibleForTesting
  Document getLocalBundleDocument( InputStream is ) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory builderFactory = XMLParserFactoryProducer.createSecureDocBuilderFactory();
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    return builder.parse( is );
  }

}
