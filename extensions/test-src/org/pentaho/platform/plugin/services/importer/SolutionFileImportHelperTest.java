package org.pentaho.platform.plugin.services.importer;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.util.Assert;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.mockito.Mockito.*;

public class SolutionFileImportHelperTest {

  public SolutionFileImportHelper solutionFileImportHelper;
  public IPlatformMimeResolver mockResolver;

  @Before
  public void setUp() {
    mockResolver = mock( IPlatformMimeResolver.class );
    PentahoSystem.registerObject( mockResolver );

    IMimeType mimeType = mock( IMimeType.class );
    when( mimeType.getConverter() ).thenReturn( null );
    when( mockResolver.resolveMimeTypeForFileName( anyString() ) ).thenReturn( mimeType );

    when( mimeType.isHidden() ).thenReturn( false );

    solutionFileImportHelper = spy( new SolutionFileImportHelper() );
  }

  @Test
  public void testIsInApprovedExtensionList() {
    Assert.assertFalse( solutionFileImportHelper.isInApprovedExtensionList( "fileName" ) );
  }

  @Test
  public void testIsInHiddenList() {
    Assert.assertFalse( solutionFileImportHelper.isInHiddenList( "fileName" ) );
  }

}
