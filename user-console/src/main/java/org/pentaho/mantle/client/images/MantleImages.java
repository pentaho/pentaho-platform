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


package org.pentaho.mantle.client.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Tree.Resources;
import com.google.gwt.user.client.ui.VerticalSplitPanel;

@SuppressWarnings( "deprecation" )
public interface MantleImages extends ClientBundle, Resources, HorizontalSplitPanel.Resources,
    VerticalSplitPanel.Resources {

  public static final MantleImages images = (MantleImages) GWT.create( MantleImages.class );

  ImageResource treeOpen();

  ImageResource treeClosed();

  ImageResource treeLeaf();

}
