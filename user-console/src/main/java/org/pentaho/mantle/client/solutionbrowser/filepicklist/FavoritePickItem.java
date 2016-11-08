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

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class FavoritePickItem implements IFilePickItem {
  private Long lastUse = 0L; // Last time file was used
  private String title; // User Friendly title
  private String fullPath; // The full path to the file including file name

  public FavoritePickItem( String fullPath ) {
    super();
    this.fullPath = fullPath;
  }

  FavoritePickItem( JSONObject jsonFilePickItem ) {
    this( jsonFilePickItem.get( "fullPath" ).isString().stringValue() );
    if ( jsonFilePickItem.get( "lastUse" ) != null ) {
      if ( jsonFilePickItem.get( "lastUse" ).isNumber() != null ) {
        this.lastUse = (long) ( jsonFilePickItem.get( "lastUse" ).isNumber().doubleValue() );
      }
    }
    if ( jsonFilePickItem.get( "title" ) == null || jsonFilePickItem.get( "title" ).isString() == null ) {
      this.title = fullPath.substring( fullPath.lastIndexOf( "/" ) + 1 );
    } else {
      this.title = jsonFilePickItem.get( "title" ).isString().stringValue();
    }
  }

  /**
   * @return the fullPath
   */
  public String getFullPath() {
    return fullPath;
  }

  /**
   * @param fullPath
   *          the fullPath to set
   */
  public void setFullPath( String fullPath ) {
    this.fullPath = fullPath;
  }

  public Long getLastUse() {
    return lastUse;
  }

  public void setLastUse( Long lastUse ) {
    this.lastUse = lastUse;
  }

  public JSONObject toJson() {
    JSONObject jso = new JSONObject();
    jso.put( "fullPath", new JSONString( fullPath ) );
    if ( lastUse != null ) {
      jso.put( "title", new JSONString( title ) );
      jso.put( "lastUse", new JSONNumber( lastUse ) );
    }
    return jso;
  }

  public boolean equals( Object o ) {
    if ( o instanceof FavoritePickItem ) {
      FavoritePickItem fpi = (FavoritePickItem) o;
      return getFullPath().equals( fpi.getFullPath() ) ? true : false;
    } else {
      return false;
    }
  }

  public int hashCode() {
    return getFullPath().hashCode();
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle( String title ) {
    this.title = title;
  }
}
