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

package org.pentaho.platform.scheduler2.quartz.test;

import org.junit.Test;
import org.junit.Ignore;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.scheduler2.quartz.QuartzJobKey;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static junit.framework.Assert.assertEquals;

@SuppressWarnings( "nls" )
public class QuartzJobKeyTest {

  static final String TEST_USER = "testUser";
  static final String TEST_JOBNAME = "testJobName";

  @Test
  public void testHappyPathKey() throws SchedulerException {
    //
    // Generate a new key based on client-provided job name and username
    //
    QuartzJobKey key = new QuartzJobKey( TEST_JOBNAME, TEST_USER );
    assertEquals( "Quartz job group is wrong", TEST_JOBNAME, key.getJobName() );
    assertEquals( "Username is wrong", TEST_USER, key.getUserName() );

    //
    // Now parse the jobId back into the key object
    //
    String jobId = key.toString();
    QuartzJobKey parsedKey = QuartzJobKey.parse( jobId );
    assertEquals( "Quartz job group is wrong", TEST_JOBNAME, parsedKey.getJobName() );
    assertEquals( "Username is wrong", TEST_USER, parsedKey.getUserName() );
  }

  @Test( expected = SchedulerException.class )
  public void testKeyMissingJobName() throws SchedulerException {
    new QuartzJobKey( null, TEST_USER );
  }

  @Test( expected = SchedulerException.class )
  public void testKeyMissingUsername() throws SchedulerException {
    new QuartzJobKey( TEST_JOBNAME, null );
  }

  @Test( expected = SchedulerException.class )
  public void testKeyBadJobIdParse() throws SchedulerException {
    QuartzJobKey.parse( "toofewelements:1234567890" );
  }
  
  @Test
  public void testKeyWithColonValues() throws SchedulerException {
    testFromId( "user", "jobName with a (:) Colon", "\t" );
  }
  
  @Test
  public void testOldFormat() throws SchedulerException {
    testFromId( "user", "jobName can't have a colon", ":" );
  }
  
  @Test( expected = SchedulerException.class )
  public void testKeyBadJobIdParse2() throws SchedulerException {
    QuartzJobKey.parse( "" );
  }
  
  private void testFromId (String user, String jobName, String delimiter) throws SchedulerException {
    assert(delimiter.equals("\t") || delimiter.equals( ":" ));
    
    //
    // Generate a new key based on client-provided job name and username
    //
    QuartzJobKey jobKey = QuartzJobKey.parse( user + delimiter + jobName + delimiter + "1234567890" );
    assertEquals( "Incorrect User", user, jobKey.getUserName() );
    assertEquals( "Incorrect Job Name", jobName, jobKey.getJobName() );
    
    //
    // Now parse the jobId back into the key object (should always be tab delimited)
    //
    String jobId = jobKey.toString();
    assertEquals( "Incorrect Returned Job Id", user + "\t" + jobName + "\t" + "1234567890" , jobId);
  }

  static class FakeJob implements Job {

    public void execute( JobExecutionContext arg0 ) throws JobExecutionException {
      // TODO Auto-generated method stub

    }

  }
}
