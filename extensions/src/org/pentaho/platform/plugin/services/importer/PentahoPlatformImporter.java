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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

/**
 * Default implementation of IPlatformImporter. This class serves to route import requests to the appropriate
 * IPlatformImportHandler based on the mime-type of the given content. If not supplied the mime-type will be computed by
 * the IPlatformImportMimeResolver.
 * <p/>
 * User: nbaker Date: 5/29/12
 */
public class PentahoPlatformImporter implements IPlatformImporter {

  private static final Log log = LogFactory.getLog( PentahoPlatformImporter.class );
  private static final Messages messages = Messages.getInstance();
  private Map<String, IPlatformImportHandler> importHandlers;
  private IPlatformImportHandler defaultHandler;
  private IPlatformImportMimeResolver mimeResolver;
  private IRepositoryImportLogger repositoryImportLogger;
  private IRepositoryContentConverterHandler repositoryContentConverterHandler;

  public PentahoPlatformImporter( List<IPlatformImportHandler> handlerList,
                                  IRepositoryContentConverterHandler repositoryContentConverterHandler ) {
    this.repositoryContentConverterHandler = repositoryContentConverterHandler;
    importHandlers = new HashMap<String, IPlatformImportHandler>();
    mimeResolver = PentahoSystem.get( IPlatformImportMimeResolver.class );

    for ( IPlatformImportHandler platformImportHandler : handlerList ) {
      addHandler( platformImportHandler );
    }
  }

  public IPlatformImportHandler getDefaultHandler() {
    return this.defaultHandler;
  }

  public void setDefaultHandler( IPlatformImportHandler defaultHandler ) {
    this.defaultHandler = defaultHandler;
  }

  /**
   * To be consumed mainly by platform plugins who want to treat importing artifacts different.
   */
  public void addHandler( String mimeType, IPlatformImportHandler handler ) {
    this.importHandlers.put( mimeType, handler );
  }

  @Override
  public void addHandler( IPlatformImportHandler platformImportHandler ) {
    for ( MimeType mimeType : platformImportHandler.getMimeTypes() ) {
      this.importHandlers.put( mimeType.getName(), platformImportHandler );
      this.mimeResolver.addMimeType( mimeType );
      for ( String extension : mimeType.getExtensions() ) {
        repositoryContentConverterHandler.addConverter( extension, mimeType.getConverter() );
      }
    }
  }

  /**
   * this is the main method that uses the mime time (from Spring) to determine which handler to invoke.
   */
  public void importFile( IPlatformImportBundle file ) throws PlatformImportException {
    String mime = file.getMimeType() != null ? file.getMimeType() : mimeResolver.resolveMimeForBundle( file );
    try {
      if ( mime == null ) {
        log.trace( messages.getString( "PentahoPlatformImporter.ERROR_0001_INVALID_MIME_TYPE" ) + file.getName() );
        repositoryImportLogger.error( messages.getString( "PentahoPlatformImporter.ERROR_0001_INVALID_MIME_TYPE" )
            + file.getName() );
        return;
      }
      IPlatformImportHandler handler =
          ( importHandlers.containsKey( mime ) == false ) ? defaultHandler : importHandlers.get( mime );
      if ( handler == null ) {
        throw new PlatformImportException( messages
            .getString( "PentahoPlatformImporter.ERROR_0002_MISSING_IMPORT_HANDLER" ),
            PlatformImportException.PUBLISH_GENERAL_ERROR
        ); // replace with default handler?
      }
      try {
        logImportFile( file );
        handler.importFile( file );
      } catch ( DomainIdNullException e1 ) {
        throw new PlatformImportException( messages
            .getString( "PentahoPlatformImporter.ERROR_0004_PUBLISH_TO_SERVER_FAILED" ),
            PlatformImportException.PUBLISH_TO_SERVER_FAILED, e1
        );
      } catch ( DomainAlreadyExistsException e1 ) {
        throw new PlatformImportException( messages
            .getString( "PentahoPlatformImporter.ERROR_0007_PUBLISH_SCHEMA_EXISTS_ERROR" ),
            PlatformImportException.PUBLISH_SCHEMA_EXISTS_ERROR, e1
        );
      } catch ( DomainStorageException e1 ) {
        throw new PlatformImportException( messages
            .getString( "PentahoPlatformImporter.ERROR_0004_PUBLISH_TO_SERVER_FAILED" ),
            PlatformImportException.PUBLISH_DATASOURCE_ERROR, e1
        );
      } catch ( IOException e1 ) {
        throw new PlatformImportException( messages
            .getString( "PentahoPlatformImporter.ERROR_0005_PUBLISH_GENERAL_ERRORR" ),
            PlatformImportException.PUBLISH_GENERAL_ERROR, e1
        );
      } catch ( PlatformImportException pe ) {
        throw pe; // if already converted - just rethrow
      } catch ( Exception e1 ) {
        throw new PlatformImportException( messages
            .getString( "PentahoPlatformImporter.ERROR_0005_PUBLISH_GENERAL_ERRORR" ),
            PlatformImportException.PUBLISH_GENERAL_ERROR, e1
        );
      }
    } catch ( Exception e ) {
      e.printStackTrace();
      // If we are doing a logged import then we do not want to fail on a single file
      // so log the error and keep going.
      RepositoryFileImportBundle bundle = (RepositoryFileImportBundle) file;
      String repositoryFilePath = RepositoryFilenameUtils.concat( bundle.getPath(), bundle.getName() );
      if ( repositoryImportLogger.hasLogger() && repositoryFilePath != null && repositoryFilePath.length() > 0 ) {
        repositoryImportLogger.error( e );
      } else {
        if ( e instanceof PlatformImportException ) {
          throw (PlatformImportException) e;
        } else {
          // shouldn't happen but just in case
          throw new PlatformImportException( e.getMessage() );
        }
      }
    }
  }

  private void logImportFile( IPlatformImportBundle file ) {
    RepositoryFileImportBundle bundle = (RepositoryFileImportBundle) file;
    String repositoryFilePath = RepositoryFilenameUtils.concat( bundle.getPath(), bundle.getName() );
    // If doing a mondrian publish then there will be no active logger
    if ( repositoryImportLogger.hasLogger() && repositoryFilePath != null && repositoryFilePath.length() > 0 ) {
      repositoryImportLogger.setCurrentFilePath( repositoryFilePath );
      repositoryImportLogger.warn( file.getName() );
    }
  }

  public static String computeBundlePath( String bundlePath ) {
    bundlePath = RepositoryFilenameUtils.separatorsToRepository( bundlePath );
    if ( bundlePath.startsWith( RepositoryFile.SEPARATOR ) ) {
      bundlePath = bundlePath.substring( 1 );
    }
    return bundlePath;
  }

  public IRepositoryImportLogger getRepositoryImportLogger() {
    return repositoryImportLogger;
  }

  public void setRepositoryImportLogger( IRepositoryImportLogger repositoryImportLogger ) {
    this.repositoryImportLogger = repositoryImportLogger;
  }

  public Map<String, IPlatformImportHandler> getHandlers() {
    return importHandlers;
  }
}
