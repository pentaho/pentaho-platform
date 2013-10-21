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
