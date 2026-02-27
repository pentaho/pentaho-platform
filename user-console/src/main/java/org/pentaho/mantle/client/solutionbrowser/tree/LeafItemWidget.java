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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.pentaho.mantle.client.images.ImageUtil;

/**
 * @author wseyler
 * 
 */
public class LeafItemWidget extends Composite {
  Image leafImage;
  Label leafLabel;

  public LeafItemWidget( String title, String... styleName ) {
    HorizontalPanel widget = new HorizontalPanel();
    initWidget( widget );

    leafImage = ImageUtil.getThemeableImage( styleName );
    widget.add( leafImage );

    leafLabel = new Label( title );
    leafLabel.removeStyleName( "gwt-Label" );
    widget.add( leafLabel );
    widget.setCellWidth( leafLabel, "100%" );
  }

  public Image getLeafImage() {
    return leafImage;
  }

  public Label getLeafLabel() {
    return leafLabel;
  }
}
