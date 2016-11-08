/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IFileFilter;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.SequenceDefinition;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author wseyler
 * 
 */
public class ActionSequenceJCRHelper {
  protected static final String PROPERTIES_SUFFIX = ".properties"; //$NON-NLS-1$
  protected static final Log logger = LogFactory.getLog( ActionSequenceJCRHelper.class );

  private IPentahoSession pentahoSession;
  private IUnifiedRepository repository;

  public ActionSequenceJCRHelper() {
    this( PentahoSessionHolder.getSession() );
  }

  public ActionSequenceJCRHelper( IPentahoSession pentahoSession ) {
    super();
    assert pentahoSession != null;
    this.pentahoSession = pentahoSession;
    repository = PentahoSystem.get( IUnifiedRepository.class, pentahoSession );
    if ( repository == null ) {
      final String errorMessage =
          Messages.getInstance().getErrorString( "ActionSequenceJCRHelper.ERROR_0001_INVALID_REPOSITORY" ); //$NON-NLS-1$
      logger.error( errorMessage );
      throw new IllegalStateException( errorMessage );
    }
  }

  /**
   * This legacy method ignores the requested actionOperation and defines read as the permission.
   * 
   * @deprecated use getActionSequence(String actionPath, int loggingLevel, RepositoryFilePermission
   *             actionOperation) instead.
   */
  @Deprecated
  public IActionSequence getActionSequence( String actionPath, int loggingLevel, int actionOperation ) {
    return getActionSequence( actionPath, loggingLevel, RepositoryFilePermission.READ );
  }

  public IActionSequence getActionSequence( String actionPath, int loggingLevel,
      RepositoryFilePermission actionOperation ) {
    Document actionSequenceDocument = getSolutionDocument( actionPath, actionOperation );
    if ( actionSequenceDocument == null ) {
      return null;
    }
    IActionSequence actionSequence =
        SequenceDefinition.ActionSequenceFactory( actionSequenceDocument, actionPath, pentahoSession, PentahoSystem
            .getApplicationContext(), loggingLevel );
    if ( actionSequence == null ) {
      return null;
    }

    return actionSequence;
  }

  public Document getSolutionDocument( final String documentPath, final RepositoryFilePermission actionOperation ) {

    RepositoryFile file = repository.getFile( documentPath );

    Document document = null;
    SimpleRepositoryFileData data = null;
    if ( file != null ) {
      data = repository.getDataForRead( file.getId(), SimpleRepositoryFileData.class );
      if ( data != null ) {
        try {
          document = XmlDom4JHelper.getDocFromStream( data.getStream() );
        } catch ( Throwable t ) {
          logger.error( Messages.getInstance().getErrorString(
              "ActionSequenceJCRHelper.ERROR_0017_INVALID_XML_DOCUMENT", documentPath ), t ); //$NON-NLS-1$
          return null;
        }
      } else {
        logger.error( Messages.getInstance().getErrorString(
            "ActionSequenceJCRHelper.ERROR_0019_NO_DATA_IN_FILE", file.getName() ) ); //$NON-NLS-1$
        return null;
      }
      if ( ( document == null ) && ( file != null ) && ( data != null ) ) {
        // the document exists but cannot be parsed
        logger.error( Messages.getInstance().getErrorString(
            "ActionSequenceJCRHelper.ERROR_0009_INVALID_DOCUMENT", documentPath ) ); //$NON-NLS-1$
        return null;
      }
      localizeDoc( document, file );
    }

    return document;
  }

  public String getURL( String filePath ) {
    RepositoryFile file = repository.getFile( filePath );
    if ( file == null || !file.getName().endsWith( ".url" ) ) { //$NON-NLS-1$
      return ""; //$NON-NLS-1$
    }
    SimpleRepositoryFileData data = null;

    data = repository.getDataForRead( file.getId(), SimpleRepositoryFileData.class );
    StringWriter writer = new StringWriter();
    try {
      IOUtils.copy( data.getStream(), writer );
    } catch ( IOException e ) {
      return ""; //$NON-NLS-1$
    }

    String props = writer.toString();
    StringTokenizer tokenizer = new StringTokenizer( props, "\n" ); //$NON-NLS-1$
    while ( tokenizer.hasMoreTokens() ) {
      String line = tokenizer.nextToken();
      int pos = line.indexOf( '=' );
      if ( pos > 0 ) {
        String propname = line.substring( 0, pos );
        String value = line.substring( pos + 1 );
        if ( ( value != null ) && ( value.length() > 0 ) && ( value.charAt( value.length() - 1 ) == '\r' ) ) {
          value = value.substring( 0, value.length() - 1 );
        }
        if ( "URL".equalsIgnoreCase( propname ) ) { //$NON-NLS-1$
          return value;
        }

      }
    }
    // No URL found
    return "";
  }

  public void localizeDoc( final Node document, final RepositoryFile file ) {
    String fileName = file.getName();
    int dotIndex = fileName.indexOf( '.' );
    String baseName = fileName.substring( 0, dotIndex );
    // TODO read in nodes from the locale file and use them to override the
    // ones in the main document
    try {
      List nodes = document.selectNodes( "descendant::*" ); //$NON-NLS-1$
      Iterator nodeIterator = nodes.iterator();
      while ( nodeIterator.hasNext() ) {
        Node node = (Node) nodeIterator.next();
        String name = node.getText();
        if ( name.startsWith( "%" ) && !node.getPath().endsWith( "/text()" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
          try {
            String localeText = getLocaleString( name, baseName, file, true );
            if ( localeText != null ) {
              node.setText( localeText );
            }
          } catch ( Exception e ) {
            logger
                .warn( Messages
                    .getInstance()
                    .getString(
                        "ActionSequenceJCRHelper.WARN_MISSING_RESOURCE_PROPERTY", name.substring( 1 ), baseName, getLocale().toString() ) ); //$NON-NLS-1$
          }
        }
      }
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getErrorString(
          "ActionSequenceJCRHelper.ERROR_0007_COULD_NOT_READ_PROPERTIES", file.getPath() ), e ); //$NON-NLS-1$
    }
  }

  protected String getLocaleString( final String key, String baseName, final RepositoryFile baseFile,
      boolean marchUpParents ) {

    String parentPath = FilenameUtils.getFullPathNoEndSeparator( baseFile.getPath() );

    RepositoryFile searchDir = repository.getFile( parentPath );
    if ( baseFile.isFolder() ) {
      searchDir = baseFile;
    }
    try {
      boolean searching = true;
      while ( searching ) {

        RepositoryFile[] propertyFiles = listFiles( searchDir, new IFileFilter() {
          public boolean accept( RepositoryFile file ) {
            return file.getName().toLowerCase().endsWith( PROPERTIES_SUFFIX );
          }
        } );
        RepositoryFile blcv = null;
        RepositoryFile blc = null;
        RepositoryFile bl = null;
        RepositoryFile b = null;
        for ( RepositoryFile element : propertyFiles ) {
          if ( element.getName().equalsIgnoreCase(
              baseName + '_' + getLocale().getLanguage() + '_' + getLocale().getCountry() + '_'
                  + getLocale().getVariant() + PROPERTIES_SUFFIX ) ) {
            blcv = element;
          }
          if ( element.getName().equalsIgnoreCase(
              baseName + '_' + getLocale().getLanguage() + '_' + getLocale().getCountry() + PROPERTIES_SUFFIX ) ) {
            blc = element;
          }
          if ( element.getName().equalsIgnoreCase( baseName + '_' + getLocale().getLanguage() + PROPERTIES_SUFFIX ) ) {
            bl = element;
          }
          if ( element.getName().equalsIgnoreCase( baseName + PROPERTIES_SUFFIX ) ) {
            b = element;
          }
        }

        String localeText = getLocaleText( key, blcv );
        if ( localeText == null ) {
          localeText = getLocaleText( key, blc );
          if ( localeText == null ) {
            localeText = getLocaleText( key, bl );
            if ( localeText == null ) {
              localeText = getLocaleText( key, b );
            }
          }
        }
        if ( localeText != null ) {
          return localeText;
        }
        if ( searching && marchUpParents ) {
          if ( !baseName.equals( "messages" ) ) { //$NON-NLS-1$
            baseName = "messages"; //$NON-NLS-1$
          } else {
            parentPath = FilenameUtils.getFullPathNoEndSeparator( searchDir.getPath() );
            // If the parent path is empty or the same as the parent's parent path (meaning root)
            if ( parentPath == null || parentPath.length() < 1 || parentPath.equals( searchDir.getPath() ) ) {
              searching = false;
            } else {
              searchDir = repository.getFile( parentPath );
            }
          }
        } else if ( !marchUpParents ) {
          searching = false;
        }
      }
      return null;
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getErrorString(
          "ActionSequenceJCRHelper.ERROR_0007_COULD_NOT_READ_PROPERTIES", baseFile.getPath() ), e ); //$NON-NLS-1$
    }
    return null;
  }

  protected Locale getLocale() {
    return LocaleHelper.getLocale();
  }

  protected String getLocaleText( final String key, final RepositoryFile file ) throws IOException {
    if ( file != null ) {

      SimpleRepositoryFileData data = null;
      data = repository.getDataForRead( file.getId(), SimpleRepositoryFileData.class );

      Properties p = new Properties();
      p.load( data.getStream() );

      String localeText = p.getProperty( key.substring( 1 ) );
      if ( localeText == null ) {
        localeText = p.getProperty( key );
      }
      if ( localeText != null ) {
        return localeText;
      }
    }
    return null;
  }

  private RepositoryFile[] listFiles( RepositoryFile searchDir, final IFileFilter filter ) {
    List<RepositoryFile> matchedFiles = new ArrayList<RepositoryFile>();
    Object[] objArray = repository.getChildren( searchDir.getId() ).toArray();
    for ( Object element : objArray ) {
      if ( filter.accept( (RepositoryFile) element ) ) {
        matchedFiles.add( (RepositoryFile) element );
      }
    }
    return matchedFiles.toArray( new RepositoryFile[] {} );
  }

}
