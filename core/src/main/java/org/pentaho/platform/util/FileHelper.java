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

package org.pentaho.platform.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.util.messages.Messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class FileHelper {

  private static final Log logger = LogFactory.getLog( FileHelper.class );

  // private static final int BUFF_SZ = 1024;

  /**
   * 
   * Note: wrap the InputStreamReader in a BufferedReader for efficiency. See:
   * http://java.sun.com/j2se/1.4.2/docs/api/java/io/InputStreamReader.html
   * 
   * @param is
   * @return
   */
  public static String getStringFromInputStream( final InputStream is ) {

    try {
      return IOUtils.toString( is );
    } catch ( IOException e ) {
      logger.error( Messages.getInstance().getErrorString( "FileUtil.ERROR_0001_ERROR", e.getMessage() ), e ); //$NON-NLS-1$
      return null;
    }
  }

  public static String getStringFromFile( final File f ) {
    FileInputStream fin = null;
    try {
      fin = new FileInputStream( f );
      return FileHelper.getStringFromInputStream( fin );
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getErrorString( "FileUtil.ERROR_0001_ERROR", e.getMessage() ), e ); //$NON-NLS-1$
    } finally {
      FileHelper.closeInputStream( fin );
    }
    return null;
  }

  public static byte[] getBytesFromFile( final File file ) throws IOException {
    InputStream is = new FileInputStream( file );
    try {
      // Create the byte array to hold the data
      byte[] bytes = IOUtils.toByteArray( is );
      return bytes;
    } finally {
      IOUtils.closeQuietly( is );
    }
  }

  /**
   * Convenience method to close an input stream and handle (log and throw away) any exceptions. Helps keep code
   * uncluttered.
   * 
   * @param strm
   *          InputStream to be closed
   */
  public static void closeInputStream( final InputStream strm ) {
    if ( null != strm ) {
      try {
        strm.close();
      } catch ( IOException e ) {
        logger.warn( Messages.getInstance().getString( "FileHelper.WARN_ERROR_CLOSING_STREAM" ), e ); //$NON-NLS-1$
      }
    }
  }

  /**
   * Convenience method to close a Reader and handle (log and throw away) any exceptions. Helps keep code
   * uncluttered.
   * 
   * @param rdr
   *          InputSReadertream to be closed
   */
  public static void closeReader( final Reader rdr ) {
    if ( null != rdr ) {
      try {
        rdr.close();
      } catch ( IOException e ) {
        logger.warn( Messages.getInstance().getString( "FileHelper.WARN_ERROR_CLOSING_READER" ), e ); //$NON-NLS-1$
      }
    }
  }
}
