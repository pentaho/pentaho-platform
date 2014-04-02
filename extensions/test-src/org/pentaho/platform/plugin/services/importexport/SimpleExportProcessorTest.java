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

package org.pentaho.platform.plugin.services.importexport;
/*
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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class SimpleExportProcessorTest {
  static String path = "/temp";
  static IUnifiedRepository unifiedRepository;
  static RepositoryFile repositoryFile;
  static DefaultExportHandler defaultExportHandler;
  static InputStream inputStream;

  @Before
  public void setUp() throws Exception {
    unifiedRepository = mock( IUnifiedRepository.class );
    defaultExportHandler = mock( DefaultExportHandler.class );
    repositoryFile = mock( RepositoryFile.class );

    inputStream = mock( InputStream.class );

    when( defaultExportHandler.doExport( repositoryFile, path ) ).thenReturn( inputStream );
    doNothing().when( inputStream ).close();
    // doNothing().when(inputStream).read(anyListOf(Byte.class));
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testPerformExportWithManifest() throws Exception {
    SimpleExportProcessor simpleExportProcessor = new SimpleExportProcessor( path, unifiedRepository );
    simpleExportProcessor.addExportHandler( defaultExportHandler );

    File resultFile = simpleExportProcessor.performExport( repositoryFile );

    assertNotNull( resultFile );
  }

  @Test
  public void testPerformExportWithoutManifest() throws Exception {

  }

  @Test
  public void testExportDirectory() throws Exception {

  }

  @Test
  public void testExportFile() throws Exception {

  }
}
