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


package org.pentaho.platform.plugin.outputs;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.FileContentItem;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.engine.services.outputhandler.BaseOutputHandler;
import org.pentaho.platform.plugin.services.messages.Messages;

public class FileOutputHandler extends BaseOutputHandler {
  protected static final Log logger = LogFactory.getLog( FileOutputHandler.class );

  @Override
  public IContentItem getFileOutputContentItem() {
    String contentRef = getContentRef();
    File file = new File( contentRef );
    File dir = file.getParentFile();
    if ( ( dir != null ) && !dir.exists() ) {
      boolean result = dir.mkdirs();
      if ( !result ) {
        try {
          URI uri = new URI( contentRef );
          file = new File( uri );
          dir = file.getParentFile();
        } catch ( URISyntaxException e ) {
          logger.error( Messages.getInstance().getErrorString(
              "FileOutputHandler.ERROR_0001_COULD_NOT_CREATE_DIRECTORY", dir.getAbsolutePath() ) ); //$NON-NLS-1$
          return null;
        }
      }
    }
    try {
      SimpleContentItem content = new FileContentItem( file );
      return content;
    } catch ( FileNotFoundException e ) {
      logger.error( Messages.getInstance().getErrorString(
        "FileOutputHandler.ERROR_0002_COULD_NOT_CREATE_OUTPUT_FILE", file.getAbsolutePath() ), e ); //$NON-NLS-1$
    }
    return null;
  }

}
