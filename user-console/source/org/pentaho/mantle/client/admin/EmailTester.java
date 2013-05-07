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
 * User: RFellows
 * Date: 5/7/13
 */
public class EmailTester {

  private JsEmailConfiguration emailConfig;
  private final String serviceUrl = GWT.getHostPageBaseURL() + "api/emailconfig/sendEmailTest";
  private final RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);

  private AsyncCallback<String> callback = null;

  public EmailTester(AsyncCallback<String> callback) {
    this.callback = callback;
  }

  public void test(JsEmailConfiguration emailConfig) {
    this.emailConfig = emailConfig;
    executableTypesRequestBuilder.setHeader("Content-Type", "application/json");
    try {
      executableTypesRequestBuilder.sendRequest(this.emailConfig.getJSONString(), new RequestCallbackHandler());
    } catch (RequestException e) {
    }

  }

  class RequestCallbackHandler implements RequestCallback {

    private final String EMAIL_TEST_SUCCESS = Messages.getString("connectionTest.sucess", emailConfig.getDefaultFrom());
    private final String EMAIL_TEST_FAIL = Messages.getString("connectionTest.fail");

    @Override
    public void onError(Request arg0, Throwable arg1) {
      callback.onFailure(new Exception(EMAIL_TEST_FAIL, arg1));
    }

    @Override
    public void onResponseReceived(Request request, Response response) {

      if (response.getText().equals("EmailTester.SUCESS")) {
        callback.onSuccess(EMAIL_TEST_SUCCESS);

      } else if (response.getText().equals("EmailTester.FAIL")) {
        callback.onFailure(new Exception(EMAIL_TEST_FAIL));
      }

    }
  }


}
