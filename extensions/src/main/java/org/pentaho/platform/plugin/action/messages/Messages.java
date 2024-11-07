/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.action.messages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.MessagesBase;

import java.util.Map;

public class Messages extends MessagesBase {
  protected static final Log logger = LogFactory.getLog( Messages.class );

  private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

  private static Messages instance = new Messages();

  private Messages() {
    super( BUNDLE_NAME );
  }

  public static Messages getInstance() {
    return instance;
  }

  public String getRunningInBackgroundLocally( final String actionIdentifier, final Map params ) {
    return getString( "ActionInvoker.INFO_0001_RUNNING_IN_BG_LOCALLY", actionIdentifier,
      StringUtil.getMapAsPrettyString( params ) );
  }

  public String getRunningInBackgroundRemotely( final String actionIdentifier, final Map params ) {
    return getString( "ActionInvoker.INFO_0002_RUNNING_IN_BG_REMOTELY", actionIdentifier,
      StringUtil.getMapAsPrettyString( params ) );
  }

  public String getRunningInBgLocallySuccess( final String actionIdentified, final Map params ) {
    return getString( "ActionInvoker.INFO_0005_RUNNING_IN_BG_LOCALLY_SUCCESS", actionIdentified,
            StringUtil.getMapAsPrettyString( params ) );
  }

  public String getPostingToResource( final String actionIdentifier, final String actionParams ) {
    return getString( "ActionInvoker.INFO_0003_POSTING_TO_RESOURCE", actionIdentifier, actionParams );
  }

  public String getResourceResponded( final String url, final int responseCode ) {
    return getString( "ActionInvoker.INFO_0004_RESOURCE_RESPONDED", url, responseCode );
  }

  public String getNoEeLicense() {
    return getErrorString( "ActionInvoker.ERROR_0001_NO_EE_LICENSE" );
  }

  public String getCantInvokeNullAction() {
    return getErrorString( "ActionInvoker.ERROR_0005_ACTION_NULL" );
  }

  public String getCantInvokeActionWithNullMap() {
    return getErrorString( "ActionInvoker.ERROR_0006_MAP_NULL" );
  }

  public String getRemoteEndpointFailure( final String url, final Map params ) {
    return getErrorString( "ActionInvoker.ERROR_0007_RMEOTE_ENTPOINT_FAILURE", url,  StringUtil.getMapAsPrettyString( params ) );
  }

  public String getMapNullCantReturnSp() {
    return getErrorString( "ActionInvoker.ERROR_0008_MAP_NULL_CANT_RETURN_SP" );
  }

  public String getMissingParamsCantReturnSp( final String paramList, final Map params ) {
    return getString( "ActionInvoker.WARN_0002_MISSING_PARAMS_CANT_RETURN_SP", paramList, StringUtil
      .getMapAsPrettyString( params ) );
  }

  public String getActionFailedToExecute( final String actionIdentifier ) {
    return getErrorString( "ActionInvoker.ERROR_0004_ACTION_FAILED", actionIdentifier );
  }

  public String getSkipRemovingOutputFile( final String fileName ) {
    return getString( "ActionInvoker.WARN_0001_SKIP_REMOVING_OUTPUT_FILE", fileName );
  }

  public String getCannotGetRepoFile( final String fileName, final String msg ) {
    return getErrorString( "ActionInvoker.ERROR_0010_CANNOT_GET_REPO_FILE", fileName, msg );
  }

  public String getCouldNotConvertContentToMap( final String content ) {
    return getErrorString( "ActionInvoker.ERROR_0011_COULD_NOT_CONVERT_CONTENT_TO_MAP", content );
  }

  public String getCouldNotInvokeActionLocally( final String actionIdentified, final Map params ) {
    return getErrorString( "ActionInvoker.ERROR_0012_COULD_NOT_INVOKE_ACTION_LOCALLY", actionIdentified,
            StringUtil.getMapAsPrettyString( params ) );
  }

  public String getCouldNotInvokeActionLocallyUnexpected( final String actionIdentified, final String params ) {
    return getErrorString( "ActionInvoker.ERROR_0012_COULD_NOT_INVOKE_ACTION_LOCALLY", actionIdentified, params );
  }

  public String getUnexpectedStatusCode( final int statusCode ) {
    return getErrorString( "ActionInvoker.ERROR_0013_BAD_STATUS_CODE", statusCode );
  }
}
