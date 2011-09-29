package org.pentaho.mantle.client.usersettings;

import java.util.ArrayList;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserSettingsManager {

  private ArrayList<IUserSettingsListener> listeners = new ArrayList<IUserSettingsListener>();

  private ArrayList<IUserSetting> settings;
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
    }
  }

  public void removeUserSettingsListener(IUserSettingsListener listener) {
    listeners.remove(listener);
  }

  public void fireUserSettingsFetched() {
    for (IUserSettingsListener listener : listeners) {
      listener.onFetchUserSettings(settings);
    }
  }

  public void fetchUserSettings(final boolean forceReload) {
    if (forceReload || settings == null) {
      fetchUserSettings(null);
    }
  }

  public void fetchUserSettings(final AsyncCallback<ArrayList<IUserSetting>> callback, final boolean forceReload) {
    if (forceReload || settings == null) {
      fetchUserSettings(callback);
    } else {
      callback.onSuccess(settings);
    }
  }

  public void fetchUserSettings(final AsyncCallback<ArrayList<IUserSetting>> callback) {
    AsyncCallback<ArrayList<IUserSetting>> internalCallback = new AsyncCallback<ArrayList<IUserSetting>>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialog = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetUserSettings"), true, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.center();
      }

      public void onSuccess(final ArrayList<IUserSetting> settings) {
        getInstance().settings = settings;
        if (callback != null) {
          callback.onSuccess(settings);
        }
        fireUserSettingsFetched();
      }
    };
    MantleServiceCache.getService().getUserSettings(internalCallback);
  }

}
