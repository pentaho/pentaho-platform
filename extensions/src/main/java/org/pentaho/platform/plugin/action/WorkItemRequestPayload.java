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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.util.ActionUtil;
import java.util.Arrays;

public class WorkItemRequestPayload {

  private String actionId;
  private String actionClass;
  private String actionParams;
  private String actionUser;
  private static final Log logger = LogFactory.getLog( IPentahoSystemListener.class );
  private boolean isValid;

  public WorkItemRequestPayload( JSONObject jsonEnvironmentVariables ) {
    isValid = buildPayload( jsonEnvironmentVariables );
  }

  public String getActionId( ) {
    return actionId;
  }
  public String getActionUser( ) {
    return actionUser;
  }
  public String getActionClass( ) {
    return actionClass;
  }
  public String getActionParams( ) {
    return actionParams;
  }

  private boolean buildPayload( JSONObject json ) {
    try {
      actionClass = (String) json.get( ActionUtil.INVOKER_ACTIONCLASS );
      actionId = (String) json.get( ActionUtil.INVOKER_ACTIONID );
      actionParams = (String) json.get( ActionUtil.INVOKER_ACTIONPARAMS );
      actionUser = (String) json.get( ActionUtil.INVOKER_ACTIONUSER );
      return !StringUtils.isEmpty( actionClass )
          && !StringUtils.isEmpty( actionId )
          && !StringUtils.isEmpty( actionParams )
          && !StringUtils.isEmpty( actionUser );
    } catch ( Exception e ) {
      logger.error( Arrays.toString( e.getStackTrace( ) ) );
      //if any exception occurs json was not in valid format and thus return false
      return false;
    }
  }
  public boolean isValid( ) {
    return isValid;
  }
}
