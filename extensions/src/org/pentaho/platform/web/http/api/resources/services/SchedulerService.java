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

package org.pentaho.platform.web.http.api.resources.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.Job.JobState;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.scheduler2.blockout.BlockoutAction;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.SchedulerAction;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.api.resources.ComplexJobTriggerProxy;
import org.pentaho.platform.web.http.api.resources.JobRequest;
import org.pentaho.platform.web.http.api.resources.JobScheduleParam;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;
import org.pentaho.platform.web.http.api.resources.SchedulerOutputPathResolver;
import org.pentaho.platform.web.http.api.resources.SchedulerResourceUtil;
import org.pentaho.platform.web.http.api.resources.SessionResource;
import org.pentaho.platform.web.http.api.resources.proxies.BlockStatusProxy;

public class SchedulerService {

  protected IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$

  protected IAuthorizationPolicy policy;

  protected IUnifiedRepository repository;

  protected SessionResource sessionResource;

  protected FileService fileService;

  protected IBlockoutManager blockoutManager;

  private static final Log logger = LogFactory.getLog( FileService.class );

  public Job createJob( JobScheduleRequest scheduleRequest )
    throws IOException, SchedulerException, IllegalAccessException {

    // Used to determine if created by a RunInBackgroundCommand
    boolean runInBackground =
        scheduleRequest.getSimpleJobTrigger() == null && scheduleRequest.getComplexJobTrigger() == null
            && scheduleRequest.getCronJobTrigger() == null;

    if ( !runInBackground && !getPolicy().isAllowed( SchedulerAction.NAME ) ) {
      throw new SecurityException();
    }

    boolean hasInputFile = !StringUtils.isEmpty( scheduleRequest.getInputFile() );
    RepositoryFile file = null;
    if ( hasInputFile ) {
      try {
        file = getRepository().getFile( scheduleRequest.getInputFile() );
      } catch ( UnifiedRepositoryException ure ) {
        hasInputFile = false;
        logger.warn( ure.getMessage(), ure );
      }
    }

    // if we have an inputfile, generate job name based on that if the name is not passed in
    if ( hasInputFile && StringUtils.isEmpty( scheduleRequest.getJobName() ) ) {
      scheduleRequest.setJobName( file.getName().substring( 0, file.getName().lastIndexOf( "." ) ) ); //$NON-NLS-1$
    } else if ( !StringUtils.isEmpty( scheduleRequest.getActionClass() ) ) {
      String actionClass =
          scheduleRequest.getActionClass().substring( scheduleRequest.getActionClass().lastIndexOf( "." ) + 1 );
      scheduleRequest.setJobName( actionClass ); //$NON-NLS-1$
    } else if ( !hasInputFile && StringUtils.isEmpty( scheduleRequest.getJobName() ) ) {
      // just make up a name
      scheduleRequest.setJobName( "" + System.currentTimeMillis() ); //$NON-NLS-1$
    }

    if ( hasInputFile ) {
      Map<String, Serializable> metadata = getRepository().getFileMetadata( file.getId() );
      if ( metadata.containsKey( "_PERM_SCHEDULABLE" ) ) {
        boolean schedulable = Boolean.parseBoolean( (String) metadata.get( "_PERM_SCHEDULABLE" ) );
        if ( !schedulable ) {
          throw new IllegalAccessException();
        }
      }
    }

    Job job = null;

    IJobTrigger jobTrigger = SchedulerResourceUtil.convertScheduleRequestToJobTrigger( scheduleRequest, scheduler );

    HashMap<String, Serializable> parameterMap = new HashMap<String, Serializable>();
    for ( JobScheduleParam param : scheduleRequest.getJobParameters() ) {
      parameterMap.put( param.getName(), param.getValue() );
    }

    if ( isPdiFile( file ) ) {
      parameterMap = handlePDIScheduling( file, parameterMap );
    }

    parameterMap.put( LocaleHelper.USER_LOCALE_PARAM, LocaleHelper.getLocale() );

    if ( hasInputFile ) {
      SchedulerOutputPathResolver outputPathResolver = getSchedulerOutputPathResolver( scheduleRequest );
      String outputFile = outputPathResolver.resolveOutputFilePath();
      String actionId =
          getExtension( scheduleRequest.getInputFile() )
              + ".backgroundExecution"; //$NON-NLS-1$ //$NON-NLS-2$
      job =
          getScheduler().createJob( scheduleRequest.getJobName(), actionId, parameterMap, jobTrigger,
              new RepositoryFileStreamProvider( scheduleRequest.getInputFile(), outputFile,
                  getAutoCreateUniqueFilename( scheduleRequest ) )
        );
    } else {
      // need to locate actions from plugins if done this way too (but for now, we're just on main)
      String actionClass = scheduleRequest.getActionClass();
      try {
        @SuppressWarnings ( "unchecked" )
        Class<IAction> iaction = getAction( actionClass );
        job = getScheduler().createJob( scheduleRequest.getJobName(), iaction, parameterMap, jobTrigger );
      } catch ( ClassNotFoundException e ) {
        throw new RuntimeException( e );
      }
    }

    return job;
  }

  public Job triggerNow( String jobId ) throws SchedulerException {
    Job job = getScheduler().getJob( jobId );
    if ( getPolicy().isAllowed( SchedulerAction.NAME ) ) {
      getScheduler().triggerNow( jobId );
    } else {
      if ( getSession().getName().equals( job.getUserName() ) ) {
        getScheduler().triggerNow( jobId );
      }
    }
    // udpate job state
    job = getScheduler().getJob( jobId );

    return job;
  }

  public Job getContentCleanerJob() throws SchedulerException {
    IPentahoSession session = getSession();
    final String principalName = session.getName(); // this authentication wasn't matching with the job user name,
    // changed to get name via the current session
    final Boolean canAdminister = getPolicy().isAllowed( AdministerSecurityAction.NAME );

    List<Job> jobs = getScheduler().getJobs( getJobFilter( canAdminister, principalName ) );

    if ( jobs.size() > 0 ) {
      return jobs.get( 0 );
    }

    return null;
  }

  /**
   * @param lineageId
   * @return
   * @throws java.io.FileNotFoundException
   */
  public List<RepositoryFileDto> doGetGeneratedContentForSchedule( String lineageId ) throws FileNotFoundException {
    return getFileService().searchGeneratedContent( getSessionResource().doGetCurrentUserDir(), lineageId,
        QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
  }

  public Job getJob( String jobId ) throws SchedulerException {
    return getScheduler().getJob( jobId );
  }

  public boolean isScheduleAllowed() {
    return getPolicy().isAllowed( SchedulerAction.NAME );
  }

  public boolean isScheduleAllowed( String id ) {
    Boolean canSchedule = isScheduleAllowed();
    if ( canSchedule ) {
      Map<String, Serializable> metadata = getRepository().getFileMetadata( id );
      if ( metadata.containsKey( "_PERM_SCHEDULABLE" ) ) {
        canSchedule = Boolean.parseBoolean( (String) metadata.get( "_PERM_SCHEDULABLE" ) );
      }
    }
    return canSchedule;
  }

  public IJobFilter getJobFilter( boolean canAdminister, String principalName ) {
    return new JobFilter( canAdminister, principalName );
  }

  private class JobFilter implements IJobFilter {

    private boolean canAdminister;
    private String principalName;

    public JobFilter( boolean canAdminister, String principalName ) {
      this.canAdminister = canAdminister;
      this.principalName = principalName;
    }

    public boolean accept( Job job ) {
      String actionClass = (String) job.getJobParams().get( "ActionAdapterQuartzJob-ActionClass" );
      if ( canAdminister && "org.pentaho.platform.admin.GeneratedContentCleaner".equals( actionClass ) ) {
        return true;
      }
      return principalName.equals( job.getUserName() )
          && "org.pentaho.platform.admin.GeneratedContentCleaner".equals( actionClass );
    }
  }

  public String doGetCanSchedule() {
    Boolean isAllowed = getPolicy().isAllowed( SchedulerAction.NAME );
    return isAllowed ? "true" : "false"; //$NON-NLS-1$//$NON-NLS-2$
  }

  public String getState() throws SchedulerException {
    return getScheduler().getStatus().name();
  }

  public String start() throws SchedulerException {
    if ( getPolicy().isAllowed( SchedulerAction.NAME ) ) {
      getScheduler().start();
    }
    return getScheduler().getStatus().name();
  }

  public String pause() throws SchedulerException {
    if ( getPolicy().isAllowed( SchedulerAction.NAME ) ) {
      getScheduler().pause();
    }
    return getScheduler().getStatus().name();
  }

  public String shutdown() throws SchedulerException {
    if ( getPolicy().isAllowed( SchedulerAction.NAME ) ) {
      getScheduler().shutdown();
    }
    return getScheduler().getStatus().name();
  }

  public JobState pauseJob( String jobId ) throws SchedulerException {
    Job job = getJob( jobId );
    if ( isScheduleAllowed() || PentahoSessionHolder.getSession().getName().equals( job.getUserName() ) ) {
      getScheduler().pauseJob( jobId );
    }
    job = getJob( jobId );
    return job.getState();
  }

  public JobState resumeJob( String jobId ) throws SchedulerException {
    Job job = getJob( jobId );
    if ( isScheduleAllowed() || PentahoSessionHolder.getSession().getName().equals( job.getUserName() ) ) {
      getScheduler().resumeJob( jobId );
    }
    job = getJob( jobId );
    return job.getState();
  }

  public boolean removeJob( String jobId ) throws SchedulerException {
    Job job = getJob( jobId );
    if ( isScheduleAllowed() || PentahoSessionHolder.getSession().getName().equals( job.getUserName() ) ) {
      getScheduler().removeJob( jobId );
      return true;
    }
    return false;
  }

  public Job getJobInfo( String jobId ) throws SchedulerException {
    Job job = getJob( jobId );
    if ( getSecurityHelper().isPentahoAdministrator( getSession() )
        || getSession().getName().equals( job.getUserName() ) ) {
      for ( String key : job.getJobParams().keySet() ) {
        Serializable value = job.getJobParams().get( key );
        if ( value.getClass().isArray() ) {
          String[] sa = ( new String[0] ).getClass().cast( value );
          ArrayList<String> list = new ArrayList<String>();
          for ( int i = 0; i < sa.length; i++ ) {
            list.add( sa[i] );
          }
          job.getJobParams().put( key, list );
        }
      }
      return job;
    } else {
      throw new RuntimeException( "Job not found or improper credentials for access" );
    }
  }

  public List<Job> getBlockOutJobs() {
    return getBlockoutManager().getBlockOutJobs();
  }

  public boolean hasBlockouts() {
    List<Job> jobs = getBlockoutManager().getBlockOutJobs();
    return jobs != null && jobs.size() > 0;
  }

  public boolean willFire( IJobTrigger trigger ) {
    return getBlockoutManager().willFire( trigger );
  }

  public boolean shouldFireNow() {
    return getBlockoutManager().shouldFireNow();
  }

  public Job addBlockout( JobScheduleRequest jobScheduleRequest ) throws IOException, IllegalAccessException, SchedulerException {
    if ( isScheduleAllowed() ) {
      jobScheduleRequest.setActionClass( BlockoutAction.class.getCanonicalName() );
      jobScheduleRequest.getJobParameters().add( getJobScheduleParam( IBlockoutManager.DURATION_PARAM,
          jobScheduleRequest.getDuration() ) );
      jobScheduleRequest.getJobParameters().add( getJobScheduleParam( IBlockoutManager.TIME_ZONE_PARAM, jobScheduleRequest.getTimeZone() ) );
      updateStartDateForTimeZone( jobScheduleRequest );
      return createJob( jobScheduleRequest );
    }
    throw new IllegalAccessException();
  }

  protected JobScheduleParam getJobScheduleParam( String name, String value ) {
    return new JobScheduleParam( name, value );
  }

  protected JobScheduleParam getJobScheduleParam( String name, long value ) {
    return new JobScheduleParam( name, value );
  }

  protected void updateStartDateForTimeZone( JobScheduleRequest jobScheduleRequest ) {
    SchedulerResourceUtil.updateStartDateForTimeZone( jobScheduleRequest );
  }

  public Job updateBlockout( String jobId, JobScheduleRequest jobScheduleRequest )
    throws IllegalAccessException, SchedulerException, IOException {
    if ( isScheduleAllowed() ) {
      boolean isJobRemoved = removeJob( jobId );
      if ( isJobRemoved ) {
        Job job = addBlockout( jobScheduleRequest );
        return job;
      }
    }
    throw new IllegalArgumentException();
  }

  public BlockStatusProxy getBlockStatus( JobScheduleRequest jobScheduleRequest ) throws SchedulerException {
    IJobTrigger trigger = convertScheduleRequestToJobTrigger( jobScheduleRequest );
    Boolean totallyBlocked = false;
    Boolean partiallyBlocked = getBlockoutManager().isPartiallyBlocked( trigger );
    if ( partiallyBlocked ) {
      totallyBlocked = !getBlockoutManager().willFire( trigger );
    }
    return getBlockStatusProxy( totallyBlocked, partiallyBlocked );
  }

  protected BlockStatusProxy getBlockStatusProxy( Boolean totallyBlocked, Boolean partiallyBlocked ) {
    return new BlockStatusProxy( totallyBlocked, partiallyBlocked );
  }

  protected IJobTrigger convertScheduleRequestToJobTrigger( JobScheduleRequest jobScheduleRequest )
    throws SchedulerException {
    return SchedulerResourceUtil.convertScheduleRequestToJobTrigger( jobScheduleRequest, scheduler );
  }

  public JobScheduleRequest getJobInfo() {
    JobScheduleRequest jobRequest = new JobScheduleRequest();
    ComplexJobTriggerProxy proxyTrigger = new ComplexJobTriggerProxy();
    proxyTrigger.setDaysOfMonth( new int[]{1, 2, 3} );
    proxyTrigger.setDaysOfWeek( new int[]{1, 2, 3} );
    proxyTrigger.setMonthsOfYear( new int[]{1, 2, 3} );
    proxyTrigger.setYears( new int[]{2012, 2013} );
    proxyTrigger.setStartTime( new Date() );
    jobRequest.setComplexJobTrigger( proxyTrigger );
    jobRequest.setInputFile( "aaaaa" );
    jobRequest.setOutputFile( "bbbbb" );
    ArrayList<JobScheduleParam> jobParams = new ArrayList<JobScheduleParam>();
    jobParams.add( new JobScheduleParam( "param1", "aString" ) );
    jobParams.add( new JobScheduleParam( "param2", 1 ) );
    jobParams.add( new JobScheduleParam( "param3", true ) );
    jobParams.add( new JobScheduleParam( "param4", new Date() ) );
    jobRequest.setJobParameters( jobParams );
    return jobRequest;
  }

  public JobState getJobState( JobRequest jobRequest ) throws SchedulerException {
    Job job = getJob( jobRequest.getJobId() );
    if ( isScheduleAllowed() || getSession().getName().equals( job.getUserName() ) ) {
      return job.getState();
    }

    throw new UnsupportedOperationException();
  }

  protected IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  public Class<IAction> getAction( String actionClass ) throws ClassNotFoundException {
    return ( (Class<IAction>) Class.forName( actionClass ) );
  }

  public IUnifiedRepository getRepository() {
    if ( repository == null ) {
      repository = PentahoSystem.get( IUnifiedRepository.class );
    }
    return repository;
  }

  public IScheduler getScheduler() {
    if ( scheduler == null ) {
      scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null );
    }

    return scheduler;
  }

  public IAuthorizationPolicy getPolicy() {
    if ( policy == null ) {
      policy = PentahoSystem.get( IAuthorizationPolicy.class );
    }

    return policy;
  }

  protected SchedulerOutputPathResolver getSchedulerOutputPathResolver( JobScheduleRequest scheduleRequest ) {
    return new SchedulerOutputPathResolver( scheduleRequest );
  }

  protected boolean isPdiFile( RepositoryFile file ) {
    return SchedulerResourceUtil.isPdiFile( file );
  }

  protected HashMap<String, Serializable> handlePDIScheduling( RepositoryFile file,
                                                               HashMap<String, Serializable> parameterMap ) {
    return SchedulerResourceUtil.handlePDIScheduling( file, parameterMap );
  }

  public boolean getAutoCreateUniqueFilename( final JobScheduleRequest scheduleRequest ) {
    ArrayList<JobScheduleParam> jobParameters = scheduleRequest.getJobParameters();
    for ( JobScheduleParam jobParameter : jobParameters ) {
      if ( "autoCreateUniqueFilename".equals( jobParameter.getName() ) && "boolean".equals( jobParameter.getType() ) ) {
        return (Boolean) jobParameter.getValue();
      }
    }
    return true;
  }

  public List<Job> getJobs() throws SchedulerException {
    IPentahoSession session = getSession();
    final String principalName = session.getName(); // this authentication wasn't matching with the job user name,
    // changed to get name via the current session
    final Boolean canAdminister = canAdminister( session );

    List<Job> jobs = getScheduler().getJobs( new IJobFilter() {
      public boolean accept( Job job ) {
        if ( canAdminister ) {
          return !IBlockoutManager.BLOCK_OUT_JOB_NAME.equals( job.getJobName() );
        }
        return principalName.equals( job.getUserName() );
      }
    } );
    return jobs;
  }

  protected Boolean canAdminister( IPentahoSession session ) {
    if ( getPolicy().isAllowed( AdministerSecurityAction.NAME ) ) {
      return true;
    }
    return false;
  }

  protected String getExtension( String filename ) {
    return RepositoryFilenameUtils.getExtension( filename );
  }

  /**
   * Gets an instance of SessionResource
   *
   * @return <code>SessionResource</code>
   */
  protected SessionResource getSessionResource() {
    if ( sessionResource == null ) {
      sessionResource = new SessionResource();
    }
    return sessionResource;
  }

  protected FileService getFileService() {
    if ( fileService == null ) {
      fileService = new FileService();
    }

    return fileService;
  }

  protected IBlockoutManager getBlockoutManager() {
    if ( blockoutManager == null ) {
      blockoutManager = PentahoSystem.get( IBlockoutManager.class, "IBlockoutManager", null ); //$NON-NLS-1$;
    }

    return blockoutManager;
  }

  protected ISecurityHelper getSecurityHelper() {
    return SecurityHelper.getInstance();
  }
}
