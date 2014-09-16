package org.pentaho.platform.repository2.unified.jcr;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoter;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.RepositoryAccessVoterManager;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

public class JcrRepositoryFileDaoFacadeTest {

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
  @Mocked
  private IRepositoryAccessVoter accessVoter;

  private Properties properties = new Properties();

  private final static String PARENT_ID = "PARENT_ID";
  private final static String FILE_ID = "FILE_ID";
  private final static String VERSION_ID = "VERSION_ID";
  private final static String NODE_IDENTIFIER = "NODE_IDENTIFIER";
  // private final static String NODE_PATH = "NODE_PATH";
  private final static String ABSOLUTE_PATH = "/ABSOLUTE_PATH/";
  private final static String PARENT_FOLDER_PATH = "/PARENT_FOLDER_PATH";
  private final static String CONTENT_TYPE = "CONTENT_TYPE";
  private final static String SESSION_NAME = "SESSION_NAME";
  private final static String ADMIN_USERNAME = "ADMIN_USERNAME";
  private final static String LOCALE = Locale.US.toString();

  private MockUp<?> jcrRepositoryFileUtilsMockUp;
  private MockUp<?> jcrRepositoryFileAclUtilsMockUp;
  private MockUp<?> jcrRepositoryFileDaoMockUp;
  private MockUp<?> repositoryFileBuilderMockUp;
  private MockUp<?> pentahoSessionHolderMockUp;
  private MockUp<?> jcrTemplateMockUp;

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
      public Map<String, Serializable> getFileMetadata( final Session session, final Serializable fileId ) {
        return Collections.emptyMap();
      }

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
      public Object getVersionSummaries( final Session session, final PentahoJcrConstants pentahoJcrConstants,
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
      public Object getVersionSummary( final Session session, final PentahoJcrConstants pentahoJcrConstants,
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
      public RepositoryFile internalGetFileById( final Serializable fileId, final boolean loadMaps,
          final IPentahoLocale locale ) {
        return file;
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

  private MockUp<JcrTemplate> setUpJcrTemplateMock() {
    return new MockUp<JcrTemplate>() {
      @Mock
      public Object execute( JcrCallback action ) throws IOException, RepositoryException {
        return action.doInJcr( session );
      }
    };
  }

  private void mockHasAccessForAccessVoterManager( final boolean value,
      final RepositoryFilePermission repositoryFilePermission ) {
    new NonStrictExpectations() {
      {
        accessVoter.hasAccess( (RepositoryFile) any, repositoryFilePermission, (RepositoryFileAcl) any,
            (IPentahoSession) any );
        result = value;
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
    JcrTemplate jcrTemplate = new JcrTemplate();

    IAuthorizationPolicy authorizationPolicy = new MockUp<IAuthorizationPolicy>() {
      @Mock
      boolean isAllowed( final String actionName ) {
        return ADMIN_USERNAME.equals( actionName );
      }
    }.getMockInstance();
    RepositoryAccessVoterManager accessVoterManager =
        new RepositoryAccessVoterManager( Collections.singletonList( accessVoter ), authorizationPolicy, ADMIN_USERNAME );

    return new JcrRepositoryFileDao( jcrTemplate, transformers, lockHelper, deleteHelper, conversionHelper, aclDao,
        null, accessVoterManager );
  }

  @Before
  public void MoksUp() throws RepositoryException {
    jcrRepositoryFileUtilsMockUp = setUpJcrRepositoryFileUtilsMock();
    jcrRepositoryFileAclUtilsMockUp = setUpJcrRepositoryFileAclUtilsMock();
    jcrRepositoryFileDaoMockUp = setUpJcrRepositoryFileDaoMock();
    repositoryFileBuilderMockUp = setUpRepositoryFileBuilderMock();
    pentahoSessionHolderMockUp = setUpPentahoSessionHolderMock();
    jcrTemplateMockUp = setUpJcrTemplateMock();
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
        file.isFolder();
        result = true;
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
    mockHasAccessForAccessVoterManager( true, RepositoryFilePermission.READ );
    mockHasAccessForAccessVoterManager( false, RepositoryFilePermission.DELETE );
  }

  @After
  public void after() {
    jcrRepositoryFileUtilsMockUp.tearDown();
    jcrRepositoryFileAclUtilsMockUp.tearDown();
    jcrRepositoryFileDaoMockUp.tearDown();
    repositoryFileBuilderMockUp.tearDown();
    pentahoSessionHolderMockUp.tearDown();
    jcrTemplateMockUp.tearDown();
  }

  // ==============================end of Mock block==========================================================

  @Test
  public void canUnlockFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.canUnlockFile( FILE_ID );
  }

  @Test
  public void copyFile() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.copyFile( FILE_ID, ABSOLUTE_PATH, null );
  }

  @Test
  public void createFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    new NonStrictExpectations() {
      {
        file.isFolder();
        result = false;
      }
    };
    dao.createFile( null, file, null, fileAcl, null );
  }

  @Test
  public void createFolderTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.createFolder( null, file, fileAcl, null );
  }

  @Test
  public void deleteFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.deleteFile( FILE_ID, null );
  }

  @Test
  public void deleteFileAtVersionTest() throws RepositoryException {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    new NonStrictExpectations() {
      {
        versionHistory.removeVersion( VERSION_ID );
        result = new RepositoryException();
      }
    };
    dao.deleteFileAtVersion( FILE_ID, VERSION_ID );

    mockHasAccessForAccessVoterManager( true, RepositoryFilePermission.DELETE );
    try {
      dao.deleteFileAtVersion( FILE_ID, VERSION_ID );
      Assert.fail( "Myst be Exception" );
    } catch ( Exception e ) {
      // test successful
    }
  }

  @Test
  public void deletepentahoLocalePropertiesForFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.deleteLocalePropertiesForFile( file, LOCALE );
  }

  @Test
  public void getAvailablepentahoLocalesForFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getAvailableLocalesForFile( file );
  }

  @Test
  public void getAvailablepentahoLocalesForFileByIdTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getAvailableLocalesForFileById( FILE_ID );
  }

  @Test
  public void getAvailablepentahoLocalesForFileByPathTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getAvailableLocalesForFileByPath( ABSOLUTE_PATH );
  }

  @Test
  public void getChildrenTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getChildren( repositoryRequest );
    dao.getChildren( FILE_ID, null, true );
  }

  @Test
  public void getDataTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getData( FILE_ID, VERSION_ID, fileData.getClass() );
  }

  @Test
  public void getDeletedFilesTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getDeletedFiles();
    dao.getDeletedFiles( PARENT_FOLDER_PATH, null );
  }

  @Test
  public void getFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getFile( FILE_ID, VERSION_ID );
    dao.getFile( ABSOLUTE_PATH );
    dao.getFile( ABSOLUTE_PATH, false, pentahoLocale );
    dao.getFile( ABSOLUTE_PATH, false );
    dao.getFile( ABSOLUTE_PATH, pentahoLocale );
  }

  @Test
  public void getFileByAbsolutePathTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getFileByAbsolutePath( ABSOLUTE_PATH );
  }

  @Test
  public void getFileByIdTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getFileById( FILE_ID );
    dao.getFileById( FILE_ID, false, pentahoLocale );
    dao.getFileById( FILE_ID, false );
    dao.getFileById( FILE_ID, pentahoLocale );
  }

  @Test
  public void getFileMetadataTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getFileMetadata( FILE_ID );
  }

  @Test
  public void getpentahoLocalePropertiesForFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getLocalePropertiesForFile( file, null );
  }

  @Test
  public void getpentahoLocalePropertiesForFileByIdTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getLocalePropertiesForFileById( FILE_ID, null );
  }

  @Test
  public void getpentahoLocalePropertiesForFileByPathTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getLocalePropertiesForFileByPath( ABSOLUTE_PATH, null );
  }

  @Test
  public void getReferrersTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getReferrers( FILE_ID );
  }

  @Test
  public void getReservedChars() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getReservedChars();
  }

  @Test
  public void getTreeTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getTree( repositoryRequest );
    dao.getTree( ABSOLUTE_PATH, 0, null, false );
  }

  @Test
  public void getVersionSummariesTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getVersionSummaries( FILE_ID );
  }

  @Test
  public void getVersionSummaryTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.getVersionSummary( FILE_ID, null );
  }

  @Test
  public void lockFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.lockFile( FILE_ID, null );
  }

  @Test
  public void moveFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.moveFile( FILE_ID, ABSOLUTE_PATH, null );
  }

  @Test
  public void permanentlyDeleteFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.permanentlyDeleteFile( FILE_ID, null );
  }

  @Test
  public void restoreFileAtVersionTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.restoreFileAtVersion( FILE_ID, VERSION_ID, null );
  }

  @Test
  public void setpentahoLocalePropertiesForFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.setLocalePropertiesForFile( file, LOCALE, properties );
  }

  @Test
  public void setpentahoLocalePropertiesForFileByIdTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.setLocalePropertiesForFileById( FILE_ID, LOCALE, properties );
  }

  @Test
  public void undeleteFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.undeleteFile( FILE_ID, null );
  }

  @Test
  public void unlockFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.unlockFile( FILE_ID );
  }

  @Test
  public void updateFileTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    new NonStrictExpectations() {
      {
        file.isFolder();
        result = false;
      }
    };
    dao.updateFile( file, content, null );
  }

  @Test
  public void updateFolderTest() {
    JcrRepositoryFileDao dao = createJcrRepositoryFileDao();
    dao.updateFolder( file, null );
  }

  @Test
  public void repositoryAccessVoterTest() {

  }

}
