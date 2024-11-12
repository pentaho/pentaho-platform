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

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

public class FavoritePickList extends AbstractFilePickList<FavoritePickItem> {

  private static FavoritePickList favoritePickList;

  private FavoritePickList() {
    super();
  }

  // public FavoritePickList(ArrayList<FavoritePickItem> filePickList) {
  // super(filePickList);
  // }

  private FavoritePickList( JSONArray jsonFilePickList ) {
    super( jsonFilePickList );
  }

  public FavoritePickItem createFilePickItem( JSONObject jsonFilePickItem ) {
    return new FavoritePickItem( jsonFilePickItem );
  }

  public static FavoritePickList getInstance() {
    if ( favoritePickList == null ) {
      favoritePickList = new FavoritePickList();
    }
    return favoritePickList;
  }

  public static FavoritePickList getInstanceFromJSON( JSONArray jsa ) {
    favoritePickList = new FavoritePickList( jsa );
    return favoritePickList;
  }

  public boolean contains( String fileNameWithPath ) {
    return favoritePickList.contains( new FavoritePickItem( fileNameWithPath ) );
  }
}
