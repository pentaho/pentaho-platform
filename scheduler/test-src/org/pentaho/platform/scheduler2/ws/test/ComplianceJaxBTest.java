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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.scheduler2.ws.test;

import java.util.Date;
import java.util.HashMap;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.scheduler2.ws.JaxBSafeMap;
import org.pentaho.platform.scheduler2.ws.JaxBSafeMap.JaxBSafeEntry;
import org.pentaho.platform.scheduler2.ws.JobAdapter;
import org.pentaho.platform.scheduler2.ws.ListParamValue;
import org.pentaho.platform.scheduler2.ws.MapParamValue;
import org.pentaho.platform.scheduler2.ws.ParamValue;
import org.pentaho.platform.scheduler2.ws.StringParamValue;

import junit.framework.Assert;

@SuppressWarnings( "nls" )
public class ComplianceJaxBTest {

  @Test
  public void testJaxbSafeMap() throws JAXBException {
    HashMap<String, ParamValue> params = new HashMap<String, ParamValue>();
    ListParamValue listValue = new ListParamValue();
    listValue.add( "testListVal0" );
    listValue.add( "testListVal1" );
    MapParamValue mapValue = new MapParamValue();
    mapValue.put( "testMapValueKey", "testMapVal" );

    params.put( "testStringkey", new StringParamValue( "testStringVal" ) );
    params.put( "testListKey", listValue );
    params.put( "testMapKey", mapValue );
    JaxBSafeMap map = new JaxBSafeMap( params );

    JaxBSafeMap unmarshalled = JaxBUtil.outin( map, JaxBSafeMap.class, JaxBSafeEntry.class, StringParamValue.class );

    Assert.assertEquals( "testStringVal", unmarshalled.entry.get( 0 ).getStringValue().toString() );
    Assert.assertEquals( "testListVal0", unmarshalled.entry.get( 1 ).getListValue().get( 0 ) );
    Assert.assertEquals( "testMapVal", unmarshalled.entry.get( 2 ).getMapValue().get( "testMapValueKey" ) );
  }

  @Test
  public void testParamValue() throws JAXBException {
    ParamValue val = new StringParamValue( "testval" );

    ParamValue unmarshalled = JaxBUtil.outin( val, StringParamValue.class );
    Assert.assertEquals( "testval", unmarshalled.toString() );
  }

  @Test
  public void testJaxbSafeJob() throws JAXBException {
    JobAdapter.JaxbSafeJob job = new JobAdapter.JaxbSafeJob();
    final Date NOW = new Date();
    job.lastRun = NOW;
    job.nextRun = NOW;
    job.jobName = "testName";
    job.jobId = "testId";
    job.schedulableClass = "test.schedulable.class";
    job.state = Job.JobState.COMPLETE;
    job.userName = "testUsername";

    HashMap<String, ParamValue> params = new HashMap<String, ParamValue>();
    params.put( "testStringkey", new StringParamValue( "testStringVal" ) );

    JaxBSafeMap safeMap = new JaxBSafeMap( params );
    job.jobParams = safeMap;

    JobAdapter.JaxbSafeJob unmarshalledJob = JaxBUtil.outin( job, JobAdapter.JaxbSafeJob.class );
    Assert.assertEquals( job.lastRun, unmarshalledJob.lastRun );
    Assert.assertEquals( job.nextRun, unmarshalledJob.nextRun );
    Assert.assertEquals( job.jobName, unmarshalledJob.jobName );
    Assert.assertEquals( job.jobId, unmarshalledJob.jobId );
    Assert.assertEquals( job.schedulableClass, unmarshalledJob.schedulableClass );
    Assert.assertEquals( job.userName, unmarshalledJob.userName );
    Assert.assertEquals( job.state, unmarshalledJob.state );
    Assert.assertEquals( job.jobTrigger, unmarshalledJob.jobTrigger );
    Assert.assertTrue( "testStringkey".equals( unmarshalledJob.jobParams.entry.get( 0 ).key ) );
    Assert.assertTrue( "testStringVal".equals( unmarshalledJob.jobParams.entry.get( 0 ).getStringValue().toString() ) );
  }

  @Test
  public void testSimpleTrigger() throws JAXBException {
    SimpleJobTrigger orig = new SimpleJobTrigger();
    Date STARTTIME = new Date();
    orig.setStartTime( STARTTIME );

    SimpleJobTrigger unmarshalled = JaxBUtil.outin( orig, SimpleJobTrigger.class );

    Assert.assertEquals( orig.getStartTime(), unmarshalled.getStartTime() );
  }
}
