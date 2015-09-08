package org.pentaho.platform.plugin.services.exporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.TestAuthorizationPolicy;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class PentahoPlatformExporterIT {

  PentahoPlatformExporter platformExporter;
  FileSystemBackedUnifiedRepository repo;

  private String tmpZipFileName;
  private MicroPlatform mp = new MicroPlatform( "test-src/solution" );

  @Before
  public void setUp() throws Exception {
    final TemporaryFolder tmpFolder = new TemporaryFolder();
    tmpFolder.create();
    tmpZipFileName = tmpFolder.getRoot().getAbsolutePath() + File.separator + "test.zip";

    NameBaseMimeResolver mimeResolver = mock( NameBaseMimeResolver.class );
    IRepositoryContentConverterHandler converterHandler = mock( IRepositoryContentConverterHandler.class );

    mp.define( ISolutionEngine.class, SolutionEngine.class );
    mp.define( IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class
      , IPentahoDefinableObjectFactory.Scope.GLOBAL );
    mp.define( IAuthorizationPolicy.class, TestAuthorizationPolicy.class );
    mp.define( IAuthorizationAction.class, AdministerSecurityAction.class );
    mp.define( DefaultExportHandler.class, DefaultExportHandler.class );
    mp.defineInstance( IRepositoryContentConverterHandler.class, converterHandler );
    mp.defineInstance( NameBaseMimeResolver.class, mimeResolver );

    repo = spy( (FileSystemBackedUnifiedRepository) PentahoSystem.get( IUnifiedRepository.class ) );
    repo.setRootDir( new File( "test-src/solution" ) );

    StandaloneSession session = new StandaloneSession();
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
    PentahoSessionHolder.setSession( session );

    platformExporter = new PentahoPlatformExporter( repo );

    doReturn( new RepositoryFileAcl( "id", new RepositoryFileSid( "name" ), true, new ArrayList<RepositoryFileAce>() ) )
      .when( repo ).getAcl( any( Serializable.class ) );

    doReturn( new ArrayList<Locale>() ).when( repo ).getAvailableLocalesForFileById( any( Serializable.class ) );

  }

  @Test
  public void testExport() throws Exception {
    File export = platformExporter.performExport( null );
    assertNotNull( export );
  }

}
