package org.pentaho.platform.plugin.services.exporter;

import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.CronJobTrigger;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.web.http.api.resources.JobScheduleParam;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class ScheduleExportUtil {

  public ScheduleExportUtil() {
    // to get 100% coverage
  }

  public static JobScheduleRequest createJobScheduleRequest( Job job ) {
    if ( job == null ) {
      throw new IllegalArgumentException( Messages.getInstance().getString( "ScheduleExportUtil.JOB_MUST_NOT_BE_NULL" ) );
    }

    JobScheduleRequest schedule = new JobScheduleRequest();
    schedule.setJobName( job.getJobName() );
    schedule.setDuration( job.getJobTrigger().getDuration() );

    Map<String, Serializable> jobParams = job.getJobParams();

    RepositoryFileStreamProvider streamProvider =
      (RepositoryFileStreamProvider) jobParams.get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER );

    if ( streamProvider != null ) {
      schedule.setInputFile( streamProvider.getInputFilePath() );
      schedule.setOutputFile( streamProvider.getOutputFilePath() );
    }

    for ( String key : jobParams.keySet() ) {
      Serializable serializable = jobParams.get( key );
      JobScheduleParam param = null;
      if ( serializable instanceof String ) {
        String value = (String) serializable;
        if ( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS.equals( key ) ) {
          schedule.setActionClass( value );
        } else if ( IBlockoutManager.TIME_ZONE_PARAM.equals( key ) ) {
          schedule.setTimeZone( value );
        }
        param = new JobScheduleParam( key, (String) serializable );
      } else if ( serializable instanceof Number ) {
        param = new JobScheduleParam( key, (Number) serializable );
      } else if ( serializable instanceof Date ) {
        param = new JobScheduleParam( key, (Date) serializable );
      } else if ( serializable instanceof Boolean ) {
        param = new JobScheduleParam( key, (Boolean) serializable );
      }
      if ( param != null ) {
        schedule.getJobParameters().add( param );
      }
    }

    if ( job.getJobTrigger() instanceof SimpleJobTrigger ) {
      SimpleJobTrigger jobTrigger = (SimpleJobTrigger) job.getJobTrigger();
      schedule.setSimpleJobTrigger( jobTrigger );

    } else if ( job.getJobTrigger() instanceof ComplexJobTrigger ) {
      ComplexJobTrigger jobTrigger = (ComplexJobTrigger) job.getJobTrigger();
      // force it to a cron trigger to get the auto-parsing of the complex trigger
      CronJobTrigger cron = new CronJobTrigger();
      cron.setCronString( jobTrigger.getCronString() );
      cron.setStartTime( jobTrigger.getStartTime() );
      cron.setEndTime( jobTrigger.getEndTime() );
      cron.setDuration( jobTrigger.getDuration() );
      cron.setUiPassParam( jobTrigger.getUiPassParam() );
      schedule.setCronJobTrigger( cron );

    } else if ( job.getJobTrigger() instanceof CronJobTrigger ) {
      CronJobTrigger jobTrigger = (CronJobTrigger) job.getJobTrigger();
      schedule.setCronJobTrigger( jobTrigger );

    } else {
      // don't know what this is, can't export it
      throw new IllegalArgumentException( Messages.getInstance().getString(
        "PentahoPlatformExporter.UNSUPPORTED_JobTrigger", job.getJobTrigger().getClass().getName() ) );

    }
    return schedule;
  }

}
