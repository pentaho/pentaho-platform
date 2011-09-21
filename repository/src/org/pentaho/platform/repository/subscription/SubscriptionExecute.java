/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.repository.subscription;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.SubscriptionSchedulerException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscription;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.api.repository.SubscriptionRepositoryCheckedException;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.BaseRequestHandler;
import org.pentaho.platform.repository.content.CoreContentRepositoryOutputHandler;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.messages.LocaleHelper;

public class SubscriptionExecute extends PentahoBase {

  private static final boolean debug = PentahoSystem.debug;

  private static final long serialVersionUID = -6053183867424885168L;

  private static final Log logger = LogFactory.getLog(SubscriptionExecute.class);

  private String logId;

  @Override
  public Log getLogger() {
    return SubscriptionExecute.logger;
  }

  @Override
  public String getLogId() {
    return logId;
  }

  public void execute(final String scheduleReference, boolean isFinalFiring) {

    PentahoSystem.systemEntryPoint();
    try {
      String sessionId = "scheduler-" + UUIDUtil.getUUIDAsString(); //$NON-NLS-1$
      StandaloneSession scheduleSession = new StandaloneSession(scheduleReference, sessionId);

      ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, scheduleSession);
      ISchedule sched = subscriptionRepository.getScheduleByScheduleReference(scheduleReference);
      if (sched == null) {
        error(Messages.getInstance().getErrorString("SubscriptionExecute.ERROR_0001_UNABLE_TO_GET_SCHEDULE", scheduleReference)); //$NON-NLS-1$
        return;
      }

      Date lastExeTm = sched.getLastTrigger();
      sched.setLastTrigger(new Date());

      List<ISubscription> subscriptionList = subscriptionRepository.getSubscriptionsForSchedule(scheduleReference);

      info("FIRE: " + scheduleReference); //$NON-NLS-1$
      for (int i = 0; i < subscriptionList.size(); ++i) {
        Subscription sub = (Subscription) subscriptionList.get(i);
        if (PentahoSystem.trace) {
          trace("" + sub); //$NON-NLS-1$
        }
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.putAll(sub.getParameters());

        ISubscribeContent subContent = sub.getContent();
        ActionInfo contentInfo = ActionInfo.parseActionString(subContent.getActionReference());

        final String jobName = sub.getUser() + " : " + sub.getTitle(); //$NON-NLS-1$

        paramMap.put("solution", contentInfo.getSolutionName()); //$NON-NLS-1$
        paramMap.put("path", contentInfo.getPath()); //$NON-NLS-1$
        paramMap.put("action", contentInfo.getActionName()); //$NON-NLS-1$

        paramMap.put("SUB_SCHEDULED_EXECUTE", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        paramMap.put("SUB_EXECUTE_TIME", sched.getLastTrigger()); //$NON-NLS-1$
        paramMap.put("SUB_PREV_EXECUTE_TIME", lastExeTm); //$NON-NLS-1$
        if (sched.isCronSchedule()) {
          paramMap.put("SUB_SCHEDULE", sched.getCronString()); //$NON-NLS-1$
        } else if (sched.isRepeatSchedule()) {
          if (null != sched.getRepeatCount()) {
            paramMap.put("SUB_SCHEDULE_REPEAT_COUNT", sched.getRepeatCount()); //$NON-NLS-1$
          }
          paramMap.put("SUB_SCHEDULE_REPEAT_TIME", sched.getRepeatInterval()); //$NON-NLS-1$
        } else {
          throw new IllegalStateException(Messages.getInstance().getErrorString("SubscriptionExecute.ERROR_0005_INVALID_CRON_OR_REPEAT", sched.getId())); //$NON-NLS-1$
        }
        DateFormat fmt = SubscriptionHelper.getDateTimeFormatter();
        Date d = sched.getStartDate();
        if (null != d) {
          paramMap.put("SUB_START_DATE", fmt.format(d)); //$NON-NLS-1$
        }
        d = sched.getEndDate();
        if (null != d) {
          paramMap.put("SUB_END_DATE", fmt.format(d)); //$NON-NLS-1$
        }
        paramMap.put("SUB_SCHEDULE_NAME", sched.getTitle()); //$NON-NLS-1$
        paramMap.put("SUB_SCHEDULE_REF", sched.getScheduleReference()); //$NON-NLS-1$
        paramMap.put("SUB_SCHEDULE_DESC", sched.getDescription()); //$NON-NLS-1$
        paramMap.put("SUB_NAME", sub.getTitle()); //$NON-NLS-1$
        paramMap.put("SUB_ID", sub.getId()); //$NON-NLS-1$

        paramMap.put("useContentRepository", Boolean.TRUE); //$NON-NLS-1$
        paramMap.put("content-handler-pattern", PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() + "GetContent?id={0}"); //$NON-NLS-1$ //$NON-NLS-2$

        paramMap.put("SUB_DESTINATION", sub.getDestination());

        try {
          SecurityHelper.runAsUser(sub.getUser(), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
              execute(jobName, paramMap, PentahoSessionHolder.getSession());
              return null;  
            }
          });
        } catch (Exception e) {
          throw new RuntimeException(e);  
        }
        
      } // end while loop
      if (isFinalFiring) {
        // done with this schedule, delete it from the Subscription Repository
        try {
          SubscriptionRepositoryHelper.deleteScheduleContentAndSubscription(subscriptionRepository, sched);
        } catch (SubscriptionRepositoryCheckedException e) {
          logger.error(Messages.getInstance().getErrorString("SubscriptionExecute.ERROR_0004_SCHEDULE_DELETE_FAILED", scheduleReference), e); //$NON-NLS-1$
        } catch (SubscriptionSchedulerException e) {
          logger.error(Messages.getInstance().getErrorString("SubscriptionExecute.ERROR_0004_SCHEDULE_DELETE_FAILED", scheduleReference), e); //$NON-NLS-1$
        }
      }
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  private void execute(final String jobName, final Map<String, Object> parametersMap, final IPentahoSession userSession) {
    try {
      LocaleHelper.setLocale(Locale.getDefault());
      logId = "Pro Subscription:" + jobName; //$NON-NLS-1$

      Date now = new Date();
      SubscriptionExecute.logger.info(Messages.getInstance().getString("SubscriptionExecute.INFO_TRIGGER_TIME", jobName, //$NON-NLS-1$ 
          DateFormat.getDateInstance().format(now), DateFormat.getTimeInstance().format(now)));

      String solutionName = (String) parametersMap.get("solution"); //$NON-NLS-1$
      String actionPath = (String) parametersMap.get("path"); //$NON-NLS-1$
      String actionName = (String) parametersMap.get("action"); //$NON-NLS-1$

      String subscriptionDestination = (String) parametersMap.get("SUB_DESTINATION");

      String instanceId = null;
      String processId = this.getClass().getName();

      if (solutionName == null) {
        error(Messages.getInstance().getErrorString("SubscriptionExecute.ERROR_0001_SOLUTION_NAME_MISSING")); //$NON-NLS-1$
        return;
      }
      if (actionPath == null) {
        error(Messages.getInstance().getErrorString("SubscriptionExecute.ERROR_0002_ACTION_PATH_MISSING")); //$NON-NLS-1$
        return;
      }
      if (actionName == null) {
        error(Messages.getInstance().getErrorString("SubscriptionExecute.ERROR_0003_ACTION_NAME_MISSING")); //$NON-NLS-1$
        return;
      }
      if (SubscriptionExecute.debug) {
        if (SubscriptionExecute.debug) {
          debug(Messages.getInstance().getString("SubscriptionExecute.DEBUG_EXECUTION_INFO", solutionName + "/" + actionPath + "/" + actionName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
      }

      boolean ignoreSubscriptionOutput = "true".equalsIgnoreCase((String) parametersMap.get("SUB_IGNORE_OUTPUT")); //$NON-NLS-1$ //$NON-NLS-2$

      String subscriptionId = (String) parametersMap.get("SUB_ID"); //$NON-NLS-1$
      String subscriptionName = (String) parametersMap.get("SUB_NAME"); //$NON-NLS-1$
      IOutputHandler outputHandler = null;
      if (ignoreSubscriptionOutput) {
        outputHandler = new SimpleOutputHandler((OutputStream) null, false);
      } else {
        String contentPath = SubscriptionHelper.getSubscriptionOutputLocation(solutionName, actionPath, actionName);
        outputHandler = new CoreContentRepositoryOutputHandler(contentPath, subscriptionId, solutionName, userSession);
        ((CoreContentRepositoryOutputHandler) outputHandler).setWriteMode(IContentItem.WRITEMODE_KEEPVERSIONS);
      }
      parametersMap.put("useContentRepository", Boolean.TRUE); //$NON-NLS-1$

      String contentUrlPattern = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
      if (!contentUrlPattern.endsWith("/")) { //$NON-NLS-1$
        contentUrlPattern += "/"; //$NON-NLS-1$
      }
      contentUrlPattern += "GetContent?id={0}"; //$NON-NLS-1$
      parametersMap.put("content-handler-pattern", contentUrlPattern); //$NON-NLS-1$
      SimpleParameterProvider parameterProvider = new SimpleParameterProvider(parametersMap);
      IParameterProvider sessionParams = new PentahoSessionParameterProvider(userSession);

      int lastDot = actionName.lastIndexOf('.');
      String type = actionName.substring(lastDot + 1);
      IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, userSession);
      IContentGenerator generator = pluginManager.getContentGeneratorForType(type, userSession);

      if (generator == null) {
        BaseRequestHandler requestHandler = new BaseRequestHandler(userSession, null, outputHandler, parameterProvider, null);
        requestHandler.setParameterProvider(IParameterProvider.SCOPE_SESSION, sessionParams);

        requestHandler.setInstanceId(instanceId);
        requestHandler.setProcessId(processId);
        requestHandler.setAction(actionPath, actionName);
        requestHandler.setSolutionName(solutionName);
        IRuntimeContext rt = null;
        try {
          rt = requestHandler.handleActionRequest(0, 0);
          if (isValidEmailAddress(subscriptionDestination)) {
            emailContent(outputHandler, subscriptionName, solutionName, actionName, instanceId, subscriptionDestination);
          } else if (!ignoreSubscriptionOutput && !outputHandler.isResponseExpected()) {
            if ((rt != null) && (rt.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS)) {
              StringBuffer buffer = new StringBuffer();
              PentahoSystem.get(IMessageFormatter.class, userSession).formatSuccessMessage("text/html", rt, buffer, false); //$NON-NLS-1$
              writeMessage(buffer.toString(), outputHandler, subscriptionName, solutionName, actionName, instanceId, userSession);
            } else {
              // we need an error message...
              StringBuffer buffer = new StringBuffer();
              PentahoSystem.get(IMessageFormatter.class, userSession).formatFailureMessage("text/html", rt, buffer, requestHandler.getMessages()); //$NON-NLS-1$
              writeMessage(buffer.toString(), outputHandler, subscriptionName, solutionName, actionName, instanceId, userSession);
            }
          }
        } finally {
          if (rt != null) {
            rt.dispose();
          }
        }
      } else {
        generator.setOutputHandler(outputHandler);
        generator.setItemName(actionName);
        generator.setInstanceId(instanceId);
        generator.setSession(userSession);
        Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
        parameterProviders.put(IParameterProvider.SCOPE_REQUEST, parameterProvider);
        parameterProviders.put(IParameterProvider.SCOPE_SESSION, new PentahoSessionParameterProvider(userSession));
        generator.setParameterProviders(parameterProviders);
        try {
          generator.createContent();
          // we succeeded
          if (isValidEmailAddress(subscriptionDestination)) {
            emailContent(outputHandler, subscriptionName, solutionName, actionName, instanceId, subscriptionDestination);
          } else if (!ignoreSubscriptionOutput && !outputHandler.isResponseExpected()) {
            String message = Messages.getInstance().getString("SubscriptionExecute.DEBUG_FINISHED_EXECUTION", jobName); //$NON-NLS-1$
            writeMessage(message.toString(), outputHandler, subscriptionName, solutionName, actionName, instanceId, userSession);
          }
        } catch (Exception e) {
          e.printStackTrace();
          // we need an error message...
          if (!ignoreSubscriptionOutput && !outputHandler.isResponseExpected()) {
            String message = Messages.getInstance().getString("PRO_SUBSCRIPTREP.EXCEPTION_WITH_SCHEDULE", jobName); //$NON-NLS-1$
            writeMessage(message.toString(), outputHandler, subscriptionName, solutionName, actionName, instanceId, userSession);
          }
        }

      }
      if (SubscriptionExecute.debug) {
        SubscriptionExecute.logger.debug(Messages.getInstance().getString("SubscriptionExecute.DEBUG_FINISHED_EXECUTION", jobName)); //$NON-NLS-1$
      }
    } catch (Throwable t) {
      SubscriptionExecute.logger.error("Error Executing Job", t); //$NON-NLS-1$
    }
  }

  protected void writeMessage(String message, IOutputHandler outputHandler, String subscriptionName, String solutionName, String fileName, String instanceId,
      IPentahoSession userSession) {
    IContentItem outputContentItem = outputHandler.getOutputContentItem(IOutputHandler.RESPONSE, IOutputHandler.CONTENT, subscriptionName, null, solutionName,
        instanceId, "text/html"); //$NON-NLS-1$
    outputContentItem.setMimeType("text/html"); //$NON-NLS-1$
    try {
      OutputStream os = outputContentItem.getOutputStream(fileName);
      os.write(message.getBytes(LocaleHelper.getSystemEncoding()));
      outputContentItem.closeOutputStream();
    } catch (IOException ex) {
      error(ex.getLocalizedMessage());
    }
  }

  protected boolean isValidEmailAddress(String destination) {
	  if (StringUtils.isEmpty(destination)) {
		  return false;
	  }
	  if (destination.contains("@")) {
	    return true;
	  }
	  return false;
  }
  
  protected void emailContent(IOutputHandler outputHandler, String subscriptionName, String solutionName, String fileName, String instanceId, String destination) {
    IContentItem outputContentItem = outputHandler.getOutputContentItem(IOutputHandler.RESPONSE, IOutputHandler.CONTENT, subscriptionName, null, solutionName,
        instanceId, null); //$NON-NLS-1$

    fileName = subscriptionName;

    if ("application/pdf".equals(outputContentItem.getMimeType())) {
      fileName += ".pdf";
    } else if ("text/html".equals(outputContentItem.getMimeType())) {
      fileName += ".html";
    } else if ("text/csv".equals(outputContentItem.getMimeType())) {
      fileName += ".csv";
    } else if ("application/vnd.ms-excel".equals(outputContentItem.getMimeType())) {
      fileName += ".xls";
    }

    SubscriptionEmailContent emailer = new SubscriptionEmailContent(outputContentItem.getDataSource(), fileName, subscriptionName, destination);

    if (!emailer.send()) {
      SubscriptionExecute.logger.error("Problem sending subscription email.");
    }
  }
}
