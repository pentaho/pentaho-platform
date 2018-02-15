/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.jcr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.repository2.unified.DefaultRepositoryVersionManager;

public class DefaultRepositoryVersioningManagerTest {
  private static final DefaultRepositoryVersionManager defaultRepositoryVersionManagerFullTrue = new DefaultRepositoryVersionManager();
  private static final DefaultRepositoryVersionManager defaultRepositoryVersionManagerPartialTrue = new DefaultRepositoryVersionManager();
  private static final DefaultRepositoryVersionManager defaultRepositoryVersionManagerFalse = new DefaultRepositoryVersionManager();
  
  private static final String FULL_VERSIONING_PATH = "/dir1/fullVersioning";
  private static final String PART_VERSIONING_PATH = "/dir1/partVersioning";
  private static final String NO_VERSIONING_PATH = "/dir1/noVersioning";

  @Before
  public void setUp() {
    IPlatformMimeResolver platformMimeResolver = mock( IPlatformMimeResolver.class );
    IMimeType mimeTypeMockFullTrue = mock( IMimeType.class );
    IMimeType mimeTypeMockPartialTrue = mock( IMimeType.class );
    IMimeType mimeTypeMockFalse = mock( IMimeType.class );
    when( mimeTypeMockFullTrue.isVersionEnabled() ).thenReturn( true );
    when( mimeTypeMockFullTrue.isVersionCommentEnabled() ).thenReturn( true );
    when( mimeTypeMockPartialTrue.isVersionEnabled() ).thenReturn( true );
    when( mimeTypeMockPartialTrue.isVersionCommentEnabled() ).thenReturn( false );
    when( mimeTypeMockFalse.isVersionEnabled() ).thenReturn( false );
    when( mimeTypeMockFalse.isVersionCommentEnabled() ).thenReturn( true );
    when( platformMimeResolver.resolveMimeTypeForFileName( FULL_VERSIONING_PATH ) ).thenReturn( mimeTypeMockFullTrue );
    when( platformMimeResolver.resolveMimeTypeForFileName( PART_VERSIONING_PATH ) ).thenReturn( mimeTypeMockPartialTrue );
    when( platformMimeResolver.resolveMimeTypeForFileName( NO_VERSIONING_PATH ) ).thenReturn( mimeTypeMockFalse );
    defaultRepositoryVersionManagerFullTrue.setMasterVersioning( true );
    defaultRepositoryVersionManagerFullTrue.setMasterVersionComments( true );
    defaultRepositoryVersionManagerFullTrue.setPlatformMimeResolver( platformMimeResolver );
    defaultRepositoryVersionManagerPartialTrue.setMasterVersioning( true );
    defaultRepositoryVersionManagerPartialTrue.setMasterVersionComments( false );
    defaultRepositoryVersionManagerPartialTrue.setPlatformMimeResolver( platformMimeResolver );
    defaultRepositoryVersionManagerFalse.setMasterVersioning( false );
    defaultRepositoryVersionManagerFalse.setMasterVersionComments( true );
    defaultRepositoryVersionManagerFalse.setPlatformMimeResolver( platformMimeResolver );
  }
  
  /*
   * Test a file set to versioningEnable=True, versionCommentsEnabled=true with various combinations
   * of the master switch.
   */
  @Test
  public void testFullTrue() {
    IRepositoryVersionManager repositoryVersionManager = defaultRepositoryVersionManagerFullTrue;
    assertTrue( repositoryVersionManager.isVersioningEnabled( FULL_VERSIONING_PATH ) );
    assertTrue( repositoryVersionManager.isVersionCommentEnabled( FULL_VERSIONING_PATH ) );
    
    repositoryVersionManager = defaultRepositoryVersionManagerPartialTrue;
    assertTrue( repositoryVersionManager.isVersioningEnabled( FULL_VERSIONING_PATH ) );
    assertFalse( repositoryVersionManager.isVersionCommentEnabled( FULL_VERSIONING_PATH ) );
    
    repositoryVersionManager = defaultRepositoryVersionManagerFalse;
    assertFalse( repositoryVersionManager.isVersioningEnabled( FULL_VERSIONING_PATH ) );
    assertFalse( repositoryVersionManager.isVersionCommentEnabled( FULL_VERSIONING_PATH ) );
  }
  
  /*
   * Test a file set to versioningEnable=True, versionCommentsEnabled=false with various combinations
   * of the master switch.
   */
  @Test
  public void testPartialTrue() {
    IRepositoryVersionManager repositoryVersionManager = defaultRepositoryVersionManagerFullTrue;
    assertTrue( repositoryVersionManager.isVersioningEnabled( PART_VERSIONING_PATH ) );
    assertFalse( repositoryVersionManager.isVersionCommentEnabled( PART_VERSIONING_PATH ) );
    
    repositoryVersionManager = defaultRepositoryVersionManagerPartialTrue;
    assertTrue( repositoryVersionManager.isVersioningEnabled( PART_VERSIONING_PATH ) );
    assertFalse( repositoryVersionManager.isVersionCommentEnabled( PART_VERSIONING_PATH ) );
    
    repositoryVersionManager = defaultRepositoryVersionManagerFalse;
    assertFalse( repositoryVersionManager.isVersioningEnabled( PART_VERSIONING_PATH ) );
    assertFalse( repositoryVersionManager.isVersionCommentEnabled( PART_VERSIONING_PATH ) );
  }
  
  /*
   * Test a file set to versioningEnable=false, versionCommentsEnabled=false with various combinations
   * of the master switch.
   */
  @Test
  public void testFalse() {
    IRepositoryVersionManager repositoryVersionManager = defaultRepositoryVersionManagerFullTrue;
    assertFalse( repositoryVersionManager.isVersioningEnabled( NO_VERSIONING_PATH ) );
    assertFalse( repositoryVersionManager.isVersionCommentEnabled( NO_VERSIONING_PATH ) );
    
    repositoryVersionManager = defaultRepositoryVersionManagerPartialTrue;
    assertFalse( repositoryVersionManager.isVersioningEnabled( NO_VERSIONING_PATH ) );
    assertFalse( repositoryVersionManager.isVersionCommentEnabled( NO_VERSIONING_PATH ) );
    
    repositoryVersionManager = defaultRepositoryVersionManagerFalse;
    assertFalse( repositoryVersionManager.isVersioningEnabled( NO_VERSIONING_PATH ) );
    assertFalse( repositoryVersionManager.isVersionCommentEnabled( NO_VERSIONING_PATH ) );
  }
}
