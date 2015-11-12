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
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserClipboard;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserFile;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class ) public class PasteFilesCommandTest {

  PasteFilesCommand pasteFilesCommand;

  @Before public void setUp() {
    pasteFilesCommand = spy( new PasteFilesCommand() );
  }

  @Test public void testPerformOperation() throws RequestException {
    SolutionBrowserClipboard mockSolutionBrowserClipboard = mock( SolutionBrowserClipboard.class );
    doReturn( mockSolutionBrowserClipboard ).when( pasteFilesCommand ).getSolutionBrowserClipboard();

    List<SolutionBrowserFile> files = new ArrayList<SolutionBrowserFile>();
    SolutionBrowserFile mockSolutionBrowserFile = mock( SolutionBrowserFile.class );
    files.add( mockSolutionBrowserFile );

    doReturn( files ).when( mockSolutionBrowserClipboard ).getClipboardItems();

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( pasteFilesCommand )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    String solutionPath = "solutionPath";
    doReturn( solutionPath ).when( pasteFilesCommand ).getSolutionPath();

    String pathToId = "pathToId";
    doReturn( pathToId ).when( pasteFilesCommand ).pathToId( anyString() );

    // TEST1
    pasteFilesCommand.performOperation( true );
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).sendRequest( isNull( String.class ), any( RequestCallback.class ) );

    // TEST2
    pasteFilesCommand.performOperation( false );
    verify( mockRequestBuilder, times( 2 ) ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder, times( 2 ) ).sendRequest( isNull( String.class ), any( RequestCallback.class ) );
  }

  @Test public void testPerformSave() throws RequestException {
    SolutionBrowserClipboard mockSolutionBrowserClipboard = mock( SolutionBrowserClipboard.class );
    Integer integer = 3;

    String pathToId = "pathToId";
    doReturn( pathToId ).when( pasteFilesCommand ).pathToId( anyString() );

    SolutionBrowserFile mockSolutionBrowserFile = mock( SolutionBrowserFile.class );

    String fileId = "fileId";
    doReturn( fileId ).when( mockSolutionBrowserFile ).getId();

    List<SolutionBrowserFile> mockclipboardFileItems = new ArrayList<>();
    mockclipboardFileItems.add( mockSolutionBrowserFile );

    doReturn( mockclipboardFileItems ).when( mockSolutionBrowserClipboard ).getClipboardItems();

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( pasteFilesCommand )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    // TEST1
    pasteFilesCommand.performSave( mockSolutionBrowserClipboard, integer );

    verify( mockRequestBuilder ).setHeader( "Content-Type", "text/plain" );
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).sendRequest( eq( fileId ), any( RequestCallback.class ) );
    verify( mockSolutionBrowserClipboard ).getClipboardItems();
    verify( mockSolutionBrowserFile ).getId();
    verify( pasteFilesCommand, times( 2 ) ).pathToId( anyString() );
  }
}
