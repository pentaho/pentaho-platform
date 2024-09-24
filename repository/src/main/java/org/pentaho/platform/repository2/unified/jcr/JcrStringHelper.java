/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.apache.jackrabbit.util.Text;
import org.apache.jackrabbit.util.ISO9075;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Collect JCR string helper methods to centralize implementations These methods are intended for internal use only.
 * However, due to architectural considerations, the methods have been made public
 */
public class JcrStringHelper {

  private static boolean useMultiByteEncoding = false;
  private static boolean multiByteValueInitialized = false;
  private static final String SEPARATOR = "/";

  private JcrStringHelper() {
  }

  /**
   * 
   * @param fileName
   * @return
   */
  public static String fileNameEncode( String fileName ) {
    return fileNameEncode( fileName, isMultiByteEncodingEnabled() );
  }

  /**
   * 
   * @param fileName
   * @param useMultiByte
   * @return
   */
  public static String fileNameEncode( String fileName, boolean useMultiByte ) {
    if ( useMultiByte ) {
      return Text.escapeIllegalJcrChars( ISO9075.encode( fileName ) );
    } else {
      return Text.escapeIllegalJcrChars( fileName );
    }
  }

  public static String idEncode( String id ) {
    return Text.escapeIllegalJcrChars( id );
  }

  /**
   * 
   * @param encodedFileName
   * @return
   */
  public static String fileNameDecode( String encodedFileName ) {
    return fileNameDecode( encodedFileName, isMultiByteEncodingEnabled() );
  }

  /**
   * 
   * @param encodedFileName
   * @param useMultiByte
   * @return
   */
  public static String fileNameDecode( String encodedFileName, boolean useMultiByte ) {
    if ( useMultiByte ) {
      return Text.unescapeIllegalJcrChars( ISO9075.decode( encodedFileName ) );
    } else {
      return Text.unescapeIllegalJcrChars( encodedFileName );
    }
  }

  /**
   * May contain just folder names or full paths
   * 
   * @param path
   * @return
   */
  public static String pathEncode( String path ) {
    String[] folders = path.split( SEPARATOR );
    StringBuilder encodedPath = new StringBuilder( path.length() * 2 );
    for ( int i = 0; i < folders.length; i++ ) {
      encodedPath.append( fileNameEncode( folders[i] ) );
      if ( i != folders.length - 1 || path.endsWith( SEPARATOR ) ) {
        encodedPath.append( SEPARATOR );
      }
    }
    return encodedPath.toString();
  }

  /**
   * May contain just folder names or full paths
   * 
   * @param path
   * @param useMultiByte
   * @return
   */
  public static String pathEncode( String path, boolean useMultiByte ) {
    String[] folders = path.split( SEPARATOR );
    StringBuilder encodedPath = new StringBuilder( path.length() * 2 );
    for ( int i = 0; i < folders.length; i++ ) {
      encodedPath.append( fileNameEncode( folders[i], useMultiByte ) );
      if ( i != folders.length - 1 || path.endsWith( SEPARATOR ) ) {
        encodedPath.append( SEPARATOR );
      }
    }
    return encodedPath.toString();
  }

  /**
   * May contain just folder names or full paths
   * 
   * @param encodedPath
   * @return
   */
  public static String pathDecode( String encodedPath ) {
    String[] folders = encodedPath.split( SEPARATOR );
    StringBuilder decodedPath = new StringBuilder( encodedPath.length() * 2 );
    for ( int i = 0; i < folders.length; i++ ) {
      decodedPath.append( fileNameDecode( folders[ i ] ) );
      if ( i != folders.length - 1 || encodedPath.endsWith( SEPARATOR ) ) {
        decodedPath.append( SEPARATOR );
      }
    }
    return decodedPath.toString();
  }

  /**
   * May contain just folder names or full paths
   * 
   * @param encodedPath
   * @param useMultiByte
   * @return
   */
  public static String pathDecode( String encodedPath, boolean useMultiByte ) {
    String[] folders = encodedPath.split( SEPARATOR );
    StringBuilder decodedPath = new StringBuilder( encodedPath.length() * 2 );
    for ( int i = 0; i < folders.length; i++ ) {
      decodedPath.append( fileNameDecode( folders[i], useMultiByte ) );
      if ( i != folders.length - 1 || encodedPath.endsWith( SEPARATOR ) ) {
        decodedPath.append( SEPARATOR );
      }
    }
    return decodedPath.toString();
  }

  public static boolean isMultiByteEncodingEnabled() {
    if ( !multiByteValueInitialized && PentahoSystem.getInitializedOK() ) {
      Boolean setting =
        PentahoSystem.get( Boolean.class, "useMultiByteEncoding", PentahoSessionHolder.getSession() );
      if ( setting == null ) {
        useMultiByteEncoding = false;
      } else {
        useMultiByteEncoding = setting;
      }
      multiByteValueInitialized = true;
    }
    return useMultiByteEncoding;
  }

  /**
   *
   * @param useMultiByteEncoding
   *
   */
  public static void setMultiByteEncodingEnabled( boolean useMultiByteEncoding ) {
    JcrStringHelper.useMultiByteEncoding = useMultiByteEncoding;
  }

  /**
   *
   * Assume that path is encoded and try to decode it.
   * If it changes from original, it is already encoded,
   * else - it is not.
   * @param path
   *          path, needed to be evaluated
   * @return
   *      true if path is already encoded,
   *      false otherwise
   *
   */
  public static boolean isEncoded( String path ) {
    return !path.equals( pathDecode( path ) );
  }

}
