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
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.workitem.WorkItemLifecyclePhase;
import org.pentaho.platform.workitem.WorkItemLifecyclePublisher;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.Map;

public class ActionInvokerSystemListener implements IPentahoSystemListener {

  private static final Log logger = LogFactory.getLog( ActionInvokerSystemListener.class );
  private static final String WORK_ITEM_FILE_EXTENSION = ".json";
  private static final String DEFAULT_CONTENT_FOLDER = "system/default-content";
  private String environmentVariablesFolder;

  //for unit testability
  String getSolutionPath( ) {
    return PentahoSystem.getApplicationContext().getSolutionPath( DEFAULT_CONTENT_FOLDER );
  }

  /**
   * Runs Work Items described in json files upon startup of the Pentaho Server.
   * @param session
   * @return
   */
  @Override
  public boolean startup( IPentahoSession session ) {
    final String solutionPath = getSolutionPath();

    File[] files = null;
    if ( !StringUtils.isEmpty( environmentVariablesFolder ) ) {
      files = listFiles( new File( environmentVariablesFolder ), WORK_ITEM_FILE_EXTENSION );
      logger.info( "Reading " + WORK_ITEM_FILE_EXTENSION + " files from " + environmentVariablesFolder );
    } else if ( !StringUtils.isEmpty( solutionPath ) ) {
      files = listFiles( new File( solutionPath ), WORK_ITEM_FILE_EXTENSION );
      logger.info( "Reading " + WORK_ITEM_FILE_EXTENSION + " files from " + solutionPath );
    }

    if ( files == null || files.length == 0 ) {
      logger.info( "No " + WORK_ITEM_FILE_EXTENSION + " files found. Exiting." );
      return true;
    }

    for ( File file : files ) {
      Payload payload = null;
      logger.info( "Attempting to read data from " + file.getAbsolutePath() );
      try {
        FileInputStream fileInputStream = new FileInputStream( file );
        String encoded = IOUtils.toString( fileInputStream );
        fileInputStream.close();
        payload = new Payload( encoded, LocaleHelper.UTF_8 );
        logger.info( "Issuing Work Item request" );
        try {
          IActionInvokeStatus status = issueRequest( payload );
        } catch ( Exception e ) {
          // TODO: send status to chronos
        }
      } catch ( IOException e ) {
        publishFailedWorkItemStatus( payload, e.toString() );
        logger.error( "Error reading files. " );
      } catch ( IllegalArgumentException e ) {
        publishFailedWorkItemStatus( payload, e.toString() );
        logger.error( file.getName() + " Payload could not be recreated. Will not process." );
      } finally {
        renameFile( file.getAbsolutePath() );
      }
    }
    return true;
  }

  private void publishFailedWorkItemStatus( final Payload payload, final String failureMessage ) {
    if ( payload != null ) {
      payload.publishWorkItemStatus( WorkItemLifecyclePhase.FAILED, failureMessage );
    } else {
      // when payload is null, we have no way of getting the wok item id or any other work item related details, the
      // best we can do is log the failure message, disconnected from all other logs related to the work item; this
      // should theoretically never occur, but we cover this just in case
      WorkItemLifecyclePublisher.publish( "?", null, WorkItemLifecyclePhase.FAILED, failureMessage );
    }
  }

  //for unit testability
  IActionInvokeStatus issueRequest( Payload payload ) throws Exception {
    return payload.issueRequest();
  }

  private void renameFile( final String filename ) {
    logger.info( "Renaming file " + filename );
    File dotJson = new File( filename );
    File dotJsonDotCurTime = new File( filename + "." + System.currentTimeMillis() );
    if ( !dotJson.renameTo( dotJsonDotCurTime ) ) {
      logger.error( "Could not rename file " + filename );
    }
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
      logger.info( folder.getAbsolutePath() + " no .json files found." );
    }
    return null;
  }
  IAction getActionBean( String actionClass, String actionId ) throws ActionInvocationException {
    return ActionUtil.createActionBean( actionClass, actionId );
  }

  /**
   * Sets the location of the folder where the system listener will consume json files from.
   * If unset, the listener defaults to the system/default-content folder.
   * @param environmentVariablesFolder
   */
  public void setEnvironmentVariablesFolder( String environmentVariablesFolder ) {
    this.environmentVariablesFolder = environmentVariablesFolder;
  }

  public IActionInvoker getActionInvoker( ) {
    return PentahoSystem.get( IActionInvoker.class );
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
      WorkItemLifecyclePublisher.publish( workItemUid, actionMap, phase, failureMessage );
    }
  }
}
