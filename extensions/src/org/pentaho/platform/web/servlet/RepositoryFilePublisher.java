/*
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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved.
 *
 * 
 * @created October 24, 2006
 * @author Michael D'Amour
 * 
 */

package org.pentaho.platform.web.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.client.PublisherUtil;
import org.pentaho.platform.util.messages.LocaleHelper;

public class RepositoryFilePublisher extends ServletBase {

  private static final long serialVersionUID = 9019152264205996418L;

  private static final Log logger = LogFactory.getLog(GetContent.class);

  /**
   * Provides utility methods for publishing solution files to the pentaho server.
   * 
   */
  private static final String PublishConfigFile = "publisher_config.xml"; //$NON-NLS-1$

  @Override
  public Log getLogger() {
    return RepositoryFilePublisher.logger;
  }

  public RepositoryFilePublisher() {
    super();
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    PentahoSystem.systemEntryPoint();
    try {
      response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
      IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
      String publishPath = request.getParameter("publishPath"); //$NON-NLS-1$
      String publishKey = request.getParameter("publishKey");//$NON-NLS-1$
      String jndiName = request.getParameter("jndiName");//$NON-NLS-1$
      String jdbcDriver = request.getParameter("jdbcDriver");//$NON-NLS-1$
      String jdbcUrl = request.getParameter("jdbcUrl");//$NON-NLS-1$
      String jdbcUserId = request.getParameter("jdbcUserId");//$NON-NLS-1$
      String jdbcPassword = request.getParameter("jdbcPassword");//$NON-NLS-1$
      boolean overwrite = Boolean.valueOf(request.getParameter("overwrite")).booleanValue(); //$NON-NLS-1$
      boolean mkdirs = Boolean.valueOf(request.getParameter("mkdirs")).booleanValue(); //$NON-NLS-1$

      List<FileItem> fileItems = Collections.emptyList();
      try {
        fileItems = getFileItems(request);
      } catch (FileUploadException e) {
        e.printStackTrace();
        if (RepositoryFilePublisher.logger.isErrorEnabled()) {
          RepositoryFilePublisher.logger.error("an exception occurred", e);
        }
        response.getWriter().println(ISolutionRepository.FILE_ADD_FAILED);
        return;
      }

      int status = doPublish(fileItems, publishPath, publishKey, jndiName, jdbcDriver, jdbcUrl, jdbcUserId, jdbcPassword, overwrite, mkdirs, pentahoSession);
      response.getWriter().println(status);
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  protected List<FileItem> getFileItems(final HttpServletRequest request) throws FileUploadException {
    ServletFileUpload fu = new ServletFileUpload(new DiskFileItemFactory());
    // If file size exceeds, a FileUploadException will be thrown
    fu.setSizeMax(10000000);
    return fu.parseRequest(request);
  }

  protected int doPublish(final List<FileItem> fileItems, final String publishPath, final String publishKey, final String jndiName, final String jdbcDriver,
      final String jdbcUrl, final String jdbcUserId, final String jdbcPassword, final boolean overwrite, final boolean mkdirs,
      final IPentahoSession pentahoSession) {

    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, pentahoSession);
    int status = ISolutionRepository.FILE_ADD_SUCCESSFUL;

    String cleanPublishPath = publishPath;
    // Fix the publish path to prevent problems with the RDBMS repository.
    if ((publishPath != null) && (publishPath.endsWith("/") || publishPath.endsWith("\\"))) { //$NON-NLS-1$ //$NON-NLS-2$
      cleanPublishPath = publishPath.substring(0, publishPath.length() - 1);
    }
    cleanPublishPath = cleanPublishPath.replace('\\', ISolutionRepository.SEPARATOR);

    if (RepositoryFilePublisher.checkPublisherKey(publishKey)) {

      String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath(""); //$NON-NLS-1$

      try {
        PentahoSystem.systemEntryPoint();

        // check to see if the publish path exists
        ISolutionFile folder = repository.getSolutionFile(cleanPublishPath, ISolutionRepository.ACTION_CREATE);
        if (folder == null) {
          if (mkdirs) {
            // we need to create the folder first
            StringTokenizer tokenizer = new StringTokenizer(cleanPublishPath, "" + ISolutionRepository.SEPARATOR); //$NON-NLS-1$
            StringBuilder testPath = new StringBuilder();
            int idx = 1;
            while (tokenizer.hasMoreTokens()) {
              String folderName = tokenizer.nextToken();
              testPath.append(ISolutionRepository.SEPARATOR).append(folderName);
              ISolutionFile testFolder = repository.getSolutionFile(testPath.toString(), ISolutionRepository.ACTION_CREATE);
              if (idx == 1 && testFolder == null) {
                // we do not allow creation of top-level folders
                status = ISolutionRepository.FILE_ADD_FAILED;
                break;
              } else if (testFolder == null) {
                // create this one
                String newFolderPath = PentahoSystem.getApplicationContext().getSolutionPath(testPath.toString());
                File newFolder = new File(newFolderPath);
                repository.createFolder(newFolder);
              }
              idx++;
            }
          } else {
            // the folder does not exist
            status = ISolutionRepository.FILE_ADD_FAILED;
          }
        }
        if (status == ISolutionRepository.FILE_ADD_SUCCESSFUL) {
          for (int i = 0; i < fileItems.size() && status == ISolutionRepository.FILE_ADD_SUCCESSFUL; i++) {
            FileItem fi = fileItems.get(i);
            String name = URLDecoder.decode(fi.getName(), "UTF-8");
            status = repository.publish(solutionPath, cleanPublishPath, name, fi.get(), overwrite);
          }
        }

      } catch (PentahoAccessControlException e) {
        status = ISolutionRepository.FILE_ADD_FAILED;
        if (RepositoryFilePublisher.logger.isErrorEnabled()) {
          RepositoryFilePublisher.logger.error("an error occurred", e);
        }
      } catch (IOException e) {
        status = ISolutionRepository.FILE_ADD_FAILED;
        if (RepositoryFilePublisher.logger.isErrorEnabled()) {
          RepositoryFilePublisher.logger.error("an error occurred", e);
        }
      } finally {
        PentahoSystem.systemExitPoint();
      }
    } else {
      status = ISolutionRepository.FILE_ADD_INVALID_PUBLISH_PASSWORD;
    }
    return status;
  }

  /**
   * Checks the publisher key in the publish_config.xml against the presented key.
   * 
   * @param key
   *          The key to verify
   * @return true if the presented key is the same as the one in publish_config.xml
   */
  public static final boolean checkPublisherKey(final String key) {
    if (key != null) {
      Document doc = PentahoSystem.getSystemSettings().getSystemSettingsDocument(RepositoryFilePublisher.PublishConfigFile);
      if (doc != null) {
        Node node = doc.selectSingleNode("//publisher-config/publisher-password"); //$NON-NLS-1$
        if (node != null) {
          String setting = node.getText();
          if ((setting != null) && (setting.length() > 0)) {
            if (setting.startsWith("MD5:")) {
              String pubKey = setting.substring(4);
              return pubKey.equals(key);
            } else {
              String pubKey = PublisherUtil.getPasswordKey(setting);
              return pubKey.equals(key);
            }
          }
        }
      }
    }
    return false;
  }
}
