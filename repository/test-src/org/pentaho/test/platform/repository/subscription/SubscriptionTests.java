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

package org.pentaho.test.platform.repository.subscription;

import java.io.File;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscription;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository.subscription.Schedule;
import org.pentaho.platform.repository.subscription.SubscribeContent;
import org.pentaho.platform.repository.subscription.Subscription;
import org.pentaho.platform.repository.subscription.SubscriptionRepository;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class SubscriptionTests extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      System.out.println("File exist returning " + SOLUTION_PATH);
      return SOLUTION_PATH;
    } else {
      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);
      return ALT_SOLUTION_PATH;
    }
  }
  Object[][] schedules = new Object[][] { { null, "Monthly", "monthly-schedule", "Last Day of Month 8:00am" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      { null, "Monday", "monday-schedule", "Monday 8:00am" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      { null, "Tuesday", "tuesday-schedule", "Tuesday 4:00am" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      { null, "Wednesday", "wednesday-schedule", "Wednesday 3:00am" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      { null, "Thursday", "thursday-schedule", "Thursday 8:00am" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      { null, "Friday", "friday-schedule", "Friday 4:30am" } //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  };

  Object[][] content = new Object[][] {
      { null, "galileo/reports/DailyBillableAll.xaction", SubscribeContent.TYPE_REPORT }, //$NON-NLS-1$
      { null, "galileo/reports/MonthlyAgentActivityDetail.xaction", SubscribeContent.TYPE_REPORT }, //$NON-NLS-1$
      { null, "galileo/reports/ProductivityAttachmentRateDetail.xaction", SubscribeContent.TYPE_REPORT }, //$NON-NLS-1$
      { null, "galileo/reports/VendorReport.xaction", SubscribeContent.TYPE_REPORT } //$NON-NLS-1$
  };

  Object[][] subscriptions = new Object[][] {
  //      {null, "fred", "My East Report", null, null, new Integer(Subscription.TYPE_PERSONAL), "REGION", "Eastern"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  //      {null, "fred", "My Central Report", null, null, new Integer(Subscription.TYPE_PERSONAL), "REGION", "Central" } //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  };

  private static final int SCHEDULE_ID_COLUMN = 0;

  private static final int SCHEDULE_TITLE_COLUMN = 1;

  private static final int SCHEDULE_REF_COLUMN = 2;

  private static final int SCHEDULE_DESC_COLUMN = 3;

  private static final int SCHEDULE_CRON_COLUMN = 4;

  private static final int SCHEDULE_GROUP_COLUMN = 5;

  private static final int CONTENT_ID_COLUMN = 0;

  private static final int CONTENT_ACTIONREF_COLUMN = 1;

  private static final int CONTENT_TYPE_COLUMN = 2;

  private static final int SUBSCRIPTION_ID_COLUMN = 0;

  private static final int SUBSCRIPTION_USER_COLUMN = 1;

  private static final int SUBSCRIPTION_TITLE_COLUMN = 2;

  private static final int SUBSCRIPTION_CONTENT_COLUMN = 3;

  private static final int SUBSCRIPTION_DESTINATION_COLUMN = 4;

  private static final int SUBSCRIPTION_TYPE_COLUMN = 5;

  private static final int SUBSCRIPTION_PARMNAME_COLUMN = 6;

  private static final int SUBSCRIPTION_PARMVALUE_COLUMN = 7;

  public void testSubscriptionRepository() {
    startTest();
    boolean ok = true;
    SubscriptionRepository subscriptionRepo = new SubscriptionRepository();
    try {
      createSchedules(subscriptionRepo);
    } catch (Exception ex) {
      error(ex.getLocalizedMessage(), ex);
      ok = false;
    }
    if (ok) {
      // Another test using created schedules.
      try {
        createScheduleContent(subscriptionRepo);
      } catch (Exception ex) {
        error(ex.getLocalizedMessage(), ex);
        ok = false;
      }
    }

    if (ok) {
      // Another test, now using created content.
      try {
        createSubscription(subscriptionRepo);
      } catch (Exception ex) {
        error(ex.getLocalizedMessage(), ex);
        ok = false;
      }
    }

    if (ok) {
      try {
        doQueryTests(subscriptionRepo);
      } catch (Exception ex) {
        error(ex.getLocalizedMessage(), ex);
        ok = false;
      }
    }

    try {
      info(Messages.getInstance().getString("SUBSCRTESTS.USER_CLEANUP_SUBSCRIPTIONS")); //$NON-NLS-1$
      ISubscription cleanupSubscription = null;
      for (int i = 0; i < subscriptions.length; i++) {
        if (subscriptions[i][SUBSCRIPTION_ID_COLUMN] != null) {
          cleanupSubscription = subscriptionRepo.getSubscriptionById((String) subscriptions[i][SUBSCRIPTION_ID_COLUMN]);
          assertNotNull(cleanupSubscription);
          HibernateUtil.makeTransient(cleanupSubscription);
        }
      }
    } catch (Exception ex) {
      error(ex.getLocalizedMessage(), ex);
    }

    // Keep trying cleanup...
    try {
      info(Messages.getInstance().getString("SUBSCRTESTS.USER_CLEANUP_CONTENT")); //$NON-NLS-1$
      ISubscribeContent cleanupContent = null;
      for (int i = 0; i < content.length; i++) {
        if (content[i][CONTENT_ID_COLUMN] != null) {
          cleanupContent = subscriptionRepo.getContentById((String) content[i][CONTENT_ID_COLUMN]);
          assertNotNull(cleanupContent);
          HibernateUtil.makeTransient(cleanupContent);
        }
      }
    } catch (Exception ex) {
      error(ex.getLocalizedMessage(), ex);
    }
    finishTest();
  }

  public void createSubscription(SubscriptionRepository subscriptionRepo) {
    String subscriptionId = null;
    ISubscription subscript = null;
    Map parameters = null;
    ISchedule sched1 = subscriptionRepo.getSchedule((String) schedules[0][SCHEDULE_ID_COLUMN]);
    assertNotNull(sched1);
    ISchedule sched2 = subscriptionRepo.getSchedule((String) schedules[2][SCHEDULE_ID_COLUMN]);
    assertNotNull(sched2);
    ISubscribeContent subContent = subscriptionRepo.getContentById((String) content[0][CONTENT_ID_COLUMN]);
    assertNotNull(subContent);
    String parameterName = null;
    String parameterValue = null;
    info(Messages.getInstance().getString("SUBSCRTESTS.USER_CREATING_SUBSCRIPTIONS")); //$NON-NLS-1$
    for (int i = 0; i < subscriptions.length; i++) {
      subscriptionId = UUIDUtil.getUUIDAsString();
      subscriptions[i][SUBSCRIPTION_ID_COLUMN] = subscriptionId;
      parameters = new HashMap();
      parameterName = (String) subscriptions[i][SUBSCRIPTION_PARMNAME_COLUMN];
      parameterValue = (String) subscriptions[i][SUBSCRIPTION_PARMVALUE_COLUMN];
      parameters.put(parameterName, parameterValue);
      subscriptions[i][SUBSCRIPTION_CONTENT_COLUMN] = subContent;
      subscript = new Subscription(subscriptionId, (String) subscriptions[i][SUBSCRIPTION_USER_COLUMN],
          (String) subscriptions[i][SUBSCRIPTION_TITLE_COLUMN], subContent,
          (String) subscriptions[i][SUBSCRIPTION_DESTINATION_COLUMN],
          ((Integer) subscriptions[i][SUBSCRIPTION_TYPE_COLUMN]).intValue(), parameters);
      subscript.addSchedule(sched1);
      subscript.addSchedule(sched2);
      subscriptionRepo.addSubscription(subscript);
    }
    commitFlushAndClear();

    // After commiting and flushing, you must begin a new transaction...
    HibernateUtil.beginTransaction();
    Schedule schedCheck;
    info(Messages.getInstance().getString("SUBSCRTESTS.USER_VALIDATING_SUBSCRIPTIONS")); //$NON-NLS-1$
    for (int i = 0; i < subscriptions.length; i++) {
      subscript = subscriptionRepo.getSubscriptionById((String) subscriptions[i][SUBSCRIPTION_ID_COLUMN]);
      assertNotNull(subscript);
      assertEquals(subscript.getTitle(), subscriptions[i][SUBSCRIPTION_TITLE_COLUMN]);
      assertEquals(subscript.getUser(), subscriptions[i][SUBSCRIPTION_USER_COLUMN]);
      assertEquals(new Integer(subscript.getType()), subscriptions[i][SUBSCRIPTION_TYPE_COLUMN]);
      parameters = subscript.getParameters();
      assertNotNull(parameters);
      parameterValue = (String) parameters.get(subscriptions[i][SUBSCRIPTION_PARMNAME_COLUMN]);
      assertNotNull(parameterValue);
      assertEquals(subscriptions[i][SUBSCRIPTION_PARMVALUE_COLUMN], parameterValue);
      List scheduleList = subscript.getSchedules();
      assertNotNull(scheduleList);
      schedCheck = (Schedule) scheduleList.get(0);
      assertNotNull(schedCheck);
      assertEquals(sched1, schedCheck);
      schedCheck = (Schedule) scheduleList.get(1);
      assertNotNull(schedCheck);
      assertEquals(sched2, schedCheck);
    }

  }

  public void createSchedules(SubscriptionRepository subscriptionRepo) {

    String scheduleId = null;
    ISchedule currentSchedule = null;
    
    HibernateUtil.beginTransaction();
    info(Messages.getInstance().getString("SUBSCRTESTS.USER_CREATING_SCHEDULES")); //$NON-NLS-1$
    for (int i = 0; i < schedules.length; i++) {
      scheduleId = UUIDUtil.getUUIDAsString();
      schedules[i][SCHEDULE_ID_COLUMN] = scheduleId;
      currentSchedule = new Schedule(scheduleId, (String) schedules[i][SCHEDULE_TITLE_COLUMN],
          (String) schedules[i][SCHEDULE_REF_COLUMN], (String) schedules[i][SCHEDULE_DESC_COLUMN],
          (String) schedules[i][SCHEDULE_CRON_COLUMN], (String) schedules[i][SCHEDULE_GROUP_COLUMN], new Date(), new Date() );
      assertNotNull(currentSchedule);
      try {
        subscriptionRepo.addSchedule(currentSchedule);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    commitFlushAndClear();

    // After commiting and flushing, you must begin a new transaction...
    HibernateUtil.beginTransaction();
    info(Messages.getInstance().getString("SUBSCRTESTS.USER_VALIDATING_SCHEDULES")); //$NON-NLS-1$
    for (int i = 0; i < schedules.length; i++) {
      currentSchedule = subscriptionRepo.getSchedule((String) schedules[i][SCHEDULE_ID_COLUMN]);
      assertNotNull(currentSchedule);
      assertEquals(currentSchedule.getTitle(), schedules[i][SCHEDULE_TITLE_COLUMN]);
      assertEquals(currentSchedule.getScheduleReference(), schedules[i][SCHEDULE_REF_COLUMN]);
      assertEquals(currentSchedule.getDescription(), schedules[i][SCHEDULE_DESC_COLUMN]);
      assertEquals(currentSchedule.getDescription(), schedules[i][SCHEDULE_CRON_COLUMN]);
      assertEquals(currentSchedule.getDescription(), schedules[i][SCHEDULE_GROUP_COLUMN]);
    }
    commitFlushAndClear();
  }

  private void commitFlushAndClear() {
    info(Messages.getInstance().getString("SUBSCRTESTS.USER_FLUSHING_CACHE")); //$NON-NLS-1$
    HibernateUtil.commitTransaction();
    HibernateUtil.flushSession(); // Force write to disk.
    HibernateUtil.clear(); // Get everything out of the cache.
  }

  private void showMessages() {
    List messages = this.getMessages();
    for (int i = 0; i < messages.size(); i++) {
      System.out.println(messages.get(i));
    }
  }

  public void createScheduleContent(SubscriptionRepository subscriptionRepo) {
    info(Messages.getInstance().getString("SUBSCRTESTS.USER_CREATING_CONTENT")); //$NON-NLS-1$
    String contentId = null;
    ISubscribeContent currentContent = null;
    for (int i = 0; i < content.length; i++) {
      contentId = UUIDUtil.getUUIDAsString();
      content[i][CONTENT_ID_COLUMN] = contentId;
      currentContent = new SubscribeContent(contentId, (String) content[i][CONTENT_ACTIONREF_COLUMN],
          (String) content[i][CONTENT_TYPE_COLUMN]);
      assertNotNull(currentContent);
      subscriptionRepo.addContent(currentContent);
    }
    commitFlushAndClear();
    // After commiting and flushing, you must begin a new transaction...
    HibernateUtil.beginTransaction();
    info(Messages.getInstance().getString("SUBSCRTESTS.USER_VALIDATING_CONTENT")); //$NON-NLS-1$
    for (int i = 0; i < content.length; i++) {
      currentContent = subscriptionRepo.getContentById((String) content[i][CONTENT_ID_COLUMN]);
      assertNotNull(currentContent);
      assertEquals(currentContent.getActionReference(), content[i][CONTENT_ACTIONREF_COLUMN]);
      assertEquals(currentContent.getType(), content[i][CONTENT_TYPE_COLUMN]);
    }

  }

  public void doQueryTests(SubscriptionRepository subscriptionRepo) {
    // This method assumes that all the objects at the top have been filled out.
    // First, test the Subscriptions-for-a-schedule query.
    ISchedule sched1 = subscriptionRepo.getSchedule((String) schedules[0][SCHEDULE_ID_COLUMN]);
    assertNotNull(sched1);
    info(Messages.getInstance().getString("SUBSCRTESTS.USER_LISTING_SUBSCRIPTIONS_FOR_SCHEDULE") + sched1.getId()); //$NON-NLS-1$
    List subscriptionList = subscriptionRepo.getSubscriptionsForSchedule(sched1);
    Subscription subscript;
    for (int i = 0; i < subscriptionList.size(); i++) {
      subscript = (Subscription) subscriptionList.get(i);
      assertNotNull(subscript);
      info(subscript.asXml());
    }

    info(Messages.getInstance().getString("SUBSCRTESTS.USER_LISTING_CONTENT_BY_ACTIONREF")); //$NON-NLS-1$
    ISubscribeContent subContent = subscriptionRepo
        .getContentByActionReference((String) content[0][CONTENT_ACTIONREF_COLUMN]);
    assertNotNull(subContent);
    info(Messages.getInstance().getString("SUBSCRTESTS.USER_FOUND_CONTENT", subContent.getId())); //$NON-NLS-1$

  }

  public void testUserSubscribeDocument() {
    startTest();
    SubscriptionRepository repository = new SubscriptionRepository();

    SubscribeContent subContent = new SubscribeContent(UUIDUtil.getUUIDAsString(),
        "samples/reporting/JFree_Quad.xaction.xaction", SubscribeContent.TYPE_REPORT); //$NON-NLS-1$
    repository.addContent(subContent);
    String subscriptionId = UUIDUtil.getUUIDAsString();
    HashMap parameters = new HashMap();
    parameters.put("REGION", "Eastern"); //$NON-NLS-1$//$NON-NLS-2$
    Subscription subscription = new Subscription(subscriptionId,
        "fred", "My East Report", subContent, "", Subscription.TYPE_PERSONAL, parameters); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    repository.addSubscription(subscription);
    subscriptionId = UUIDUtil.getUUIDAsString();
    parameters = new HashMap();
    parameters.put("REGION", "Central"); //$NON-NLS-1$//$NON-NLS-2$
    subscription = new Subscription(subscriptionId,
        "fred", "My Central Report", subContent, "", Subscription.TYPE_PERSONAL, parameters); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    repository.addSubscription(subscription);
    StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$

    Document userSubscriptionsDocument = repository.getUserSubscriptions("fred", null, session); //$NON-NLS-1$

    OutputStream os = getOutputStream("SubscriptionTests.testUserSubscribeDocument", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$

    try {
      os.write(userSubscriptionsDocument.asXML().getBytes());
    } catch (Exception e) {

    }

    finishTest();
  }

  public static void main(String[] args) {
    SubscriptionTests test = new SubscriptionTests();
    test.setUp();
    try {
      // test.testUserSubscribeDocument();
      test.testSubscriptionRepository();
      test.showMessages();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
