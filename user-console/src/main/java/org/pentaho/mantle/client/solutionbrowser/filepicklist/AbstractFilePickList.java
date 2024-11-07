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

package org.pentaho.mantle.client.solutionbrowser.filepicklist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.csrf.CsrfRequestBuilder;
import org.pentaho.mantle.client.messages.Messages;

import java.util.ArrayList;

public abstract class AbstractFilePickList<T extends IFilePickItem> {

  private static final String FILE_PICK_ADD = "add";
  private static final String FILE_PICK_REMOVE = "remove";

  private ArrayList<T> filePickList;
  private int maxSize = 0; // 0 size equals no limit
  private ArrayList<IFilePickListListener<T>> listeners = new ArrayList<IFilePickListListener<T>>();

  public AbstractFilePickList() {
    this.filePickList = new ArrayList<T>( 10 );
  }

  public AbstractFilePickList( ArrayList<T> filePickList ) {
    this.filePickList = filePickList;
    fireItemsChangedEvent();
  }

  public AbstractFilePickList( JSONArray jsonFilePickList ) {
    filePickList = new ArrayList<T>();
    T filePickItem;
    for ( int i = 0; i < jsonFilePickList.size(); i++ ) {
      filePickItem = createFilePickItem( jsonFilePickList.get( i ).isObject() );
      filePickList.add( filePickItem );
    }
    fireItemsChangedEvent();
  }

  abstract T createFilePickItem( JSONObject jsonFilePickItem );

  public abstract boolean contains( String fileNameWithPath );

  /**
   * @return JSONArray representation of list suitable for storage
   */
  public JSONArray toJson() {
    JSONArray jsa = new JSONArray();
    T filePickItem;
    for ( int i = 0; i < filePickList.size(); i++ ) {
      filePickItem = filePickList.get( i );
      jsa.set( i, filePickItem.toJson() );
    }
    return jsa;
  }

  public int size() {
    return filePickList.size();
  }

  public void add( T pickListItem ) {
    reloadFavorites( pickListItem, FILE_PICK_ADD );
  }

  /**
   * If the object is already in the list it will be removed first and the index adjusted accordingly. If maxSize is
   * positive and adding the item would exceed maxSize, then the item will not be added.
   *
   * @param index
   * @param pickListItem
   */
  public void add( int index, T pickListItem ) {
    int i = filePickList.indexOf( pickListItem );
    if ( i != -1 ) {
      if ( i < index ) {
        index--;
      }
      filePickList.remove( i );
    }
    if ( maxSize <= 0 || filePickList.size() < maxSize ) {
      filePickList.add( index, pickListItem );
    }
    fireItemsChangedEvent();
  }

  public void remove( T pickListItem ) {
    if ( pickListItem instanceof FavoritePickItem ) {
      reloadFavorites( pickListItem, FILE_PICK_REMOVE );
    } else if ( pickListItem instanceof RecentPickItem ) {
      reloadRecents( pickListItem, FILE_PICK_REMOVE );
    }
  }

  public T remove( int index ) {
    T removed = filePickList.remove( index );
    fireItemsChangedEvent();
    return removed;
  }

  public void clear() {
    filePickList.clear();
    fireItemsChangedEvent();
  }

  public boolean contains( T pickListItem ) {
    return filePickList.contains( pickListItem );
  }

  public void fireItemsChangedEvent() {
    for ( IFilePickListListener<T> listener : listeners ) {
      listener.itemsChanged( this );
    }
  }

  public void addPickListListener( IFilePickListListener<T> listener ) {
    listeners.add( listener );
  }

  public void removePickListListener( IFilePickListListener<T> listener ) {
    listeners.remove( listener );
  }

  /**
   * @return serialize to JSON String suitable for storage
   */
  public String stringify() {
    return toJson().toString();
  }

  /**
   * helper method for debugging. Use <code>stringify()</code> for formal JSON conversion.
   */
  public String toString() {
    return toJson().toString();
  }

  /**
   * @return true if the list was truncated
   */
  private boolean truncateToMaxSize() {
    boolean result = false;
    if ( maxSize > 0 ) {
      if ( filePickList.size() > maxSize ) {
        while ( filePickList.size() > maxSize ) {
          filePickList.remove( filePickList.size() - 1 );
        }
        result = true;
      }
    }
    return result;
  }

  public ArrayList<T> getFilePickList() {
    return filePickList;
  }

  public void setFilePickList( ArrayList<T> filePickList ) {
    this.filePickList = filePickList;
  }

  /**
   * @return the maxSize
   */
  public int getMaxSize() {
    return maxSize;
  }

  /**
   * @param maxSize Set Maximum number of entries in list, 0 = unlimited
   */
  public void setMaxSize( int maxSize ) {
    this.maxSize = maxSize;
    if ( truncateToMaxSize() ) {
      fireItemsChangedEvent();
    }
  }

  /**
   * Convert the FilePickList to JSON and save it to a user setting
   *
   * @param settingName
   */
  public void save( String settingName ) {

    String url = GWT.getHostPageBaseURL() + "api/user-settings/" + settingName;
    RequestBuilder builder = new CsrfRequestBuilder( RequestBuilder.POST, url );
    try {
      builder.setHeader( "accept", "application/json" );
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      builder.sendRequest( toJson().toString(), new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          MessageDialogBox dialog =
            new MessageDialogBox(
              Messages.getString( "error" ),
              Messages.getString( "couldNotSetUserSettings" ),
              true,
              false,
              true );
          dialog.center();
        }

        public void onResponseReceived( Request request, Response response ) {
          fireOnSavedEvent();
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  public void reloadFavorites( final T pickListItem, final String command ) {
    final String url = GWT.getHostPageBaseURL() + "api/user-settings/favorites";

    RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
    try {
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      builder.setHeader( "accept", "application/json" );
      builder.sendRequest( null, new RequestCallback() {
        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == Response.SC_NO_CONTENT && FILE_PICK_ADD.equals( command ) ) {
            filePickList = new ArrayList<T>();
            add( filePickList.size(), pickListItem );
          } else if ( response.getStatusCode() == Response.SC_OK ) {
            try {
              JSONArray jsonArr = (JSONArray) JSONParser.parse( response.getText() );
              filePickList = new ArrayList<T>();
              T filePickItem;
              for ( int i = 0; i < jsonArr.size(); i++ ) {
                filePickItem = createFilePickItem( jsonArr.get( i ).isObject() );
                filePickList.add( filePickItem );
              }
              if ( FILE_PICK_ADD.equals( command ) ) {
                add( filePickList.size(), pickListItem );
              } else if ( FILE_PICK_REMOVE.equals( command ) ) {
                filePickList.remove( pickListItem );
                fireItemsChangedEvent();
              }
            } catch ( Exception e ) {
              if ( FILE_PICK_ADD.equals( command ) ) {
                add( filePickList.size(), pickListItem );
              } else if ( FILE_PICK_REMOVE.equals( command ) ) {
                filePickList.remove( pickListItem );
                fireItemsChangedEvent();
              }
            }
          }
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  public void reloadRecents( final T pickListItem, final String command ) {
    final String url = GWT.getHostPageBaseURL() + "api/user-settings/recent";

    RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
    try {
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      builder.setHeader( "accept", "application/json" );
      builder.sendRequest( null, new RequestCallback() {
        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == Response.SC_OK ) {
            try {
              JSONArray jsonArr = (JSONArray) JSONParser.parse( response.getText() );
              filePickList = new ArrayList<T>();
              T filePickItem;
              for ( int i = 0; i < jsonArr.size(); i++ ) {
                filePickItem = createFilePickItem( jsonArr.get( i ).isObject() );
                filePickList.add( filePickItem );
              }
              if ( FILE_PICK_REMOVE.equals( command ) ) {
                filePickList.remove( pickListItem );
                fireItemsChangedEvent();
              }
            } catch ( Exception e ) {
              if ( FILE_PICK_REMOVE.equals( command ) ) {
                filePickList.remove( pickListItem );
                fireItemsChangedEvent();
              }
            }
          }
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  private void fireOnSavedEvent() {
    for ( IFilePickListListener<T> listener : listeners ) {
      listener.onSaveComplete( this );
    }
  }
}
