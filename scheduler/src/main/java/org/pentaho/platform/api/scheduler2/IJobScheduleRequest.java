package org.pentaho.platform.api.scheduler2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IJobScheduleRequest {
  void setJobName( String jobName );

  void setDuration( long duration );

  void setJobState( IJob.JobState state );

  void setInputFile( String inputFilePath );

  void setOutputFile( String outputFilePath );

  <V, K> Map<K,V> getPdiParameters();

  void setPdiParameters( HashMap<String, String> stringStringHashMap );

  void setActionClass( String value );

  void setTimeZone( String value );

  void setSimpleJobTrigger( ISimpleJobTrigger jobTrigger );

  void setCronJobTrigger( ICronJobTrigger cron );

  String getInputFile();

  String getJobName();

  String getOutputFile();

  List<IJobScheduleParam> getJobParameters();

  long getDuration();

  String getActionClass();

  String getTimeZone();

  ISimpleJobTrigger getSimpleJobTrigger();
}

