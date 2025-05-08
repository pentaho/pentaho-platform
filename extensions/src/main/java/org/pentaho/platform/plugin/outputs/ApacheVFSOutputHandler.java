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
