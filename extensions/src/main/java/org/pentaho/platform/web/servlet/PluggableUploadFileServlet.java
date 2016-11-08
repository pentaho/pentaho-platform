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

package org.pentaho.platform.web.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.safehaus.uuid.UUID;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class PluggableUploadFileServlet extends HttpServlet implements Servlet {

  private static final long serialVersionUID = -1575521113175397124L;

  protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    IOException {
    try {

      IUploadFileServletPlugin uploaderPlugin = getUploaderBean( getDispatchKey( request ), response );
      if ( uploaderPlugin == null ) {
        return;
      }

      response.setContentType( "text/plain" ); //$NON-NLS-1$
      FileItem uploadItem = getFileItem( request, uploaderPlugin.getMaxFileSize() );
      if ( uploadItem == null ) {
        response.getWriter().write(
            Messages.getInstance().getErrorString( "PluggableUploadFileServlet.ERROR_0001_NO_FILE_TO_UPLOAD" ) ); //$NON-NLS-1$
        return;
      }

      String path = PentahoSystem.getApplicationContext().getSolutionPath( uploaderPlugin.getTargetFolder() );

      File pathDir = new File( path );
      // create the path if it doesn't exist yet
      if ( !pathDir.exists() ) {
        pathDir.mkdirs();
      }

      if ( uploadItem.getSize() + getFolderSize( pathDir ) > uploaderPlugin.getMaxFolderSize() ) {
        response.getWriter().write(
            Messages.getInstance().getErrorString( "PluggableUploadFileServlet.ERROR_0004_FOLDER_SIZE_LIMIT_REACHED" ) ); //$NON-NLS-1$
        return;
      }

      UUID id = UUIDUtil.getUUID();
      String filename = id.toString() + uploaderPlugin.getFileExtension();

      File outFile = new File( path, filename );

      if ( doesFileExists( outFile ) ) {
        response.getWriter().write(
            Messages.getInstance().getErrorString( "PluggableUploadFileServlet.ERROR_0002_FILE_ALREADY_EXIST" ) ); //$NON-NLS-1$
        return;
      }

      InputStream fileInputStream = uploadItem.getInputStream();
      FileOutputStream outputStream = new FileOutputStream( outFile );
      try {
        IOUtils.copy( fileInputStream, outputStream );
      } finally {
        if ( outputStream != null ) {
          outputStream.close();
        }
        if ( fileInputStream != null ) {
          fileInputStream.close();
        }
      }

      uploaderPlugin.onSuccess( outFile.getAbsolutePath(), response );
    } catch ( Exception e ) {
      response.getWriter().write(
          Messages.getInstance().getErrorString(
              "PluggableUploadFileServlet.ERROR_0005_UNKNOWN_ERROR", e.getLocalizedMessage() ) ); //$NON-NLS-1$
    }
  }

  protected IUploadFileServletPlugin getUploaderBean( String uploaderBeanId, HttpServletResponse response )
    throws PluginBeanException, IOException {
    if ( StringUtils.isEmpty( uploaderBeanId ) ) {
      response.getWriter().write(
          Messages.getInstance().getErrorString( "PluggableUploadFileServlet.ERROR_0006_NO_UPLOADER_FOUND" ) ); //$NON-NLS-1$
      return null;
    }

    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

    if ( !pluginManager.isBeanRegistered( uploaderBeanId ) ) {
      response.getWriter().write(
          Messages.getInstance().getErrorString(
            "PluggableUploadFileServlet.ERROR_0008_NO_UPLOADER_BY_ID", uploaderBeanId ) ); //$NON-NLS-1$
      return null;
    }

    Object uploaderBean = pluginManager.getBean( uploaderBeanId );
    if ( !( uploaderBean instanceof IUploadFileServletPlugin ) ) {
      response.getWriter().write(
          Messages.getInstance().getErrorString(
              "PluggableUploadFileServlet.ERROR_0007_UPLOADER_WRONG_TYPE", IUploadFileServletPlugin.class.getName() ) ); //$NON-NLS-1$
      return null;
    }

    IUploadFileServletPlugin uploaderPlugin = (IUploadFileServletPlugin) uploaderBean;
    return uploaderPlugin;
  }

  @SuppressWarnings( "unchecked" )
  private FileItem getFileItem( HttpServletRequest request, long maxFileSize ) throws FileUploadException {
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload( factory );
    upload.setFileSizeMax( maxFileSize );
    List items = upload.parseRequest( request );
    Iterator it = items.iterator();
    while ( it.hasNext() ) {
      FileItem item = (FileItem) it.next();
      if ( !item.isFormField() ) {
        return item;
      }
    }
    return null;
  }

  private long getFolderSize( File folder ) {
    long foldersize = 0;
    File[] filelist = folder.listFiles();
    for ( int i = 0; i < filelist.length; i++ ) {
      if ( filelist[i].isDirectory() ) {
        foldersize += getFolderSize( filelist[i] );
      } else {
        foldersize += filelist[i].length();
      }
    }
    return foldersize;
  }

  private boolean doesFileExists( File file ) {
    return file.exists();
  }

  /**
   * Returns the dispatch key for this request. This name is the part of the request path beyond the servlet base path.
   * I.e. if the PluggableUploadFileServlet is mapped to the "/upload" context in web.xml, then this method will return
   * "testuploader" given a request to "http://localhost:8080/pentaho/upload/testuploader".
   * 
   * @return the part of the request url used to dispatch the request
   */
  public String getDispatchKey( HttpServletRequest request ) {
    // path info will give us what we want with
    String requestPathInfo = request.getPathInfo();
    if ( requestPathInfo.startsWith( "/" ) ) { //$NON-NLS-1$
      requestPathInfo = requestPathInfo.substring( 1 );
    }
    return requestPathInfo;
  }
}
