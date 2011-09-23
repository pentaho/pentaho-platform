package org.pentaho.platform.plugin.service.metadata;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.metadata.MetadataDomainRepository;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;

public class MetadataDomainRepositoryTest extends TestCase {

  private MicroPlatform microPlatform;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    File tmpDir = createTempDirectory();
    File srcDir = new File("test-res/MetadataDomainRepositoryTest");
    FileUtils.copyDirectory(srcDir, tmpDir);
    microPlatform = new MicroPlatform("tests/integration-tests/resource/");
    microPlatform.define(IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class, Scope.GLOBAL);
    FileSystemBackedUnifiedRepository unifiedRepository = (FileSystemBackedUnifiedRepository)PentahoSystem.get(IUnifiedRepository.class, null);
    unifiedRepository.setRootDir(tmpDir);
  }

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

  @Override
  protected void tearDown() throws Exception {
  }
  
  public void testMetadataDomainRepository() throws Exception
  {
    MetadataDomainRepository metaDomainRepository = new MetadataDomainRepository();
    Set<String> domainIds = metaDomainRepository.getDomainIds();
    assertTrue(domainIds.size() > 0);
    
    Domain domain = metaDomainRepository.getDomain("steel-wheels.xmi");
    assertNotNull(domain);
    assertNotNull(domain.findLogicalModel("BV_ORDERS"));
    
    metaDomainRepository.removeModel("steel-wheels.xmi", "BV_ORDERS");
    domain = metaDomainRepository.getDomain("steel-wheels.xmi");
    assertNotNull(domain);
    assertNull(domain.findLogicalModel("BV_ORDERS"));
    
    metaDomainRepository.reloadDomains();
    domain = metaDomainRepository.getDomain("steel-wheels.xmi");
    assertNotNull(domain);
    assertNull(domain.findLogicalModel("BV_ORDERS"));
  }

}
