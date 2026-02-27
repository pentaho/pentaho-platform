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


package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.repository2.unified.IStreamListener;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.util.web.MimeHelper;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class RepositoryFileStreamProvider implements IBackgroundExecutionStreamProvider, IStreamListener {

  // providing a serialVersionUID will help prevent quartz from throwing incompatible class exceptions
  private static final long serialVersionUID = 2812310908328498989L;

  private static final Log logger = LogFactory.getLog( RepositoryFileStreamProvider.class );

  public String outputFilePath;
  public String inputFilePath;
  public String appendDateFormat;
  private IStreamingAction streamingAction;
  private boolean autoCreateUniqueFilename;

  public RepositoryFileStreamProvider( final String inputFilePath, final String outputFilePath,
      final boolean autoCreateUniqueFilename ) {
    this.outputFilePath = outputFilePath;
    this.inputFilePath = inputFilePath;
    this.autoCreateUniqueFilename = autoCreateUniqueFilename;
  }

  public RepositoryFileStreamProvider( final String inputFilePath, final String outputFilePath,
      final boolean autoCreateUniqueFilename, final String appendDateFormat ) {
    this( inputFilePath, outputFilePath, autoCreateUniqueFilename );
    this.appendDateFormat = appendDateFormat;
  }

  public RepositoryFileStreamProvider() {
  }

  public IStreamingAction getStreamingAction() {
    return streamingAction;
  }

  public void setStreamingAction( IStreamingAction streamingAction ) {
    this.streamingAction = streamingAction;
  }

  public String getOutputPath() {
    return outputFilePath;
  }

  public String getMimeType() {
    String mimeType = null;
    if ( streamingAction != null ) {
      mimeType = streamingAction.getMimeType( null );
    }
    if ( mimeType == null ) {
      mimeType = MimeHelper.getMimeTypeFromFileName( outputFilePath );
    }
    if ( mimeType == null ) {
      mimeType = "binary/octet-stream";
    }
    return mimeType;
  }

  public OutputStream getOutputStream() throws Exception {
    String tempOutputFilePath = getTempOutputFilePath();

    RepositoryFileOutputStream outputStream =
        new RepositoryFileOutputStream( tempOutputFilePath, autoCreateUniqueFilename, true );
    outputStream.addListener( this );
    outputStream.forceFlush( false );
    return outputStream;
  }

  protected String getTempOutputFilePath() {
    String tempOutputFilePath = outputFilePath;
    String extension = RepositoryFilenameUtils.getExtension( tempOutputFilePath );
    if ( "*".equals( extension ) ) { //$NON-NLS-1$
      tempOutputFilePath = tempOutputFilePath.substring( 0, tempOutputFilePath.lastIndexOf( '.' ) );

      if ( appendDateFormat != null ) {
        try {
          LocalDateTime now = LocalDateTime.now();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern( appendDateFormat );
          String formattedDate = now.format( formatter );
          tempOutputFilePath += formattedDate;
        } catch ( Exception e ) {
          logger.warn( "Unable to calculate current date: " + e.getMessage() );
        }
      }

      if ( streamingAction != null ) {
        String mimeType = streamingAction.getMimeType( null );
        if ( mimeType != null && MimeHelper.getExtension( mimeType ) != null ) {
          tempOutputFilePath += MimeHelper.getExtension( mimeType );
        }
      }
    }
    return tempOutputFilePath;
  }

  public void fileCreated( String filePath ) {
    IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class );
    RepositoryFile outputFile = repository.getFile( filePath );
    if ( outputFile != null ) {
      Map<String, Serializable> fileMetadata = repository.getFileMetadata( outputFile.getId() );
      RepositoryFile inputFile = repository.getFile( inputFilePath );
      if ( inputFile != null ) {
        fileMetadata.put( PentahoJcrConstants.PHO_CONTENTCREATOR, inputFile.getId() );
        repository.setFileMetadata( outputFile.getId(), fileMetadata );
      }
    }
  }

  public String getOutputFilePath() {
    return outputFilePath;
  }

  public void setOutputFilePath( String filePath ) {
    this.outputFilePath = filePath;
  }

  public String getInputFilePath() {
    return inputFilePath;
  }

  public void setInputFilePath( String filePath ) {
    this.inputFilePath = filePath;
  }

  public String getAppendDateFormat() {
    return appendDateFormat;
  }

  public void setAppendDateFormat( String appendDateFormat ) {
    this.appendDateFormat = appendDateFormat;
  }

  public InputStream getInputStream() throws Exception {
    IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class );
    RepositoryFile repositoryFile = repository.getFile( inputFilePath );
    if ( ( repositoryFile == null ) || repositoryFile.isFolder() ) {
      throw new FileNotFoundException();
    }
    return new RepositoryFileInputStream( repositoryFile );
  }

  public boolean autoCreateUniqueFilename() {
    return this.autoCreateUniqueFilename;
  }

  public String toString() {
    // TODO Auto-generated method stub
    return "input file = " + inputFilePath + ":" + "outputFile = " + outputFilePath;
  }
}
