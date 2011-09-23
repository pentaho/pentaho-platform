package org.pentaho.test.platform.web.http.api;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PlatformPlugin;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileReader;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileWriter;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.web.http.api.resources.SimpleJobScheduleRequest;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.security.userrole.ws.MockUserRoleListService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.grizzly.GrizzlyTestContainerFactory;

@SuppressWarnings("nls")
public class SchedulerResourceTest extends JerseyTest {

  public static final String USERNAME_JOE = "joe";

  private static MicroPlatform mp = new MicroPlatform("test-res/SchedulerResourceTest/");

  private static MicroPlatform.RepositoryModule repo;

  private static WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder(
      "org.pentaho.platform.web.http.api.resources").contextPath("api").build();

  public SchedulerResourceTest() throws Exception {
    super(webAppDescriptor);
    this.setTestContainerFactory(new GrizzlyTestContainerFactory());
    mp.setFullyQualifiedServerUrl(getBaseURI() + webAppDescriptor.getContextPath() + "/");
    mp.define(IPluginManager.class, DefaultPluginManager.class, Scope.GLOBAL);
    mp.define(IPluginProvider.class, JUnitContentGeneratorPluginProvider.class, Scope.GLOBAL);
    mp.define(IScheduler.class, QuartzScheduler.class, Scope.GLOBAL);
    mp.define(IUserRoleListService.class, MockUserRoleListService.class);
    PentahoSystem.get(IPluginManager.class).reload();
    ((QuartzScheduler)PentahoSystem.get(IScheduler.class)).start();
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    repo = mp.getRepositoryModule();
    repo.up();
  }

  @AfterClass
  public static void afterClass() {
    repo.down();
  }

  @Before
  public void beforeTest() {
    repo.login(USERNAME_JOE, TenantUtils.TENANTID_SINGLE_TENANT);
  }

  @After
  public void afterTest() {
    repo.logout();
  }

  @Test
  public void testScheduleFile() {
    IUnifiedRepository unifiedRepository = PentahoSystem.get(IUnifiedRepository.class);
    RepositoryFile parentFolder = unifiedRepository.getFile("/public");
    unifiedRepository.createFolder(parentFolder.getId(), new RepositoryFile.Builder("schedulerTest").folder(true).build(), "");
    
    RepositoryFileReader fileReader = null;
    try {
      RepositoryFileWriter fileWriter = new RepositoryFileWriter("/public/schedulerTest/inputFile.prpt", "UTF-8");
      fileWriter.write("hello world");
      fileWriter.close();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
    
    SimpleJobScheduleRequest jobScheduleRequest = new SimpleJobScheduleRequest();
    jobScheduleRequest.setInputFile("/public/schedulerTest/inputFile.prpt");
    jobScheduleRequest.setOutputFile("/public/schedulerTest/outputFile.txt");
    jobScheduleRequest.setJobTrigger(JobTrigger.ONCE_NOW);
    
    WebResource webResource = resource();
    ClientResponse postResponse = webResource.path("scheduler/job").type(MediaType.APPLICATION_XML).post(ClientResponse.class, jobScheduleRequest);
    sleep(90);
    RepositoryFile outputFile = unifiedRepository.getFile("/public/schedulerTest/outputFile.txt");
    Assert.assertNotNull(outputFile);
    try {
      fileReader = new RepositoryFileReader(outputFile);
      String outputString = IOUtils.toString(fileReader);
      Assert.assertEquals("hello world", outputString);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      Assert.fail();
    }
  }
  
  public static class JUnitContentGeneratorPluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins(IPentahoSession session) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin(new DefaultListableBeanFactory());
      p.setId("JUnitContentGeneratorPluginProvider");
      BeanDefinition def = BeanDefinitionBuilder.rootBeanDefinition(SchedulerTestAction.class.getName()).getBeanDefinition();
      p.getBeanFactory().registerBeanDefinition("prptAction", def);
      return Arrays.asList((IPlatformPlugin) p);
    }
  }
  
  private void sleep(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
