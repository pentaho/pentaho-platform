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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created 3/1/2011
 * @author Ramaiz Mansoor
 *
 */
package org.pentaho.platform.web.http.api.resources;

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
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.ActionSequenceJCRHelper;
import org.pentaho.platform.engine.services.SoapHelper;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.messages.Messages;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;

public class XactionUtil
{
  private static final Log logger = LogFactory.getLog(XactionUtil.class);

  @SuppressWarnings("unchecked")
  public static void createOutputFileName(RepositoryFile file, IOutputHandler outputHandler)
  {
    IPentahoSession userSession = PentahoSessionHolder.getSession();
    ActionSequenceJCRHelper actionHelper = new ActionSequenceJCRHelper(userSession);
    IActionSequence actionSequence = actionHelper.getActionSequence(file.getPath(), PentahoSystem.loggingLevel, 1);

    String fileName = "content"; //$NON-NLS-1$
    if (actionSequence != null) {
      String title = actionSequence.getTitle();
      if ((title != null) && (title.length() > 0)) {
        fileName = title;
      } else {
        String sequenceName = actionSequence.getSequenceName();

        if ((sequenceName != null) && (sequenceName.length() > 0)) {
          fileName = sequenceName;
        } else {
          List actionDefinitionsList = actionSequence.getActionDefinitionsAndSequences();
          int i = 0;
          boolean done = false;

          while ((actionDefinitionsList.size() > i) && (!done)) {
            IActionDefinition actionDefinition = (IActionDefinition)actionDefinitionsList.get(i);
            String componentName = actionDefinition.getComponentName();
            if ((componentName != null) && (componentName.length() > 0))
            {
              fileName = componentName;
              done = true;
            } else {
              ++i;
            }
          }
        }
      }
    }
    IMimeTypeListener mimeTypeListener = outputHandler.getMimeTypeListener();
    if (mimeTypeListener != null)
      mimeTypeListener.setName(fileName);
  }

  public static OutputStream getOutputStream(HttpServletResponse response, boolean doMessages)
    throws ServletException, IOException
  {
    OutputStream outputStream = null;
    if (doMessages)
      outputStream = new ByteArrayOutputStream();
    else {
      outputStream = response.getOutputStream();
    }

    return outputStream;
  }

  public static void setupOutputHandler(HttpOutputHandler outputHandler, IParameterProvider requestParameters) {
    int outputPreference = 3;
    outputHandler.setOutputPreference(outputPreference);
  }

  public static HttpOutputHandler createOutputHandler(HttpServletResponse response, OutputStream outputStream) {
    return new HttpOutputHandler(response, outputStream, true);
  }

  public static boolean doMessages(HttpServletRequest request) {
    return "true".equalsIgnoreCase(request.getParameter("debug"));  //$NON-NLS-1$//$NON-NLS-2$
  }

  public static String postExecute(IRuntimeContext runtime, boolean doMessages, boolean doWrapper, IOutputHandler outputHandler, Map<String, IParameterProvider> parameterProviders)
    throws Exception
  {
    StringBuffer buffer = new StringBuffer();

    IMessageFormatter formatter = PentahoSystem.get(IMessageFormatter.class);
    if ((!outputHandler.isResponseExpected()) || (doMessages)) {
      if ((runtime != null) && (runtime.getStatus() == 6)) {
        formatter.formatSuccessMessage("text/html", runtime, buffer, doMessages, doWrapper); //$NON-NLS-1$
      }
      else {
        formatter.formatFailureMessage("text/html", runtime, buffer, null); //$NON-NLS-1$
      }
    }
    return buffer.toString();
  }

  @SuppressWarnings("unchecked")
  public static String executeHtml(RepositoryFile file, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, IPentahoSession userSession)
    throws Exception
  {
    IParameterProvider requestParams = new HttpRequestParameterProvider(httpServletRequest);
    IRuntimeContext runtime = null;
    try {
      IOutputHandler outputHandler = createOutputHandler(httpServletResponse, getOutputStream(httpServletResponse, doMessages(httpServletRequest)));

      HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider(userSession);
      HttpRequestParameterProvider requestParameters = new HttpRequestParameterProvider(httpServletRequest);
      Map parameterProviders = new HashMap();
      parameterProviders.put("request", requestParameters); //$NON-NLS-1$
      parameterProviders.put("session", sessionParameters); //$NON-NLS-1$
      createOutputFileName(file, outputHandler);
      int outputPreference = 3;
      outputHandler.setOutputPreference(outputPreference);
      runtime = executeInternal(file, requestParams, httpServletRequest, outputHandler, parameterProviders, userSession);
      boolean doMessages = "true".equalsIgnoreCase(requestParams.getStringParameter("debug", "false")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      boolean doWrapper = "true".equalsIgnoreCase(requestParams.getStringParameter("wrapper", "true")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      String str = postExecute(runtime, doMessages, doWrapper, outputHandler, parameterProviders);
      return str;
    }
    catch (Exception e) {
      logger.error(Messages.getInstance().getString("XactionUtil.ERROR_EXECUTING_ACTION_SEQUENCE", file.getName()), e); //$NON-NLS-1$
      throw e;
    }
    finally
    {
      if (runtime != null)
        runtime.dispose();
      
    }
  }

  @SuppressWarnings("unchecked")
  protected static IRuntimeContext executeInternal(RepositoryFile file, IParameterProvider requestParams, HttpServletRequest httpServletRequest, IOutputHandler outputHandler, Map<String, IParameterProvider> parameterProviders, IPentahoSession userSession)
    throws Exception
  {
    String processId = XactionUtil.class.getName();
    String instanceId = httpServletRequest.getParameter("instance-id"); //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory(""); //$NON-NLS-1$
    List messages = new ArrayList();
    ISolutionEngine solutionEngine = PentahoSystem.get(ISolutionEngine.class, userSession);
    ISystemSettings systemSettings = PentahoSystem.getSystemSettings();

    if (solutionEngine == null) {
      throw new ObjectFactoryException("No Solution Engine");
    }

    boolean instanceEnds = "true".equalsIgnoreCase(requestParams.getStringParameter("instanceends", "true")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    String parameterXsl = systemSettings.getSystemSetting("default-parameter-xsl", "DefaultParameterForm.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
    boolean forcePrompt = "true".equalsIgnoreCase(requestParams.getStringParameter("prompt", "false"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

    solutionEngine.setLoggingLevel(2);
    solutionEngine.init(userSession);
    solutionEngine.setForcePrompt(forcePrompt);
    if (parameterXsl != null) {
      solutionEngine.setParameterXsl(parameterXsl);
    }
    return solutionEngine.execute(file.getPath(), processId, false, instanceEnds, instanceId, false, parameterProviders, outputHandler, null, urlFactory, messages);
  }

  @SuppressWarnings("unchecked")
  public static String executeXml(RepositoryFile file, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, IPentahoSession userSession)
    throws Exception
  {
    try
    {
      HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider(userSession);
      HttpRequestParameterProvider requestParameters = new HttpRequestParameterProvider(httpServletRequest);
      Map parameterProviders = new HashMap();
      parameterProviders.put("request", requestParameters); //$NON-NLS-1$
      parameterProviders.put("session", sessionParameters); //$NON-NLS-1$
      List messages = new ArrayList();
      IParameterProvider requestParams = new HttpRequestParameterProvider(httpServletRequest);
      httpServletResponse.setContentType("text/xml"); //$NON-NLS-1$
      httpServletResponse.setCharacterEncoding(LocaleHelper.getSystemEncoding());

      OutputStream contentStream = new ByteArrayOutputStream();
      SimpleOutputHandler outputHandler = new SimpleOutputHandler(contentStream, false);

      IRuntimeContext runtime = null;
      try {
        runtime = executeInternal(file, requestParams, httpServletRequest, outputHandler, parameterProviders, userSession);
        Document responseDoc = SoapHelper.createSoapResponseDocument(runtime, outputHandler, contentStream, messages);
        OutputFormat format = OutputFormat.createCompactFormat();
        format.setSuppressDeclaration(true);
        format.setEncoding("utf-8"); //$NON-NLS-1$
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLWriter writer = new XMLWriter(outputStream, format);
        writer.write(responseDoc);
        writer.flush();
        return outputStream.toString("utf-8"); //$NON-NLS-1$
      }
      finally
      {
        if (runtime != null)
          runtime.dispose();
      }
    }
    catch (Exception e) {
      logger.warn(Messages.getInstance().getString("XactionUtil.XML_OUTPUT_NOT_SUPPORTED")); //$NON-NLS-1$
      throw e;
    }
  }

  public static String execute(String returnContentType, RepositoryFile file, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, IPentahoSession userSession) throws Exception {
    if ((returnContentType != null) && (returnContentType.equals(MediaType.APPLICATION_XML))) {
      return executeXml(file, httpServletRequest, httpServletResponse, userSession);
    }
    return executeHtml(file, httpServletRequest, httpServletResponse, userSession);
  }
}