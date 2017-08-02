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
 * Copyright (c) 2017 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.action;

import org.pentaho.platform.web.http.api.resources.utils.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.api.resources.WorkerNodeActionInvokerAuditor;

import java.io.Serializable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

public class ActionInvokerSystemListener implements IPentahoSystemListener {

  private static final Log logger = LogFactory.getLog( ActionInvokerSystemListener.class );
  private static final String WORK_ITEM_FILE_EXTENSION = ".json";
  private static final String DEFAULT_CONTENT_FOLDER = "system/default-content";
  private static final String ERROR = "error";
  private static final String OK = "ok";
  private static final String WI_STATUS = "wi-status.";
  private String environmentVariablesFolder;
  //for unit testability
  String getSolutionPath( ) {
    return PentahoSystem.getApplicationContext().getSolutionPath( DEFAULT_CONTENT_FOLDER );
  }

  //places a failure breadcrumb in the specified folder
  private void placeBreadcrumbFile( String folderPath, boolean success ) {
    String ext = ERROR;
    if ( success ) {
      ext = OK;
    }
    File breadcrumb = new File( folderPath, WI_STATUS + ext );

    try {
      if ( !breadcrumb.createNewFile() ) {
        logger.error( Messages.getInstance().getErrorString( "ActionInvokerSystemListener.ERROR_0004_COULD_NOT_WRITE_STATUS" ) );
      }
    } catch ( IOException e ) {
      logger.error( Messages.getInstance().getErrorString( "ActionInvokerSystemListener.ERROR_0004_COULD_NOT_WRITE_STATUS" ) );
    }
  }
  /**
   * Runs Work Items described in json files upon startup of the Pentaho Server.
   *
   * @param session The Pentaho Session  {@link IPentahoSession}
   * @return always returns true, so that the pentaho server starts up
   */
  @Override
  public boolean startup( IPentahoSession session ) {
    final String solutionPath = getSolutionPath();
    boolean workItemSuccess = false;
    File[] files = null;
    if ( !StringUtils.isEmpty( environmentVariablesFolder ) ) {
      files = listFiles( new File( environmentVariablesFolder ), WORK_ITEM_FILE_EXTENSION );
      logger.info( Messages.getInstance().getString( "ActionInvokerSystemListener.INFO_0001_READ_FILES_FROM_FOLDER", environmentVariablesFolder, WORK_ITEM_FILE_EXTENSION ) );
    } else if ( !StringUtils.isEmpty( solutionPath ) ) {
      files = listFiles( new File( solutionPath ), WORK_ITEM_FILE_EXTENSION );
      logger.info( Messages.getInstance().getString( "ActionInvokerSystemListener.INFO_0003_READ_DATA_FROM_FILE", WORK_ITEM_FILE_EXTENSION, solutionPath ) );
    }

    if ( files == null || files.length == 0 ) {
      logger.info( Messages.getInstance().getString( "ActionInvokerSystemListener.INFO_0002_NO_FILES_FOUND", WORK_ITEM_FILE_EXTENSION ) );
      placeBreadcrumbFile( getSolutionPath(), false );
      return true;
    }

    for ( File file : files ) {
      logger.info( Messages.getInstance().getString( "ActionInvokerSystemListener.INFO_0003_READ_DATA_FROM_FILE", file.getAbsolutePath() ) );
      FileInputStream fileInputStream = null;
      try {
        fileInputStream = new FileInputStream( file );
        String encoded = IOUtils.toString( fileInputStream );
        Payload payload = new Payload( encoded, LocaleHelper.UTF_8 );
        logger.info( Messages.getInstance().getString( "ActionInvokerSystemListener.INFO_0004_ISSUE_REQUEST" ) );
        try {
          IActionInvokeStatus status = issueRequest( payload );
          if ( status != null && status.getThrowable() == null ) {
            logger.info( Messages.getInstance().getString( "ActionInvokerSystemListener.INFO_0005_REQUEST_SUCCEEDED"  ) );
            workItemSuccess = true;
          } else {
            logger.error( Messages.getInstance().getErrorString( "ActionInvokerSystemListener.ERROR_0001_REQUEST_FAILED" ) );
          }
        } catch ( Exception e ) {
          logger.error( e.getMessage() );
        }
      } catch ( IOException e ) {
        logger.error( Messages.getInstance().getErrorString( "ActionInvokerSystemListener_ERROR_0002_ERROR_READING_FILES" ) );
      } catch ( IllegalArgumentException e ) {
        logger.error( Messages.getInstance().getErrorString( "ActionInvokerSystemListener_ERROR_0003_COULD_NOT_PROCESS", file.getName() ) );
      } finally {
        FileUtils.closeQuietly( fileInputStream );
        placeBreadcrumbFile( getSolutionPath(), workItemSuccess );
      }
    }
    return true;
  }

  private IActionInvokeStatus issueRequest( Payload payload ) throws Exception {
    return payload.issueRequest();
  }

  @Override
  public void shutdown( ) {

  }

  File[] listFiles( final File folder, final String fileExtension ) {
    if ( folder.isDirectory() && folder.canRead() ) {
      return folder.listFiles( new FileFilter() {
        @Override
        public boolean accept( File f ) {
          return f.isFile() && f.getName().toLowerCase().endsWith( fileExtension );
        }
      } );
    } else {
      logger.info( Messages.getInstance().getString( "ActionInvokerSystemListener_INFO_0006_NO_FILES_FOUND", folder.getAbsolutePath() ) );
    }
    return null;
  }

  IAction getActionBean( String actionClass, String actionId ) throws ActionInvocationException {
    return ActionUtil.createActionBean( actionClass, actionId );
  }

  /**
   * Sets the location of the folder where the system listener will consume json files from.
   * If unset, the listener defaults to the system/default-content folder.
   *
   * @param environmentVariablesFolder The folder where work item files will be placed
   */
  public void setEnvironmentVariablesFolder( String environmentVariablesFolder ) {
    this.environmentVariablesFolder = environmentVariablesFolder;
  }

  public IActionInvoker getActionInvoker( ) {
    return new WorkerNodeActionInvokerAuditor( PentahoSystem.get( IActionInvoker.class ) );
  }

  class Payload {
    private String actionUser;

    private IAction action;
    private Map<String, Serializable> actionMap;


    Payload( String urlEncodedJson, String enc ) {
      buildPayload( urlEncodedJson, enc );
    }

    private void buildPayload( String urlEncodedJson, String enc ) {
      try {
        JSONObject jsonVars = new JSONObject( urlEncodedJson );
        String actionClass = URLDecoder.decode( jsonVars.getString( ActionUtil.INVOKER_ACTIONCLASS ), enc );
        String actionId = URLDecoder.decode( jsonVars.getString( ActionUtil.INVOKER_ACTIONID ), enc );
        actionUser = URLDecoder.decode( jsonVars.getString( ActionUtil.INVOKER_ACTIONUSER ), enc );
        action = getActionBean( actionClass, actionId );
        String stringActionParams = URLDecoder.decode( jsonVars.getString( ActionUtil.INVOKER_ACTIONPARAMS ), enc );
        ActionParams actionParams = ActionParams.fromJson( stringActionParams );
        actionMap = ActionParams.deserialize( action, actionParams );
      } catch ( JSONException | IOException | ActionInvocationException e ) {
        throw new IllegalArgumentException( e );
      }
    }

    Payload( String json ) {
      buildPayload( json, LocaleHelper.UTF_8 );
    }

    IActionInvokeStatus issueRequest( ) throws Exception {
      return getActionInvoker().invokeAction( action, actionUser, actionMap );
    }
  }
}
