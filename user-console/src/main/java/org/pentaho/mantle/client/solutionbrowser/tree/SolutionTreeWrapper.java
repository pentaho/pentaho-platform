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


package org.pentaho.mantle.client.solutionbrowser.tree;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.SimplePanel;

public class SolutionTreeWrapper extends SimplePanel {
  SolutionTree tree;

  public SolutionTreeWrapper( SolutionTree tree ) {
    super();
    this.tree = tree;
    add( tree );
    setStyleName( "files-list-panel" ); //$NON-NLS-1$
    sinkEvents( Event.MOUSEEVENTS );
  }

  public void onBrowserEvent( Event event ) {
    return;
  }

}
