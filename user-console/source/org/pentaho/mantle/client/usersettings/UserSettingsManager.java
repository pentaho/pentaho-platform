package org.pentaho.mantle.client.usersettings;

import java.util.ArrayList;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserSettingsManager {

  private ArrayList<IUserSettingsListener> listeners = new ArrayList<IUserSettingsListener>();

  private JsArray<JsSetting> settings;
  private static UserSettingsManager instance;

  private UserSettingsManager() {
  }

  public static UserSettingsManager getInstance() {
    if (instance == null) {
      instance = new UserSettingsManager();
    }
    return instance;
  }

  public void addUserSettingsListener(IUserSettingsListener listener) {
    listeners.add(listener);
    if (settings == null) {
      fetchUserSettings(true);
    } else {
    	listener.onFetchUserSettings(settings);
    }
  }

  public void removeUserSettingsListener(IUserSettingsListener listener) {
    listeners.remove(listener);
  }

  public void fireUserSettingsFetched() {
    ArrayList<IUserSettingsListener> copy = new ArrayList<IUserSettingsListener>(listeners);
    for (IUserSettingsListener listener : copy) {
      listener.onFetchUserSettings(settings);
    }
  }

  public void fetchUserSettings(final boolean forceReload) {
    if (forceReload || settings == null) {
      fetchUserSettings(null);
    }
  }

  public void fetchUserSettings(final AsyncCallback<JsArray<JsSetting>> callback, final boolean forceReload) {
    if (forceReload || settings == null) {
      fetchUserSettings(callback);
    } else {
      callback.onSuccess(settings);
    }
  }

  public void fetchUserSettings(final AsyncCallback<JsArray<JsSetting>> callback) {
    final String url = GWT.getHostPageBaseURL() + "api/user-settings/list"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    builder.setHeader("accept", "application/json");

    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialog = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetUserSettings"), true, false, true); //$NON-NLS-1$ //$NON-NLS-2$
          dialog.center();
        }

        public void onResponseReceived(Request request, Response response) {
          JsArray<JsSetting> jsSettings = JsSetting.parseSettingsJson(JsonUtils.escapeJsonForEval(response.getText()));

          getInstance().settings = jsSettings;
          if (callback != null) {
            callback.onSuccess(settings);
          }
          fireUserSettingsFetched();
        }

      });
    } catch (RequestException e) {
      // showError(e);
    }

  }

}
