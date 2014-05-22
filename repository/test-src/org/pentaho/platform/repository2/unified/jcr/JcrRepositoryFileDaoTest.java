package org.pentaho.platform.repository2.unified.jcr;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.lock.LockManager;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;

public class JcrRepositoryFileDaoTest {

  @Mocked
  private RepositoryFileAcl fileAcl;
  @Mocked
  private RepositoryFile file;
  @Mocked
  private Session session;
  @Mocked
  private Node fileNode;
  @Mocked
  private NodeRepositoryFileData content;
  @Mocked
  private Workspace workspace;
  @Mocked
  private VersionManager versionManager;
  @Mocked
  private VersionHistory versionHistory;
  @Mocked
  private IRepositoryFileData fileData;
  @Mocked
  private IPentahoLocale pentahoLocale;
  @Mocked
  private IPentahoSession pentahoSession;
  @Mocked
  private Property referrerProperty;
  @Mocked
  private PropertyIterator propertyIterator;
  @Mocked
  private RepositoryRequest repositoryRequest;
  @Mocked
  private LockManager lockManager;

  private final static String PARENT_ID = "PARENT_ID";
  private final static String FILE_ID = "FILE_ID";
  private final static String VERSION_ID = "VERSION_ID";
  private final static String NODE_IDENTIFIER = "NODE_IDENTIFIER";
  private final static String ABSOLUTE_PATH = "/ABSOLUTE_PATH/";
  private final static String PARENT_FOLDER_PATH = "/PARENT_FOLDER_PATH/";
  private final static String CONTENT_TYPE = "CONTENT_TYPE";
  private final static String SESSION_NAME = "SESSION_NAME";

  private MockUp<?> jcrRepositoryFileUtilsMockUp;
  private MockUp<?> jcrRepositoryFileAclUtilsMockUp;
  private MockUp<?> jcrRepositoryFileDaoMockUp;
  private MockUp<?> repositoryFileBuilderMockUp;
  private MockUp<?> pentahoSessionHolderMockUp;

  private MockUp<JcrRepositoryFileAclUtils> setUpJcrRepositoryFileAclUtilsMock() {
    return new MockUp<JcrRepositoryFileAclUtils>() {
      @Mock
      public RepositoryFileAcl createAcl( Session session, PentahoJcrConstants pentahoJcrConstants,
          Serializable fileId, RepositoryFileAcl acl ) {
        return null;
      }
    };
  }

  private MockUp<JcrRepositoryFileUtils> setUpJcrRepositoryFileUtilsMock() {
    return new MockUp<JcrRepositoryFileUtils>() {
      @Mock
      public RepositoryFile nodeToFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Node node,
          final boolean loadMaps, IPentahoLocale pentahoLocale ) {
        return file;
      }

      @Mock
      public RepositoryFile getFileAtVersion( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Serializable fileId,
          final Serializable versionId ) {
        return file;
      }

      @Mock
      public List<VersionSummary>
        getVersionSummaries( final Session session, final PentahoJcrConstants pentahoJcrConstants,
            final Serializable fileId, final boolean includeAclOnlyChanges ) {
        return Collections.emptyList();
      }

      @Mock
      public Node deleteFileLocaleProperties( final Session session, final Serializable fileId, String locale ) {
        return fileNode;
      }

      @Mock
      public Node updateFileLocaleProperties( final Session session, final Serializable fileId, String locale,
          Properties properties ) {
        return fileNode;
      }

      @Mock
      public RepositoryFileTree getTree( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final String absPath,
          final RepositoryRequest repositoryRequest, IRepositoryAccessVoterManager accessVoterManager ) {
        return null;
      }

      @Mock
      public boolean isSupportedNodeType( final PentahoJcrConstants pentahoJcrConstants, final Node node ) {
        return true;
      }

      @Mock
      public boolean isPentahoFolder( final PentahoJcrConstants pentahoJcrConstants, final Node node ) {
        return false;
      }

      @Mock
      public boolean isPentahoFile( final PentahoJcrConstants pentahoJcrConstants, final Node node ) {
        return true;
      }

      @Mock
      public IRepositoryFileData getContent( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final Serializable fileId, final Serializable versionId, final ITransformer<IRepositoryFileData> transformer ) {
        return fileData;
      }

      @Mock
      public String getFileContentType( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final Serializable fileId, final Serializable versionId ) {
        return CONTENT_TYPE;
      }

      @Mock
      public List<RepositoryFile> getChildren( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper,
          final RepositoryRequest repositoryRequest ) {
        return Collections.emptyList();
      }

      @Mock
      public void checkoutNearestVersionableFileIfNecessary( final Session session,
          final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId ) {
      }

      @Mock
      public void checkinNearestVersionableFileIfNecessary( final Session session,
          final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId, final String versionMessage ) {
      }

      @Mock
      public void checkinNearestVersionableFileIfNecessary( final Session session,
          final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId, final String versionMessage,
          final Date versionDate, final boolean aclOnlyChange ) {
      }

      @Mock
      public void checkoutNearestVersionableNodeIfNecessary( final Session session,
          final PentahoJcrConstants pentahoJcrConstants, final Node node ) {
      }

      @Mock
      public void checkinNearestVersionableNodeIfNecessary( final Session session,
          final PentahoJcrConstants pentahoJcrConstants, final Node node, final String versionMessage ) {
      }

      @Mock
      public Serializable getParentId( final Session session, final Serializable fileId ) {
        return PARENT_ID;
      }

      @Mock
      public Node updateFolderNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final RepositoryFile folder ) {
        return fileNode;
      }

      @Mock
      public Node updateFileNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final RepositoryFile file, final IRepositoryFileData content,
          final ITransformer<IRepositoryFileData> transformer ) {
        return fileNode;
      }

      @Mock
      public RepositoryFile nodeIdToFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Serializable fileId ) {
        return file;
      }

      @Mock
      public Node createFileNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final Serializable parentFolderId, final RepositoryFile file, final IRepositoryFileData content,
          final ITransformer<IRepositoryFileData> transformer ) {
        return fileNode;
      }

      @Mock
      public RepositoryFile nodeToFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper, final Node node ) {
        return file;
      }

      @Mock
      public VersionSummary getVersionSummary( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final Serializable fileId, final Serializable versionId ) {
        return null;
      }

      @Mock
      public Node createFolderNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final Serializable parentFolderId, final RepositoryFile folder ) {
        return new MockUp<Node>() {
          @Mock
          public String getIdentifier() {
            return NODE_IDENTIFIER;
          }
        }.getMockInstance();
      }

      @Mock
      public String getAbsolutePath( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final Node node ) {
        return ABSOLUTE_PATH;
      }
    };
  }

  private MockUp<JcrRepositoryFileDao> setUpJcrRepositoryFileDaoMock() {
    return new MockUp<JcrRepositoryFileDao>() {
      @Mock
      public RepositoryFile internalGetFileById( Session session, final Serializable fileId, final boolean loadMaps,
          final IPentahoLocale locale ) {
        return file;
      }

      @Mock
      public boolean hasAccess( Session session, Serializable fileId, RepositoryFilePermission... permissions ) {
        return true;
      }

      @Mock
      public boolean hasAccess( RepositoryFile file, RepositoryFilePermission... permissions ) {
        return true;
      }

      @Mock
      public boolean hasUserAccess( RepositoryFile file, RepositoryFilePermission... permissions ) {
        return true;
      }

      @Mock
      public ITransformer<IRepositoryFileData>
        findTransformerForWrite( final Class<? extends IRepositoryFileData> clazz ) {
        return null;
      }

      @Mock
      public RepositoryFile internalGetFile( final Session session, final String absPath, final boolean loadMaps,
          final IPentahoLocale locale ) {
        return file;
      }

      @Mock
      public ITransformer<IRepositoryFileData> findTransformerForRead( final String contentType,
          final Class<? extends IRepositoryFileData> clazz ) {
        return null;
      }
    };
  }

  private MockUp<RepositoryFile.Builder> setUpRepositoryFileBuilderMock() {
    return new MockUp<RepositoryFile.Builder>() {
      @Mock
      public void $init( final RepositoryFile other ) {
      }

      @Mock
      public RepositoryFile build() {
        return null;
      }
    };
  }

  private MockUp<PentahoSessionHolder> setUpPentahoSessionHolderMock() {
    return new MockUp<PentahoSessionHolder>() {
      @Mock
      public IPentahoSession getSession() {
        return pentahoSession;
      }
    };
  }

  private JcrRepositoryFileDao createJcrRepositoryFileDao() {
    List<ITransformer<IRepositoryFileData>> transformers = Collections.emptyList();
    ILockHelper lockHelper = new MockUp<ILockHelper>() {
      @Mock
      void addLockTokenToSessionIfNecessary( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final Serializable fileId ) {
      }
    }.getMockInstance();
    IRepositoryFileAclDao aclDao = new MockUp<IRepositoryFileAclDao>() {
      @Mock
      RepositoryFileAcl createAcl( final Serializable fileId, final RepositoryFileAcl acl ) {
        return null;
      }
    }.getMockInstance();
    IDeleteHelper deleteHelper = new MockUp<IDeleteHelper>() {
      @Mock
      public String getOriginalParentFolderPath( final Session session, final PentahoJcrConstants pentahoJcrConstants,
          final Serializable fileId ) {
        return PARENT_FOLDER_PATH;
      }
    }.getMockInstance();
    IPathConversionHelper conversionHelper = new MockUp<IPathConversionHelper>() {
      @Mock
      public String absToRel( final String absPath ) {
        return "";
      }

      @Mock
      public String relToAbs( final String relPath ) {
        return ABSOLUTE_PATH;
      }
    }.getMockInstance();
    IRepositoryAccessVoterManager accessVoterManager = new MockUp<IRepositoryAccessVoterManager>() {
      @Mock
      public boolean hasAccess( final RepositoryFile file, final RepositoryFilePermission operation,
          final RepositoryFileAcl repositoryFileAcl, final IPentahoSession session ) {
        return true;
      }
    }.getMockInstance();
    return new JcrRepositoryFileDao( transformers, lockHelper, deleteHelper, conversionHelper, aclDao, null,
        accessVoterManager, null );
  }

  @Before
  public void MoksUp() throws RepositoryException {
    jcrRepositoryFileUtilsMockUp = setUpJcrRepositoryFileUtilsMock();
    jcrRepositoryFileAclUtilsMockUp = setUpJcrRepositoryFileAclUtilsMock();
    jcrRepositoryFileDaoMockUp = setUpJcrRepositoryFileDaoMock();
    repositoryFileBuilderMockUp = setUpRepositoryFileBuilderMock();
    pentahoSessionHolderMockUp = setUpPentahoSessionHolderMock();
    new NonStrictExpectations() {
      {
        propertyIterator.hasNext();
        result = false;
      }
      {
        fileNode.getPath();
        result = ABSOLUTE_PATH;
      }
      {
        fileNode.isNodeType( anyString );
        result = true;
      }
      {
        fileNode.getReferences();
        result = propertyIterator;
      }
      {
        fileNode.getIdentifier();
        result = NODE_IDENTIFIER;
      }
      {
        session.save();
      }
      {
        session.getWorkspace();
        result = workspace;
      }
      {
        session.getNodeByIdentifier( FILE_ID );
        result = fileNode;
      }
      {
        session.getItem( anyString );
        result = fileNode;
      }
      {
        file.isVersioned();
        result = false;
      }
      {
        file.getId();
        result = FILE_ID;
      }
      {
        lockManager.getLock( ABSOLUTE_PATH );
        result = null;
      }
      {
        workspace.getVersionManager();
        result = versionManager;
      }
      {
        workspace.getLockManager();
        result = lockManager;
      }
      {
        versionManager.getVersionHistory( ABSOLUTE_PATH );
        result = versionHistory;
      }
      {
        versionHistory.removeVersion( VERSION_ID );
      }
      {
        pentahoSession.getName();
        result = SESSION_NAME;
      }
      {
        referrerProperty.getParent();
        result = fileNode;
      }
      {
        repositoryRequest.getPath();
        result = ABSOLUTE_PATH;
      }
    };
  }

  @After
  public void after() {
    jcrRepositoryFileUtilsMockUp.tearDown();
    jcrRepositoryFileAclUtilsMockUp.tearDown();
    jcrRepositoryFileDaoMockUp.tearDown();
    repositoryFileBuilderMockUp.tearDown();
    pentahoSessionHolderMockUp.tearDown();
  }

  //==============================end of Mock block==========================================================

  @Test
  public void getVersionSummaryTest() throws RepositoryException {
    MockUp<?> jcrRepositoryFileUtilsMockUp = setUpJcrRepositoryFileUtilsMock();

    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getVersionSummary( null, null, null );

    jcrRepositoryFileUtilsMockUp.tearDown();
  }

  @Test
  public void internalCreateFolderTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.internalCreateFolder( session, null, file, fileAcl, null );
  }

  @Test
  public void internalCreateFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.internalCreateFile( session, null, file, null, fileAcl, null );
  }

  @Test
  public void internalUpdateFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.internalUpdateFile( session, file, content, null );
  }

  @Test
  public void internalUpdateFolderTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.internalUpdateFolder( session, file, null );
  }

  @Test
  public void undeleteFileTest() throws RepositoryException, IOException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.undeleteFile( session, null, null );
  }

  @Test
  public void deleteFileTest() throws RepositoryException, IOException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.deleteFile( session, null, null );
  }

  @Test
  public void deleteFileAtVersionTest() throws RepositoryException, IOException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.deleteFileAtVersion( session, FILE_ID, VERSION_ID );
  }

  @Test
  public void getDeletedFilesTest() throws RepositoryException, IOException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getDeletedFiles( session );
    dao.getDeletedFiles( session, null, null );
  }

  @Test
  public void permanentlyDeleteFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.permanentlyDeleteFile( session, FILE_ID, null );
  }

  @Test
  public void getChildrenTest() throws RepositoryException, IOException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getChildren( session, null );
    dao.getChildren( session, FILE_ID, null, false );
  }

  @Test
  public void getDataTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getData( session, FILE_ID, VERSION_ID, fileData.getClass() );
  }

  @Test
  public void checkAndGetFileByIdTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.checkAndGetFileById( session, FILE_ID, false, pentahoLocale );
  }

  @Test
  public void getFileByRelPathTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getFileByRelPath( session, ABSOLUTE_PATH, false, pentahoLocale );
  }

  @Test
  public void internalGetFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.internalGetFile( session, ABSOLUTE_PATH, false, pentahoLocale );
  }

  @Test
  public void isUserTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    Deencapsulation.invoke( dao, "isUser" );
  }

  @Test
  public void internalCopyOrMoveTest() throws RepositoryException, IOException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.internalCopyOrMove( session, file, ABSOLUTE_PATH, null, true );
    dao.internalCopyOrMove( session, file, ABSOLUTE_PATH, null, false );
  }

  @Test
  public void getReferrerFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getReferrerFile( session, new PentahoJcrConstants( session ), referrerProperty );
  }

  @Test
  public void getReferrersTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getReferrers( session, FILE_ID );
  }

  @Test
  public void getTreeTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getTree( session, repositoryRequest );
  }

  @Test
  public void canUnlockFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.canUnlockFile( session, FILE_ID );
  }

  @Test
  public void restoreFileAtVersionTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.restoreFileAtVersion( session, FILE_ID, VERSION_ID, null );
  }

  @Test
  public void getAvailableLocalesForFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getAvailableLocalesForFile( file );
  }

  @Test
  public void getLocalePropertiesForFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getLocalePropertiesForFile( file, null );
  }

  @Test
  public void setLocalePropertiesForFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.setLocalePropertiesForFile( session, file, null, null );
  }

  @Test
  public void deleteLocalePropertiesForFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.deleteLocalePropertiesForFile( session, file, null );
  }

  @Test
  public void lockFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.lockFile( session, FILE_ID, null );
  }

  @Test
  public void unlockFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.unlockFile( session, FILE_ID );
  }

  @Test
  public void getVersionSummariesTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getVersionSummaries( session, FILE_ID );
  }

  @Test
  public void getFileTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getFile( session, FILE_ID, VERSION_ID );
  }

  @Test
  public void internalGetFileByIdTest() {
    jcrRepositoryFileDaoMockUp.tearDown();
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    Deencapsulation.invoke( dao, "internalGetFileById", session, FILE_ID, false, pentahoLocale );
  }

  @Test
  public void hasAccessTest() {
    jcrRepositoryFileDaoMockUp.tearDown();
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    Deencapsulation.invoke( dao, "hasAccess", session, FILE_ID,
        new RepositoryFilePermission[] { RepositoryFilePermission.READ } );
  }
}
