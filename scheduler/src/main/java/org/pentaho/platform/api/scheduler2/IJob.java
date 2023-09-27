package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.Map;
import java.util.Date;


public interface IJob {
  IJobTrigger getJobTrigger();
  Map<String, Serializable> getJobParams();
  String getJobId();
  String getJobName();
  JobState getState();
}
