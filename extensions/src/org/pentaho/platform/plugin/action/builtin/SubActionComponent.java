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

package org.pentaho.platform.plugin.action.builtin;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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

  private static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

  @Override
  public Log getLogger() {
    return LogFactory.getLog( SubActionComponent.class );
  }

  @Override
  protected boolean validateAction() {
    boolean value = false;
    SubActionAction subAction = null;

    if ( getActionDefinition() instanceof SubActionAction ) {
      subAction = (SubActionAction) getActionDefinition();

      if ( ( subAction.getAction() != ActionInputConstant.NULL_INPUT )
          && ( subAction.getPath() != ActionInputConstant.NULL_INPUT )
          && ( subAction.getSolution() != ActionInputConstant.NULL_INPUT ) ) {
        value = true;
      } else if ( subAction.getPath() != ActionInputConstant.NULL_INPUT ) {
        // for backwards compatibility we will retain action/path/solution
        // however, in 5.0 and beyond we only need the path element, eg /home/user/folder/file.xaction
        value = true;
      }
    } else {
      error( Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML() ) ); //$NON-NLS-1$      
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

  public String buildActionPath( final String solution, final String path, final String action ) {
    String actionPath = "";

    if ( StringUtils.isEmpty( solution ) == false ) {
      actionPath = solution;
    }

    if ( StringUtils.isEmpty( path ) == false ) {
      if ( StringUtils.isEmpty( actionPath ) ) {
        actionPath = path;
      } else {
        actionPath += PATH_SEPARATOR + path;
      }
    }

    if ( StringUtils.isEmpty( action ) == false ) {
      if ( StringUtils.isEmpty( actionPath ) ) {
        actionPath = action;
      } else {
        actionPath += PATH_SEPARATOR + action;
      }
    }

    // in the unlikely event that the xaction is referencing the path to the subaction using
    // Windows separators (back-slash), we will now convert them to JCR separator (forward slash)
    actionPath = actionPath.replace( '\\', '/' );

    // remove any double // that may have been introduced by the xaction providing extra leading
    // or trailing slashes which get mixed with the PATH_SEPARATOR that we are adding
    while ( actionPath.contains( "//" ) ) {
      actionPath = actionPath.replaceAll( "//", "/" );
    }

    // when using JCR (5.0 and up) we need to make sure the path starts with / (PATH_SEPARATOR)
    if ( StringUtils.isEmpty( actionPath ) == false
        && actionPath.startsWith( PATH_SEPARATOR ) == false ) {
      actionPath = PATH_SEPARATOR + actionPath;
    }
    return actionPath;
  }

  @SuppressWarnings ( "deprecation" )
  @Override
  protected boolean executeAction() throws Throwable {
    SubActionAction subAction = (SubActionAction) getActionDefinition();
    List<Object> ignoreParameters = new ArrayList<Object>();

    String actionPath = buildActionPath( subAction.getSolution().getStringValue(),
        subAction.getPath().getStringValue(),
        subAction.getAction().getStringValue() );

    // see if we are supposed to proxy the session
    IPentahoSession session = getSession();
    if ( subAction.getSessionProxy() != ActionInputConstant.NULL_INPUT ) {
      String sessionName = subAction.getSessionProxy().getStringValue();
      // TODO support user-by-user locales
      PentahoSessionParameterProvider params = new PentahoSessionParameterProvider( session );
      session = new UserSession( sessionName, LocaleHelper.getLocale(), params );
    }

    // create a parameter provider
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    Iterator<?> iterator = getInputNames().iterator();
    while ( iterator.hasNext() ) {
      String inputName = (String) iterator.next();
      if ( !StandardSettings.SOLUTION.equals( inputName ) && !StandardSettings.PATH.equals( inputName )
          && !StandardSettings.ACTION.equals( inputName ) ) {
        Object value = getInputValue( inputName );
        ignoreParameters.add( value );
        parameters.put( inputName, value );
      }
    }

    parameters
        .put( StandardSettings.ACTION_URL_COMPONENT, getInputStringValue( StandardSettings.ACTION_URL_COMPONENT ) );

    // get the ouptut stream
    // TODO verify this with MB and JD
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // getDefaultOutputStream();
    ISolutionEngine solutionEngine = null;
    try {
      solutionEngine =
          SolutionHelper.execute( getProcessId(), session, actionPath, parameters, outputStream, null, true, false );
      if ( outputStream.size() > 0 ) {
        getDefaultOutputStream( null ).write( outputStream.toByteArray() );
      }

      int status = solutionEngine.getStatus();
      if ( status == IRuntimeContext.RUNTIME_STATUS_SUCCESS ) {
        // now pass any outputs back
        Iterator<?> it = this.getOutputNames().iterator();
        while ( it.hasNext() ) {
          String outputName = (String) it.next();
          IActionParameter param = solutionEngine.getExecutionContext().getOutputParameter( outputName );
          if ( param != null ) {
            setOutputValue( outputName, param.getValue() );
            ignoreParameters.add( param.getValue() );
          }
        }
        return true;
      } else {
        return false;
      }
    } finally {
      if ( solutionEngine != null ) {
        solutionEngine.getExecutionContext().dispose( ignoreParameters );
      }
    }
  }

  @Override
  public boolean init() {
    return true;
  }

}
