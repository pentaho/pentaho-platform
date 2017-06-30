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
 * Copyright (c) 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.workitem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.util.ActionUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WorkItemLifecycleEventFileWriterTest {

  private WorkItemLifecycleEventFileWriter listener;
  private WorkItemLifecycleEvent event;
  private String workItemUid;
  private Map workItemDetails;
  private WorkItemLifecyclePhase workItemLifecyclePhase;
  private String lifecycleDetails;
  private Date sourceTimestamp;
  private Date targetTimestamp;
  private String actionUser;
  private String inputFile;

  @Before
  public void setup() {
    listener = new WorkItemLifecycleEventFileWriter();
    workItemUid = "my-workItemUid";
    workItemLifecyclePhase = WorkItemLifecyclePhase.SUBMITTED;
    lifecycleDetails = "Some details";
    sourceTimestamp = new Date();
    targetTimestamp = new Date();
    actionUser = "someUser";
    inputFile = "ReportInputFile";
    workItemDetails = new HashMap();
    workItemDetails.put( ActionUtil.INVOKER_ACTIONUSER, actionUser );
    workItemDetails.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, inputFile );

    event = new WorkItemLifecycleEvent( workItemUid, workItemDetails, workItemLifecyclePhase, lifecycleDetails,
      sourceTimestamp );
    event.setTargetTimestamp( targetTimestamp );
  }

  @After
  public void teardown() {
    listener = null;
  }

  @Test
  public void testResources() {
    assertEquals(
      "ERROR_0001_MISSING_WORK_ITEM_LIFECYCLE - Work item lifecycle information is missing, cannot publish", listener
        .getMessageBundle().getErrorString( "ERROR_0001_MISSING_WORK_ITEM_LIFECYCLE" ) );
  }

  @Test
  public void testGetContentDefault() {

    assertEquals( targetTimestamp.getTime() + "|" + workItemUid + "|" + workItemLifecyclePhase.getName() + "|" +
      lifecycleDetails + "|" + sourceTimestamp.getTime() + "|" + event.getSourceHostName() + "|" + event
      .getSourceHostIp() + "|" + actionUser + "|" + inputFile, listener
      .getContent( event ) );
  }

  @Test
  public void testGetContentModified() {
    final String delimiter = "::";
    final List<String> fields = new ArrayList<String>();
    listener.setMessageEventFields( fields );

    fields.add( "targetTimestamp" );
    fields.add( "workItemUid" );
    fields.add( "workItemLifecyclePhase" );

    Map.Entry envVar = null;
    // lookup some env variable, any is file
    if ( System.getenv() != null && System.getenv().size() > 0 ) {
      envVar = System.getenv().entrySet().iterator().next();
    }
    if ( envVar != null ) {
      fields.add( WorkItemLifecycleEventFileWriter.ENV_VAR_PREFIX + envVar.getKey().toString() );
      assertEquals( targetTimestamp + "|" + workItemUid + "|" + workItemLifecyclePhase + "|" + envVar.getValue(),
        listener.getContent( event ) );
    } else {
      assertEquals( targetTimestamp + "|" + workItemUid + "|" + workItemLifecyclePhase, listener.getContent( event ) );
    }
  }
}
