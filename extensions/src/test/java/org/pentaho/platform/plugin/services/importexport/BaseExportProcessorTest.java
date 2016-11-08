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
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static org.mockito.Mockito.mock;

/**
 * Just exercise the public methods provided by the abstract class
 */
public class BaseExportProcessorTest {

  static TestExportProcessor testExportProcessor;
  static DefaultExportHandler defaultExportHandler;

  class TestExportProcessor extends BaseExportProcessor {
    @Override
    public File performExport( RepositoryFile exportRepositoryFile ) throws ExportException, IOException {
      return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exportDirectory( RepositoryFile repositoryDir, OutputStream outputStream, String filePath )
      throws ExportException, IOException {
      // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exportFile( RepositoryFile repositoryFile, OutputStream outputStream, String filePath )
      throws ExportException, IOException {
      // To change body of implemented methods use File | Settings | File Templates.
    }
  }

  @Before
  public void setUp() throws Exception {
    testExportProcessor = new TestExportProcessor();
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testAddExportHandler() throws Exception {
    defaultExportHandler = mock( DefaultExportHandler.class );

    // we should be able to add export handlers
    testExportProcessor.addExportHandler( defaultExportHandler );
  }

}
