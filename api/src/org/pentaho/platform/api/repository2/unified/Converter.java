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
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

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
  IRepositoryFileData convert( final InputStream inputStream, final String charset, final String mimeType );

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
