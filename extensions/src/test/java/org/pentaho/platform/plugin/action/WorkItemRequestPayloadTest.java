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

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.util.ActionUtil;

public class WorkItemRequestPayloadTest {
  private static final String actionParams = "{\"serializedParams\":\"[ \\\"java.util.HashMap\\\","
      + " {\\n  \\\"actionUser\\\" : \\\"admin\\\",\\n  \\\"user_locale\\\" : [ \\\"java.util.Locale\\\", "
      + "\\\"en_GB\\\" ],\\n  \\\"outputFilePattern\\\" : \\\"/home/admin/Inventory List (report).*\\\",\\n  \\\"line\\\" : "
      + "[ \\\"[Ljava.lang.String;\\\", [ \\\"Classic Cars\\\", \\\"Planes\\\", \\\"Ships\\\", \\\"Trains\\\" ] ],\\n  \\\"renderMode\\\" : \\\"PARAMETER\\\",\\n  \\\"accepted-page\\\" : 0,\\n  \\\"inputFile\\\" :"
      + " \\\"/public/Steel Wheels/Inventory List (report).prpt\\\",\\n  \\\"output-target\\\" : \\\"table/html;page-mode=page\\\",\\n  \\\"maximum-query-limit\\\" : 0,\\n  "
      + "\\\"htmlProportionalWidth\\\" : \\\"false\\\",\\n  \\\"query-limit-ui-enabled\\\" : \\\"true\\\",\\n  \\\"lineage-id\\\" : \\\"7227c391-aadf-4b6c-845b-9368d4262938\\\",\\n  \\\"actionId\\\" : "
      + "\\\"prpt.backgroundExecution\\\",\\n  \\\"HideBarSection\\\" : \\\"TRUE\\\",\\n  \\\"showParameters\\\" : \\\"true\\\",\\n  \\\"query-limit\\\" : 0,\\n  \\\"showNotes\\\" : \\\"FALSE\\\",\\n "
      + " \\\"autoCreateUniqueFilename\\\" : true\\n} ]\",\"paramsToRecreate\":[\"::session\",\"streamProvider\"]}";
  private static final String actionId = "prpt.backgroundExecution";
  private static final String actionClass = "org.pentaho.reporting.platform.plugin.SimpleReportingAction";
  private static final String actionUser = "admin";

  //testing valid json, and that the getters return the expected strings
  @Test
  public void testValidWorkItemRequest( ) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put( ActionUtil.INVOKER_ACTIONPARAMS, actionParams );
    jsonObject.put( ActionUtil.INVOKER_ACTIONUSER, actionUser );
    jsonObject.put( ActionUtil.INVOKER_ACTIONID, actionId );
    jsonObject.put( ActionUtil.INVOKER_ACTIONCLASS, actionClass );
    WorkItemRequestPayload workItemRequestPayload = new WorkItemRequestPayload( jsonObject );
    Assert.assertTrue( workItemRequestPayload.isValid() );
    Assert.assertEquals( workItemRequestPayload.getActionClass(), actionClass );
    Assert.assertEquals( workItemRequestPayload.getActionParams(), actionParams );
    Assert.assertEquals( workItemRequestPayload.getActionId(), actionId );
    Assert.assertEquals( workItemRequestPayload.getActionUser(), actionUser );
  }

  //testing json where all the parameter names are correct but some values may be missing
  @Test
  public void testInvalidWorkItemRequest( ) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put( ActionUtil.INVOKER_ACTIONPARAMS, actionParams );
    jsonObject.put( ActionUtil.INVOKER_ACTIONUSER, null );
    jsonObject.put( ActionUtil.INVOKER_ACTIONID, actionId );
    jsonObject.put( ActionUtil.INVOKER_ACTIONCLASS, actionClass );
    WorkItemRequestPayload workItemRequestPayload = new WorkItemRequestPayload( jsonObject );
    Assert.assertFalse( workItemRequestPayload.isValid() );
  }

  //test json object that has most of the params, but is missing one
  @Test
  public void testMissingParameterWorkItemRequest( ) {
    //missing action user
    JSONObject missingActionUser = new JSONObject();
    missingActionUser.put( ActionUtil.INVOKER_ACTIONPARAMS, actionParams );
    missingActionUser.put( ActionUtil.INVOKER_ACTIONCLASS, actionClass );
    missingActionUser.put( ActionUtil.INVOKER_ACTIONID, actionId );
    //missing action id
    JSONObject missingActionId = new JSONObject();
    missingActionId.put( ActionUtil.INVOKER_ACTIONPARAMS, actionParams );
    missingActionId.put( ActionUtil.INVOKER_ACTIONUSER, actionUser );
    missingActionId.put( ActionUtil.INVOKER_ACTIONCLASS, actionClass );
    //missing action class
    JSONObject missingActionClass = new JSONObject();
    missingActionClass.put( ActionUtil.INVOKER_ACTIONPARAMS, actionParams );
    missingActionClass.put( ActionUtil.INVOKER_ACTIONUSER, actionUser );
    missingActionClass.put( ActionUtil.INVOKER_ACTIONID, actionId);
    //missing action params
    JSONObject missingActionParams = new JSONObject();
    missingActionParams.put(ActionUtil.INVOKER_ACTIONUSER, actionUser );
    missingActionParams.put( ActionUtil.INVOKER_ACTIONUSER, actionUser );
    missingActionParams.put( ActionUtil.INVOKER_ACTIONID, actionId );

    WorkItemRequestPayload workItemRequestPayload = new WorkItemRequestPayload( missingActionUser );
    WorkItemRequestPayload workItemRequestPayload1 = new WorkItemRequestPayload( missingActionId );
    WorkItemRequestPayload workItemRequestPayload2 = new WorkItemRequestPayload( missingActionClass );
    WorkItemRequestPayload workItemRequestPayload3 = new WorkItemRequestPayload( missingActionParams );
    Assert.assertFalse( workItemRequestPayload.isValid() );
    Assert.assertFalse( workItemRequestPayload1.isValid() );
    Assert.assertFalse( workItemRequestPayload2.isValid() );
    Assert.assertFalse( workItemRequestPayload3.isValid() );
  }

  //test various JSON objects that won't be formatted correctly, don't have correct parameters, or are empty/null
  @Test
  public void testBadJsonInput( ) {
    Assert.assertFalse( new WorkItemRequestPayload( null ).isValid() );
    Assert.assertFalse( new WorkItemRequestPayload( new JSONObject() ).isValid() );
    JSONObject jsonObject = new JSONObject();
    jsonObject.put( actionId, actionId );
    Assert.assertFalse( new WorkItemRequestPayload( jsonObject ).isValid() );
  }
}
