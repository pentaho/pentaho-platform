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
 * @created Aug. 1, 2008 
 * @author Steven Barkdull
 */
package org.pentaho.platform.repository.subscription;

import java.util.Date;
import java.util.List;

import org.pentaho.platform.api.engine.SubscriptionSchedulerException;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscription;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.api.repository.SubscriptionRepositoryCheckedException;

/**
 * Provides "algorithms" to operate on an instance of ISolutionRepository.
 * The algorithms are frequently used sequences of method calls on ISolutionRepository.
 * The idea is that ISolutionRepository's interface should be minimal be sufficient,
 * and this class can provide methods that are not minimal, but perform common
 * sequences of operations on the ISubscriptionRepository.
 * 
 * @author Steven Barkdull
 *
 */
public class SubscriptionRepositoryHelper {

  private SubscriptionRepositoryHelper() {

  }

  /**
   * 
   * @param subscriptionRepository
   * @param schedule
   * 
   * @throws SubscriptionRepositoryCheckedException thrown if:
   *  subscriptionRepository.deleteSubscriptionForSchedule() fails
   *  subscriptionRepository.deleteSubscription() fails
   *  subscriptionRepository.deleteContentForSchedule() fails
   *  subscriptionRepository.deleteSchedule() fails
   *  
   * @throws SubscriptionSchedulerException throw if:
   *  subscriptionRepository.deleteSubscribeContent() fails
   */
  public static void deleteScheduleContentAndSubscription(ISubscriptionRepository subscriptionRepository,
      ISchedule schedule) throws SubscriptionRepositoryCheckedException, SubscriptionSchedulerException {

    List<ISubscription> subscriptions = subscriptionRepository.getSubscriptionsForSchedule(schedule);
    for (ISubscription subscription : subscriptions) {
      subscriptionRepository.deleteSubscriptionForSchedule(subscription, schedule);
      if (0 == subscription.getSchedules().size()) {
        subscriptionRepository.deleteSubscription(subscription.getId());
      }
    }

    List<ISubscribeContent> contentList = subscriptionRepository.getContentBySchedule(schedule);
    for (ISubscribeContent content : contentList) {
      subscriptionRepository.deleteContentForSchedule(content, schedule);
      if (0 == content.getSchedules().size()) {
        subscriptionRepository.deleteSubscribeContent(content);
      }
    }

    boolean deleteSuccess = subscriptionRepository.deleteSchedule(schedule);
    if (!deleteSuccess) {
      throw new SubscriptionSchedulerException("Failed to delete schedule with id: " + schedule.getId()
          + "and description: " + schedule.getDescription());
    }
  }

  public static ISchedule editScheduleAndContent(ISubscriptionRepository subscriptionRepository, String schedId,
      String title, String schedRef, String desc, String cronExpr, Integer repeatCount, Integer repeatInterval,
      String group, Date startDate, Date endDate, String[] actionRefs) throws SubscriptionRepositoryCheckedException {

    ISchedule sched = null;
    if (null != cronExpr) {
      sched = subscriptionRepository.editCronSchedule(schedId, title, schedRef, desc, cronExpr, group, startDate,
          endDate);
    } else {
      sched = subscriptionRepository.editRepeatSchedule(schedId, title, schedRef, desc, repeatCount, repeatInterval,
          group, startDate, endDate);
    }

    List<ISubscribeContent> contentList = subscriptionRepository.getContentBySchedule(sched);

    // delete the old content, next, we'll add the new content
    for (ISubscribeContent content : contentList) {
      subscriptionRepository.deleteContentForSchedule(content, sched);
    }

    List<ISubscribeContent> newContents = subscriptionRepository.addContent(actionRefs);

    // associate the new schedule with each of the action sequences (aka content)
    for (ISubscribeContent newContent : newContents) {
      subscriptionRepository.addContentForSchedule(newContent, sched);
    }

    return sched;
  }
  
  
  public static ISchedule editScheduleWithoutContent(ISubscriptionRepository subscriptionRepository, String schedId,
      String title, String schedRef, String desc, String cronExpr, Integer repeatCount, Integer repeatInterval,
      String group, Date startDate, Date endDate) throws SubscriptionRepositoryCheckedException {

    ISchedule sched = null;
    if (null != cronExpr) {
      sched = subscriptionRepository.editCronSchedule(schedId, title, schedRef, desc, cronExpr, group, startDate,
          endDate);
    } else {
      sched = subscriptionRepository.editRepeatSchedule(schedId, title, schedRef, desc, repeatCount, repeatInterval,
          group, startDate, endDate);
    }

    return sched;
  }

  public static ISchedule addScheduleAndContent(ISubscriptionRepository subscriptionRepository, String title,
      String schedRef, String desc, String cronExpr, Integer repeatCount, Integer repeatInterval, String group,
      Date startDate, Date endDate, String[] actionRefs) throws SubscriptionRepositoryCheckedException {

    // create a new schedule
    ISchedule sched = null;
    if (null != cronExpr) {
      sched = subscriptionRepository.addCronSchedule(title, schedRef, desc, cronExpr, group, startDate, endDate);
    } else {
      sched = subscriptionRepository.addRepeatSchedule(title, schedRef, desc, repeatCount, repeatInterval, group,
          startDate, endDate);
    }

    // add each of the action sequences in actionRefs to the repository as new
    // content (Note: if the action sequence is already in the subscription 
    // repository, it won't get added again)
    List<ISubscribeContent> newContents = subscriptionRepository.addContent(actionRefs);

    // associate the new schedule with each of the action sequences (aka content)
    for (ISubscribeContent newContent : newContents) {
      subscriptionRepository.addContentForSchedule(newContent.getId(), sched.getId());
    }

    return sched;
  }

  public static ISchedule addScheduleWithoutContent(ISubscriptionRepository subscriptionRepository, String title, String schedRef,
      String desc, String cronExpr, Integer repeatCount, Integer repeatInterval, String group, Date startDate,
      Date endDate) throws SubscriptionRepositoryCheckedException {

    // create a new schedule
    ISchedule sched = null;
    if (null != cronExpr) {
      sched = subscriptionRepository.addCronSchedule(title, schedRef, desc, cronExpr, group, startDate, endDate);
    } else {
      sched = subscriptionRepository.addRepeatSchedule(title, schedRef, desc, repeatCount, repeatInterval, group,
          startDate, endDate);
    }

    return sched;
  }
}
