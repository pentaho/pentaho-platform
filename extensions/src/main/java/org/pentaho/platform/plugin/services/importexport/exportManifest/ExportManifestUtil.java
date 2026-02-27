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


package org.pentaho.platform.plugin.services.importexport.exportManifest;

import org.pentaho.platform.api.scheduler.JobScheduleParam;
import org.pentaho.platform.api.scheduler2.ICronJobTrigger;
import org.pentaho.platform.api.scheduler2.IJobScheduleParam;
import org.pentaho.platform.api.scheduler2.IJobScheduleRequest;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.ISimpleJobTrigger;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.CronJobTrigger;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.JobScheduleRequest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.SimpleJobTrigger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExportManifestUtil {

  public static ArrayList<JobScheduleRequest> fromSchedulerToBindingRequest( List<IJobScheduleRequest> scheduleList ) {
    ArrayList<JobScheduleRequest> schedules = new ArrayList<>();
    for ( IJobScheduleRequest schedulerRequest : scheduleList ) {
      JobScheduleRequest bindingRequest = new JobScheduleRequest();
      bindingRequest.setJobName( schedulerRequest.getJobName() );
      bindingRequest.setDuration( schedulerRequest.getDuration() );
      bindingRequest.setJobState( schedulerRequest.getJobState() );
      bindingRequest.setInputFile( schedulerRequest.getInputFile() );
      bindingRequest.setOutputFile( schedulerRequest.getOutputFile() );
      bindingRequest.setPdiParameters( schedulerRequest.getPdiParameters() );
      bindingRequest.setActionClass( schedulerRequest.getActionClass() );
      bindingRequest.setTimeZone( schedulerRequest.getTimeZone() );
      bindingRequest.setJobParameters(
        fromSchedulerToBindingRequestJobParameters( schedulerRequest.getJobParameters() ) );
      bindingRequest.setSimpleJobTrigger(
        fromSchedulerToBindingRequestJobTrigger( schedulerRequest.getSimpleJobTrigger() ) );
      bindingRequest.setCronJobTrigger(
        fromSchedulerToBindingRequestCronJobTrigger( schedulerRequest.getCronJobTrigger() ) );
      schedules.add( bindingRequest );
    }
    return schedules;
  }

  private static List<JobScheduleParam> fromSchedulerToBindingRequestJobParameters(
    List<IJobScheduleParam> incomingParams ) {
    List<JobScheduleParam> outgoingParams = new ArrayList<>();
    for ( IJobScheduleParam incomingParam : incomingParams ) {
      JobScheduleParam outgoingParam = new JobScheduleParam();
      outgoingParam.setName( incomingParam.getName() );
      outgoingParam.setType( incomingParam.getType() );
      outgoingParam.setStringValue( incomingParam.getStringValue() );
      outgoingParams.add( outgoingParam );
    }
    return outgoingParams;
  }

  private static SimpleJobTrigger fromSchedulerToBindingRequestJobTrigger( ISimpleJobTrigger incomingJobTrigger ) {
    SimpleJobTrigger outgoingJobTrigger = null;
    if ( incomingJobTrigger != null ) {
      outgoingJobTrigger = new SimpleJobTrigger();
      outgoingJobTrigger.setRepeatCount( incomingJobTrigger.getRepeatCount() );
      outgoingJobTrigger.setRepeatInterval( incomingJobTrigger.getRepeatInterval() );
      outgoingJobTrigger.setUiPassParam( incomingJobTrigger.getUiPassParam() );
      outgoingJobTrigger.setStartTime( XmlGregorianCalendarConverter.asXMLGregorianCalendar( incomingJobTrigger.getStartTime() ) );
      outgoingJobTrigger.setEndTime( XmlGregorianCalendarConverter.asXMLGregorianCalendar( incomingJobTrigger.getEndTime() ) );
      outgoingJobTrigger.setStartHour( incomingJobTrigger.getStartHour() );
      outgoingJobTrigger.setStartMin( incomingJobTrigger.getStartMin() );
      outgoingJobTrigger.setStartYear( incomingJobTrigger.getStartYear() );
      outgoingJobTrigger.setStartMonth( incomingJobTrigger.getStartMonth() );
      outgoingJobTrigger.setStartDay( incomingJobTrigger.getStartDay() );
      outgoingJobTrigger.setStartAmPm( incomingJobTrigger.getStartAmPm() );
      outgoingJobTrigger.setTimeZone( incomingJobTrigger.getTimeZone() );
    }
    return outgoingJobTrigger;
  }

  private static CronJobTrigger fromSchedulerToBindingRequestCronJobTrigger( ICronJobTrigger incomingCronJobTrigger ) {
    CronJobTrigger outgoingCronJobTrigger = null;
    if ( incomingCronJobTrigger != null ) {
      outgoingCronJobTrigger = new CronJobTrigger();
      outgoingCronJobTrigger.setCronString( incomingCronJobTrigger.getCronString() );
      outgoingCronJobTrigger.setDuration( incomingCronJobTrigger.getDuration() );
      outgoingCronJobTrigger.setEndTime( XmlGregorianCalendarConverter.asXMLGregorianCalendar( incomingCronJobTrigger.getEndTime() ) );
      outgoingCronJobTrigger.setStartTime( XmlGregorianCalendarConverter.asXMLGregorianCalendar( incomingCronJobTrigger.getStartTime() ) );
      outgoingCronJobTrigger.setUiPassParam( incomingCronJobTrigger.getUiPassParam() );
      outgoingCronJobTrigger.setStartHour( incomingCronJobTrigger.getStartHour() );
      outgoingCronJobTrigger.setStartMin( incomingCronJobTrigger.getStartMin() );
      outgoingCronJobTrigger.setStartYear( incomingCronJobTrigger.getStartYear() );
      outgoingCronJobTrigger.setStartMonth( incomingCronJobTrigger.getStartMonth() );
      outgoingCronJobTrigger.setStartDay( incomingCronJobTrigger.getStartDay() );
      outgoingCronJobTrigger.setStartAmPm( incomingCronJobTrigger.getStartAmPm() );
      outgoingCronJobTrigger.setTimeZone( incomingCronJobTrigger.getTimeZone() );
    }
    return outgoingCronJobTrigger;
  }

  public static ArrayList<IJobScheduleRequest> fromBindingToSchedulerRequest(
    List<JobScheduleRequest> bindingRequests ) {
    IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$
    assert scheduler != null;
    ArrayList<IJobScheduleRequest> schedules = new ArrayList<>();
    for ( JobScheduleRequest bindingRequest : bindingRequests ) {
      IJobScheduleRequest scheduleRequest = scheduler.createJobScheduleRequest();
      scheduleRequest.setJobName( bindingRequest.getJobName() );
      scheduleRequest.setDuration( bindingRequest.getDuration() );
      scheduleRequest.setJobState( bindingRequest.getJobState() );
      scheduleRequest.setInputFile( bindingRequest.getInputFile() );
      scheduleRequest.setOutputFile( bindingRequest.getOutputFile() );
      scheduleRequest.setPdiParameters( bindingRequest.getPdiParameters() );
      scheduleRequest.setActionClass( bindingRequest.getActionClass() );
      scheduleRequest.setTimeZone( bindingRequest.getTimeZone() );
      scheduleRequest.setJobParameters(
        fromBindingToSchedulerRequestJobParameters( bindingRequest.getJobParameters() ) );
      scheduleRequest.setSimpleJobTrigger(
        fromBindingToSchedulerRequestJobTrigger( bindingRequest.getSimpleJobTrigger() ) );
      scheduleRequest.setCronJobTrigger(
        fromBindingToSchedulerRequestCronJobTrigger( bindingRequest.getCronJobTrigger() ) );
      schedules.add( scheduleRequest );
    }
    return schedules;
  }

  private static List<IJobScheduleParam> fromBindingToSchedulerRequestJobParameters(
    List<JobScheduleParam> incomingParams ) {
    IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$
    List<IJobScheduleParam> outgoingParams = new ArrayList<>();
    for ( JobScheduleParam incomingParam : incomingParams ) {
      IJobScheduleParam outgoingParam = scheduler.createJobScheduleParam();
      outgoingParam.setName( incomingParam.getName() );
      outgoingParam.setType( incomingParam.getType() );
      outgoingParam.setStringValue( incomingParam.getStringValue() );
      outgoingParams.add( outgoingParam );
    }
    return outgoingParams;
  }

  private static ISimpleJobTrigger fromBindingToSchedulerRequestJobTrigger(
    SimpleJobTrigger incomingSimpleJobTrigger ) {
    IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$
    ISimpleJobTrigger outgoingJobTrigger = null;
    if ( incomingSimpleJobTrigger != null ) {
      outgoingJobTrigger = scheduler.createSimpleJobTrigger(
              new Date(),
              null,
              incomingSimpleJobTrigger.getRepeatCount(),
              incomingSimpleJobTrigger.getRepeatInterval() );
      outgoingJobTrigger.setUiPassParam( incomingSimpleJobTrigger.getUiPassParam() );
      outgoingJobTrigger.setStartTime( XmlGregorianCalendarConverter.asDate( incomingSimpleJobTrigger.getStartTime() ) );
      outgoingJobTrigger.setEndTime( XmlGregorianCalendarConverter.asDate( incomingSimpleJobTrigger.getEndTime() ) );
      outgoingJobTrigger.setUiPassParam( incomingSimpleJobTrigger.getUiPassParam() );
      outgoingJobTrigger.setStartHour( incomingSimpleJobTrigger.getStartHour() );
      outgoingJobTrigger.setStartMin( incomingSimpleJobTrigger.getStartMin() );
      outgoingJobTrigger.setStartYear( incomingSimpleJobTrigger.getStartYear() );
      outgoingJobTrigger.setStartMonth( incomingSimpleJobTrigger.getStartMonth() );
      outgoingJobTrigger.setStartDay( incomingSimpleJobTrigger.getStartDay() );
      outgoingJobTrigger.setStartAmPm( incomingSimpleJobTrigger.getStartAmPm() );
      outgoingJobTrigger.setTimeZone( incomingSimpleJobTrigger.getTimeZone() );
    }
    return outgoingJobTrigger;
  }

  private static ICronJobTrigger fromBindingToSchedulerRequestCronJobTrigger( CronJobTrigger incomingCronJobTrigger ) {
    IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$
    ICronJobTrigger outgoingCronJobTrigger = null;
    if( incomingCronJobTrigger != null ) {
      outgoingCronJobTrigger = scheduler.createCronJobTrigger();
      outgoingCronJobTrigger.setCronString( incomingCronJobTrigger.getCronString() );
      outgoingCronJobTrigger.setDuration( incomingCronJobTrigger.getDuration() );
      outgoingCronJobTrigger.setEndTime( XmlGregorianCalendarConverter.asDate( incomingCronJobTrigger.getEndTime() ) );
      outgoingCronJobTrigger.setStartTime( XmlGregorianCalendarConverter.asDate( incomingCronJobTrigger.getStartTime() ) );
      outgoingCronJobTrigger.setUiPassParam( incomingCronJobTrigger.getUiPassParam() );
      outgoingCronJobTrigger.setStartHour( incomingCronJobTrigger.getStartHour() );
      outgoingCronJobTrigger.setStartMin( incomingCronJobTrigger.getStartMin() );
      outgoingCronJobTrigger.setStartYear( incomingCronJobTrigger.getStartYear() );
      outgoingCronJobTrigger.setStartMonth( incomingCronJobTrigger.getStartMonth() );
      outgoingCronJobTrigger.setStartDay( incomingCronJobTrigger.getStartDay() );
      outgoingCronJobTrigger.setStartAmPm( incomingCronJobTrigger.getStartAmPm() );
      outgoingCronJobTrigger.setTimeZone( incomingCronJobTrigger.getTimeZone() );
    }
    return outgoingCronJobTrigger;
  }
}
