package org.pentaho.test.platform.datasource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.datasource.DatasourceServiceException;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.datasource.DatasourceInfo;
import org.pentaho.platform.datasource.MetadataDatasource;
import org.pentaho.platform.datasource.MetadataDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.plugin.services.metadata.MockSessionAwareMetadataDomainRepository;

public class MetadataDatasourceServiceTest extends TestCase{
  
private MicroPlatform microPlatform;
MetadataDatasourceService service;
  
  
  public static File createTempDirectory() throws IOException {
    final File temp;

    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

    if (!(temp.delete())) {
      throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
    }

    if (!(temp.mkdir())) {
      throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
    }

    return (temp);
  }

  @Before
  public void setUp() throws Exception {
    File tmpDir = createTempDirectory();
    File srcDir = new File("test-res/MetadataDomainRepositoryTest");
    FileUtils.copyDirectory(srcDir, tmpDir);
    microPlatform = new MicroPlatform("tests/integration-tests/resource/");
    microPlatform.define(IMetadataDomainRepository.class, MockSessionAwareMetadataDomainRepository.class, Scope.GLOBAL);
    microPlatform.define(IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class, Scope.GLOBAL);
    FileSystemBackedUnifiedRepository unifiedRepository = (FileSystemBackedUnifiedRepository)PentahoSystem.get(IUnifiedRepository.class, null);
    unifiedRepository.setRootDir(tmpDir);
    
  }


  @Test
  public void testAdd() throws Exception {
    try {
      service = new  MetadataDatasourceService(new MockSessionAwareMetadataDomainRepository(), new AdministratorAuthorizationPolicy());
      service.add(new MetadataDatasource(getTestDomain("myTestDomain.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain.xmi", MetadataDatasourceService.TYPE)), false);
      MetadataDatasource datasource = service.get("myTestDomain.xmi");
      assertNotNull(datasource);
      service.remove("myTestDomain.xmi");
    } catch(DatasourceServiceException e) {
      assertFalse(true);
    }
    
    try {
      service = new  MetadataDatasourceService(new MockSessionAwareMetadataDomainRepository(), new NonAdministratorAuthorizationPolicy());
      service.add(new MetadataDatasource(getTestDomain("myTestDomain.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain.xmi", MetadataDatasourceService.TYPE)), false);
      assertFalse(true);
    } catch(PentahoAccessControlException e) {
      assertTrue(true);
    }

  }

  @Test
  public void testEdit() throws Exception {
    try {
      service = new  MetadataDatasourceService(new MockSessionAwareMetadataDomainRepository(), new AdministratorAuthorizationPolicy());
      service.add(new MetadataDatasource(getTestDomain("myTestDomain.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain.xmi", MetadataDatasourceService.TYPE)), false);
      MetadataDatasource datasource = service.get("myTestDomain.xmi");
      assertNotNull(datasource);
      Domain domain = datasource.getDatasource();
      updateTestDomain(domain);
      service.update(new MetadataDatasource(domain, new DatasourceInfo(domain.getId(), domain.getId(), MetadataDatasourceService.TYPE)));
      service.remove("myTestDomain.xmi");
    } catch(DatasourceServiceException e) {
      assertFalse(true);
    }
    
    try {
      service.add(new MetadataDatasource(getTestDomain("myTestDomain.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain.xmi", MetadataDatasourceService.TYPE)), false);
      MetadataDatasource datasource = service.get("myTestDomain.xmi");
      assertNotNull(datasource);
      Domain domain = datasource.getDatasource();
      updateTestDomain(domain);
      service = new  MetadataDatasourceService(new MockSessionAwareMetadataDomainRepository(), new NonAdministratorAuthorizationPolicy());
      service.update(new MetadataDatasource(domain, new DatasourceInfo(domain.getId(), domain.getId(), MetadataDatasourceService.TYPE)));
      assertFalse(true);
    } catch(PentahoAccessControlException e) {
      service = new  MetadataDatasourceService(new MockSessionAwareMetadataDomainRepository(), new AdministratorAuthorizationPolicy());
      service.remove("myTestDomain.xmi");
      assertTrue(true);
    }

  }


  @Test
  public void testRemove() throws Exception {
    try {
      service = new  MetadataDatasourceService(new MockSessionAwareMetadataDomainRepository(), new AdministratorAuthorizationPolicy());
      service.add(new MetadataDatasource(getTestDomain("myTestDomain.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain.xmi", MetadataDatasourceService.TYPE)), false);
      MetadataDatasource datasource = service.get("myTestDomain.xmi");
      assertNotNull(datasource);
      service.remove("myTestDomain.xmi");
      MetadataDatasource removedDatasource = service.get("myTestDomain.xmi");
      assertEquals(removedDatasource, null);
    } catch(DatasourceServiceException e) {
      assertFalse(true);
    }
    
    try {
      service.add(new MetadataDatasource(getTestDomain("myTestDomain.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain.xmi", MetadataDatasourceService.TYPE)), false);
      MetadataDatasource datasource = service.get("myTestDomain.xmi");
      assertNotNull(datasource);
      service = new  MetadataDatasourceService(new MockSessionAwareMetadataDomainRepository(), new NonAdministratorAuthorizationPolicy());
      service.remove("myTestDomain.xmi");
      assertFalse(true);
    } catch(PentahoAccessControlException e) {
      assertTrue(true);
    }
  }
  
  @Test
  public void testList() throws Exception {
    try {
    service = new  MetadataDatasourceService(new MockSessionAwareMetadataDomainRepository(), new AdministratorAuthorizationPolicy());
    service.add(new MetadataDatasource(getTestDomain("myTestDomain1.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain1.xmi", MetadataDatasourceService.TYPE)), false);
    service.add(new MetadataDatasource(getTestDomain("myTestDomain2.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain2.xmi", MetadataDatasourceService.TYPE)), false);
    service.add(new MetadataDatasource(getTestDomain("myTestDomain3.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain3.xmi", MetadataDatasourceService.TYPE)), false);
    List<IDatasourceInfo> domainList = service.getIds();
    assertNotNull(domainList);
    assertEquals(domainList.size(), 3);
    } catch(DatasourceServiceException e) {
      assertFalse(true);
    }
    
    try {
        service.remove("myTestDomain1.xmi");
        service.remove("myTestDomain2.xmi");
        service.remove("myTestDomain3.xmi");
        service.add(new MetadataDatasource(getTestDomain("myTestDomain1.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain1.xmi", MetadataDatasourceService.TYPE)), false);
        service.add(new MetadataDatasource(getTestDomain("myTestDomain2.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain2.xmi", MetadataDatasourceService.TYPE)), false);
        service.add(new MetadataDatasource(getTestDomain("myTestDomain3.xmi"), new DatasourceInfo("myTestDomain", "myTestDomain3.xmi", MetadataDatasourceService.TYPE)), false);
        service = new  MetadataDatasourceService(new MockSessionAwareMetadataDomainRepository(), new NonAdministratorAuthorizationPolicy());
      } catch(PentahoAccessControlException e) {
        assertTrue(true);
      }    

  }
  
  private Domain getTestDomain(String id) {
    Domain d = new Domain();
    d.setId(id);
    return d;
  }
  
  private void updateTestDomain(Domain domain) {
    domain.setProperty("test", new String("Test"));
  }
  
  class AdministratorAuthorizationPolicy implements IAuthorizationPolicy {

    public AdministratorAuthorizationPolicy() {
      super();
      // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isAllowed(String actionName) {
      // TODO Auto-generated method stub
      return true;
    }

    @Override
    public List<String> getAllowedActions(String actionNamespace) {
      // TODO Auto-generated method stub
      return null;
    }
    
  }

  class NonAdministratorAuthorizationPolicy implements IAuthorizationPolicy {

    public NonAdministratorAuthorizationPolicy() {
      super();
      // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isAllowed(String actionName) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public List<String> getAllowedActions(String actionNamespace) {
      // TODO Auto-generated method stub
      return null;
    }
    
  }

}
