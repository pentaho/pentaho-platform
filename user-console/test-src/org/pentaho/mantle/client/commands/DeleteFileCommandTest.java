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

package org.pentaho.mantle.client.commands;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class ) public class DeleteFileCommandTest {

  DeleteFileCommand deleteFileCommand;

  @Before public void setUp() {
    deleteFileCommand = spy( new DeleteFileCommand() );
  }

  @Test public void testPerformOperation1() {
    doNothing().when( deleteFileCommand ).performOperation( anyBoolean() );

    // TEST1
    deleteFileCommand.performOperation();

    verify( deleteFileCommand ).performOperation( true );

    String solutionPath = "solutionPath";
    doReturn( solutionPath ).when( deleteFileCommand ).getSolutionPath();

    String fileNames = "fileNames";
    doReturn( fileNames ).when( deleteFileCommand ).getFileNames();

    String fieldIds = "fieldIds";
    doReturn( fieldIds ).when( deleteFileCommand ).getFileIds();

    // TEST2
    deleteFileCommand.performOperation();

    verify( deleteFileCommand, times( 2 ) ).performOperation( true );
  }

  @Test public void testPerformOperation2() {
    SolutionFileActionEvent mockSolutionFileActionEvent = mock( SolutionFileActionEvent.class );
    doReturn( mockSolutionFileActionEvent ).when( deleteFileCommand ).getSolutionFileActionEvent();

    PromptDialogBox mockPromptDialogBox = mock( PromptDialogBox.class );
    doReturn( mockPromptDialogBox ).when( deleteFileCommand )
        .getPromptDialogBox( anyString(), anyString(), anyString(), anyBoolean(), anyBoolean() );

    doNothing().when( deleteFileCommand ).doDelete( anyString(), any( SolutionFileActionEvent.class ) );

    // TEST1
    deleteFileCommand.performOperation( true );

    verify( deleteFileCommand, never() ).doDelete( anyString(), any( SolutionFileActionEvent.class ) );
    verify( mockSolutionFileActionEvent ).setAction( anyString() );
    verify( deleteFileCommand ).getPromptDialogBox( anyString(), anyString(), anyString(), eq( true ), eq( true ) );
    verify( mockPromptDialogBox ).setContent( any( HTML.class ) );
    verify( mockPromptDialogBox ).setCallback( any( IDialogCallback.class ) );
    verify( mockPromptDialogBox ).center();

    // TEST2
    deleteFileCommand.performOperation( false );

    verify( deleteFileCommand ).doDelete( anyString(), any( SolutionFileActionEvent.class ) );
  }

  @Test public void testDoDelete() throws RequestException {
    String filesList = "fileList";
    SolutionFileActionEvent mockSolutionFileActionEvent = mock( SolutionFileActionEvent.class );

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( deleteFileCommand )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    MessageDialogBox mockMessageDialogBox = mock( MessageDialogBox.class );
    doReturn( mockMessageDialogBox ).when( deleteFileCommand )
        .getMessageDialogBox( anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean() );

    // TEST1
    deleteFileCommand.doDelete( filesList, mockSolutionFileActionEvent );

    mockRequestBuilder.setHeader( "Content-Type", "text/plain" );
    mockRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    mockRequestBuilder.sendRequest( eq( filesList ), any( RequestCallback.class ) );

    // TEST2
    RequestException mockRequestException = mock( RequestException.class );
    doThrow( mockRequestException ).when( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    deleteFileCommand.doDelete( filesList, mockSolutionFileActionEvent );

    verify( mockMessageDialogBox ).center();
    verify( mockSolutionFileActionEvent ).setMessage( anyString() );
  }
}
