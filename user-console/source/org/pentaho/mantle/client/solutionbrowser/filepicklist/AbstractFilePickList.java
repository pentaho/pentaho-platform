/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.solutionbrowser.filepicklist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import java.util.ArrayList;

public abstract class AbstractFilePickList<T extends IFilePickItem> {

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
    add( filePickList.size(), pickListItem );
  }

  /**
   * If the object is already in the list it will be removed first and the index adjusted accordingly. If maxSize
   * is positive and adding the item would exceed maxSize, then the item will not be added.
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

  public boolean remove( T pickListItem ) {
    boolean removed = filePickList.remove( pickListItem );
    if ( removed ) {
      fireItemsChangedEvent();
    }
    return removed;
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
   * 
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
   * @param maxSize
   *          Set Maximum number of entries in list, 0 = unlimited
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
    final String url = GWT.getHostPageBaseURL() + "api/user-settings/" + settingName; //$NON-NLS-1$

    RequestBuilder builder = new RequestBuilder( RequestBuilder.POST, url );
    try {
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      builder.sendRequest( toJson().toString(), new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          MessageDialogBox dialog =
              new MessageDialogBox(
                  Messages.getString( "error" ), Messages.getString( "couldNotSetUserSettings" ), true, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
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

  private void fireOnSavedEvent() {
    for ( IFilePickListListener<T> listener : listeners ) {
      listener.onSaveComplete( this );
    }
  }

}
