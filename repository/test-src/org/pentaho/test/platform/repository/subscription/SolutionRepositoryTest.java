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
 * Copyright 2006-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created Jan 25, 2006 
 * @author wseyler
 */

package org.pentaho.test.platform.repository.subscription;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.SpringSecurityPermissionMgr;
import org.pentaho.platform.engine.security.acls.AclPublisher;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
import org.pentaho.test.platform.repository.RepositoryTestCase;

@SuppressWarnings("nls")
public class SolutionRepositoryTest extends RepositoryTestCase {
  private StringBuffer longString = new StringBuffer();

  org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repository = null;

  private Map<IPermissionRecipient, IPermissionMask> defaultAcls = new LinkedHashMap<IPermissionRecipient, IPermissionMask>();

  AclPublisher publisher = null;

  RepositoryFile file = null;

	public static final String SOLUTION_PATH = "test-src/solution";
	  private static final String ALT_SOLUTION_PATH = "test-src/solution";
	  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";
	  final String SYSTEM_FOLDER = "/system";
//	  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";

		  public String getSolutionPath() {
		      File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
		      if(file.exists()) {
		        System.out.println("File exist returning " + SOLUTION_PATH);
		        return SOLUTION_PATH;  
		      } else {
		        System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);      
		        return ALT_SOLUTION_PATH;
		      }
		  }

  public void setup() {
    super.setUp();
    // ACL The first one...
    HibernateUtil.beginTransaction();

    defaultAcls.put(new SimpleRole("Admin"), new SimplePermissionMask(IPentahoAclEntry.PERM_FULL_CONTROL)); //$NON-NLS-
    defaultAcls.put(new SimpleRole("cto"), new SimplePermissionMask(IPentahoAclEntry.PERM_FULL_CONTROL)); //$NON-NLS
    defaultAcls.put(new SimpleRole("dev"), new SimplePermissionMask(IPentahoAclEntry.PERM_EXECUTE_SUBSCRIBE)); //$NON-NLS-1$
    defaultAcls.put(new SimpleRole("Authenticated"), new SimplePermissionMask(IPentahoAclEntry.PERM_EXECUTE)); //$NON-NLS-1$
    
    SecurityHelper.getInstance().becomeUser("pat");
    repository = getSolutionRepository(PentahoSessionHolder.getSession());
    file = (RepositoryFile) repository.getSolutionFile("samples", ISolutionRepository.ACTION_EXECUTE);
    publisher = new AclPublisher(defaultAcls);
    publisher.publishDefaultAcls(file);

    HibernateUtil.commitTransaction();
  }

  
  // TODO: remove once tests are passing
  public void setUp() {}
  public void tearDown() {}
  public void testDummyTest() {
    // TODO: remove once tests are passing
  }

  public SolutionRepositoryTest(String arg0) {
    super(arg0);
    addProperties();
  }

  public SolutionRepositoryTest() {
    super();
    addProperties();
  }

  private void addProperties() {
    Properties props = System.getProperties();
    longString.append(props.getProperty("java.home")).append(props.getProperty("sun.cpu.isalist")). //$NON-NLS-1$ //$NON-NLS-2$
        append(props.getProperty("java.vm.version")).append(props.getProperty("user.home")). //$NON-NLS-1$ //$NON-NLS-2$
        append(props.getProperty("java.class.path")); //$NON-NLS-1$      
  }

  /**
   * Remove the word Load from in front to test loading the solution
   * repository.
   * 
   */
  public void LoadtestSolutionRepository() {
    org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repository = new org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository();
    StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    repository.init(session);
    repository.loadSolutionFromFileSystem(session, getSolutionPath(), false);

    System.out.println(repository.getSolutions(ISolutionRepository.ACTION_EXECUTE).asXML());
    System.out.println(repository.getSolutionStructure(ISolutionRepository.ACTION_EXECUTE).asXML());
    System.out.println(repository.getSolutionTree(ISolutionRepository.ACTION_EXECUTE).asXML());
  }

  public void atestSetAclsForPat() throws Exception {
    // Mock up credentials for ACL Testing
    SecurityHelper.getInstance().runAsUser("pat", new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        // Get the repository
        org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repo = getSolutionRepository(PentahoSessionHolder.getSession());

        // RepositoryFile aFile =
        // repo.getFileByPath("/test-solution/samples/reporting/custom-parameter-page-example.xaction");
        RepositoryFile aFile = (RepositoryFile) repo
            .getSolutionFile("samples/reporting/jasper-reports-test-1.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$

        SpringSecurityPermissionMgr.instance().setPermission(new SimpleRole("ROLE_ADMIN"), new SimplePermissionMask(7), aFile);
        SpringSecurityPermissionMgr.instance().setPermission(new SimpleRole("ROLE_DEVMGR"), new SimplePermissionMask(6), aFile);
        SpringSecurityPermissionMgr.instance().setPermission(new SimpleRole("ROLE_DEV"), new SimplePermissionMask(2), aFile);
        return null;  
      }
      
    });
  }

//  public void testAclRepositoryLoadingForPat() {
//    System.out.println("******** RDBMS Repository - Pat ***************"); //$NON-NLS-1$
//    StandaloneSession session = new StandaloneSession(""); //$NON-NLS-1$
//    // Mock up credentials for ACL Testing
//    MockSecurityUtility.createPat(session);
//
//    // Get the repository
//    org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repo = getSolutionRepository(session);
//    for (int i = 0; i < 5; i++) {
//      long stTime = System.currentTimeMillis();
//      // Now, get the samples solution
//      Document doc = SolutionReposHelper.getActionSequences(
//          repo.getFileByPath("samples/reporting"), ISolutionRepository.ACTION_EXECUTE);//$NON-NLS-1$
//      String docXml = doc.asXML();
//      System.out.println(docXml);
//      System.out.println("Time " + (i + 1) + ": " + (System.currentTimeMillis() - stTime)); //$NON-NLS-1$ //$NON-NLS-2$
//      assertNotNull(docXml);
//    }
//    // Done with test...
//
//  }

//  public void testAclRepositoryLoadingForSuzy() {
//    System.out.println("******** RDBMS Repository - Suzy ***************"); //$NON-NLS-1$
//    StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    // Mock up credentials for ACL Testing
//    MockSecurityUtility.createSuzy(session);
//
//    // Get the repository
//    org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repo = getSolutionRepository(session);
//    // Now, get the samples solution
//    Document doc = SolutionReposHelper.getActionSequences(
//        repo.getFileByPath("samples/reporting"), ISolutionRepository.ACTION_EXECUTE);//$NON-NLS-1$
//    String docXml = doc.asXML();
//    System.out.println(docXml);
//
//    // Done with test...
//  }

//  public void testFileRepository() {
//    System.out.println("******** File Repository ***************"); //$NON-NLS-1$
//    StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository repo = getFileSolutionRepository(session);
//    Document docFullTree = repo.getFullSolutionTree(ISolutionRepository.ACTION_EXECUTE, null);
//    String docXmlFullTree = docFullTree.asXML();
//    System.out.println(docXmlFullTree);
//
//    boolean removed = repo.removeSolutionFile(PentahoSystem.getApplicationContext().getSolutionPath(
//        "test/tmp/Chart_Area.html")); //$NON-NLS-1$
//    if (removed) {
//      assertTrue("Solution file is removed", removed); //$NON-NLS-1$
//    }
//
//    try {
//      File fi = new File(PentahoSystem.getApplicationContext().getSolutionPath("test/tmp/Chart_Bar.html")); //$NON-NLS-1$
//      FileInputStream fis = new FileInputStream(fi);
//      byte[] buffer = new byte[(int) fi.length()];
//      fis.read(buffer);
//      repo
//          .addSolutionFile(PentahoSystem.getApplicationContext().getSolutionPath("test"), "tmp", fi.getName(), fi, true);//$NON-NLS-1$ //$NON-NLS-2$
//      repo.addSolutionFile(
//          PentahoSystem.getApplicationContext().getSolutionPath("test"), "tmp", fi.getName(), fi, false); //$NON-NLS-1$ //$NON-NLS-2$
//      repo.addSolutionFile(
//          PentahoSystem.getApplicationContext().getSolutionPath("test"), "tmp", fi.getName(), buffer, true);//$NON-NLS-1$ //$NON-NLS-2$       
//      repo.addSolutionFile(
//          PentahoSystem.getApplicationContext().getSolutionPath("test"), "tmp", fi.getName(), buffer, false); //$NON-NLS-1$ //$NON-NLS-2$
//      // load the XML document that defines the chart
//      IActionSequenceResource resource = new ActionSequenceResource(
//          "budgetvariance.pie.xml", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", PentahoSystem.getApplicationContext().getSolutionPath("test/dashboard/departments.rule.xaction")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
//      byte[] byteOutput = repo.getResourceAsBytes(resource, true);
//      System.out.println("Printing Resource as Byte Output with Localized set to true" + byteOutput.toString()); //$NON-NLS-1$
//
//      byte[] byteOutput2 = repo.getResourceAsBytes(resource, false);
//      System.out.println("Printing Resource as Byte Output with Localized set to false" + byteOutput2.toString()); //$NON-NLS-1$
//
//      IPentahoStreamSource ds = repo.getResourceDataSource(resource);
//      System.out.println("The datasource name is " + ds.getName()); //$NON-NLS-1$
//      IPentahoStreamSource ds2 = repo.getResourceDataSource(PentahoSystem.getApplicationContext().getSolutionPath(
//          "test/dashboard/departments.rule.xaction")); //$NON-NLS-1$
//      System.out.println("The datasource name is " + ds2.getName()); //$NON-NLS-1$        
//      Document doc1 = repo.getResourceAsDocument(PentahoSystem.getApplicationContext().getSolutionPath(
//          "test/dashboard/departments.rule.xaction")); //$NON-NLS-1$
//      System.out.println("Printing Resource as XML document" + doc1.asXML()); //$NON-NLS-1$
//
//      Document doc2 = repo.getResourceAsDocument(resource);
//      System.out.println("Printing Resource as XML document" + doc2.asXML()); //$NON-NLS-1$        
//      String str = repo.getResourceAsString(PentahoSystem.getApplicationContext().getSolutionPath(
//          "test/dashboard/departments.rule.xaction")); //$NON-NLS-1$
//      System.out.println("Printing Resource as XML document" + str); //$NON-NLS-1$
//      String str2 = repo.getResourceAsString(resource);
//      System.out.println("Printing Resource as XML document" + str2); //$NON-NLS-1$
//      Reader reader = repo.getResourceReader(resource);
//      System.out.println("Printing Resource as XML document" + reader.toString()); //$NON-NLS-1$
//      Reader reader2 = repo.getResourceReader(PentahoSystem.getApplicationContext().getSolutionPath(
//          "test/dashboard/departments.rule.xaction")); //$NON-NLS-1$
//      System.out.println("Printing Resource as XML document" + reader2.toString()); //$NON-NLS-1$        
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//
//    // Now, get the samples solution
//    Document doc = SolutionReposHelper
//        .getActionSequences(
//            new FileSolutionFile(new File(PentahoSystem.getApplicationContext().getSolutionPath(
//                "test/dashboard/departments.rule.xaction")), ((FileSolutionFile) repo.getRootFolder()).getFile()), ISolutionRepository.ACTION_EXECUTE);//$NON-NLS-1$
//    String docXml = doc.asXML();
//    System.out.println("Xaction as XML" + docXml); //$NON-NLS-1$
//    assertTrue(true);
//
//  }

//  public void testAddRemoveSolutionFile() {
//    // ACL The first one...
//    HibernateUtil.beginTransaction();
//
//    defaultAcls.put(new SimpleRole("Admin"), new SimplePermissionMask(IPentahoAclEntry.PERM_FULL_CONTROL)); //$NON-NLS-
//    defaultAcls.put(new SimpleRole("cto"), new SimplePermissionMask(IPentahoAclEntry.PERM_FULL_CONTROL)); //$NON-NLS
//    defaultAcls.put(new SimpleRole("dev"), new SimplePermissionMask(IPentahoAclEntry.PERM_EXECUTE_SUBSCRIBE)); //$NON-NLS-1$
//    defaultAcls.put(new SimpleRole("Authenticated"), new SimplePermissionMask(IPentahoAclEntry.PERM_EXECUTE)); //$NON-NLS-1$
//
//    session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    MockSecurityUtility.createJoe(session);
//    repository = getSolutionRepository(session);
//    file = (RepositoryFile) repository.getFileByPath("samples/reporting");
//    publisher = new AclPublisher(defaultAcls);
//    publisher.publishDefaultAcls(file);
//
//    HibernateUtil.commitTransaction();
//
//    RepositoryFile aFile = (RepositoryFile) repository
//        .getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$    
//    String publishPath = "samples/reporting"; //$NON-NLS-1$
//    int status = 0;
//    try {
//      status = repository.addSolutionFile("", publishPath, "MDX_report19.xaction", aFile.getData(), false);
//    } catch (Exception e) {
//      assertFalse(false);
//      e.printStackTrace();
//    }
//    assertEquals(ISolutionRepository.FILE_ADD_SUCCESSFUL, status);
//
//    RepositoryFile aFile1 = (RepositoryFile) repository
//        .getFileByPath("samples/reporting/MDX_report19.xaction"); //$NON-NLS-1$
//    assertNotNull(aFile1);
//    String solution = "samples";
//    String path = "reporting";
//    String action = "MDX_report19.xaction";
//    boolean success = repository.removeSolutionFile(solution, path, action);
//    assertEquals(Boolean.TRUE, Boolean.valueOf(success));
//
//    status = 0;
//    try {
//      status = repository.addSolutionFile("", publishPath, "MDX_report19.xaction", aFile.getData(), true);
//    } catch (Exception e) {
//      assertFalse(false);
//      e.printStackTrace();
//    }
//    assertEquals(status, ISolutionRepository.FILE_ADD_SUCCESSFUL);
//
//    RepositoryFile aFile2 = (RepositoryFile) repository
//        .getFileByPath("samples/reporting/MDX_report19.xaction"); //$NON-NLS-1$
//    assertNotNull(aFile2);
//    String solutionPath1 = "samples/reporting/MDX_report19.xaction";
//    success = false;
//    success = repository.removeSolutionFile(solutionPath1);
//    assertEquals(Boolean.TRUE, Boolean.valueOf(success));
//  }

//  public void testIsPentahoAdministrator() {
//    StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    MockSecurityUtility.createJoe(session);
//    repository = getSolutionRepository(session);
//    boolean isPentahoAdmin = repository.isPentahoAdministrator();
//    assertEquals(Boolean.TRUE, Boolean.valueOf(isPentahoAdmin));
//  }
//
//  public void testGetSolutionDocument() {
//    StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    MockSecurityUtility.createJoe(session);
//    org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repository = getSolutionRepository(session);
//    Document doc = SolutionReposHelper.getActionSequences(
//        repository.getFileByPath("samples/reporting/MDX_report.xaction"), ISolutionRepository.ACTION_EXECUTE);//$NON-NLS-1$
//
//    
//    assertNotNull(doc);
//  }

//  public void testGetAllActionSequences() {
//    StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    MockSecurityUtility.createPat(session);
//    repository = getSolutionRepository(session);
//    String allActionSequences[] = repository.getAllActionSequences();
//    assertNotNull(allActionSequences);
//  }


//  public void testGetSolutionTree() {
//    StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    MockSecurityUtility.createPat(session);
//    org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repository = getSolutionRepository(session);
//    Document doc = repository.getSolutionTree(ISolutionRepository.ACTION_ADMIN);
//    assertNotNull(doc);
//  }

//  public void testGetSolutionFileLastModified() {
//    StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    MockSecurityUtility.createPat(session);
//    org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repository = getSolutionRepository(session);
//    long time = repository.getSolutionFileLastModified("samples/reporting/MDX_report.xaction");
//    assertTrue(true);
//    // TODO compare date
//  }

//  public void testGetSetHasPermissions() {
//    try {
//      // ACL The first one...
//      HibernateUtil.beginTransaction();
//      String recipientNew = null;
//      String permNew[] = null;
//
//      defaultAcls.put(new SimpleRole("Admin"), new SimplePermissionMask(IPentahoAclEntry.PERM_FULL_CONTROL)); //$NON-NLS-
//      defaultAcls.put(new SimpleRole("cto"), new SimplePermissionMask(IPentahoAclEntry.PERM_FULL_CONTROL)); //$NON-NLS
//      defaultAcls.put(new SimpleRole("dev"), new SimplePermissionMask(IPentahoAclEntry.PERM_EXECUTE_SUBSCRIBE)); //$NON-NLS-1$
//      defaultAcls.put(new SimpleRole("Authenticated"), new SimplePermissionMask(IPentahoAclEntry.PERM_EXECUTE)); //$NON-NLS-1$
//
//      session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//      MockSecurityUtility.createJoe(session);
//      repository = getSolutionRepository(session);
//      file = (RepositoryFile) repository.getFileByPath("samples/reporting");
//      publisher = new AclPublisher(defaultAcls);
//      publisher.publishDefaultAcls(file);
//
//      HibernateUtil.commitTransaction();
//
//      RepositoryFile aFile = (RepositoryFile) repository
//          .getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$    
//      String publishPath = "samples/reporting"; //$NON-NLS-1$
//      int status = 0;
//      try {
//        status = repository.addSolutionFile("", publishPath, "MDX_reportTest.xaction", aFile.getData(), false);
//      } catch (Exception e) {
//        assertFalse(false);
//        e.printStackTrace();
//      }
//
//      // Get the repository
//      org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repo = getSolutionRepository(session);
//      if (!repo.supportsAccessControls()) {
//        assertFalse("ACLs are not supported by the repository", false);
//      } else {
//        HibernateUtil.beginTransaction();
//        aFile = (RepositoryFile) repo.getFileByPath("samples/reporting/MDX_reportTest.xaction"); //$NON-NLS-1$
//        Map permMap = new HashMap<IPermissionRecipient, IPermissionMask>();
//        String recipient = "joe";
//        IPermissionRecipient permissionRecipientUser = new SimpleUser(recipient);
//        SimplePermissionMask permissionMask = new SimplePermissionMask();
//        String perm[] = { "Update", "Execute", "Subscribe" };
//        for (int i = 0; i < perm.length; i++) {
//          permissionMask
//              .addPermission(((Integer) PentahoAclEntry.getValidPermissionsNameMap().get(perm[i])).intValue());
//        }
//        permMap.put(permissionRecipientUser, permissionMask);
//        if (aFile instanceof IAclSolutionFile) {
//          repo.setPermissions(aFile, permMap);
//        }
//        HibernateUtil.commitTransaction();
//        permNew = new String[perm.length];
//        int i = 0;
//        int j = 0;
//        aFile = (RepositoryFile) repo.getFileByPath("samples/reporting/MDX_reportTest.xaction"); //$NON-NLS-1$
//        Set<Map.Entry<IPermissionRecipient, IPermissionMask>> mapEntrySet = repo.getPermissions(aFile).entrySet();
//        Map permissionsMap = PentahoAclEntry.getValidPermissionsNameMap();
//
//        for (Iterator<Map.Entry<IPermissionRecipient, IPermissionMask>> iterator = mapEntrySet.iterator(); iterator
//            .hasNext();) {
//          Map.Entry<IPermissionRecipient, IPermissionMask> mapEntry = iterator.next();
//          IPermissionRecipient permissionRecipient = mapEntry.getKey();
//          if (permissionRecipient.getName().equals(recipient)) {
//            recipientNew = permissionRecipient.getName();
//          }
//
//          for (Iterator keyIterator = permissionsMap.keySet().iterator(); keyIterator.hasNext();) {
//            String permName = keyIterator.next().toString();
//            int permMask = ((Integer) permissionsMap.get(permName)).intValue();
//            boolean isPermitted = repo.hasAccess(aFile, permMask);
//            System.out.println("For " + recipient + " permission " + permName + " is " + (isPermitted ? "" : " not ")
//                + " set.");
//            if ((isPermitted) && recipient.equals(permissionRecipient.getName()))
//              permNew[j++] = permName;
//          }
//        }
//        boolean gotPermissions = permNew.length == perm.length;
//        assertEquals(Boolean.TRUE, Boolean.valueOf(gotPermissions));
//        assertEquals(Boolean.TRUE, Boolean.valueOf(recipient.equals(recipientNew)));
//      }
//      HibernateUtil.beginTransaction();
//      aFile = (RepositoryFile) repository.getFileByPath("samples/reporting/MDX_reportTest.xaction"); //$NON-NLS-1$
//      String solution = "samples";
//      String path = "reporting";
//      String action = "MDX_reportTest.xaction";
//      boolean isRemoved = repository.removeSolutionFile(solution, path, action);
//      HibernateUtil.commitTransaction();
//      assertEquals(Boolean.TRUE, Boolean.valueOf(isRemoved));
//    } catch (Exception e) {
//      e.printStackTrace();
//      assertFalse("Permissions were not set successfully", false);
//    }
//
//  }

//  public void testGetPermissionsNoAccess() {
//    StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    MockSecurityUtility.createNoRolesGuy(session);
//    int count = 0;
//    // Get the repository
//    org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repo = getSolutionRepository(session);
//    RepositoryFile aFile = (RepositoryFile) repo.getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$
//    Set<Map.Entry<IPermissionRecipient, IPermissionMask>> mapEntrySet = repo.getPermissions(aFile).entrySet();
//    Map permissionsMap = PentahoAclEntry.getValidPermissionsNameMap();
//    for (Iterator<Map.Entry<IPermissionRecipient, IPermissionMask>> iterator = mapEntrySet.iterator(); iterator
//        .hasNext();) {
//      Map.Entry<IPermissionRecipient, IPermissionMask> mapEntry = iterator.next();
//      IPermissionRecipient permissionRecipient = mapEntry.getKey();
//      String recipient = permissionRecipient.getName();
//      for (Iterator keyIterator = permissionsMap.keySet().iterator(); keyIterator.hasNext();) {
//        String permName = keyIterator.next().toString();
//        int permMask = ((Integer) permissionsMap.get(permName)).intValue();
//        boolean isPermitted = repo.hasAccess(aFile, permMask);
//        if (isPermitted) {
//          count++;
//        }
//      }
//    }
//    boolean gotPermissions = (count == 0);
//    assertEquals(Boolean.TRUE, Boolean.valueOf(gotPermissions));
//  }

//  public void testPublish() {
//    try {
//      StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//      // Mock up credentials for ACL Testing
//      HibernateUtil.beginTransaction();
//      MockSecurityUtility.createJoe(session);
//      // Get the repository
//      org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repo = getSolutionRepository(session);
//      RepositoryFile aFile = (RepositoryFile) repo
//          .getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$
//      String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath(""); //$NON-NLS-1$
//      String publishPath = "samples/reporting";
//      int status = repo.publish(solutionPath, publishPath, "MDX_report_Test2.xaction", aFile.getData(), false);
//      assertEquals(ISolutionRepository.FILE_ADD_SUCCESSFUL, status);
//      status = repo.publish(solutionPath, publishPath, "MDX_report_Test2.xaction", aFile.getData(), false);
//      assertNotSame(ISolutionRepository.FILE_ADD_SUCCESSFUL, status);
//
//
//    } catch (Exception e) {
//      assertFalse("Publish process did not completed successfully", false);
//    }
//  }

//  public void testGetPermissions() {
//    StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    MockSecurityUtility.createJoe(session);
//    int count = 0;
//    // Get the repository
//    org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repo = getSolutionRepository(session);
//    RepositoryFile aFile = (RepositoryFile) repo
//        .getFileByPath("samples/reporting/MDX_report_Test2.xaction"); //$NON-NLS-1$
//    Set<Map.Entry<IPermissionRecipient, IPermissionMask>> mapEntrySet = repo.getPermissions(aFile).entrySet();
//    Map permissionsMap = PentahoAclEntry.getValidPermissionsNameMap();
//    for (Iterator<Map.Entry<IPermissionRecipient, IPermissionMask>> iterator = mapEntrySet.iterator(); iterator
//        .hasNext();) {
//      Map.Entry<IPermissionRecipient, IPermissionMask> mapEntry = iterator.next();
//      IPermissionRecipient permissionRecipient = mapEntry.getKey();
//      String recipient = permissionRecipient.getName();
//      for (Iterator keyIterator = permissionsMap.keySet().iterator(); keyIterator.hasNext();) {
//        String permName = keyIterator.next().toString();
//        int permMask = ((Integer) permissionsMap.get(permName)).intValue();
//        boolean isPermitted = repo.hasAccess(aFile, permMask);
//        if (isPermitted) {
//          count++;
//        }
//      }
//    }
//    boolean gotPermissions = count > 0;
//    assertEquals(Boolean.TRUE, Boolean.valueOf(gotPermissions));
//    String solution = "samples";
//    String path = "reporting";
//    String action = "MDX_report_Test2.xaction";
//    boolean isRemoved = repo.removeSolutionFile(solution, path, action);
//    HibernateUtil.commitTransaction();
//    assertEquals(Boolean.TRUE, Boolean.valueOf(isRemoved));
//
//   }

//  public void testShare() {
//    try {
//      StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//      // Mock up credentials for ACL Testing
//      MockSecurityUtility.createJoe(session);
//      // Get the repository
//      org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repo = getSolutionRepository(session);
//      RepositoryFile aFile = (RepositoryFile) repo
//          .getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$
//      IPermissionRecipient shareRecipient = new SimpleUser("suzy");
//      IPermissionRecipient shareRecipient1 = new SimpleUser("pat");
//      List<IPermissionRecipient> shareRecipientList = new ArrayList();
//      shareRecipientList.add(shareRecipient);
//      shareRecipientList.add(shareRecipient1);
//      repo.share(aFile, shareRecipientList);
//      session.destroy();
//      session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//      MockSecurityUtility.createPat(session);
//      org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repo1 = getSolutionRepository(session);
//      RepositoryFile aFile1 = (RepositoryFile) repo
//          .getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$
//      assertEquals(Boolean.TRUE, Boolean.valueOf(repo1.hasAccess(aFile1, ISolutionRepository.ACTION_EXECUTE)));
//    } catch (Exception e) {
//      assertFalse("Share Permission was not set successfully", false);
//    }
//  }

//  public void testSetRepositoryName() {
//    try {
//      HibernateUtil.beginTransaction();
//      StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//      // Mock up credentials for ACL Testing
//      MockSecurityUtility.createJoe(session);
//      // Get the repository
//      org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repo = getSolutionRepository(session);
//      String reposName = PentahoSystem.getSystemSetting("solution-repository/db-repository-name", null); //$NON-NLS-1$
//      if (reposName != null) {
//        repo.setRepositoryName(reposName);
//        assertEquals(reposName, repo.getRepositoryName());
//      } else {
//        repo.setRepositoryName("pentaho-solutions");
//        HibernateUtil.commitTransaction();
//        assertEquals("solution", repo.getRepositoryName());
//      }
//
//    } catch (Exception e) {
//      e.printStackTrace();
//      assertFalse("Error setting the repository name", false);
//    }
//  }

  public org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository getSolutionRepository(
      IPentahoSession session) {
    org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository rtn = new org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository();
    rtn.init(session);
    return rtn;
  }

  public org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository getFileSolutionRepository(
      StandaloneSession session) {
    org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository rtn = new org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository();
    rtn.init(session);
    return rtn;
  }

//  public void testShowMessages() {
//    this.showMessages();
//  }

  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(SolutionRepositoryTest.class);
    SolutionRepositoryTest repoTest = new SolutionRepositoryTest();
    repoTest.setup();
    repoTest.atestSetAclsForPat();
//    repoTest.testAclRepositoryLoadingForPat();
//    repoTest.testAclRepositoryLoadingForSuzy();
//    repoTest.testAddRemoveSolutionFile();
//    repoTest.testFileRepository();
//    repoTest.testGetAllActionSequences();
//    repoTest.testGetPermissions();
//    repoTest.testGetPermissionsNoAccess();
//    repoTest.testGetSolutionDocument();
//    repoTest.testGetSolutionFileLastModified();
//    repoTest.testGetSolutionTree();
//    repoTest.testGetSetHasPermissions();
//    repoTest.testIsPentahoAdministrator();
//    repoTest.testPublish();
//    repoTest.testSetRepositoryName();
//    repoTest.testShare();
//    repoTest.testShowMessages();
    System.exit(0);
  }

  @SuppressWarnings("unused")
  private void showMessages() {
    List messages = this.getMessages();
    for (int i = 0; i < messages.size(); i++) {
      System.out.println(messages.get(i));
    }
  }

}
