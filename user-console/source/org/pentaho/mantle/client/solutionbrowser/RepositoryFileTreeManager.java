package org.pentaho.mantle.client.solutionbrowser;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.gwt.widgets.client.filechooser.XMLToRepositoryFileTreeConverter;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RepositoryFileTreeManager {
  public static final String SEPARATOR = "/"; //$NON-NLS-1$
  public static final String FOLDER_HOME = "home"; //$NON-NLS-1$
  private ArrayList<IRepositoryFileTreeListener> listeners = new ArrayList<IRepositoryFileTreeListener>();

  private RepositoryFileTree fileTree;
  private List<RepositoryFile> trashItems;
  private static RepositoryFileTreeManager instance;

  private static boolean fetching = false;

  private RepositoryFileTreeManager() {
    flagRepositoryFileTreeLoaded(false);
  }

  private native void flagRepositoryFileTreeLoaded(boolean repositoryFileTreeLoaded)
  /*-{
    $wnd.mantle_repository_loaded = repositoryFileTreeLoaded;
  }-*/;

  public static RepositoryFileTreeManager getInstance() {
    if (instance == null) {
      instance = new RepositoryFileTreeManager();
    }
    return instance;
  }

  public void addRepositoryFileTreeListener(IRepositoryFileTreeListener listener, Integer depth, String filter, Boolean showHidden) {
    listeners.add(listener);
    synchronized (RepositoryFileTreeManager.class) {
      if (!fetching && fileTree == null) {
        fetching = true;
        fetchRepositoryFileTree(true, depth, filter, showHidden);
      }
    }
  }

  public void removeRepositoryFileTreeListener(IRepositoryFileTreeListener listener) {
    listeners.remove(listener);
  }

  private void fireRepositoryFileTreeFetched() {
    fetching = false;
    for (IRepositoryFileTreeListener listener : listeners) {
      listener.onFetchRepositoryFileTree(fileTree, trashItems);
    }
    // flag that we have the document so that other things might start to use it (PDB-500)
    flagRepositoryFileTreeLoaded(true);
  }

  public void beforeFetchRepositoryFileTree() {
    for (IRepositoryFileTreeListener listener : listeners) {
      listener.beforeFetchRepositoryFileTree();
    }
  }

  public void fetchRepositoryFileTree(final boolean forceReload, Integer depth, String filter, Boolean showHidden) {
    if (forceReload || fileTree == null) {
      fetchRepositoryFileTree(null, depth, filter, showHidden);
    }
  }

  public void fetchRepositoryFileTree(final AsyncCallback<RepositoryFileTree> callback, final boolean forceReload, Integer depth, String filter,
      Boolean showHidden) {
    if (forceReload || fileTree == null) {
      fetchRepositoryFileTree(callback, depth, filter, showHidden);
    } else {
      callback.onSuccess(fileTree);
    }
  }

  private native String getFullyQualifiedURL()/*-{
                                              return $wnd.FULL_QUALIFIED_URL;
                                              }-*/;

  public void fetchRepositoryFileTree(final AsyncCallback<RepositoryFileTree> callback, Integer depth, String filter, Boolean showHidden) {
    // notify listeners that we are about to talk to the server (in case there's anything they want to do
    // such as busy cursor or tree loading indicators)
    beforeFetchRepositoryFileTree();
    RequestBuilder builder = null;
    String url = getFullyQualifiedURL() + "api/repo/files/:/children?"; //$NON-NLS-1$
    if (depth == null) {
      depth = -1;
    }
    if (filter == null) {
      filter = "*"; //$NON-NLS-1$
    }
    if (showHidden == null) {
      showHidden = Boolean.FALSE;
    }
    url = url + "depth=" + depth + "&filter=" + filter + "&showHidden=" + showHidden; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    builder = new RequestBuilder(RequestBuilder.GET, url);

    RequestCallback innerCallback = new RequestCallback() {

      public void onError(Request request, Throwable exception) {
        Window.alert(exception.toString());
      }

      public void onResponseReceived(Request request, Response response) {
        if (response.getStatusCode() == Response.SC_OK) {
          final XMLToRepositoryFileTreeConverter converter = new XMLToRepositoryFileTreeConverter(response.getText());
          fileTree = converter.getTree();
          String deletedFilesUrl = getFullyQualifiedURL() + "api/repo/files/deleted";
          RequestBuilder deletedFilesRequestBuilder = new RequestBuilder(RequestBuilder.GET, deletedFilesUrl);
          try {
            deletedFilesRequestBuilder.sendRequest(null, new RequestCallback() {

              public void onError(Request request, Throwable exception) {
                fireRepositoryFileTreeFetched();
                Window.alert(exception.toString());
              }

              public void onResponseReceived(Request delRequest, Response delResponse) {
                if (delResponse.getStatusCode() == Response.SC_OK) {
                  trashItems = XMLToRepositoryFileTreeConverter.getTrashFiles(delResponse.getText());
                  fireRepositoryFileTreeFetched();
                } else {
                  fireRepositoryFileTreeFetched();
                }
              }

            });
          } catch (Exception e) {
            fireRepositoryFileTreeFetched();
          }
          if (callback != null) {
            callback.onSuccess(fileTree);
          }
        } else {
          fileTree = new RepositoryFileTree();
          RepositoryFile errorFile = new RepositoryFile();
          errorFile.setFolder(true);
          errorFile.setName("!ERROR!");
          fileTree.setFile(errorFile);
        }
      }

    };
    try {
      builder.sendRequest(null, innerCallback);
    } catch (RequestException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
