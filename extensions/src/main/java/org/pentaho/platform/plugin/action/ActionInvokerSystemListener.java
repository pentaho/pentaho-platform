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
 * Copyright (c) 2017 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.plugin.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.web.http.api.resources.ActionResource;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileFilter;

public class ActionInvokerSystemListener implements IPentahoSystemListener {

  private String environmentVariablesFilepath;
  private static final Log logger = LogFactory.getLog( IPentahoSystemListener.class );
  private static final String WORK_ITEM_FILE_EXTENSION = ".json";
  private static final String DEFAULT_CONTENT_FOLDER = "system/default-content";

  private Map<String, JSONObject> parseJsonFiles( )  {
    Map<String, JSONObject> dotJsons = new HashMap<>();
    final String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath( DEFAULT_CONTENT_FOLDER );
    if ( StringUtils.isEmpty( solutionPath ) ) {
      logger.info( getClass().getName() + " Empty solution path" );
    } else {
      File[] jsonFiles = listFiles( new File( solutionPath ), WORK_ITEM_FILE_EXTENSION );
      if ( jsonFiles != null ) {
        for ( File jsonFile : jsonFiles ) {
          JSONObject jsonObject = getJsonFromFile( jsonFile );
          if ( jsonObject != null ) {
            dotJsons.put( jsonFile.getAbsolutePath(), jsonObject );
          }
        }
      }
    }
    return dotJsons;
  }

  private JSONObject getJsonFromFile( final File file )  {
    FileReader fr;
    try {
      fr = new FileReader( file );
    } catch ( FileNotFoundException e ) {
      logger.error( e.getMessage() );
      return null;
    }
    try {
      JSONParser jsonParser = new JSONParser();
      JSONObject jsonObject = (JSONObject) jsonParser.parse( fr );
      String jsonString = jsonObject.toJSONString();
      fr.close();
      return (JSONObject) jsonParser.parse( jsonString );
    } catch ( ParseException e ) {
      logger.error( "File: " + file.getAbsolutePath() + " is not valid json, and will not be processed" );
      try {
        fr.close();
      } catch ( IOException ex ) {
        ex.getMessage();
      }
      //rename the file here and allow the chronos container to die because of bad input
      renameFile( file.getAbsolutePath() );
    } catch ( IOException e ) {
      logger.error( e.getMessage() );
      renameFile( file.getAbsolutePath() );
    }
    return null;
  }

  private File[] listFiles( final File folder, final String fileExtension ) {
    if ( folder.isDirectory() && folder.canRead() ) {
      return folder.listFiles( new FileFilter() {
        @Override
        public boolean accept( File f ) {
          return f.isFile() && f.getName().toLowerCase().endsWith( fileExtension );
        }
      } );
    } else {
      logger.error( folder.getAbsolutePath() + " is not a valid directory" );
    }
    return null;
  }

  private void renameFile( final String filename ) {
    File dotJson = new File( filename );
    File dotJsonDotCurTime = new File( filename + "." + System.currentTimeMillis() );
    dotJson.renameTo( dotJsonDotCurTime );
  }

  /**
   * System listener looks for JSON files at start up and runs them as work items
   * @param session
   * @return
   */
  @Override
  public boolean startup( IPentahoSession session ) {
    Response status;
    Map<String, JSONObject> jsonFiles = new HashMap<>(); //map between the json objects and the filenames from which they were consumed
    if ( environmentVariablesFilepath != null ) {
      JSONObject environmentVariables = getJsonFromFile( new File( environmentVariablesFilepath ) );
      jsonFiles.put( environmentVariablesFilepath, environmentVariables );
    } else {
      jsonFiles = parseJsonFiles();
    }
    for ( String filename : jsonFiles.keySet() ) {
      JSONObject json = jsonFiles.get( filename );
      WorkItemRequestPayload payload = new WorkItemRequestPayload( json );
      if ( !payload.isValid() ) {
        logger.error( "File: " + filename + " is missing required parameters, skipping" );
        renameFile( filename );
      } else {
        try {
          status = issueRequest( payload );
          if ( status == null ) {
            logger.error( "File: " + filename + " has parameters formatted in an unparsable way. Could not process work item." );
          }
          //todo- sometype of status handling once WI lifecycle is defined
        } catch ( Exception e ) {
          logger.error( "Error processing " + filename + " skipping" );
        } finally {
          renameFile( filename );
        }
      }
    }
    return true;
  }

  private Response issueRequest( final WorkItemRequestPayload payload ) {
    return new ActionResource().invokeAction(
        ActionUtil.INVOKER_SYNC_VALUE,
        payload.getActionId(),
        payload.getActionClass(),
        payload.getActionUser(),
        payload.getActionParams( ) );
  }

  @Override
  public void shutdown( ) {

  }
  public void setEnvironmentVariablesFilepath( String environmentVariablesFilepath ) {
    this.environmentVariablesFilepath = environmentVariablesFilepath;
  }
}
