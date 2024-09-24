/*!
 *
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
 *
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileExtraMetaData;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifestEntity;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifestFormatException;

public class ImportSessionTest {
  private static final String PATH = "path";

  private ImportSession importSession;

  @Before
  public void setUp() {
    importSession = ImportSession.getSession();
    importSession.initialize();
  }

  @Test
  public void testSettingAclProperties() {
    importSession.setAclProperties( true, true, true );
    assertTrue( importSession.isApplyAclSettings() );
    assertTrue( importSession.isRetainOwnership() );
    assertTrue( importSession.isOverwriteAclSettings() );

    importSession.setAclProperties( false, true, true );
    assertFalse( importSession.isApplyAclSettings() );
    assertTrue( importSession.isRetainOwnership() );
    assertTrue( importSession.isOverwriteAclSettings() );

    importSession.setAclProperties( false, false, true );
    assertFalse( importSession.isApplyAclSettings() );
    assertFalse( importSession.isRetainOwnership() );
    assertTrue( importSession.isOverwriteAclSettings() );

    importSession.setAclProperties( false, false, false );
    assertFalse( importSession.isApplyAclSettings() );
    assertFalse( importSession.isRetainOwnership() );
    assertFalse( importSession.isOverwriteAclSettings() );

    importSession.setAclProperties( true, false, false );
    assertTrue( importSession.isApplyAclSettings() );
    assertFalse( importSession.isRetainOwnership() );
    assertFalse( importSession.isOverwriteAclSettings() );

    importSession.setAclProperties( true, true, false );
    assertTrue( importSession.isApplyAclSettings() );
    assertTrue( importSession.isRetainOwnership() );
    assertFalse( importSession.isOverwriteAclSettings() );
  }

  @Test
  public void fileHiddenPropertyIsNullWhenAclIsDisabled() {
    importSession.setAclProperties( false, true, false );
    assertNull( importSession.isFileHidden( PATH ) );
  }

  @Test
  public void fileHiddenPropertyIsDefinedWhenAclIsEnabled() {
    importSession.setAclProperties( true, false, false );

    RepositoryFile virtualFile = mock( RepositoryFile.class );
    when( virtualFile.isHidden() ).thenReturn( Boolean.TRUE );

    ExportManifestEntity fake = mock( ExportManifestEntity.class );
    when( fake.getRepositoryFile() ).thenReturn( virtualFile );

    ExportManifest manifest = mock( ExportManifest.class );
    when( manifest.getExportManifestEntity( PATH ) ).thenReturn( fake );

    importSession.setManifest( manifest );
    assertEquals( virtualFile.isHidden(), importSession.isFileHidden( PATH ) );
  }

  @Test
  public void testClearSession() {
    ExportManifest manifest = mock( ExportManifest.class );
    ImportSession.getSession().setManifest( manifest );
    assertNotNull( ImportSession.getSession().getManifest() );
    ImportSession.clearSession();
    assertNull( ImportSession.getSession().getManifest() );
  }

  @Test
  public void testProcessExtraMetaDataForFile() throws ExportManifestFormatException {
    ExportManifest manifest = mock( ExportManifest.class );
    ExportManifestEntity entity = mock( ExportManifestEntity.class );
    RepositoryFileExtraMetaData repositoryFileExtraMetaData = mock( RepositoryFileExtraMetaData.class );

    when( manifest.getExportManifestEntity( nullable( String.class ) )).thenReturn( entity );
    when( entity.getRepositoryFileExtraMetaData() ).thenReturn( repositoryFileExtraMetaData );

    importSession.setManifest( manifest );
    Assert.assertEquals( repositoryFileExtraMetaData, importSession.processExtraMetaDataForFile( "filePath" ) );

  }
}
