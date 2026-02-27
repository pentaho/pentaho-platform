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


package org.pentaho.platform.plugin.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.pentaho.platform.plugin.services.importexport.PentahoMetadataFileInfo.FileType.UNKNOWN;

/**
 * Parser for potential Pentaho Metadata Filenames from the old repository
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class PentahoMetadataFileInfo {
  private static Log log = LogFactory.getLog( PentahoMetadataFileInfo.class );

  public enum FileType {
    XMI, PROPERTIES, UNKNOWN
  }

  private static final Pattern[] xmiPatterns = new Pattern[] {
    // Created by data access
    Pattern.compile( ".*/([^/]+/resources/metadata/[^/]+\\.xmi)$" ),

    // Stored in solution repository
    Pattern.compile( ".*/([^/]+)/metadata.xmi$" ), };

  private static final String LANG = "[a-z]{2}";
  private static final String LANG_CC = LANG + "_[A-Z]{2}";
  private static final String LANG_CC_EXT = LANG_CC + "_[^/]+";

  private static final String PROPERTY_BUNDLE_DOMAIN_POSTFIX = ".xmi";

  private static final Pattern[] propertyBundlePatternsWithPostfix = new Pattern[] {
    // Created by data access
    Pattern.compile( ".*/([^/]+/resources/metadata/[^/]+)_(" + LANG + ")\\.properties$" ),
    Pattern.compile( ".*/([^/]+/resources/metadata/[^/]+)_(" + LANG_CC + ")\\.properties$" ),
    Pattern.compile( ".*/([^/]+/resources/metadata/[^/]+)_(" + LANG_CC_EXT + ")\\.properties$" ) };

  private static final Pattern[] propertyBundlePatternsNoPostfix = new Pattern[] {
    // Store in solution repository
    Pattern.compile( ".*/([^/]+)/metadata_(" + LANG + ").properties$" ),
    Pattern.compile( ".*/([^/]+)/metadata_(" + LANG_CC + ").properties$" ),
    Pattern.compile( ".*/([^/]+)/metadata_(" + LANG_CC_EXT + ").properties$" ),
    Pattern.compile( ".*/([^/]+)_(" + LANG + ")\\.properties$" ),
    Pattern.compile( ".*/([^/]+)_(" + LANG_CC + ")\\.properties$" ),
    Pattern.compile( ".*/([^/]+)_(" + LANG_CC_EXT + ")\\.properties$" ), };

  private FileType fileType;
  private String locale;
  private String domainId;
  private String path;

  public FileType getFileType() {
    return fileType;
  }

  public String getLocale() {
    return locale;
  }

  public String getDomainId() {
    return domainId;
  }

  public String getPath() {
    return path;
  }

  public PentahoMetadataFileInfo( final String path ) {
    this.path = path;
    this.fileType = UNKNOWN;
    final String internalPath = RepositoryFilenameUtils.normalize( path, true );
    if ( !StringUtils.isEmpty( internalPath ) ) {
      final String fileExtension = RepositoryFilenameUtils.getExtension( path );
      if ( StringUtils.equals( fileExtension, "xmi" ) ) {
        for ( final Pattern xmiPattern : xmiPatterns ) {
          final Matcher xmiMatcher = xmiPattern.matcher( internalPath );
          if ( xmiMatcher.matches() ) {
            log.trace( "MATCH: [" + internalPath + "] by [" + xmiPattern.pattern() + "] - group(s)=["
                + xmiMatcher.group( 1 ) + "]" );
            initialize( FileType.XMI, xmiMatcher.group( 1 ), null );
            break;
          }
        }
      } else if ( StringUtils.equals( fileExtension, "properties" ) ) {
        for ( final Pattern propertyBundlePattern : propertyBundlePatternsNoPostfix ) {
          final Matcher propertyBundleMatcher = propertyBundlePattern.matcher( internalPath );
          if ( propertyBundleMatcher.matches() ) {
            log.trace( "MATCH: [" + internalPath + "] by [" + propertyBundleMatcher.pattern() + "] - group(s)=["
                + propertyBundleMatcher.group( 1 ) + " : " + propertyBundleMatcher.group( 2 ) + "]" );
            initialize( FileType.PROPERTIES, propertyBundleMatcher.group( 1 ), propertyBundleMatcher.group( 2 ) );
            break;
          }
        }
        for ( final Pattern propertyBundlePattern : propertyBundlePatternsWithPostfix ) {
          final Matcher propertyBundleMatcher = propertyBundlePattern.matcher( internalPath );
          if ( propertyBundleMatcher.matches() ) {
            log.trace( "MATCH: [" + internalPath + "] by [" + propertyBundleMatcher.pattern() + "] - group(s)=["
                + propertyBundleMatcher.group( 1 ) + " : " + propertyBundleMatcher.group( 2 ) + "]" );
            initialize( FileType.PROPERTIES, propertyBundleMatcher.group( 1 ) + PROPERTY_BUNDLE_DOMAIN_POSTFIX,
                propertyBundleMatcher.group( 2 ) );
            break;
          }
        }
      }
    }
  }

  protected void initialize( final FileType fileType, final String domainId, final String locale ) {
    Assert.hasText( domainId, "Domain ID must not be null or empty" );
    Assert.isTrue( locale == null || !StringUtils.isEmpty( locale ), "Locale must be null or non-empty" );
    this.fileType = fileType;
    this.domainId = domainId;
    this.locale = locale;
  }

  public String toString() {
    final StringBuilder s = new StringBuilder();
    s.append( "PentahoMetadataFileInfo[fileType=" );
    switch ( fileType ) {
      case UNKNOWN:
        s.append( "unknown" );
        break;
      case XMI:
        s.append( "XMI : domainID=" ).append( domainId );
        break;

      case PROPERTIES:
        s.append( "PROPERTIES : domainID=" ).append( domainId ).append( " : locale=" ).append( locale );
        break;
    }
    s.append( " : path=" ).append( path ).append( "]" );
    return s.toString();
  }
}
