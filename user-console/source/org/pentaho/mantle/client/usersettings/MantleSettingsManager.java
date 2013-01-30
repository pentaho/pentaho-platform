/*
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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.usersettings;

import java.util.ArrayList;
import java.util.HashMap;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MantleSettingsManager {

  private ArrayList<IMantleSettingsListener> listeners = new ArrayList<IMantleSettingsListener>();

  private HashMap<String, String> settings = new HashMap<String, String>();
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
    if (settings.size() == 0) {
      fetchMantleSettings(true);
    }
  }

  public void removeMantleSettingsListener(IMantleSettingsListener listener) {
    listeners.remove(listener);
  }

  public void fireMantleSettingsFetched() {
    ArrayList<IMantleSettingsListener> copy = new ArrayList<IMantleSettingsListener>(listeners);
    for (IMantleSettingsListener listener : copy) {
      listener.onFetchMantleSettings(settings);
    }
  }

  public void fetchMantleSettings(final boolean forceReload) {
    if (forceReload || settings.size() == 0) {
      fetchMantleSettings(null);
    }
  }

  public void fetchMantleSettings(final AsyncCallback<HashMap<String, String>> callback, final boolean forceReload) {
    if (forceReload || settings.size() == 0) {
      fetchMantleSettings(callback);
    } else {
      callback.onSuccess(settings);
    }
  }

  public void fetchMantleSettings(final AsyncCallback<HashMap<String, String>> callback) {
    final RequestCallback internalCallback = new RequestCallback() {

      public void onError(Request request, Throwable exception) {
        MessageDialogBox dialog = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetUserSettings"), true, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.center();
      }

      public void onResponseReceived(Request request, Response response) {

		JsArray<JsSetting> jsSettings = null;
        try {
          jsSettings = JsSetting.parseSettingsJson(response.getText());
        } catch (Throwable t) {
          // happens when there are no settings
        }

        for (int i = 0; i < jsSettings.length(); i++) {
          settings.put(jsSettings.get(i).getName(), jsSettings.get(i).getValue());
        }

        settings.put("is-administrator", "" + isAdministrator);
        if (callback != null) {
          callback.onSuccess(settings);
        }
        fireMantleSettingsFetched();
      }
    };

    final RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL() + "api/mantle/settings");
    builder.setHeader("accept", "application/json");

    try {
      final String url = GWT.getHostPageBaseURL() + "api/repo/files/canAdminister"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
      requestBuilder.setHeader("accept", "text/plain");
      requestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable caught) {
          try {
            builder.sendRequest(null, internalCallback);
          } catch (RequestException e) {
          }
          MantleSettingsManager.getInstance().isAdministrator = false;
        }

        public void onResponseReceived(Request request, Response response) {
          try {
            builder.sendRequest(null, internalCallback);
          } catch (RequestException e) {
          }
          MantleSettingsManager.getInstance().isAdministrator = isAdministrator;
        }

      });
    } catch (RequestException e) {
      Window.alert(e.getMessage());
    }
  }

}
