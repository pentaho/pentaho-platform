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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 * 
 * @created March 7, 2005 
 * @author Doug Moran 
 * 
 */

package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.io.IOUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.servlet.messages.Messages;

public class GetContent extends ServletBase {

  /**
   * 
   */
  private static final long serialVersionUID = 9019152264205996418L;

  private static final Log logger = LogFactory.getLog(GetContent.class);

  @Override
  public Log getLogger() {
    return GetContent.logger;
  }

  public GetContent() {
    super();
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

    response.setCharacterEncoding(LocaleHelper.getSystemEncoding());

    PentahoSystem.systemEntryPoint();
    try {
      IPentahoSession userSession = getPentahoSession(request);

      String id = request.getParameter("id"); //$NON-NLS-1$
      if (id == null) {
        returnError(response, Messages.getInstance().getErrorString("GetContent.ERROR_0001_ID_PARAMETER_EMPTY")); //$NON-NLS-1$
        return;
      }

      IContentRepository contentRepos = PentahoSystem.get(IContentRepository.class, userSession);
      if (contentRepos == null) {
        returnError(response, Messages.getInstance().getString("GetContent.ERROR_0002_CONTENT_REPOS_UNAVAILABLE")); //$NON-NLS-1$
        return;
      }

      try {
        IContentItem contentItem = contentRepos.getContentItemById(id);
        if (contentItem == null) {
          returnError(response, Messages.getInstance().getString("GetContent.ERROR_0005_CONTENT_NOT_FOUND", id)); //$NON-NLS-1$
          return;
        }

        String mimetype = contentItem.getMimeType();
        if ((mimetype == null) || (mimetype.length() < 1)) {
          mimetype = request.getParameter("mimetype"); //$NON-NLS-1$
        }

        // Set it if we know what it is
        if ((mimetype != null) && (mimetype.length() > 0)) {
          response.setContentType(mimetype);
        }
        if (! (mimetype.equalsIgnoreCase("text/html") )) { //$NON-NLS-1$
          response.setHeader("Content-Disposition", "inline; filename=\"" + contentItem.getTitle() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        // Send it back
        InputStream inStr = contentItem.getInputStream();
        ServletOutputStream outStr = response.getOutputStream();

        try {
          IOUtils.getInstance().copyStreams(inStr, outStr);
        } finally {
          inStr.close();
          // You are not allowed to close this output stream
          // outStr.close();
        }
      } catch (Exception ex) {
        error(Messages.getInstance().getErrorString("GetContent.ERROR_0003_CONTENT_READ_ERROR"), ex); //$NON-NLS-1$
      }

    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  void returnError(final HttpServletResponse response, final String message) {
    error(message);

    response.setContentType("text/plain"); //$NON-NLS-1$
    try {
      response.getWriter().println(Messages.getInstance().getString("GetContent.ERROR_0004_RETURN_MESSAGE") + message); //$NON-NLS-1$
    } catch (Throwable t) {
    }
  }

}
