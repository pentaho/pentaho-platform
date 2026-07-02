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
