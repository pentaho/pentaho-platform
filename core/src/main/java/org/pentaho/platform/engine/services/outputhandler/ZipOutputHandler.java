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

import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleContentItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ZipOutputHandler extends BaseOutputHandler {

  public IContentItem getFileOutputContentItem() {

    String contentRef = getContentRef();
    File file = new File( contentRef );
    File dir = file.getParentFile();
    if ( !dir.exists() ) {
      dir.mkdirs();
    }
    try {
      FileOutputStream outputStream = new FileOutputStream( file );
      SimpleContentItem content = new SimpleContentItem( outputStream );
      return content;
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    }
    return null;
  }

}
