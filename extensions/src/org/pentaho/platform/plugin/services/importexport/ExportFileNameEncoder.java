package org.pentaho.platform.plugin.services.importexport;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * This class is used to encode and decode file names added to or extracted from an export
 * zip file.
 * 
 * @author tkafalas
 *
 */
public class ExportFileNameEncoder {
  /**
   * Encode a single file or folder name
   * 
   * @param fileName As stored in repo
   * @return fileName suitable for zip file or fileSystem
   */
  public static String encodeZipFileName( String fileName ) {
    try {
      return URLEncoder.encode( fileName, "UTF-8" ).replace( "*" , "%2A" );
    } catch ( UnsupportedEncodingException e ) {
      // Should not happen
      e.printStackTrace();
      return fileName;
    }
  }

  /**
   * Decode the encoded file or folder name back to the orignal repo name.
   * 
   * @param encodedFile/folder Name
   * @return original repo file/folder name
   */
  public static String decodeZipFileName( String encodedFileName ) {
    try {
      return URLDecoder.decode( encodedFileName, "UTF-8" );
    } catch ( UnsupportedEncodingException e ) {
      // should not happen
      e.printStackTrace();
      return encodedFileName;
    }
  }

  /**
   * Encode a full path to a file or path to a folder
   * @param path
   * @return encode path
   */
  public static String encodeZipPathName( String path ) {
    String[] folders = path.split( "/" );
    StringBuilder encodedPath = new StringBuilder( path.length() * 2 );
    for ( int i = 0; i < folders.length; i++ ) {
      encodedPath.append( encodeZipFileName( folders[i] ) );
      if ( i != folders.length - 1 || path.endsWith( "/" ) ) {
        encodedPath.append( "/" );
      }
    }
    return encodedPath.toString();
  }

  /**
   * Decode the path back into the repo path
   * @param encodedPath
   * @return repo path
   */
  public static String decodeZipPath( String encodedPath ) {
    String[] folders = encodedPath.split( "/" );
    StringBuilder decodedPath = new StringBuilder( encodedPath.length() * 2 );
    for ( int i = 0; i < folders.length; i++ ) {
      decodedPath.append( decodeZipFileName( folders[i] ) );
      if ( i != folders.length - 1 || encodedPath.endsWith( "/" ) ) {
        decodedPath.append( "/" );
      }
    }
    return decodedPath.toString();
  }
}
