/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.api.scheduler;

import java.util.Date;

public interface IJobSchedule {
  public String getName();

  public void setName( String name );

  public String getFullname();

  public void setFullname( String fullname );

  public String getTriggerName();

  public void setTriggerName( String triggerName );

  public String getTriggerGroup();

  public void setTriggerGroup( String triggerGroup );

  public int getTriggerState();

  public void setTriggerState( int triggerState );

  public Date getNextFireTime();

  public void setNextFireTime( Date nextFireTime );

  public Date getPreviousFireTime();

  public void setPreviousFireTime( Date previousFireTime );

  public String getJobName();

  public void setJobName( String jobName );

  public String getJobGroup();

  public void setJobGroup( String jobGroup );

  public String getJobDescription();

  public void setJobDescription( String jobDescription );
}
