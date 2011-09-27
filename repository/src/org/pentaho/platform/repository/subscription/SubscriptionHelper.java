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
 * 
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Oct 15, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.repository.subscription;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IParameterSetter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentItemFile;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscription;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.api.scheduler.BackgroundExecutionException;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SimpleParameterSetter;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;

public class SubscriptionHelper {

  protected static final Log logger = LogFactory.getLog(SubscriptionHelper.class);

  public static void editSubscription(final String subscriptionName, final IPentahoSession session, final SimpleUrlFactory urlFactory,
      final OutputStream outputStream) {

    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session);
    ISubscription subscription = subscriptionRepository.getSubscription(subscriptionName, session);
    if (subscription == null) {
      // TODO surface an error
      return;
    }
    ISubscribeContent content = subscription.getContent();
    Map contentParameters = content.getParameters();
    SimpleParameterSetter parameters = new SimpleParameterSetter();
    parameters.setParameters(contentParameters);
    SubscriptionHelper.getSubscriptionParameters(subscriptionName, parameters, session);
    // SimpleParameterProvider parameterProvider = new SimpleParameterProvider(
    // contentParameters );

    // Proposed fix for bug BISERVER-97 by Ezequiel Cuellar
    // Changed to set parameterXsl from the value specified specified in the Pentaho.xml tag "default-parameter-xsl"

    // Proposed fix for bug BISERVER-238 by Ezequiel Cuellar
    // Took away reference to SubscribeForm.xsl and changed it to DefaultParameterForm.xsl
    ISystemSettings systemSettings = PentahoSystem.getSystemSettings();
    String defaultParameterXsl = systemSettings.getSystemSetting("default-parameter-xsl", "DefaultParameterForm.xsl"); //$NON-NLS-1$ //$NON-NLS-2$

    ISolutionEngine solutionEngine = (ISolutionEngine) PentahoSystem.get(ISolutionEngine.class, session);
    solutionEngine.setLoggingLevel(PentahoSystem.loggingLevel);
    solutionEngine.init(session);
    solutionEngine.setParameterXsl(defaultParameterXsl);
    solutionEngine.setForcePrompt(true);

    Map parameterProviderMap = new HashMap();
    parameterProviderMap.put("PRO_EDIT_SUBSCRIPTION", parameters); //$NON-NLS-1$  
    PentahoSessionParameterProvider sessionParameters = new PentahoSessionParameterProvider(session);
    parameterProviderMap.put(IParameterProvider.SCOPE_SESSION, sessionParameters);

    SimpleOutputHandler outputHandler = null;
    if (outputStream != null) {
      outputHandler = new SimpleOutputHandler(outputStream, true);
    }

    outputHandler.setOutputPreference(IOutputHandler.OUTPUT_TYPE_PARAMETERS);
    ArrayList messages = new ArrayList();
    IRuntimeContext runtime = null;
    String instanceId = null;
    boolean persisted = false;

    try {
      runtime = solutionEngine.execute(content.getActionReference(),
          "Subscriptions", false, true, instanceId, persisted, parameterProviderMap, outputHandler, null, urlFactory, messages); //$NON-NLS-1$
    } finally {
      runtime.dispose();
    }
  }

  /*
   * The regular save subscription
   */
  public static String saveSubscription(final IParameterProvider parameterProvider, final String actionReference, final IPentahoSession userSession) {
    return SubscriptionHelper.saveSubscription(parameterProvider, actionReference, userSession, false);
  }

  /*
   * Pass in boolean true to save subscription information without requiring a content to be defined - Used for JPovot views
   */
  public static String saveSubscription(final IParameterProvider parameterProvider, final String actionReference, final IPentahoSession userSession,
      final boolean saveOnly) {

    if ((userSession == null) || (userSession.getName() == null)) {
      return Messages.getInstance().getString("SubscriptionHelper.USER_LOGIN_NEEDED"); //$NON-NLS-1$
    }

    String subscriptionId = (String) parameterProvider.getParameter("subscribe-id"); //$NON-NLS-1$
    boolean editing = (subscriptionId != null) && (subscriptionId.length() > 0);
    String subscriptionName = (String) parameterProvider.getParameter("subscribe-name"); //$NON-NLS-1$

    String destination = parameterProvider.getStringParameter("destination", null); //$NON-NLS-1$
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, userSession);

    if (!editing) {
      boolean isUniqueName = subscriptionRepository.checkUniqueSubscriptionName(subscriptionName, userSession.getName(), actionReference);
      if (!isUniqueName) {
        return Messages.getInstance().getString("SubscriptionHelper.USER_SUBSCRIPTION_NAME_ALREADY_EXISTS", subscriptionName); //$NON-NLS-1$
      }
    }

    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    ActionInfo contentInfo = ActionInfo.parseActionString(actionReference);
    IActionSequence actionSequence = solutionRepository.getActionSequence(contentInfo.getSolutionName(), contentInfo.getPath(), contentInfo.getActionName(),
        PentahoSystem.loggingLevel, ISolutionRepository.ACTION_SUBSCRIBE);
    if (actionSequence == null) {
      // TODO log an error
      return Messages.getInstance().getString("SubscriptionHelper.USER_SUBSCRIPTION_NOT_CREATE"); //$NON-NLS-1$
    }
    Map inputs = actionSequence.getInputDefinitionsForParameterProvider(IParameterProvider.SCOPE_REQUEST);
    ISubscribeContent content = subscriptionRepository.getContentByActionReference(actionReference);
    if (content == null) {
      if (saveOnly) {
        String subContId = UUIDUtil.getUUIDAsString();
        content = new SubscribeContent(subContId, actionReference, SubscribeContent.TYPE_REPORT);
        subscriptionRepository.addContent(content);
      } else {
        return (Messages.getInstance().getString("SubscriptionHelper.ACTION_SEQUENCE_NOT_ALLOWED", contentInfo.getActionName())); //$NON-NLS-1$
      }
    }

    HashMap parameters = new HashMap();

    // we need to grab the parameters
    // TODO load the action sequence from the repository so we can limit this
    // to just the required parameters or the action name
    Iterator inputsIterator = inputs.keySet().iterator();
    while (inputsIterator.hasNext()) {
      String parameterName = (String) inputsIterator.next();
      Object parameterValue = parameterProvider.getParameter(parameterName);
      if (parameterValue != null) {
        parameters.put(parameterName, parameterValue);
      }
    }

    // Just in case it's a PivotView Subscription
    if (saveOnly) {
      String parameterValue = (String) parameterProvider.getParameter("mdx"); //$NON-NLS-1$
      if (parameterValue != null) {
        parameters.put("mdx", parameterValue); //$NON-NLS-1$
      }
    }

    String parameterValue = (String) parameterProvider.getParameter("action2"); //$NON-NLS-1$
    if (parameterValue != null) {
      parameters.put("action", parameterValue); //$NON-NLS-1$
    }
    ISubscription subscription = null;
    if (editing) {
      // update an existing subscription
      subscription = subscriptionRepository.getSubscription(subscriptionId, userSession);
      if (subscription == null) {
        editing = false;
      } else {
        subscription.setTitle(subscriptionName);
        subscription.setDestination(destination);
        subscription.getParameters().clear();
        subscription.getParameters().putAll(parameters);
        subscription.getSchedules().clear();
      }
    }
    if (!editing) {
      // create a new subscription
      subscriptionId = UUIDUtil.getUUIDAsString();
      subscription = new Subscription(subscriptionId, userSession.getName(), subscriptionName, content, destination, Subscription.TYPE_PERSONAL, parameters);
    }

    // now add the schedules
    List schedules = subscriptionRepository.getSchedules();
    for (int i = 0; i < schedules.size(); i++) {
      ISchedule schedule = (ISchedule) schedules.get(i);
      String scheduleId = schedule.getId();
      String scheduleValue = (String) parameterProvider.getParameter("schedule-" + scheduleId); //$NON-NLS-1$
      if ("true".equals(scheduleValue)) { //$NON-NLS-1$
        subscription.addSchedule(schedule);
      }
    }

    if (subscriptionRepository.addSubscription(subscription)) {
      return Messages.getInstance().getString("SubscriptionHelper.USER_SUBSCRIPTION_CREATED"); //$NON-NLS-1$
    } else {
      // TODO log an error
      return Messages.getInstance().getString("SubscriptionHelper.USER_SUBSCRIPTION_NOT_CREATE"); //$NON-NLS-1$
    }
  }

  /*
   * This method doesn't have any callers, and it introduces un-needed dependencies. So, it's being removed.
   * 
   * public static boolean scheduleSubscription(final ISubscription subscription) { IScheduler scheduler = PentahoSystem.getScheduler(); return
   * scheduler.scheduleSubscription(subscription); }
   */
  public static String deleteSubscription(final String subscriptionId, final IPentahoSession userSession) {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, userSession);

    ISubscription subscription = subscriptionRepository.getSubscription(subscriptionId, userSession);
    if (subscription == null) {
      return Messages.getInstance().getString("SubscriptionHelper.USER_SUBSCRIPTION_NOT_DELETED"); //$NON-NLS-1$
    }

    try {
      subscriptionRepository.deleteSubscription(subscriptionId, userSession);
      return Messages.getInstance().getString("SubscriptionHelper.USER_SUBSCRIPTION_DELETED"); //$NON-NLS-1$
    } catch (Exception e) {
      return Messages.getInstance().getString("SubscriptionHelper.USER_SUBSCRIPTION_NOT_DELETED"); //$NON-NLS-1$
    }
  }

  public static void runSubscription(final String subscriptionName, final IPentahoSession session, final IParameterProvider sessionParameters,
      final SimpleUrlFactory urlFactory, final IOutputHandler outputHandler) {
    try {
      ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session);
      ISubscription subscription = subscriptionRepository.getSubscription(subscriptionName, session);
      if (subscription == null) {
        // TODO surface an error
        return;
      }
      ISubscribeContent content = subscription.getContent();
      Map contentParameters = content.getParameters();
      Map subscriptionParameters = subscription.getParameters();
      ActionInfo contentInfo = ActionInfo.parseActionString(content.getActionReference());
      subscriptionParameters.put("solution", contentInfo.getSolutionName()); //$NON-NLS-1$
      subscriptionParameters.put("path", contentInfo.getPath()); //$NON-NLS-1$
      subscriptionParameters.put("action", contentInfo.getActionName()); //$NON-NLS-1$

      SimpleParameterProvider parameterProvider = new SimpleParameterProvider(contentParameters);
      parameterProvider.setParameters(subscriptionParameters);

      ArrayList messages = new ArrayList();
      String instanceId = null;
      boolean persisted = false;

      String actionName = contentInfo.getActionName();
      int lastDot = actionName.lastIndexOf('.');
      String type = actionName.substring(lastDot + 1);

      IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, session);
      IContentGenerator generator = pluginManager.getContentGeneratorForType(type, session);

      if (generator == null) {
        IRuntimeContext runtime = null;
        try {
          ISolutionEngine solutionEngine = PentahoSystem.get(ISolutionEngine.class, session);
          solutionEngine.setLoggingLevel(PentahoSystem.loggingLevel);
          solutionEngine.init(session);
          Map parameterProviderMap = new HashMap();
          parameterProviderMap.put(IParameterProvider.SCOPE_REQUEST, parameterProvider);
          parameterProviderMap.put(IParameterProvider.SCOPE_SESSION, sessionParameters);
          runtime = solutionEngine.execute(content.getActionReference(),
              "Subscriptions", false, true, instanceId, persisted, parameterProviderMap, outputHandler, null, urlFactory, messages); //$NON-NLS-1$
          // see if we need to provide feedback to the caller

          if (!outputHandler.contentDone()) {
            IContentItem contentItem = outputHandler.getFeedbackContentItem();
            OutputStream outputStream = contentItem.getOutputStream(subscriptionName);

            if (runtime == null) {
              // we need an error message...
              StringBuffer buffer = new StringBuffer();
              PentahoSystem.get(IMessageFormatter.class, session).formatFailureMessage("text/html", runtime, buffer); //$NON-NLS-1$
              outputStream.write(buffer.toString().getBytes());
              contentItem.closeOutputStream();
            } else if (runtime.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS) {
              StringBuffer buffer = new StringBuffer();
              PentahoSystem.get(IMessageFormatter.class, session).formatSuccessMessage("text/html", runtime, buffer, false); //$NON-NLS-1$
              outputStream.write(buffer.toString().getBytes());
              contentItem.closeOutputStream();
            } else {
              // we need an error message...
              StringBuffer buffer = new StringBuffer();
              PentahoSystem.get(IMessageFormatter.class, session).formatFailureMessage("text/html", runtime, buffer); //$NON-NLS-1$
              outputStream.write(buffer.toString().getBytes());
              contentItem.closeOutputStream();
            }
          }
        } catch (Throwable t) {
        } finally {
          if (runtime != null) {
            runtime.dispose();
          }
        }
      } else {
        // we have a generator
        generator.setOutputHandler(outputHandler);
        generator.setItemName(actionName);
        generator.setInstanceId(instanceId);
        generator.setSession(session);
        Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
        parameterProviders.put(IParameterProvider.SCOPE_REQUEST, parameterProvider);
        parameterProviders.put(IParameterProvider.SCOPE_SESSION, new PentahoSessionParameterProvider(session));
        generator.setParameterProviders(parameterProviders);
        try {
          generator.createContent();
          // we succeeded
          if (!outputHandler.contentDone()) {
            String message = Messages.getInstance().getString("SubscriptionExecute.DEBUG_FINISHED_EXECUTION", subscriptionName); //$NON-NLS-1$
            writeMessage(message.toString(), outputHandler, subscriptionName, contentInfo.getSolutionName(), actionName, instanceId, session);
          }
        } catch (Exception e) {
          e.printStackTrace();
          // we need an error message...
          if (!outputHandler.contentDone()) {
            String message = Messages.getInstance().getString("PRO_SUBSCRIPTREP.EXCEPTION_WITH_SCHEDULE", subscriptionName); //$NON-NLS-1$
            writeMessage(message.toString(), outputHandler, subscriptionName, contentInfo.getSolutionName(), actionName, instanceId, session);
          }
        }
      }
    } catch (Throwable t) {
      logger.error("Error Executing Subscription", t); //$NON-NLS-1$
    }

  }

  protected static void writeMessage(String message, IOutputHandler outputHandler, String subscriptionName, String solutionName, String fileName,
      String instanceId, IPentahoSession userSession) {
    IContentItem outputContentItem = outputHandler.getOutputContentItem(IOutputHandler.RESPONSE, IOutputHandler.CONTENT, subscriptionName, null, solutionName,
        instanceId, "text/html"); //$NON-NLS-1$
    outputContentItem.setMimeType("text/html"); //$NON-NLS-1$
    try {
      OutputStream os = outputContentItem.getOutputStream(fileName);
      os.write(message.getBytes(LocaleHelper.getSystemEncoding()));
      outputContentItem.closeOutputStream();
    } catch (IOException ex) {
      logger.error(ex.getLocalizedMessage());
    }
  }

  public static void getArchived(final String subscriptionName, final String fileId, final IPentahoSession session, final IOutputHandler outputHandler) {

    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session);
    ISubscription subscription = subscriptionRepository.getSubscription(subscriptionName, session);
    if (subscription == null) {
      // TODO surface an error
      return;
    }
    IContentItem contentItem = subscriptionRepository.getContentItem(subscriptionName, session);
    List files = contentItem.getFileVersions();
    Iterator fileIterator = files.iterator();
    IContentItemFile file = null;
    while (fileIterator.hasNext()) {
      file = (IContentItemFile) fileIterator.next();
      if (fileId.equals(file.getId())) {
        break;
      }
    }

    try {
      IContentItem outoutContentItem = outputHandler.getOutputContentItem(IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, null, contentItem
          .getMimeType());
      OutputStream outputStream = outoutContentItem.getOutputStream(subscriptionName);
      if (file == null) {
        // TODO surface an error
        outputStream.write(Messages.getInstance().getString("SubscriptionHelper.USER_ARCHIVE_NOT_FOUND").getBytes()); //$NON-NLS-1$
        outoutContentItem.closeOutputStream();
        return;
      }
      outoutContentItem.setMimeType(contentItem.getMimeType());
      InputStream inputStream = file.getInputStream();
      byte buffer[] = new byte[2048];
      int n = inputStream.read(buffer);
      while (n > 0) {
        outputStream.write(buffer, 0, n);
        n = inputStream.read(buffer);
      }
      outoutContentItem.closeOutputStream();
    } catch (IOException e) {
      // TODO surface an error
      SubscriptionHelper.logger.error(null, e);
    }

  }

  public static String getSubscriptionParameters(final String subscriptionName, final IParameterSetter parameters, final IPentahoSession session) {

    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session);
    ISubscription subscription = subscriptionRepository.getSubscription(subscriptionName, session);
    if (subscription == null) {
      // TODO surface an error
      return Messages.getInstance().getString("SubscriptionHelper.USER_SUBSCRIPTION_DOES_NOT_EXIST"); //$NON-NLS-1$
    }

    // handle the parameters for the subscription
    Map subscriptionParameters = subscription.getParameters();
    Iterator parameterIterator = subscriptionParameters.keySet().iterator();
    while (parameterIterator.hasNext()) {
      String key = (String) parameterIterator.next();
      Object value = subscriptionParameters.get(key);
      parameters.setParameter(key, value);
    }

    // handle the main subscription info
    parameters.setParameter("subscribe-title", subscription.getTitle()); //$NON-NLS-1$
    parameters.setParameter("destination", subscription.getDestination()); //$NON-NLS-1$
    parameters.setParameter("action", subscription.getContent().getId()); //$NON-NLS-1$
    // parameters.setParameter( "editing", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
    // add the subscription id so that we can update the object when the user
    // saves
    parameters.setParameter("subscribe-id", subscription.getId()); //$NON-NLS-1$
    String actionReference = subscription.getContent().getActionReference();
    ActionInfo actionInfo = ActionInfo.parseActionString(actionReference);
    parameters.setParameter("solution", actionInfo.getSolutionName()); //$NON-NLS-1$
    parameters.setParameter("path", actionInfo.getPath()); //$NON-NLS-1$
    parameters.setParameter("action", actionInfo.getActionName()); //$NON-NLS-1$
    // parameters.setParameter( "subscribe", "edit" );
    // handle the schedules
    List schedules = subscription.getSchedules();
    ISchedule schedule;
    for (int i = 0; i < schedules.size(); i++) {
      schedule = (ISchedule) schedules.get(i);
      parameters.setParameter("schedule-" + schedule.getId(), "true"); //$NON-NLS-1$ //$NON-NLS-2$      
    }

    return null;
  }

  public static String deleteSubscriptionArchive(final String subscriptionName, final String fileId, final IPentahoSession session) {

    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session);
    ISubscription subscription = subscriptionRepository.getSubscription(subscriptionName, session);
    if (subscription == null) {
      // TODO surface an error
      return Messages.getInstance().getString("SubscriptionHelper.USER_SUBSCRIPTION_DOES_NOT_EXIST"); //$NON-NLS-1$
    }
    IContentItem contentItem = subscriptionRepository.getContentItem(subscriptionName, session);
    if (contentItem == null) {
      // TODO surface an error
      return Messages.getInstance().getString("SubscriptionHelper.USER_CONTENT_ITEM_DOES_NOT_EXIST"); //$NON-NLS-1$
    }
    contentItem.removeVersion(fileId);
    return Messages.getInstance().getString("SubscriptionHelper.USER_ARCHIVE_DELETED"); //$NON-NLS-1$

  }

  public static String createSubscriptionArchive(final String subscriptionName, final IPentahoSession session, final SimpleUrlFactory urlFactory,
      final IParameterProvider sessionParameters) throws BackgroundExecutionException {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session);
    ISubscription subscription = subscriptionRepository.getSubscription(subscriptionName, session);
    if (subscription == null) {
      // TODO surface an error
      return Messages.getInstance().getString("SubscriptionHelper.USER_SUBSCRIPTION_DOES_NOT_EXIST"); //$NON-NLS-1$
    }
    ISubscribeContent content = subscription.getContent();
    if (content == null) {
      // TODO surface an error
      return Messages.getInstance().getString("SubscriptionHelper.USER_CONTENT_ITEM_DOES_NOT_EXIST"); //$NON-NLS-1$
    }
    Map contentParameters = content.getParameters();
    Map subscriptionParameters = subscription.getParameters();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider(contentParameters);
    parameterProvider.setParameters(subscriptionParameters);
    if (subscriptionName != null) {
      parameterProvider.setParameter("subscribe-name", subscriptionName); //$NON-NLS-1$
    }
    ActionInfo contentInfo = ActionInfo.parseActionString(content.getActionReference());

    String actionName = (String) subscriptionParameters.get("action"); //$NON-NLS-1$
    if (actionName == null) {
      actionName = contentInfo.getActionName();
    }

    String path = ActionInfo.buildSolutionPath(contentInfo.getSolutionName(), contentInfo.getPath(), actionName);
    parameterProvider.setParameter(StandardSettings.ACTIONS_REF, path);
    // MB - Old code talked directly to quartz classes.
    IBackgroundExecution be = PentahoSystem.get(IBackgroundExecution.class, "BackgroundSubscriptionExecution", session); //$NON-NLS-1$
    return be.backgroundExecuteAction(session, parameterProvider);
  }

  public static String getSubscriptionOutputLocation(final String solutionName, final String actionPath, final String actionName) {
    String outputFolder = actionName.substring(0, actionName.lastIndexOf('.'));
    return solutionName + "/" + actionPath + "/" + outputFolder + "/subscriptions"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  /**
   * This formatter works with a date/time string with this format: May 21, 2008 8:29:21 PM
   * 
   * NOTE: the formatter cannot be shared across threads (since DateFormat implementations are not guaranteed to be thread safe) or across sessions (since
   * different sessions may have different locales). So create a new one an each call.
   * 
   * @return
   */
  public static DateFormat getDateTimeFormatter() {
    return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, LocaleHelper.getLocale());
  }
}
