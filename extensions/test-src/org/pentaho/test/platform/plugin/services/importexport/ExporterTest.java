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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.importexport;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.importexport.Exporter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExporterTest {
  private static final Integer DIR_ID = 0;
  private static final Integer FILE_ID = 1;

  private Exporter exporter;

  @Before
  public void setUp() throws Exception {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    exporter = new Exporter( repositoryMock );
  }


  @Test( expected = IllegalArgumentException.class )
  public void exportDirectory_whenDirectoryIsNull() throws Exception {
    exporter.exportDirectory( null, null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void exportDirectory_whenDirectoryIsFile() throws Exception {
    exporter.exportDirectory( createVirtualFile(), null );
  }

  @Test( expected = IOException.class )
  public void exportDirectory_whenDestinationDirectoryIsNull() throws Exception {
    exporter.exportDirectory( createVirtualFolder(), null );
  }


  @Test( expected = IOException.class )
  public void exportFile_whenFileIsNull() throws Exception {
    exporter.exportFile( null, null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void exportFile_whenDestinationDirectoryIsFile() throws Exception {
    File destDir = mock( File.class );
    when( destDir.exists() ).thenReturn( Boolean.TRUE );
    when( destDir.isDirectory() ).thenReturn( Boolean.FALSE );

    exporter.exportFile( createVirtualFolder(), destDir );
  }


  private static RepositoryFile createVirtualFolder() {
    return createRepositoryElement( DIR_ID, "dir", true );
  }

  private static RepositoryFile createVirtualFile() {
    return createRepositoryElement( FILE_ID, "file", false );
  }

  private static RepositoryFile createRepositoryElement( Serializable id, String name, boolean isFolder ) {
    return new RepositoryFile( id, name, isFolder, false, false, null, "", new Date(), new Date(), false, null, null,
      null, "en_US", "", "", "/", null, 1, "",
      Collections.<String, Properties>emptyMap() );
  }
}
