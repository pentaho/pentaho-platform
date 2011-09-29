package org.pentaho.mantle.client.usersettings;

import java.util.ArrayList;
import java.util.HashMap;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class MantleSettingsManager {

  private ArrayList<IMantleSettingsListener> listeners = new ArrayList<IMantleSettingsListener>();

  private HashMap<String, String> settings;
  private boolean isAdministrator = false;

  private static MantleSettingsManager instance;

  private MantleSettingsManager() {
  }

  public static MantleSettingsManager getInstance() {
    if (instance == null) {
      instance = new MantleSettingsManager();
    }
    return instance;
  }

  public void addMantleSettingsListener(IMantleSettingsListener listener) {
    listeners.add(listener);
    if (settings == null) {
      fetchMantleSettings(true);
    }
  }

  public void removeMantleSettingsListener(IMantleSettingsListener listener) {
    listeners.remove(listener);
  }

  public void fireMantleSettingsFetched() {
    for (IMantleSettingsListener listener : listeners) {
      listener.onFetchMantleSettings(settings);
    }
  }

  public void fetchMantleSettings(final boolean forceReload) {
    if (forceReload || settings == null) {
      fetchMantleSettings(null);
    }
  }

  public void fetchMantleSettings(final AsyncCallback<HashMap<String, String>> callback, final boolean forceReload) {
    if (forceReload || settings == null) {
      fetchMantleSettings(callback);
    } else {
      callback.onSuccess(settings);
    }
  }

  public void fetchMantleSettings(final AsyncCallback<HashMap<String, String>> callback) {
    final AsyncCallback<HashMap<String, String>> internalCallback = new AsyncCallback<HashMap<String, String>>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialog = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetUserSettings"), true, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.center();
      }

      public void onSuccess(final HashMap<String, String> settings) {
        getInstance().settings = settings;
        settings.put("is-administrator", "" + isAdministrator);
        if (callback != null) {
          callback.onSuccess(settings);
        }
        fireMantleSettingsFetched();
      }
    };

    AsyncCallback<Boolean> isAdministratorCallback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean isAdministrator) {
        MantleServiceCache.getService().getMantleSettings(internalCallback);
        MantleSettingsManager.getInstance().isAdministrator = isAdministrator;
      }

      public void onFailure(Throwable caught) {
        MantleServiceCache.getService().getMantleSettings(internalCallback);
        MantleSettingsManager.getInstance().isAdministrator = false;
      }

    };
    MantleServiceCache.getService().isAdministrator(isAdministratorCallback);
  }

}
