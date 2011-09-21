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
 */
package org.pentaho.platform.repository2.unified.webservices.jaxws;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.JackrabbitRepositoryTestBase;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sun.xml.ws.developer.JAXWSProperties;

/**
 * JUnit 4 test for web services.
 * 
 * <p>
 * Note the RunWith annotation that uses a special runner that knows how to setup a Spring application context. The 
 * application context config files are listed in the ContextConfiguration annotation. By implementing 
 * {@link ApplicationContextAware}, this unit test can access various beans defined in the application context, 
 * including the bean under test.
 * </p>
 * 
 * @author mlowery
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" })
@SuppressWarnings("nls")
public class DefaultUnifiedRepositoryJaxwsWebServiceTest extends JackrabbitRepositoryTestBase implements
    ApplicationContextAware {
  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  /**
   * Server-side IUnifiedRepository.
   */
  private IUnifiedRepository _repo;

  /**
   * Client-side IUnifiedRepository.
   */
  private IUnifiedRepository repo;

  private boolean startupCalled;

  // ~ Constructors ==================================================================================================== 

  public DefaultUnifiedRepositoryJaxwsWebServiceTest() throws Exception {
    super();
  }

  // ~ Methods =========================================================================================================

  @BeforeClass
  public static void setUpClass() throws Exception {
    // unfortunate reference to superclass
    JackrabbitRepositoryTestBase.setUpClass();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    JackrabbitRepositoryTestBase.tearDownClass();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    startupCalled = true;

    String address = "http://localhost:9000/repo";
    Endpoint.publish(address, new DefaultUnifiedRepositoryJaxwsWebService(_repo));

    Service service = Service.create(new URL("http://localhost:9000/repo?wsdl"), new QName(
        "http://www.pentaho.org/ws/1.0", "unifiedRepository"));

    IUnifiedRepositoryJaxwsWebService repoWebService = service.getPort(IUnifiedRepositoryJaxwsWebService.class);

    // next two lines not needed since we manually populate filters
    //((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "suzy");
    //((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "password");
    // accept cookies to maintain session on server
    ((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
    // support streaming binary data
    ((BindingProvider) repoWebService).getRequestContext().put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
    SOAPBinding binding = (SOAPBinding) ((BindingProvider) repoWebService).getBinding();
    binding.setMTOMEnabled(true);

    repo = new UnifiedRepositoryToWebServiceAdapter(repoWebService);

    populateHolders();

  }

  /**
   * Simulates Spring Security filters that populate SecurityContextHolder and Pentaho filters that populate 
   * PentahoSessionHolder.
   */
  private static void populateHolders() {
    final String username = "suzy";
    StandaloneSession pentahoSession = new StandaloneSession(username);
    pentahoSession.setAuthenticated(username);
    final GrantedAuthority[] authorities = new GrantedAuthority[2];
    authorities[0] = new GrantedAuthorityImpl("Authenticated");
    authorities[1] = new GrantedAuthorityImpl("acme_Authenticated");
    final String password = "ignored";
    UserDetails userDetails = new User(username, password, true, true, true, true, authorities);
    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, password, authorities);
    // for PentahoSessionCredentialsStrategy
    PentahoSessionHolder.setSession(pentahoSession);
    // for Spring Security method security on IUnifiedRepository
    pentahoSession.setAttribute(IPentahoSession.TENANT_ID_KEY, "acme");
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  //  protected void cleanup() throws Exception {
  //    RepositoryFile folder1 = repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/folder1");
  //    if (folder1 != null) {
  //      repo.deleteFile(folder1.getId(), true, null);
  //    }
  //  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();

    if (startupCalled) {
      manager.shutdown();
    }

    // null out fields to get back memory
    repo = null;
  }

  @Test
  public void testEverything() throws Exception {
    manager.startup();
    setUpRoleBindings();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    TestUtils.testEverything(repo);
  }
  
  @Test
  public void testGetDataForReadInBatch() throws Exception {
    manager.startup();
    setUpRoleBindings();
    login(USERNAME_SUZY, TENANT_ID_ACME);

    final String string1 = "string1";
    final String string2 = "string2";
    final String prop = "prop";

    final RepositoryFile parentFolder = repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY));
    final DataNode node1 = new DataNode("testNode1");
    node1.setProperty(prop, string1);
    final NodeRepositoryFileData data1 = new NodeRepositoryFileData(node1);
    final RepositoryFile file1 = repo.createFile(parentFolder.getId(), new RepositoryFile.Builder("test.file").versioned(true).build(), data1, null);

    final DataNode node2 = new DataNode("testNode2");
    node2.setProperty(prop, string2);
    final NodeRepositoryFileData data2 = new NodeRepositoryFileData(node2);
    final RepositoryFile file2 = repo.createFile(parentFolder.getId(), new RepositoryFile.Builder("test.file2").build(), data2, null);

    List<NodeRepositoryFileData> result = repo.getDataForReadInBatch(Arrays.asList(file1, file2), NodeRepositoryFileData.class);
    assertEquals(2, result.size());
    assertEquals(node1.getProperty(prop).getString(), result.get(0).getNode().getProperty(prop).getString());
    assertEquals(node2.getProperty(prop).getString(), result.get(1).getNode().getProperty(prop).getString());
  }
  
//  @Test
//  public void testMetadata() throws Exception {
//    manager.startup();
//    setUpRoleBindings();
//    login(USERNAME_SUZY, TENANT_ID_ACME);
//
//    final String string1 = "string1";
//    final String prop = "prop";
//
//    final RepositoryFile parentFolder = repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY));
//    final DataNode node1 = new DataNode("testNode1");
//    node1.setProperty(prop, string1);
//    final NodeRepositoryFileData data1 = new NodeRepositoryFileData(node1);
//    final RepositoryFile file1 = repo.createFile(parentFolder.getId(), new RepositoryFile.Builder("test.file").versioned(true).build(), data1, null);
//    FileMetadataMap metadataMap = new FileMetadataMap();
//    repo.setFileMetadata(file1.getId(), metadataMap);
//    repo.getFileMetadata(file1.getId());
//  }
  
  @Test
  public void testGetVersionSummaryInBatch() throws Exception {
    manager.startup();
    setUpRoleBindings();
    login(USERNAME_SUZY, TENANT_ID_ACME);

    final String string1 = "string1";
    final String string2 = "string2";
    final String prop = "prop";

    final RepositoryFile parentFolder = repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY));
    final DataNode node1 = new DataNode("testNode1");
    node1.setProperty(prop, string1);
    final NodeRepositoryFileData data1 = new NodeRepositoryFileData(node1);
    RepositoryFile file1 = repo.createFile(parentFolder.getId(), new RepositoryFile.Builder("test.file").versioned(true).build(), data1, "original version");
    file1 = repo.updateFile(file1, data1, "updated");

    final DataNode node2 = new DataNode("testNode2");
    node2.setProperty(prop, string2);
    final NodeRepositoryFileData data2 = new NodeRepositoryFileData(node2);
    final String file2Message = "original";
    final RepositoryFile file2 = repo.createFile(parentFolder.getId(), new RepositoryFile.Builder("test.file2").versioned(true).build(), data2, file2Message);

    List<VersionSummary> result = repo.getVersionSummaryInBatch(Arrays.asList(file1, file2));
    assertEquals(2, result.size());
    assertEquals(file1.getVersionId(), result.get(0).getId());
    assertEquals(file2.getVersionId(), result.get(1).getId());
    assertEquals(file2Message, result.get(1).getMessage());
  }

  private void setUpRoleBindings() {
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    super.setApplicationContext(applicationContext);
    _repo = (IUnifiedRepository) applicationContext.getBean("unifiedRepository");
  }

}
