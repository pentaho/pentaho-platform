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
