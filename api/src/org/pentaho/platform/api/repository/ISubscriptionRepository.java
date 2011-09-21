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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Oct 10, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.api.repository;

import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.SubscriptionSchedulerException;

public interface ISubscriptionRepository extends IPentahoInitializer {

  public boolean addSubscription(ISubscription subscription);

  public void addContent(ISubscribeContent content);

  public void deleteContent(final ISubscribeContent content);

  public ISubscribeContent editContent(String id, String actionRef, String contentType);

  public ISubscribeContent addContent(String actionRef, String contentType);

  public void setContent(String actionRefs[]) throws Exception;
  
  public List<ISubscribeContent> addContent(final String actionRefs[]) throws SubscriptionRepositoryCheckedException;
  
  public List<ISubscribeContent> getAllContent();

  public List<ISubscription> getAllSubscriptions();

  public List<ISubscription> getUserSubscriptions(String user);

  public boolean deleteContentForSchedule(String contentId, String schedId) throws SubscriptionRepositoryCheckedException;

  public boolean deleteContentForSchedule(ISubscribeContent content, ISchedule sched) throws SubscriptionRepositoryCheckedException;

  public boolean addContentForSchedule(String contentId, String schedId) throws SubscriptionRepositoryCheckedException;

  public void setContentForSchedule(String contentId[], String schedId) throws Exception;
  
  public void addContentForSchedule(final String contentIds[], final String schedId) throws SubscriptionRepositoryCheckedException;

  public boolean addContentForSchedule(ISubscribeContent content, ISchedule sched) throws SubscriptionRepositoryCheckedException;

  public void setSchedulesForContent(String scheduleId[], String contentId) throws Exception;

  public Document getUserSubscriptions(String user, String contentId, IPentahoSession session);

  /**
   * Add a new schedule whose task-execution time is defined by a Cron string.
   * 
   * @param title Title of the schedule. Title is typically displayed in the subscription UI
   * to help the user making the subscription to identify which schedule they want to
   * execute the action sequence.
   * @param scheduleRef The name of the schedule. (In Quartz implementation, this name will
   * be used as the trigger name).
   * @param description a description of the schedule
   * @param cronString a Cron string identifying when the schedule will execute
   * @param group the group name. (In Quartz implementations, this will be used as the trigger's
   * group name).
   * @param startDate date/time the schedule should begin executing it's tasks.
   * @param endDate date/time that the schedule should stop executing its tasks. This parameter
   * may be null, in which case the schedule will never stop.
   * @return the schedule that was created
   * @throws SubscriptionRepositoryCheckedException if the subscription repository
   * fails to create the schedule.
   */
  public ISchedule addCronSchedule(String title, String scheduleRef, String description, String cronString, String group, 
      Date startDate, Date endDate )
      throws SubscriptionRepositoryCheckedException;

  /**
   * Add a new schedule whose task-execution time is defined by a repeat interval
   * and a repeat count. The repeat interval is the period of time that passes between
   * schedule executions. The repeat count is the number of times the schedule should
   * execute. If the repeat count is 0, the schedule will execute once.
   * 
   * @param title Title of the schedule. Title is typically displayed in the subscription UI
   * to help the user making the subscription to identify which schedule they want to
   * execute the action sequence.
   * @param scheduleRef The name of the schedule. (In Quartz implementation, this name will
   * be used as the trigger name).
   * @param description a description of the schedule
   * @param repeatCount the number of times the schedule should execute
   * @param repeatInterval the period of time that passes between schedule executions
   * @param group the group name. (In Quartz implementations, this will be used as the trigger's
   * group name).
   * @param startDate date/time the schedule should begin executing it's tasks.
   * @param endDate date/time that the schedule should stop executing its tasks. This parameter
   * may be null, in which case the schedule will never stop.
   * @return the schedule that was created
   * @throws SubscriptionRepositoryCheckedException if the subscription repository
   * fails to create the schedule.
   */
  public ISchedule addRepeatSchedule(String title, String scheduleRef, String description, Integer repeatCount, Integer repeatInterval, String group, 
      Date startDate, Date endDate )
      throws SubscriptionRepositoryCheckedException;

  public void addSchedule(final ISchedule schedule) throws SubscriptionRepositoryCheckedException;

  /**
   * Edit an existing schedule. The edited schedule will have task-execution time that is
   * defined by a Cron string.
   * 
   * @param title Title of the schedule. Title is typically displayed in the subscription UI
   * to help the user making the subscription to identify which schedule they want to
   * execute the action sequence.
   * @param scheduleRef The name of the schedule. (In Quartz implementation, this name will
   * be used as the trigger name).
   * @param description a description of the schedule
   * @param cronString a Cron string identifying when the schedule will execute
   * @param group the group name. (In Quartz implementations, this will be used as the trigger's
   * group name).
   * @param startDate date/time the schedule should begin executing it's tasks.
   * @param endDate date/time that the schedule should stop executing its tasks. This parameter
   * may be null, in which case the schedule will never stop.
   * @return the schedule that was created
   * @throws SubscriptionRepositoryCheckedException if the subscription repository
   * fails to create the schedule.
   */
  public ISchedule editCronSchedule(String id, String title, String scheduleRef, String description, String cronString,
      String group, Date startDate, Date endDate ) throws SubscriptionRepositoryCheckedException;
  
  /**
   * Edit an existing schedule. The edited schedule will have a task-execution time that is 
   * defined by a repeat interval and a repeat count. The repeat interval is the period
   * of time that passes between
   * schedule executions. The repeat count is the number of times the schedule should
   * execute. If the repeat count is 0, the schedule will execute once.
   * 
   * @param title Title of the schedule. Title is typically displayed in the subscription UI
   * to help the user making the subscription to identify which schedule they want to
   * execute the action sequence.
   * @param scheduleRef The name of the schedule. (In Quartz implementation, this name will
   * be used as the trigger name).
   * @param description a description of the schedule
   * @param repeatCount the number of times the schedule should execute
   * @param repeatInterval the period of time that passes between schedule executions
   * @param group the group name. (In Quartz implementations, this will be used as the trigger's
   * group name).
   * @param startDate date/time the schedule should begin executing it's tasks.
   * @param endDate date/time that the schedule should stop executing its tasks. This parameter
   * may be null, in which case the schedule will never stop.
   * @return the schedule that was created
   * @throws SubscriptionRepositoryCheckedException if the subscription repository
   * fails to create the schedule.
   */
  public ISchedule editRepeatSchedule(String id, String title, String scheduleRef, String description, Integer repeatCount, Integer repeatInterval,
      String group, Date startDate, Date endDate ) throws SubscriptionRepositoryCheckedException;

  public void addSubscriptionToDocument(ISubscription subscription, Element subscriptionsNode, String editId,
      IPentahoSession session);

  public void addSubscriptionsToDocument(String user, String contentId, Element subscriptionsNode, String editId,
      IPentahoSession session);

  public void addSchedulesToDocument(String user, String contentId, Element schedulesNode, String editId);

  public ISubscribeContent getContentByActionReference(String actionReference);

  public ISubscribeContent getContentById(String contentId);

  public List<ISubscribeContent> getContentBySchedule(ISchedule schedule);

  public ISubscription getSubscription(String subscriptionId, IPentahoSession session);

  public boolean deleteSubscription(String subscriptionId, IPentahoSession session);

  public boolean deleteSubscription(String subscriptionId) throws SubscriptionRepositoryCheckedException;

  public boolean deleteSubscriptionForSchedule(final ISubscription subscription, final ISchedule sched) throws SubscriptionRepositoryCheckedException;
  
  public IContentItem getContentItem(String subscriptionName, IPentahoSession session);

  public List<ISchedule> getSchedules();

  public ISchedule getSchedule(String id);

  public List<ISubscription> getSubscriptionsForSchedule(String scheduleId);

  public List<ISubscription> getSubscriptionsForSchedule(ISchedule schedule);

  @SuppressWarnings("unchecked")
  public List getSubscriptionArchives(final String subscriptionName, final IPentahoSession session);
  
  public IPentahoResultSet getSubscriptions(String scheduleId, IPentahoSession session, String solution, String path,
      String action, String parameterNames[]);

  public boolean deleteSubscribeContentById(String subContentId) throws SubscriptionSchedulerException;

  public boolean deleteSubscribeContent(ISubscribeContent subContent) throws SubscriptionSchedulerException;

  public boolean deleteScheduleById(String scheduleId) throws SubscriptionRepositoryCheckedException;

  public boolean deleteSchedule(ISchedule sched) throws SubscriptionRepositoryCheckedException;

  public ISchedule getScheduleByScheduleReference(String scheduleReference);

  public List<ISchedule> getSchedulesByTitle(String title);

  public void deleteUserSubscriptions(String user);

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
  public boolean checkUniqueSubscriptionName(String name, String user, String contentId);

  public Element importSchedules(Document doc);

  public Element importContent(Document doc);

  public List<ISchedule> syncSchedules() throws Exception;

}
