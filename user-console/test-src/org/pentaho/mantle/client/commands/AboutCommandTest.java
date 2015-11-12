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
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class ) public class AboutCommandTest {

  AboutCommand aboutCommand;

  @Before public void setUp() {
    aboutCommand = spy( new AboutCommand() );
  }

  @Test public void testPerformOperation() throws RequestException {
    String mantleRevisionOverride = "";
    doReturn( mantleRevisionOverride ).when( aboutCommand ).getMantleRevisionOverride();

    PromptDialogBox mockPromptDialogBox = mock( PromptDialogBox.class );
    doReturn( mockPromptDialogBox ).when( aboutCommand )
        .getPromptDialogBox( anyString(), anyString(), anyString(), anyBoolean(), anyBoolean() );

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( aboutCommand ).getRequestBuilder( eq( RequestBuilder.GET ), anyString() );

    // TEST1
    aboutCommand.performOperation( true );

    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).setHeader( "accept", "text/plain" );
    verify( mockRequestBuilder ).sendRequest( isNull( String.class ), any( RequestCallback.class ) );

    // TEST2
    mantleRevisionOverride = "test";
    doReturn( mantleRevisionOverride ).when( aboutCommand ).getMantleRevisionOverride();

    aboutCommand.performOperation( true );
  }
}
