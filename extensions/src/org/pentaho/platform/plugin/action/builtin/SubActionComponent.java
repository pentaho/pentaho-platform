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
 * @created Jan 26, 2006 
 * @author James Dixon
 */

package org.pentaho.platform.plugin.action.builtin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.actions.SubActionAction;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.SolutionHelper;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

public class SubActionComponent extends ComponentBase {

  private static final long serialVersionUID = 3557732430102823611L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog(SubActionComponent.class);
  }

  @Override
  protected boolean validateAction() {
    boolean value = false;
    SubActionAction subAction = null;

    if (getActionDefinition() instanceof SubActionAction) {
      subAction = (SubActionAction) getActionDefinition();

      if ((subAction.getAction() != ActionInputConstant.NULL_INPUT)
          && (subAction.getPath() != ActionInputConstant.NULL_INPUT)
          && (subAction.getSolution() != ActionInputConstant.NULL_INPUT)) {
        value = true;
      }
    } else {
      error(Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML())); //$NON-NLS-1$      
    }

    return value;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {
  }

  @SuppressWarnings("deprecation")
  @Override
  protected boolean executeAction() throws Throwable {
    SubActionAction subAction = (SubActionAction) getActionDefinition();
    List ignoreParameters = new ArrayList();
    String solution = subAction.getSolution().getStringValue();
    String path = subAction.getPath().getStringValue();
    String action = subAction.getAction().getStringValue();
    String actionPath;
    if ((path == null) || path.equals("")) { //$NON-NLS-1$
      actionPath = solution + File.separator + action;
    } else {
      actionPath = solution + File.separator + path + File.separator + action;
    }

    // see if we are supposed to proxy the session
    IPentahoSession session = getSession();
    if (subAction.getSessionProxy() != ActionInputConstant.NULL_INPUT) {
      String sessionName = subAction.getSessionProxy().getStringValue();
      // TODO support user-by-user locales
      PentahoSessionParameterProvider params = new PentahoSessionParameterProvider(session);
      session = new UserSession(sessionName, LocaleHelper.getLocale(), params);
    }

    // create a parameter provider
    HashMap parameters = new HashMap();
    Iterator iterator = getInputNames().iterator();
    while (iterator.hasNext()) {
      String inputName = (String) iterator.next();
      if (!StandardSettings.SOLUTION.equals(inputName) && !StandardSettings.PATH.equals(inputName)
          && !StandardSettings.ACTION.equals(inputName)) {
        Object value = getInputValue(inputName);
        ignoreParameters.add(value);
        parameters.put(inputName, value);
      }
    }

    parameters.put(StandardSettings.ACTION_URL_COMPONENT, getInputStringValue(StandardSettings.ACTION_URL_COMPONENT));

    // get the ouptut stream
    // TODO verify this with MB and JD 
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // getDefaultOutputStream();
    ISolutionEngine solutionEngine = null;
    try {
      solutionEngine = SolutionHelper.execute(getProcessId(), session, actionPath, parameters, outputStream, null, true, false);      
      if (outputStream.size() > 0) {
        getDefaultOutputStream(null).write(outputStream.toByteArray());
      }

      int status = solutionEngine.getStatus();
      if (status == IRuntimeContext.RUNTIME_STATUS_SUCCESS) {
        // now pass any outputs back
        Iterator it = this.getOutputNames().iterator();
        while (it.hasNext()) {
          String outputName = (String) it.next();
          IActionParameter param = solutionEngine.getExecutionContext().getOutputParameter(outputName);
          if (param != null) {
            setOutputValue(outputName, param.getValue());
            ignoreParameters.add(param.getValue());
          }
        }
        return true;
      } else {
        return false;
      }
    } finally {
      if (solutionEngine != null) {
        solutionEngine.getExecutionContext().dispose(ignoreParameters);
      }
    }
  }

  @Override
  public boolean init() {
    return true;
  }

}
