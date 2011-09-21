package org.pentaho.platform.repository2.unified.webservices.jaxws;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

import com.sun.xml.ws.developer.JAXWSProperties;

public class RepoClient {

  private IUnifiedRepository repo;

  @Before
  public void setUp() throws Exception {
    Service service = Service.create(new URL("http://localhost:8080/pentaho/webservices/unifiedRepository?wsdl"),
        new QName("http://www.pentaho.org/ws/1.0", "unifiedRepository"));

    IUnifiedRepositoryJaxwsWebService repoWebService = service.getPort(IUnifiedRepositoryJaxwsWebService.class);

    // basic auth
    ((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "suzy");
    ((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "password");
    // accept cookies to maintain session on server
    ((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
    // support streaming binary data
    ((BindingProvider) repoWebService).getRequestContext().put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
    SOAPBinding binding = (SOAPBinding) ((BindingProvider) repoWebService).getBinding();
    binding.setMTOMEnabled(true);

    repo = new UnifiedRepositoryToWebServiceAdapter(repoWebService);
  }

  @After
  public void tearDown() throws Exception {
    RepositoryFile folder1 = repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath("suzy") + "/folder1");
    if (folder1 != null) {
      repo.deleteFile(folder1.getId(), true, null);
    }
  }

  @Test
  public void testEverything() throws Exception {
    TestUtils.testEverything(repo);
  }

}
