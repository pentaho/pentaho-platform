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
*/
package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUITemplater;
import org.pentaho.platform.api.repository.SubscriptionAdminException;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.platform.web.jsp.messages.Messages;
import org.pentaho.platform.web.refactor.SubscriptionAdminUIComponent;

public class SubscriptionAdminServlet extends ServletBase {

  private static final Log logger = LogFactory.getLog(SubscriptionAdminServlet.class);

  private static final long serialVersionUID = 420L;

  public SubscriptionAdminServlet() {
    super();
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException {
    PentahoSystem.systemEntryPoint();
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    try {
    response.setCharacterEncoding(LocaleHelper.getSystemEncoding());

    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();

    String mimeType = request.getParameter("requestedMimeType");
    if (StringUtils.isEmpty(mimeType)) {
      mimeType = "text/html";
    }

    IPentahoSession userSession = PentahoSessionHolder.getSession();
    HttpRequestParameterProvider requestParameters = new HttpRequestParameterProvider(request);
    HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider(userSession);
    String thisUrl = requestContext.getContextPath() + "SubscriptionAdmin?"; //$NON-NLS-1$

    SimpleUrlFactory urlFactory = new SimpleUrlFactory(thisUrl);
    ArrayList messages = new ArrayList();

    String content = "";
    SubscriptionAdminUIComponent admin = null;

    String intro = "";
    String footer = "";

    try {
      admin = new SubscriptionAdminUIComponent(urlFactory, messages); //$NON-NLS-1
      admin.validate(userSession, null);
      admin.setParameterProvider(HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters); //$NON-NLS-1$
      admin.setParameterProvider(HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters); //$NON-NLS-1$

      content = admin.getContent(mimeType); //$NON-NLS-1$
      if ("text/html".equals(mimeType)) {
        response.setHeader("Pragma", "no-cache"); // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Cache-Control", "no-store, no-cache, private, must-revalidate, max-stale=0");
        response.setHeader("Expires", "0");
        if (content == null) {
          StringBuffer buffer = new StringBuffer();
          PentahoSystem.get(IMessageFormatter.class, userSession).formatErrorMessage("text/html", "ERROR", messages, buffer); //$NON-NLS-1$ //$NON-NLS-2$
          content = buffer.toString();
        }

        IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession);
        if (templater != null) {
          String sections[] = templater.breakTemplate("template.html", "Subscribe Manager", userSession); //$NON-NLS-1$ //$NON-NLS-2$
          if (sections != null && sections.length > 0) {
            intro = sections[0];
          }
          if (sections != null && sections.length > 1) {
            footer = sections[1];
          }
        } else {
          intro = Messages.getInstance().getString("UI.ERROR_0002_BAD_TEMPLATE_OBJECT");
        }

        content = content.replaceAll("\\\\", "\\\\\\\\");
        content = content.replaceAll("\\$", "\\\\\\$");
        response.getWriter().print(intro);
        response.getWriter().print(content);
        response.getWriter().print(footer);
      } else {
        if (content == null) {
          // TODO
          content = "<error msg='" + "Error" + "'></error>";
        }
        response.getWriter().print(content);
      }
    } catch (SubscriptionAdminException e) {
      // TODO
      content = "<error msg='" + "Error" + "'></error>";
      response.getWriter().print(content);
      }
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException {
    doGet(request, response);
  }

  @Override
  public Log getLogger() {
    return logger;
  }
}
