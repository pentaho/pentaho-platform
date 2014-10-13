package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class JcrRepositoryFileDaoFacade implements IRepositoryFileDao {

  private JcrTemplate jcrTemplate;
  private JcrRepositoryFileDaoInst jcrRepositoryFileDao;

  public JcrRepositoryFileDaoFacade( JcrTemplate jcrTemplate, JcrRepositoryFileDaoInst jcrRepositoryFileDao ) {
    this.jcrTemplate = jcrTemplate;
    this.jcrRepositoryFileDao = jcrRepositoryFileDao;
  }

  static abstract class VoidJcrCallback implements JcrCallback {
    @Override
    public Object doInJcr( Session session ) throws IOException, RepositoryException {
      runInJcr( session );
      return null;
    }

    public abstract void runInJcr( Session session ) throws IOException, RepositoryException;
  }

  private void checkPath( String path ) {
    Assert.hasText( path );
    Assert.isTrue( path.startsWith( RepositoryFile.SEPARATOR ) );
  }

  @Override
  public RepositoryFile getFileByAbsolutePath( final String absPath ) {
    checkPath( absPath );
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.internalGetFile( session, absPath, false, null );
      }
    } );
  }

  @Override
  public RepositoryFile getFile( final String relPath, final boolean loadMaps ) {
    checkPath( relPath );
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.getFileByRelPath( session, relPath, loadMaps, null );
      }
    } );
  }

  @Override
  public void moveFile( final Serializable fileId, final String destRelPath, final String versionMessage ) {
    copyOrMoveFile( fileId, destRelPath, versionMessage, false );
  }

  @Override
  public void copyFile( final Serializable fileId, final String destRelPath, final String versionMessage ) {
    copyOrMoveFile( fileId, destRelPath, versionMessage, true );
  }

  private void copyOrMoveFile( final Serializable fileId, final String destRelPath, final String versionMessage,
      final boolean copy ) {
    Assert.notNull( fileId );
    checkKiosk();
    jcrTemplate.execute( new VoidJcrCallback() {
      @Override
      public void runInJcr( final Session session ) throws RepositoryException, IOException {
        jcrRepositoryFileDao.internalCopyOrMove( session, getFileById( fileId ), destRelPath, versionMessage, copy );
      }
    } );
  }

  @Override
  public VersionSummary getVersionSummary( final Serializable fileId, final Serializable versionId ) {
    Assert.notNull( fileId );
    return (VersionSummary) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.getVersionSummary( session, fileId, versionId );
      }
    } );
  }

  @Override
  public void
    restoreFileAtVersion( final Serializable fileId, final Serializable versionId, final String versionMessage ) {
    Assert.notNull( fileId );
    Assert.notNull( versionId );
    checkKiosk();
    jcrTemplate.execute( new VoidJcrCallback() {
      @Override
      public void runInJcr( final Session session ) throws RepositoryException, IOException {
        jcrRepositoryFileDao.restoreFileAtVersion( session, fileId, versionId, versionMessage );
      }
    } );
  }

  @Override
  public boolean canUnlockFile( final Serializable fileId ) {
    return (Boolean) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.canUnlockFile( session, fileId );
      }
    } );
  }

  @Override
  public RepositoryFileTree getTree( final RepositoryRequest repositoryRequest ) {
    Assert.hasText( repositoryRequest.getPath() );
    return (RepositoryFileTree) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.getTree( session, repositoryRequest );
      }
    } );
  }

  @Override
  @Deprecated
  public RepositoryFileTree getTree( final String relPath, final int depth, final String filter,
      final boolean showHidden ) {
    return getTree( new RepositoryRequest( relPath, showHidden, depth, filter ) );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<RepositoryFile> getReferrers( final Serializable fileId ) {
    if ( fileId == null ) {
      return Collections.emptyList();
    }
    return (List<RepositoryFile>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.getReferrers( session, fileId );
      }
    } );
  }

  @Override
  public void setFileMetadata( final Serializable fileId, final Map<String, Serializable> metadataMap ) {
    Assert.notNull( fileId );
    checkKiosk();
    jcrTemplate.execute( new VoidJcrCallback() {
      @Override
      public void runInJcr( final Session session ) throws RepositoryException, IOException {
        JcrRepositoryFileUtils.setFileMetadata( session, fileId, metadataMap );
      }
    } );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public Map<String, Serializable> getFileMetadata( final Serializable fileId ) {
    Assert.notNull( fileId );
    return (Map<String, Serializable>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( Session session ) throws IOException, RepositoryException {
        return JcrRepositoryFileUtils.getFileMetadata( session, fileId );
      }
    } );
  }

  @Override
  public List<Locale> getAvailableLocalesForFileById( Serializable fileId ) {
    return getAvailableLocalesForFile( getFileById( fileId, true ) );
  }

  @Override
  public List<Locale> getAvailableLocalesForFileByPath( String relPath ) {
    return getAvailableLocalesForFile( getFileById( relPath, true ) );
  }

  @Override
  public List<Locale> getAvailableLocalesForFile( RepositoryFile repositoryFile ) {
    return jcrRepositoryFileDao.getAvailableLocalesForFile( repositoryFile );
  }

  @Override
  public Properties getLocalePropertiesForFileById( Serializable fileId, String locale ) {
    return getLocalePropertiesForFile( getFileById( fileId, true ), locale );
  }

  @Override
  public Properties getLocalePropertiesForFileByPath( String relPath, String locale ) {
    return getLocalePropertiesForFile( getFileById( relPath, true ), locale );
  }

  @Override
  public Properties getLocalePropertiesForFile( RepositoryFile repositoryFile, String locale ) {
    return jcrRepositoryFileDao.getLocalePropertiesForFile( repositoryFile, locale );
  }

  @Override
  public void setLocalePropertiesForFileById( Serializable fileId, String locale, Properties properties ) {
    RepositoryFile repositoryFile = getFileById( fileId, true );
    setLocalePropertiesForFile( repositoryFile, locale, properties );
  }

  @Override
  public void setLocalePropertiesForFileByPath( String relPath, String locale, Properties properties ) {
    RepositoryFile repositoryFile = getFileById( relPath, true );
    setLocalePropertiesForFile( repositoryFile, locale, properties );
  }

  @Override
  public void setLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale,
      final Properties properties ) {
    Assert.notNull( repositoryFile );
    Assert.notNull( locale );
    Assert.notNull( properties );
    checkKiosk();
    jcrTemplate.execute( new VoidJcrCallback() {
      @Override
      public void runInJcr( Session session ) throws IOException, RepositoryException {
        jcrRepositoryFileDao.setLocalePropertiesForFile( session, repositoryFile, locale, properties );
      }
    } );
  }

  @Override
  public void deleteLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale ) {
    Assert.notNull( repositoryFile );
    Assert.notNull( locale );
    checkKiosk();
    jcrTemplate.execute( new VoidJcrCallback() {
      @Override
      public void runInJcr( final Session session ) throws RepositoryException, IOException {
        jcrRepositoryFileDao.deleteLocalePropertiesForFile( session, repositoryFile, locale );
      }
    } );
  }

  @Override
  public RepositoryFile updateFolder( final RepositoryFile file, final String versionMessage ) {
    Assert.notNull( file );
    Assert.isTrue( file.isFolder() );
    checkKiosk();
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.internalUpdateFolder( session, file, versionMessage );
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void undeleteFile( final Serializable fileId, final String versionMessage ) {
    Assert.notNull( fileId );
    checkKiosk();
    jcrTemplate.execute( new VoidJcrCallback() {
      @Override
      public void runInJcr( final Session session ) throws RepositoryException, IOException {
        jcrRepositoryFileDao.undeleteFile( session, fileId, versionMessage );
      }
    } );
  }

  @Override
  public void deleteFile( final Serializable fileId, final String versionMessage ) {
    Assert.notNull( fileId );
    checkKiosk();
    jcrTemplate.execute( new VoidJcrCallback() {
      @Override
      public void runInJcr( final Session session ) throws RepositoryException, IOException {
        jcrRepositoryFileDao.deleteFile( session, fileId, versionMessage );
      }
    } );
  }

  @Override
  public void deleteFileAtVersion( final Serializable fileId, final Serializable versionId ) {
    Assert.notNull( fileId );
    Assert.notNull( versionId );
    checkKiosk();
    jcrTemplate.execute( new VoidJcrCallback() {
      @Override
      public void runInJcr( final Session session ) throws RepositoryException, IOException {
        jcrRepositoryFileDao.deleteFileAtVersion( session, fileId, versionId );
      }
    } );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<RepositoryFile> getDeletedFiles( final String origParentFolderPath, final String filter ) {
    Assert.hasLength( origParentFolderPath );
    return (List<RepositoryFile>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.getDeletedFiles( session, origParentFolderPath, filter );
      }
    } );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<RepositoryFile> getDeletedFiles() {
    return (List<RepositoryFile>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.getDeletedFiles( session );
      }
    } );
  }

  /**
   * <p>
   * No checkout needed as .trash is not versioned.
   * </p>
   */
  @Override
  public void permanentlyDeleteFile( final Serializable fileId, final String versionMessage ) {
    Assert.notNull( fileId );
    checkKiosk();
    jcrTemplate.execute( new VoidJcrCallback() {
      @Override
      public void runInJcr( final Session session ) throws RepositoryException, IOException {
        jcrRepositoryFileDao.permanentlyDeleteFile( session, fileId, versionMessage );
      }
    } );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T extends IRepositoryFileData> T getData( final Serializable fileId, final Serializable versionId,
      final Class<T> contentClass ) {
    Assert.notNull( fileId );
    return (T) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.getData( session, fileId, versionId, contentClass );
      }
    } );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<RepositoryFile> getChildren( final RepositoryRequest repositoryRequest ) {
    Assert.notNull( repositoryRequest.getPath() );
    return (List<RepositoryFile>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.getChildren( session, repositoryRequest );
      }
    } );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<RepositoryFile> getChildren( final Serializable folderId, final String filter,
      final Boolean showHiddenFiles ) {
    Assert.notNull( folderId );
    return (List<RepositoryFile>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.getChildren( session, folderId, filter, showHiddenFiles );
      }
    } );
  }

  @Override
  public RepositoryFile updateFile( final RepositoryFile file, final IRepositoryFileData content,
      final String versionMessage ) {
    Assert.notNull( file );
    Assert.isTrue( !file.isFolder() );
    Assert.notNull( content );
    checkKiosk();
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.internalUpdateFile( session, file, content, versionMessage );
      }
    } );
  }

  @Override
  public void lockFile( final Serializable fileId, final String message ) {
    Assert.notNull( fileId );
    checkKiosk();
    jcrTemplate.execute( new VoidJcrCallback() {
      @Override
      public void runInJcr( final Session session ) throws RepositoryException, IOException {
        jcrRepositoryFileDao.lockFile( session, fileId, message );
      }
    } );
  }

  @Override
  public void unlockFile( final Serializable fileId ) {
    Assert.notNull( fileId );
    checkKiosk();
    jcrTemplate.execute( new VoidJcrCallback() {
      @Override
      public void runInJcr( final Session session ) throws RepositoryException, IOException {
        jcrRepositoryFileDao.unlockFile( session, fileId );
      }
    } );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<VersionSummary> getVersionSummaries( final Serializable fileId ) {
    Assert.notNull( fileId );
    return (List<VersionSummary>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.getVersionSummaries( session, fileId );
      }
    } );
  }

  @Override
  public RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final RepositoryFileAcl acl, final String versionMessage ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }
    /*
     * PPP-3049: Changed the Assert.notNull(content) to code that creates a file with a single blank when the assert
     * WOULD have been triggered.
     */
    Assert.notNull( file );
    Assert.isTrue( !file.isFolder() );
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.internalCreateFile( session, parentFolderId, file, content, acl, versionMessage );
      }
    } );
  }

  @Override
  public RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile folder,
      final RepositoryFileAcl acl, final String versionMessage ) {
    Assert.notNull( folder );
    Assert.isTrue( folder.isFolder() );
    checkKiosk();
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.internalCreateFolder( session, parentFolderId, folder, acl, versionMessage );
      }
    } );
  }

  @Override
  public List<Character> getReservedChars() {
    return JcrRepositoryFileUtils.getReservedChars();
  }

  private void checkKiosk() {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }
  }

  private boolean isKioskEnabled() {
    if ( PentahoSystem.getInitializedOK() ) {
      return "true".equals( PentahoSystem.getSystemSetting( "kiosk-mode", "false" ) );
    } else {
      return false;
    }
  }

  @Override
  public RepositoryFile getFile( final String relPath ) {
    return getFile( relPath, false, null );
  }

  @Override
  public RepositoryFile getFile( final String relPath, final IPentahoLocale locale ) {
    return getFile( relPath, false, locale );
  }

  @Override
  public RepositoryFile getFile( final String relPath, final boolean loadLocaleMaps, final IPentahoLocale locale ) {
    Assert.hasText( relPath );
    Assert.isTrue( relPath.startsWith( RepositoryFile.SEPARATOR ) );
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.internalGetFile( session, relPath, loadLocaleMaps, locale );
      }
    } );
  }

  @Override
  public RepositoryFile getFileById( final Serializable fileId ) {
    return getFileById( fileId, null );
  }

  public RepositoryFile getFileById( final Serializable fileId, final boolean loadMaps ) {
    return getFileById( fileId, loadMaps, null );
  }

  @Override
  public RepositoryFile getFileById( Serializable fileId, IPentahoLocale locale ) {
    return getFileById( fileId, false, locale );
  }

  @Override
  public RepositoryFile getFile( final Serializable fileId, final Serializable versionId ) {
    Assert.notNull( fileId );
    Assert.notNull( versionId );
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.getFile( session, fileId, versionId );
      }
    } );
  }

  @Override
  public RepositoryFile getFileById( final Serializable fileId, final boolean loadLocaleMaps,
      final IPentahoLocale locale ) {
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return jcrRepositoryFileDao.checkAndGetFileById( session, fileId, loadLocaleMaps, locale );
      }
    } );
  }
}
