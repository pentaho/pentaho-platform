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


package org.pentaho.mantle.client.ui.custom;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Class that wraps list box into div necessary for horizontal scroll effect
 */
public class HorizontalScrollWrapper extends Composite {

  private String id;

  public HorizontalScrollWrapper( ListBox listBox ) {
    id = getListBoxWrapperUIId( listBox );

    FlowPanel container = new FlowPanel();
    container.getElement().setId( id );
    container.setStyleName( "scrollable-horizontally" );

    SimplePanel helperDiv = new SimplePanel();
    Label dummyText = new Label( "&nbsp" );
    dummyText.setStyleName( "invisible-font" );
    helperDiv.add( dummyText );

    container.add( listBox );
    container.add( helperDiv );

    initWidget( container );
  }

  @Override
  public void onLoad() {
    super.onLoad();
    prepareHorizontalScroll( id );
  }

  /**
   * Get html div id
   */
  public static String getListBoxWrapperUIId( ListBox listBox ) {
    return "horizontalScroll-" + listBox.hashCode();
  }

  /**
   * Sets width for select and helper div to enable scrolling
   */
  private static native void prepareHorizontalScroll( String id ) /*-{
      $wnd.prepareHorizontalScroll(id);
  }-*/;
}
