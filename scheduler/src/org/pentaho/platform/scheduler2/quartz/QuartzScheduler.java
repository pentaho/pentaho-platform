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

package org.pentaho.platform.scheduler2.quartz;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IJobResult;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduleSubject;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.ISchedulerListener;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.Job.JobState;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.api.scheduler2.recur.ITimeRecurrence;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.scheduler2.messsages.Messages;
import org.pentaho.platform.scheduler2.recur.IncrementalRecurrence;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfMonth;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek.DayOfWeek;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek.DayOfWeekQualifier;
import org.pentaho.platform.scheduler2.recur.RecurrenceList;
import org.pentaho.platform.scheduler2.recur.SequentialRecurrence;
import org.quartz.Calendar;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import java.io.Serializable;
import java.security.Principal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A Quartz implementation of {@link IScheduler}
 * 
 * @author aphillips
 */
public class QuartzScheduler implements IScheduler {

  public static final String RESERVEDMAPKEY_ACTIONCLASS = "ActionAdapterQuartzJob-ActionClass"; //$NON-NLS-1$

  public static final String RESERVEDMAPKEY_ACTIONUSER = "ActionAdapterQuartzJob-ActionUser"; //$NON-NLS-1$

  public static final String RESERVEDMAPKEY_ACTIONID = "ActionAdapterQuartzJob-ActionId"; //$NON-NLS-1$

  public static final String RESERVEDMAPKEY_STREAMPROVIDER = "ActionAdapterQuartzJob-StreamProvider"; //$NON-NLS-1$

  public static final String RESERVEDMAPKEY_UIPASSPARAM = "uiPassParam";

  public static final String RESERVEDMAPKEY_LINEAGE_ID = "lineage-id";

  public static final String RESERVEDMAPKEY_RESTART_FLAG = "ActionAdapterQuartzJob-Restart";

  private static final Log logger = LogFactory.getLog( QuartzScheduler.class );

  private SchedulerFactory quartzSchedulerFactory;

  private Scheduler quartzScheduler;

  private ArrayList<ISchedulerListener> listeners = new ArrayList<ISchedulerListener>();

  private static final Pattern listPattern = Pattern.compile( "\\d+" ); //$NON-NLS-1$

  private static final Pattern dayOfWeekRangePattern = Pattern.compile( ".*\\-.*" ); //$NON-NLS-1$

  private static final Pattern sequencePattern = Pattern.compile( "\\d+\\-\\d+" ); //$NON-NLS-1$

  private static final Pattern intervalPattern = Pattern.compile( "\\d+/\\d+" ); //$NON-NLS-1$

  private static final Pattern qualifiedDayPattern = Pattern.compile( "\\d+#\\d+" ); //$NON-NLS-1$

  private static final Pattern lastDayPattern = Pattern.compile( "\\d+L" ); //$NON-NLS-1$

  public QuartzScheduler( SchedulerFactory schedulerFactory ) {
    this.quartzSchedulerFactory = schedulerFactory;
  }

  public QuartzScheduler() {
    this.quartzSchedulerFactory = new StdSchedulerFactory();
  }

  /**
   * Overrides the default Quartz {@link SchedulerFactory}. Note: depending on the type of scheduler you are setting
   * here, there may be initializing required prior to this setter being called. Only the
   * {@link SchedulerFactory#getScheduler()} will be called later, so the factory set here must already be in a state
   * where that invocation will be successful.
   * 
   * @param quartzSchedulerFactory
   *          the quartz factory to use for generating scheduler instances
   */
  public void setQuartzSchedulerFactory( SchedulerFactory quartzSchedulerFactory ) throws SchedulerException {
    this.quartzSchedulerFactory = quartzSchedulerFactory;
    if( quartzScheduler != null ){
      this.shutdown();
      quartzScheduler = null;
    }
  }

  public Scheduler getQuartzScheduler() throws org.quartz.SchedulerException {
    if ( quartzScheduler == null ) {
      /*
       * Currently, quartz will always give you the same scheduler object when any factory instance is asked for a
       * scheduler. In other words there is no such thing as scheduler-level isolation. If we really need multiple
       * isolated scheduler instances, we should investigate named schedulers, but this API getScheduler() will not help
       * us in that regard.
       */
      quartzScheduler = quartzSchedulerFactory.getScheduler();
    }

    logger.debug( "Using quartz scheduler " + quartzScheduler ); //$NON-NLS-1$
    return quartzScheduler;
  }
  
  private void setQuartzScheduler( Scheduler quartzScheduler ) {
    this.quartzScheduler = quartzScheduler;
  }

  /** {@inheritDoc} */
  public Job createJob( String jobName, String actionId, Map<String, Serializable> jobParams, IJobTrigger trigger )
    throws SchedulerException {
    return createJob( jobName, actionId, jobParams, trigger, null );
  }

  /** {@inheritDoc} */
  public Job createJob( String jobName, Class<? extends IAction> action, Map<String, Serializable> jobParams,
      IJobTrigger trigger ) throws SchedulerException {
    return createJob( jobName, action, jobParams, trigger, null );
  }

  /** {@inheritDoc} */
  public Job createJob( String jobName, Class<? extends IAction> action, Map<String, Serializable> jobParams,
      IJobTrigger trigger, IBackgroundExecutionStreamProvider outputStreamProvider ) throws SchedulerException {

    if ( action == null ) {
      throw new SchedulerException( Messages.getInstance().getString( "QuartzScheduler.ERROR_0003_ACTION_IS_NULL" ) ); //$NON-NLS-1$
    }

    if ( jobParams == null ) {
      jobParams = new HashMap<String, Serializable>();
    }

    jobParams.put( RESERVEDMAPKEY_ACTIONCLASS, action.getName() );
    Job ret = createJob( jobName, jobParams, trigger, outputStreamProvider );
    ret.setSchedulableClass( action.getName() );
    return ret;
  }

  /** {@inheritDoc} */
  public Job createJob( String jobName, String actionId, Map<String, Serializable> jobParams, IJobTrigger trigger,
      IBackgroundExecutionStreamProvider outputStreamProvider ) throws SchedulerException {
    if ( StringUtils.isEmpty( actionId ) ) {
      throw new SchedulerException( Messages.getInstance().getString( "QuartzScheduler.ERROR_0003_ACTION_IS_NULL" ) ); //$NON-NLS-1$
    }

    if ( jobParams == null ) {
      jobParams = new HashMap<String, Serializable>();
    }

    jobParams.put( RESERVEDMAPKEY_ACTIONID, actionId );
    Job ret = createJob( jobName, jobParams, trigger, outputStreamProvider );
    ret.setSchedulableClass( "" ); //$NON-NLS-1$
    return ret;
  }

  public static Trigger createQuartzTrigger( IJobTrigger jobTrigger, QuartzJobKey jobId ) throws SchedulerException {
    Trigger quartzTrigger = null;
    if ( jobTrigger instanceof ComplexJobTrigger ) {
      try {
        quartzTrigger =
            new CronTrigger( jobId.toString(), jobId.getUserName(), jobTrigger.getCronString() != null ? jobTrigger
                .getCronString() : QuartzCronStringFactory.createCronString( (ComplexJobTrigger) jobTrigger ) );
      } catch ( ParseException e ) {
        throw new SchedulerException( Messages.getInstance().getString(
            "QuartzScheduler.ERROR_0001_FAILED_TO_SCHEDULE_JOB", jobId.getJobName() ), e ); //$NON-NLS-1$
      }
    } else if ( jobTrigger instanceof SimpleJobTrigger ) {
      SimpleJobTrigger simpleTrigger = (SimpleJobTrigger) jobTrigger;
      long interval = simpleTrigger.getRepeatInterval();
      if ( interval > 0 ) {
        interval *= 1000;
      }
      int repeatCount =
          simpleTrigger.getRepeatCount() < 0 ? SimpleTrigger.REPEAT_INDEFINITELY : simpleTrigger.getRepeatCount();
      quartzTrigger =
          new SimpleTrigger( jobId.toString(), jobId.getUserName(), simpleTrigger.getStartTime(), simpleTrigger
              .getEndTime(), repeatCount, interval );
    } else {
      throw new SchedulerException( Messages.getInstance().getString( "QuartzScheduler.ERROR_0002_TRIGGER_WRONG_TYPE" ) ); //$NON-NLS-1$
    }
    if ( quartzTrigger instanceof SimpleTrigger ) {
      quartzTrigger.setMisfireInstruction( SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT );
    } else {
      quartzTrigger.setMisfireInstruction( SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW );
    }
    return quartzTrigger;
  }

  private JobDetail createJobDetails( QuartzJobKey jobId, Map<String, Serializable> jobParams ) {
    JobDetail jobDetail = new JobDetail( jobId.toString(), jobId.getUserName(), BlockingQuartzJob.class );
    jobParams.put( RESERVEDMAPKEY_ACTIONUSER, jobId.getUserName() );
    JobDataMap jobDataMap = new JobDataMap( jobParams );
    jobDetail.setJobDataMap( jobDataMap );
    return jobDetail;
  }

  private Calendar createQuartzCalendar( ComplexJobTrigger complexJobTrigger ) {
    Calendar triggerCalendar = null;
    if ( ( complexJobTrigger.getStartTime() != null ) || ( complexJobTrigger.getEndTime() != null ) ) {
      triggerCalendar =
          new QuartzSchedulerAvailability( complexJobTrigger.getStartTime(), complexJobTrigger.getEndTime() );
    }
    return triggerCalendar;
  }

  /** {@inheritDoc} */
  protected Job createJob( String jobName, Map<String, Serializable> jobParams, IJobTrigger trigger,
      IBackgroundExecutionStreamProvider outputStreamProvider ) throws SchedulerException {

    String curUser = getCurrentUser();

    // determine if the job params tell us who owns the job
    Serializable jobOwner = jobParams.get( RESERVEDMAPKEY_ACTIONUSER );
    if ( jobOwner != null && jobOwner.toString().length() > 0 ) {
      curUser = jobOwner.toString();
    }

    QuartzJobKey jobId = new QuartzJobKey( jobName, curUser );

    Trigger quartzTrigger = createQuartzTrigger( trigger, jobId );
    
    if( trigger.getEndTime() != null ){
      quartzTrigger.setEndTime( trigger.getEndTime() );
    }

    Calendar triggerCalendar =
        quartzTrigger instanceof CronTrigger ? createQuartzCalendar( (ComplexJobTrigger) trigger ) : null;

    if ( outputStreamProvider != null ) {
      jobParams.put( RESERVEDMAPKEY_STREAMPROVIDER, outputStreamProvider );
    }

    if ( trigger.getUiPassParam() != null ) {
      jobParams.put( RESERVEDMAPKEY_UIPASSPARAM, trigger.getUiPassParam() );
    }

    if ( !jobParams.containsKey( RESERVEDMAPKEY_LINEAGE_ID ) ) {
      String uuid = UUID.randomUUID().toString();
      jobParams.put( RESERVEDMAPKEY_LINEAGE_ID, uuid );
    }

    JobDetail jobDetail = createJobDetails( jobId, jobParams );

    try {
      Scheduler scheduler = getQuartzScheduler();
      if ( triggerCalendar != null ) {
        scheduler.addCalendar( jobId.toString(), triggerCalendar, false, false );
        quartzTrigger.setCalendarName( jobId.toString() );
      }
      logger
          .debug( MessageFormat
              .format(
                  "Scheduling job {0} with trigger {1} and job parameters [ {2} ]", jobId.toString(), trigger, prettyPrintMap( jobParams ) ) ); //$NON-NLS-1$
      scheduler.scheduleJob( jobDetail, quartzTrigger );
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException( Messages.getInstance().getString(
          "QuartzScheduler.ERROR_0001_FAILED_TO_SCHEDULE_JOB", jobName ), e ); //$NON-NLS-1$
    }

    Job job = new Job();
    job.setJobParams( jobParams );
    job.setJobTrigger( (JobTrigger) trigger );
    job.setNextRun( quartzTrigger.getNextFireTime() );
    job.setLastRun( quartzTrigger.getPreviousFireTime() );
    job.setJobId( jobId.toString() );
    job.setJobName( jobName );
    job.setUserName( curUser );
    job.setState( JobState.NORMAL );

    return job;
  }

  @Override
  public void updateJob( String jobId, Map<String, Serializable> jobParams, IJobTrigger trigger )
    throws SchedulerException {
    QuartzJobKey jobKey = QuartzJobKey.parse( jobId );

    Trigger quartzTrigger = createQuartzTrigger( trigger, jobKey );
    quartzTrigger.setJobName( jobId );
    quartzTrigger.setJobGroup( jobKey.getUserName() );

    Calendar triggerCalendar =
        quartzTrigger instanceof CronTrigger ? createQuartzCalendar( (ComplexJobTrigger) trigger ) : null;

    try {
      Scheduler scheduler = getQuartzScheduler();
      // int triggerState = scheduler.getTriggerState(jobId, jobKey.getUserName());
      // if (triggerState != Trigger.STATE_PAUSED) {
      // scheduler.pauseTrigger(jobId, jobKey.getUserName());
      // }
      JobDetail origJobDetail = scheduler.getJobDetail( jobId, jobKey.getUserName() );
      if ( origJobDetail.getJobDataMap().containsKey( RESERVEDMAPKEY_ACTIONCLASS ) ) {
        jobParams.put( RESERVEDMAPKEY_ACTIONCLASS, origJobDetail.getJobDataMap().get( RESERVEDMAPKEY_ACTIONCLASS )
            .toString() );
      } else if ( origJobDetail.getJobDataMap().containsKey( RESERVEDMAPKEY_ACTIONID ) ) {
        jobParams
            .put( RESERVEDMAPKEY_ACTIONID, origJobDetail.getJobDataMap().get( RESERVEDMAPKEY_ACTIONID ).toString() );
      }

      if ( origJobDetail.getJobDataMap().containsKey( RESERVEDMAPKEY_STREAMPROVIDER ) ) {
        jobParams.put( RESERVEDMAPKEY_STREAMPROVIDER, (Serializable) origJobDetail.getJobDataMap().get(
            RESERVEDMAPKEY_STREAMPROVIDER ) );
      }
      if ( origJobDetail.getJobDataMap().containsKey( RESERVEDMAPKEY_UIPASSPARAM ) ) {
        jobParams.put( RESERVEDMAPKEY_UIPASSPARAM, (Serializable) origJobDetail.getJobDataMap().get(
            RESERVEDMAPKEY_UIPASSPARAM ) );
      }

      JobDetail jobDetail = createJobDetails( jobKey, jobParams );
      scheduler.addJob( jobDetail, true );
      if ( triggerCalendar != null ) {
        scheduler.addCalendar( jobId.toString(), triggerCalendar, true, true );
        quartzTrigger.setCalendarName( jobId.toString() );
      }
      scheduler.rescheduleJob( jobId, jobKey.getUserName(), quartzTrigger );
      // if (triggerState != Trigger.STATE_PAUSED) {
      // scheduler.resumeTrigger(jobId, jobKey.getUserName());
      // }
      logger
          .debug( MessageFormat
              .format(
                  "Scheduling job {0} with trigger {1} and job parameters [ {2} ]", jobId.toString(), trigger, prettyPrintMap( jobParams ) ) ); //$NON-NLS-1$
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException( Messages.getInstance().getString(
          "QuartzScheduler.ERROR_0001_FAILED_TO_SCHEDULE_JOB", jobKey.getJobName() ), e ); //$NON-NLS-1$
    }
  }

  /** {@inheritDoc} */
  public Map<IScheduleSubject, ComplexJobTrigger> getAvailabilityWindows() {
    // TODO Auto-generated method stub
    return null;
  }

  /** {@inheritDoc} */
  public List<IJobResult> getJobHistory( String jobId ) {
    // TODO Auto-generated method stub
    return null;
  }

  /** {@inheritDoc} */
  public void triggerNow( String jobId ) throws SchedulerException {
    try {
      QuartzJobKey jobKey = QuartzJobKey.parse( jobId );
      Scheduler scheduler = getQuartzScheduler();
      String groupName = jobKey.getUserName();
      for ( Trigger trigger : scheduler.getTriggersOfJob( jobId, groupName ) ) {
        if ( "MANUAL_TRIGGER".equals( trigger.getGroup() ) ) {
          continue;
        }
        if ( trigger instanceof SimpleTrigger ) {
          ( (SimpleTrigger) trigger ).setPreviousFireTime( new Date() );
        } else if ( trigger instanceof CronTrigger ) {
          ( (CronTrigger) trigger ).setPreviousFireTime( new Date() );
        }
        // force the trigger to be updated with the previous fire time
        scheduler.rescheduleJob( jobId, jobKey.getUserName(), trigger );
      }

      scheduler.triggerJob( jobId, jobKey.getUserName() );
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException( Messages.getInstance().getString(
          "QuartzScheduler.ERROR_0007_FAILED_TO_GET_JOB", jobId ), e ); //$NON-NLS-1$
    }
  }

  /** {@inheritDoc} */
  @SuppressWarnings( "unchecked" )
  public Job getJob( String jobId ) throws SchedulerException {
    try {
      Scheduler scheduler = getQuartzScheduler();
      QuartzJobKey jobKey = QuartzJobKey.parse( jobId );
      String groupName = jobKey.getUserName();
      for ( Trigger trigger : scheduler.getTriggersOfJob( jobId, groupName ) ) {
        Job job = new Job();
        JobDetail jobDetail = scheduler.getJobDetail( jobId, groupName );
        if ( jobDetail != null ) {
          JobDataMap jobDataMap = jobDetail.getJobDataMap();
          if ( jobDataMap != null ) {
            Map<String, Serializable> wrappedMap = jobDataMap.getWrappedMap();
            job.setJobParams( wrappedMap );
          }
        }

        job.setJobId( jobId );
        setJobTrigger( scheduler, job, trigger );
        job.setUserName( jobDetail.getGroup() );
        return job;
      }
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException( Messages.getInstance().getString(
          "QuartzScheduler.ERROR_0007_FAILED_TO_GET_JOB", jobId ), e ); //$NON-NLS-1$
    }
    return null;
  }

  /** {@inheritDoc} */
  @SuppressWarnings( "unchecked" )
  public List<Job> getJobs( IJobFilter filter ) throws SchedulerException {
    ArrayList<Job> jobs = new ArrayList<Job>();
    try {
      Scheduler scheduler = getQuartzScheduler();
      for ( String groupName : scheduler.getJobGroupNames() ) {
        for ( String jobId : scheduler.getJobNames( groupName ) ) {
          for ( Trigger trigger : scheduler.getTriggersOfJob( jobId, groupName ) ) {
            if ( "MANUAL_TRIGGER".equals( trigger.getGroup() ) ) {
              continue;
            }
            Job job = new Job();
            job.setGroupName( groupName );
            JobDetail jobDetail = scheduler.getJobDetail( jobId, groupName );
            if ( jobDetail != null ) {
              job.setUserName( jobDetail.getGroup() );
              JobDataMap jobDataMap = jobDetail.getJobDataMap();
              if ( jobDataMap != null ) {
                Map<String, Serializable> wrappedMap = jobDataMap.getWrappedMap();
                job.setJobParams( wrappedMap );
              }
            }

            job.setJobId( jobId );
            setJobTrigger( scheduler, job, trigger );
            job.setJobName( QuartzJobKey.parse( jobId ).getJobName() );
            job.setNextRun( trigger.getNextFireTime() );
            job.setLastRun( trigger.getPreviousFireTime() );
            if ( ( filter == null ) || filter.accept( job ) ) {
              jobs.add( job );
            }
          }
        }
      }
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException(
          Messages.getInstance().getString( "QuartzScheduler.ERROR_0004_FAILED_TO_LIST_JOBS" ), e ); //$NON-NLS-1$
    }
    return jobs;
  }

  private void setJobTrigger( Scheduler scheduler, Job job, Trigger trigger ) throws SchedulerException,
    org.quartz.SchedulerException {
    QuartzJobKey jobKey = QuartzJobKey.parse( job.getJobId() );
    String groupName = jobKey.getUserName();

    if ( trigger instanceof SimpleTrigger ) {
      SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
      SimpleJobTrigger simpleJobTrigger = new SimpleJobTrigger();
      simpleJobTrigger.setStartTime( simpleTrigger.getStartTime() );
      simpleJobTrigger.setEndTime( simpleTrigger.getEndTime() );
      simpleJobTrigger.setUiPassParam( (String) job.getJobParams().get( RESERVEDMAPKEY_UIPASSPARAM ) );
      long interval = simpleTrigger.getRepeatInterval();
      if ( interval > 0 ) {
        interval /= 1000;
      }
      simpleJobTrigger.setRepeatInterval( interval );
      simpleJobTrigger.setRepeatCount( simpleTrigger.getRepeatCount() );
      job.setJobTrigger( simpleJobTrigger );
    } else if ( trigger instanceof CronTrigger ) {
      CronTrigger cronTrigger = (CronTrigger) trigger;
      ComplexJobTrigger complexJobTrigger = createComplexTrigger( cronTrigger.getCronExpression() );
      complexJobTrigger.setUiPassParam( (String) job.getJobParams().get( RESERVEDMAPKEY_UIPASSPARAM ) );
      complexJobTrigger.setCronString( ( (CronTrigger) trigger ).getCronExpression() );
      job.setJobTrigger( complexJobTrigger );
      if ( trigger.getCalendarName() != null ) {
        Calendar calendar = scheduler.getCalendar( trigger.getCalendarName() );
        if ( calendar instanceof QuartzSchedulerAvailability ) {
          QuartzSchedulerAvailability quartzSchedulerAvailability = (QuartzSchedulerAvailability) calendar;
          complexJobTrigger.setStartTime( quartzSchedulerAvailability.getStartTime() );
          complexJobTrigger.setEndTime( quartzSchedulerAvailability.getEndTime() );
        }
      }
      complexJobTrigger.setCronString( ( (CronTrigger) trigger ).getCronExpression() );
    }

    int triggerState = scheduler.getTriggerState( job.getJobId(), groupName );
    switch ( triggerState ) {
      case Trigger.STATE_NORMAL:
        job.setState( JobState.NORMAL );
        break;
      case Trigger.STATE_BLOCKED:
        job.setState( JobState.BLOCKED );
        break;
      case Trigger.STATE_COMPLETE:
        job.setState( JobState.COMPLETE );
        break;
      case Trigger.STATE_ERROR:
        job.setState( JobState.ERROR );
        break;
      case Trigger.STATE_PAUSED:
        job.setState( JobState.PAUSED );
        break;
      default:
        job.setState( JobState.UNKNOWN );
        break;
    }

    job.setJobName( QuartzJobKey.parse( job.getJobId() ).getJobName() );
    job.setNextRun( trigger.getNextFireTime() );
    job.setLastRun( trigger.getPreviousFireTime() );

  }

  /** {@inheritDoc} */
  public Integer getMinScheduleInterval( IScheduleSubject subject ) {
    // TODO Auto-generated method stub
    return 0;
  }

  /** {@inheritDoc} */
  public ComplexJobTrigger getSubjectAvailabilityWindow( IScheduleSubject subject ) {
    // TODO Auto-generated method stub
    return null;
  }

  /** {@inheritDoc} */
  public void pause() throws SchedulerException {
    try {
      getQuartzScheduler().standby();
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException( e );
    }
  }

  /** {@inheritDoc} */
  public void pauseJob( String jobId ) throws SchedulerException {
    try {
      Scheduler scheduler = getQuartzScheduler();
      scheduler.pauseJob( jobId, QuartzJobKey.parse( jobId ).getUserName() );
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException( Messages.getInstance()
          .getString( "QuartzScheduler.ERROR_0005_FAILED_TO_PAUSE_JOBS" ), e ); //$NON-NLS-1$
    }
  }

  /** {@inheritDoc} */
  public void removeJob( String jobId ) throws SchedulerException {
    try {
      Scheduler scheduler = getQuartzScheduler();
      scheduler.deleteJob( jobId, QuartzJobKey.parse( jobId ).getUserName() );
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException( Messages.getInstance()
          .getString( "QuartzScheduler.ERROR_0005_FAILED_TO_PAUSE_JOBS" ), e ); //$NON-NLS-1$
    }
  }

  /** {@inheritDoc} */
  public void start() throws SchedulerException {
    try {
      getQuartzScheduler().start();
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException( e );
    }
  }

  /** {@inheritDoc} */
  public void resumeJob( String jobId ) throws SchedulerException {
    try {
      Scheduler scheduler = getQuartzScheduler();
      scheduler.resumeJob( jobId, QuartzJobKey.parse( jobId ).getUserName() );
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException( Messages.getInstance().getString(
          "QuartzScheduler.ERROR_0005_FAILED_TO_RESUME_JOBS" ), e ); //$NON-NLS-1$
    }
  }

  /** {@inheritDoc} */
  public void setAvailabilityWindows( Map<IScheduleSubject, ComplexJobTrigger> availability ) {
    // TODO Auto-generated method stub

  }

  /** {@inheritDoc} */
  public void setMinScheduleInterval( IScheduleSubject subject, int intervalInSeconds ) {
    // TODO Auto-generated method stub

  }

  /** {@inheritDoc} */
  public void setSubjectAvailabilityWindow( IScheduleSubject subject, ComplexJobTrigger availability ) {
    // TODO Auto-generated method stub

  }

  /**
   * @return
   */
  protected String getCurrentUser() {
    IPentahoSession session = PentahoSessionHolder.getSession();
    if ( session == null ) {
      return null;
    }
    Principal p = SecurityHelper.getInstance().getAuthentication();
    return ( p == null ) ? null : p.getName();
  }

  public static ComplexJobTrigger createComplexTrigger( String cronExpression ) {
    ComplexJobTrigger complexJobTrigger = new ComplexJobTrigger();
    complexJobTrigger.setHourlyRecurrence( (ITimeRecurrence) null );
    complexJobTrigger.setMinuteRecurrence( (ITimeRecurrence) null );
    complexJobTrigger.setSecondRecurrence( (ITimeRecurrence) null );

    for ( ITimeRecurrence recurrence : parseRecurrence( cronExpression, 6 ) ) {
      complexJobTrigger.addYearlyRecurrence( recurrence );
    }
    for ( ITimeRecurrence recurrence : parseRecurrence( cronExpression, 4 ) ) {
      complexJobTrigger.addMonthlyRecurrence( recurrence );
    }
    List<ITimeRecurrence> dayOfWeekRecurrences = parseDayOfWeekRecurrences( cronExpression );
    List<ITimeRecurrence> dayOfMonthRecurrences = parseRecurrence( cronExpression, 3 );
    if ( ( dayOfWeekRecurrences.size() > 0 ) && ( dayOfMonthRecurrences.size() == 0 ) ) {
      for ( ITimeRecurrence recurrence : dayOfWeekRecurrences ) {
        complexJobTrigger.addDayOfWeekRecurrence( recurrence );
      }
    } else if ( ( dayOfWeekRecurrences.size() == 0 ) && ( dayOfMonthRecurrences.size() > 0 ) ) {
      for ( ITimeRecurrence recurrence : dayOfMonthRecurrences ) {
        complexJobTrigger.addDayOfMonthRecurrence( recurrence );
      }
    }
    for ( ITimeRecurrence recurrence : parseRecurrence( cronExpression, 2 ) ) {
      complexJobTrigger.addHourlyRecurrence( recurrence );
    }
    for ( ITimeRecurrence recurrence : parseRecurrence( cronExpression, 1 ) ) {
      complexJobTrigger.addMinuteRecurrence( recurrence );
    }
    for ( ITimeRecurrence recurrence : parseRecurrence( cronExpression, 0 ) ) {
      complexJobTrigger.addSecondRecurrence( recurrence );
    }
    return complexJobTrigger;
  }

  private static List<ITimeRecurrence> parseDayOfWeekRecurrences( String cronExpression ) {
    List<ITimeRecurrence> dayOfWeekRecurrence = new ArrayList<ITimeRecurrence>();
    String delims = "[ ]+"; //$NON-NLS-1$
    String[] tokens = cronExpression.split( delims );
    if ( tokens.length >= 6 ) {
      String dayOfWeekTokens = tokens[5];
      tokens = dayOfWeekTokens.split( "," ); //$NON-NLS-1$
      if ( ( tokens.length > 1 ) || !( tokens[0].equals( "*" ) || tokens[0].equals( "?" ) ) ) { //$NON-NLS-1$ //$NON-NLS-2$
        RecurrenceList dayOfWeekList = null;
        for ( String token : tokens ) {
          if ( listPattern.matcher( token ).matches() ) {
            if ( dayOfWeekList == null ) {
              dayOfWeekList = new RecurrenceList();
            }
            dayOfWeekList.getValues().add( Integer.parseInt( token ) );
          } else {
            if ( dayOfWeekList != null ) {
              dayOfWeekRecurrence.add( dayOfWeekList );
              dayOfWeekList = null;
            }
            if ( sequencePattern.matcher( token ).matches() ) {
              String[] days = token.split( "-" ); //$NON-NLS-1$
              dayOfWeekRecurrence.add( new SequentialRecurrence( Integer.parseInt( days[0] ), Integer
                  .parseInt( days[1] ) ) );
            } else if ( intervalPattern.matcher( token ).matches() ) {
              String[] days = token.split( "/" ); //$NON-NLS-1$
              dayOfWeekRecurrence.add( new IncrementalRecurrence( Integer.parseInt( days[0] ), Integer
                  .parseInt( days[1] ) ) );
            } else if ( qualifiedDayPattern.matcher( token ).matches() ) {
              String[] days = token.split( "#" ); //$NON-NLS-1$
              dayOfWeekRecurrence
                  .add( new QualifiedDayOfWeek( Integer.parseInt( days[1] ), Integer.parseInt( days[0] ) ) );
            } else if ( lastDayPattern.matcher( token ).matches() ) {
              DayOfWeek dayOfWeek =
                  DayOfWeek.values()[( Integer.parseInt( token.substring( 0, token.length() - 1 ) ) - 1 ) % 7];
              dayOfWeekRecurrence.add( new QualifiedDayOfWeek( DayOfWeekQualifier.LAST, dayOfWeek ) );
            } else if ( dayOfWeekRangePattern.matcher( token ).matches() ) {
              String[] days = token.split( "-" ); //$NON-NLS-1$
              int start = DayOfWeek.valueOf( days[0] ).ordinal();
              int finish = DayOfWeek.valueOf( days[1] ).ordinal();
              dayOfWeekRecurrence.add( new SequentialRecurrence( start, finish ) );
            } else {
              dayOfWeekList = new RecurrenceList();
              dayOfWeekList.getValues().add( DayOfWeek.valueOf( token ).ordinal() );
              dayOfWeekRecurrence.add( dayOfWeekList );
              dayOfWeekList = null;
              // } else {
              // throw new IllegalArgumentException(Messages.getInstance().getErrorString(
              //                  "ComplexJobTrigger.ERROR_0001_InvalidCronExpression")); //$NON-NLS-1$
            }
          }

        }
        if ( dayOfWeekList != null ) {
          dayOfWeekRecurrence.add( dayOfWeekList );
        }
      }
    } else {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "ComplexJobTrigger.ERROR_0001_InvalidCronExpression" ) ); //$NON-NLS-1$
    }
    return dayOfWeekRecurrence;
  }

  private static List<ITimeRecurrence> parseRecurrence( String cronExpression, int tokenIndex ) {
    List<ITimeRecurrence> timeRecurrence = new ArrayList<ITimeRecurrence>();
    String delims = "[ ]+"; //$NON-NLS-1$
    String[] tokens = cronExpression.split( delims );
    if ( tokens.length > tokenIndex ) {
      String timeTokens = tokens[tokenIndex];
      tokens = timeTokens.split( "," ); //$NON-NLS-1$
      if ( ( tokens.length > 1 ) || !( tokens[0].equals( "*" ) || tokens[0].equals( "?" ) ) ) { //$NON-NLS-1$ //$NON-NLS-2$
        RecurrenceList timeList = null;
        for ( String token : tokens ) {
          if ( listPattern.matcher( token ).matches() ) {
            if ( timeList == null ) {
              timeList = new RecurrenceList();
            }
            timeList.getValues().add( Integer.parseInt( token ) );
          } else {
            if ( timeList != null ) {
              timeRecurrence.add( timeList );
              timeList = null;
            }
            if ( sequencePattern.matcher( token ).matches() ) {
              String[] days = token.split( "-" ); //$NON-NLS-1$
              timeRecurrence.add( new SequentialRecurrence( Integer.parseInt( days[0] ),
                      Integer.parseInt( days[ 1 ] ) ) );
            } else if ( intervalPattern.matcher( token ).matches() ) {
              String[] days = token.split( "/" ); //$NON-NLS-1$
              timeRecurrence
                  .add( new IncrementalRecurrence( Integer.parseInt( days[ 0 ] ), Integer.parseInt( days[ 1 ] ) ) );
            } else if ( "L".equalsIgnoreCase( token ) ) {
              timeRecurrence.add( new QualifiedDayOfMonth() );
            } else {
              throw new IllegalArgumentException( Messages.getInstance().getErrorString(
                  "ComplexJobTrigger.ERROR_0001_InvalidCronExpression" ) ); //$NON-NLS-1$
            }
          }

        }
        if ( timeList != null ) {
          timeRecurrence.add( timeList );
        }
      }
    } else {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "ComplexJobTrigger.ERROR_0001_InvalidCronExpression" ) ); //$NON-NLS-1$
    }
    return timeRecurrence;
  }

  /** {@inheritDoc} */
  public SchedulerStatus getStatus() throws SchedulerException {
    SchedulerStatus schedulerStatus = SchedulerStatus.STOPPED;
    try {
      if ( getQuartzScheduler().isInStandbyMode() ) {
        schedulerStatus = SchedulerStatus.PAUSED;
      } else if ( getQuartzScheduler().isStarted() ) {
        schedulerStatus = SchedulerStatus.RUNNING;
      }
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException( Messages.getInstance().getString(
          "QuartzScheduler.ERROR_0006_FAILED_TO_GET_SCHEDULER_STATUS" ), e ); //$NON-NLS-1$
    }
    return schedulerStatus;
  }

  /** {@inheritDoc} */
  public void shutdown() throws SchedulerException {
    try {
      boolean waitForJobsToComplete = true;
      getQuartzScheduler().shutdown( waitForJobsToComplete );
      setQuartzScheduler(null);
    } catch ( org.quartz.SchedulerException e ) {
      throw new SchedulerException( e );
    }
  }

  public static String prettyPrintMap( Map<String, Serializable> map ) {
    StringBuilder b = new StringBuilder();
    for ( Map.Entry<String, Serializable> entry : map.entrySet() ) {
      b.append( entry.getKey() );
      b.append( "=" ); //$NON-NLS-1$
      b.append( entry.getValue() );
      b.append( "; " ); //$NON-NLS-1$
    }
    return b.toString();
  }

  public void addListener( ISchedulerListener listener ) {
    listeners.add( listener );
  }

  public void setListeners( Collection<ISchedulerListener> listeners ) {
    this.listeners.addAll( listeners );
  }

  public void fireJobCompleted( IAction actionBean, String actionUser, Map<String, Serializable> params,
      IBackgroundExecutionStreamProvider streamProvider ) {
    for ( ISchedulerListener listener : listeners ) {
      listener.jobCompleted( actionBean, actionUser, params, streamProvider );
    }
  }
}
