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

public class RecentPickList extends AbstractFilePickList<RecentPickItem> {

  private static RecentPickList recentPickList;

  private RecentPickList() {
    super();
  }

  // public RecentPickList(ArrayList<RecentPickItem> filePickList) {
  // super(filePickList);
  // }

  private RecentPickList( JSONArray jsonFilePickList ) {
    super( jsonFilePickList );
  }

  public RecentPickItem createFilePickItem( JSONObject jsonFilePickItem ) {
    return new RecentPickItem( jsonFilePickItem );
  }

  public static RecentPickList getInstance() {
    if ( recentPickList == null ) {
      recentPickList = new RecentPickList();
    }
    return recentPickList;
  }

  public static RecentPickList getInstanceFromJSON( JSONArray jsa ) {
    recentPickList = new RecentPickList( jsa );
    return recentPickList;
  }

  /**
   * Handle the additional logic associated with adding a new recent including insertion as first item in array and
   * checking list size.
   */
  @Override
  public void add( RecentPickItem pickListItem ) {
    recentPickList.getFilePickList().remove( pickListItem );
    if ( recentPickList.getMaxSize() > 0 && recentPickList.size() >= recentPickList.getMaxSize() ) {
      recentPickList.remove( recentPickList.getMaxSize() - 1 );
    }
    super.add( 0, pickListItem );
  }

  /**
   * Disable ability to add recent anywhere in list
   */
  @Override
  public void add( int index, RecentPickItem pickListItem ) {
    add( pickListItem );
  }

  public boolean contains( String fileNameWithPath ) {
    return recentPickList.contains( new RecentPickItem( fileNameWithPath ) );
  }
}
