package org.pentaho.platform.api.scheduler2;

import java.util.List;
import java.util.Map;

public interface IJobScheduleRequest {

  void setJobId( String jobId );

  String getJobId();

  void setJobName( String jobName );

  void setDuration( long duration );

  void setJobState( JobState state );

  void setInputFile( String inputFilePath );

  void setOutputFile( String outputFilePath );

  Map<String, String> getPdiParameters();

  void setPdiParameters( Map<String, String> stringStringHashMap );

  void setActionClass( String value );

  String getActionClass();

  void setTimeZone( String value );

  String getTimeZone();

  void setSimpleJobTrigger( ISimpleJobTrigger jobTrigger );

  ISimpleJobTrigger getSimpleJobTrigger();

  void setCronJobTrigger( ICronJobTrigger cron );

  String getInputFile();

  String getJobName();

  String getOutputFile();

  List<IJobScheduleParam> getJobParameters();

  void setJobParameters( List<IJobScheduleParam> parameters );

  long getDuration();

  JobState getJobState();

  ICronJobTrigger getCronJobTrigger();
}

