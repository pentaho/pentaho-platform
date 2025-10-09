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

import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Converts stream of binary or character data.
 * 
 * @author mlowery
 */
public class StreamConverter implements Converter {

  IUnifiedRepository repository;

  public StreamConverter( IUnifiedRepository repository ) {
    this.repository = repository;
  }

  public StreamConverter() {

  }

  public InputStream convert( final IRepositoryFileData data ) {
    throw new UnsupportedOperationException();
  }

  public InputStream convert( final Serializable fileId ) {
    InputStream stream = null;
    if ( repository != null ) {
      SimpleRepositoryFileData fileData = repository.getDataForRead( fileId, SimpleRepositoryFileData.class );
      stream = fileData.getStream();
    }
    return stream;
  }

  public IRepositoryFileData convert( final InputStream inputStream, final String charset, final String mimeType ) {
    return new SimpleRepositoryFileData( inputStream, charset, mimeType );
  }

}
