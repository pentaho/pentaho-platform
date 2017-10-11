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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */
package org.pentaho.platform.plugin.services.importer;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.junit.Test;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryDefaultAclHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.core.mimetype.MimeType;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository.ICurrentUserProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 
 * @author tkafalas
 *
 */
public class RepositoryFileImportFileHandlerTest {
  private static final String USER_NAME = "__root__"; // The mock unified repository gives this user full access
  private static final String USER_NAME2 = "mickeyMouse"; // Used with acls
  private static final String PATH = "/public/path1/path2";
  //we use it as file name when we import file and we use it as folder name when we import the folder
  private static final String TARGET_RESOURCE_NAME = "resourceName.dum";
  private static final String TARGET_RESOURCE_NAME_NOEXT = "resourceName";
  private static final String TARGET_RESOURCE_NAME_ERREXT = "resourceName.err";
  private static final String MIMENAME = "mimeName";
  private static final String MIME_EXTENSION = "dum";
  DefaultTenantedPrincipleNameResolver principleNameResolver = new DefaultTenantedPrincipleNameResolver();

  RepositoryFileImportFileHandler fileHandler;
  ImportSession importSession;
  IUnifiedRepository mockRepository;
  RepositoryFileImportBundle mockBundle;
  UserProvider userProvider;

  @Test
  public void testGetImportSession() {
    setup( MIMENAME, MIME_EXTENSION, "", "", false );
    assertNotNull( importSession );
  }

  @Test
  public void testGetLogger() {
    setup( MIMENAME, MIME_EXTENSION, "", "", false );
    Log logger = fileHandler.getLogger();
    assertNotNull( logger );
  }

  @Test
  public void testGetMimeTypeMap() {
    setup( MIMENAME, MIME_EXTENSION, "", "", false );
    Map<String, IMimeType> map = fileHandler.getMimeTypeMap();
    assertNotNull( map );
    assertNotNull( map.get( MIMENAME ) );
  }

  @Test
  public void testImportNewFileWithNoManifest() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasDefaultPermissions( acl );
    assertHasDefaultOwner( acl );
  }

  @Test
  public void testImportNewFileWithNoManifestNoEXT() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.targetName( TARGET_RESOURCE_NAME_NOEXT ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME_NOEXT );
    assertNull( repositoryFile );
  }

  @Test
  public void testImportNewFileWithNoManifestNoMime() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.mimename( null ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
    assertNull( repositoryFile );
  }

  @Test
  public void testImportNewFileWithNoManifestMimeNotRegistred() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.targetName( TARGET_RESOURCE_NAME_ERREXT ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
    assertNull( repositoryFile );
  }

  @Test
  public void testImportNewFileWithManifestApplied() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.hasManifest( true ).applyAclSettings( true )
    .overwriteAclSettings( true ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasManifestPermissions( acl );
    assertHasManifestOwner( acl );
  }

  @Test
  public void testImportNewFileWithManifestPermissionsApplied() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.hasManifest( true ).applyAclSettings( true ).overwriteAclSettings( true )
        .retainOwnership( true ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasManifestPermissions( acl );
    assertHasDefaultOwner( acl );
  }

  @Test
  public void testImportNewFileWithManifestOwnerApplied() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.hasManifest( true ).applyAclSettings( false ).overwriteAclSettings( true ).retainOwnership(
        false ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasDefaultPermissions( acl );
    assertHasManifestOwner( acl );
  }

  @Test
  public void testImportExistingFileWithManifestApplied() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();

    importTesterBuilder.fileExists( true ).hasManifest( true )
    .overwriteFileIfExists( true ).applyAclSettings( true )
    .overwriteAclSettings( true ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasManifestPermissions( acl );
    assertHasManifestOwner( acl );

  }

  /*
   * This case tests attempt to overwrite a file when flag does not allow overwriting the file
   */
  @Test
  public void testNonImportExistingFileWithManifestApplied() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    try {
      importTesterBuilder.fileExists( true ).hasManifest( true ).overwriteFileIfExists( false ).applyAclSettings( true )
          .overwriteAclSettings( true ).build().initialSetup().execute();
    } catch ( PlatformImportException e ) {
      // This test should throw this exception. Check that the original file exists
      RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
      assertNotNull( repositoryFile );
      RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
      assertHasDefaultPermissions( acl );
      assertHasDefaultOwner( acl );
      return;
    }
    fail( "This test should have thrown a PlatformImportException" );
  }

  @Test
  public void testImportExistingFileWithManifestNotApplied() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.fileExists( true ).hasManifest( true ).retainOwnership( true ).overwriteFileIfExists( true )
        .build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasDefaultPermissions( acl );
    assertHasDefaultOwner( acl );
  }

  @Test
  public void testImportExistingFileWithManifestButOverwriteAclDisabled() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.fileExists( true ).hasManifest( true ).retainOwnership( false ).applyAclSettings( true )
        .overwriteAclSettings( false ).overwriteFileIfExists( true ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasDefaultPermissions( acl );
    assertHasManifestOwner( acl ); // ******* I don't think this is right *******
  }

  @Test
  public void testImportExistingFileWithManifestPermissions() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();

    importTesterBuilder.fileExists( true ).hasManifest( true ).overwriteFileIfExists( true ).retainOwnership( true )
        .applyAclSettings( true ).overwriteAclSettings( true ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasManifestPermissions( acl );
    assertHasDefaultOwner( acl );
  }

  @Test
  public void testImportExistingFileWithManifestOwner() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();

    importTesterBuilder.fileExists( true ).hasManifest( true ).overwriteFileIfExists( true ).retainOwnership( false )
        .applyAclSettings( true ).overwriteAclSettings( false ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( PATH + "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasDefaultPermissions( acl );
    assertHasManifestOwner( acl );
  }

  @Test
  public void testImportNewFolderWithNoManifest() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.folder( true ).path( "/" ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    assertTrue( repositoryFile.isFolder() );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasDefaultPermissions( acl );
    assertHasDefaultOwner( acl );
  }

  @Test
  public void testImportNewFolderWithManifestApplied() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.folder( true ).path( "/" ).hasManifest( true ).
    applyAclSettings( true ).overwriteAclSettings( true ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    assertTrue( repositoryFile.isFolder() );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasManifestPermissions( acl );
    assertHasManifestOwner( acl );
  }

  @Test
  public void testImportNewFolderWithManifestPermissionsApplied() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.folder( true ).path( "/" ).hasManifest( true )
    .applyAclSettings( true ).overwriteAclSettings( true )
    .retainOwnership( true ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    assertTrue( repositoryFile.isFolder() );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasManifestPermissions( acl );
    assertHasDefaultOwner( acl );
  }

  @Test
  public void testImportNewFolderWithManifestOwnerApplied() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.folder( true ).path( "/" ).hasManifest( true )
    .applyAclSettings( false ).overwriteAclSettings( true )
    .retainOwnership( false ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( "/" + TARGET_RESOURCE_NAME );
    assertNotNull( repositoryFile );
    assertTrue( repositoryFile.isFolder() );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasDefaultPermissions( acl );
    assertHasManifestOwner( acl );
  }

  @Test
  public void testImportExistingFolderWithManifestApplied() throws Exception {
    ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
    importTesterBuilder.folder( true ).path( "/public" ).targetName( TARGET_RESOURCE_NAME )
    .fileExists( true ).hasManifest( true )
    .overwriteFileIfExists( true ).applyAclSettings( true )
    .overwriteAclSettings( true ).build().initialSetup().execute();

    RepositoryFile repositoryFile = mockRepository.getFile( "/public/" + TARGET_RESOURCE_NAME  );
    assertNotNull( repositoryFile );
    assertTrue( repositoryFile.isFolder() );
    RepositoryFileAcl acl = mockRepository.getAcl( repositoryFile.getId() );
    assertHasManifestPermissions( acl );
    assertHasManifestOwner( acl );
  }

  @Test
  public void testExtensionNotTruncated() {
    String name = "file.csv";
    setup( MIMENAME, MIME_EXTENSION, "", "", false );
    String title = fileHandler.getTitle( name );
    assertEquals( name, title );
  }

  @Test
  public void testExtensionTruncated() {
    String name = "file.prpt";
    setup( MIMENAME, MIME_EXTENSION, "", "", false );
    String title = fileHandler.getTitle( name );
    assertEquals( "file", title );
  }

  @Test
  public void testFileWithoutExtension() {
    String name = "file";
    setup( MIMENAME, MIME_EXTENSION, "", "", false );
    String title = fileHandler.getTitle( name );
    assertEquals( name, title );
  }

  private void assertHasDefaultPermissions( RepositoryFileAcl acl ) {
    assertNotNull( acl );
    assertTrue( acl.isEntriesInheriting() );
    assertNotNull( acl.getAces() );
    assertEquals( 0, acl.getAces().size() );
  }

  private void assertHasManifestPermissions( RepositoryFileAcl acl ) {
    assertNotNull( acl );
    assertFalse( acl.isEntriesInheriting() );
    assertNotNull( acl.getAces() );
    assertEquals( 2, acl.getAces().size() );
    EnumSet<RepositoryFilePermission> permissions = acl.getAces().get( 0 ).getPermissions();
    assertNotNull( permissions );
    assertEquals( 3, permissions.size() );
  }

  private void assertHasDefaultOwner( RepositoryFileAcl acl ) {
    assertNotNull( acl );
    assertNotNull( acl.getOwner() );
    assertEquals( USER_NAME, principleNameResolver.getPrincipleName( acl.getOwner().getName() ) );
  }

  private void assertHasManifestOwner( RepositoryFileAcl acl ) {
    assertNotNull( acl );
    assertNotNull( acl.getOwner() );
    assertEquals( USER_NAME2, principleNameResolver.getPrincipleName( acl.getOwner().getName() ) );
  }

  private ExportManifest createExportManifest( RepositoryFile repoFile, RepositoryFileAcl acl ) throws Exception {
    String path = repoFile.getPath();
    String parentFolder = path.substring( 0, path.lastIndexOf( "/" ) );
    ExportManifest manifest = new ExportManifest();
    manifest.getManifestInformation().setManifestVersion( "2" );
    manifest.getManifestInformation().setRootFolder( parentFolder );
    manifest.add( repoFile, acl );
    return manifest;
  }

  private RepositoryFileAcl createRepositoryFileAcl2() {
    final RepositoryFileSid sid = new RepositoryFileSid( USER_NAME2 );
    final boolean inheriting = false;
    final RepositoryFileAce ace1 =
        new RepositoryFileAce( sid, RepositoryFilePermission.READ, RepositoryFilePermission.WRITE,
            RepositoryFilePermission.DELETE );
    final RepositoryFileAce ace2 =
        new RepositoryFileAce( new RepositoryFileSid( USER_NAME ), RepositoryFilePermission.READ,
            RepositoryFilePermission.WRITE, RepositoryFilePermission.DELETE );
    final List<RepositoryFileAce> aces = Arrays.asList( ace1, ace2 );
    return new RepositoryFileAcl( "", sid, inheriting, aces );
  }

  private void setup( String mimeTypeName, String extension, String path, String fileName, boolean folder ) {
    List<String> extensions = Arrays.asList( extension );
    IMimeType mimeType = new MimeType( mimeTypeName, extensions );
    mimeType.setConverter( mock( Converter.class ) );
    List<IMimeType> mimeTypeList = Arrays.asList( mimeType );
    NameBaseMimeResolver mimeResolver = new NameBaseMimeResolver();
    mimeResolver.addMimeType( mimeType );
    SolutionFileImportHelper.testMimeResolver = mimeResolver;

    userProvider = new UserProvider();
    mockRepository = new MockUnifiedRepository( userProvider );
    fileHandler = new RepositoryFileImportFileHandler( mimeTypeList );
    fileHandler.setRepository( mockRepository );
    fileHandler.setDefaultAclHandler( new DefaultAclHandler() );
    fileHandler.setKnownExtensions( Arrays.asList( "prpt" ) );
    IPlatformImporter mockPlatformImporter = mock( IPlatformImporter.class );
    when( mockPlatformImporter.getRepositoryImportLogger() ).thenReturn( new Log4JRepositoryImportLogger() );
    ImportSession.iPlatformImporter = mockPlatformImporter;
    importSession = fileHandler.getImportSession();
    importSession.initialize();

    mockBundle = mock( RepositoryFileImportBundle.class );
    when( mockBundle.getPath() ).thenReturn( path );
    when( mockBundle.getName() ).thenReturn( fileName );
    when( mockBundle.getMimeType() ).thenReturn( mimeTypeName );
    when( mockBundle.isFolder() ).thenReturn( folder );
  }

  public class ImportTester {
    private final boolean fileExists;
    private final boolean overwriteFileIfExists;
    private final boolean hasManifest;
    private final boolean applyAclSettings;
    private final boolean overwriteAclSettings;
    private final boolean setRetainOwnership;
    private final String path;
    private final String targetName;
    private final String mimename;
    private final String mimeExtension = MIME_EXTENSION;
    private final boolean folder;

    public ImportTester( boolean fileExists,
        boolean overwriteFileIfExists, boolean hasManifest,
        boolean applyAclSettings, boolean overwriteAclSettings,
        boolean setRetainOwnership, boolean folder, String path,
        String targetName, String mimename ) {
      this.fileExists = fileExists;
      this.overwriteFileIfExists = overwriteFileIfExists;
      this.hasManifest = hasManifest;
      this.applyAclSettings = applyAclSettings;
      this.overwriteAclSettings = overwriteAclSettings;
      this.setRetainOwnership = setRetainOwnership;
      this.folder = folder;
      this.path = path;
      this.targetName = targetName;
      this.mimename = mimename;
    }

    public ImportTester initialSetup() {
      setup( mimename, mimeExtension, path, targetName, folder );
      return this;
    }

    public void execute() throws Exception {

      if ( fileExists ) {
        // First we need to import something into our blank repo
        ImportTestBuilder importTesterBuilder = new ImportTestBuilder();
        importTesterBuilder.path( this.path ).folder( this.folder ).build().execute();
        importSession.initialize(); // Don't carry the import state into the real import
      }

      RepositoryFile repoFile = new RepositoryFile.Builder( targetName )
          .path( path ).folder( this.folder ).build();
      when( mockBundle.isOverwriteInRepository() ).thenReturn( overwriteFileIfExists );
      when( mockBundle.overwriteInRepossitory() ).thenReturn( overwriteFileIfExists );
      when( mockBundle.getFile() ).thenReturn( repoFile );

      ExportManifest manifest = null;
      if ( hasManifest ) {
        // Setup manifest
        RepositoryFileAcl acl = createRepositoryFileAcl2();
        when( mockBundle.getAcl() ).thenReturn( acl );
        manifest = createExportManifest( repoFile, acl );
      }

      // setup importFlags
      importSession.setManifest( manifest );
      importSession.setApplyAclSettings( applyAclSettings );
      importSession.setOverwriteAclSettings( overwriteAclSettings );
      importSession.setRetainOwnership( setRetainOwnership );

      fileHandler.importFile( mockBundle );
    }
  }

  /*
   * Facilitates the creation of an ImportTester instance.
   */
  public class ImportTestBuilder {
    boolean fileExists;
    boolean hasManifest;
    boolean applyAclSettings;
    boolean overwriteAclSettings;
    boolean retainOwnership;
    boolean overwriteFileIfExists;
    boolean folder;
    //by default it is simular with home folder
    String path = PATH;
    //by default it is correct name
    String targetName = TARGET_RESOURCE_NAME;
    String mimename = MIMENAME;

    public ImportTestBuilder mimename( final String mimename ) {
      this.mimename = mimename;
      return this;
    }

    public ImportTestBuilder targetName( final String targetName ) {
      this.targetName = targetName;
      return this;
    }

    public ImportTestBuilder path( final String path ) {
      this.path = path;
      return this;
    }

    public ImportTestBuilder fileExists( final boolean fileExists1 ) {
      this.fileExists = fileExists1;
      return this;
    }

    public ImportTestBuilder overwriteFileIfExists( final boolean overwriteFileIfExists1 ) {
      this.overwriteFileIfExists = overwriteFileIfExists1;
      return this;
    }

    public ImportTestBuilder hasManifest( final boolean hasManifest1 ) {
      this.hasManifest = hasManifest1;
      return this;
    }

    public ImportTestBuilder applyAclSettings( final boolean applyAclSettings1 ) {
      this.applyAclSettings = applyAclSettings1;
      return this;
    }

    public ImportTestBuilder overwriteAclSettings( final boolean overwriteAclSettings1 ) {
      this.overwriteAclSettings = overwriteAclSettings1;
      return this;
    }

    public ImportTestBuilder retainOwnership( final boolean retainOwnership1 ) {
      this.retainOwnership = retainOwnership1;
      return this;
    }

    public ImportTestBuilder folder( final boolean folder1 ) {
      this.folder = folder1;
      return this;
    }

    public ImportTester build() {
      ImportTester tester =
          new ImportTester( fileExists, overwriteFileIfExists,
              hasManifest, applyAclSettings, overwriteAclSettings,
              retainOwnership, folder, path, targetName, mimename );
      return tester;
    }
  }

  public class UserProvider implements ICurrentUserProvider {
    public String getUser() {
      return USER_NAME;
    }

    public List<String> getRoles() {
      return Arrays.asList( "__everyone__", "role2"  );
    }
  }

  public class DefaultAclHandler implements IRepositoryDefaultAclHandler {
    @Override
    public RepositoryFileAcl createDefaultAcl( RepositoryFile repositoryFile ) {
      return new RepositoryFileAcl.Builder( USER_NAME ).entriesInheriting( true ).build();
    }
  }
}
