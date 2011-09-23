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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Aug 18, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.web.refactor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISubscription;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.api.scheduler.BackgroundExecutionException;
import org.pentaho.platform.api.scheduler.IJobDetail;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler.SchedulerHelper;
import org.pentaho.platform.uifoundation.component.xml.XmlComponent;
import org.pentaho.platform.web.http.messages.Messages;

public class UserFilesComponent extends XmlComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -7404173000559758744L;

  protected final static String FILE = "file"; //$NON-NLS-1$

  protected final static String NAME = "name"; //$NON-NLS-1$

  protected final static String TIMESTAMP = "timestamp"; //$NON-NLS-1$

  protected final static String ACTIONS = "actions"; //$NON-NLS-1$

  protected final static String ACTION = "action"; //$NON-NLS-1$

  protected final static String TITLE = "title"; //$NON-NLS-1$

  protected final static String PARAMS = "params"; //$NON-NLS-1$

  protected final static String PARAM = "param"; //$NON-NLS-1$

  protected final static String PARAM_NAME = "param-name"; //$NON-NLS-1$

  protected final static String PARAM_VALUE = "param-value"; //$NON-NLS-1$

  protected final static String USER_FILES = "user-files"; //$NON-NLS-1$

  protected final static String MIMETYPE = "mimetype"; //$NON-NLS-1$

  protected final static String SIZE = "size"; //$NON-NLS-1$

  protected HttpServletRequest request;

  protected HttpServletResponse response;

  private static final Log logger = LogFactory.getLog(UserFilesComponent.class);

  @Override
  public Log getLogger() {
    return UserFilesComponent.logger;
  }

  public UserFilesComponent() {
    super(null, null, null);
    setXsl("text/html", "UserFiles.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public UserFilesComponent(final IPentahoUrlFactory urlFactory, final HttpServletRequest request, final HttpServletResponse response, final List messages) {
    super(urlFactory, messages, null);
    this.request = request;
    this.response = response;
    setXsl("text/html", "UserFiles.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void setRequest(final HttpServletRequest request) {
    this.request = request;
  }

  public void setResponse(final HttpServletResponse response) {
    this.response = response;
  }

  @Override
  public boolean validate() {
    return true;
  }

  @Override
  public Document getXmlContent() {

    Document result = DocumentHelper.createDocument();
    Element root = result.addElement(UserFilesComponent.USER_FILES);

    addFiles(root);
    return result;
  }

  protected void addFiles(final Element root) {

    addScheduledAndExecuting(root);
    addExecutedlist(root);
    addSubscriptions(root);
  }

  protected void addScheduledAndExecuting(final Element root) {
    Element jobs = root.addElement("scheduled"); //$NON-NLS-1$
    IPentahoSession session = getSession();
    IBackgroundExecution backgroundExecution = PentahoSystem.get(IBackgroundExecution.class, session);
    List<IJobDetail> jobsList = null;
    if (backgroundExecution != null) {
      try {
        jobsList = backgroundExecution.getScheduledAndExecutingBackgroundJobs(session);  
      } catch(BackgroundExecutionException bex) {
        jobsList = new ArrayList<IJobDetail>();
        Element errorRoot = root.addElement("error"); //$NON-NLS-1$
        errorRoot.addElement("error-message").setText(Messages.getInstance().getErrorString("UI.USER_ERROR_0003_NO_BACKGROUND_EXECUTION")); //$NON-NLS-1$//$NON-NLS-2$ 
      }
    } else {
      jobsList = new ArrayList<IJobDetail>();
      Element errorRoot = root.addElement("error"); //$NON-NLS-1$
      errorRoot.addElement("error-message").setText(Messages.getInstance().getErrorString("UI.USER_ERROR_0003_NO_BACKGROUND_EXECUTION")); //$NON-NLS-1$//$NON-NLS-2$ 
    }
    if ((jobsList != null) && (jobsList.size() > 0)) {
      for (IJobDetail jobDetail : jobsList) {
        Element job = jobs.addElement(UserFilesComponent.FILE);
        job.addElement(NAME).setText(jobDetail.getActionName() != null ? jobDetail.getActionName() : jobDetail.getName());
        job.addElement(TIMESTAMP).setText(jobDetail.getSubmissionDate() != null?jobDetail.getSubmissionDate():(new Date()).toString());
        Element actions = job.addElement(ACTIONS);
        Element action = actions.addElement(ACTION);
        action.addElement(TITLE).setText(Messages.getInstance().getString("UI.USER_CANCEL")); //$NON-NLS-1$
        Element params = action.addElement(PARAMS);
        Element param = params.addElement(PARAM);
        param.addElement(PARAM_NAME).setText("del-job-name"); //$NON-NLS-1$
        param.addElement(PARAM_VALUE).setText(jobDetail.getName());
        param = params.addElement(PARAM);
        param.addElement(PARAM_NAME).setText("del-job-group"); //$NON-NLS-1$
        param.addElement(PARAM_VALUE).setText(jobDetail.getGroupName());
        param = params.addElement(PARAM);
        param.addElement(PARAM_NAME).setText("action"); //$NON-NLS-1$
        param.addElement(PARAM_VALUE).setText("cancel-job"); //$NON-NLS-1$
      }
    }
  }

  protected void addExecutedlist(final Element root) {
    Element jobs = root.addElement("executed"); //$NON-NLS-1$
    IPentahoSession session = getSession();
    IBackgroundExecution backgroundExecution = PentahoSystem.get(IBackgroundExecution.class, session);

    List<IContentItem> pastExecutionList = null;
    if (backgroundExecution != null) {
      pastExecutionList = backgroundExecution.getBackgroundExecutedContentList(session);
    } else {
      pastExecutionList = new ArrayList<IContentItem>();
    }
    SimpleDateFormat fmt = new SimpleDateFormat();

    for (int i = 0; i < pastExecutionList.size(); i++) {
      IContentItem item = (IContentItem) pastExecutionList.get(i);
      Element job = jobs.addElement(UserFilesComponent.FILE);

      job.addElement(UserFilesComponent.NAME).setText(item.getTitle());
      String dateStr = ""; //$NON-NLS-1$
      Date time = item.getFileDateTime();
      if (time != null) {
        dateStr = fmt.format(time);
      }
      job.addElement(UserFilesComponent.TIMESTAMP).setText(dateStr);
      job.addElement(UserFilesComponent.MIMETYPE).setText(item.getMimeType());
      job.addElement(UserFilesComponent.SIZE).setText(Long.toString(item.getFileSize()));

      Element actions = job.addElement(UserFilesComponent.ACTIONS);
      Element action = actions.addElement(UserFilesComponent.ACTION);
      action.addElement(UserFilesComponent.TITLE).setText(Messages.getInstance().getString("UI.USER_VIEW")); //$NON-NLS-1$
      Element params = action.addElement(UserFilesComponent.PARAMS);
      Element param = params.addElement(UserFilesComponent.PARAM);
      param.addElement(UserFilesComponent.PARAM_NAME).setText("action"); //$NON-NLS-1$
      param.addElement(UserFilesComponent.PARAM_VALUE).setText("view"); //$NON-NLS-1$

      param = params.addElement(UserFilesComponent.PARAM);
      param.addElement(UserFilesComponent.PARAM_NAME).setText("id"); //$NON-NLS-1$
      param.addElement(UserFilesComponent.PARAM_VALUE).setText(item.getId());

      action = actions.addElement(UserFilesComponent.ACTION);
      action.addElement(UserFilesComponent.TITLE).setText(Messages.getInstance().getString("UI.USER_DELETE")); //$NON-NLS-1$
      params = action.addElement(UserFilesComponent.PARAMS);
      param = params.addElement(UserFilesComponent.PARAM);
      param.addElement(UserFilesComponent.PARAM_NAME).setText("action"); //$NON-NLS-1$
      param.addElement(UserFilesComponent.PARAM_VALUE).setText("delete"); //$NON-NLS-1$

      param = params.addElement(UserFilesComponent.PARAM);
      param.addElement(UserFilesComponent.PARAM_NAME).setText("content-id"); //$NON-NLS-1$
      param.addElement(UserFilesComponent.PARAM_VALUE).setText(item.getId());

    }
  }

  public boolean cancelJob(final String jobName, final String jobGroup) {
    try {
      SchedulerHelper.deleteJob(getSession(), jobName, jobGroup);
      return true;
    } catch (Throwable t) {
      error(Messages.getInstance().getErrorString("Scheduler.ERROR_0001_SCHEDULER_CANNOT_CANCEL", t.getMessage()), t); //$NON-NLS-1$
    }
    return false;
  }

  public boolean deleteContent(final String contentId) {
    try {
      IPentahoSession session = getSession();
      IBackgroundExecution backgroundExecution = PentahoSystem.get(IBackgroundExecution.class, session);
      if (backgroundExecution != null) {
        backgroundExecution.removeBackgroundExecutedContentForID(contentId, session);
        return true;
      } else {
        return false;
      }
    } catch (Throwable t) {
      error(Messages.getInstance().getErrorString("Scheduler.ERROR_0001_SCHEDULER_CANNOT_CANCEL", t.getMessage()), t); //$NON-NLS-1$
    }
    return false;
  }

  protected void addSubscriptions(final Element root) {
    SubscriptionAdminUIComponent admin = null;
    try {
      admin = new SubscriptionAdminUIComponent(urlFactory, getMessages()); //$NON-NLS-1
      // admin.setLoggingLevel( admin.DEBUG );
      admin.validate(getSession(), null);
      SimpleParameterProvider params = new SimpleParameterProvider();

      admin.setParameterProvider(IParameterProvider.SCOPE_REQUEST, params);

      params.setParameter(SubscriptionAdminUIComponent.SCHEDULER_ACTION, SubscriptionAdminUIComponent.ACTION_SUBSCRIPTION_SHOW_LIST);
      params.setParameter("user", getSession().getName()); //$NON-NLS-1$

      Document doc = admin.getXmlContent();

      List subscriptionList = doc.selectNodes("listSubscriptions/subscriptions/subscription"); //$NON-NLS-1$
      if (subscriptionList != null) {
        ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getSession());
        Iterator it = subscriptionList.iterator();
        while (it.hasNext()) {
          Element node = (Element) it.next();
          String actionRef = node.selectSingleNode("actionRef").getText(); //$NON-NLS-1$
          ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, getSession());
          ActionInfo actionInfo = ActionInfo.parseActionString(actionRef);
          IActionSequence action = repo.getActionSequence(actionInfo.getSolutionName(), actionInfo.getPath(), actionInfo.getActionName(), repo
              .getLoggingLevel(), ISolutionRepository.ACTION_EXECUTE);
          String actionTitle = action.getTitle();
          node.addElement("action-title").setText(actionTitle); //$NON-NLS-1$
          if (subscriptionRepository != null) {
            try {
              ISubscription subscription = subscriptionRepository.getSubscription(node.selectSingleNode("@subscriptionId").getText(), getSession()); //$NON-NLS-1$
              subscriptionRepository.addSubscriptionToDocument(subscription, node, null, getSession());
              // subscriptionRepository.addSubscriptionsToDocument(getSession().getName(), actionRef, node, null, getSession());
            } catch (Throwable t) {
              error(Messages.getInstance().getErrorString("PRO_SUBSCRIPTREP.ERROR_0005_GENERAL_ERROR"), t); //$NON-NLS-1$
            }
          }
        }
      }

      Element subsRoot = doc.getRootElement();
      subsRoot.detach();
      root.add(subsRoot);

    } catch (Exception e) {
      error(Messages.getInstance().getErrorString("PRO_SUBSCRIPTREP.ERROR_0005_GENERAL_ERROR"), e); //$NON-NLS-1$
    }
  }

}
