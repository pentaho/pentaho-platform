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


package org.pentaho.platform.web.servlet;

import jakarta.servlet.http.HttpServletResponse;

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
