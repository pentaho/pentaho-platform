/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.uifoundation.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

public class ActionComponent extends BaseUIComponent {

  private static final long serialVersionUID = 1217363866006312765L;

  private static final Log logger = LogFactory.getLog( ActionComponent.class );

  private String solutionName;

  private String actionPath;

  private String actionName;

  private String instanceId;

  private int outputPreference;

  public ActionComponent( final String solutionName, final String actionPath, final String actionName,
      final String instanceId, final int outputPreference, final IPentahoUrlFactory urlFactory, final List messages ) {
    super( urlFactory, messages, solutionName + File.separator + actionPath );
    this.solutionName = solutionName;
    this.actionName = actionName;
    this.actionPath = actionPath;
    this.instanceId = instanceId;
    this.outputPreference = outputPreference;
  }

  public ActionComponent( final String actionString, final String instanceId, final int outputPreference,
      final IPentahoUrlFactory urlFactory, final List messages ) {
    super( urlFactory, messages, null );
    ActionInfo info = ActionInfo.parseActionString( actionString );
    if ( info != null ) {
      solutionName = info.getSolutionName();
      actionPath = info.getPath();
      actionName = info.getActionName();
    }
    setSourcePath( solutionName + File.separator + actionPath );
    this.instanceId = instanceId;
    this.outputPreference = outputPreference;
  }

  @Override
  public Log getLogger() {
    return ActionComponent.logger;
  }

  @Override
  public boolean validate() {
    return true;
  }

  protected ByteArrayOutputStream getContentAsStream( final String mimeType ) {
    IPentahoSession userSession = getSession();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, true );
    outputHandler.setOutputPreference( outputPreference );

    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, getSession() );
    solutionEngine.setLoggingLevel( getLoggingLevel() );
    solutionEngine.init( userSession );

    IRuntimeContext context = null;
    try {
      String actionSeqPath = ActionInfo.buildSolutionPath( solutionName, actionPath, actionName );

      context =
          solutionEngine
              .execute(
                  actionSeqPath,
                  Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_TEST" ), false, true, instanceId, false, getParameterProviders(), outputHandler, null, urlFactory, getMessages() ); //$NON-NLS-1$
    } finally {
      if ( context != null ) {
        context.dispose();
      }
    }
    return outputStream;
  }

  @Override
  public String getContent( final String mimeType ) {
    ByteArrayOutputStream outputStream = getContentAsStream( mimeType );
    // TODO test the return result
    String result = ""; //$NON-NLS-1$
    try {
      result = outputStream.toString( LocaleHelper.getSystemEncoding() );
    } catch ( Exception e ) {
      return outputStream.toString();
    }
    return result;

  }

}
