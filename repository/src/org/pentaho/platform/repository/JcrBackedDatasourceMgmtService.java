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

package org.pentaho.platform.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

public class JcrBackedDatasourceMgmtService implements IDatasourceMgmtService {

  private IUnifiedRepository repository;

  private volatile List<Character> cachedReservedChars; // make sure threads see up-to-date value

  private Serializable cachedDatabaseParentFolderId;

  private static final String FOLDER_PDI = "pdi"; //$NON-NLS-1$

  private static final String FOLDER_DATABASES = "databases"; //$NON-NLS-1$

  private DatabaseHelper databaseHelper;

  public JcrBackedDatasourceMgmtService() {
  }

  public JcrBackedDatasourceMgmtService( IUnifiedRepository repository, IDatabaseDialectService databaseDialectService ) {
    super();
    this.repository = repository;
    cachedReservedChars = repository.getReservedChars();
    databaseHelper = new DatabaseHelper( databaseDialectService );
  }

  public void init( IPentahoSession session ) {
    repository = PentahoSystem.get( IUnifiedRepository.class, session );
    cachedReservedChars = repository.getReservedChars();
  }

  public String createDatasource( IDatabaseConnection databaseConnection ) throws DuplicateDatasourceException,
    DatasourceMgmtServiceException {
    try {
      // IPasswordService passwordService = PentahoSystem.get(IPasswordService.class,
      // PentahoSessionHolder.getSession());
      // databaseMeta.setPassword(passwordService.encrypt(databaseMeta.getPassword()));

      RepositoryFile file =
          new RepositoryFile.Builder( RepositoryFilenameUtils.escape( databaseConnection.getName()
              + RepositoryObjectType.DATABASE.getExtension(), cachedReservedChars ) ).title(
              RepositoryFile.DEFAULT_LOCALE, databaseConnection.getName() ).versioned( true ).build();
      file =
          repository.createFile( getDatabaseParentFolderId(), file, new NodeRepositoryFileData( databaseHelper
              .databaseConnectionToDataNode( databaseConnection ) ), null );
      if ( file != null && file.getId() != null ) {
        return file.getId().toString();
      } else {
        return null;
      }
      // } catch(PasswordServiceException pse) {
      // throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
      //      "DatasourceMgmtService.ERROR_0007_UNABLE_TO_ENCRYPT_PASSWORD"), pse ); //$NON-NLS-1$
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException(
          Messages
              .getInstance()
              .getErrorString(
                  "DatasourceMgmtService.ERROR_0001_UNABLE_TO_CREATE_DATASOURCE", databaseConnection.getName(), ure.getLocalizedMessage() ), ure ); //$NON-NLS-1$
    }

  }

  public void deleteDatasourceByName( String name ) throws NonExistingDatasourceException,
    DatasourceMgmtServiceException {
    RepositoryFile fileToDelete = null;
    try {
      fileToDelete = repository.getFile( getPath( name ) );
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException(
          Messages
              .getInstance()
              .getErrorString(
                  "DatasourceMgmtService.ERROR_0002_UNABLE_TO_DELETE_DATASOURCE", fileToDelete.getName(), ure.getLocalizedMessage() ), ure ); //$NON-NLS-1$
    }
    deleteDatasource( fileToDelete );
  }

  public void deleteDatasourceById( String id ) throws NonExistingDatasourceException, DatasourceMgmtServiceException {
    RepositoryFile fileToDelete = null;
    try {
      fileToDelete = repository.getFileById( id );
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException(
          Messages
              .getInstance()
              .getErrorString(
                  "DatasourceMgmtService.ERROR_0002_UNABLE_TO_DELETE_DATASOURCE", fileToDelete.getName(), ure.getLocalizedMessage() ), ure ); //$NON-NLS-1$
    }
    deleteDatasource( fileToDelete );
  }

  private void deleteDatasource( RepositoryFile file ) throws DatasourceMgmtServiceException {
    try {
      if ( file != null ) {
        // Permanently Deletes the File
        repository.deleteFile( file.getId(), true, null );
      } else {
        throw new DatasourceMgmtServiceException( Messages.getInstance().getErrorString(
            "DatasourceMgmtService.ERROR_0002_UNABLE_TO_DELETE_DATASOURCE", "", "" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$       
      }
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException(
          Messages
              .getInstance()
              .getErrorString(
                  "DatasourceMgmtService.ERROR_0002_UNABLE_TO_DELETE_DATASOURCE", file.getName(), ure.getLocalizedMessage() ), ure ); //$NON-NLS-1$
    }
  }

  public IDatabaseConnection getDatasourceByName( String name ) throws DatasourceMgmtServiceException {
    RepositoryFile file = null;
    try {
      file = repository.getFile( getPath( name ) );
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException( Messages.getInstance().getErrorString(
          "DatasourceMgmtService.ERROR_0004_UNABLE_TO_RETRIEVE_DATASOURCE", name, ure.getLocalizedMessage() ), ure ); //$NON-NLS-1$
    }
    if ( file != null ) {
      return getDatasource( file );
    }
    return null;
  }

  public IDatabaseConnection getDatasourceById( String id ) throws DatasourceMgmtServiceException {
    RepositoryFile file = null;
    try {
      file = repository.getFileById( id );
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException( Messages.getInstance().getErrorString(
          "DatasourceMgmtService.ERROR_0004_UNABLE_TO_RETRIEVE_DATASOURCE", file.getName() ), ure ); //$NON-NLS-1$
    }
    if ( file != null ) {
      return getDatasource( file );
    }
    return null;
  }

  private IDatabaseConnection getDatasource( RepositoryFile file ) throws DatasourceMgmtServiceException {
    try {
      if ( file != null ) {
        NodeRepositoryFileData data = repository.getDataForRead( file.getId(), NodeRepositoryFileData.class );
        IDatabaseConnection databaseConnection =
            databaseHelper.dataNodeToDatabaseConnection( file.getId(), file.getTitle(), data.getNode() );
        // IPasswordService passwordService = PentahoSystem.get(IPasswordService.class,
        // PentahoSessionHolder.getSession());
        // databaseMeta.setPassword(passwordService.decrypt(databaseMeta.getPassword()));
        return databaseConnection;
      } else {
        throw new DatasourceMgmtServiceException( Messages.getInstance().getErrorString(
            "DatasourceMgmtService.ERROR_0004_UNABLE_TO_RETRIEVE_DATASOURCE", "", "" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
      // } catch(PasswordServiceException pse) {
      // throw new DatasourceMgmtServiceException(Messages.getInstance()
      //      .getErrorString("DatasourceMgmtService.ERROR_0008_UNABLE_TO_DECRYPT_PASSWORD"), pse ); //$NON-NLS-1$
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException(
          Messages
              .getInstance()
              .getErrorString(
                  "DatasourceMgmtService.ERROR_0004_UNABLE_TO_RETRIEVE_DATASOURCE", file.getName(), ure.getLocalizedMessage() ), ure ); //$NON-NLS-1$
    }
  }

  public List<IDatabaseConnection> getDatasources() throws DatasourceMgmtServiceException {
    try {
      List<IDatabaseConnection> datasourceList = new ArrayList<IDatabaseConnection>();
      List<RepositoryFile> repositoryFiles = getRepositoryFiles();
      if ( repositoryFiles != null ) {
        for ( RepositoryFile file : repositoryFiles ) {
          NodeRepositoryFileData data = repository.getDataForRead( file.getId(), NodeRepositoryFileData.class );
          IDatabaseConnection databaseConnection =
              databaseHelper.dataNodeToDatabaseConnection( file.getId(), file.getTitle(), data.getNode() );
          // IPasswordService passwordService = PentahoSystem.get(IPasswordService.class,
          // PentahoSessionHolder.getSession());
          // databaseMeta.setPassword(passwordService.decrypt(databaseMeta.getPassword()));
          datasourceList.add( databaseConnection );
        }
      }
      return datasourceList;
      // } catch(PasswordServiceException pse) {
      // throw new DatasourceMgmtServiceException(Messages.getInstance()
      //      .getErrorString("DatasourceMgmtService.ERROR_0008_UNABLE_TO_DECRYPT_PASSWORD"), pse ); //$NON-NLS-1$
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException( Messages.getInstance().getErrorString(
          "DatasourceMgmtService.ERROR_0004_UNABLE_TO_RETRIEVE_DATASOURCE", "", ure.getLocalizedMessage() ), ure ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public List<String> getDatasourceIds() throws DatasourceMgmtServiceException {
    try {
      List<String> datasourceList = new ArrayList<String>();
      List<RepositoryFile> repositoryFiles = getRepositoryFiles();
      if ( repositoryFiles != null ) {
        for ( RepositoryFile file : repositoryFiles ) {
          datasourceList.add( file.getId().toString() );
        }
      }
      return datasourceList;
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException( Messages.getInstance().getErrorString(
          "DatasourceMgmtService.ERROR_0004_UNABLE_TO_RETRIEVE_DATASOURCE", "", ure.getLocalizedMessage() ), ure ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public String updateDatasourceById( String id, IDatabaseConnection databaseConnection )
    throws NonExistingDatasourceException, DatasourceMgmtServiceException {
    RepositoryFile file = null;
    try {
      file = repository.getFileById( id );
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException(
          Messages
              .getInstance()
              .getErrorString(
                  "DatasourceMgmtService.ERROR_0003_UNABLE_TO_UPDATE_DATASOURCE", databaseConnection.getName(), ure.getLocalizedMessage() ), ure ); //$NON-NLS-1$
    }
    return updateDatasource( file, databaseConnection );
  }

  public String updateDatasourceByName( String name, IDatabaseConnection databaseConnection )
    throws NonExistingDatasourceException, DatasourceMgmtServiceException {
    RepositoryFile file = null;
    try {
      if ( databaseConnection.getId() != null ) {
        file = repository.getFileById( databaseConnection.getId() );
      } else {
        file = repository.getFile( getPath( name ) );
      }
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException(
          Messages
              .getInstance()
              .getErrorString(
                  "DatasourceMgmtService.ERROR_0003_UNABLE_TO_UPDATE_DATASOURCE", databaseConnection.getName(), ure.getLocalizedMessage() ), ure ); //$NON-NLS-1$
    }
    return updateDatasource( file, databaseConnection );
  }

  private String updateDatasource( RepositoryFile file, IDatabaseConnection databaseConnection )
    throws NonExistingDatasourceException, DatasourceMgmtServiceException {
    try {
      // IPasswordService passwordService = PentahoSystem.get(IPasswordService.class,
      // PentahoSessionHolder.getSession());
      // Store the new encrypted password in the datasource object
      // databaseMeta.setPassword(passwordService.encrypt(databaseMeta.getPassword()));

      if ( file != null ) {
        file =
            new RepositoryFile.Builder( file ).versionId( file.getVersionId() ).id( file.getId() ).title(
                RepositoryFile.DEFAULT_LOCALE, databaseConnection.getName() ).build();
        file =
            repository.updateFile( file, new NodeRepositoryFileData( databaseHelper
                .databaseConnectionToDataNode( databaseConnection ) ), null );
        renameIfNecessary( databaseConnection, file );
        return file.getId().toString();
      } else {
        throw new NonExistingDatasourceException( Messages.getInstance().getErrorString(
            "DatasourceMgmtService.ERROR_0006_DATASOURCE_DOES_NOT_EXIST", databaseConnection.getName() ) ); //$NON-NLS-1$
      }

      // } catch(PasswordServiceException pse) {
      // throw new DatasourceMgmtServiceException(Messages.getInstance()
      //      .getErrorString("DatasourceMgmtService.ERROR_0007_UNABLE_TO_ENCRYPT_PASSWORD"), pse ); //$NON-NLS-1$
    } catch ( UnifiedRepositoryException ure ) {
      throw new DatasourceMgmtServiceException(
          Messages
              .getInstance()
              .getErrorString(
                  "DatasourceMgmtService.ERROR_0003_UNABLE_TO_UPDATE_DATASOURCE", databaseConnection.getName(), ure.getLocalizedMessage() ), ure ); //$NON-NLS-1$
    }
  }

  private String getDatabaseParentFolderPath() {
    return ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + FOLDER_PDI + RepositoryFile.SEPARATOR
        + FOLDER_DATABASES;
  }

  private Serializable getDatabaseParentFolderId() {
    if ( cachedDatabaseParentFolderId == null ) {
      try {
        RepositoryFile f = repository.getFile( getDatabaseParentFolderPath() );
        cachedDatabaseParentFolderId = f.getId();
      } catch ( UnifiedRepositoryException ure ) {
        return cachedDatabaseParentFolderId;
      }
    }
    return cachedDatabaseParentFolderId;
  }

  private List<RepositoryFile> getRepositoryFiles() {
    Serializable folderId = getDatabaseParentFolderId();
    if ( folderId != null ) {
      return repository.getChildren( folderId, "*" + RepositoryObjectType.DATABASE.getExtension() );
    } else {
      return null;
    }

  }

  private String getPath( final String name ) {
    String escapedName = RepositoryFilenameUtils.escape( name, cachedReservedChars );
    return getDatabaseParentFolderPath() + RepositoryFile.SEPARATOR + escapedName
        + RepositoryObjectType.DATABASE.getExtension();
  }

  private void renameIfNecessary( final IDatabaseConnection databaseConnection, final RepositoryFile file ) {
    if ( !isRenamed( databaseConnection, file ) ) {
      return;
    }
    StringBuilder buf = new StringBuilder( file.getPath().length() );
    buf.append( getParentPath( file.getPath() ) );
    buf.append( RepositoryFile.SEPARATOR );
    buf.append( RepositoryFilenameUtils.escape( databaseConnection.getName(), cachedReservedChars ) );
    buf.append( RepositoryObjectType.DATABASE.getExtension() );
    repository.moveFile( file.getId(), buf.toString(), null );
  }

  private boolean isRenamed( final IDatabaseConnection databaseConnection, final RepositoryFile file ) {
    String filename = databaseConnection.getName() + RepositoryObjectType.DATABASE.getExtension();
    if ( !file.getName().equals( RepositoryFilenameUtils.escape( filename, cachedReservedChars ) ) ) {
      return true;
    }
    return false;
  }

  protected String getParentPath( final String path ) {
    if ( path == null ) {
      throw new IllegalArgumentException();
    } else if ( RepositoryFile.SEPARATOR.equals( path ) ) {
      return null;
    }
    int lastSlashIndex = path.lastIndexOf( RepositoryFile.SEPARATOR );
    if ( lastSlashIndex == 0 ) {
      return RepositoryFile.SEPARATOR;
    } else if ( lastSlashIndex > 0 ) {
      return path.substring( 0, lastSlashIndex );
    } else {
      throw new IllegalArgumentException();
    }
  }
}
