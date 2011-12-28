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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *  
 * @author dkincade
 */
package org.pentaho.platform.repository2.unified.webservices.jaxws;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;

import junit.framework.TestCase;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class UnifiedRepositoryToWebServiceAdapterTest extends TestCase {
  private UnifiedRepositoryToWebServiceAdapter adapter;
  private IUnifiedRepositoryJaxwsWebService repositoryWS;
  private IUnifiedRepository repository;

  @Override
  protected void setUp() throws Exception {
    repository = new MockUnifiedRepository(new MockUserProvider());
    repositoryWS = new DefaultUnifiedRepositoryJaxwsWebService(repository);
    adapter = new UnifiedRepositoryToWebServiceAdapter(repositoryWS);
  }

  @Test
  public void testFileMetadata() throws Exception {
    final RepositoryFile testfile = repository.createFile(repository.getFile("/etc").getId(),
        new RepositoryFile.Builder("testfile").build(),
        new SimpleRepositoryFileData(new ByteArrayInputStream("test".getBytes()), "UTF-8", "text/plain"), null);

    {
      // Make sure the repository is setup correctly
      assertNotNull(testfile);
      assertNotNull(testfile.getId());
      final Map<String, Serializable> fileMetadata = repository.getFileMetadata(testfile.getId());
      assertNotNull(fileMetadata);
      assertEquals(0, fileMetadata.size());
    }

    final Map<String, Serializable> metadata = new HashMap<String, Serializable>();
    metadata.put("sample key", "sample value");
    metadata.put("complex key?", "\"an even more 'complex' value\"! {and them some}");

    adapter.setFileMetadata(testfile.getId(), metadata);

    {
      // Make sure the repository sees the metadata
      assertNotNull(testfile);
      assertNotNull(testfile.getId());
      final Map<String, Serializable> fileMetadata = repository.getFileMetadata(testfile.getId());
      assertNotNull(fileMetadata);
      assertEquals(2, fileMetadata.size());
    }

    {
      // Make sure we can get the same metadata back via the web service
      final Map<String, Serializable> fileMetadata = adapter.getFileMetadata(testfile.getId());
      assertNotNull(fileMetadata);
      assertEquals(2, fileMetadata.size());
      assertTrue(StringUtils.equals("sample value", (String) fileMetadata.get("sample key")));
      assertTrue(StringUtils.equals("\"an even more 'complex' value\"! {and them some}",
          (String) fileMetadata.get("complex key?")));
    }
  }

  /**
   * Mock user provider for the mock repository
   */
  private class MockUserProvider implements MockUnifiedRepository.ICurrentUserProvider {
    @Override
    public String getUser() {
      return MockUnifiedRepository.root().getName();
    }

    @Override
    public List<String> getRoles() {
      return new ArrayList<String>();
    }
  }
}
