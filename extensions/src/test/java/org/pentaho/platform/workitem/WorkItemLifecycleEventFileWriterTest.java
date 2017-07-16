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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class WorkItemLifecycleEventFileWriterTest {

  private WorkItemLifecycleEventFileWriter listener = null;
  private WorkItemLifecycleEvent event = null;
  private String workItemUid = "my-uid";
  private String workItemDetails = "some-details";
  private WorkItemLifecyclePhase workItemLifecyclePhase = WorkItemLifecyclePhase.DISPATCHED;
  private String lifecycleDetails = "additional-details";
  private Date sourceTimestamp = new Date();
  private String hostName = "my-host-name";
  private String hostIp = "my-host-ip";
  private String envVarName = "SOME-ENV-VAR";
  private String envVarValue = "SOME-ENV-VALUE";

  @Before
  public void setup() {
    listener = spy( new WorkItemLifecycleEventFileWriter() );
    when( listener.getEnvVarValue( envVarName ) ).thenReturn( envVarValue );

    event = new WorkItemLifecycleEvent( workItemUid, workItemDetails, workItemLifecyclePhase, lifecycleDetails,
      sourceTimestamp );
    event.setSourceHostName( hostName );
    event.setSourceHostIp( hostIp );
  }

  @After
  public void teardown() {
    listener = null;
    event = null;
  }

  @Test
  public void testResources() {
    Assert.assertEquals(
      "ERROR_0001_MISSING_WORK_ITEM_LIFECYCLE - Work item lifecycle information is missing, cannot publish", listener
        .getMessageBundle().getErrorString( "ERROR_0001_MISSING_WORK_ITEM_LIFECYCLE" ) );
  }

  @Test
  public void testGetMessage() {
    // reset env vars, since the machine where the test is run (for instance the build machine) might contain the
    // default env vars, and in that case the test wil fail
    listener.setEnvVars( null );
    assertEquals( workItemUid + "|" + WorkItemLifecycleEventFileWriter.getPaddedLifecyclePhaseNameName(
      workItemLifecyclePhase ) + "|" + WorkItemLifecycleEventFileWriter.DATE_FORMAT.format( sourceTimestamp ) + "|"
        + hostName + "|" + hostIp + "|" + workItemDetails + "|" + lifecycleDetails, listener.getMessage( event ) );

    // remove work item details
    event.setWorkItemDetails( null );
    assertEquals( workItemUid + "|" + WorkItemLifecycleEventFileWriter.getPaddedLifecyclePhaseNameName(
      workItemLifecyclePhase ) + "|" + WorkItemLifecycleEventFileWriter.DATE_FORMAT.format( sourceTimestamp ) + "|" +
        hostName + "|" + hostIp + "|" + lifecycleDetails, listener.getMessage( event ) );

    event.setWorkItemDetails( "" );
    assertEquals( workItemUid + "|" + WorkItemLifecycleEventFileWriter.getPaddedLifecyclePhaseNameName
      ( workItemLifecyclePhase ) + "|" + WorkItemLifecycleEventFileWriter.DATE_FORMAT.format( sourceTimestamp ) + "|"
        + hostName + "|" + hostIp + "|" + lifecycleDetails, listener.getMessage( event ) );

    // set some env vars
    final List<String> envVars = new ArrayList<String>();
    envVars.add( envVarName );
    listener.setEnvVars( envVars );
    assertEquals( workItemUid + "|" + WorkItemLifecycleEventFileWriter.getPaddedLifecyclePhaseNameName
      ( workItemLifecyclePhase ) + "|" + WorkItemLifecycleEventFileWriter.DATE_FORMAT.format( sourceTimestamp ) + "|"
      + hostName + "|" + hostIp + "|" + lifecycleDetails + "|" + envVarValue, listener.getMessage( event ) );

    // remove lifecycle details
    event.setLifecycleDetails( null );
    assertEquals( workItemUid + "|" + WorkItemLifecycleEventFileWriter.getPaddedLifecyclePhaseNameName
      ( workItemLifecyclePhase ) + "|" + WorkItemLifecycleEventFileWriter.DATE_FORMAT.format( sourceTimestamp ) + "|"
      + hostName + "|" + hostIp + "|" + envVarValue, listener.getMessage( event ) );

    event.setLifecycleDetails( "" );
    assertEquals( workItemUid + "|" + WorkItemLifecycleEventFileWriter.getPaddedLifecyclePhaseNameName
      ( workItemLifecyclePhase ) + "|" + WorkItemLifecycleEventFileWriter.DATE_FORMAT.format( sourceTimestamp ) + "|"
      + hostName + "|" + hostIp + "|" + envVarValue, listener.getMessage( event ) );
  }

  @Test
  public void testOnWorkItemLifecycleEvent() {
    listener.onWorkItemLifecycleEvent( event );
    verify( listener, times( 1 ) ).getLogger();
    verify( listener, times( 1 ) ).getMessage( event );
  }
}