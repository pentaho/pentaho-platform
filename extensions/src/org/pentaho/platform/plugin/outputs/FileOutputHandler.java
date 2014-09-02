/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.outputs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IContentItem;
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
      FileOutputStream outputStream = new FileOutputStream( file );
      SimpleContentItem content = new SimpleContentItem( outputStream );
      return content;
    } catch ( FileNotFoundException e ) {
      logger.error( Messages.getInstance().getErrorString(
        "FileOutputHandler.ERROR_0002_COULD_NOT_CREATE_OUTPUT_FILE", file.getAbsolutePath() ), e ); //$NON-NLS-1$
    }
    return null;
  }

}
