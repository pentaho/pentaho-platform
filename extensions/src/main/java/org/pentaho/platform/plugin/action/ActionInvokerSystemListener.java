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
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.api.resources.ActionResource;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileFilter;
import java.net.URLDecoder;

public class ActionInvokerSystemListener implements IPentahoSystemListener {

  private static final Log logger = LogFactory.getLog( ActionInvokerSystemListener.class );
  private static final String WORK_ITEM_FILE_EXTENSION = ".json";
  private static final String DEFAULT_CONTENT_FOLDER = "system/default-content";
  private String environmentVariablesFolder;

  @Override
  public boolean startup( IPentahoSession session ) {
    final String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath( DEFAULT_CONTENT_FOLDER );

    File[] files;
    if ( !StringUtils.isEmpty( environmentVariablesFolder ) ) {
      files = listFiles( new File( environmentVariablesFolder ), WORK_ITEM_FILE_EXTENSION );
      logger.info( "Reading " + WORK_ITEM_FILE_EXTENSION + " files from " + environmentVariablesFolder );
    } else {
      files = listFiles( new File( solutionPath ), WORK_ITEM_FILE_EXTENSION );
      logger.info( "Reading " + WORK_ITEM_FILE_EXTENSION + " files from " + solutionPath );
    }

    if ( files == null || files.length == 0 ) {
      logger.info( "No " + WORK_ITEM_FILE_EXTENSION + " files found. Exiting." );
      return true;
    }

    for ( File file : files ) {

      logger.info( "Attempting to read data from " + file.getAbsolutePath() );
      try {
        String encoded = IOUtils.toString( new FileInputStream( file ) );
        Payload payload = new Payload( encoded, LocaleHelper.UTF_8 );

        logger.info( "Issuing Work Item request" );

        Response response = payload.issueRequest();

        logger.info( "Work Item Request Issued and status returned was " + response.getStatus() );
      } catch ( IOException e ) {
        logger.error( "error reading file " + file.getAbsolutePath() );
      } catch ( IllegalArgumentException e ) {
        logger.error( "Payload could not be recreated. Will not process." );
      } finally {
        renameFile( file.getAbsolutePath() );
      }
    }
    return true;
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

  private void setEnvironmentVariablesFolder( String environmentVariablesFolder ) {
    this.environmentVariablesFolder = environmentVariablesFolder;
  }

  public class Payload {
    private String actionUser;
    private String actionId;
    private String actionClass;
    private String actionParams;


    public Payload( String urlEncodedJson, String enc ) {
      buildPayload( urlEncodedJson, enc );
    }

    private void buildPayload( String urlEncodedJson, String enc ) {
      try {
        JSONObject jsonVars = new JSONObject( urlEncodedJson );
        actionClass = URLDecoder.decode( jsonVars.getString( ActionUtil.INVOKER_ACTIONCLASS ), enc );
        actionId = URLDecoder.decode( jsonVars.getString( ActionUtil.INVOKER_ACTIONID ), enc );
        actionUser = URLDecoder.decode( jsonVars.getString( ActionUtil.INVOKER_ACTIONUSER ), enc );
        actionParams = URLDecoder.decode( jsonVars.getString( ActionUtil.INVOKER_ACTIONPARAMS ), enc );
      } catch ( JSONException | IOException e ) {
        throw new IllegalArgumentException( e );
      }
    }

    public Payload( String json ) {
      buildPayload( json, LocaleHelper.UTF_8 );
    }

    public Response issueRequest( ) throws IOException {
      ActionResource actionResource = new ActionResource();
      return actionResource.invokeAction( ActionUtil.INVOKER_SYNC_VALUE, actionId, actionClass, actionUser,
        ActionParams.fromJson( actionParams ) );
    }
  }
}
