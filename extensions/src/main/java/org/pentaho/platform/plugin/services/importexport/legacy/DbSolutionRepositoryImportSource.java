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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importexport.legacy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An {@link org.pentaho.platform.plugin.services.importexport.ImportSource} that connects to the legacy database-based
 * solution repository.
 * <p/>
 * This implementation works in the following way:
 * <p/>
 * <ol>
 * <li>Fetch all (joined) rows in a single query.</li>
 * <li>Process the result set, creating files, ACLs, and possibly temporary files containing the files' data.</li>
 * <li>Write this info to disk in batches.</li>
 * <li>Close the result set.</li>
 * <li>Iterate over the batches, one at a time.</li>
 * </ol>
 * <p/>
 * This implementation saves memory at the expense of disk space.
 * 
 * @author mlowery
 */
public class DbSolutionRepositoryImportSource extends AbstractImportSource {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog( DbSolutionRepositoryImportSource.class );

  // ~ Instance fields =================================================================================================

  private String srcCharset;
  private String requiredCharset;
  private String ownerName;
  private JdbcTemplate jdbcTemplate;

  // ~ Constructors ====================================================================================================

  public DbSolutionRepositoryImportSource( final DataSource dataSource, final String srcCharset,
      final String requiredCharset, final String ownerName ) {
    super();
    Assert.notNull( dataSource );
    this.jdbcTemplate = new JdbcTemplate( dataSource );
    Assert.hasLength( srcCharset );
    this.srcCharset = srcCharset;
    this.requiredCharset = requiredCharset;
    this.ownerName = ownerName;
  }

  // ~ Methods =========================================================================================================

  /**
   * The set is created dynamically - so we can't know this
   */
  @Override
  public int getCount() {
    return -1;
  }

  /**
   * @return
   */
  public Iterable<IRepositoryFileBundle> getFiles() {
    Assert.hasLength( requiredCharset );
    return new Iterable<IRepositoryFileBundle>() {
      public Iterator<IRepositoryFileBundle> iterator() {
        return new RepositoryFileBundleIterator();
      }
    };
  }

  /**
   * DESCRIPTION NEEDED
   */
  private class RepositoryFileBundleIterator implements Iterator<IRepositoryFileBundle> {

    public static final String GET_FILES_QUERY =
        "SELECT f.FILE_ID, f.fileName, f.fullPath, f.data, f.directory, f.lastModified, "
            + "a.ACL_MASK, a.RECIP_TYPE, a.RECIPIENT " + "FROM PRO_FILES f LEFT OUTER JOIN PRO_ACLS_LIST a "
            + "ON f.FILE_ID = a.ACL_ID " + "ORDER BY f.fullPath, a.ACL_POSITION ";

    // public static final String GET_FILE_QUERY = "SELECT f.fileName, f.fullPath, f.data, f.directory, f.lastModified "
    // + "FROM PRO_FILES f " + "WHERE f.fullPath = ?";

    private static final int BATCH_SIZE = 100;

    private int i = BATCH_SIZE; // this initial value forces fetch
    private int actualBatchSize = BATCH_SIZE; // this initial value forces fetch
    private int batchNumber;
    private List<IRepositoryFileBundle> batch;
    private List<File> serializedBatches;

    public RepositoryFileBundleIterator() {
      DbsrRowCallbackHandler dbsrRowCallbackHandler = new DbsrRowCallbackHandler();
      jdbcTemplate.query( GET_FILES_QUERY, dbsrRowCallbackHandler );
      serializedBatches = dbsrRowCallbackHandler.getSerializedBatches();
    }

    public boolean hasNext() {
      if ( i == BATCH_SIZE && actualBatchSize == BATCH_SIZE ) {
        fetchNextBatch();
      }
      return i < actualBatchSize;
    }

    public IRepositoryFileBundle next() {
      return batch.get( i++ );
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    private void fetchNextBatch() {
      try {
        InputStream in = FileUtils.openInputStream( serializedBatches.get( batchNumber ) );
        ObjectInputStream ois = new ObjectInputStream( in );
        batch = (List<IRepositoryFileBundle>) ois.readObject();
        IOUtils.closeQuietly( ois );
        actualBatchSize = batch.size();
        i = 0;
        batchNumber += 1;
      } catch ( IOException e ) {
        throw new RuntimeException( e );
      } catch ( ClassNotFoundException e ) {
        throw new RuntimeException( e );
      }
    }

    /**
     *
     */
    private class DbsrRowCallbackHandler implements RowCallbackHandler {
      private String lastId;
      private RepositoryFile currentFile;
      private RepositoryFileAcl.Builder currentAclBuilder;
      private File currentTmpFile;
      private String currentPath;
      private String currentMimeType;
      private String currentCharset;
      private final List<IRepositoryFileBundle> currentBatch = new ArrayList<IRepositoryFileBundle>( BATCH_SIZE );
      private final List<File> serializedBatches = new ArrayList<File>();
      private final List<String> binaryFileTypes = new ArrayList<String>( Arrays.asList( new String[] { "gif", "jpg",
        "png", "prpt" } ) );

      @SuppressWarnings( "nls" )
      public DbsrRowCallbackHandler() {
      }

      /**
       * List is manually sorted!
       */
      @SuppressWarnings( "nls" )
      public void processRow( final ResultSet rs ) throws SQLException {
        final String id = rs.getString( 1 );
        if ( !id.equals( lastId ) ) {

          if ( lastId != null ) { // prevent adding to batch on first row
            // id is different from the last; add completed file and acl to batch
            currentBatch.add( new org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle( currentFile,
                currentAclBuilder != null ? currentAclBuilder.build() : null, currentPath, currentTmpFile,
                currentCharset, currentMimeType ) );
            currentFile = null;
            currentAclBuilder = null;
            currentTmpFile = null;
            currentPath = null;
            currentMimeType = null;
            currentCharset = null;
            if ( currentBatch.size() == BATCH_SIZE ) {
              flushBatch();
            }
          }

          lastId = id;

          final String name = rs.getString( 2 );
          currentPath = rs.getString( 3 );
          final boolean folder = rs.getBoolean( 5 );
          Date lastModificationDate = null;
          int lastModificationDateColumnType = rs.getMetaData().getColumnType( 6 );
          if ( lastModificationDateColumnType == Types.DATE ) {
            lastModificationDate = rs.getDate( 6 );
          } else {
            lastModificationDate = new Date( rs.getLong( 6 ) );
          }
          currentFile =
              new RepositoryFile.Builder( name ).hidden( false ).folder( folder ).lastModificationDate(
                lastModificationDate ).build();
          // currentTmpFile holds contents (i.e. data) of currentFile
          currentTmpFile = null;
          currentCharset = null;
          currentMimeType = null;
          if ( !folder ) {
            try {
              currentTmpFile = getTmpFile( name, rs.getBlob( 4 ).getBinaryStream() );
            } catch ( IOException e ) {
              throw new RuntimeException( e );
            }
            currentCharset = isBinary( name ) ? null : requiredCharset;
            currentMimeType = getMimeType( getExtension( name ) );
          }

          if ( hasAcl( rs ) ) {
            final int mask = rs.getInt( 7 );
            final int recipientType = rs.getInt( 8 );
            final String recipient = rs.getString( 9 );
            // currentAclBuilder = new RepositoryFileAcl.Builder(ownerName, RepositoryFileSid.Type.USER);
            // currentAclBuilder.ace(recipient, recipientType == 0 ? RepositoryFileSid.Type.USER
            // : RepositoryFileSid.Type.ROLE, makePerms(mask));
          }
        } else {
          // the only way to get here is to see two consecutive rows with same id; this necessarily means that this file
          // has an ACL so just add the ACE to its ACL
          final int mask = rs.getInt( 7 );
          final int recipientType = rs.getInt( 8 );
          final String recipient = rs.getString( 9 );
          // just add ace
          // currentAclBuilder.ace(recipient, recipientType == 0 ? RepositoryFileSid.Type.USER
          // : RepositoryFileSid.Type.ROLE, makePerms(mask));
        }

      }

      private void flushBatch() {
        if ( currentFile != null ) { // save the last file to the batch
          currentBatch.add( new org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle( currentFile,
              currentAclBuilder != null ? currentAclBuilder.build() : null, currentPath, currentTmpFile,
              currentCharset, currentMimeType ) );
        }

        if ( !currentBatch.isEmpty() ) {
          File tmpFile;
          try {
            tmpFile = File.createTempFile( "pentaho", ".ser" );
            tmpFile.deleteOnExit();
            FileOutputStream fout = FileUtils.openOutputStream( tmpFile );
            ObjectOutputStream oos = new ObjectOutputStream( fout );
            oos.writeObject( currentBatch );
            oos.close();
            currentBatch.clear();
            serializedBatches.add( tmpFile );
          } catch ( IOException e ) {
            throw new RuntimeException( e );
          }
        }
      }

      public List<File> getSerializedBatches() {
        // flush one more time
        flushBatch();
        return serializedBatches;
      }

      private boolean hasAcl( final ResultSet rs ) throws SQLException {
        // left outer join puts a non-null value in RECIPIENT column if file has an ACL;
        // not all files have an ACL in DBSR
        return rs.getString( 9 ) != null;
      }

      private EnumSet<RepositoryFilePermission> makePerms( final int mask ) {
        if ( mask == -1 ) {
          return EnumSet.of( RepositoryFilePermission.ALL );
        } else {
          Set<RepositoryFilePermission> perms = new HashSet<RepositoryFilePermission>();
          if ( ( mask & 1 ) == 1 ) { // EXECUTE
            perms.add( RepositoryFilePermission.READ );
          }
          /*
           * SUBSCRIBE (decimal value 2) is not a permission in PUR! Skipping...
           */
          if ( ( mask & 4 ) == 4 ) { // CREATE
            perms.add( RepositoryFilePermission.WRITE );
          }
          if ( ( mask & 8 ) == 8 ) { // UPDATE
            perms.add( RepositoryFilePermission.WRITE );
          }
          if ( ( mask & 16 ) == 16 ) { // DELETE
            perms.add( RepositoryFilePermission.WRITE );
          }
          if ( ( mask & 32 ) == 32 ) { // UPDATE_PERMISSIONS
            perms.add( RepositoryFilePermission.ACL_MANAGEMENT );
          }
          if ( perms.isEmpty() ) {
            return EnumSet.noneOf( RepositoryFilePermission.class );
          } else {
            return EnumSet.copyOf( perms );
          }
        }
      }

      private File getTmpFile( final String name, final InputStream in ) throws IOException {
        File tmp = File.createTempFile( "pentaho", ".tmp" );
        tmp.deleteOnExit();

        if ( !isBinary( name ) ) {
          // read bytes in src charset
          Reader reader = new InputStreamReader( in, srcCharset );
          // write bytes in dest charset
          Writer out = new OutputStreamWriter( new FileOutputStream( tmp ), requiredCharset );
          IOUtils.copy( reader, out );
          IOUtils.closeQuietly( in );
          IOUtils.closeQuietly( out );
        } else {
          logger.debug( name + " is binary" );
          FileOutputStream out = new FileOutputStream( tmp );
          IOUtils.copy( in, out );
          IOUtils.closeQuietly( in );
          IOUtils.closeQuietly( out );
        }
        return tmp;
      }

      private boolean isBinary( final String name ) {
        String ext = getExtension( name );
        if ( ext != null ) {
          return Collections.binarySearch( binaryFileTypes, ext ) >= 0;
        } else {
          return false;
        }
      }

      private String getExtension( final String name ) {
        Assert.notNull( name );
        int lastDot = name.lastIndexOf( '.' );
        if ( lastDot > -1 ) {
          return name.substring( lastDot + 1 ).toLowerCase();
        } else {
          return null;
        }
      }
    }
  }
}
