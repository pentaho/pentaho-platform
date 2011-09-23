/*
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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created April 2006 
 * @author Doug Moran
 */

package org.pentaho.platform.web.refactor;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IScheduledJob;
import org.pentaho.platform.api.engine.ISubscriptionScheduler;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.engine.SubscriptionSchedulerException;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscription;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.api.repository.SubscriptionAdminException;
import org.pentaho.platform.api.repository.SubscriptionRepositoryCheckedException;
import org.pentaho.platform.api.util.PentahoCheckedChainedException;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.ActionInfo.ActionInfoParseException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SolutionCompare;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.repository.subscription.Schedule;
import org.pentaho.platform.repository.subscription.SubscribeContent;
import org.pentaho.platform.repository.subscription.SubscriptionHelper;
import org.pentaho.platform.repository.subscription.SubscriptionRepositoryHelper;
import org.pentaho.platform.uifoundation.component.xml.XmlComponent;
import org.pentaho.platform.util.client.PublisherUtil;
import org.pentaho.platform.web.http.messages.Messages;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;

/**
 * @author wseyler
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SubscriptionAdminUIComponent extends XmlComponent {

  private static final long serialVersionUID = 2963902264708970015L;

  public static final String SCHEDULER_ACTION = "schedulerAction"; //$NON-NLS-1$

  public static final String NODE_SUBSCRIPTION_ADMIN = "subscriptionAdmin"; //$NON-NLS-1$

  public static final String NODE_CMD_RESULT = "commandResult"; //$NON-NLS-1$

  public static final String NODE_EXCEPTION = "exception"; //$NON-NLS-1$

  public static final String NODE_PARAM_ERRORS = "paramErrors"; //$NON-NLS-1$

  public static final String NODE_PARAM_MISSING = "paramMissing"; //$NON-NLS-1$

  public static final String NODE_CONTENT = "content"; //$NON-NLS-1$

  public static final String NODE_SCHEDULER_STATUS = "schedulerStatus"; //$NON-NLS-1$

  public static final String NODE_RESULT_MSG = "message"; //$NON-NLS-1$

  public static final String NODE_RESULT_TYPE = "result"; //$NON-NLS-1$

  public static final String NODE_RETURN_URL = "returnURL"; //$NON-NLS-1$

  public static final String NODE_RETURN_PARAM = "returnParam"; //$NON-NLS-1$

  public static final String NODE_STATUS_OK = "OK"; //$NON-NLS-1$

  public static final String NODE_STATUS_ERROR = "ERROR"; //$NON-NLS-1$

  public static final String NODE_STATUS_WARNING = "WARNING"; //$NON-NLS-1$

  public static final String NODE_STATUS_INFO = "INFO"; //$NON-NLS-1$

  public static final String ACTION_JOB_DO_PAUSE = "doPauseJob"; //$NON-NLS-1$

  public static final String ACTION_JOB_DO_RESUME = "doResumeJob"; //$NON-NLS-1$

  public static final String ACTION_JOB_DO_DELETE = "doDeleteJob"; //$NON-NLS-1$

  public static final String ACTION_JOB_DO_EXECUTE = "doExecuteJob"; //$NON-NLS-1$

  public static final String ACTION_JOB_DO_SCHEDULE = "doScheduleJob"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULER_DO_RESUME = "doResumeScheduler"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULER_DO_SUSPEND = "doSuspendScheduler"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULE_SHOW_DETAILS = "scheduleDetails"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULE_SHOW_ADD = "addSchedule"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULE_DO_ADD = "doAddSchedule"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULE_DO_ADD_CONTENT = "doAddContentForSchedule"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULE_SHOW_ADD_CONTENT = "addContentForSchedule"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULE_SHOW_EDIT = "editSchedule"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULE_DO_EDIT = "doEditSchedule"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULE_DO_DELETE = "doDeleteSchedule"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULE_DO_DELETE_CONTENT = "doDeleteContentForSchedule"; //$NON-NLS-1$

  public static final String ACTION_SCHEDULE_SHOW_LIST = "listSchedules"; //$NON-NLS-1$
  
  public static final String ACTION_ADD_SCHEDULE_AND_CONTENT = "doAddScheduleAndContent"; //$NON-NLS-1$
  
  public static final String ACTION_ADD_SCHEDULE_WITHOUT_CONTENT = "doAddScheduleWithoutContent"; //$NON-NLS-1$
    
  public static final String ACTION_EDIT_SCHEDULE_AND_CONTENT = "doEditScheduleAndContent"; //$NON-NLS-1$
  
  public static final String ACTION_EDIT_SCHEDULE_WITHOUT_CONTENT = "doEditScheduleWithoutContent"; //$NON-NLS-1$
  
  public static final String ACTION_DELETE_SCHEDULE_CONTENT_AND_SUBSCRIPTION = "doDeleteScheduleContentAndSubscription"; //$NON-NLS-1$
  
  public static final String ACTION_SCHEDULE_ALL_JOBS = "scheduleAll"; //$NON-NLS-1$

  public static final String ACTION_CONTENT_SHOW_LIST = "listContent"; //$NON-NLS-1$

  public static final String ACTION_CONTENT_SHOW_EDIT = "editContent"; //$NON-NLS-1$

  public static final String ACTION_CONTENT_DO_EDIT = "doEditContent"; //$NON-NLS-1$

  public static final String ACTION_CONTENT_SHOW_ADD = "addContent"; //$NON-NLS-1$

  public static final String ACTION_CONTENT_SHOW_ADD_SCHEDULE = "addScheduleForContent"; //$NON-NLS-1$

  public static final String ACTION_CONTENT_DO_ADD_SCHEDULE = "doAddScheduleForContent"; //$NON-NLS-1$

  public static final String ACTION_CONTENT_DO_SET = "doSetContent"; //$NON-NLS-1$

  public static final String ACTION_CONTENT_DO_ADD = "doAddContent"; //$NON-NLS-1$

  public static final String ACTION_CONTENT_DO_DELETE = "doDeleteContent"; //$NON-NLS-1$

  public static final String ACTION_SUBSCRIPTION_SHOW_LIST = "listSubscriptions"; //$NON-NLS-1$

  public static final String ACTION_SUBSCRIPTION_DO_DELETE = "doDeleteSubscription"; //$NON-NLS-1$

  public static final String ACTION_SHOW_IMPORT = "showImport"; //$NON-NLS-1$

  public static final String ACTION_DO_IMPORT = "doImport"; //$NON-NLS-1$

  private ISubscriptionScheduler scheduler = null;

  private ISubscriptionRepository subscriptionRepository = null;
  
  private static final Map<String,String> PARAM_NAME_TO_FRIENDLY_NAME = new HashMap<String, String>();
  static {
    PARAM_NAME_TO_FRIENDLY_NAME.put( "schedId", "system schedule id" ); //$NON-NLS-1$
    PARAM_NAME_TO_FRIENDLY_NAME.put( "title", "title" ); //$NON-NLS-1$
    PARAM_NAME_TO_FRIENDLY_NAME.put( "schedRef", "name" ); //$NON-NLS-1$
    PARAM_NAME_TO_FRIENDLY_NAME.put( "desc", "description" ); //$NON-NLS-1$
    PARAM_NAME_TO_FRIENDLY_NAME.put( "cron", "cron string" ); //$NON-NLS-1$
    PARAM_NAME_TO_FRIENDLY_NAME.put( "group", "group name" ); //$NON-NLS-1$
    PARAM_NAME_TO_FRIENDLY_NAME.put( "actionRefs", "action sequence path(s)" ); //$NON-NLS-1$
    PARAM_NAME_TO_FRIENDLY_NAME.put( "contentId", "system content id" ); //$NON-NLS-1$
    PARAM_NAME_TO_FRIENDLY_NAME.put( "jobId", "name" ); // more precisely, the trigger name in the quartz implementation  //$NON-NLS-1$
    PARAM_NAME_TO_FRIENDLY_NAME.put( "subscriptionId", "system subscription id" ); //$NON-NLS-1$
  }
  private static final Log logger = LogFactory.getLog(SubscriptionAdminUIComponent.class);

  /**
   * Provides utility methods for publishing solution files to the pentaho server.
   * 
   */
  private static final String PublishConfigFile = "publisher_config.xml"; //$NON-NLS-1$
  
  /**
   * @param urlFactory
   */
  public SubscriptionAdminUIComponent(final IPentahoUrlFactory urlFactory, final List messages) throws SubscriptionAdminException {
    super(urlFactory, messages, null);
    scheduler = PentahoSystem.get(ISubscriptionScheduler.class, this.getSession());

    subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getSession());
    if (subscriptionRepository == null) {
      error(Messages.getInstance().getErrorString("PentahoSystem.ERROR_0003_SUBSCRIPTION_REPOSITORY_NOT_INITIALIZED")); //$NON-NLS-1$
      throw (new SubscriptionAdminException(Messages.getInstance().getErrorString("PentahoSystem.ERROR_0003_SUBSCRIPTION_REPOSITORY_NOT_INITIALIZED"))); //$NON-NLS-1$
    }

    // set the default XSL
    setXsl("text/html", "SubscriptionAdmin.xsl"); //$NON-NLS-1$ //$NON-NLS-2$

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.system.PentahoBase#getLogger()
   */
  @Override
  public Log getLogger() {
    return SubscriptionAdminUIComponent.logger;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.component.BaseUIComponent#validate()
   */
  @Override
  public boolean validate() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.component.BaseUIComponent#getXmlContent()
   */
  @Override
  public Document getXmlContent() {
    setXslProperty("baseUrl", urlFactory.getDisplayUrlBuilder().getUrl()); //$NON-NLS-1$

    String schedulerActionStr = getParameter(SubscriptionAdminUIComponent.SCHEDULER_ACTION, SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_LIST);

    //        System.out.println("ACTION: " + schedulerActionStr);
    //        System.out.println("JOB-ID: " + getParameter("jobId", "-"));
    //        System.out.println("SCHED-ID: " + getParameter("schedId", "-"));

    try {
      if (SubscriptionAdminUIComponent.ACTION_ADD_SCHEDULE_AND_CONTENT.equals(schedulerActionStr)) {
        return doAddScheduleAndContent();
      }
      if (SubscriptionAdminUIComponent.ACTION_ADD_SCHEDULE_WITHOUT_CONTENT.equals(schedulerActionStr)) {
        return doAddScheduleWithoutContent();
      }
      if (SubscriptionAdminUIComponent.ACTION_EDIT_SCHEDULE_AND_CONTENT.equals(schedulerActionStr)) {
        return doEditScheduleAndContent();
      }
      if (SubscriptionAdminUIComponent.ACTION_EDIT_SCHEDULE_WITHOUT_CONTENT.equals(schedulerActionStr)) {
        return doEditScheduleWithoutContent();
      }      
      if (SubscriptionAdminUIComponent.ACTION_DELETE_SCHEDULE_CONTENT_AND_SUBSCRIPTION.equals(schedulerActionStr)) {
        return doDeleteScheduleContentAndSubscription();
      }
      if (SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_LIST.equals(schedulerActionStr)) {
        return (showAdminPageUI(null));
      }
      if (SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_EDIT.equals(schedulerActionStr)) {
        return (showEditScheduleUI(null));
      }
      
      if (SubscriptionAdminUIComponent.ACTION_SCHEDULE_DO_EDIT.equals(schedulerActionStr)) {
        if (getParameter("editDelete", null) != null) { //$NON-NLS-1$
          return (doDeleteSchedule());
        }

        if (getParameter("editAdd", null) != null) { //$NON-NLS-1$
          return (doAddSchedule());
        }

        return (doEditSchedule());
      }

      if (SubscriptionAdminUIComponent.ACTION_SCHEDULE_DO_DELETE.equals(schedulerActionStr)) {
        return (doDeleteSchedule());
      }

      if (SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_ADD.equals(schedulerActionStr)) {
        return (showAddScheduleUI(null));
      }

      if (SubscriptionAdminUIComponent.ACTION_SCHEDULE_DO_ADD.equals(schedulerActionStr)) {
        return (doAddSchedule());
      }

      if (SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_ADD_CONTENT.equals(schedulerActionStr)) {
        return (showAddContentForScheduleUI());
      }

      if (SubscriptionAdminUIComponent.ACTION_SCHEDULE_DO_DELETE_CONTENT.equals(schedulerActionStr)) {
        return (doDeleteContentForSchedule());
      }

      if (SubscriptionAdminUIComponent.ACTION_SCHEDULE_DO_ADD_CONTENT.equals(schedulerActionStr)) {
        return (doAddContentForSchedule());
      }

      if (SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_DETAILS.equals(schedulerActionStr)) {
        return (showCommandResultUI(getInfoMessage("TODO: Implement " + SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_DETAILS), SubscriptionAdminUIComponent.NODE_STATUS_INFO)); //$NON-NLS-1$
      }

      if (SubscriptionAdminUIComponent.ACTION_JOB_DO_SCHEDULE.equals(schedulerActionStr)) {
        return (doScheduleJob());
      }

      if (SubscriptionAdminUIComponent.ACTION_SCHEDULE_ALL_JOBS.equals(schedulerActionStr)) {
        return (doScheduleAllJobs());
      }

      if (SubscriptionAdminUIComponent.ACTION_CONTENT_SHOW_EDIT.equals(schedulerActionStr)) {
        return (showEditContentUI(null));
      }

      if (SubscriptionAdminUIComponent.ACTION_CONTENT_DO_EDIT.equals(schedulerActionStr)) {
        if (getParameter("editDelete", null) != null) { //$NON-NLS-1$
          return (doDeleteContent());
        }

        if (getParameter("editAdd", null) != null) { //$NON-NLS-1$
          return (doSetContent());
        }

        return (doEditContent());
      }

      if (SubscriptionAdminUIComponent.ACTION_CONTENT_DO_DELETE.equals(schedulerActionStr)) {
        return (doDeleteContent());
      }

      if (SubscriptionAdminUIComponent.ACTION_CONTENT_DO_SET.equals(schedulerActionStr)) {
        return (doSetContent());
      }

      if (SubscriptionAdminUIComponent.ACTION_CONTENT_DO_ADD.equals(schedulerActionStr)) {
        return (doAddContent());
      }

      if (SubscriptionAdminUIComponent.ACTION_CONTENT_SHOW_ADD.equals(schedulerActionStr)) {
        return (showAddContentUI(null));
      }

      if (SubscriptionAdminUIComponent.ACTION_CONTENT_SHOW_ADD_SCHEDULE.equals(schedulerActionStr)) {
        return (showAddScheduleForContentUI());
      }

      if (SubscriptionAdminUIComponent.ACTION_CONTENT_DO_ADD_SCHEDULE.equals(schedulerActionStr)) {
        return (doAddScheduleForContent());
      }

      if (SubscriptionAdminUIComponent.ACTION_JOB_DO_EXECUTE.equals(schedulerActionStr)) {
        return (doJobAction(SubscriptionAdminUIComponent.ACTION_JOB_DO_EXECUTE));
      }

      if (SubscriptionAdminUIComponent.ACTION_JOB_DO_DELETE.equals(schedulerActionStr)) {
        return (doJobAction(SubscriptionAdminUIComponent.ACTION_JOB_DO_DELETE));
      }

      if (SubscriptionAdminUIComponent.ACTION_JOB_DO_PAUSE.equals(schedulerActionStr)) {
        return (doJobAction(SubscriptionAdminUIComponent.ACTION_JOB_DO_PAUSE));
      }

      if (SubscriptionAdminUIComponent.ACTION_JOB_DO_RESUME.equals(schedulerActionStr)) {
        return (doJobAction(SubscriptionAdminUIComponent.ACTION_JOB_DO_RESUME));
      }

      if (SubscriptionAdminUIComponent.ACTION_SCHEDULER_DO_RESUME.equals(schedulerActionStr)) {
        return (doResumeScheduler());
      }

      if (SubscriptionAdminUIComponent.ACTION_SCHEDULER_DO_SUSPEND.equals(schedulerActionStr)) {
        return (doPauseScheduler());
      }

      if (SubscriptionAdminUIComponent.ACTION_SUBSCRIPTION_SHOW_LIST.equals(schedulerActionStr)) {
        return (showSubscriptionsPageUI(null));
      }

      if (SubscriptionAdminUIComponent.ACTION_SUBSCRIPTION_DO_DELETE.equals(schedulerActionStr)) {
        return (doDeleteSubscription());
      }

      if (SubscriptionAdminUIComponent.ACTION_SHOW_IMPORT.equals(schedulerActionStr)) {
        return (showImportUI());
      }

      if (SubscriptionAdminUIComponent.ACTION_DO_IMPORT.equals(schedulerActionStr)) {
        return (doImport());
      }
    } catch (ParameterValidationException e) {
      logger.error( e.getMessage() );
      Element errorEl = getErrorMessage( e.getMessage() );
      return getDocument( NODE_EXCEPTION, errorEl );
    } catch (CronStringException e) {
      logger.error( e.getMessage() );
      Element errorEl = getErrorMessage( e.getMessage() );
      return getDocument( NODE_EXCEPTION, errorEl );
    } catch (SubscriptionRepositoryCheckedException e) {
      logger.error( e.getMessage() );
      Element errorEl = getErrorMessage( e.getMessage() );
      return getDocument( NODE_EXCEPTION, errorEl );
    } catch (SubscriptionSchedulerException e) {
      logger.error( e.getMessage() );
      Element errorEl = getErrorMessage( e.getMessage() );
      return getDocument( NODE_EXCEPTION, errorEl );
    } catch ( Exception e ) {
      return (showCommandResultUI(getErrorMessage(Messages.getInstance().getString(
          "SubscriptionAdminUIComponent.UNABLE_TO_COMPLETE_REQUEST", e.getLocalizedMessage())), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$
    }

    Document document = showCommandResultUI(getInfoMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.INVALID_COMMAND", schedulerActionStr)), SubscriptionAdminUIComponent.NODE_STATUS_ERROR); //$NON-NLS-1$
    document.getRootElement().add(getReturnParams());

    return (document);
  }

  /**
   * @return
   * @throws Exception
   */
  private void doListSchedules(Element root) throws Exception {
    root = root.addElement(SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_LIST);
    ISubscriptionScheduler subScheduler = PentahoSystem.get(ISubscriptionScheduler.class, this.getSession());

    List schedList = subscriptionRepository.getSchedules();
    Map jobMap = subScheduler.getScheduledJobMap();

    Element scheduledJobs = root.addElement("scheduledJobs"); //$NON-NLS-1$
    Element unScheduledJobs = root.addElement("unScheduledJobs"); //$NON-NLS-1$
    Element extraScheduledJobs = root.addElement("extraScheduledJobs"); //$NON-NLS-1$

    int pauseCounter = 0;
    int errorCounter = 0;

    for (int i = 0; i < schedList.size(); ++i) {
      ISchedule sched = (ISchedule) schedList.get(i);
      IScheduledJob schedJob = (IScheduledJob) jobMap.remove(sched.getScheduleReference());
      if (schedJob != null) {
        int jobState = schedJob.getExecutionState();
        if (jobState == IScheduledJob.STATE_PAUSED) {
          ++pauseCounter;
        } else if (jobState != IScheduledJob.STATE_NORMAL) {
          ++errorCounter;
        }
      }

      Element job = (schedJob == null) ? unScheduledJobs.addElement("job") : scheduledJobs.addElement("job"); //$NON-NLS-1$ //$NON-NLS-2$
      try {
        getJob(sched, schedJob, job);
      } catch (ActionInfoParseException e) {
        root.add(getInfoMessage( e.getMessage() ) );
      }
    }

    for (Iterator it = jobMap.entrySet().iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry) it.next();
      IScheduledJob schedJob = (IScheduledJob) entry.getValue();
      Element job = extraScheduledJobs.addElement("job"); //$NON-NLS-1$

      job.addElement("schedRef").addText(entry.getKey().toString()); //$NON-NLS-1$
      job.addElement("desc").addText(schedJob.getDescription()); //$NON-NLS-1$

      if (schedJob != null) {
        job.addAttribute("triggerState", Integer.toString(schedJob.getExecutionState())); //$NON-NLS-1$
        Date date = schedJob.getNextTriggerTime();
        job
            .addElement("nextFireTime").addText((date == null) ? Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_NEVER") : date.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        date = schedJob.getLastTriggerTime();
        job
            .addElement("prevFireTime").addText((date == null) ? Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_NEVER") : date.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        job.addElement("jobId").addText(schedJob.getUniqueId()); //$NON-NLS-1$
        String errorMsg = schedJob.getErrorMessage();
        if (errorMsg != null) {
          job.addElement("errorMsg").addText(errorMsg); //$NON-NLS-1$
        }
      } else {
        job.addAttribute("triggerState", Integer.toString(IScheduledJob.STATE_NONE)); //$NON-NLS-1$1$
      }
    }

    if (schedList.size() == 0) {
      root.add(getInfoMessage(Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_NO_SUBSCRIPTIONS_DEFINED"))); //$NON-NLS-1$
    } else if (scheduledJobs.elements().size() == 0) {
      root.remove(scheduledJobs);
    }

    if (errorCounter > 0) {
      root.add(getErrorMessage(errorCounter
          + Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_SCHEDULED_JOBS_ARE_IN_ERROR_STATE"))); //$NON-NLS-1$
    }

    if (pauseCounter > 0) {
      root.add(getWarningMessage(pauseCounter
          + Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_SCHEDULED_JOBS_ARE_PAUSED"))); //$NON-NLS-1$
    }

    if (unScheduledJobs.elements().size() == 0) {
      root.remove(unScheduledJobs);
    } else {
      root.add(getWarningMessage(Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_SUBSCRIPTIONS_NOT_SCHEDULED"))); //$NON-NLS-1$
    }

    if (extraScheduledJobs.elements().size() == 0) {
      root.remove(extraScheduledJobs);
    } else {
      root.add(getWarningMessage(Messages.getInstance()
          .getString("SubscriptionAdminUIComponent.SUBSCRIPTION_JOBS_WITHOUT_SUBSCRIPTION"))); //$NON-NLS-1$
    }
  }

  private Element getSubscriptions(final List<ISubscription> subList, final boolean schedules) {
    Element ele = getCollectionElement("subscriptions", subList); //$NON-NLS-1$
    if (subList != null) {
      for (int i = 0; i < subList.size(); ++i) {
        ele.add(getSubscription(subList.get(i), schedules));
      }
    }
    return (ele);
  }

  private Element getSubscription(final ISubscription sub, final boolean schedules) {
    //@todo Protect the nulls!!!
    Element ele = DocumentHelper.createElement("subscription"); //$NON-NLS-1$
    ele.addAttribute("subscriptionId", sub.getId()); //$NON-NLS-1$
    ele.addElement("actionRef").addText(sub.getContent().getActionReference()); //$NON-NLS-1$
    ele.addElement("title").addText(sub.getTitle()); //$NON-NLS-1$
    ele.addElement("user").addText(sub.getUser()); //$NON-NLS-1$

    if (schedules) {
      ele.add(getSchedules(sub.getSchedules()));
    }
    return (ele);
  }

  /**
   * 
   * @param sched
   * @param schedJob
   * @param job
   * @throws ActionInfoParseException when the content associated with this job, contains an action sequence
   * path that is not valid.
   */
  private void getJob(final ISchedule sched, final IScheduledJob schedJob, final Element job) throws ActionInfoParseException {
    job.addElement("schedId").addText(sched.getId()); //$NON-NLS-1$
    job.addElement("schedRef").addText(sched.getScheduleReference()); //$NON-NLS-1$
    job.addElement("title").addText(sched.getTitle()); //$NON-NLS-1$
    job.addElement("desc").addText(sched.getDescription()); //$NON-NLS-1$
    job.addElement("group").addText(sched.getGroup()); //$NON-NLS-1$
    if ( sched.isCronSchedule() ) {
      job.addElement("cron").addText(sched.getCronString()); //$NON-NLS-1$
    } else if ( sched.isRepeatSchedule() ){
      if ( null != sched.getRepeatCount() ) {
        job.addElement("repeat-count").addText(Integer.toString(sched.getRepeatCount())); //$NON-NLS-1$
      }
      job.addElement("repeat-time-millisecs").addText(Integer.toString(sched.getRepeatInterval())); //$NON-NLS-1$
    } else {
      throw new IllegalStateException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0104_INCOMPLETE_SCHEDULE", sched.getId()));
      
    }
    Date d = sched.getStartDate();
    if ( null != d ) {
      job.addElement( "start-date" ).addText(Long.toString(d.getTime()));
    }
    d = sched.getEndDate();
    if ( null != d ) {
      job.addElement( "end-date" ).addText(Long.toString(d.getTime()));
    }
    job.addAttribute(
        "subscriberCount", Integer.toString(subscriptionRepository.getSubscriptionsForSchedule(sched).size())); //$NON-NLS-1$


    Element content = job.addElement( "content" );
    List<ISubscribeContent> scList = subscriptionRepository.getContentBySchedule(sched);
    addContent( content, scList );
    
    
    if (schedJob != null) {
      int jobState = schedJob.getExecutionState();
      job.addAttribute("triggerState", Integer.toString(jobState)); //$NON-NLS-1$1$
      Date date = schedJob.getNextTriggerTime();
      job
          .addElement("nextFireTime").addText((date == null) ? Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_NEVER") : date.toString()); //$NON-NLS-1$ //$NON-NLS-2$
      date = sched.getLastTrigger();
      job
          .addElement("prevFireTime").addText(((date == null) || (date.getTime() == 0)) ? Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_NEVER") : date.toString()); //$NON-NLS-1$ //$NON-NLS-2$
      job.addElement("jobId").addText(sched.getScheduleReference()); //$NON-NLS-1$
      String errorMsg = schedJob.getErrorMessage();
      if (errorMsg != null) {
        job.addElement("errorMsg").addText(errorMsg); //$NON-NLS-1$
      }
    } else {
      job.addAttribute("triggerState", Integer.toString(IScheduledJob.STATE_NONE)); //$NON-NLS-1$1$
    }
  }

  private void addContent( Element contentEl, List<ISubscribeContent> scList ) throws ActionInfoParseException {
    
    for ( ISubscribeContent content : scList ) {
      ActionInfo actionInfo = ActionInfo.parseActionString( content.getActionReference() );
      if ( null == actionInfo ) {
        throw new ActionInfoParseException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0103_FAILED_PARSE_SOLUTION_REPOSITORY", content.getActionReference()));
      }
      Element actionEl = contentEl.addElement( "action" );
      actionEl.addAttribute( StandardSettings.SOLUTION, actionInfo.getSolutionName() );
      actionEl.addAttribute( StandardSettings.PATH, actionInfo.getPath() );
      actionEl.addAttribute( StandardSettings.ACTION, actionInfo.getActionName() );
    }
  }
  
  private void doListContent(Element ele) {
    List contentList = subscriptionRepository.getAllContent();
    if (contentList.size() == 0) {
      ele.add(getInfoMessage(Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_NO_SUBSCRIPTION_CONTENT"))); //$NON-NLS-1$
    } else {
      ele = ele.addElement(SubscriptionAdminUIComponent.ACTION_CONTENT_SHOW_LIST);
      ele.addAttribute("count", String.valueOf(contentList.size())); //$NON-NLS-1$
      for (int i = 0; i < contentList.size(); ++i) {
        getSubscriptionContent((ISubscribeContent) contentList.get(i), ele.addElement(SubscriptionAdminUIComponent.NODE_CONTENT));
      }
    }
  }

  private void getSubscriptionContent(final ISubscribeContent content, final Element ele) {
    ele.addElement("actionRef").addText(content.getActionReference()); //$NON-NLS-1$
    ele.addAttribute("contentId", content.getId()); //$NON-NLS-1$
    ele.addElement("type").addText(content.getType()); //$NON-NLS-1$
  }

  /**
   * @return
   */
  private void doGetSchedulerStatus(Element root) {
    root = root.addElement(SubscriptionAdminUIComponent.NODE_SCHEDULER_STATUS);
    ISubscriptionScheduler subScheduler = PentahoSystem.get(ISubscriptionScheduler.class, this.getSession());
    int schedulerState = IScheduledJob.STATE_ERROR;
    try {
      schedulerState = subScheduler.getSchedulerState();
    } catch (Throwable t) {
      root
          .add(getErrorMessage(Messages.getInstance().getString("SubscriptionAdminUIComponent.ERROR_GETTING_SCHEDULER_STATUS") + t.getLocalizedMessage())); //$NON-NLS-1$
    }
    root.addAttribute("state", String.valueOf(schedulerState)); //$NON-NLS-1$
  }

  /** ********** subscription util methods ******************* */

  /**
   * Has the scheduler check the validity of the CRON expression. Returns Error XML
   * if invalid or null if ok.
   */
  Element validateCronExpression(final String cronExpr) {
    if ((cronExpr == null) || (cronExpr.length() == 0)) {
      return (getErrorMessage(Messages.getInstance().getString("SubscriptionAdminUIComponent.CRON_EXPRESSION_EMPTY"))); //$NON-NLS-1$
    }
    try {
      scheduler.getCronSummary(cronExpr); // Should throw execption for bad
      // string
    } catch (Throwable t) {
      return (getException(Messages.getInstance().getString("SubscriptionAdminUIComponent.INVALID_CRON_EXPRESSION") + cronExpr, t)); //$NON-NLS-1$
    }

    return (null);
  }

  Element validateRepeatSpec(final Integer repeatCount, final Integer repeatInterval ) {
    if ( null == repeatCount || null == repeatInterval ) {
      StringBuilder sb = new StringBuilder();
      if ( null == repeatCount || repeatCount < 0 ) {
        sb.append(Messages.getInstance().getString("SubscriptionAdminUIComponent.INVALID_REPEAT_COUNT_IN_REPEAT_SCHEDULE")); //$NON-NLS-1$
      }
      if ( null == repeatInterval || repeatInterval < 0 ) {
        sb.append(Messages.getInstance().getString("SubscriptionAdminUIComponent.INVALID_REPEAT_INTERVAL_IN_REPEAT_SCHEDULE")); //$NON-NLS-1$
      }
      return getErrorMessage( sb.toString() );
    } else {
      return null;
    }
  }
  
  private void validateCronExpressionEx(final String cronExpr) throws CronStringException {
    if ( StringUtils.isEmpty( cronExpr ) ) {
      throw new CronStringException( Messages.getInstance().getString("SubscriptionAdminUIComponent.CRON_EXPRESSION_EMPTY")); //$NON-NLS-1$
    }
    try {
      scheduler.getCronSummary(cronExpr); // Should throw execption for bad
      // string
    } catch ( Exception e ) {
      throw new CronStringException( Messages.getInstance().getString("SubscriptionAdminUIComponent.INVALID_CRON_EXPRESSION") + cronExpr, e ); //$NON-NLS-1$
    }
  }

  /************ Worker Methods ********************/

  /**
   * Creates XML Document for the Main Admin Scheduler Page - The Big Ass List
   */
  Document showAdminPageUI(final Element ele) {
    Document document = getDocument(SubscriptionAdminUIComponent.NODE_SUBSCRIPTION_ADMIN, ele);
    Element root = document.getRootElement();
    try {
      doListSchedules(root);
      doListContent(root);
      doGetSchedulerStatus(root);
      root.add(getReturnURL());
    } catch (Throwable t) {
      root.add(getException(Messages.getInstance().getString("SubscriptionAdminUIComponent.EXCEPTION_BUILDING_ADMIN_PAGE"), t)); //$NON-NLS-1$
    }
    return (document);
  }

  /**
   * Creates XML Document for the Edit Schedules Page - Edit, Delete or New Copy of
   * an existing an schedule
   */
  Document showEditScheduleUI(final Element ele) {
    Element errorEle = validateParameters(new String[] { "schedId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }

    String schedId = getParameter("schedId", null); //$NON-NLS-1$
    ISchedule sched = subscriptionRepository.getSchedule(schedId);
    if (sched == null) {
      return (showCommandResultUI(getErrorMessage(Messages.getInstance().getString(
          "SubscriptionAdminUIComponent.SCHEDULE_NOT_FOUND", getParameter("schedRef", schedId))), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    Document document = getDocument(SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_EDIT, ele);
    Element root = document.getRootElement();
    root.add(getReturnURL());

    ISubscriptionScheduler subScheduler = PentahoSystem.get(ISubscriptionScheduler.class, this.getSession());
    IScheduledJob schedJob;
    try {
      schedJob = subScheduler.getScheduledJob(sched.getScheduleReference());
    } catch (SubscriptionSchedulerException e) {
      return showCommandResultUI( getErrorMessage( Messages.getInstance().getString(
          "SubscriptionAdminUIComponent.SCHEDULE_NOT_FOUND", sched.getScheduleReference())), SubscriptionAdminUIComponent.NODE_STATUS_ERROR );
    }

    try {
      getJob(sched, schedJob, root);
    } catch (ActionInfoParseException e) {
      return showCommandResultUI( getErrorMessage( e.getMessage()), SubscriptionAdminUIComponent.NODE_STATUS_ERROR );
    }

    List subList = subscriptionRepository.getSubscriptionsForSchedule(sched);
    root.add(getSubscriptions(subList, true));

    List contentList = subscriptionRepository.getContentBySchedule(sched);
    root = root.addElement(SubscriptionAdminUIComponent.ACTION_CONTENT_SHOW_LIST);
    root.addAttribute("count", String.valueOf(contentList.size())); //$NON-NLS-1$
    for (int i = 0; i < contentList.size(); ++i) {
      Element tmpEle = root.addElement(SubscriptionAdminUIComponent.NODE_CONTENT);
      tmpEle.addElement("schedId").addText(sched.getId()); //$NON-NLS-1$
      tmpEle.addElement("schedRef").addText(sched.getScheduleReference()); //$NON-NLS-1$
      getSubscriptionContent((SubscribeContent) contentList.get(i), tmpEle);
    }

    return (document);
  }

  /**
   * Performs the Edit Schedule function
   */
  // TODO sbarkdull, does not yet support startDate, endDate, repeatCount, repeatInterval
  // currently only accessed by the PCI's subscription admin UI, which does not support these four params
  // so no need to modify this code yet
  Document doEditSchedule() throws Exception {
    Element errorEle = validateParameters(new String[] { "schedId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }
    String schedId = getParameter("schedId", null); //$NON-NLS-1$
    String cronExpr = getParameter("cron", null); //$NON-NLS-1$
    String paramNames[] = new String[] { "title", "schedRef", "desc", "cron", "group" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    if ((cronExpr != null) && false) {
      errorEle = validateCronExpression(cronExpr);
      if (errorEle != null) {
        Document document = showEditScheduleUI(errorEle);
        setParametersAsNodes(document.getRootElement(), paramNames);
        return (document);
      }
    }

    ISchedule sched;
    try {
      sched = subscriptionRepository
          .editCronSchedule(
              schedId,
              getParameter("title", null), getParameter("schedRef", null), getParameter("desc", null), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              cronExpr, getParameter("group", null), null, null); //$NON-NLS-1$
    } catch (Throwable t) {
      Document document = showEditScheduleUI(getException(Messages.getInstance().getString(
          "SubscriptionAdminUIComponent.ERROR_EDITING_SCHEDULE", getParameter("schedRef", schedId)), t)); //$NON-NLS-1$ //$NON-NLS-2$
      setParametersAsNodes(document.getRootElement(), paramNames);
      return (document);
    }

    // TODO maybe success should return to details page
    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.USER_MODIFIED_SCHEDULE", sched.getScheduleReference())), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$
  }

  
  private Document doEditScheduleWithoutContent() throws ParameterValidationException, CronStringException, SubscriptionRepositoryCheckedException {

    String paramNames[] = new String[] { "schedId", "title", "group" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
    validateParametersEx(paramNames, true );

    String schedId = getParameter("schedId", null); //$NON-NLS-1$
    String cronExpr = getParameter("cron", null); //$NON-NLS-1$
    String strRepeatInterval = getParameter("repeat-time-millisecs", null); //$NON-NLS-1$
    String strRepeatCount = getParameter("repeat-count", null); //$NON-NLS-1$
    String schedRef = getParameter("schedRef", null); //$NON-NLS-1$
    String title = getParameter("title", null); //$NON-NLS-1$
    String desc = getParameter("desc", null); //$NON-NLS-1$
    String group = getParameter("group", null); //$NON-NLS-1$
    String strStartDate = getParameter(StandardSettings.START_DATE_TIME, null);
    String strEndDate = getParameter(StandardSettings.END_DATE_TIME, null);
    
    if ( null != cronExpr ) {
      validateCronExpressionEx(cronExpr);
    } else {
      if ( null == strRepeatInterval ) {
        throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0102_INVALID_SCHEDULE_PARAM"));
      }
    }
    Integer repeatCount = ( null != strRepeatCount ) ? Integer.parseInt( strRepeatCount ) : null;
    Integer repeatInterval = ( null != strRepeatInterval ) ? Integer.parseInt( strRepeatInterval ) : null;
    assert repeatInterval == null || repeatInterval >= 0 : Messages.getInstance().getString("SubscriptionAdminUIComponent.INVALID_REPEAT_INTERVAL",strRepeatInterval);
    DateFormat fmt = SubscriptionHelper.getDateTimeFormatter();
    Date startDate = null;
    if (strStartDate != null) {
      try {
        startDate = new Date(Long.parseLong(strStartDate));
      } catch (NumberFormatException ex) {
        try {
          startDate = fmt.parse( strStartDate );
        } catch (ParseException e) {
          throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0100_INVALID_START_DATE_PARAM",strStartDate),e);
        }
      }
    }
    
    Date endDate = null;
    if (strEndDate != null) {
      try {
        endDate = new Date(Long.parseLong(strEndDate));
      } catch (NumberFormatException ex) {
        try {
          endDate = fmt.parse( strEndDate );
        } catch (ParseException e) {
          throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0100_INVALID_END_DATE_PARAM", strEndDate),e);
        }
      }
    }

    SubscriptionRepositoryHelper.editScheduleWithoutContent( subscriptionRepository, schedId, title, schedRef, desc,
        cronExpr, repeatCount, repeatInterval, group, startDate, endDate);
    
    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.USER_MODIFIED_SCHEDULE", schedRef)), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$

  }
  
  
  
  
  
  
  
  
  /**
   * Creates XML Document for the Add Schedules Page - Add a new schedule
   */
  Document showAddScheduleUI(final Element ele) {
    Document document = getDocument(SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_ADD, ele);
    document.getRootElement().add(getReturnURL());
    return (document);
  }

  /**
   * Performs the Add Schedule function
   * TODO sbarkdull, currently does not support repeat schedules or start date or end date
   */
  Document doAddSchedule() {
    String paramNames[] = new String[] { "title", "schedRef", "desc", "cron", "group" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    Element errorEle = validateParameters(paramNames, true, null);
    if (errorEle != null) {
      Document document = showAddScheduleUI(errorEle);
      setParametersAsNodes(document.getRootElement(), paramNames);
      return (document);
    }

    String cronExpr = getParameter("cron", null); //$NON-NLS-1$
    errorEle = validateCronExpression(cronExpr);
    if (errorEle != null) {
      Document document = showAddScheduleUI(errorEle);
      setParametersAsNodes(document.getRootElement(), paramNames);
      return (document);
    }

    String schedRef = getParameter("schedRef", null); //$NON-NLS-1$
    try {
      subscriptionRepository.addCronSchedule(
          getParameter("title", null), schedRef, getParameter("desc", null), cronExpr, getParameter("group", null), null, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } catch (Throwable t) {
      Document document = showAddScheduleUI(getException(Messages.getInstance().getString(
          "SubscriptionAdminUIComponent.ERROR_ADDING_SCHEDULE", schedRef), t)); //$NON-NLS-1$
      setParametersAsNodes(document.getRootElement(), paramNames);
      return (document);
    }

    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.USER_ADDED_SCHEDULE", schedRef)), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$
  }

  
  /**
   * Performs the Add Schedule function with out content
   * 
   */
  Document doAddScheduleWithoutContent() throws ParameterValidationException, CronStringException, SubscriptionRepositoryCheckedException, SubscriptionSchedulerException {
    String paramNames[] = new String[] { "title", "schedRef", "desc", "group" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
    validateParametersEx(paramNames, true );

    String cronExpr = getParameter("cron", null); //$NON-NLS-1$
    String strRepeatInterval = getParameter("repeat-time-millisecs", null); //$NON-NLS-1$
    String strRepeatCount = getParameter("repeat-count", null); //$NON-NLS-1$
    String schedRef = getParameter("schedRef", null); //$NON-NLS-1$
    String title = getParameter("title", null); //$NON-NLS-1$
    String desc = getParameter("desc", null); //$NON-NLS-1$
    String group = getParameter("group", null); //$NON-NLS-1$
    String strStartDate = getParameter(StandardSettings.START_DATE_TIME, null); 
    String strEndDate = getParameter(StandardSettings.END_DATE_TIME, null); 
    
    if ( null != cronExpr ) {
      validateCronExpressionEx(cronExpr);
    } else {
      if ( null == strRepeatInterval ) {
        throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0102_INVALID_SCHEDULE_PARAM"));
      }
    }
    Integer repeatCount = ( null != strRepeatCount ) ? Integer.parseInt( strRepeatCount ) : null;
    Integer repeatInterval = ( null != strRepeatInterval ) ? Integer.parseInt( strRepeatInterval ) : null;
    
    
    
    DateFormat fmt = SubscriptionHelper.getDateTimeFormatter();
    
    Date startDate = null;
    if (strStartDate != null) {
      try {
        startDate = new Date(Long.parseLong(strStartDate));
      } catch (NumberFormatException ex) {
        try {
          startDate = fmt.parse( strStartDate );
        } catch (ParseException e) {
          throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0100_INVALID_START_DATE_PARAM",strStartDate),e);
        }
      }
    }
    
    Date endDate = null;
    if (strEndDate != null) {
      try {
        endDate = new Date(Long.parseLong(strEndDate));
      } catch (NumberFormatException ex) {
        try {
          endDate = fmt.parse( strEndDate );
        } catch (ParseException e) {
          throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0100_INVALID_END_DATE_PARAM", strEndDate),e);
        }
      }
    }

    SubscriptionRepositoryHelper.addScheduleWithoutContent( subscriptionRepository, title, schedRef, desc,
        cronExpr, repeatCount, repeatInterval, group, startDate, endDate);

    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.USER_ADDED_SCHEDULE", schedRef)), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$ 

  }

  /**
   * Performs the Add Schedule function
   */
  Document showAddContentForScheduleUI() {
    Element errorEle = validateParameters(new String[] { "schedId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }

    String schedId = getParameter("schedId", null); //$NON-NLS-1$
    ISchedule sched = subscriptionRepository.getSchedule(schedId);
    if (sched == null) {
      return (showCommandResultUI(
          getErrorMessage(Messages.getInstance().getString(
              "SubscriptionAdminUIComponent.ERROR_SCHEDULE_NOT_FOUND", getParameter("schedRef", schedId))), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    List contentList = subscriptionRepository.getContentBySchedule(sched);
    Set currentContent = new HashSet();
    for (int i = 0; i < contentList.size(); ++i) {
      currentContent.add(((SubscribeContent) contentList.get(i)).getActionReference());
    }

    Document document = getDocument(SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_ADD_CONTENT, null);
    Element root = document.getRootElement();
    root.add(getReturnURL());

    root.add(createTextElement("schedId", schedId)); //$NON-NLS-1$
    root.add(createTextElement("schedRef", sched.getScheduleReference())); //$NON-NLS-1$

    List allContentList = subscriptionRepository.getAllContent();
    Element ele = getCollectionElement(SubscriptionAdminUIComponent.ACTION_CONTENT_SHOW_LIST, allContentList);
    for (int i = 0; i < allContentList.size(); ++i) {
      Element tmpEle = ele.addElement(SubscriptionAdminUIComponent.NODE_CONTENT);
      SubscribeContent subContent = (SubscribeContent) allContentList.get(i);
      getSubscriptionContent(subContent, tmpEle);
      tmpEle.addAttribute("selected", currentContent.contains(subContent.getActionReference()) ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    root.add(ele);

    return (document);
  }

  // TODO sbarkdull, maybe last 2 exceptions can be caught and wrapped in a local exception?
  private Document doAddScheduleAndContent() throws ParameterValidationException, CronStringException, SubscriptionRepositoryCheckedException, SubscriptionSchedulerException {

    String paramNames[] = new String[] { "title", "schedRef", "group", "actionRefs" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
    validateParametersEx(paramNames, true );

    String cronExpr = getParameter("cron", null); //$NON-NLS-1$
    String strRepeatInterval = getParameter("repeat-time-millisecs", null); //$NON-NLS-1$
    String strRepeatCount = getParameter("repeat-count", null); //$NON-NLS-1$
    String schedRef = getParameter("schedRef", null); //$NON-NLS-1$
    String actionRefs[] = getParameterAsArray("actionRefs"); //$NON-NLS-1$
    String title = getParameter("title", null); //$NON-NLS-1$
    String desc = getParameter("desc", null); //$NON-NLS-1$
    String group = getParameter("group", null); //$NON-NLS-1$
    String strStartDate = getParameter(StandardSettings.START_DATE_TIME, null); 
    String strEndDate = getParameter(StandardSettings.END_DATE_TIME, null); 
    
    if ( null != cronExpr ) {
      validateCronExpressionEx(cronExpr);
    } else {
      if ( null == strRepeatInterval ) {
        throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0102_INVALID_SCHEDULE_PARAM"));
        
      }
    }
    Integer repeatCount = ( null != strRepeatCount ) ? Integer.parseInt( strRepeatCount ) : null;
    Integer repeatInterval = ( null != strRepeatInterval ) ? Integer.parseInt( strRepeatInterval ) : null;
    DateFormat fmt = SubscriptionHelper.getDateTimeFormatter();
    
    Date startDate = null;
    if (strStartDate != null) {
      try {
        startDate = new Date(Long.parseLong(strStartDate));
      } catch (NumberFormatException ex) {
        try {
          startDate = fmt.parse( strStartDate );
        } catch (ParseException e) {
          throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0100_INVALID_START_DATE_PARAM",strStartDate),e);
        }
      }
    }
    
    Date endDate = null;
    if (strEndDate != null) {
      try {
        endDate = new Date(Long.parseLong(strEndDate));
      } catch (NumberFormatException ex) {
        try {
          endDate = fmt.parse( strEndDate );
        } catch (ParseException e) {
          throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0100_INVALID_END_DATE_PARAM", strEndDate),e);
        }
      }
    }
    
    SubscriptionRepositoryHelper.addScheduleAndContent( subscriptionRepository, title, schedRef, desc,
        cronExpr, repeatCount, repeatInterval, group, startDate, endDate, actionRefs );

    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.USER_SCHEDULED", schedRef)), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$ 
  }
  
  private Document doEditScheduleAndContent() throws ParameterValidationException, CronStringException, SubscriptionRepositoryCheckedException {

    String paramNames[] = new String[] { "schedId", StandardSettings.ACTIONS_REFS, "title", "group" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
    validateParametersEx(paramNames, true );

    String schedId = getParameter("schedId", null); //$NON-NLS-1$
    String cronExpr = getParameter("cron", null); //$NON-NLS-1$
    String strRepeatInterval = getParameter("repeat-time-millisecs", null); //$NON-NLS-1$
    String strRepeatCount = getParameter("repeat-count", null); //$NON-NLS-1$
    String schedRef = getParameter("schedRef", null); //$NON-NLS-1$
    String[] actionRefs = getParameterAsArray( StandardSettings.ACTIONS_REFS );
    String title = getParameter("title", null); //$NON-NLS-1$
    String desc = getParameter("desc", null); //$NON-NLS-1$
    String group = getParameter("group", null); //$NON-NLS-1$
    String strStartDate = getParameter(StandardSettings.START_DATE_TIME, null);
    String strEndDate = getParameter(StandardSettings.END_DATE_TIME, null);
    
    if ( null != cronExpr ) {
      validateCronExpressionEx(cronExpr);
    } else {
      if ( null == strRepeatInterval ) {
        throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0102_INVALID_SCHEDULE_PARAM"));
      }
    }
    Integer repeatCount = ( null != strRepeatCount ) ? Integer.parseInt( strRepeatCount ) : null;
    Integer repeatInterval = ( null != strRepeatInterval ) ? Integer.parseInt( strRepeatInterval ) : null;
    assert repeatInterval == null || repeatInterval >= 0 : Messages.getInstance().getString("SubscriptionAdminUIComponent.INVALID_REPEAT_INTERVAL",strRepeatInterval);
    
    DateFormat fmt = SubscriptionHelper.getDateTimeFormatter();
    Date startDate = null;
    if (strStartDate != null) {
      try {
        startDate = new Date(Long.parseLong(strStartDate));
      } catch (NumberFormatException ex) {
        try {
          startDate = fmt.parse( strStartDate );
        } catch (ParseException e) {
          throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0100_INVALID_START_DATE_PARAM",strStartDate),e);
        }
      }
    }
    
    Date endDate = null;
    if (strEndDate != null) {
      try {
        endDate = new Date(Long.parseLong(strEndDate));
      } catch (NumberFormatException ex) {
        try {
          endDate = fmt.parse( strEndDate );
        } catch (ParseException e) {
          throw new ParameterValidationException(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0100_INVALID_END_DATE_PARAM", strEndDate),e);
        }
      }
    }

    SubscriptionRepositoryHelper.editScheduleAndContent( subscriptionRepository, schedId, title, schedRef, desc,
        cronExpr, repeatCount, repeatInterval, group, startDate, endDate, actionRefs );
    
    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.USER_MODIFIED_SCHEDULE", schedRef)), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$

  }
  
  /**
   * Performs the Add Schedule function
   */
  Document doAddContentForSchedule() {
    Element errorEle = validateParameters(new String[] { "schedId", "contentId" }, true, null); //$NON-NLS-1$ //$NON-NLS-2$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }

    String schedId = getParameter("schedId", null); //$NON-NLS-1$
    ISchedule sched = subscriptionRepository.getSchedule(schedId);
    if (sched == null) {
      return (showCommandResultUI(
          getErrorMessage(Messages.getInstance().getString(
              "SubscriptionAdminUIComponent.ERROR_SCHEDULE_NOT_FOUND", getParameter("schedRef", schedId))), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    String contentArray[] = getParameterAsArray("contentId"); //$NON-NLS-1$

    try {
      subscriptionRepository.setContentForSchedule(contentArray, schedId);
    } catch (Throwable t) {
      return (showCommandResultUI(
          getException(Messages.getInstance().getString(
              "SubscriptionAdminUIComponent.ERROR_SETTING_CONTENT_FOR_SCHEDULE", getParameter("schedRef", schedId)), t), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    return (showCommandResultUI(
        getOkMessage(Messages.getInstance().getString(
            "SubscriptionAdminUIComponent.USER_SET_CONTENT_FOR_SCHEDULE", getParameter("schedRef", schedId))), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Performs the Delete Schedule function
   */
  Document doDeleteSchedule() throws SubscriptionRepositoryCheckedException {
    Element errorEle = validateParameters(new String[] { "schedId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }

    String schedId = getParameter("schedId", null); //$NON-NLS-1$
    subscriptionRepository.deleteScheduleById(schedId);

    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.USER_DELETED_SCHEDULE", getParameter("schedRef", schedId))), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Syncronizes the schedule with the Scheduler's trigger for that subscription
   */
  Document doScheduleJob() {
    Element errorEle = validateParameters(new String[] { "schedId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }

    String schedId = getParameter("schedId", null); //$NON-NLS-1$
    try {
      ISchedule sched = subscriptionRepository.getSchedule(schedId);
      if (sched == null) {
        return (showCommandResultUI(getErrorMessage(Messages.getInstance().getString(
            "SubscriptionAdminUIComponent.ERROR_SCHEDULE_NOT_FOUND", schedId)), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$
      } else {
        if ( sched.isCronSchedule() ) {
          errorEle = validateCronExpression(sched.getCronString());
        } else if ( sched.isRepeatSchedule() ){
          errorEle = validateRepeatSpec( sched.getRepeatCount(), sched.getRepeatInterval() );
        } else {
          errorEle = getErrorMessage(Messages.getInstance().getString("SubscriptionAdminUIComponent.INCOMPLETE_SCHEDULE", sched.getId()));
          
        }
        if (errorEle != null) {
          Document document = showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR);
          document.getRootElement().add(
              getErrorMessage(Messages.getInstance().getString("SubscriptionAdminUIComponent.ERROR_CANNOT_START_SCHEDULE"))); //$NON-NLS-1$
          return (document);
        }

        if (scheduler.syncSchedule(null, sched) == null) {
          return (showCommandResultUI(
              getErrorMessage(Messages.getInstance().getString(
                  "SubscriptionAdminUIComponent.ERROR_UNABLE_TO_SCHEDULE_JOB", sched.getScheduleReference())), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$
        }
      }
    } catch (Throwable t) {
      return (showCommandResultUI(getException(Messages.getInstance().getString(
          "SubscriptionAdminUIComponent.ERROR_SCHEDULING", getParameter("schedRef", schedId)), t), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.USER_SCHEDULED", getParameter("schedRef", schedId))), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Syncronizes all schedules with the Scheduler's triggers
   */
  Document doScheduleAllJobs() {
    try {
      List errorMessages = subscriptionRepository.syncSchedules();
      if (errorMessages.size() > 0) {
        Document document = showCommandResultUI(getWarningMessage(Messages.getInstance()
            .getString("SubscriptionAdminUIComponent.WARNING_NOT_ALL_SCHEDULES_STARTED")), SubscriptionAdminUIComponent.NODE_STATUS_WARNING); //$NON-NLS-1$
        Element root = document.getRootElement();
        for (int i = 0; i < errorMessages.size(); ++i) {
          root.add(getWarningMessage(errorMessages.get(i).toString()));
        }
        return (document);
      }
    } catch (Throwable t) {
      return (showCommandResultUI(getException(Messages.getInstance()
          .getString("SubscriptionAdminUIComponent.ERROR_SYNCHRONIZING_SCHEDULES"), t), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$
    }

    return (showAdminPageUI(getOkMessage(Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_SYNCHRONIZE_COMPLETE")))); //$NON-NLS-1$
  }

  // TODO sbarkdull, needs to be broken up into 4 methods, pause, resume, delete, execute
  /**
   * Execute a schedule now
   */
  Document doJobAction(final String command) {
    // NOTE: in the Quartz implementation, jobId is actually the trigger name, go figure
    Element errorEle = validateParameters(new String[] { "jobId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }
    String jobId = getParameter("jobId", null); //$NON-NLS-1$
    String okMessage = ""; //$NON-NLS-1$
    try {
      if (SubscriptionAdminUIComponent.ACTION_JOB_DO_PAUSE.equals(command)) {
        scheduler.pauseJob(jobId);
        okMessage = Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_PAUSED_JOB"); //$NON-NLS-1$
      } else if (SubscriptionAdminUIComponent.ACTION_JOB_DO_RESUME.equals(command)) {
        scheduler.resumeJob(jobId);
        okMessage = Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_RESUMED_JOB"); //$NON-NLS-1$
      } else if (SubscriptionAdminUIComponent.ACTION_JOB_DO_DELETE.equals(command)) {
        scheduler.deleteJob(jobId);
        okMessage = Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_DELETED_JOB"); //$NON-NLS-1$
      } else if (SubscriptionAdminUIComponent.ACTION_JOB_DO_EXECUTE.equals(command)) {
        scheduler.executeJob(jobId);
        okMessage = Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_SUBMITTED_JOB"); //$NON-NLS-1$
      }
    } catch (Throwable t) {
      return (showCommandResultUI(getException("", t), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$
    }

    return (showCommandResultUI(getOkMessage(okMessage + getParameter("schedRef", jobId)), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$
  }

  Document doResumeScheduler() {
    try {
      scheduler.resumeScheduler();
    } catch (Throwable t) {
      return (showCommandResultUI(getException(Messages.getInstance()
          .getString("SubscriptionAdminUIComponent.ERROR_RESUMING_SCHEDULER"), t), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$
    }
    return (showCommandResultUI(
        getOkMessage(Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_SCHEDULER_RESUMED")), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$
  }

  Document doPauseScheduler() {
    try {
      scheduler.pauseScheduler();
    } catch (Throwable t) {
      return (showCommandResultUI(getException(Messages.getInstance()
          .getString("SubscriptionAdminUIComponent.ERROR_PAUSING_SCHEDULER"), t), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$
    }
    return (showCommandResultUI(
        getOkMessage(Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_PAUSED_SCHEDULER")), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$
  }

  /**
   * Creates XML Document for the Edit Content Page - Edit, Delete or New Copy of
   * an existing an content item
   */
  Document showEditContentUI(Element ele) throws Exception {
    Element errorEle = validateParameters(new String[] { "contentId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }

    String contentId = getParameter("contentId", null); //$NON-NLS-1$
    ISubscribeContent subContent = subscriptionRepository.getContentById(contentId);
    if (subContent == null) {
      return (showCommandResultUI(
          getErrorMessage(Messages.getInstance().getString(
              "SubscriptionAdminUIComponent.ERROR_CONTENT_NOT_FOUND", getParameter("actionRef", contentId))), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    Document document = getDocument(SubscriptionAdminUIComponent.ACTION_CONTENT_SHOW_EDIT, ele);
    Element root = document.getRootElement();
    getSubscriptionContent(subContent, root);
    root.add(getReturnURL());

    List schedulelist = subContent.getSchedules();
    ele = getCollectionElement("schedules", schedulelist); //$NON-NLS-1$
    for (int i = 0; i < schedulelist.size(); ++i) {
      Element tmpEle = getSchedule((Schedule) schedulelist.get(i));
      tmpEle.addElement("contentId").addText(contentId); //$NON-NLS-1$
      tmpEle.addElement("actionRef").addText(getParameter("actionRef", contentId)); //$NON-NLS-1$ //$NON-NLS-2$
      ele.add(tmpEle);
    }
    root.add(ele);

    return (document);
  }

  private Element getSchedule(final Schedule sched) {
    Element ele = DocumentHelper.createElement("schedule"); //$NON-NLS-1$
    ele.addElement("schedId").addText(sched.getId()); //$NON-NLS-1$
    ele.addElement("schedRef").addText(sched.getScheduleReference()); //$NON-NLS-1$
    ele.addElement("title").addText(sched.getTitle()); //$NON-NLS-1$
    ele.addElement("desc").addText(sched.getDescription()); //$NON-NLS-1$
    ele.addElement("group").addText(sched.getGroup()); //$NON-NLS-1$
    return (ele);
  }

  private Element getSchedules(final List schedList) {
    Element ele = getCollectionElement("schedules", schedList); //$NON-NLS-1$

    int listSize = (schedList == null) ? 0 : schedList.size();
    for (int i = 0; i < listSize; ++i) {
      ele.add(getSchedule((Schedule) schedList.get(i)));
    }

    return (ele);
  }

  private Element getCollectionElement(final String name, final Collection c) {
    Element ele = DocumentHelper.createElement(name);
    if (c == null) {
      ele.addAttribute("count", "0"); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      ele.addAttribute("count", String.valueOf(c.size())); //$NON-NLS-1$
    }
    return (ele);
  }

  /**
   * Performs the Edit Content function
   */
  Document doEditContent() throws Exception {
    Element errorEle = validateParameters(new String[] { "contentId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }
    String contentId = getParameter("contentId", null); //$NON-NLS-1$
    String paramNames[] = new String[] { "actionRef", "type" }; //$NON-NLS-1$ //$NON-NLS-2$

    ISubscribeContent subContent;
    try {
      subContent = subscriptionRepository.editContent(contentId,
          getParameter("actionRef", null), getParameter("type", null)); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (Throwable t) {
      Document document = showEditContentUI(getException(Messages.getInstance().getString(
          "SubscriptionAdminUIComponent.ERROR_EDITING_CONTENT", getParameter("actionRef", contentId)), t)); //$NON-NLS-1$ //$NON-NLS-2$
      setParametersAsNodes(document.getRootElement(), paramNames);
      return (document);
    }

    // TODO maybe success should return to details page
    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.USER_MODIFIED_CONTENT", subContent.getActionReference())), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$
  }

  /**
   * Creates XML Document for the Add Content Page - Add a new content
   */
  Document showAddContentUI(Element ele) {
    Document document = getDocument(SubscriptionAdminUIComponent.ACTION_CONTENT_SHOW_ADD, ele);
    document.getRootElement().add(getReturnURL());

    List contentList = subscriptionRepository.getAllContent();
    Set currentContent = new HashSet();
    for (int i = 0; i < contentList.size(); ++i) {
      currentContent.add(((SubscribeContent) contentList.get(i)).getActionReference());
    }

    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getSession());
    String actionSequences[] = repository.getAllActionSequences(ISolutionRepository.ACTION_EXECUTE);
    Arrays.sort(actionSequences, new SolutionCompare()); // Put these babies in order
    ele = DocumentHelper.createElement("listContent"); //$NON-NLS-1$
    ele.addAttribute("count", String.valueOf(actionSequences.length)); //$NON-NLS-1$
    String lastFolder = null;
    Element folderEle = null;
    for (String s : actionSequences) {
      String currentFolder = getFolder(s);
      if (!currentFolder.equals(lastFolder)) {
        folderEle = DocumentHelper.createElement("folder");
        folderEle.addAttribute("name", currentFolder);
        ele.add(folderEle);
        lastFolder = currentFolder;
      }
      Element tmpEle = DocumentHelper.createElement("content").addText(getFile(s)); //$NON-NLS-1$
      
      tmpEle.addAttribute("selected", currentContent.contains(s) ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      folderEle.add(tmpEle);   
    }

    document.getRootElement().add(ele);
    System.out.println(document.asXML());
    return (document);
  }
  
  protected String getFolder(final String fullPath) {
	  int idx = fullPath.lastIndexOf("/");
	  if (idx > 0) {
		  return fullPath.substring(0, idx);
	  }
	  return "";
  }

  protected String getFile(final String fullPath) {
	  int idx = fullPath.lastIndexOf("/");
	  if (idx > 0) {
		  return fullPath.substring(idx+1);
	  }
	  return fullPath;
  }
  
  /**
   * Add the action sequences whose paths are in the actionRef array to 
   * the content in the subscription repository, if the action sequence is not
   * already in the subscription repository. If it is already in the 
   * subscription repository, leave it there.
   */
  Document doAddContent() {
    String paramNames[] = new String[] { "actionRef" }; //$NON-NLS-1$
    Element errorEle = validateParameters(paramNames, true, null);
    if (errorEle != null) {
      Document document = showAddContentUI(errorEle);
      setParametersAsNodes(document.getRootElement(), paramNames);
      return (document);
    }

    String actionRef[] = getParameterAsArray("actionRef"); //$NON-NLS-1$
    try {
      subscriptionRepository.addContent(actionRef);
    } catch (Throwable t) {
      Document document = showAddContentUI(getException(Messages.getInstance()
          .getString("SubscriptionAdminUIComponent.ERROR_SETTING_CONTENT"), t)); //$NON-NLS-1$
      setParametersAsNodes(document.getRootElement(), paramNames);
      return (document);
    }

    return (showCommandResultUI(getOkMessage(Messages.getInstance()
        .getString("SubscriptionAdminUIComponent.USER_SET_SUBSCRIPTION_CONTENT")), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$
  }
  
  /**
   * If the action sequences (content) that are currently in the subscription repository
   * are not also in the actionRef array, remove them from the subscription repository.
   * If each action sequence specified in the actionRef array is not in the
   * subscription repository, add it to the subscription repository.
   */
  Document doSetContent() {
    String paramNames[] = new String[] { "actionRef" }; //$NON-NLS-1$
    Element errorEle = validateParameters(paramNames, true, null);
    if (errorEle != null) {
      Document document = showAddContentUI(errorEle);
      setParametersAsNodes(document.getRootElement(), paramNames);
      return (document);
    }

    String actionRef[] = getParameterAsArray("actionRef"); //$NON-NLS-1$
    try {
      subscriptionRepository.setContent(actionRef);
    } catch (Throwable t) {
      Document document = showAddContentUI(getException(Messages.getInstance()
          .getString("SubscriptionAdminUIComponent.ERROR_SETTING_CONTENT"), t)); //$NON-NLS-1$
      setParametersAsNodes(document.getRootElement(), paramNames);
      return (document);
    }

    return (showCommandResultUI(getOkMessage(Messages.getInstance()
        .getString("SubscriptionAdminUIComponent.USER_SET_SUBSCRIPTION_CONTENT")), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$
  }

  /**
   * Performs the Delete Content function
   */
  Document doDeleteContent() {
    Element errorEle = validateParameters(new String[] { "contentId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }

    String contentId = getParameter("contentId", null); //$NON-NLS-1$
    try {
      subscriptionRepository.deleteSubscribeContentById(contentId);
    } catch (Throwable t) {
      return (showCommandResultUI(
          getException(Messages.getInstance().getString(
              "SubscriptionAdminUIComponent.ERROR_DELETING_CONTENT", getParameter("actionRef", contentId)), t), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.USER_DELETED_CONTENT", getParameter("actionRef", contentId))), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Performs the Delete Content function
   */
  Document doDeleteContentForSchedule() throws Exception {
    Element errorEle = validateParameters(new String[] { "contentId", "schedId" }, true, null); //$NON-NLS-1$ //$NON-NLS-2$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }

    String contentId = getParameter("contentId", null); //$NON-NLS-1$
    String schedId = getParameter("schedId", null); //$NON-NLS-1$
    try {
      subscriptionRepository.deleteContentForSchedule(contentId, schedId);
    } catch (Exception e ) {
      return (showCommandResultUI(
          getException(
              Messages.getInstance()
                  .getString(
                      "SubscriptionAdminUIComponent.ERROR_DELETING_CONTENT_FOR_SCHEDULE", getParameter("actionRef", contentId), getParameter("schedRef", schedId)), e), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    return (showEditContentUI(getOkMessage(Messages.getInstance()
        .getString(
            "SubscriptionAdminUIComponent.USER_DELETED_CONTENT_FOR_SCHEDULE", getParameter("actionRef", contentId), getParameter("schedRef", schedId))))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  /**
   * Performs the Add Schedule function
   */
  Document showAddScheduleForContentUI() {
    Element errorEle = validateParameters(new String[] { "contentId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }

    String contentId = getParameter("contentId", null); //$NON-NLS-1$
    ISubscribeContent content = subscriptionRepository.getContentById(contentId);
    if (content == null) {
      return (showCommandResultUI(
          getErrorMessage(Messages.getInstance().getString(
              "SubscriptionAdminUIComponent.ERROR_CONTENT_NOT_FOUND", getParameter("actionRef", contentId))), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    List scheduleList = content.getSchedules();
    Set currentSchedules = new HashSet();
    for (int i = 0; i < scheduleList.size(); ++i) {
      currentSchedules.add(((Schedule) scheduleList.get(i)).getId());
    }

    Document document = getDocument(SubscriptionAdminUIComponent.ACTION_CONTENT_SHOW_ADD_SCHEDULE, null);
    Element root = document.getRootElement();
    root.add(getReturnURL());

    root.add(createTextElement("contentId", contentId)); //$NON-NLS-1$
    root.add(createTextElement("actionRef", content.getActionReference())); //$NON-NLS-1$

    List allScheduleList = subscriptionRepository.getSchedules();
    Element ele = getCollectionElement(SubscriptionAdminUIComponent.ACTION_SCHEDULE_SHOW_LIST, allScheduleList);
    for (int i = 0; i < allScheduleList.size(); ++i) {
      Schedule sched = (Schedule) allScheduleList.get(i);
      Element tmpEle = getSchedule(sched);
      tmpEle.addAttribute("selected", currentSchedules.contains(sched.getId()) ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      ele.add(tmpEle);
    }

    root.add(ele);

    return (document);
  }

  /**
   * Performs the Add Schedule function
   */
  Document doAddScheduleForContent() {
    Element errorEle = validateParameters(new String[] { "contentId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }

    String contentId = getParameter("contentId", null); //$NON-NLS-1$
    ISubscribeContent content = subscriptionRepository.getContentById(contentId);
    if (content == null) {
      return (showCommandResultUI(
          getErrorMessage(Messages.getInstance().getString(
              "SubscriptionAdminUIComponent.ERROR_CONTENT_NOT_FOUND", getParameter("actionRef", contentId))), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
    }
    // schedId is an optional param?
    String scheduleArray[] = getParameterAsArray("schedId"); //$NON-NLS-1$

    try {
      subscriptionRepository.setSchedulesForContent(scheduleArray, contentId);
    } catch (Throwable t) {
      return (showCommandResultUI(
          getException(
              Messages.getInstance()
                  .getString(
                      "SubscriptionAdminUIComponent.ERROR_SETTING_SCHEDULES_FOR_CONTENT", getParameter("actionRef", contentId)), t), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    return (showCommandResultUI(
        getOkMessage(Messages.getInstance().getString(
            "SubscriptionAdminUIComponent.USER_SET_SCHEDULES_FOR_CONTENT", getParameter("actionRef", contentId))), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$ //$NON-NLS-2$
  }

  Document showImportUI() {
    Document document = getDocument(SubscriptionAdminUIComponent.ACTION_SHOW_IMPORT, null);
    document.getRootElement().add(getReturnURL());
    return (document);
  }

  public Document doImport() {

    HttpServletRequest request = ((HttpRequestParameterProvider) getParameterProviders().get(
        HttpRequestParameterProvider.SCOPE_REQUEST)).getRequest();
    String contentType = request.getContentType();
    if ((contentType == null)
        || ((contentType.indexOf("multipart/form-data") < 0) && (contentType.indexOf("multipart/mixed stream") < 0))) { //$NON-NLS-1$ //$NON-NLS-2$
      return (showCommandResultUI(getErrorMessage(Messages.getInstance()
          .getString("SubscriptionAdminUIComponent.ERROR_IMPORT_FILE_NOT_UPLOADED")), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$
    }
    Enumeration enumer = request.getParameterNames();
    while (enumer.hasMoreElements()) {
      System.out.println(enumer.nextElement().toString());
    }
    try {
      String publishPassword = null;
      FileItem publishFile = null;

      // DiskFileUpload uploader = new DiskFileUpload();
      ServletFileUpload uploader = new ServletFileUpload(new DiskFileItemFactory());
      
      List fileList = uploader.parseRequest(request);
      Iterator iter = fileList.iterator();
      while (iter.hasNext()) {
        FileItem fi = (FileItem) iter.next();
        if (fi.isFormField()) {
          publishPassword = new String(fi.get());
        } else {
          publishFile = fi;
        }
      }
      saveFileItem(publishFile, publishPassword);
    } catch (Throwable t) {
      return (showCommandResultUI(getException(Messages.getInstance()
          .getString("SubscriptionAdminUIComponent.ERROR_UNABLE_TO_PARSE_FILE"), t), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$
    }

    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString("SubscriptionAdminUIComponent.USER_IMPORT_SUCCESSFUL")), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$

  }

  /**
   * @param fi
   * @param string
   */
  private int saveFileItem(FileItem fi, String password) {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getSession() );
    int status = ISolutionRepository.FILE_ADD_SUCCESSFUL;
    
    if ( checkPublisherKey(PublisherUtil.getPasswordKey(password)) ) {

      String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath(""); //$NON-NLS-1$

      try {
        PentahoSystem.systemEntryPoint();
        status = repository.publish(solutionPath, "system", "ScheduleAndContentImport.xml", fi.get(), true);
      } catch (PentahoAccessControlException e) {
        status = ISolutionRepository.FILE_ADD_FAILED;
        if (SubscriptionAdminUIComponent.logger.isErrorEnabled()) {
          SubscriptionAdminUIComponent.logger.error(Messages.getInstance().getErrorString("SubscriptionAdminUIComponent.ERROR_0104_USER_ERROR"), e);
        }
      } finally {
        PentahoSystem.systemExitPoint();
      }
    } else {
      status = ISolutionRepository.FILE_ADD_INVALID_PUBLISH_PASSWORD;
    }
    return status;
  }


  Document showSubscriptionsPageUI(final Element ele) {
    String userName = getParameter("user", null); //$NON-NLS-1$

    Document document = getDocument(SubscriptionAdminUIComponent.ACTION_SUBSCRIPTION_SHOW_LIST, ele);
    Element root = document.getRootElement();
    try {
      List<ISubscription> allSubscriptions;
      if (userName == null) {
        allSubscriptions = subscriptionRepository.getAllSubscriptions();
      } else {
        allSubscriptions = subscriptionRepository.getUserSubscriptions(userName);
      }
      root.add(getSubscriptions(allSubscriptions, true));
    } catch (Throwable t) {
      root.add(getException(Messages.getInstance().getString("SubscriptionAdminUIComponent.ERROR_BUILDING_SUBSCRIPTION_PAGE"), t)); //$NON-NLS-1$
    }
    return (document);
  }


  private Document doDeleteScheduleContentAndSubscription() throws SubscriptionRepositoryCheckedException, ParameterValidationException, SubscriptionSchedulerException {
    
    String paramNames[] = new String[] { "schedId" }; //$NON-NLS-1$
    validateParametersEx( paramNames, true );

    String schedId = getParameter("schedId", null); //$NON-NLS-1$
    
    ISchedule schedule = subscriptionRepository.getSchedule( schedId );
    SubscriptionRepositoryHelper.deleteScheduleContentAndSubscription( subscriptionRepository, schedule );

    return (showCommandResultUI(getOkMessage(Messages.getInstance().getString(
        "SubscriptionAdminUIComponent.USER_DELETED_SCHEDULE", getParameter("schedRef", schedId))), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  /**
   * Performs the Delete Content function
   */
  Document doDeleteSubscription() {
    Element errorEle = validateParameters(new String[] { "subscriptionId" }, true, null); //$NON-NLS-1$
    if (errorEle != null) {
      return (showCommandResultUI(errorEle, SubscriptionAdminUIComponent.NODE_STATUS_ERROR));
    }

    String subscriptionId = getParameter("subscriptionId", null); //$NON-NLS-1$
    try {
      subscriptionRepository.deleteSubscription(subscriptionId);
    } catch (Throwable t) {
      return (showCommandResultUI(
          getException(Messages.getInstance().getString(
              "SubscriptionAdminUIComponent.ERROR_DELETING_SUBSCRIPTION", getParameter("title", subscriptionId)), t), SubscriptionAdminUIComponent.NODE_STATUS_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    return (showCommandResultUI(
        getOkMessage(Messages.getInstance().getString(
            "SubscriptionAdminUIComponent.USER_DELETED_SUBSCRIPTION", getParameter("title", subscriptionId))), SubscriptionAdminUIComponent.NODE_STATUS_OK)); //$NON-NLS-1$ //$NON-NLS-2$
  }


  /** *** Generic Utility Methods ******* */

  /**
   * Creates XML Document for the Command Results Page - Displays success or failure
   * messages
   */
  Document showCommandResultUI(final Element ele, final String result) {
    Document document = getDocument(SubscriptionAdminUIComponent.NODE_CMD_RESULT, ele);
    Element root = document.getRootElement();
    root.addAttribute("result", result); //$NON-NLS-1$
    return (document);
  }

  Element getReturnParams() {
    Element ele = DocumentHelper.createElement(SubscriptionAdminUIComponent.NODE_RETURN_PARAM);
    IParameterProvider parameterProvider = (IParameterProvider) getParameterProviders().get(
        HttpRequestParameterProvider.SCOPE_REQUEST);
    for (Iterator nameItr = parameterProvider.getParameterNames(); nameItr.hasNext();) {
      String name = (String) nameItr.next();
      String value = parameterProvider.getStringParameter(name, null);
      if (value != null) {
        ele.addElement(name).addText(value);
      }
    }
    return (ele);
  }

  Element getReturnURL() {
    Element ele = DocumentHelper.createElement(SubscriptionAdminUIComponent.NODE_RETURN_URL);
    IParameterProvider parameterProvider = (IParameterProvider) getParameterProviders().get(
        HttpRequestParameterProvider.SCOPE_REQUEST);
    String str = ""; //$NON-NLS-1$
    for (Iterator nameItr = parameterProvider.getParameterNames(); nameItr.hasNext();) {
      String name = (String) nameItr.next();
      String value = parameterProvider.getStringParameter(name, null);
      if (value != null) {
        str += "&" + name + "=" + value; //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    ele.addText(str);
    return (ele);
  }

  Element getErrorMessage(final String message) {
    return (getMessage(SubscriptionAdminUIComponent.NODE_STATUS_ERROR, message));
  }

  Element getWarningMessage(final String message) {
    return (getMessage(SubscriptionAdminUIComponent.NODE_STATUS_WARNING, message));
  }

  Element getOkMessage(final String message) {
    return (getMessage(SubscriptionAdminUIComponent.NODE_STATUS_OK, message));
  }

  Element getInfoMessage(final String message) {
    return (getMessage(SubscriptionAdminUIComponent.NODE_STATUS_INFO, message));
  }

  Element getMessage(final String type, final String message) {
    return (DocumentHelper.createElement(SubscriptionAdminUIComponent.NODE_RESULT_MSG).addAttribute("result", type).addText(message)); //$NON-NLS-1$
  }

  Element getException(final String message, final Throwable t) {
    Element root = DocumentHelper.createElement(SubscriptionAdminUIComponent.NODE_EXCEPTION);
    if (message != null) {
      root.addElement("message").addText(message); //$NON-NLS-1$
    }

    // Some exceptions may have null messages, look for one in the chain.
    String exMsg = null;
    Throwable tmpT = t;
    while ((tmpT != null) && (exMsg == null)) {
      exMsg = tmpT.getLocalizedMessage();
      tmpT = tmpT.getCause();
    }

    root
        .addElement("exceptionMessage").addText((exMsg != null) ? exMsg : Messages.getInstance().getString("SubscriptionAdminUIComponent.ERROR_CAUSE_UNKNOWN", t.getClass().getName())); //$NON-NLS-1$ //$NON-NLS-2$
    return (root);
  }

  private Element createTextElement(final String elementName, final String text) {
    return (DocumentHelper.createElement(elementName).addText(text));
  }

  /**
   * Verify that the passed in parameterNames exist in the HTTP request. Missing
   * parameters are added to ele as new parameter error nodes (see
   * NODE_PARAM_ERRORS, and NODE_PARAM_MISSING) If 'ele' is null, a new Element is
   * created to contain the error messages. The Element 'ele' is returned to the
   * caller unchanged if all the parameters pass verification.
   * 
   * @param ele
   *            The element that the new parameter error Elements will be added to.
   *            If null, a new one will be created
   * @param paramNames
   *            The names of the parameters to verify in the request.
   * @param notEmpty
   *            if true, the parameter must exist and can not be the empty string ""
   */
  Element validateParameters(final String params[], final boolean notEmpty, Element ele) {
    Object param;
    for (String element : params) {
      param = getObjectParameter(element, null);
      if ((param == null) || ( notEmpty && isEmpty(param))) {
        if (ele == null) {
          ele = DocumentHelper.createElement(SubscriptionAdminUIComponent.NODE_PARAM_ERRORS);
        }
        ele.addElement(SubscriptionAdminUIComponent.NODE_PARAM_MISSING).addText(element);
      }
    }
    return (ele);
  }
  
  private boolean isEmpty(Object parameter) {
    assert parameter != null;
    if (parameter instanceof String) {
      return ((String)parameter).length() == 0;
    } else if (parameter instanceof Object[]) {
      Object[] objArray = ((Object[])parameter);
      for (int i=0; i< objArray.length; i++ ) {
        if (isEmpty(objArray[i])) {
          return true;
        }
      }
    }
    return false;
  }
  
  private void validateParametersEx(final String params[], final boolean notEmpty ) throws ParameterValidationException {
    Object param;
    List<String> missingParams = new ArrayList<String>();
    for (String paramName : params) {
      param = getObjectParameter(paramName, null);
      if ((param == null) || ( notEmpty && isEmpty(param))) {
        missingParams.add( paramName );
      }
    }
    if ( missingParams.size() > 0 ) {
      StringBuilder sb = new StringBuilder();
      sb.append(Messages.getInstance().getString("SubscriptionAdminUIComponent.MISSING_PARAMETERS"));
      for ( String paramName : missingParams ) {
        String friendlyName = PARAM_NAME_TO_FRIENDLY_NAME.get( paramName );
        sb.append( friendlyName ).append( ", " ); //$NON-NLS-1$
      }
      sb.delete( sb.length()-2, sb.length() );  // remove last ", "
      
      throw new ParameterValidationException( sb.toString() );
    }
  }

  /**
   * Adds the passed in parameters from the HTTP request to the passed in XML
   * document. The Parameters are added as new Elements to the passed in 'parent'
   * Element. If 'parent' is null, a new Element is created to be the parent. The
   * parent is returned to the caller.
   * 
   * @param parent
   *            The element that the new Elements will be added to. If null, a new
   *            one will be created
   * @param paramNames
   *            The names of the parameters to pull out of the request.
   */
  private void setParametersAsNodes(final Element parent, final String paramNames[]) {
    if ((parent == null) || (paramNames == null)) {
      return;
    }

    for (String element : paramNames) {
      Node node = parent.selectSingleNode(element);
      if (node instanceof Element) {
        ((Element) node).setText(getParameter(element, "")); //$NON-NLS-1$
      } else {
        parent.addElement(element).addText(getParameter(element, "")); //$NON-NLS-1$
      }
    }
  }

  /**
   * Convienence method for creating a new Document with a root of the passed in
   * 'rootName'. If 'ele' is not null, it is added to the Document root.
   * 
   * @param rootName
   *            Name of the root node to create
   * @param ele
   *            element to add to the new root
   * @return The new Document
   */
  private Document getDocument(final String rootName, final Element ele) {
    Document document = DocumentHelper.createDocument();
    Element root = document.addElement(rootName);
    if (ele != null) {
      root.add(ele);
    }
    return (document);
  }

  /**
   * Checks the publisher key in the publish_config.xml against the presented key.
   * 
   * @param key
   *            The key to verify
   * @return true if the presented key is the same as the one in publish_config.xml
   */
  private static final boolean checkPublisherKey(final String key) {
    if (key != null) {
      Document doc = PentahoSystem.getSystemSettings().getSystemSettingsDocument(PublishConfigFile);
      if (doc != null) {
        Node node = doc.selectSingleNode("//publisher-config/publisher-password"); //$NON-NLS-1$
        if (node != null) {
          String setting = node.getText();
          if ((setting != null) && (setting.length() > 0)) {
            String pubKey = PublisherUtil.getPasswordKey(setting);
            return pubKey.equals(key);
          }
        }
      }
    }
    return false;
  }

  private static class ParameterValidationException extends PentahoCheckedChainedException {
    private static final long serialVersionUID = 666L;
    public ParameterValidationException() {
      super();
    }
    public ParameterValidationException(final String message, final Throwable reas) {
      super(message, reas);
    }
    public ParameterValidationException(final String message) {
      super(message);
    }
    public ParameterValidationException(final Throwable reas) {
      super(reas);
    }
  }

  private static class CronStringException extends PentahoCheckedChainedException {
    private static final long serialVersionUID = 666L;
    public CronStringException() {
      super();
    }
    public CronStringException(final String message, final Throwable reas) {
      super(message, reas);
    }
    public CronStringException(final String message) {
      super(message);
    }
    public CronStringException(final Throwable reas) {
      super(reas);
    }
  }
}
