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

package org.pentaho.platform.plugin.action.jfreereport.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.util.Iterator;
import java.util.Set;

/**
 * Creation-Date: 07.07.2006, 16:26:25
 * 
 * @author Thomas Morgner
 */
public class JFreeReportValidateParametersComponent extends AbstractJFreeReportComponent {
  private static final long serialVersionUID = -2888256934867581182L;

  private boolean parameterUiNeeded;

  public JFreeReportValidateParametersComponent() {
  }

  @Override
  protected boolean validateAction() {
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {

  }

  private boolean isParameterUIAvailable() {
    /*
     * See if we are allowed to generate a parameter selection user interface. If we are being called as part of a
     * process, this will not be allowed.
     */
    if ( !feedbackAllowed() ) {
      // We could not get an output stream for the feedback, but we are
      // allowed
      // to generate UI, so return an error
      error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0020_INVALID_FEEDBACK_STREAM" ) ); //$NON-NLS-1$
      return false;
    }
    // We need input from the user, we have delivered an input form into the
    // feeback stream
    setFeedbackMimeType( "text/html" ); //$NON-NLS-1$
    return true;
  }

  @Override
  protected boolean executeAction() throws Throwable {
    final String defaultValue = ""; //$NON-NLS-1$

    // Get input parameters, and set them as properties in the report
    // object.
    final Set paramNames = getInputNames();
    boolean parameterUINeeded = false;

    final Iterator it = paramNames.iterator();
    while ( it.hasNext() ) {
      String paramName = (String) it.next();
      Object paramValue = getInputValue( paramName );
      if ( ( paramValue == null ) || ( "".equals( paramValue ) ) ) //$NON-NLS-1$
      {
        IActionParameter paramParameter = getInputParameter( paramName );
        if ( paramParameter.getPromptStatus() == IActionParameter.PROMPT_PENDING ) {
          parameterUINeeded = true;
          continue;
        }
        if ( isParameterUIAvailable() ) {
          // The parameter value was not provided, and we are allowed
          // to
          // create user interface forms
          createFeedbackParameter( paramName, paramName, "", defaultValue, true ); //$NON-NLS-1$
          parameterUINeeded = true;
        } else {
          return false;
        }
      }
    }
    if ( parameterUINeeded ) {
      this.parameterUiNeeded = true;
    } else {
      this.parameterUiNeeded = false;
    }

    return true;
  }

  public boolean isParameterUiNeeded() {
    return parameterUiNeeded;
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( JFreeReportValidateParametersComponent.class );
  }
}
