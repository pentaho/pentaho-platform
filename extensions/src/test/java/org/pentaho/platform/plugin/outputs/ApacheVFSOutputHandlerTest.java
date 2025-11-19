/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
