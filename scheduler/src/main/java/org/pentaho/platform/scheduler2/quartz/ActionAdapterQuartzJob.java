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

import java.io.OutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IPostProcessingAction;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.action.IVarArgsAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.ISourcesStreamEvents;
import org.pentaho.platform.api.repository2.unified.IStreamListener;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.solution.ActionSequenceCompatibilityFormatter;
import org.pentaho.platform.scheduler2.blockout.BlockoutAction;
import org.pentaho.platform.scheduler2.email.Emailer;
import org.pentaho.platform.scheduler2.messsages.Messages;
import org.pentaho.platform.util.beans.ActionHarness;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.MimeHelper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A Quartz job that is responsible for executing the {@link IAction} referred to in the job context.
 * 
 * @author aphillips
 */
public class ActionAdapterQuartzJob implements Job {

  static final Log log = LogFactory.getLog( ActionAdapterQuartzJob.class );
  private static final long RETRY_COUNT = 6;
  private static final long RETRY_SLEEP_AMOUNT = 10000;

  private String outputFilePath = null;
  private Object lock = new Object();

  protected Class<?> resolveClass( JobDataMap jobDataMap ) throws PluginBeanException, JobExecutionException {
    String actionClass = jobDataMap.getString( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS );
    String actionId = jobDataMap.getString( QuartzScheduler.RESERVEDMAPKEY_ACTIONID );

    Class<?> clazz = null;

    if ( StringUtils.isEmpty( actionId ) && StringUtils.isEmpty( actionClass ) ) {
      throw new LoggingJobExecutionException( Messages.getInstance().getErrorString(
          "ActionAdapterQuartzJob.ERROR_0001_REQUIRED_PARAM_MISSING", //$NON-NLS-1$
          QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, QuartzScheduler.RESERVEDMAPKEY_ACTIONID ) );
    }

    for ( int i = 0; i < RETRY_COUNT; i++ ) {
      try {
        if ( !StringUtils.isEmpty( actionId ) ) {
          IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
          clazz = pluginManager.loadClass( actionId );
          return clazz;
        } else if ( !StringUtils.isEmpty( actionClass ) ) {
          clazz = Class.forName( actionClass );
          return clazz;
        }
      } catch ( Throwable t ) {
        try {
          Thread.sleep( RETRY_SLEEP_AMOUNT );
        } catch ( InterruptedException ie ) {
          log.info( ie.getMessage(), ie );
        }
      }
    }

    // we have failed to locate the class for the actionClass
    // and we're giving up waiting for it to become available/registered
    // which can typically happen at system startup
    throw new LoggingJobExecutionException( Messages.getInstance().getErrorString(
        "ActionAdapterQuartzJob.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
        StringUtils.isEmpty( actionId ) ? actionClass : actionId ) );
  }

  @SuppressWarnings( "unchecked" )
  public void execute( JobExecutionContext context ) throws JobExecutionException {
    JobDataMap jobDataMap = context.getMergedJobDataMap();
    String actionUser = jobDataMap.getString( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER );

    Object bean;
    Class<?> actionClass = null;
    try {
      actionClass = resolveClass( jobDataMap );
      bean = actionClass.newInstance();
    } catch ( Exception e ) {
      throw new LoggingJobExecutionException( Messages.getInstance().getErrorString(
          "ActionAdapterQuartzJob.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
          ( actionClass == null ) ? "unknown" : actionClass.getName() ), e ); //$NON-NLS-1$
    }

    if ( !( bean instanceof IAction ) ) {
      throw new LoggingJobExecutionException( Messages.getInstance().getErrorString(
          "ActionAdapterQuartzJob.ERROR_0003_ACTION_WRONG_TYPE", actionClass.getName(), //$NON-NLS-1$
          IAction.class.getName() ) );
    }

    final IAction actionBean = (IAction) bean;

    try {
      invokeAction( actionBean, actionUser, context, jobDataMap.getWrappedMap() );

    } catch ( Throwable t ) {
      // ensure that scheduler thread isn't blocked on lock
      synchronized ( lock ) {
        lock.notifyAll();
      }

      // We should not distinguish between checked and unchecked exceptions here. All job execution failures
      // should result in a rethrow of a quartz exception
      throw new LoggingJobExecutionException( Messages.getInstance().getErrorString(
          "ActionAdapterQuartzJob.ERROR_0004_ACTION_FAILED", actionBean //$NON-NLS-1$
              .getClass().getName() ), t );
    }
  }

  protected void invokeAction( final IAction actionBean, final String actionUser, final JobExecutionContext context,
      final Map<String, Serializable> params ) throws Exception {

    final IScheduler scheduler = PentahoSystem.getObjectFactory().get( IScheduler.class, "IScheduler2", null );
    final Map<String, Serializable> jobParams = new HashMap<String, Serializable>( params ); // shallow copy

    // remove the scheduling infrastructure properties
    params.remove( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS );
    params.remove( QuartzScheduler.RESERVEDMAPKEY_ACTIONID );
    params.remove( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER );
    Object objsp = params.get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER );
    IBackgroundExecutionStreamProvider sp = null;
    if ( objsp != null && IBackgroundExecutionStreamProvider.class.isAssignableFrom( objsp.getClass() ) ) {
      sp = (IBackgroundExecutionStreamProvider) objsp;
    }
    final IBackgroundExecutionStreamProvider streamProvider = sp;
    params.remove( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER );
    params.remove( QuartzScheduler.RESERVEDMAPKEY_UIPASSPARAM );
    // The scheduled_fire_time is useful only to the blockoutAction see PDI-10171
    if ( actionBean instanceof BlockoutAction ) {
      params.put( IBlockoutManager.SCHEDULED_FIRE_TIME, context.getScheduledFireTime() );
    }

    if ( log.isDebugEnabled() ) {
      log.debug( MessageFormat.format(
          "Scheduling system invoking action {0} as user {1} with params [ {2} ]", actionBean //$NON-NLS-1$
              .getClass().getName(), actionUser, QuartzScheduler.prettyPrintMap( params ) ) );
    }

    Callable<Boolean> actionBeanRunner = new Callable<Boolean>() {

      public Boolean call() throws Exception {
        LocaleHelper.setLocaleOverride( (Locale) params.get( LocaleHelper.USER_LOCALE_PARAM ) );
        // sync job params to the action bean
        ActionHarness actionHarness = new ActionHarness( actionBean );
        boolean updateJob = false;

        final Map<String, Object> actionParams = new HashMap<String, Object>();
        actionParams.putAll( params );
        if ( streamProvider != null ) {
          actionParams.put( "inputStream", streamProvider.getInputStream() );
        }
        actionHarness.setValues( actionParams, new ActionSequenceCompatibilityFormatter() );

        if ( actionBean instanceof IVarArgsAction ) {
          actionParams.remove( "inputStream" );
          actionParams.remove( "outputStream" );
          ( (IVarArgsAction) actionBean ).setVarArgs( actionParams );
        }

        boolean waitForFileCreated = false;
        OutputStream stream = null;

        if ( streamProvider != null ) {
          actionParams.remove( "inputStream" );
          if ( actionBean instanceof IStreamingAction ) {
            streamProvider.setStreamingAction( (IStreamingAction) actionBean );
          }

          // BISERVER-9414 - validate that output path still exist
          SchedulerOutputPathResolver resolver =
              new SchedulerOutputPathResolver( streamProvider.getOutputPath(), actionUser );
          String outputPath = resolver.resolveOutputFilePath();
          actionParams.put( "useJcr", Boolean.TRUE );
          actionParams.put( "jcrOutputPath", outputPath.substring( 0, outputPath.lastIndexOf( "/" ) ) );

          if ( !outputPath.equals( streamProvider.getOutputPath() ) ) {
            streamProvider.setOutputFilePath( outputPath ); // set fallback path
            updateJob = true; // job needs to be deleted and recreated with the new output path
          }

          stream = streamProvider.getOutputStream();
          if ( stream instanceof ISourcesStreamEvents ) {
            ( (ISourcesStreamEvents) stream ).addListener( new IStreamListener() {
              public void fileCreated( final String filePath ) {
                synchronized ( lock ) {
                  outputFilePath = filePath;
                  lock.notifyAll();
                }
              }
            } );
            waitForFileCreated = true;
          }
          actionParams.put( "outputStream", stream );
          // The lineage_id is only useful for the metadata and not needed at this level see PDI-10171
          actionParams.remove( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
          actionHarness.setValues( actionParams );
        }

        actionBean.execute();

        if ( stream != null ) {
          IOUtils.closeQuietly( stream );
        }

        if ( waitForFileCreated ) {
          synchronized ( lock ) {
            if ( outputFilePath == null ) {
              lock.wait();
            }
          }
          sendEmail( actionParams, params, outputFilePath );
        }
        if ( actionBean instanceof IPostProcessingAction ) {
          markContentAsGenerated( (IPostProcessingAction) actionBean );
        }
        return updateJob;
      }

      private void markContentAsGenerated( IPostProcessingAction actionBean ) {
        IUnifiedRepository repo = PentahoSystem.get( IUnifiedRepository.class );
        String lineageId = (String) params.get( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
        for ( IContentItem contentItem : actionBean.getActionOutputContents() ) {
          RepositoryFile sourceFile = repo.getFile( contentItem.getPath() );
           // add metadata iof we have access and we have file
          if ( sourceFile != null ) {
            Map<String, Serializable> metadata = repo.getFileMetadata( sourceFile.getId() );
            metadata.put( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID, lineageId );
            repo.setFileMetadata( sourceFile.getId(), metadata );
          }
        }
      }
    };

    boolean requiresUpdate = false;
    if ( ( actionUser == null ) || ( actionUser.equals( "system session" ) ) ) { //$NON-NLS-1$
      // For now, don't try to run quartz jobs as authenticated if the user
      // that created the job is a system user. See PPP-2350
      requiresUpdate = SecurityHelper.getInstance().runAsAnonymous( actionBeanRunner );
    } else {
      try {
        requiresUpdate = SecurityHelper.getInstance().runAsUser( actionUser, actionBeanRunner );
      } catch ( Throwable t ) {
        Object restartFlag = jobParams.get( QuartzScheduler.RESERVEDMAPKEY_RESTART_FLAG );
        if ( restartFlag == null ) {
          final SimpleJobTrigger trigger = new SimpleJobTrigger( new Date(), null, 0, 0 );
          final Class<IAction> iaction = (Class<IAction>) actionBean.getClass();
          // recreate the job in the context of the original creator
          SecurityHelper.getInstance().runAsUser( actionUser, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
              if ( streamProvider != null ) {
                streamProvider.setStreamingAction( null ); // remove generated content  
              }
              QuartzJobKey jobKey = QuartzJobKey.parse( context.getJobDetail().getName() );
              String jobName = jobKey.getJobName();
              jobParams.put( QuartzScheduler.RESERVEDMAPKEY_RESTART_FLAG, Boolean.TRUE );
              scheduler.createJob( jobName, iaction, jobParams, trigger, streamProvider );
              log.warn( "New RunOnce job created for " + jobName + " -> possible startup synchronization error" );
              return null;
            }
          } );
        } else {
          log.warn( "RunOnce already created, skipping" );
          throw new Exception( t );
        }
      }
    }

    scheduler.fireJobCompleted( actionBean, actionUser, params, streamProvider );

    if ( requiresUpdate ) {
      log.warn( "Output path for job: " + context.getJobDetail().getName() + " has changed. Job requires update" );
      try {
        final IJobTrigger trigger = scheduler.getJob( context.getJobDetail().getName() ).getJobTrigger();
        final Class<IAction> iaction = (Class<IAction>) actionBean.getClass();

        // remove job with outdated/invalid output path
        scheduler.removeJob( context.getJobDetail().getName() );

        // recreate the job in the context of the original creator
        SecurityHelper.getInstance().runAsUser( actionUser, new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            streamProvider.setStreamingAction( null ); // remove generated content
            QuartzJobKey jobKey = QuartzJobKey.parse( context.getJobDetail().getName() );
            String jobName = jobKey.getJobName();
            org.pentaho.platform.api.scheduler2.Job j =
                scheduler.createJob( jobName, iaction, jobParams, trigger, streamProvider );
            log.warn( "New Job: " + j.getJobId() + " created" );
            return null;
          }
        } );
      } catch ( Exception e ) {
        log.error( e.getMessage(), e );
      }
    }

    if ( log.isDebugEnabled() ) {
      log.debug( MessageFormat.format(
          "Scheduling system successfully invoked action {0} as user {1} with params [ {2} ]", actionBean //$NON-NLS-1$
              .getClass().getName(), actionUser, QuartzScheduler.prettyPrintMap( params ) ) );
    }

  }

  private void sendEmail( Map<String, Object> actionParams, Map<String, Serializable> params, String filePath ) {
    try {
      IUnifiedRepository repo = PentahoSystem.get( IUnifiedRepository.class );
      RepositoryFile sourceFile = repo.getFile( filePath );
      // add metadata
      Map<String, Serializable> metadata = repo.getFileMetadata( sourceFile.getId() );
      String lineageId = (String) params.get( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
      metadata.put( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID, lineageId );
      repo.setFileMetadata( sourceFile.getId(), metadata );
      // send email
      SimpleRepositoryFileData data = repo.getDataForRead( sourceFile.getId(), SimpleRepositoryFileData.class );
      // if email is setup and we have tos, then do it
      Emailer emailer = new Emailer();
      if ( !emailer.setup() ) {
        // email not configured
        return;
      }
      String to = (String) actionParams.get( "_SCH_EMAIL_TO" );
      String cc = (String) actionParams.get( "_SCH_EMAIL_CC" );
      String bcc = (String) actionParams.get( "_SCH_EMAIL_BCC" );
      if ( ( to == null || "".equals( to ) ) && ( cc == null || "".equals( cc ) )
          && ( bcc == null || "".equals( bcc ) ) ) {
        // no destination
        return;
      }
      emailer.setTo( to );
      emailer.setCc( cc );
      emailer.setBcc( bcc );
      emailer.setAttachment( data.getInputStream() );
      emailer.setAttachmentName( "attachment" );
      String attachmentName = (String) actionParams.get( "_SCH_EMAIL_ATTACHMENT_NAME" );
      if ( attachmentName != null && !"".equals( attachmentName ) ) {
        String path = filePath;
        if ( path.endsWith( ".*" ) ) {
          path = path.replace( ".*", "" );
        }
        String extension = MimeHelper.getExtension( data.getMimeType() );
        if ( extension == null ) {
          extension = ".bin";
        }
        if ( !attachmentName.endsWith( extension ) ) {
          emailer.setAttachmentName( attachmentName + extension );
        } else {
          emailer.setAttachmentName( attachmentName );
        }
      } else if ( data != null ) {
        String path = filePath;
        if ( path.endsWith( ".*" ) ) {
          path = path.replace( ".*", "" );
        }
        String extension = MimeHelper.getExtension( data.getMimeType() );
        if ( extension == null ) {
          extension = ".bin";
        }
        path = path.substring( path.lastIndexOf( "/" ) + 1, path.length() );
        if ( !path.endsWith( extension ) ) {
          emailer.setAttachmentName( path + extension );
        } else {
          emailer.setAttachmentName( path );
        }
      }
      if ( data == null || data.getMimeType() == null || "".equals( data.getMimeType() ) ) {
        emailer.setAttachmentMimeType( "binary/octet-stream" );
      } else {
        emailer.setAttachmentMimeType( data.getMimeType() );
      }
      String subject = (String) actionParams.get( "_SCH_EMAIL_SUBJECT" );
      if ( subject != null && !"".equals( subject ) ) {
        emailer.setSubject( subject );
      } else {
        emailer.setSubject( "Pentaho Scheduler: " + emailer.getAttachmentName() );
      }
      String message = (String) actionParams.get( "_SCH_EMAIL_MESSAGE" );
      if ( subject != null && !"".equals( subject ) ) {
        emailer.setBody( message );
      }
      emailer.send();
    } catch ( Exception e ) {
      log.warn( e.getMessage(), e );
    }
  }

  class LoggingJobExecutionException extends JobExecutionException {
    private static final long serialVersionUID = -4124907454208034326L;

    public LoggingJobExecutionException( String msg ) {
      super( msg );
      log.error( msg );
    }

    public LoggingJobExecutionException( String msg, Throwable t ) {
      super( msg, t );
      log.error( msg, t );
    }

  }

}
