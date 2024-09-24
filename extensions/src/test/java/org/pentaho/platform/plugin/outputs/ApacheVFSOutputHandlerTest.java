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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.outputs;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.repository.IContentItem;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ApacheVFSOutputHandlerTest {

  private static final String CONTENT_NAME = "filecontent";
  private ApacheVFSOutputHandler apacheVFSOutputHandler;
  private FileSystemManager mockedFSM;
  private FileObject mockFileObject;
  private FileContent mockFileContent;

  @Before
  public void setup() {

    apacheVFSOutputHandler = spy( new ApacheVFSOutputHandler() );
    doReturn( "test1" ).when( apacheVFSOutputHandler ).getHandlerId();

    mockedFSM = Mockito.mock( FileSystemManager.class );
    mockFileObject = Mockito.mock( FileObject.class );
    mockFileContent = Mockito.mock( FileContent.class );
  }

  @Test
  public void getOutputContentItemTest() {

    setupMockObjectsDefaults( CONTENT_NAME, true, true );

    IContentItem contentItem = apacheVFSOutputHandler.getFileOutputContentItem();
    assertNotNull( contentItem );
  }


  @Test
  public void getOutputContentWrongFileNameTest() {

    setupMockObjectsDefaults( "bogus", true, true );

    IContentItem contentItem = apacheVFSOutputHandler.getFileOutputContentItem();
    assertNull( contentItem );
    verify( apacheVFSOutputHandler ).logError( "Cannot get virtual file: 1:bogus" );
  }

  @Test
  public void getOutputContentFileNotWritableTest() {

    setupMockObjectsDefaults( CONTENT_NAME, false, true );

    IContentItem contentItem = apacheVFSOutputHandler.getFileOutputContentItem();
    assertNull( contentItem );
    verify( apacheVFSOutputHandler ).logError( "Cannot write to virtal file: 1:filecontent" );
  }

  @Test
  public void getOutputContentFileNoContentTest() {

    setupMockObjectsDefaults( CONTENT_NAME, true, false );

    IContentItem contentItem = apacheVFSOutputHandler.getFileOutputContentItem();
    assertNull( contentItem );
    verify( apacheVFSOutputHandler ).logError( "Cannot get virtal file content: 1:filecontent" );
  }

  private void setupMockObjectsDefaults( String contentName, boolean isWritable, boolean hasContent ) {
    try {
      when( mockFileContent.getOutputStream() ).thenReturn( new ByteArrayOutputStream() );
      when( mockFileObject.isWriteable() ).thenReturn( isWritable );
      when( mockFileObject.getContent() ).thenReturn( hasContent ? mockFileContent : null );
      when( mockedFSM.resolveFile( "1:filecontent" ) ).thenReturn( mockFileObject );
      doReturn( mockedFSM ).when( apacheVFSOutputHandler ).getFileSystemManager();
      doReturn( contentName ).when( apacheVFSOutputHandler ).getContentRef();
    } catch ( FileSystemException e ) {
      assertTrue( "Shouldn't have thrown exception here", false );
    }
  }
}
