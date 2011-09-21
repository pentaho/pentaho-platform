/*
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
 */
package org.pentaho.platform.api.repository2.unified.data.simple;

import java.io.IOException;
import java.io.InputStream;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;

/**
 * A {@link IRepositoryFileData} that has an input stream, encoding, and optional MIME type.
 * 
 * @author mlowery
 */
public class SimpleRepositoryFileData implements IRepositoryFileData {

  // ~ Static fields/initializers ======================================================================================

  private static final long serialVersionUID = -1571991472814251230L;

  // ~ Instance fields =================================================================================================

  private InputStream stream;

  private String encoding;

  private String mimeType;

  // ~ Constructors ====================================================================================================

  public SimpleRepositoryFileData(final InputStream stream, final String encoding, final String mimeType) {
    super();
    this.stream = stream;
    this.encoding = encoding;
    this.mimeType = mimeType;
  }

  // ~ Methods =========================================================================================================

  /**
   * Returns a stream for reading the data in this file.
   * 
   * @return stream (may be {@code null})
   */
  public InputStream getStream() {
    return stream;
  }

  /**
   * Returns the character encoding of the bytes in the data stream. May be {@code null} for non-character data.
   * 
   * @return character encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Returns the MIME type of the data in this file.
   * 
   * @return MIME type
   */
  public String getMimeType() {
    return mimeType;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.IRepositoryFileData#getDataSize()
   */
  @Override
  public long getDataSize() {
    try {
      return stream.available();
    } catch (IOException e) {
      return 0;
    }
  }

}
