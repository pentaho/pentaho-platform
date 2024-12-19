/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.mantle.client.solutionbrowser.filepicklist;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class RecentPickItem implements IFilePickItem {
  private Long lastUse; // Last time file was used
  private String title; // User Friendly Display Title
  private String fullPath; // The full path to the file including file name

  public RecentPickItem( String fullPath ) {
    super();
    this.fullPath = fullPath;
  }

  RecentPickItem( JSONObject jsonFilePickItem ) {
    this( jsonFilePickItem.get( "fullPath" ).isString().stringValue() );
    this.lastUse = (long) ( jsonFilePickItem.get( "lastUse" ).isNumber().doubleValue() );
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
    jso.put( "title", new JSONString( title ) );
    jso.put( "lastUse", new JSONNumber( lastUse ) );
    return jso;
  }

  public boolean equals( Object o ) {
    if ( o instanceof RecentPickItem ) {
      RecentPickItem rpi = (RecentPickItem) o;
      return getFullPath().equals( rpi.getFullPath() ) ? true : false;
    } else {
      return false;
    }
  }

  public int hashCode() {
    return getFullPath().hashCode();
  }

  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle( String title ) {
    this.title = title;
  }
}
