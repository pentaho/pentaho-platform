package org.pentaho.mantle.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class EmptyRequestCallback implements RequestCallback {

  private static EmptyRequestCallback instance = new EmptyRequestCallback();

  public static EmptyRequestCallback getInstance() {
    return instance;
  }

  public EmptyRequestCallback() {
  }

  public void onError(Request request, Throwable exception) {
  }

  public void onResponseReceived(Request request, Response response) {
  }

}
