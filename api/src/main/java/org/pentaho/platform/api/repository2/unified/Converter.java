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


package org.pentaho.platform.api.repository2.unified;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Implementations of this interface are responsible for converting between an {@code InputStream} and an
 * {@link IRepositoryFileData}.
 * 
 * Added support for using just fileId and assume converter encapsulates all necessary logic for getting file data and
 * conversion
 * 
 * @author mlowery
 */
public interface Converter {

  /**
   * 
   * @param inputStream
   * @param charset
   * @param mimeType
   * @return
   */
  IRepositoryFileData convert( final InputStream inputStream, final String charset, final String mimeType ) throws ConverterException;

  /**
   * 
   * @param data
   * @return
   */
  InputStream convert( final IRepositoryFileData data );

  /**
   * 
   * @param fileId
   * @return
   */
  InputStream convert( final Serializable fileId );

  /**
   * Invoked for further processing of content after it was saved to repository.
   *
   * @param file repository file where content was stored in.
   */
  default void convertPostRepoSave( RepositoryFile file ) {
  }
}
