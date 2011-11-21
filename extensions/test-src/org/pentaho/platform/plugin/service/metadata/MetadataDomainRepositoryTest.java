package org.pentaho.platform.plugin.service.metadata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryMatchers.isLikeFile;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.plugin.services.metadata.MetadataDomainRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;

@SuppressWarnings("nls")
public class MetadataDomainRepositoryTest {

  @Test
  public void testMetadataDomainRepository() throws Exception {
    MicroPlatform microPlatform = new MicroPlatform();

    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    final String metadataFolderPath = "/public/pentaho-solutions/metadata";
    final String metadataFolderId = "abc";
    doReturn(new RepositoryFile.Builder(metadataFolderId, "metadata").path(metadataFolderPath).folder(true).build())
        .when(repo).getFile(metadataFolderPath);
    final String path = metadataFolderPath + RepositoryFile.SEPARATOR + "steel-wheels.xmi";
    final RepositoryFile steelWheelsXmiFile = new RepositoryFile.Builder("456", "steel-wheels.xmi").path(path).build();
    doReturn(steelWheelsXmiFile).when(repo).getFile(path);
    doReturn(
        new SimpleRepositoryFileData(FileUtils.openInputStream(new File(
            "test-res/MetadataDomainRepositoryTest/public/pentaho-solutions/metadata/steel-wheels.xmi")), "UTF-8",
            "text/xml")).when(repo).getDataForRead("456", SimpleRepositoryFileData.class);

    doReturn(Arrays.asList(steelWheelsXmiFile)).when(repo).getChildren(metadataFolderId, "*.xmi");

    microPlatform.defineInstance(IUnifiedRepository.class, repo);

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

    // verify
    verify(repo).updateFile(argThat(isLikeFile(steelWheelsXmiFile)), any(IRepositoryFileData.class), anyString());

  }
}
