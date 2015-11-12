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
import org.pentaho.mantle.client.events.SolutionFileActionEvent;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class ) public class RestoreFileCommandTest {
  RestoreFileCommand command;

  @Before public void setUp() {
    command = spy( new RestoreFileCommand() );
  }

  @Test public void testExecute() throws RequestException {
    SolutionFileActionEvent mockSolutionFileActionEvent = mock( SolutionFileActionEvent.class );
    doReturn( mockSolutionFileActionEvent ).when( command ).getSolutionFileActionEvent();

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( command ).getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    command.execute();

    verify( mockSolutionFileActionEvent ).setAction( anyString() );
    verify( mockRequestBuilder ).setHeader( "Content-Type", "text/plain" );
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );
  }
}
