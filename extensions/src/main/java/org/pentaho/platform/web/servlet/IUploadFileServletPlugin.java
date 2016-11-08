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

import javax.servlet.http.HttpServletResponse;

/**
 * Allows a platform plugin to provide it's own file uploading capability which is not currently supported in
 * {@link IContentGenerator}.
 * 
 * @author aphillips
 */
public interface IUploadFileServletPlugin {
  /**
   * A path where the uploaded files will be saved, relative to the solution path.
   * 
   * @return solution-relative path where files will be saved.
   */
  public String getTargetFolder();

  /**
   * The maximum permitted upload file size in bytes
   * 
   * @return max files size in bytes
   */
  public long getMaxFileSize();

  /**
   * The maximum number of files allowed in the target folder.
   * 
   * @return max number of files in target folder
   */
  public long getMaxFolderSize();

  /**
   * The file extension to append to the randomly generated filename resulting from the file upload.
   * 
   * @return the file extension (must explicitly include the '.' character)
   */
  public String getFileExtension();

  /**
   * A callback invoked upon successful file upload. The {@link UploadFileServlet} can do whatever it needs to with the
   * repsonse which will be the actual servlet response.
   * 
   * @param pathToFile
   *          the absolute path to the file successfully uploaded
   * @param response
   *          the servlet response when the file upload succeedes
   */
  public void onSuccess( String pathToFile, HttpServletResponse response );
}
