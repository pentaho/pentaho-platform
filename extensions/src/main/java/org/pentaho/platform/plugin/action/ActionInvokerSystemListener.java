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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.di.core.util.HttpClientManager;
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
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;
import org.pentaho.platform.workitem.WorkItemLifecyclePhase;
import org.pentaho.platform.workitem.WorkItemLifecycleEventUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Map;

public class ActionInvokerSystemListener implements IPentahoSystemListener {

  private static final Log logger = LogFactory.getLog( ActionInvokerSystemListener.class );
  private static final String WORK_ITEM_FILE_EXTENSION = ".json";
  private static final String DEFAULT_CONTENT_FOLDER = "system/default-content";
  private static final String ERROR = "error";
  private static final String OK = "ok";
  private static final String WI_STATUS = "wi-status.";
  private static final String IS_ALIVE_ENDPOINT = "/pentaho/ping/alive.gif";
  private static final String WN_HOST = "localhost";
  private static final String HTTP_SCHEME = "http";
  private static final int WN_PORT = 8080;
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

  private int pingIsAlive( ) {
    if ( logger.isDebugEnabled() ) {
      logger.debug( " Checking is alive endpoint..." );
    }
    try {
      final HttpClient client = HttpClientManager.getInstance().createDefaultClient();
      final URIBuilder uriBuilder = new URIBuilder()
          .setScheme( HTTP_SCHEME )
          .setHost( WN_HOST )
          .setPort( WN_PORT )
          .setPath( IS_ALIVE_ENDPOINT );
      final URI pingIsAliveURI = uriBuilder.build();
      final HttpGet pingIsAlive = new HttpGet( pingIsAliveURI.toString()  );
      final HttpResponse response = client.execute( pingIsAlive );
      return response.getStatusLine().getStatusCode();
    } catch ( URISyntaxException | IOException e ) {
      logger.error( e.getMessage() );
    }
    return HttpStatus.SC_INTERNAL_SERVER_ERROR;
  }

  private void waitUntilServerIsAlive( ) {
    int timeout = 600000;
    final int sleep_time = 5000;
    if ( logger.isDebugEnabled() ) {
      logger.debug( "Waiting until the server is alive..." );
    }
    boolean isAlive = false;
    while ( !isAlive ) {
      int statusCode = pingIsAlive();
      if ( statusCode == HttpStatus.SC_OK ) {
        isAlive = true;
      } else {
        if ( timeout == 0 ) {
          placeBreadcrumbFile( getSolutionPath(), false );
          break;
        } else {
          try {
            Thread.sleep( sleep_time );
          } catch ( InterruptedException e ) {
            logger.error( e.getMessage() );
          }
          timeout = timeout - sleep_time;
        }
      }
    }
  }
  /**
   * Launches a background thread which will run work items only after the pentaho server is successfully initialized
   * @param session The Pentaho Session  {@link IPentahoSession}
   * @return always returns true immediately, so that the pentaho server starts up
   */
  @Override
  public boolean startup( IPentahoSession session ) {
    try {
      final Runnable runnable = new Runnable() {
        public void run( ) {
          waitUntilServerIsAlive();
          runWorkItemFromFile( session );
        }
      };
      final Thread s = new Thread( runnable );
      s.start();
    } catch ( Exception e ) {
      logger.error( e.getMessage() );
    }
    return true;
  }

  /**
   * Runs Work Items described in json files upon runWorkItemFromFile of the Pentaho Server.
   *
   * @param session The Pentaho Session  {@link IPentahoSession}
   * @return always returns true, so that the pentaho server starts up
   */
  public boolean runWorkItemFromFile( IPentahoSession session ) {
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
      Payload payload = null;
      logger.info( Messages.getInstance().getString( "ActionInvokerSystemListener.INFO_0003_READ_DATA_FROM_FILE", file.getAbsolutePath() ) );
      FileInputStream fileInputStream = null;
      try {
        fileInputStream = new FileInputStream( file );
        String encoded = IOUtils.toString( fileInputStream );
        payload = new Payload( encoded, LocaleHelper.UTF_8 );
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
        publishFailedWorkItemStatus( payload, e.toString() );
        logger.error( Messages.getInstance().getErrorString( "ActionInvokerSystemListener_ERROR_0002_ERROR_READING_FILES" ) );
      } catch ( IllegalArgumentException e ) {
        publishFailedWorkItemStatus( payload, e.toString() );
        logger.error( Messages.getInstance().getErrorString( "ActionInvokerSystemListener_ERROR_0003_COULD_NOT_PROCESS", file.getName() ) );
      } finally {
        FileUtils.closeQuietly( fileInputStream );
        placeBreadcrumbFile( getSolutionPath(), workItemSuccess );
      }
    }
    return true;
  }

  private void publishFailedWorkItemStatus( final Payload payload, final String failureMessage ) {
    if ( payload != null ) {
      payload.publishWorkItemStatus( WorkItemLifecyclePhase.FAILED, failureMessage );
    } else {
      // when payload is null, we have no way of getting the work item id or any other work item related details, the
      // best we can do is log the failure message, disconnected from all other logs related to the work item; this
      // should theoretically never occur, but we cover this just in case
      WorkItemLifecycleEventUtil.publish( "?", null, WorkItemLifecyclePhase.FAILED, failureMessage );
    }
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
    private String workItemUid;
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
        workItemUid = URLDecoder.decode( jsonVars.getString( ActionUtil.WORK_ITEM_UID ), enc );
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
      publishWorkItemStatus( WorkItemLifecyclePhase.RECEIVED, null );
      return getActionInvoker().invokeAction( action, actionUser, actionMap );
    }

    void publishWorkItemStatus( final WorkItemLifecyclePhase phase, final String failureMessage ) {
      WorkItemLifecycleEventUtil.publish( workItemUid, actionMap, phase, failureMessage );
    }
  }
}
