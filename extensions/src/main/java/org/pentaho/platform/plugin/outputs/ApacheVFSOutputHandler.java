/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.outputs;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.engine.services.outputhandler.BaseOutputHandler;
import org.pentaho.platform.util.logging.Logger;

import java.io.OutputStream;

public class ApacheVFSOutputHandler extends BaseOutputHandler {

  @Override
  public IContentItem getFileOutputContentItem() {

    String contentRef = getContentRef();
    try {
      String contentName = getHandlerId().substring( 4 ) + ":" + contentRef; //$NON-NLS-1$
      FileSystemManager fsManager = getFileSystemManager();
      if ( fsManager == null ) {
        logError( Messages.getInstance().getString(
          "ApacheVFSOutputHandler.ERROR_0001_CANNOT_GET_VFSMGR" ) );
        return null;
      }
      FileObject file = fsManager.resolveFile( contentName );
      if ( file == null ) {
        logError( Messages.getInstance().getString(
          "ApacheVFSOutputHandler.ERROR_0002_CANNOT_GET_VF", contentName ) );
        return null;
      }
      if ( !file.isWriteable() ) {
        logError( Messages.getInstance().getString(
            "ApacheVFSOutputHandler.ERROR_0003_CANNOT_WRITE", contentName ) );
        return null;
      }
      FileContent fileContent = file.getContent();
      if ( fileContent == null ) {
        logError( Messages.getInstance().getString(
            "ApacheVFSOutputHandler.ERROR_0004_CANNOT_GET_CTX", contentName ) );
        return null;
      }
      OutputStream outputStream = fileContent.getOutputStream();

      SimpleContentItem content = new SimpleContentItem( outputStream );
      return content;
    } catch ( Throwable t ) {
      Logger.error( ApacheVFSOutputHandler.class.getName(), Messages.getInstance().getString(
          "ApacheVFSOutputHandler.ERROR_0005_CANNOT_GET_HANDLER", contentRef ), t ); //$NON-NLS-1$
    }

    return null;
  }

  void logError( String string ) {
    Logger.error( ApacheVFSOutputHandler.class.getName(), string ); //$NON-NLS-1$
  }

  FileSystemManager getFileSystemManager() throws FileSystemException {
    return VFS.getManager();
  }

}
