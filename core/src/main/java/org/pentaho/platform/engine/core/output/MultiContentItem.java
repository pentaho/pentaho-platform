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


package org.pentaho.platform.engine.core.output;

import org.pentaho.platform.api.repository.IContentItem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MultiContentItem extends SimpleContentItem {

  protected List<IContentItem> contentItems = new ArrayList<IContentItem>();
  protected MultiOutputStream out;

  public void addContentItem( IContentItem contentItem ) {
    contentItems.add( contentItem );
  }

  @Override
  public OutputStream getOutputStream( String actionName ) throws IOException {

    OutputStream[] outs = new OutputStream[contentItems.size()];

    for ( int idx = 0; idx < outs.length; idx++ ) {
      outs[idx] = contentItems.get( idx ).getOutputStream( actionName );
    }
    out = new MultiOutputStream( outs );
    return out;
  }

  @Override
  public void closeOutputStream() {
    try {
      out.close();
    } catch ( Exception e ) {
      // TODO log this
    }
  }

}
