/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.webservices;

import junit.framework.TestCase;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Test class for DefaultUnifiedRepositoryWebService
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class DefaultUnifiedRepositoryWebServiceTest extends TestCase {
  private IUnifiedRepository repository;
  private IUnifiedRepositoryWebService repositoryWS;

  public void setUp() throws Exception {
    repository = new MockUnifiedRepository( new MockUserProvider() );
    repositoryWS = new DefaultUnifiedRepositoryWebService( repository );
  }

  public void testFileMetadata() throws Exception {
    final RepositoryFile testfile =
        repository.createFile( repository.getFile( "/etc" ).getId(), new RepositoryFile.Builder( "testfile" ).build(),
            new SimpleRepositoryFileData( new ByteArrayInputStream( "test".getBytes() ),
              "UTF-8", "text/plain" ), null );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure the repository is setup correctly
      assertNotNull( testfile );
      assertNotNull( testfile.getId() );
      final Map<String, Serializable> fileMetadata = repository.getFileMetadata( testfile.getId() );
      assertNotNull( fileMetadata );
      assertEquals( 0, fileMetadata.size() );
    }

    final List<StringKeyStringValueDto> metadata = new ArrayList<StringKeyStringValueDto>();
    metadata.add( new StringKeyStringValueDto( "sample key", "sample value" ) );
    metadata.add( new StringKeyStringValueDto( "complex key?", "\"an even more 'complex' value\"! {and them some}" ) );

    repositoryWS.setFileMetadata( testfile.getId().toString(), metadata );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure the repository sees the metadata
      assertNotNull( testfile );
      assertNotNull( testfile.getId() );
      final Map<String, Serializable> fileMetadata = repository.getFileMetadata( testfile.getId() );
      assertNotNull( fileMetadata );
      assertEquals( 2, fileMetadata.size() );
    }
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure we can get the same metadata back via the web service
      final List<StringKeyStringValueDto> fileMetadata = repositoryWS.getFileMetadata( testfile.getId().toString() );
      assertNotNull( fileMetadata );
      assertEquals( 2, fileMetadata.size() );
      assertTrue( metadata.get( 0 ).equals( fileMetadata.get( 0 ) ) || metadata.get( 0 )
        .equals( fileMetadata.get( 1 ) ) );
      assertTrue( metadata.get( 1 ).equals( fileMetadata.get( 0 ) ) || metadata.get( 1 )
        .equals( fileMetadata.get( 1 ) ) );
    }
  }

  /**
   * Mock ICurrentUserProvider for testing
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
