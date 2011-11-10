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
 * Copyright 2010 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.scheduler2.quartz;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.scheduler2.messsages.Messages;
import org.pentaho.platform.util.beans.ActionHarness;
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

  static final Log log = LogFactory.getLog(ActionAdapterQuartzJob.class);

  protected Class<?> resolveClass(JobDataMap jobDataMap) throws PluginBeanException, JobExecutionException {
    String actionClass = jobDataMap.getString(QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS);
    String actionId = jobDataMap.getString(QuartzScheduler.RESERVEDMAPKEY_ACTIONID);

    Class<?> clazz = null;

    if (!StringUtils.isEmpty(actionId)) {
      IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class);
      clazz = pluginManager.loadClass(actionId);
    } else if (!StringUtils.isEmpty(actionClass)) {
      try {
        clazz = Class.forName(actionClass);
      } catch (Exception e) {
        throw new LoggingJobExecutionException(Messages.getInstance().getErrorString("ActionAdapterQuartzJob.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
            actionClass), e);
      }
    } else {
      throw new LoggingJobExecutionException(Messages.getInstance().getErrorString("ActionAdapterQuartzJob.ERROR_0001_REQUIRED_PARAM_MISSING", //$NON-NLS-1$
          QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, QuartzScheduler.RESERVEDMAPKEY_ACTIONID));
    }
    return clazz;
  }

  @SuppressWarnings("unchecked")
  public void execute(JobExecutionContext context) throws JobExecutionException {
    JobDataMap jobDataMap = context.getMergedJobDataMap();
    String actionUser = jobDataMap.getString(QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER);

    Object bean;
    Class actionClass = null;
    try {
      actionClass = resolveClass(jobDataMap);
      bean = actionClass.newInstance();
    } catch (Exception e) {
      throw new LoggingJobExecutionException(Messages.getInstance().getErrorString("ActionAdapterQuartzJob.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
          (actionClass == null) ? "unknown" : actionClass.getName()), e); //$NON-NLS-1$
    }

    if (!(bean instanceof IAction)) {
      throw new LoggingJobExecutionException(Messages.getInstance().getErrorString(
          "ActionAdapterQuartzJob.ERROR_0003_ACTION_WRONG_TYPE", actionClass.getName(), //$NON-NLS-1$
          IAction.class.getName()));
    }

    final IAction actionBean = (IAction) bean;

    try {

      invokeAction(actionBean, actionUser, jobDataMap.getWrappedMap());

    } catch (Throwable t) {
      // We should not distinguish between checked and unchecked exceptions here. All job execution failures
      // should result in a rethrow of a quartz exception
      throw new LoggingJobExecutionException(Messages.getInstance().getErrorString("ActionAdapterQuartzJob.ERROR_0004_ACTION_FAILED", actionBean //$NON-NLS-1$
          .getClass().getName()), t);
    }
  }

  protected void invokeAction(final IAction actionBean, final String actionUser, final Map<String, Serializable> params) throws Exception {

    // remove the scheduling infrastructure properties
    params.remove(QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS);
    params.remove(QuartzScheduler.RESERVEDMAPKEY_ACTIONID);
    params.remove(QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER);
    final IBackgroundExecutionStreamProvider streamProvider = (IBackgroundExecutionStreamProvider)params.get(QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER);
    params.remove(QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER);

    if (log.isDebugEnabled()) {
      log.debug(MessageFormat.format("Scheduling system invoking action {0} as user {1} with params [ {2} ]", actionBean //$NON-NLS-1$
          .getClass().getName(), actionUser, QuartzScheduler.prettyPrintMap(params)));
    }


    Callable<Object> actionBeanRunner = new Callable<Object>() {

      public Object call() throws Exception {
        // sync job params to the action bean
        ActionHarness actionHarness = new ActionHarness(actionBean);

        Map<String, Object> actionParams = new HashMap<String, Object>();
        actionParams.putAll(params);
        if (streamProvider != null) {
          actionParams.put("inputStream", streamProvider.getInputStream());
        }
        actionHarness.setValues(actionParams);
        if (streamProvider != null) {
          actionParams.clear();
          if (actionBean instanceof IStreamingAction) {
            streamProvider.setStreamingAction((IStreamingAction)actionBean);
          }
          actionParams.put("outputStream", streamProvider.getOutputStream());
          actionHarness.setValues(actionParams);
        }
        actionBean.execute();
        return null;
      }
    };

    if ( (actionUser == null) || (actionUser.equals("system session")) ) { //$NON-NLS-1$
      // For now, don't try to run quartz jobs as authenticated if the user
      // that created the job is a system user. See PPP-2350
      SecurityHelper.getInstance().runAsUnauthenticated(actionBeanRunner);
    } else {
      SecurityHelper.getInstance().runAsUser(actionUser, actionBeanRunner);
    }
    IScheduler scheduler = PentahoSystem.getObjectFactory().get(IScheduler.class, "IScheduler2", null);
    scheduler.fireJobCompleted(actionBean, actionUser, params, streamProvider);

    if (log.isDebugEnabled()) {
      log.debug(MessageFormat.format("Scheduling system successfully invoked action {0} as user {1} with params [ {2} ]", actionBean //$NON-NLS-1$
          .getClass().getName(), actionUser, QuartzScheduler.prettyPrintMap(params)));
    }

  }

  class LoggingJobExecutionException extends JobExecutionException {
    private static final long serialVersionUID = -4124907454208034326L;

    public LoggingJobExecutionException(String msg) {
      super(msg);
      log.error(msg);
    }

    public LoggingJobExecutionException(String msg, Throwable t) {
      super(msg, t);
      log.error(msg, t);
    }

  }

}
