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


package org.pentaho.platform.repository2.unified.fileio;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class RepositoryFileReader extends InputStreamReader {

  protected static String getEncoding( RepositoryFile file ) throws FileNotFoundException {
    IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class );
    SimpleRepositoryFileData fileData = repository.getDataForRead( file.getId(), SimpleRepositoryFileData.class );
    return fileData.getEncoding();
  }

  protected static String getEncoding( String path ) throws FileNotFoundException {
    IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class );
    RepositoryFile file = ( new RepositoryFileInputStream( path ) ).getFile();
    SimpleRepositoryFileData fileData = repository.getDataForRead( file.getId(), SimpleRepositoryFileData.class );
    return fileData.getEncoding();
  }

  protected static String getEncoding( Serializable id ) throws FileNotFoundException {
    IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class );
    RepositoryFile file = ( new RepositoryFileInputStream( id ) ).getFile();
    SimpleRepositoryFileData fileData = repository.getDataForRead( file.getId(), SimpleRepositoryFileData.class );
    return fileData.getEncoding();
  }

  public RepositoryFileReader( String path ) throws FileNotFoundException, UnsupportedEncodingException {
    super( new RepositoryFileInputStream( path ), getEncoding( path ) );
  }

  public RepositoryFileReader( RepositoryFile file ) throws FileNotFoundException, UnsupportedEncodingException {
    super( new RepositoryFileInputStream( file.getPath() ), getEncoding( file.getPath() ) );
  }

  public RepositoryFileReader( Serializable id ) throws FileNotFoundException, UnsupportedEncodingException {
    super( new RepositoryFileInputStream( id ), getEncoding( id ) );
  }
}
