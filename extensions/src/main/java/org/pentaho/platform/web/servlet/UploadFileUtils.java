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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.servlet;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.http.Part;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class UploadFileUtils {

  private static final long MAX_FILE_SIZE = 10000000; // about 9m
  private static final long MAX_FOLDER_SIZE = 500000000; // about 476mb
  private static final long MAX_TMP_FOLDER_SIZE = 500000000; // about 476mb
  private static final String DEFAULT_EXTENSIONS = "csv,dat,txt,tar,zip,tgz,gz,gzip";
  public static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH = File.separatorChar
    + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles" + File.separatorChar;
  private static final char DOT = '.';
  private static final String DOT_TMP = ".tmp";
  private String fileName;
  private boolean shouldUnzip;
  private boolean temporary;
  private Writer writer;
  private Part uploadedPart;
  private IPentahoSession session;
  private long maxFileSize;
  private long maxFolderSize;
  private long maxTmpFolderSize;
  private String relativePath;
  private String path;
  private File pathDir;
  private File tmpPathDir;
  private Set<String> allowedExtensions;
  private String allowedExtensionsString;
  private boolean allowsNoExtension;

  public UploadFileUtils( IPentahoSession sessionValue ) {
    this.session = sessionValue;

    relativePath =
      PentahoSystem.getSystemSetting( "file-upload-defaults/relative-path", DEFAULT_RELATIVE_UPLOAD_FILE_PATH );

    String maxFileLimit =
      PentahoSystem.getSystemSetting( "file-upload-defaults/max-file-limit", String.valueOf( MAX_FILE_SIZE ) );

    String maxFolderLimit =
      PentahoSystem.getSystemSetting( "file-upload-defaults/max-folder-limit", String.valueOf( MAX_FOLDER_SIZE ) );

    // PPP-3629
    String maxTmpFolderLimit =
      PentahoSystem.getSystemSetting( "file-upload-defaults/max-tmp-folder-limit",
        String.valueOf( MAX_TMP_FOLDER_SIZE ) );

    // PPP-3630
    String tmpAllowedExtensions =
      PentahoSystem.getSystemSetting( "file-upload-defaults/allowed-extensions", DEFAULT_EXTENSIONS );
    this.setAllowedExtensionsString( tmpAllowedExtensions );
    // Are files without any extension allowed ? Notably found in .zip files and such.
    String allowsNoExtensionString =
      PentahoSystem.getSystemSetting( "file-upload-defaults/allow-files-without-extension", "false" );

    this.allowsNoExtension = Boolean.parseBoolean( allowsNoExtensionString );
    this.maxFileSize = Long.parseLong( maxFileLimit );
    this.maxFolderSize = Long.parseLong( maxFolderLimit );
    this.maxTmpFolderSize = Long.parseLong( maxTmpFolderLimit );
  }

  /**
   * <p>Checks if the given extension is supported.</p>
   * <p>A <code>null</code> is an invalid extension; to check if a NoExtension is allowed, an empty string must be
   * given.</p>
   *
   * @param extension   the extension to check
   * @param emitMessage if a message is to be written when an error occurs
   * @return <code>true</code> if the given extension is supported, <code>false</code> if it's an invalid
   * or unsupported extension
   * @throws IOException
   */
  protected boolean checkExtension( String extension, boolean emitMessage ) throws IOException {
    if ( null == extension ) {
      if ( emitMessage ) {
        getWriter()
          .write( Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0010_FILE_NAME_INVALID" ) );
      }
      return false;
    }

    if ( extension.isEmpty() ) {
      if ( !allowsNoExtension && emitMessage ) {
        getWriter()
          .write( Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0010_FILE_NAME_INVALID" ) );
      }
      return allowsNoExtension;
    }

    if ( !allowedExtensions.contains( extension ) ) {
      if ( emitMessage ) {
        getWriter()
          .write( Messages.getInstance()
            .getErrorString( "UploadFileServlet.ERROR_0011_ILLEGAL_FILE_TYPE", this.allowedExtensionsString ) );
      }
      return false;
    }

    return true;
  }

  public boolean process() throws Exception {
    path = PentahoSystem.getApplicationContext().getSolutionPath( relativePath );
    pathDir = new File( path );
    // create the path if it doesn't exist yet
    if ( !pathDir.exists() ) {
      pathDir.mkdirs();
    }

    // Handle PPP-3630 - check size of tmp folder too...
    tmpPathDir = new File( PentahoSystem.getApplicationContext().getSolutionPath( "system/tmp" ) );
    // Create tmp path if it doesn't exist yet
    if ( !tmpPathDir.exists() ) {
      tmpPathDir.mkdirs();
    }

    if ( !checkLimits( getUploadedPart().getSize() ) ) {
      return false;
    }

    if ( this.fileName == null ) {
      getWriter()
        .write( Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0010_FILE_NAME_INVALID" ) );
      return false;
    }

    if ( !checkExtension( FilenameUtils.getExtension( this.fileName ), true ) ) {
      return false;
    }

    boolean res = process( getUploadedPart().getInputStream() );
    getUploadedPart().delete(); // Forcibly deletes temp file - now WE track it.
    return res;
  }

  /**
   * process uploading using inputStream instead of UploadedFileItem do not support unzipping
   *
   * @param inputStream
   * @return <code>true</code> if the processing finished successfully and <code>false</code> otherwise
   * @throws Exception
   */
  public boolean process( InputStream inputStream ) throws Exception {
    if ( inputStream == null ) {
      return false;
    }

    File file = null;
    if ( isTemporary() ) {
      // Use the full filename because GZip relies on the extensions of the file to discover it's content
      String extension = ( getUploadedPart() != null )
        ? DOT + removeFileName( getUploadedPart().getSubmittedFileName() ) + DOT_TMP
        : DOT_TMP;
      file =
        PentahoSystem.getApplicationContext()
          .createTempFile( session, StringUtil.EMPTY_STRING, extension, true );
    } else {
      file = new File( getPath() + File.separatorChar + fileName );
      // Check that it's where it belongs - prevent ../../.. attacks.
      String cp = file.getCanonicalPath();
      String relPath = getPathDir().getCanonicalPath();
      if ( !cp.startsWith( relPath ) ) {
        // Trying to upload outside of folder.
        getWriter()
          .write( Messages.getInstance()
            .getErrorString( "UploadFileServlet.ERROR_0008_FILE_LOCATION_INVALID" ) );
        return false;
      }
    }

    try {
      OutputStream outputStream = new BufferedOutputStream( new FileOutputStream( file ) );
      try {
        IOUtils.copy( inputStream, outputStream );
      } finally {
        IOUtils.closeQuietly( outputStream ); // note - close calls flush.
      }
    } finally {
      IOUtils.closeQuietly( inputStream );
    }

    if ( shouldUnzip && getUploadedPart() != null ) {
      return handleUnzip( file );
    } else {
      writer.write( file.getName() );
    }
    return true;
  }

  protected boolean handleUnzip( File file ) throws IOException {
    String fileNames = file.getName();

    // .zip/.tar/.gz/.tgz files are always considered temporary and deleted on session expire

    if ( session != null ) {
      ITempFileDeleter fileDeleter =
        (ITempFileDeleter) session.getAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE );
      if ( fileDeleter != null ) {
        fileDeleter.trackTempFile( file ); // make sure the deleter knows to clean this puppy up...
      }
    }

    String fileNameLowerCase = getUploadedPart().getSubmittedFileName().toLowerCase();
    String extension = FilenameUtils.getExtension( fileNameLowerCase );
    String contentType = getUploadedPart().getContentType();

    if ( "zip".equals( extension ) || "application/zip".equals( contentType ) ) {
      // handle a zip
      if ( checkLimits( getUncompressedZipFileSize( file ), true ) ) {
        fileNames = handleZip( file );
      } else {
        file.delete(); // delete immediately (see requirements on BISERVER-4321)
        return false;
      }
    } else if ( "tgz".equals( extension )
      || fileNameLowerCase.endsWith( ".tar.gz" )
      || "application/x-compressed".equals( contentType )
      || "application/tgz".equals( contentType ) ) {
      // handle a tgz
      long tarSize = getUncompressedGZipFileSize( file );
      if ( checkLimits( tarSize, true )
        && ( isTemporary() || checkLimits( tarSize * 2, true ) ) ) {
        fileNames = handleTarGZ( file );
      } else {
        file.delete(); // delete immediately (see requirements on BISERVER-4321)
        return false;
      }
    } else if ( "gzip".equals( extension ) || "gz".equals( extension ) ) {
      // handle a gzip
      if ( checkLimits( getUncompressedGZipFileSize( file ), true ) ) {
        fileNames = handleGZip( file, false );
      } else {
        file.delete(); // delete immediately (see requirements on BISERVER-4321)
        return false;
      }
    } else if ( "tar".equals( extension ) || "application/x-tar".equals( contentType ) ) {
      // handle a tar
      //
      // Note - after the .tar file lands on the file system, you have to
      // unpack it which means it is again the size of the file. So, we check
      // disk space before putting the .tar onto the disk. Then, after the .tar
      // hits disk, we check the size AGAIN because untarring the file will
      // amount to double the size of the file. If isTemporary is checked, then
      // we don't need to worry about this since the .tar file will be deleted.
      // Marc
      if ( isTemporary() || checkLimits( getUploadedPart().getSize(), true ) ) {
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
   * @param theFile the zip file
   * @return a long representing the uncompressed file size.
   * @throws IOException
   * @see #getUncompressedGZipFileSize(File)
   */
  private long getUncompressedZipFileSize( File theFile ) throws IOException {
    long rtn = 0;
    try ( ZipFile zf = new ZipFile( theFile ) ) {
      Enumeration<? extends ZipEntry> zfEntries = zf.entries();
      ZipEntry ze = null;
      while ( zfEntries.hasMoreElements() ) {
        ze = zfEntries.nextElement();
        rtn += ze.getSize();
      }
    }

    return rtn;
  }

  /**
   * Gets the uncompressed file size of a .gz file by reading the last four bytes of the file.
   *
   * @param theFile the gzip file
   * @return a long representing the uncompressed file size.
   * @throws IOException
   * @see #getUncompressedZipFileSize(File)
   */
  private long getUncompressedGZipFileSize( File theFile ) throws IOException {
    long rtn = 0;
    try ( RandomAccessFile gzipFile = new RandomAccessFile( theFile, "r" ) ) {
      // go 4 bytes from end of file - the original uncompressed file size is there
      gzipFile.seek( gzipFile.length() - 4 );
      byte[] intelSize = new byte[ 4 ];
      gzipFile.read( intelSize ); // read the size ....
      // rfc1952; ISIZE is the input size modulo 2^32
      // 00F01E69 is really 691EF000
      // The &0xFF turns signed byte into unsigned.
      rtn =
        ( ( ( intelSize[ 3 ] & 0xFF ) << 24 ) | ( ( intelSize[ 2 ] & 0xFF ) << 16 ) + ( ( intelSize[ 1 ] & 0xFF ) << 8 )
          + ( intelSize[ 0 ] & 0xFF ) ) & 0xffffffffL;
    }

    return rtn;
  }

  /**
   * Decompress a zip file and return a list of the file names that were unpacked
   *
   * @param file
   * @return
   * @throws IOException
   */
  protected String handleZip( File file ) throws IOException {
    StringBuilder sb = new StringBuilder();
    FileInputStream fileStream = new FileInputStream( file );
    try {
      // create a zip input stream from the tmp file that was uploaded
      ZipInputStream zipStream = new ZipInputStream( new BufferedInputStream( fileStream ) );
      try {
        ZipEntry entry = zipStream.getNextEntry();
        // iterate through the entries in the zip file
        while ( entry != null ) {
          // ignore hidden directories and files, extract the rest
          if ( !entry.isDirectory() && !entry.getName().startsWith( "." )
            && !entry.getName().startsWith( "__MACOSX/" ) ) {

            File entryFile = null;

            String extension = FilenameUtils.getExtension( entry.getName() );
            if ( checkExtension( extension, false ) ) {
              boolean isDestinationValid = true;

              if ( isTemporary() ) {
                entryFile =
                  PentahoSystem.getApplicationContext()
                    .createTempFile( session, StringUtil.EMPTY_STRING, DOT + extension + DOT_TMP, true );
              } else {
                File destination = new File( getPath() + File.separatorChar );
                entryFile = new File( destination, entry.getName() );
                isDestinationValid = validateZipSlip( entryFile, destination );
              }

              if ( isDestinationValid ) {
                if ( sb.length() > 0 ) {
                  sb.append( '\n' );
                }

                sb.append( entryFile.getName() );
                FileOutputStream entryOutputStream = new FileOutputStream( entryFile );

                try {
                  IOUtils.copy( zipStream, entryOutputStream );
                } finally {
                  IOUtils.closeQuietly( entryOutputStream );
                }
              }
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
    if ( sb.length() > 0 ) {
      return sb.toString();
    } else {
      // no valid entries in the zip - nothing unzipped
      return Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0012_ILLEGAL_CONTENTS" );
    }
  }

  protected String handleGZip( File file, boolean fullPath ) throws IOException {
    FileInputStream fileStream = new FileInputStream( file );

    try {
      // Find the real extension (the one of the compressed file)
      String gzFile = file.getCanonicalPath();

      // First: if this is a temporary file, ignore this extension
      if ( FilenameUtils.isExtension( gzFile, "tmp" ) ) {
        gzFile = FilenameUtils.removeExtension( gzFile );
      }
      // Second: this is the .gz/.gzip part: ignore it
      gzFile = FilenameUtils.removeExtension( gzFile );
      // We now have the real extension
      String extension = FilenameUtils.getExtension( gzFile );
      if ( !checkExtension( extension, true ) ) {
        return StringUtil.EMPTY_STRING;
      }

      File entryFile = null;
      if ( isTemporary() || fullPath ) {
        entryFile =
          PentahoSystem.getApplicationContext()
            .createTempFile( session, StringUtil.EMPTY_STRING, DOT + extension + DOT_TMP, true );
      } else {
        if ( !extension.isEmpty() ) {
          entryFile = new File( extension );
        } else {
          // Odd - someone specified the name as .gz or .gzip... create a temp file (for naming)
          // Note - not added to deleter because it's a file that should stay around - it's CSV data
          File parentFolder = file.getParentFile();
          entryFile = File.createTempFile( "upload_gzip", DOT_TMP, parentFolder );
        }
      }

      // create a gzip input stream from the tmp file that was uploaded
      GZIPInputStream zipStream = new GZIPInputStream( new BufferedInputStream( fileStream ) );

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
        // iterate through the entries in the zip file
        while ( entry != null ) {
          // ignore hidden directories and files, extract the rest
          if ( !entry.isDirectory() && !entry.getName().startsWith( "." )
            && !entry.getName().startsWith( "__MACOSX/" ) ) {

            File entryFile = null;

            String extension = FilenameUtils.getExtension( entry.getName() );
            if ( checkExtension( extension, false ) ) {
              boolean isDestinationValid = true;

              if ( isTemporary() ) {
                entryFile =
                  PentahoSystem.getApplicationContext()
                    .createTempFile( session, StringUtil.EMPTY_STRING, DOT + extension + DOT_TMP, true );
              } else {
                File destination = new File( getPath() + File.separatorChar );
                entryFile = new File( destination, entry.getName() );
                isDestinationValid = validateZipSlip( entryFile, destination );
              }

              if ( isDestinationValid ) {
                if ( sb.length() > 0 ) {
                  sb.append( '\n' );
                }

                sb.append( entryFile.getName() );
                FileOutputStream entryOutputStream = new FileOutputStream( entryFile );

                try {
                  IOUtils.copy( zipStream, entryOutputStream );
                } finally {
                  IOUtils.closeQuietly( entryOutputStream );
                }
              }
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
    if ( sb.length() > 0 ) {
      return sb.toString();
    } else {
      // no valid entries in the zip - nothing unzipped
      return Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0012_ILLEGAL_CONTENTS" );
    }
  }

  private boolean validateZipSlip( File destinationFile, File destinationDir ) throws IOException {
    return destinationFile.getCanonicalPath().startsWith( destinationDir.getCanonicalPath() + File.separator );
  }

  protected String handleTarGZ( File file ) throws IOException {

    // first extract the gz
    String filename = handleGZip( file, true );

    File tarFile = new File( filename );

    if ( session != null ) {
      ITempFileDeleter fileDeleter =
        (ITempFileDeleter) session.getAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE );
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
      String error =
        compressed ? Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0006_FILE_TOO_BIG" )
          : Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0003_FILE_TOO_BIG" );
      writer.write( error );
      return false;
    }

    File checkDir;
    long folderLimit;
    if ( !isTemporary() ) {
      checkDir = pathDir;
      folderLimit = maxFolderSize;
    } else {
      checkDir = tmpPathDir;
      folderLimit = maxTmpFolderSize;
    }
    long actualDirSize = getFolderSize( checkDir );
    if ( ( itemSize + actualDirSize ) > folderLimit ) {
      String error =
        compressed ? Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0007_FOLDER_SIZE_LIMIT_REACHED" )
          : Messages.getInstance()
          .getErrorString( "UploadFileServlet.ERROR_0004_FOLDER_SIZE_LIMIT_REACHED" );
      writer.write( error );
      return false;
    }
    return true;
  }

  private long getFolderSize( File folder ) {
    long foldersize = 0;
    if ( folder.isDirectory() ) {
      for ( File file : folder.listFiles() ) {
        if ( file.isDirectory() ) {
          foldersize += getFolderSize( file );
        } else {
          foldersize += file.length();
        }
      }
    }
    return foldersize;
  }

  /**
   * <p>Removes the file name, leaving all extensions of a given filename.</p>
   * <p>Returns <code>null</code> if filename is <code>null</code>, and an empty string if the filename is empty or has
   * no extensions.</p>
   * <p>The result, if not <code>null</code> nor an empty string is sanitized by replacing any character that is not a
   * letter (lower or upper case), a digit (0-9), an underscore ("_"), a dot (".") or a dash ("-"), by an underscore
   * ("_").</p>
   *
   * @param filename the filename for which we want the extensions
   * @return all extensions (sanitized) of the given filename
   */
  protected String removeFileName( String filename ) {
    if ( null != filename ) {
      int dotIndex = filename.indexOf( DOT );

      if ( 0 < dotIndex ) {
        int len = filename.length();
        ++dotIndex;
        if ( len > dotIndex ) {
          return filename.substring( dotIndex ).replaceAll( "[^a-zA-Z0-9_.-]", "_" );
        }
      }

      return StringUtil.EMPTY_STRING;
    }

    return null;
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
    this.uploadedPart = value != null ? new FileItemPart( value ) : null;
  }

  public FileItem getUploadedFileItem() {
    return this.uploadedPart instanceof FileItemPart ? ( (FileItemPart) uploadedPart ).getFileItem() : null;
  }

  public Part getUploadedPart() {
    return uploadedPart;
  }

  public void setUploadedPart( Part uploadedPart ) {
    this.uploadedPart = uploadedPart;
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

  void setAllowsNoExtension( boolean value ) {
    this.allowsNoExtension = value;
  }

  boolean getAllowsNoExtension() {
    return this.allowsNoExtension;
  }

  void setAllowedExtensionsString( String value ) {
    this.allowedExtensionsString = value;
    String[] extensions = value.split( "," );

    HashSet<String> theSet = new HashSet<>( extensions.length );
    Collections.addAll( theSet, extensions );
    this.allowedExtensions = theSet;
  }

  String getAllowedExtensionsString() {
    return this.allowedExtensionsString;
  }

  private static class FileItemPart implements Part {
    @NonNull
    private final FileItem fileItem;

    public FileItemPart( @NonNull FileItem fileItem ) {
      this.fileItem = fileItem;
    }

    @NonNull
    public FileItem getFileItem() {
      return fileItem;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return fileItem.getInputStream();
    }

    @Override
    public String getContentType() {
      return fileItem.getContentType();
    }

    @Override
    public String getName() {
      return fileItem.getFieldName();
    }

    @Override
    public String getSubmittedFileName() {
      return fileItem.getName();
    }

    @Override
    public long getSize() {
      return fileItem.getSize();
    }

    @Override
    public void delete() throws IOException {
      fileItem.delete();
    }


    @Override
    public void write( String s ) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader( String s ) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaders( String s ) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaderNames() {
      throw new UnsupportedOperationException();
    }
  }
}
