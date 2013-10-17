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

package org.pentaho.platform.web.servlet;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.servlet.messages.Messages;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class UploadFileUtils {

  private static final long MAX_FILE_SIZE = 300000;
  private static final long MAX_FOLDER_SIZE = 3000000;
  public static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH = File.separatorChar
      + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles" + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
  private String fileName;
  private boolean shouldUnzip;
  private boolean temporary;
  private Writer writer;
  private FileItem uploadedItem;
  private IPentahoSession session;
  private long maxFileSize;
  private long maxFolderSize;
  private String relativePath;
  private String path;
  private File pathDir;

  public UploadFileUtils( IPentahoSession sessionValue ) {
    this.session = sessionValue;
    relativePath =
        PentahoSystem.getSystemSetting(
            "file-upload-defaults/relative-path", String.valueOf( DEFAULT_RELATIVE_UPLOAD_FILE_PATH ) ); //$NON-NLS-1$ 
    String maxFileLimit =
        PentahoSystem.getSystemSetting( "file-upload-defaults/max-file-limit", String.valueOf( MAX_FILE_SIZE ) ); //$NON-NLS-1$    
    String maxFolderLimit =
        PentahoSystem.getSystemSetting( "file-upload-defaults/max-folder-limit", String.valueOf( MAX_FOLDER_SIZE ) ); //$NON-NLS-1$
    this.maxFileSize = Long.parseLong( maxFileLimit );
    this.maxFolderSize = Long.parseLong( maxFolderLimit );
    path = PentahoSystem.getApplicationContext().getSolutionPath( relativePath );
    pathDir = new File( path );
    // create the path if it doesn't exist yet
    if ( !pathDir.exists() ) {
      pathDir.mkdirs();
    }
  }

  public boolean process() throws Exception {
    if ( !checkLimits( getUploadedFileItem().getSize() ) ) {
      return false;
    }

    File file = null;
    if ( isTemporary() ) {
      file = PentahoSystem.getApplicationContext().createTempFile( session, "", ".tmp", true ); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      file = new File( getPath() + File.separatorChar + fileName );
      // Check that it's where it belongs - prevent ../../.. attacks.
      String cp = file.getCanonicalPath();
      String relPath = getPathDir().getCanonicalPath();
      if ( !cp.startsWith( relPath ) ) {
        // Trying to upload outside of folder.
        getWriter()
            .write( Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0008_FILE_LOCATION_INVALID" ) ); //$NON-NLS-1$
        return false;
      }
    }

    InputStream itemInputStream = getUploadedFileItem().getInputStream();
    try {
      OutputStream outputStream = new BufferedOutputStream( new FileOutputStream( file ) );
      try {
        IOUtils.copy( itemInputStream, outputStream );
      } finally {
        IOUtils.closeQuietly( outputStream ); // note - close calls flush.
      }
    } finally {
      IOUtils.closeQuietly( itemInputStream );
    }
    getUploadedFileItem().delete(); // Forcibly deletes temp file - now WE track it.

    if ( shouldUnzip ) {
      return handleUnzip( file );
    } else {
      writer.write( file.getName() );
    }
    return true;
  }

  protected boolean handleUnzip( File file ) throws IOException {
    String fileNames = file.getName();

    // .zip/.tar/.gz/.tgz files are always considered temporary and deleted on session expire

    ITempFileDeleter fileDeleter = null;
    if ( ( session != null ) ) {
      fileDeleter = (ITempFileDeleter) session.getAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE );
      if ( fileDeleter != null ) {
        fileDeleter.trackTempFile( file ); // make sure the deleter knows to clean this puppy up...
      }
    }

    if ( ( getUploadedFileItem().getName().toLowerCase().endsWith( ".zip" ) || getUploadedFileItem().getContentType()
        .equals( "application/zip" ) ) ) { //$NON-NLS-1$ //$NON-NLS-2$
      // handle a zip
      if ( checkLimits( getUncompressedZipFileSize( file ), true ) ) {
        fileNames = handleZip( file );
      } else {
        file.delete(); // delete immediately (see requirements on BISERVER-4321)
        return false;
      }
    } else if ( ( getUploadedFileItem().getName().toLowerCase().endsWith( ".tgz" ) || //$NON-NLS-1$
        getUploadedFileItem().getName().toLowerCase().endsWith( ".tar.gz" ) || //$NON-NLS-1$
        getUploadedFileItem().getContentType().equals( "application/x-compressed" ) || getUploadedFileItem().getContentType().equals( "application/tgz" ) ) ) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      // handle a tgz
      long tarSize = getUncompressedGZipFileSize( file );
      if ( checkLimits( tarSize, true ) ) {
        if ( isTemporary() || checkLimits( tarSize * 2, true ) ) {
          fileNames = handleTarGZ( file );
        } else {
          return false;
        }
      } else {
        file.delete(); // delete immediately (see requirements on BISERVER-4321)
        return false;
      }
    } else if ( ( getUploadedFileItem().getName().toLowerCase().endsWith( ".gzip" ) || getUploadedFileItem().getName().toLowerCase().endsWith( ".gz" ) ) ) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      // handle a gzip
      if ( checkLimits( getUncompressedGZipFileSize( file ), true ) ) {
        fileNames = handleGZip( file, false );
      } else {
        file.delete(); // delete immediately (see requirements on BISERVER-4321)
        return false;
      }
    } else if ( ( getUploadedFileItem().getName().toLowerCase().endsWith( ".tar" ) || getUploadedFileItem().getContentType().equals( "application/x-tar" ) ) ) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      // handle a tar
      //
      // Note - after the .tar file lands on the file system, you have to
      // unpack it which means it is again the size of the file. So, we check
      // disk space before putting the .tar onto the disk. Then, after the .tar
      // hits disk, we check the size AGAIN because untarring the file will
      // amount to double the size of the file. If isTemporary is checked, then
      // we don't need to worry about this since the .tar file will be deleted.
      // Marc
      if ( isTemporary() || checkLimits( getUploadedFileItem().getSize(), true ) ) {
        fileNames = handleTar( file );
      } else {
        file.delete(); // delete immediately (see requirements on BISERVER-4321)
        return false;
      }
    }
    // else - just outputs the file name.
    writer.write( fileNames );
    return true;
  }

  /**
   * Gets the uncompressed file size of a .zip file.
   * 
   * @param theFile
   * @return long uncompressed file size.
   * @throws IOException
   *           mbatchelor
   */
  private long getUncompressedZipFileSize( File theFile ) throws IOException {
    long rtn = 0;
    ZipFile zf = new ZipFile( theFile );
    try {
      Enumeration<? extends ZipEntry> zfEntries = zf.entries();
      ZipEntry ze = null;
      while ( zfEntries.hasMoreElements() ) {
        ze = zfEntries.nextElement();
        rtn += ze.getSize();
      }
    } finally {
      try {
        zf.close();
      } catch ( Exception ignored ) {
        //ignored
      }
    }
    return rtn;
  }

  /**
   * Gets the uncompressed file size of a .gz file by reading the last four bytes of the file
   * 
   * @param file
   * @return long uncompressed original file size
   * @throws IOException
   *           mbatchelor
   */
  private long getUncompressedGZipFileSize( File file ) throws IOException {
    long rtn = 0;
    RandomAccessFile gzipFile = new RandomAccessFile( file, "r" );
    try {
      // go 4 bytes from end of file - the original uncompressed file size is there
      gzipFile.seek( gzipFile.length() - 4 );
      byte[] intelSize = new byte[4];
      gzipFile.read( intelSize ); // read the size ....
      // rfc1952; ISIZE is the input size modulo 2^32
      // 00F01E69 is really 691EF000
      // The &0xFF turns signed byte into unsigned.
      rtn =
          ( ( ( intelSize[3] & 0xFF ) << 24 ) | ( ( intelSize[2] & 0xFF ) << 16 ) + ( ( intelSize[1] & 0xFF ) << 8 )
              + ( intelSize[0] & 0xFF ) ) & 0xffffffffL;
    } finally {
      try {
        gzipFile.close();
      } catch ( Exception ignored ) {
        //ignored
      }
    }
    return rtn;
  }

  /**
   * Decompress a zip file and return a list of the file names that were unpacked
   * 
   * @param file
   * @param session
   * @return
   * @throws IOException
   */
  protected String handleZip( File file ) throws IOException {
    StringBuilder sb = new StringBuilder();
    FileInputStream fileStream = new FileInputStream( file.getAbsolutePath() );
    try {
      // create a zip input stream from the tmp file that was uploaded
      ZipInputStream zipStream = new ZipInputStream( new BufferedInputStream( fileStream ) );
      try {
        ZipEntry entry = zipStream.getNextEntry();
        // iterate thru the entries in the zip file
        while ( entry != null ) {

          // ignore hidden directories and files, extract the rest
          if ( !entry.isDirectory() && !entry.getName().startsWith( "." ) && !entry.getName().startsWith( "__MACOSX/" ) ) { //$NON-NLS-1$ //$NON-NLS-2$

            File entryFile = null;
            if ( isTemporary() ) {
              String extension = ".tmp"; //$NON-NLS-1$
              int idx = entry.getName().lastIndexOf( '.' );
              if ( idx != -1 ) {
                extension = entry.getName().substring( idx ) + extension;
              }
              entryFile = PentahoSystem.getApplicationContext().createTempFile( session, "", extension, true ); //$NON-NLS-1$
            } else {
              entryFile = new File( getPath() + File.separatorChar + entry.getName() );
            }

            if ( sb.length() > 0 ) {
              sb.append( "\n" ); //$NON-NLS-1$
            }
            sb.append( entryFile.getName() );
            FileOutputStream entryOutputStream = new FileOutputStream( entryFile );
            try {
              IOUtils.copy( zipStream, entryOutputStream );
            } finally {
              IOUtils.closeQuietly( entryOutputStream );
            }
          }
          // go on to the next entry
          entry = zipStream.getNextEntry();
        }
      } finally {
        IOUtils.closeQuietly( zipStream );
      }
    } finally {
      IOUtils.closeQuietly( fileStream );
    }
    return sb.toString();

  }

  protected String handleGZip( File file, boolean fullPath ) throws IOException {
    FileInputStream fileStream = new FileInputStream( file.getAbsolutePath() );

    try {
      // create a gzip input stream from the tmp file that was uploaded
      GZIPInputStream zipStream = new GZIPInputStream( new BufferedInputStream( fileStream ) );

      File entryFile = null;
      if ( isTemporary() || fullPath ) {
        entryFile = PentahoSystem.getApplicationContext().createTempFile( session, "", ".tmp", true ); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        String gzFile = file.getCanonicalPath();
        int idx = gzFile.lastIndexOf( '.' );
        if ( idx > 0 ) {
          entryFile = new File( gzFile.substring( 0, idx ) ); // Cut off the .gz/.gzip part.
        } else {
          // Odd - someone specified the name as .gz or .gzip... create a temp file (for naming)
          // Note - not added to deleter because it's a file that should stay around - it's CSV data
          File parentFolder = file.getParentFile();
          entryFile = File.createTempFile( "upload_gzip", ".tmp", parentFolder ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
      try {
        FileOutputStream entryOutputStream = new FileOutputStream( entryFile );
        try {
          IOUtils.copy( zipStream, entryOutputStream );
        } finally {
          IOUtils.closeQuietly( entryOutputStream );
        }
      } finally {
        IOUtils.closeQuietly( zipStream );
      }

      if ( fullPath ) {
        return entryFile.getCanonicalPath();
      } else {
        return entryFile.getName();
      }
    } finally {
      IOUtils.closeQuietly( fileStream );
    }
  }

  protected String handleTar( File file ) throws IOException {

    // now extract the tar files
    StringBuilder sb = new StringBuilder();
    FileInputStream fileStream = new FileInputStream( file );
    try {
      // create a zip input stream from the tmp file that was uploaded
      TarInputStream zipStream = new TarInputStream( new BufferedInputStream( fileStream ) );
      try {
        TarEntry entry = zipStream.getNextEntry();
        // iterate thru the entries in the zip file
        while ( entry != null ) {
          // ignore hidden directories and files, extract the rest
          if ( !entry.isDirectory() && !entry.getName().startsWith( "." ) && !entry.getName().startsWith( "__MACOSX/" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
            File entryFile = null;
            if ( isTemporary() ) {
              String extension = ".tmp"; //$NON-NLS-1$
              int idx = entry.getName().lastIndexOf( '.' );
              if ( idx != -1 ) {
                extension = entry.getName().substring( idx ) + extension;
              }
              entryFile = PentahoSystem.getApplicationContext().createTempFile( session, "", extension, true ); //$NON-NLS-1$ 
            } else {
              entryFile = new File( getPath() + File.separatorChar + entry.getName() );
            }

            if ( sb.length() > 0 ) {
              sb.append( "\n" ); //$NON-NLS-1$
            }
            sb.append( entryFile.getName() );
            FileOutputStream entryOutputStream = new FileOutputStream( entryFile );
            try {
              IOUtils.copy( zipStream, entryOutputStream );
            } finally {
              IOUtils.closeQuietly( entryOutputStream );
            }
          }
          // go on to the next entry
          entry = zipStream.getNextEntry();
        }
      } finally {
        IOUtils.closeQuietly( zipStream );
      }

    } finally {
      IOUtils.closeQuietly( fileStream );
    }
    return sb.toString();
  }

  protected String handleTarGZ( File file ) throws IOException {

    // first extract the gz
    String filename = handleGZip( file, true );

    File tarFile = new File( filename );

    ITempFileDeleter fileDeleter;
    if ( ( session != null ) ) {
      fileDeleter = (ITempFileDeleter) session.getAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE );
      if ( fileDeleter != null ) {
        fileDeleter.trackTempFile( tarFile ); // make sure the deleter knows to clean this puppy up...
      }
    }

    return handleTar( tarFile );
  }

  public boolean checkLimits( long itemSize ) throws IOException {
    return checkLimits( itemSize, false );
  }

  public boolean checkLimits( long itemSize, boolean compressed ) throws IOException {
    if ( itemSize > maxFileSize ) {
      String error = compressed ? Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0006_FILE_TOO_BIG" ) //$NON-NLS-1$
          : Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0003_FILE_TOO_BIG" ); //$NON-NLS-1$
      writer.write( error );
      return false;
    }

    if ( itemSize + getFolderSize( pathDir ) > maxFolderSize ) {
      String error =
          compressed ? Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0007_FOLDER_SIZE_LIMIT_REACHED" ) //$NON-NLS-1$
              : Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0004_FOLDER_SIZE_LIMIT_REACHED" ); //$NON-NLS-1$ 
      writer.write( error );
      return false;
    }
    return true;
  }

  private long getFolderSize( File folder ) {
    long foldersize = 0;
    File[] filelist = folder.listFiles();
    for ( int i = 0; i < filelist.length; i++ ) {
      if ( filelist[i].isDirectory() ) {
        foldersize += getFolderSize( filelist[i] );
      } else {
        foldersize += filelist[i].length();
      }
    }
    return foldersize;
  }

  /******************* Getters and Setters ********************/

  public String getFileName() {
    return fileName;
  }

  public void setFileName( String value ) {
    this.fileName = value;
  }

  public boolean isShouldUnzip() {
    return shouldUnzip;
  }

  public void setShouldUnzip( boolean value ) {
    this.shouldUnzip = value;
  }

  public boolean isTemporary() {
    return temporary;
  }

  public void setTemporary( boolean value ) {
    this.temporary = value;
  }

  public void setWriter( Writer value ) {
    this.writer = value;
  }

  public Writer getWriter() {
    return this.writer;
  }

  public void setUploadedFileItem( FileItem value ) {
    this.uploadedItem = value;
  }

  public FileItem getUploadedFileItem() {
    return this.uploadedItem;
  }

  public String getPath() {
    return this.path;
  }

  public File getPathDir() {
    return this.pathDir;
  }

  public String getRelativePath() {
    return this.relativePath;
  }

}
