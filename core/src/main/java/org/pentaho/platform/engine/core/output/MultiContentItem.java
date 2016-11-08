/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
