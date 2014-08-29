/**
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
 * Copyright 2006 - 2014 Pentaho Corporation.  All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.util.Text;

/**
 * Collect JCR string helper methods to centralize implementations
 * These methods are intended for internal use only. However, due to
 * architectural considerations, the methods have been made public
 */
public class JcrStringHelper {

  /**
   *
   * @param fileName
   * @return
   */
  public static String fileNameEncode(String fileName) {
    return ISO9075.encode( Text.escapeIllegalJcrChars( fileName ) );
  }

  public static String idEncode(String id) {
    return Text.escapeIllegalJcrChars( id );
  }

  /**
   *
   * @param encodedFileName
   * @return
   */
  public static String fileNameDecode(String encodedFileName) {
    return ISO9075.decode( Text.unescapeIllegalJcrChars( encodedFileName ) );
  }

  /**
   * May contain just folder names or full paths
   * @param path
   * @return
   */
  public static String pathEncode( String path) {
    String[] folders = path.split("/");
    StringBuilder encodedPath = new StringBuilder(path.length() * 2);
    for (int i=0; i < folders.length; i++) {
      encodedPath.append( fileNameEncode(folders[i]) );
      if ( i != folders.length - 1 || path.endsWith( "/" ) ) {
        encodedPath.append( "/" );
      }
    }
    return encodedPath.toString();
  }

  /**
   * May contain just folder names or full paths
   * @param encodedPath
   * @return
   */
  public static String pathDecode( String encodedPath ) {
    String[] folders = encodedPath.split("/");
    StringBuilder decodedPath = new StringBuilder(encodedPath.length() * 2);
    for (int i=0; i < folders.length; i++) {
      decodedPath.append( fileNameDecode(folders[i]) );
      if ( i != folders.length - 1 || encodedPath.endsWith( "/" ) ) {
        decodedPath.append( "/" );
      }
    }
    return decodedPath.toString();
  }

}
