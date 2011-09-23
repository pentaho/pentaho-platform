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
 * @created Aug 15, 2005 
 * @author James Dixon
 */

package org.pentaho.test.platform.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
//import java.util.List;
//import java.util.Locale;
//
//import org.pentaho.platform.api.engine.IScheduler;
//import org.pentaho.platform.api.repository.ISubscriptionRepository;
//import org.pentaho.platform.engine.core.output.SimpleContentItem;
//import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
//import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
//import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
//import org.pentaho.platform.engine.core.system.UserSession;
//import org.pentaho.platform.repository.messages.Messages;
//import org.pentaho.platform.repository.subscription.Subscription;
//import org.pentaho.platform.repository.subscription.SubscriptionHelper;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class SubscriptionHelperTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

//  private StandaloneApplicationContext applicationContext;

  public SubscriptionHelperTest() {
    startTest();
    //    applicationContext = new StandaloneApplicationContext(SOLUTION_PATH, ""); //$NON-NLS-1$
    //    PentahoSystem.init(applicationContext);
  }

  public org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository getFileSolutionRepository(
      StandaloneSession session) {
    org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository rtn = new org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository();
    rtn.init(session);
    return rtn;
  }

  /*public void testSaveSubscription() {
    startTest();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    String solutionName = "test"; //$NON-NLS-1$
    String actionPath = "dashboard"; //$NON-NLS-1$
    String actionName = "departments.rule.xaction"; //$NON-NLS-1$
    String name = "MyMonthlySubscriptionTest";//$NON-NLS-1$
    String subscriberId = "24234234"; //$NON-NLS-1$
    String subscriptionDestination = "c:/"; //$NON-NLS-1$
    parameters.setParameter("solution", solutionName); //$NON-NLS-1$
    parameters.setParameter("path", actionPath); //$NON-NLS-1$
    parameters.setParameter("action", actionName); //$NON-NLS-1$
    parameters.setParameter("subscribe-id", subscriberId); //$NON-NLS-1$    
    parameters.setParameter("subscribe-name", name); //$NON-NLS-1$
    parameters.setParameter("destination", subscriptionDestination); //$NON-NLS-1$
    UserSession session = new UserSession("Joe", Locale.US, true, parameters); //$NON-NLS-1$
    String actionReference = solutionName + "/" + actionPath + "/" + actionName; //$NON-NLS-1$ //$NON-NLS-2$
    org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository repository = getFileSolutionRepository(session);
    String result = SubscriptionHelper.saveSubscription(parameters, actionReference, session, true);
    System.out.println("Result of the Subscription Save Operation:  " + result);//$NON-NLS-1$
    assertTrue("Subscription was successfully saved", result != null); //$NON-NLS-1$
    finishTest();
  }*/

  /*public void testEditSubscription() {
    startTest();
    try {
      SimpleParameterProvider parameters1 = new SimpleParameterProvider();
      String solutionName1 = "test"; //$NON-NLS-1$
      String actionPath1 = "dashboard"; //$NON-NLS-1$
      String actionName1 = "departments.rule.xaction"; //$NON-NLS-1$
      String name1 = "MyMonthlySubscriptionTest";//$NON-NLS-1$
      String subscriberId1 = "24234234"; //$NON-NLS-1$
      String subscriptionDestination1 = "c:/"; //$NON-NLS-1$
      parameters1.setParameter("solution", solutionName1); //$NON-NLS-1$
      parameters1.setParameter("path", actionPath1); //$NON-NLS-1$
      parameters1.setParameter("action", actionName1); //$NON-NLS-1$
      parameters1.setParameter("subscribe-id", subscriberId1); //$NON-NLS-1$    
      parameters1.setParameter("subscribe-name", name1); //$NON-NLS-1$
      parameters1.setParameter("destination", subscriptionDestination1); //$NON-NLS-1$
      UserSession session = new UserSession("Joe", Locale.US, true, parameters1); //$NON-NLS-1$
      String actionReference = solutionName1 + "/" + actionPath1 + "/" + actionName1; //$NON-NLS-1$ //$NON-NLS-2$      
      String result = SubscriptionHelper.saveSubscription(parameters1, actionReference, session, true);
      SimpleParameterProvider parameters = new SimpleParameterProvider();
      String solutionName = "test"; //$NON-NLS-1$
      String actionPath = "dashboard"; //$NON-NLS-1$
      String actionName = "departments.rule.xaction"; //$NON-NLS-1$
      String name = "MyMonthlySubscription";//$NON-NLS-1$
      parameters.setParameter("solution", solutionName); //$NON-NLS-1$
      parameters.setParameter("path", actionPath); //$NON-NLS-1$
      parameters.setParameter("action", actionName); //$NON-NLS-1$
      OutputStream outputStream = getOutputStream("EditSubscription", ".txt"); //$NON-NLS-1$ //$NON-NLS-2$     
      ISubscriptionRepository subscriptionRepository = PentahoSystem.getSubscriptionRepository(session);
      List subscriptionList = subscriptionRepository.getUserSubscriptions("Joe"); //$NON-NLS-1$
      List schedule = subscriptionRepository.getSchedules();
      for (int i = 0; i < schedule.size(); i++) {
        System.out.println("Schedule #" + (i + 1) + schedule.get(i)); //$NON-NLS-1$
      }
      String subscriptionId = null;
      for (int i = 0; i < subscriptionList.size(); i++) {
        Subscription subscription = (Subscription) subscriptionList.get(i);
        String title = subscription.getTitle();
        if ((title != null) && (title.equals(name1))) {
          subscriptionId = subscription.getId();
          break;
        }
      }
      assertNotNull("Subscription not found", subscriptionId);
      SubscriptionHelper.editSubscription(subscriptionId, session, null, outputStream);
    } catch (Exception e) {
      assertTrue("Exception was thrown" + e.getLocalizedMessage(), false);
    }
    assertTrue("Subscription was successfully Edited", true); //$NON-NLS-1$
    finishTest();
  }*/

  /*public void testCreateSubscriptionArchive() {
    startTest();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    String solutionName = "test"; //$NON-NLS-1$
    String actionPath = "dashboard"; //$NON-NLS-1$
    String actionName = "departments.rule.xaction"; //$NON-NLS-1$
    String name = "MyMonthlySubscriptionTest";//$NON-NLS-1$
    parameters.setParameter("solution", solutionName); //$NON-NLS-1$
    parameters.setParameter("path", actionPath); //$NON-NLS-1$
    parameters.setParameter("action", actionName); //$NON-NLS-1$
    parameters.setParameter("subscribe-name", name); //$NON-NLS-1$
    UserSession session = new UserSession("Joe", Locale.US, true, parameters); //$NON-NLS-1$
    ISubscriptionRepository subscriptionRepository = PentahoSystem.getSubscriptionRepository(session);
    List subscriptionList = subscriptionRepository.getUserSubscriptions("Joe"); //$NON-NLS-1$

    String subscriptionId = null;
    for (int i = 0; i < subscriptionList.size(); i++) {
      Subscription subscription = (Subscription) subscriptionList.get(i);
      String title = subscription.getTitle();
      if ((title != null) && (title.equals(name))) {
        subscriptionId = subscription.getId();
        break;
      }
    }

    String result = SubscriptionHelper.createSubscriptionArchive(subscriptionId, session, null, parameters);
    assertTrue("The result of the subcsription save action was " + result, true); //$NON-NLS-1$
    assertTrue(true);
    finishTest();
  }*/

/*  public void testGetArchived() {
    startTest();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    String solutionName = "test"; //$NON-NLS-1$
    String actionPath = "dashboard"; //$NON-NLS-1$
    String actionName = "departments.rule.xaction"; //$NON-NLS-1$
    String name = "MyMonthlySubscription";//$NON-NLS-1$
    parameters.setParameter("solution", solutionName); //$NON-NLS-1$
    parameters.setParameter("path", actionPath); //$NON-NLS-1$
    parameters.setParameter("action", actionName); //$NON-NLS-1$
    UserSession session = new UserSession("Joe", Locale.US, true, parameters); //$NON-NLS-1$
    ISubscriptionRepository subscriptionRepository = PentahoSystem.getSubscriptionRepository(session);
    List subscriptionList = subscriptionRepository.getUserSubscriptions("Joe"); //$NON-NLS-1$
    //List subscriptionList = subscriptionRepository.getAllSubscriptions(); //$NON-NLS-1$
    OutputStream outputStream = getOutputStream("GetArchived", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    String subscriptionId = null;
    for (int i = 0; i < subscriptionList.size(); i++) {
      Subscription subscription = (Subscription) subscriptionList.get(i);
      String title = subscription.getTitle();
      if ((title != null) && (title.equals(name))) {
        subscriptionId = subscription.getId();
        break;
      }
    }
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(new SimpleContentItem(outputStream), true);
    SubscriptionHelper.getArchived(subscriptionId, name, session, outputHandler);

    assertTrue(true);
    finishTest();
  } */

  /*public void testDeleteSubscriptionArchive() {
    startTest();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    String solutionName = "test"; //$NON-NLS-1$
    String actionPath = "dashboard"; //$NON-NLS-1$
    String actionName = "departments.rule.xaction"; //$NON-NLS-1$
    String name = "MyMonthlySubscriptionTest";//$NON-NLS-1$
    parameters.setParameter("solution", solutionName); //$NON-NLS-1$
    parameters.setParameter("path", actionPath); //$NON-NLS-1$
    parameters.setParameter("action", actionName); //$NON-NLS-1$
    UserSession session = new UserSession("Joe", Locale.US, true, parameters); //$NON-NLS-1$
    ISubscriptionRepository subscriptionRepository = PentahoSystem.getSubscriptionRepository(session);
    List subscriptionList = subscriptionRepository.getUserSubscriptions("Joe"); //$NON-NLS-1$
    String subscriptionId = null;
    for (int i = 0; i < subscriptionList.size(); i++) {
      Subscription subscription = (Subscription) subscriptionList.get(i);
      String title = subscription.getTitle();
      if ((title != null) && (title.equals(name))) {
        subscriptionId = subscription.getId();
        break;
      }
    }
    String result = SubscriptionHelper.deleteSubscriptionArchive(subscriptionId, name, session);
    assertTrue("The result of the subcsription save action was " + result, true); //$NON-NLS-1$

    assertTrue(true);
    finishTest();
  }*/

/*  public void testDeleteSubscription() {
    startTest();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    String solutionName = "samples"; //$NON-NLS-1$
    String actionPath = "getting-started"; //$NON-NLS-1$
    String actionName = "HelloEmail.xaction"; //$NON-NLS-1$
    String name = "MyMonthlySubscription";//$NON-NLS-1$
    String subscriberId = "24234234"; //$NON-NLS-1$
    String subscriptionDestination = "c:/code/latest"; //$NON-NLS-1$
    parameters.setParameter("solution", solutionName); //$NON-NLS-1$
    parameters.setParameter("path", actionPath); //$NON-NLS-1$
    parameters.setParameter("action", actionName); //$NON-NLS-1$
    parameters.setParameter("subscribe-id", subscriberId); //$NON-NLS-1$    
    parameters.setParameter("subscribe-name", name); //$NON-NLS-1$
    parameters.setParameter("destination", subscriptionDestination); //$NON-NLS-1$
    UserSession session = new UserSession("Joe", Locale.US, true, parameters); //$NON-NLS-1$
    ISubscriptionRepository subscriptionRepository = PentahoSystem.getSubscriptionRepository(session);
    List subscriptionList = subscriptionRepository.getUserSubscriptions("Joe"); //$NON-NLS-1$
    String subscriptionId = null;
    for (int i = 0; i < subscriptionList.size(); i++) {
      Subscription subscription = (Subscription) subscriptionList.get(i);
      String title = subscription.getTitle();
      if ((title != null) && (title.equals(name))) {
        subscriptionId = subscription.getId();
        break;
      }
    }
    String result = SubscriptionHelper.deleteSubscription(subscriptionId, session);
    assertTrue("The result of delete subcsription action was " + result, true); //$NON-NLS-1$
    assertTrue(true);

    finishTest();
  } */

/*  public void testSaveEditAndDeleteSubscription() {
    startTest();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    String solutionName = "test"; //$NON-NLS-1$
    String actionPath = "dashboard"; //$NON-NLS-1$
    String actionName = "departments.rule.xaction"; //$NON-NLS-1$
    String name = "MyMonthlySubscription";//$NON-NLS-1$

    String subscriberId = "24234234"; //$NON-NLS-1$
    String subscriptionDestination = "c:/code/latest"; //$NON-NLS-1$

    parameters.setParameter("solution", solutionName); //$NON-NLS-1$
    parameters.setParameter("path", actionPath); //$NON-NLS-1$
    parameters.setParameter("action", actionName); //$NON-NLS-1$
    parameters.setParameter("subscribe-id", subscriberId); //$NON-NLS-1$    
    parameters.setParameter("subscribe-name", name); //$NON-NLS-1$
    parameters.setParameter("destination", subscriptionDestination); //$NON-NLS-1$
    OutputStream outputStream = getOutputStream("SaveSubscription", ".html"); //$NON-NLS-1$ //$NON-NLS-2$     
    UserSession session = new UserSession("Joe", Locale.US, true, parameters); //$NON-NLS-1$

    String actionReference = solutionName + "/" + actionPath + "/" + actionName; //$NON-NLS-1$ //$NON-NLS-2$
    String result = SubscriptionHelper.saveSubscription(parameters, actionReference, session, true);

    assertTrue("The result of the subcsription save action was " + result, true); //$NON-NLS-1$

    ISubscriptionRepository subscriptionRepository = PentahoSystem.getSubscriptionRepository(session);
    List subscriptionList = subscriptionRepository.getUserSubscriptions("Joe"); //$NON-NLS-1$
    String subscriptionId = null;
    Subscription subscription = null;
    for (int i = 0; i < subscriptionList.size(); i++) {
      subscription = (Subscription) subscriptionList.get(i);
      String title = subscription.getTitle();
      if ((title != null) && (title.equals(name))) {
        subscriptionId = subscription.getId();
        break;
      }
    }

    IScheduler scheduler = PentahoSystem.getScheduler(session);
    boolean isScheduled = scheduler.scheduleSubscription(subscription);

    // boolean isScheduled = SubscriptionHelper.scheduleSubscription(subscription);

    if (isScheduled) {
      assertTrue("Subscription was scheduled", true); //$NON-NLS-1$
    }
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(new SimpleContentItem(outputStream), true);
    SubscriptionHelper.runSubscription(subscriptionId, session, parameters, null, outputHandler);
    outputStream = getOutputStream("EditSubscription", ".html"); //$NON-NLS-1$ //$NON-NLS-2$    
    SubscriptionHelper.editSubscription(subscriptionId, session, null, outputStream);
    result = SubscriptionHelper.deleteSubscription(subscriptionId, session);

    assertTrue(true);
    finishTest();
  }*/

/*  public void testCreateSaveEditAndDeleteSubscriptionArchive() {
    startTest();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    String solutionName = "test"; //$NON-NLS-1$
    String actionPath = "dashboard"; //$NON-NLS-1$
    String actionName = "departments.rule.xaction"; //$NON-NLS-1$
    String name = "MyMonthlySubscription";//$NON-NLS-1$
    String subscriberId = "24234234"; //$NON-NLS-1$
    String subscriptionDestination = "c:/code/latest"; //$NON-NLS-1$

    parameters.setParameter("solution", solutionName); //$NON-NLS-1$
    parameters.setParameter("path", actionPath); //$NON-NLS-1$
    parameters.setParameter("action", actionName); //$NON-NLS-1$
    parameters.setParameter("subscribe-id", subscriberId); //$NON-NLS-1$    
    parameters.setParameter("subscribe-name", name); //$NON-NLS-1$
    parameters.setParameter("destination", subscriptionDestination); //$NON-NLS-1$

    OutputStream outputStream = getOutputStream("SaveSubscription", ".html"); //$NON-NLS-1$ //$NON-NLS-2$     
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$

    String actionReference = solutionName + "/" + actionPath + "/" + actionName; //$NON-NLS-1$ //$NON-NLS-2$
    String result = SubscriptionHelper.saveSubscription(parameters, actionReference, session);

    assertTrue("The result of the subcsription save action was " + result, true); //$NON-NLS-1$

    ISubscriptionRepository subscriptionRepository = PentahoSystem.getSubscriptionRepository(session);
    List subscriptionList = subscriptionRepository.getUserSubscriptions("Joe"); //$NON-NLS-1$
    String subscriptionId = null;
    Subscription subscription = null;
    for (int i = 0; i < subscriptionList.size(); i++) {
      subscription = (Subscription) subscriptionList.get(i);
      String title = subscription.getTitle();
      if ((title != null) && (title.equals(name))) {
        subscriptionId = subscription.getId();
        break;
      }
    }
    //SubscriptionHelper.scheduleSubscription(subscription);

    SimpleOutputHandler outputHandler = new SimpleOutputHandler(new SimpleContentItem(outputStream), true);
    SubscriptionHelper.getArchived(subscriptionId, name, session, outputHandler);

    result = SubscriptionHelper.createSubscriptionArchive(subscriptionId, session, null, parameters);
    result = SubscriptionHelper.deleteSubscriptionArchive(subscriptionId, name, session);

    assertTrue(true);
    finishTest();
  }*/

  protected InputStream getInputStreamFromOutput(String testName, String extension) {
    String path = PentahoSystem.getApplicationContext().getFileOutputPath("test/tmp/" + testName + extension); //$NON-NLS-1$
    File f = new File(path);
    if (f.exists()) {
      try {
        FileInputStream fis = new FileInputStream(f);
        return fis;
      } catch (Exception ignored) {
        return null;
      }
    } else {
      return null;
    }
  }

  protected OutputStream getOutputStream(String testName, String extension) {
    OutputStream outputStream = null;
    try {
      String tmpDir = PentahoSystem.getApplicationContext().getFileOutputPath("test/tmp"); //$NON-NLS-1$
      File file = new File(tmpDir);
      file.mkdirs();
      String path = PentahoSystem.getApplicationContext().getFileOutputPath("test/tmp/" + testName + extension); //$NON-NLS-1$
      outputStream = new FileOutputStream(path);
    } catch (FileNotFoundException e) {

    }
    return outputStream;
  }
  
  public void testDummyTest() {}

  public static void main(String[] args) {
//    SubscriptionHelperTest test = new SubscriptionHelperTest();
    //test.testCreateSubscriptionArchive();
    //test.testDeleteSubscriptionArchive();
    //test.testSaveSubscription();
    //test.testEditSubscription();
    //test.testDeleteSubscription();
    //test.testSaveEditAndDeleteSubscription();
    //test.testCreateSaveEditAndDeleteSubscriptionArchive();
  }

}
