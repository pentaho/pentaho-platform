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

package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.pentaho.mantle.client.messages.Messages;

/**
 * User: RFellows Date: 5/7/13
 */
public class EmailTester {

  private JsEmailConfiguration emailConfig;
  private final String serviceUrl = GWT.getHostPageBaseURL() + "api/emailconfig/sendEmailTest";
  private final RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.PUT, serviceUrl );

  private AsyncCallback<String> callback = null;

  public EmailTester( AsyncCallback<String> callback ) {
    this.callback = callback;
  }

  public void test( JsEmailConfiguration emailConfig ) {
    this.emailConfig = emailConfig;
    executableTypesRequestBuilder.setHeader( "Content-Type", "application/json" );
    executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      executableTypesRequestBuilder.sendRequest( this.emailConfig.getJSONString(), new RequestCallbackHandler() );
    } catch ( RequestException e ) {
      //ignored
    }

  }

  class RequestCallbackHandler implements RequestCallback {

    private final String EMAIL_TEST_SUCCESS = Messages
        .getString( "connectionTest.sucess", emailConfig.getDefaultFrom() );
    private final String EMAIL_TEST_FAIL = Messages.getString( "connectionTest.fail" );

    @Override
    public void onError( Request arg0, Throwable arg1 ) {
      callback.onFailure( new Exception( EMAIL_TEST_FAIL, arg1 ) );
    }

    @Override
    public void onResponseReceived( Request request, Response response ) {

      if ( response.getText().equals( "EmailTester.SUCESS" ) ) {
        callback.onSuccess( EMAIL_TEST_SUCCESS );

      } else if ( response.getText().equals( "EmailTester.FAIL" ) ) {
        callback.onFailure( new Exception( EMAIL_TEST_FAIL ) );
      }

    }
  }

}
