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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.plugin.services.exporter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.CronJobTrigger;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.web.http.api.resources.JobScheduleParam;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

public class ScheduleExportUtil {

  public static final  String RUN_PARAMETERS_KEY = "parameters";

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
    schedule.setJobState( job.getState() );

    Map<String, Serializable> jobParams = job.getJobParams();

    Object streamProviderObj = jobParams.get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER );
    RepositoryFileStreamProvider streamProvider = null;
    if ( streamProviderObj instanceof RepositoryFileStreamProvider ) {
      streamProvider = (RepositoryFileStreamProvider) streamProviderObj;
    } else if ( streamProviderObj instanceof String ) {
      String inputFilePath = null;
      String outputFilePath = null;
      String inputOutputString = (String) streamProviderObj;
      String[] tokens = inputOutputString.split( ":" );
      if ( !ArrayUtils.isEmpty( tokens ) && tokens.length == 2 ) {
        inputFilePath = tokens[ 0 ].split( "=" )[ 1 ].trim();
        outputFilePath = tokens[ 1 ].split( "=" )[ 1 ].trim();

        streamProvider = new RepositoryFileStreamProvider( inputFilePath, outputFilePath, true );
      }
    }

    if ( streamProvider != null ) {
      schedule.setInputFile( streamProvider.getInputFilePath() );
      schedule.setOutputFile( streamProvider.getOutputFilePath() );
    } else {
      // let's look to see if we can figure out the input and output file
      String directory = (String) jobParams.get( "directory" );
      String transName = (String) jobParams.get( "transformation" );
      String jobName = (String) jobParams.get( "job" );
      String artifact = transName == null ? jobName : transName;

      if ( directory != null && artifact != null ) {
        String outputFile = RepositoryFilenameUtils.concat( directory, artifact );
        outputFile += "*";

        if ( artifact.equals( jobName ) ) {
          artifact += ".kjb";
        } else {
          artifact += ".ktr";
        }
        String inputFile = RepositoryFilenameUtils.concat( directory, artifact );
        schedule.setInputFile( inputFile );
        schedule.setOutputFile( outputFile );
      }
    }

    for ( String key : jobParams.keySet() ) {
      Serializable serializable = jobParams.get( key );
      if ( RUN_PARAMETERS_KEY.equals( key ) ) {
        schedule.getPdiParameters().putAll( (Map<String, String>) serializable );
      } else {
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
