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

import java.io.OutputStream;
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
import org.pentaho.platform.api.action.IVarArgsAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.repository2.unified.ISourcesStreamEvents;
import org.pentaho.platform.api.repository2.unified.IStreamListener;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.solution.ActionSequenceCompatibilityFormatter;
import org.pentaho.platform.scheduler2.email.Emailer;
import org.pentaho.platform.scheduler2.messsages.Messages;
import org.pentaho.platform.util.beans.ActionHarness;
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

  static final Log log = LogFactory.getLog(ActionAdapterQuartzJob.class);
  private static final long RETRY_COUNT = 6;
  private static final long RETRY_SLEEP_AMOUNT = 10000;

  protected Class<?> resolveClass(JobDataMap jobDataMap) throws PluginBeanException, JobExecutionException {
    String actionClass = jobDataMap.getString(QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS);
    String actionId = jobDataMap.getString(QuartzScheduler.RESERVEDMAPKEY_ACTIONID);

    Class<?> clazz = null;

    if (StringUtils.isEmpty(actionId) && StringUtils.isEmpty(actionClass)) {
      throw new LoggingJobExecutionException(Messages.getInstance().getErrorString("ActionAdapterQuartzJob.ERROR_0001_REQUIRED_PARAM_MISSING", //$NON-NLS-1$
          QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, QuartzScheduler.RESERVEDMAPKEY_ACTIONID));
    }

    for (int i = 0; i < RETRY_COUNT; i++) {
      try {
        if (!StringUtils.isEmpty(actionId)) {
          IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class);
          clazz = pluginManager.loadClass(actionId);
          return clazz;
        } else if (!StringUtils.isEmpty(actionClass)) {
          clazz = Class.forName(actionClass);
          return clazz;
        }
      } catch (Throwable t) {
        try {
          Thread.sleep(RETRY_SLEEP_AMOUNT);
        } catch (InterruptedException ie) {
          log.info(ie.getMessage(), ie);
        }
      }
    }

    // we have failed to locate the class for the actionClass
    // and we're giving up waiting for it to become available/registered
    // which can typically happen at system startup
    throw new LoggingJobExecutionException(Messages.getInstance().getErrorString("ActionAdapterQuartzJob.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
        StringUtils.isEmpty(actionId) ? actionClass : actionId));
  }

  @SuppressWarnings("unchecked")
  public void execute(JobExecutionContext context) throws JobExecutionException {
    JobDataMap jobDataMap = context.getMergedJobDataMap();
    String actionUser = jobDataMap.getString(QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER);

    Object bean;
    Class<?> actionClass = null;
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
      invokeAction(actionBean, actionUser, context, jobDataMap.getWrappedMap());

    } catch (Throwable t) {
      // We should not distinguish between checked and unchecked exceptions here. All job execution failures
      // should result in a rethrow of a quartz exception
      throw new LoggingJobExecutionException(Messages.getInstance().getErrorString("ActionAdapterQuartzJob.ERROR_0004_ACTION_FAILED", actionBean //$NON-NLS-1$
          .getClass().getName()), t);
    }
  }

  protected void invokeAction(final IAction actionBean, final String actionUser, final JobExecutionContext context, final Map<String, Serializable> params)
      throws Exception {

    // remove the scheduling infrastructure properties
    params.remove(QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS);
    params.remove(QuartzScheduler.RESERVEDMAPKEY_ACTIONID);
    params.remove(QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER);
    final IBackgroundExecutionStreamProvider streamProvider = (IBackgroundExecutionStreamProvider) params.get(QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER);
    params.remove(QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER);
    params.remove(QuartzScheduler.RESERVEDMAPKEY_UIPASSPARAM);
    params.put(IBlockoutManager.SCHEDULED_FIRE_TIME, context.getScheduledFireTime());

    if (log.isDebugEnabled()) {
      log.debug(MessageFormat.format("Scheduling system invoking action {0} as user {1} with params [ {2} ]", actionBean //$NON-NLS-1$
          .getClass().getName(), actionUser, QuartzScheduler.prettyPrintMap(params)));
    }

    Callable<Object> actionBeanRunner = new Callable<Object>() {

      public Object call() throws Exception {
        // sync job params to the action bean
        ActionHarness actionHarness = new ActionHarness(actionBean);

        final Map<String, Object> actionParams = new HashMap<String, Object>();
        actionParams.putAll(params);
        if (streamProvider != null) {
          actionParams.put("inputStream", streamProvider.getInputStream());
        }
        actionHarness.setValues(actionParams, new ActionSequenceCompatibilityFormatter());

        if (actionBean instanceof IVarArgsAction) {
          actionParams.remove("inputStream");
          actionParams.remove("outputStream");
          ((IVarArgsAction) actionBean).setVarArgs(actionParams);
        }

        if (streamProvider != null) {
          actionParams.remove("inputStream");
          if (actionBean instanceof IStreamingAction) {
            streamProvider.setStreamingAction((IStreamingAction) actionBean);
          }
          OutputStream stream = streamProvider.getOutputStream();
          if (stream instanceof ISourcesStreamEvents) {
            ((ISourcesStreamEvents) stream).addListener(new IStreamListener() {
              public void fileCreated(String filePath) {
                IUnifiedRepository repo = PentahoSystem.get(IUnifiedRepository.class);
                RepositoryFile sourceFile = repo.getFile(filePath);
                // add metadata
                Map<String, Serializable> metadata = repo.getFileMetadata(sourceFile.getId());
                String lineageId = (String) params.get(QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID);
                metadata.put(QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID, lineageId);
                repo.setFileMetadata(sourceFile.getId(), metadata);
                // send email
                SimpleRepositoryFileData data = repo.getDataForRead(sourceFile.getId(), SimpleRepositoryFileData.class);
                try {
                  sendEmail(actionParams, filePath, data);
                } catch (Throwable t) {
                  log.warn(t.getMessage(), t);
                }
              }
            });
          }
          actionParams.put("outputStream", stream);
          actionHarness.setValues(actionParams);
        }

        actionBean.execute();

        return null;
      }
    };

    if ((actionUser == null) || (actionUser.equals("system session"))) { //$NON-NLS-1$
      // For now, don't try to run quartz jobs as authenticated if the user
      // that created the job is a system user. See PPP-2350
      SecurityHelper.getInstance().runAsAnonymous(actionBeanRunner);
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

  private void sendEmail(Map<String, Object> actionParams, String filePath, SimpleRepositoryFileData data) {
    // if email is setup and we have tos, then do it
    Emailer emailer = new Emailer();
    if (!emailer.setup()) {
      // email not configured
      return;
    }
    String to = (String) actionParams.get("_SCH_EMAIL_TO");
    String cc = (String) actionParams.get("_SCH_EMAIL_CC");
    String bcc = (String) actionParams.get("_SCH_EMAIL_BCC");
    if ((to == null || "".equals(to)) && (cc == null || "".equals(cc)) && (bcc == null || "".equals(bcc))) {
      // no destination
      return;
    }
    emailer.setTo(to);
    emailer.setCc(cc);
    emailer.setBcc(bcc);
    emailer.setAttachment(data.getInputStream());
    emailer.setAttachmentName("attachment");
    String attachmentName = (String) actionParams.get("_SCH_EMAIL_ATTACHMENT_NAME");
    if (attachmentName != null && !"".equals(attachmentName)) {
      String path = filePath;
      if (path.endsWith(".*")) {
        path = path.replace(".*", "");
      }
      String extension = MimeHelper.getExtension(data.getMimeType());
      if (extension == null) {
        extension = ".bin";
      }
      if (!attachmentName.endsWith(extension)) {
        emailer.setAttachmentName(attachmentName + extension);
      } else {
        emailer.setAttachmentName(attachmentName);
      }
    } else if (data != null) {
      String path = filePath;
      if (path.endsWith(".*")) {
        path = path.replace(".*", "");
      }
      String extension = MimeHelper.getExtension(data.getMimeType());
      if (extension == null) {
        extension = ".bin";
      }
      path = path.substring(path.lastIndexOf("/") + 1, path.length());
      if (!path.endsWith(extension)) {
        emailer.setAttachmentName(path + extension);
      } else {
        emailer.setAttachmentName(path);
      }
    }
    if (data == null || data.getMimeType() == null || "".equals(data.getMimeType())) {
      emailer.setAttachmentMimeType("binary/octet-stream");
    } else {
      emailer.setAttachmentMimeType(data.getMimeType());
    }
    String subject = (String) actionParams.get("_SCH_EMAIL_SUBJECT");
    if (subject != null && !"".equals(subject)) {
      emailer.setSubject(subject);
    } else {
      emailer.setSubject("Pentaho Scheduler: " + emailer.getAttachmentName());
    }
    String message = (String) actionParams.get("_SCH_EMAIL_MESSAGE");
    if (subject != null && !"".equals(subject)) {
      emailer.setBody(message);
    }
    emailer.send();
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
