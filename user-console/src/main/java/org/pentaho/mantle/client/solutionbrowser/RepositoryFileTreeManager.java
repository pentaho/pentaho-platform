/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.mantle.client.solutionbrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.pentaho.gwt.widgets.client.filechooser.JsonToRepositoryFileTreeConverter;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;

import java.util.ArrayList;
import java.util.List;

public class RepositoryFileTreeManager {
  public static final String SEPARATOR = "/"; //$NON-NLS-1$
  public static final String FOLDER_HOME = "home"; //$NON-NLS-1$
  private ArrayList<IRepositoryFileTreeListener> listeners = new ArrayList<IRepositoryFileTreeListener>();

  private RepositoryFileTree fileTree;
  private List<RepositoryFile> trashItems;
  private static RepositoryFileTreeManager instance;

  private static boolean fetching = false;

  private RepositoryFileTreeManager() {
    flagRepositoryFileTreeLoaded( false );
  }

  private native void flagRepositoryFileTreeLoaded( boolean repositoryFileTreeLoaded )
  /*-{
    $wnd.mantle_repository_loaded = repositoryFileTreeLoaded;
  }-*/;

  public static RepositoryFileTreeManager getInstance() {
    if ( instance == null ) {
      instance = new RepositoryFileTreeManager();
    }
    return instance;
  }

  public void addRepositoryFileTreeListener( IRepositoryFileTreeListener listener, Integer depth, String filter,
      Boolean showHidden ) {
    listeners.add( listener );
    synchronized ( RepositoryFileTreeManager.class ) {
      if ( !fetching && fileTree == null ) {
        fetching = true;
        fetchRepositoryFileTree( true, depth, filter, showHidden );
      } else {
        listener.beforeFetchRepositoryFileTree();
        listener.onFetchRepositoryFileTree( fileTree, trashItems );
      }
    }
  }

  public void removeRepositoryFileTreeListener( IRepositoryFileTreeListener listener ) {
    listeners.remove( listener );
  }

  private void fireRepositoryFileTreeFetched() {
    fetching = false;
    for ( IRepositoryFileTreeListener listener : listeners ) {
      listener.onFetchRepositoryFileTree( fileTree, trashItems );
    }
    // flag that we have the document so that other things might start to use it (PDB-500)
    flagRepositoryFileTreeLoaded( true );
  }

  public void beforeFetchRepositoryFileTree() {
    for ( IRepositoryFileTreeListener listener : listeners ) {
      listener.beforeFetchRepositoryFileTree();
    }
  }

  public void fetchRepositoryFileTree( final boolean forceReload, Integer depth, String filter, Boolean showHidden ) {
    if ( forceReload || fileTree == null ) {
      fetchRepositoryFileTree( null, depth, filter, showHidden );
    }
  }

  public void fetchRepositoryFileTree( final AsyncCallback<RepositoryFileTree> callback, final boolean forceReload,
      Integer depth, String filter, Boolean showHidden ) {
    if ( forceReload || fileTree == null ) {
      fetchRepositoryFileTree( callback, depth, filter, showHidden );
    } else {
      callback.onSuccess( fileTree );
    }
  }

  public void fetchRepositoryFileTree( final AsyncCallback<RepositoryFileTree> callback, Integer depth, String filter,
      Boolean showHidden ) {
    // notify listeners that we are about to talk to the server (in case there's anything they want to do
    // such as busy cursor or tree loading indicators)
    beforeFetchRepositoryFileTree();
    RequestBuilder builder = null;
    String url = GWT.getHostPageBaseURL() + "api/repo/files/:/tree?"; //$NON-NLS-1$
    if ( depth == null ) {
      depth = -1;
    }
    if ( filter == null ) {
      filter = "*"; //$NON-NLS-1$
    }
    if ( showHidden == null ) {
      showHidden = Boolean.FALSE;
    }
    url =
        url
            + "depth=" + depth + "&filter=" + filter + "&showHidden=" + showHidden + "&ts=" + System.currentTimeMillis(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    builder = new RequestBuilder( RequestBuilder.GET, url );
    builder.setHeader( "Accept", "application/json" );
    builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );

    RequestCallback innerCallback = new RequestCallback() {

      public void onError( Request request, Throwable exception ) {
        Window.alert( exception.toString() );
      }

      public void onResponseReceived( Request request, Response response ) {
        if ( response.getStatusCode() == Response.SC_OK ) {
          String json = response.getText();
          System.out.println( json );

          final JsonToRepositoryFileTreeConverter converter =
              new JsonToRepositoryFileTreeConverter( response.getText() );
          fileTree = converter.getTree();

          String deletedFilesUrl = GWT.getHostPageBaseURL() + "api/repo/files/deleted?ts=" + System.currentTimeMillis();
          RequestBuilder deletedFilesRequestBuilder = new RequestBuilder( RequestBuilder.GET, deletedFilesUrl );
          deletedFilesRequestBuilder.setHeader( "Accept", "application/json" );
          deletedFilesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
          try {
            deletedFilesRequestBuilder.sendRequest( null, new RequestCallback() {

              public void onError( Request request, Throwable exception ) {
                fireRepositoryFileTreeFetched();
                Window.alert( exception.toString() );
              }

              public void onResponseReceived( Request delRequest, Response delResponse ) {
                if ( delResponse.getStatusCode() == Response.SC_OK ) {
                  try {
                    trashItems = JsonToRepositoryFileTreeConverter.getTrashFiles( delResponse.getText() );
                  } catch ( Throwable t ) {
                    // apparently this happens when you have no trash
                  }
                  fireRepositoryFileTreeFetched();
                } else {
                  fireRepositoryFileTreeFetched();
                }
              }

            } );
          } catch ( Exception e ) {
            fireRepositoryFileTreeFetched();
          }
          if ( callback != null ) {
            callback.onSuccess( fileTree );
          }
        } else {
          fileTree = new RepositoryFileTree();
          RepositoryFile errorFile = new RepositoryFile();
          errorFile.setFolder( true );
          errorFile.setName( "!ERROR!" );
          fileTree.setFile( errorFile );
        }
      }

    };
    try {
      builder.sendRequest( null, innerCallback );
    } catch ( RequestException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
