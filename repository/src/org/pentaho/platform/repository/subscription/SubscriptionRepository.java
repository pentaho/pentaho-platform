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
 * @created Oct 7, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.repository.subscription;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISubscriptionScheduler;
import org.pentaho.platform.api.engine.SubscriptionSchedulerException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentItemFile;
import org.pentaho.platform.api.repository.IContentLocation;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscription;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.api.repository.SubscriptionRepositoryCheckedException;
import org.pentaho.platform.api.repository.SubscriptionRepositoryException;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;

public class SubscriptionRepository implements ISubscriptionRepository {
  private static final byte[] SyncLock = new byte[0];

  private static final Log logger = LogFactory.getLog(SubscriptionRepository.class);

  private static final ISubscriptionScheduler subscriptionScheduler = PentahoSystem.get(ISubscriptionScheduler.class, null);

  public Log getLogger() {
    return SubscriptionRepository.logger;
  }

  public SubscriptionRepository() {
    super();
  }

  // TODO sbarkdull, this needs to be modified to support repeat count, repeat interval, start date and end date.
  public Element importSchedules(final Document doc) {
    Element resultElement = DocumentHelper.createElement("importSchedulesResults"); //$NON-NLS-1$

    if (doc == null) {
      Element ele = resultElement
          .addElement("message").addText(Messages.getInstance().getString("PRO_SUBSCRIPTREP.DOCUMENT_IS_NULL")); //$NON-NLS-1$ //$NON-NLS-2$
      ele.addAttribute("result", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
      return (resultElement);
    }

    List scheduleNodes = doc.selectNodes("//schedules/*"); //$NON-NLS-1$
    if (scheduleNodes.size() == 0) {
      Element ele = resultElement
          .addElement("message").addText(Messages.getInstance().getString("PRO_SUBSCRIPTREP.NO_SCHEDULES_DEFINED")); //$NON-NLS-1$ //$NON-NLS-2$
      ele.addAttribute("result", "WARNING"); //$NON-NLS-1$ //$NON-NLS-2$
      return (resultElement);
    }

    synchronized (SubscriptionRepository.SyncLock) {

      Node scheduleNode = null;
      String schedName, schedDesc, schedRef, cronString, schedGroup;
      ISchedule aSchedule;
      // List schedList;

      try {
        SubscriptionRepository.logger.info(Messages.getInstance().getString(
            "PRO_SUBSCRIPTREP.USER_IMPORT_PROCESSING_SCHEDULES", Integer.toString(scheduleNodes.size()))); //$NON-NLS-1$
        for (int i = 0; i < scheduleNodes.size(); i++) {
          scheduleNode = (Node) scheduleNodes.get(i);
          schedRef = scheduleNode.selectSingleNode("@ref").getText(); //$NON-NLS-1$
          schedName = scheduleNode.selectSingleNode("@name").getText(); //$NON-NLS-1$
          schedDesc = scheduleNode.selectSingleNode("@description").getText(); //$NON-NLS-1$
          schedGroup = scheduleNode.selectSingleNode("@group").getText(); //$NON-NLS-1$
          cronString = scheduleNode.getText();

          try {
            aSchedule = getScheduleByScheduleReference(schedRef);
            if (aSchedule != null) {
              aSchedule.setCronString(cronString);
              aSchedule.setDescription(schedDesc);
              aSchedule.setGroup(schedGroup);
              aSchedule.setTitle(schedName);
              resultElement.addElement("modified").addText(schedRef); //$NON-NLS-1$
              SubscriptionRepository.logger.info(Messages.getInstance().getString(
                  "PRO_SUBSCRIPTREP.MODIFIED_SUBSCRIPTION_SCHEDULE", schedRef)); //$NON-NLS-1$
            } else {
              aSchedule = addCronSchedule(schedName, schedRef, schedDesc, cronString, schedGroup, null, null );
              resultElement.addElement("added").addText(schedRef); //$NON-NLS-1$
              SubscriptionRepository.logger.info(Messages.getInstance().getString(
                  "PRO_SUBSCRIPTREP.ADDED_SUBSCRIPTION_SCHEDULE", schedRef)); //$NON-NLS-1$
            }
            SubscriptionRepository.subscriptionScheduler.getCronSummary(cronString); // Throws an exception if invalid                        
          } catch (Exception e) {
            resultElement
                .addElement("message").addText(Messages.getInstance().getString("PRO_SUBSCRIPTREP.ERROR_OCCURRED_WITH_SCHEDULE", schedRef, e.getLocalizedMessage())); //$NON-NLS-1$ //$NON-NLS-2$
            SubscriptionRepository.logger.warn(
                Messages.getInstance().getString("PRO_SUBSCRIPTREP.EXCEPTION_WITH_SCHEDULE", schedRef), e); //$NON-NLS-1$
          }
        }

      } catch (Exception e) {
        Element ele = resultElement
            .addElement("message").addText(Messages.getInstance().getString("PRO_SUBSCRIPTREP.ERROR_PROCESSING_IMPORTS") + e.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        ele.addAttribute("result", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
        SubscriptionRepository.logger.error(Messages.getInstance().getString("PRO_SUBSCRIPTREP.EXCEPTION_PROCESSING_IMPORTS"), e); //$NON-NLS-1$
        return (resultElement);
      }
    }
    return (resultElement);
  }

  public Element importContent(final Document doc) {
    Element resultElement = DocumentHelper.createElement("importContentResults"); //$NON-NLS-1$

    if (doc == null) {
      Element ele = resultElement
          .addElement("message").addText(Messages.getInstance().getString("PRO_SUBSCRIPTREP.DOCUMENT_IS_NULL")); //$NON-NLS-1$ //$NON-NLS-2$
      ele.addAttribute("result", "ERROR"); //$NON-NLS-1$//$NON-NLS-2$
      return (resultElement);
    }

    List contentNodes = doc.selectNodes("//subscription-content/*"); //$NON-NLS-1$
    if (contentNodes.size() == 0) {
      Element ele = resultElement
          .addElement("message").addText(Messages.getInstance().getString("PRO_SUBSCRIPTREP.NO_SCHEDULES_DEFINED")); //$NON-NLS-1$ //$NON-NLS-2$
      ele.addAttribute("result", "WARNING"); //$NON-NLS-1$//$NON-NLS-2$
      return (resultElement);
    }

    synchronized (SubscriptionRepository.SyncLock) {

      Node contentNode = null;
      Node tempNode = null;
      String actionRef, contentType, allowAllSchedules;
      ISubscribeContent subscribeContent = null;

      List schedList = getSchedules();
      Map scheduleMap = new HashMap();
      Map groupMap = new HashMap();

      for (int j = 0; j < schedList.size(); ++j) {
        Schedule aSchedule = (Schedule) schedList.get(j);
        scheduleMap.put(aSchedule.getScheduleReference(), aSchedule);
        List groupList = (List) groupMap.get(aSchedule.getGroup());
        if (groupList == null) {
          groupList = new ArrayList();
          groupMap.put(aSchedule.getGroup(), groupList);
        }
        groupList.add(aSchedule);
      }

      try {
        SubscriptionRepository.logger.info(Messages.getInstance().getString(
            "PRO_SUBSCRIPTREP.USER_PROCESSING_CONTENT_NODES", Integer.toString(contentNodes.size()))); //$NON-NLS-1$
        for (int i = 0; i < contentNodes.size(); i++) {
          contentNode = (Node) contentNodes.get(i);
          actionRef = contentNode.selectSingleNode("@action").getText(); //$NON-NLS-1$
          contentType = contentNode.selectSingleNode("@type").getText(); //$NON-NLS-1$
          tempNode = contentNode.selectSingleNode("@allowAllSchedules"); //$NON-NLS-1$
          if (tempNode != null) {
            allowAllSchedules = tempNode.getText();
          } else {
            allowAllSchedules = "false"; //$NON-NLS-1$
          }

          try {
            subscribeContent = getContentByActionReference(actionRef);
            if (subscribeContent != null) {
              subscribeContent.setType(contentType);
              resultElement.addElement("modified").addText(actionRef); //$NON-NLS-1$
              SubscriptionRepository.logger.info(Messages.getInstance().getString(
                  "PRO_SUBSCRIPTREP.MODIFIED_SUBSCRIPTION_CONTENT", actionRef)); //$NON-NLS-1$
            } else {
              subscribeContent = addContent(actionRef, contentType);
              resultElement.addElement("added").addText(actionRef); //$NON-NLS-1$
              SubscriptionRepository.logger.info(Messages.getInstance().getString(
                  "PRO_SUBSCRIPTREP.ADDED_SUBSCRIPTION_CONTENT", actionRef)); //$NON-NLS-1$
            }

          } catch (Exception e) {
            resultElement
                .addElement("message").addText(Messages.getInstance().getString("PRO_SUBSCRIPTREP.ERROR_WITH_CONTENT", actionRef, e.getLocalizedMessage())); //$NON-NLS-1$ //$NON-NLS-2$ 
            SubscriptionRepository.logger.warn(
                Messages.getInstance().getString("PRO_SUBSCRIPTREP.ERROR_WITH_CONTENT_LOG", actionRef), e); //$NON-NLS-1$ 
          }

          List contentSchedules = new ArrayList();
          if ("true".equalsIgnoreCase(allowAllSchedules)) { //$NON-NLS-1$
            contentSchedules.addAll(schedList);
          } else {

            List suppliedSchedules = contentNode.selectNodes("schedule"); //$NON-NLS-1$
            if (suppliedSchedules != null) {
              for (int j = 0; j < suppliedSchedules.size(); j++) {
                tempNode = (Node) suppliedSchedules.get(j);
                String schName = tempNode.getText();
                if (schName != null) {
                  Object aSchedule = scheduleMap.get(schName);
                  if (aSchedule != null) {
                    contentSchedules.add(aSchedule);
                  }
                }
              }
            }

            List suppliedGroups = contentNode.selectNodes("group"); //$NON-NLS-1$
            if (suppliedGroups != null) {
              for (int j = 0; j < suppliedGroups.size(); j++) {
                tempNode = (Node) suppliedGroups.get(j);
                String grpName = tempNode.getText();
                if (grpName != null) {
                  List groupList = (List) groupMap.get(grpName);
                  if (groupList != null) {
                    contentSchedules.addAll(groupList);
                  }
                }
              }
            }
          }
          HibernateUtil.beginTransaction(); // Need to do this or the schedules don't get saved if the content item is new
          subscribeContent.setSchedules(contentSchedules);
          HibernateUtil.commitTransaction();
        }
      } catch (Exception e) {
        Element ele = resultElement
            .addElement("message").addText(Messages.getInstance().getString("PRO_SUBSCRIPTREP.ERROR_PROCESSING_IMPORTS") + e.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        ele.addAttribute("result", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
        SubscriptionRepository.logger.error(Messages.getInstance().getString("PRO_SUBSCRIPTREP.EXCEPTION_PROCESSING_IMPORTS"), e); //$NON-NLS-1$
        return (resultElement);
      }

    }
    return (resultElement);
  }

  public boolean addSubscription(final ISubscription subscription) {
    if ((subscription.getId() != null) && (subscription.getTitle() != null)) {
      // commented by sbarkdull HibernateUtil.beginTransaction();
      Session session = HibernateUtil.getSession();
      session.save(subscription);
      // subscriptionMap.put( subscription.getId(), subscription );
      // commented by sbarkdull HibernateUtil.commitTransaction();
      return true;
    } else {
      // TODO log an error
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public List<ISubscription> getSubscriptionsForSchedule(final ISchedule schedule) {
    if (schedule == null) {
      return new ArrayList<ISubscription>();
    }
    Session session = HibernateUtil.getSession();
    Query qry = session.getNamedQuery(
        "org.pentaho.platform.repository.subscription.Schedule.findSubscriptionsBySchedule").setCacheable(true); //$NON-NLS-1$
    qry.setEntity("schedule", schedule); //$NON-NLS-1$
    return qry.list();
  }

  public List<ISubscription> getSubscriptionsForSchedule(final String scheduleId) {
    return getSubscriptionsForSchedule(this.getScheduleByScheduleReference(scheduleId));
  }

  public IPentahoResultSet getSubscriptions(final String scheduleId, final IPentahoSession session,
      final String solution, final String path, final String action, final String parameterNames[]) {
    IPentahoResultSet results = new SubscriptionResultSet(scheduleId, session, parameterNames, solution, path, action);
    return results;
  }

  public void addContent(final ISubscribeContent content) {
    if (getContentByActionReference(content.getActionReference()) != null) {
      throw new SubscriptionRepositoryException(Messages.getInstance().getString(
          "PRO_SUBSCRIPTREP.ACTION_SEQUENCE_ALREADY_EXISTS", content.getActionReference())); //$NON-NLS-1$ 
    }

// commented by sbarkdull     HibernateUtil.beginTransaction();
    Session session = HibernateUtil.getSession();
    session.save(content);
// commented by sbarkdull     HibernateUtil.commitTransaction();
    clearSessionCaches();
  }
  
  public void deleteContent(final ISubscribeContent content) {
//  commented by wseyler    HibernateUtil.beginTransaction();
    Session session = HibernateUtil.getSession();
    session.delete(content);
//  commented by wseyler    HibernateUtil.commitTransaction();
    clearSessionCaches();   
  }

  private void clearSessionCaches() {
    ICacheManager cm = PentahoSystem.getCacheManager( null ); // TODO sbarkdull, need to get real session in here
    if (cm != null) {
      //cm.killSessionCaches();
      // TODO Need to find a way for kill all session caches
      cm.killSessionCaches();
    }
  }

  public ISubscribeContent addContent(final String actionRef, final String contentType) {
    ISubscribeContent newContent = new SubscribeContent(UUIDUtil.getUUIDAsString(), actionRef, contentType);
    addContent(newContent);
    return (newContent);
  }

  /**
   * For each action sequence path in the actionRefs array, if there is not already
   * content in the subscription repository for that action sequence, add it. If the
   * content is already in the subscription repository, do nothing. 
   * Return the a list of the content that is associated with each action sequence
   * in the actionRefs parameter, regardless of if it was newly added, or already
   * existed.
   */
  public List<ISubscribeContent> addContent(final String actionRefs[]) throws SubscriptionRepositoryCheckedException {

    List<ISubscribeContent> newContents = new ArrayList<ISubscribeContent>();
    List<ISubscribeContent> allContent = getAllContent();
    Map<String, ISubscribeContent> existingContentActionRef = new HashMap<String, ISubscribeContent>();
    for ( ISubscribeContent content : allContent ) {
      existingContentActionRef.put( content.getActionReference(), content );
    }
    for ( String actionRef : actionRefs ) {
      if ( !existingContentActionRef.containsKey( actionRef ) ) {
        ISubscribeContent newContent = addContent( actionRef, "" ); //$NON-NLS-1$
        newContents.add( newContent );
      } else {
        newContents.add( existingContentActionRef.get( actionRef) );
      }
    }
    return newContents;
  }

  /**
   * If an action sequence currently in the subscription repository is not
   * in the actionRefs array parameter, remove it from the subscription repository.
   * If an action sequence in the actionRefs array is not already in the subscription
   * repository, add it to the subscription repository. If the action sequence in the
   * actionRefs array is already in the subscription repository, do nothing.
   */
  public void setContent(final String actionRefs[]) throws Exception {
    List<String> keepList = new ArrayList<String>(Arrays.asList(actionRefs)); // Strings
    List<ISubscribeContent> allContent = getAllContent();
    List<ISubscribeContent> deleteList = new ArrayList<ISubscribeContent>(); // SubscribeContent
    for ( ISubscribeContent content : allContent ) {
      if (keepList.contains(content.getActionReference())) {
        keepList.remove(content.getActionReference());
      } else {
        deleteList.add(content);
      }
    }

    for (int i = 0; i < keepList.size(); ++i) {
      addContent((String) keepList.get(i), ""); //$NON-NLS-1$
    }

    for (int i = 0; i < deleteList.size(); ++i) {
      deleteSubscribeContent((SubscribeContent) deleteList.get(i));
    }
  }

  public ISubscribeContent editContent(final String contentId, final String actionRef, final String contentType) {

    ISubscribeContent oldCont = getContentById(contentId);
    if (oldCont != null) {
      // Any null values keep previous values
      if (actionRef != null) {
        oldCont.setActionReference(actionRef);
      }
      if (contentType != null) {
        oldCont.setType(contentType);
      }
      return (oldCont);
    }

    return (null);
  }

  public boolean deleteContentForSchedule(final String contentId, final String schedId) throws SubscriptionRepositoryCheckedException {
    ISubscribeContent content = getContentById(contentId);
    ISchedule sched = getSchedule(schedId);
    return (deleteContentForSchedule(content, sched));
  }

  // maybe this should be called deleteScheduleFromContent?
  public boolean deleteContentForSchedule(final ISubscribeContent content, final ISchedule sched) throws SubscriptionRepositoryCheckedException {
    if (content == null) {
      return (false);
    }
    return (content.removeSchedule(sched));
  }

  public boolean addContentForSchedule(final String contentId, final String schedId) throws SubscriptionRepositoryCheckedException {
    ISubscribeContent content = getContentById(contentId);
    ISchedule sched = getSchedule(schedId);
    return (addContentForSchedule(content, sched));
  }

  public boolean addContentForSchedule(final ISubscribeContent content, final ISchedule sched) throws SubscriptionRepositoryCheckedException {
    if (content == null) {
      return (false);
    }
    content.addSchedule(sched);
    return (true);
  }

  public void setSchedulesForContent(final String scheduleId[], final String contentId) throws Exception {
    ISubscribeContent content = getContentById(contentId);

    List schedList = new ArrayList();
    for (String element : scheduleId) {
      ISchedule sched = getSchedule(element);
      if (sched != null) {
        schedList.add(sched);
      }
    }

    content.setSchedules(schedList);
  }

  public void addContentForSchedule(final String contentIds[], final String schedId) throws SubscriptionRepositoryCheckedException {
    ISchedule sched = getSchedule(schedId);

    for (String contentId : contentIds) {
      ISubscribeContent content = getContentById(contentId);
      addContentForSchedule(content, sched);
    }
  }

  public void setContentForSchedule(final String contentIds[], final String schedId) throws Exception {
    ISchedule sched = getSchedule(schedId);

    List<ISubscribeContent> allContent = getAllContent();
    for (String contentId : contentIds) {
      ISubscribeContent content = getContentById(contentId);
      addContentForSchedule(content, sched);
      allContent.remove(content);
    }

    for (int i = 0; i < allContent.size(); ++i) {
      SubscribeContent content = (SubscribeContent) allContent.get(i);
      deleteContentForSchedule(content, sched);
    }
  }

  public ISubscribeContent getContentById(final String theId) {
    Session session = HibernateUtil.getSession();
    try {
      return (ISubscribeContent) session.get(SubscribeContent.class, theId);
    } catch (HibernateException ex) {
      throw new SubscriptionRepositoryException(Messages.getInstance().getErrorString(
          "PRO_SUBSCRIPTREP.ERROR_0001_GETTING_SUBSCRIPTION", theId), ex); //$NON-NLS-1$
    }
  }

  @SuppressWarnings("unchecked")
  public List<ISubscribeContent> getAllContent() {
    Session session = HibernateUtil.getSession();
    Query qry = session
        .getNamedQuery("org.pentaho.platform.repository.subscription.SubscribeContent.getAllContent").setCacheable(true); //$NON-NLS-1$
    return qry.list();
  }

  public ISubscribeContent getContentByActionReference(final String actionReference) {
    Session session = HibernateUtil.getSession();
    Query qry = session
        .getNamedQuery(
            "org.pentaho.platform.repository.subscription.SubscribeContent.findSubscriptionContentByActionRef").setCacheable(true); //$NON-NLS-1$
    qry.setString("searchTerm", actionReference); //$NON-NLS-1$
    Object rtn = null;
    try {
      rtn = qry.uniqueResult();
    } catch (Exception ignored) {
      // sbarkdull, added the logging statement, original author believed the exception can be ignored.
      logger.error( "Exception being ignored: ", ignored ); //$NON-NLS-1$
    }
    return (SubscribeContent) rtn;
  }

  @SuppressWarnings("unchecked")
  public List<ISubscribeContent> getContentBySchedule(final ISchedule schedule) {
    Session session = HibernateUtil.getSession();
    Query qry = session.getNamedQuery(
        "org.pentaho.platform.repository.subscription.SubscribeContent.findContentBySchedule").setCacheable(true); //$NON-NLS-1$
    qry.setParameter("schedule", schedule); //$NON-NLS-1$
    return (qry.list());
  }

  public Document getUserSubscriptions(final String user, final String contentId, final IPentahoSession session) {
    Document document = DocumentHelper.createDocument();
    Element subscriptionsNode = document.addElement("subscriptions"); //$NON-NLS-1$
    addSubscriptionsToDocument(user, contentId, subscriptionsNode, null, session);
    return document;
  }

  public void deleteUserSubscriptions(final String user) {
    //
    Session session = HibernateUtil.getSession();
    Query qry = session
        .getNamedQuery("org.pentaho.platform.repository.subscription.Subscription.findUserSubscriptionsForDeletion").setCacheable(true); //$NON-NLS-1$
    qry.setString("searchUser", user); //$NON-NLS-1$
    SubscriptionRepository.logger.warn(Messages.getInstance().getErrorString("PRO_SUBSCRIPTREP.USER_REMOVING_USER", user)); //$NON-NLS-1$
    List subscriptions = qry.list();
    Subscription subscriptToDelete = null;
    for (int i = 0; i < subscriptions.size(); i++) {
      subscriptToDelete = (Subscription) subscriptions.get(i);
      HibernateUtil.makeTransient(subscriptToDelete);
    }
    HibernateUtil.flushSession();
  }

  @SuppressWarnings("unchecked")
  public List<ISubscription> getUserSubscriptionsToContentReference(final String user, final String contentId) {
    Session session = HibernateUtil.getSession();
    ISubscribeContent content = this.getContentByActionReference(contentId);
    if (content == null) {
      return new ArrayList<ISubscription>();
    }
    Query qry = session
        .getNamedQuery("org.pentaho.platform.repository.subscription.Subscription.findUserSubscriptions").setCacheable(true); //$NON-NLS-1$
    qry.setString("searchUser", user); //$NON-NLS-1$
    qry.setEntity("searchContent", content); //$NON-NLS-1$
    return qry.list();
  }

  @SuppressWarnings("unchecked")
  public List<ISubscription> getUserSubscriptions(final String user) {
    Session session = HibernateUtil.getSession();
    Query qry = session.getNamedQuery(
        "org.pentaho.platform.repository.subscription.Subscription.findAllUserSubscriptions").setCacheable(true); //$NON-NLS-1$
    qry.setString("searchUser", user); //$NON-NLS-1$
    return qry.list();
  }

  @SuppressWarnings("unchecked")
  public List<ISubscription> getAllSubscriptions() {
    Session session = HibernateUtil.getSession();
    Query qry = session
        .getNamedQuery("org.pentaho.platform.repository.subscription.Subscription.getAllSubscriptions").setCacheable(true); //$NON-NLS-1$
    return qry.list();
  }

  public void addSubscriptionsToDocument(final String user, final String contentId, final Element subscriptionsNode,
      final String editId, final IPentahoSession session) {
    List<ISubscription> userSubsToContent = getUserSubscriptionsToContentReference(user, contentId);
    if (userSubsToContent == null) {
      return;
    }
    for (int i = 0; i < userSubsToContent.size(); i++) {
      ISubscription subscription = userSubsToContent.get(i);
      addSubscriptionToDocument(subscription, subscriptionsNode, editId, session);
    }
  }

  /**
   * Returns true if the subscription name is unique for the user/contentId
   * 
   * @param Name
   *            the schedule name to check for uniqueness
   * @param user
   *            the user that owns the schedules
   * @param contentId
   *            The action sequence separated by slashes
   * @return
   */
  public boolean checkUniqueSubscriptionName(final String name, final String user, final String contentId) {
    if (name == null) {
      return true;
    }
    List userSubsToContent = getUserSubscriptionsToContentReference(user, contentId);
    if (userSubsToContent == null) {
      return true;
    }
    for (int i = 0; i < userSubsToContent.size(); i++) {
      Subscription subscription = (Subscription) userSubsToContent.get(i);
      if (name.equals(subscription.getTitle())) {
        return false;
      }
    }

    return true;
  }

  public void addSchedulesToDocument(final String user, final String contentId, final Element schedulesNode,
      final String editId) {
    // now see if there are schedules
    ISubscribeContent content = this.getContentByActionReference(contentId);
    if (content == null) {
      return;
    }
    List schedules = content.getSchedules();
    Iterator scheduleIterator = schedules.iterator();
    Schedule schedule;
    while (scheduleIterator.hasNext()) {
      schedule = (Schedule) scheduleIterator.next();
      if (schedule != null) {
        Element scheduleNode = schedulesNode.addElement("schedule"); //$NON-NLS-1$
        scheduleNode.addElement("id").setText(schedule.getId()); //$NON-NLS-1$
        scheduleNode.addElement("title").setText(schedule.getTitle()); //$NON-NLS-1$
        scheduleNode.addElement("group").setText(schedule.getGroup()); //$NON-NLS-1$
        scheduleNode.addElement("schedRef").setText(schedule.getScheduleReference()); //$NON-NLS-1$
        // see if this schedule is currently selected
        /*
         * if( selectedSchedules.contains( schedule.getId() ) ) {
         * scheduleNode.addAttribute( "selected", "true" );
         * //$NON-NLS-1$//$NON-NLS-2$ }
         */
      }
    }
  }

  public void addSubscriptionToDocument(final ISubscription subscription, final Element subscriptionsNode,
      final String editId, final IPentahoSession session) {

    Element node = subscriptionsNode.addElement("subscription"); //$NON-NLS-1$
    if (subscription.getTitle() == null) {
      node.addElement("title").setText("Unknown"); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      node.addElement("title").setText(subscription.getTitle()); //$NON-NLS-1$
    }
    node.addElement("content-id").setText(subscription.getContent().getId()); //$NON-NLS-1$
    node.addElement("id").setText(subscription.getId()); //$NON-NLS-1$
    node.addElement("user").setText(subscription.getUser()); //$NON-NLS-1$
    if ((editId != null) && editId.equals(subscription.getId())) {
      node.addAttribute("edit", "true"); //$NON-NLS-1$//$NON-NLS-2$
    }
    int type = subscription.getType();
    switch (type) {
      case Subscription.TYPE_PERSONAL:
        node.addElement("type").setText("personal");break; //$NON-NLS-1$ //$NON-NLS-2$
      case Subscription.TYPE_GROUP:
        node.addElement("type").setText("group");break; //$NON-NLS-1$ //$NON-NLS-2$
      case Subscription.TYPE_ROLE:
        node.addElement("type").setText("role");break; //$NON-NLS-1$ //$NON-NLS-2$
    }
    Map parameters = subscription.getParameters();
    Iterator parameterIterator = parameters.keySet().iterator();
    Element parametersNode = node.addElement("parameters"); //$NON-NLS-1$
    while (parameterIterator.hasNext()) {
      String name = (String) parameterIterator.next();
      Object value = parameters.get(name);
      Element parameterNode = parametersNode.addElement("parameter"); //$NON-NLS-1$
      parameterNode.addElement("name").setText(name); //$NON-NLS-1$
      parameterNode.addElement("value").setText(value.toString()); //$NON-NLS-1$
    }
    // now add the archives
    IContentItem contentItem = getContentItem(subscription.getId(), session);
    if (contentItem != null) {
      List contentFiles = contentItem.getFileVersions();
      if ((contentFiles != null) && (contentFiles.size() > 0)) {
        Element archivesNode = node.addElement("archives"); //$NON-NLS-1$
        Iterator contentFileIterator = contentFiles.iterator();
        while (contentFileIterator.hasNext()) {
          IContentItemFile file = (IContentItemFile) contentFileIterator.next();
          Date date = file.getFileDateTime();
          // TODO make this format driven from configuration
          SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm aa"); //$NON-NLS-1$
          String fileId = file.getId();
          Element archiveNode = archivesNode.addElement("archive"); //$NON-NLS-1$
          archiveNode.addElement("id").setText(fileId); //$NON-NLS-1$
          archiveNode.addElement("date").setText(format.format(date)); //$NON-NLS-1$
          archiveNode.addElement("size").setText(Long.toString(file.getFileSize())); //$NON-NLS-1$
          archiveNode.addElement("mimetype").setText(contentItem.getMimeType()); //$NON-NLS-1$
        }
      }
    }
  }

  public ISubscription getSubscriptionById(final String theId) {
    Session session = HibernateUtil.getSession();
    try {
      return (ISubscription) session.get(Subscription.class, theId);
    } catch (HibernateException ex) {
      throw new SubscriptionRepositoryException(Messages.getInstance().getErrorString(
          "PRO_SUBSCRIPTREP.ERROR_0001_GETTING_SUBSCRIPTION", theId), ex); //$NON-NLS-1$
    }
  }

  public ISubscription getSubscription(final String subscriptionId, final IPentahoSession session) {
    if (session == null) {
      // TODO surface an error
      return null;
    }
    if (!session.isAuthenticated()) {
      // TODO surface an error
      return null;
    }
    ISubscription subscription = getSubscriptionById(subscriptionId);
    if (subscription == null) {
      // TODO surface an error
      return null;
    }
    if (subscription.getUser().equals(session.getName())) {
      return subscription;
    }
    // TODO surface an error
    return null;
  }

  public void init(final IPentahoSession session) {
    HibernateUtil.beginTransaction();
  }

  public List<ISchedule> getSchedules() {
    Session session = HibernateUtil.getSession();
    Query qry = session
        .getNamedQuery("org.pentaho.platform.repository.subscription.Schedule.getAllSchedules").setCacheable(true); //$NON-NLS-1$
    return qry.list();
  }

  public List<ISchedule> syncSchedules() throws Exception {
    return (SubscriptionRepository.subscriptionScheduler.syncSchedule(getSchedules()));
  }

  public List<ISchedule> getSchedulesByTitle(final String title) {
    Session session = HibernateUtil.getSession();
    Query qry = session
        .getNamedQuery("org.pentaho.platform.repository.subscription.Schedule.findScheduleByTitle").setCacheable(true); //$NON-NLS-1$
    qry.setString("searchTerm", title); //$NON-NLS-1$
    return qry.list();
  }

  public ISchedule getScheduleByScheduleReference(final String scheduleReference) {
    Session session = HibernateUtil.getSession();
    Query qry = session
        .getNamedQuery("org.pentaho.platform.repository.subscription.Schedule.findSchedulesByReference").setCacheable(true); //$NON-NLS-1$
    qry.setString("searchTerm", scheduleReference); //$NON-NLS-1$
    return (Schedule) qry.uniqueResult();
  }

  public boolean deleteSubscription(final String subscriptionId, final IPentahoSession session) {
    if (session == null) {
      return false;
    }
    if (!session.isAuthenticated()) {
      return false;
    }
    ISubscription subscription = getSubscription(subscriptionId, session);
    if (subscription == null) {
      // TODO throw a warning here
      return false;
    }
    if (subscription.getUser().equals(session.getName())) {

      HibernateUtil.makeTransient(subscription);
      return true;
    } else {
      // TODO throw an error here
      return false;
    }
  }

  public boolean deleteSubscription(final String subscriptionId) throws SubscriptionRepositoryCheckedException {
    ISubscription subscription = getSubscriptionById(subscriptionId);
    if (subscription == null) {
      throw new SubscriptionRepositoryException(Messages.getInstance().getString(
          "PRO_SUBSCRIPTREP.SUBSCRIPTION_ID_NOT_FOUND", subscriptionId)); //$NON-NLS-1$         }
    }
    HibernateUtil.makeTransient(subscription);
    return true;
  }
  
  public boolean deleteSubscriptionForSchedule(final ISubscription subscription, final ISchedule sched) throws SubscriptionRepositoryCheckedException {
    return subscription.deleteSchedule( sched );
  }
  
  public boolean deleteSchedule(final ISchedule sched) throws SubscriptionRepositoryCheckedException {
    if (sched == null) {
      return (false);
    }

    List<ISubscribeContent> contentList = getContentBySchedule(sched);
    for ( ISubscribeContent content : contentList ) {
      deleteContentForSchedule(content, sched);
    }

    HibernateUtil.makeTransient(sched);
    HibernateUtil.flushSession();
    try {
      SubscriptionRepository.subscriptionScheduler.syncSchedule(sched.getScheduleReference(), null);
    } catch (SubscriptionSchedulerException e) {
      HibernateUtil.rollbackTransaction();
      throw new SubscriptionRepositoryCheckedException( "Failed to delete schedule with name " 
          + sched.getScheduleReference() + " and id " + sched.getId(), e );
    }
    return true;
  }

  public boolean deleteScheduleById(final String scheduleId) throws SubscriptionRepositoryCheckedException {
    ISchedule sched = getSchedule(scheduleId);
    if (sched == null) {
      throw new SubscriptionRepositoryCheckedException(Messages.getInstance()
          .getString("PRO_SUBSCRIPTREP.SCHEDULE_ID_NOT_FOUND", scheduleId)); //$NON-NLS-1$         }
    }
    return deleteSchedule(sched);
  }

  public boolean deleteSubscribeContent(final ISubscribeContent subContent) throws SubscriptionSchedulerException {
    HibernateUtil.makeTransient(subContent);
    HibernateUtil.flushSession();
    clearSessionCaches();
    return true;
  }

  public boolean deleteSubscribeContentById(final String subContentId) throws SubscriptionSchedulerException {
    ISubscribeContent content = getContentById(subContentId);
    return deleteSubscribeContent(content);
  }

  public void addSchedule(final ISchedule schedule) throws SubscriptionRepositoryCheckedException {
    if (getScheduleByScheduleReference(schedule.getScheduleReference()) != null) {
      throw new SubscriptionRepositoryCheckedException(Messages.getInstance().getString(
          "PRO_SUBSCRIPTREP.SCHEDULE_REF_NOT_UNIQUE_ADD", schedule.getScheduleReference())); //$NON-NLS-1$ 
    }
// commented by sbarkdull     HibernateUtil.beginTransaction();
    Session session = HibernateUtil.getSession();
    session.save(schedule);
    try {
      SubscriptionRepository.subscriptionScheduler.syncSchedule(null, schedule);
// commented by sbarkdull       HibernateUtil.commitTransaction();
    } catch (SubscriptionSchedulerException e) {
      // TODO sbarkdull, at this point the transaction needs to be aborted, but how!!!
      HibernateUtil.rollbackTransaction();
      throw new SubscriptionRepositoryCheckedException( "", e ); // TODO sbarkdull, error msg? //$NON-NLS-1$
    }
  }

  public ISchedule addCronSchedule(final String title, final String scheduleRef, final String description,
      final String cronString, final String group, Date startDate, Date endDate ) throws SubscriptionRepositoryCheckedException {
    Schedule newSched = new Schedule(UUIDUtil.getUUIDAsString(), title, scheduleRef, description, cronString, group, startDate, endDate );
    addSchedule(newSched);
    return (newSched);
  }


  public ISchedule addRepeatSchedule(String title, String scheduleRef, String description, Integer repeatCount, Integer repeatInterval, String group, 
      Date startDate, Date endDate )
      throws SubscriptionRepositoryCheckedException {
    Schedule newSched = new Schedule(UUIDUtil.getUUIDAsString(), title, scheduleRef, description, repeatCount, repeatInterval, group, startDate, endDate );
    addSchedule(newSched);
    return newSched;
  }
  
  public ISchedule editCronSchedule(final String id, final String title, final String scheduleRef,
      final String description, final String cronString, final String group, Date startDate, Date endDate ) throws SubscriptionRepositoryCheckedException {

    ISchedule oldSched = getSchedule(id);
    if (oldSched != null) {
      String oldScheduleReference = oldSched.getScheduleReference();

      // Any null values keep previous values
      if (scheduleRef != null) {
        if ((!oldScheduleReference.equals(scheduleRef)) && (getScheduleByScheduleReference(scheduleRef) != null)) {
          throw new SubscriptionRepositoryException(Messages.getInstance().getString(
              "PRO_SUBSCRIPTREP.SCHEDULE_REF_NOT_UNIQUE_EDIT", scheduleRef)); //$NON-NLS-1$
        }
        oldSched.setScheduleReference(scheduleRef);
      }

      if (title != null) {
        oldSched.setTitle(title);
      }
      if (description != null) {
        oldSched.setDescription(description);
      }
      if (cronString != null) {
        oldSched.setCronString(cronString);
      }
      if (group != null) {
        oldSched.setGroup(group);
      }
      oldSched.setStartDate( startDate );
      oldSched.setEndDate( endDate );
      try {
        SubscriptionRepository.subscriptionScheduler.syncSchedule(oldScheduleReference, oldSched);
      } catch (SubscriptionSchedulerException ex) {
        throw new SubscriptionRepositoryException(Messages.getInstance().getString("PRO_SUBSCRIPTREP.CANNOT_EDIT_SCHEDULE", id), ex); //$NON-NLS-1$
      }
      return (oldSched);
    }
    return (null);
  }
  
  public ISchedule editRepeatSchedule(final String id, final String title, final String scheduleRef,
      final String description, final Integer repeatCount, final Integer repeatInterval, final String group, Date startDate, Date endDate ) throws SubscriptionRepositoryCheckedException {

    ISchedule oldSched = getSchedule(id);
    if (oldSched != null) {
      String oldScheduleReference = oldSched.getScheduleReference();

      // Any null values keep previous values
      if (scheduleRef != null) {
        if ((!oldScheduleReference.equals(scheduleRef)) && (getScheduleByScheduleReference(scheduleRef) != null)) {
          throw new SubscriptionRepositoryException(Messages.getInstance().getString(
              "PRO_SUBSCRIPTREP.SCHEDULE_REF_NOT_UNIQUE_EDIT", scheduleRef)); //$NON-NLS-1$
        }
        oldSched.setScheduleReference(scheduleRef);
      }

      if (title != null) {
        oldSched.setTitle(title);
      }
      if (description != null) {
        oldSched.setDescription(description);
      }
      oldSched.setRepeatCount( repeatCount);
      if (repeatInterval != null) {
        oldSched.setRepeatInterval( repeatInterval);
      }
      if (group != null) {
        oldSched.setGroup(group);
      }
      oldSched.setStartDate( startDate );
      oldSched.setEndDate( endDate );
      try {
        SubscriptionRepository.subscriptionScheduler.syncSchedule(oldScheduleReference, oldSched);
      } catch (SubscriptionSchedulerException ex) {
        throw new SubscriptionRepositoryException(Messages.getInstance().getString("PRO_SUBSCRIPTREP.CANNOT_EDIT_SCHEDULE", id), ex); //$NON-NLS-1$
      }
      return (oldSched);
    }
    return (null);
  }

  public ISchedule getSchedule(final String scheduleId) {
    // return (Schedule) scheduleMap.get( scheduleId );
    Session session = HibernateUtil.getSession();
    try {
      return (Schedule) session.get(Schedule.class, scheduleId);
    } catch (HibernateException ex) {
      throw new SubscriptionRepositoryException(Messages.getInstance().getErrorString(
          "PRO_SUBSCRIPTREP.ERROR_0002_GETTING_SCHEDULE", scheduleId), ex); //$NON-NLS-1$
    }
  }

  public List getSubscriptionArchives(final String subscriptionName, final IPentahoSession session) {

    IContentItem contentItem = getContentItem(subscriptionName, session);
    if (contentItem == null) {
      // we have no archived versions
      return null;
    }
    return contentItem.getFileVersions();

  }

  public IContentItem getContentItem(final String subscriptionName, final IPentahoSession session) {
    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session);
    ISubscription subscription = subscriptionRepository.getSubscription(subscriptionName, session);
    if (subscription == null) {
      // TODO surface an error
      return null;
    }
    ISubscribeContent content = subscription.getContent();
    Map subscriptionParameters = subscription.getParameters();
    ActionInfo contentInfo = ActionInfo.parseActionString(content.getActionReference());
    //        String solutionName = contentInfo[0];
    //        String actionPath = contentInfo[1];
    String actionName = (String) subscriptionParameters.get("action"); //$NON-NLS-1$
    if (actionName == null) {
      actionName = contentInfo.getActionName();
    }

    IContentItem contentItem = getContentItem(actionName, subscriptionName, null, null, session, contentInfo
        .getSolutionName(), contentInfo.getPath(), false);
    if (contentItem == null) {
      return null;
    }
    return contentItem;
  }

  public IContentItem getContentItem(final String contentName, final String subscriptionName, final String mimeType,
      final String extension, final IPentahoSession session, final String solutionName, final String solutionPath,
      final boolean allowCreate) {

    // get an output stream to hand to the caller
    IContentRepository contentRepository = PentahoSystem.get(IContentRepository.class, session);
    if (contentRepository == null) {
      // error(
      // Messages.getInstance().getErrorString("RuntimeContext.ERROR_0024_NO_CONTENT_REPOSITORY")
      // ); //$NON-NLS-1$
      return null;
    }
    String contentPath = SubscriptionHelper.getSubscriptionOutputLocation(solutionName, solutionPath, contentName);
    // Find the location if it's already there.
    IContentLocation contentLocation = null;
    try {
      contentLocation = contentRepository.getContentLocationByPath(contentPath);
    } catch (Exception ex) {
      // ignored
    }
    if ((contentLocation == null) && allowCreate) {
      contentLocation = contentRepository.newContentLocation(contentPath, subscriptionName, subscriptionName,
          solutionName, true);
    }
    if (contentLocation == null) {
      // error(
      // Messages.getInstance().getErrorString("RuntimeContext.ERROR_0025_INVALID_CONTENT_LOCATION")
      // ); //$NON-NLS-1$
      return null;
    }

    // Get the content item from the location - if it's there.
    IContentItem contentItem = null;
    try {
      contentItem = contentLocation.getContentItemByName(subscriptionName);
    } catch (Exception ex) {
      // Ignored
    }
    if ((contentItem == null) && allowCreate) {
      contentItem = contentLocation.newContentItem(subscriptionName, contentName, extension, mimeType, null,
          IContentItem.WRITEMODE_KEEPVERSIONS);
    }

    return contentItem;

  }

}
