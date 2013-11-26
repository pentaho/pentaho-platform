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

package org.pentaho.platform.engine.services.outputhandler;

import org.pentaho.platform.api.engine.IContentListener;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.BufferedContentItem;

/**
 * An output handler that stores the output into memory
 * 
 * @author jamesdixon
 * 
 */
public class MemoryOutputHandler extends BaseOutputHandler implements IContentListener {

  private String path = ""; //$NON-NLS-1$

  public IContentItem getFileOutputContentItem() {
    path = getContentRef();
    IContentItem item = new BufferedContentItem( this );
    item.setName( path );
    item.setMimeType( getMimeType() );
    return item;
  }

  public void close() {
  }

}
