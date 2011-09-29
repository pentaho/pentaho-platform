package org.pentaho.mantle.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EmptyCallback implements AsyncCallback<Void> {

  private static EmptyCallback instance;

  private EmptyCallback() {
    instance = this;
  }

  public static EmptyCallback getInstance() {
    if (instance == null) {
      instance = new EmptyCallback();
    }
    return instance;
  }

  public void onFailure(Throwable caught) {
  }

  public void onSuccess(Void result) {
  }

}
