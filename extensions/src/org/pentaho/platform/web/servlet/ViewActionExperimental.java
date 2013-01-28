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
 * @created Jul 12, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUITemplater;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.scheduler.BackgroundExecutionException;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.ActionSequenceContentGenerator;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.platform.web.servlet.messages.Messages;

/**
 * Servlet Class
 * 
 * web.servlet name="ViewAction" display-name="Name for ViewAction" description="Description for ViewAction" web.servlet-mapping url-pattern="/ViewAction"
 * web.servlet-init-param name="A parameter" value="A value"
 */
public class ViewActionExperimental extends ServletBase {

  private static final long serialVersionUID = 4680027723733552639L;

  private static final Log logger = LogFactory.getLog(ViewAction.class);

  @Override
  public Log getLogger() {
    return ViewActionExperimental.logger;
  }

  /**
   * 
   */
  public ViewActionExperimental() {
    super();
  }

  protected boolean doBackgroundExecution(final HttpServletRequest request, final HttpServletResponse response,
      final IPentahoSession userSession) throws ServletException, IOException {
    if ("true".equals(request.getParameter("background"))) { //$NON-NLS-1$ //$NON-NLS-2$
      IBackgroundExecution backgroundExecutionHandler = PentahoSystem.get(IBackgroundExecution.class, userSession);
      if (backgroundExecutionHandler != null) {
        HttpRequestParameterProvider parameterProvider = new HttpRequestParameterProvider(request);
        String intro = ""; //$NON-NLS-1$
        String footer = ""; //$NON-NLS-1$
        IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession);
        if (templater != null) {
          String sections[] = templater.breakTemplate("template-dialog.html", "", userSession); //$NON-NLS-1$ //$NON-NLS-2$ 
          if ((sections != null) && (sections.length > 0)) {
            intro = sections[0];
          }
          if ((sections != null) && (sections.length > 1)) {
            footer = sections[1];
          }
        } else {
          intro = Messages.getInstance().getString("ViewAction.ERROR_0002_BAD_TEMPLATE_OBJECT"); //$NON-NLS-1$
        }

        response.getWriter().print(intro);
        String backgroundResponse = null;
        try  {
          backgroundResponse = backgroundExecutionHandler.backgroundExecuteAction(userSession, parameterProvider);  
        } catch(BackgroundExecutionException bex) {
          backgroundResponse = bex.getLocalizedMessage();
          response.getWriter().print(backgroundResponse);
          response.getWriter().print(footer);
          error(Messages.getInstance().getErrorString("ViewAction.ERROR_0004_UNABLE_TO_PERFORM_BACKGROUND_EXECUTION")); //$NON-NLS-1$
          return false;          
        }
        response.getWriter().print(backgroundResponse);
        response.getWriter().print(footer);
        return true;
      } else {
        error(Messages.getInstance().getErrorString("ViewAction.ERROR_0001_BACKGROUND_EXECUTE_NOT_SUPPORTED")); //$NON-NLS-1$
      }
    }
    return false;
  }

  protected OutputStream getOutputStream(final HttpServletResponse response, final boolean doMessages)
      throws ServletException, IOException {
    OutputStream outputStream = null;
    if (doMessages) {
      outputStream = new ByteArrayOutputStream();
    } else {
      outputStream = response.getOutputStream();
    }

    return outputStream;
  }

  protected boolean doMessages(final HttpServletRequest request) {
    return "true".equalsIgnoreCase(request.getParameter("debug")); //$NON-NLS-1$ //$NON-NLS-2$
  }

  protected void handleActionRequest(final HttpServletRequest request, final HttpServletResponse response,
      final HttpOutputHandler outputHandler, OutputStream outputStream,
      final IContentItem contentItem, final IPentahoSession userSession) throws ServletException, IOException {
      IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    try {

    	// create the parameter providers
        HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider(userSession);
        HttpRequestParameterProvider requestParameters = new HttpRequestParameterProvider(request);
        Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
        parameterProviders.put( IParameterProvider.SCOPE_REQUEST , requestParameters);
        parameterProviders.put( IParameterProvider.SCOPE_SESSION , sessionParameters);
        
        // create the URL factory
        SimpleUrlFactory urlFactory = new SimpleUrlFactory(requestContext.getContextPath() + "ViewAction?"); //$NON-NLS-1$

        // create the message list
    	List<String> messages = new ArrayList<String>();
    	
    	// create the content generator and give it the things it needs
    	IContentGenerator contentGenerator = new ActionSequenceContentGenerator();
    	contentGenerator.setOutputHandler(outputHandler);
    	contentGenerator.setMessagesList(messages);
    	contentGenerator.setParameterProviders(parameterProviders);
    	contentGenerator.setSession(userSession);
    	contentGenerator.setUrlFactory(urlFactory);
    	contentGenerator.createContent();
    	
    } catch (Exception e) {
    }
    if (contentItem != null) {
      contentItem.closeOutputStream();
    }
  }

  protected HttpOutputHandler createOutputHandler(final HttpServletResponse response, final OutputStream outputStream) {
    return new HttpOutputHandler(response, outputStream, true);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException {
    PentahoSystem.systemEntryPoint();
    IContentItem contentItem = null;
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    try {
      IPentahoSession userSession = getPentahoSession(request);
      if (!doBackgroundExecution(request, response, userSession)) {
        OutputStream outputStream = getOutputStream(response, doMessages(request));
//        ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
//        String solutionName = request.getParameter("solution"); //$NON-NLS-1$
 //       String actionPath = request.getParameter("path"); //$NON-NLS-1$
//        String actionName = request.getParameter("action"); //$NON-NLS-1$
 //       IActionSequence actionSequence = repository.getActionSequence(solutionName, actionPath, actionName,
 //           PentahoSystem.loggingLevel, ISolutionRepository.ACTION_EXECUTE);

        HttpOutputHandler outputHandler = createOutputHandler(response, outputStream);
        outputHandler.setSession(userSession);
//        if ((contentItem != null) && (fileName != null)) {
//          outputHandler.setOutputContent(contentItem);
//        }

        IMimeTypeListener listener = new HttpMimeTypeListener(request, response);
        outputHandler.setMimeTypeListener(listener);
        SimpleUrlFactory urlFactory = new SimpleUrlFactory(requestContext.getContextPath() + "ViewAction?"); //$NON-NLS-1$
        IParameterProvider requestParameters = new HttpRequestParameterProvider(request);
        handleActionRequest(request, response, outputHandler, outputStream, contentItem, userSession );
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

  
  /*
  // TODO: Test Code Only!!! DM - returns an actionsequence as a string
  private String getActionSequence(IPentahoSession session) {
    try {
      return (PentahoSystem.get(ISolutionRepository.class, session).getResourceAsString("test/metadata/EmbeddedReport.xaction")); //$NON-NLS-1$
    } catch (Throwable t) {
    }
    return (""); //$NON-NLS-1$
    // File f = new File( "test/metadata/EmbeddedReport.xaction" );
    // return( FileHelper.getStringFromFile(f) );
  }
  */

}