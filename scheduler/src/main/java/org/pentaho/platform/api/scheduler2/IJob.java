package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.Map;
import java.util.Date;


public interface IJob {

  enum JobState {
    NORMAL, PAUSED, COMPLETE, ERROR, BLOCKED, UNKNOWN
  }

  JobState state = JobState.UNKNOWN;

  public IJobTrigger getJobTrigger();
  public Map<String, Serializable> getJobParams();
  public String getJobId();
  public String getJobName();
  public Date getLastRun();
  public String getUserName();
  public void setJobTrigger( IJobTrigger jobTrigger );
  public void setJobId( String jobId );
  public void setUserName( String userName );
  public void setJobName( String jobName );
  public JobState getState();
  public void setState( JobState state );
}
