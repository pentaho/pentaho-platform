/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.platform.repository2.unified.jcr;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Tatsiana_Kasiankova
 * 
 */
public class JcrRepositoryFileUtilsTest {
  /**
   * 
   */
  private static final String VERSION_NAME_TEST = "Version Name Test";
  /**
   * 
   */
  private static final String UNSUPPORTED_REPOSITORY_OPERATION_EXCEPTION_TEST_MESSAGE =
      "Unsupported Repository Operation Exception Test Message";
  /**
   * 
   */
  private static final String REPOSITORY_EXCEPTION_TEST_MESSAGE = "Repository Exception Test Message";
  private PentahoJcrConstants pJcrConstMock = mock( PentahoJcrConstants.class );
  private Node nodeMock = mock( Node.class );
  private VersionManager vmanagerMock = mock( VersionManager.class );
  private Workspace workspaceMock = mock( Workspace.class );
  private Session sessionMock = mock( Session.class );

  @Before
  public void setUp() throws UnsupportedRepositoryOperationException, RepositoryException {
    when( workspaceMock.getVersionManager() ).thenReturn( vmanagerMock );
    when( sessionMock.getWorkspace() ).thenReturn( workspaceMock );
  }

  /**
   * This test covers the change for avoiding NPE from Jackrabbit: see http://jira.pentaho.com/browse/BACKLOG-175
   * 
   * @throws UnsupportedRepositoryOperationException
   * @throws RepositoryException
   */
  @Test
  public void testNoNPEThrows_WhenNPEGettingFromContentRepository() throws Exception {
    when( vmanagerMock.getBaseVersion( anyString() ) ).thenThrow( new NullPointerException() );
    try {
      String versionId = JcrRepositoryFileUtils.getVersionId( sessionMock, pJcrConstMock, nodeMock );
      assertNull( versionId );
    } catch ( NullPointerException e ) {
      fail( "The exception shold not be thrown: " + e );
    }

  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testRepositoryExceptionThrows() throws Exception {
    when( vmanagerMock.getBaseVersion( anyString() ) ).thenThrow(
        new RepositoryException( REPOSITORY_EXCEPTION_TEST_MESSAGE ) );
    exception.expect( RepositoryException.class );
    exception.expectMessage( REPOSITORY_EXCEPTION_TEST_MESSAGE );
    JcrRepositoryFileUtils.getVersionId( sessionMock, pJcrConstMock, nodeMock );
  }

  @Test
  public void testUnsupportedRepositoryOperationExceptionThrows() throws Exception {
    when( workspaceMock.getVersionManager() ).thenThrow(
        new UnsupportedRepositoryOperationException( UNSUPPORTED_REPOSITORY_OPERATION_EXCEPTION_TEST_MESSAGE ) );
    exception.expect( UnsupportedRepositoryOperationException.class );
    exception.expectMessage( UNSUPPORTED_REPOSITORY_OPERATION_EXCEPTION_TEST_MESSAGE );
    JcrRepositoryFileUtils.getVersionId( sessionMock, pJcrConstMock, nodeMock );
  }

  @Test
  public void testVersionIdIsNull_WhenJCRReturnsNull() throws Exception {
    when( vmanagerMock.getBaseVersion( anyString() ) ).thenReturn( null );
    try {
      String versionId = JcrRepositoryFileUtils.getVersionId( sessionMock, pJcrConstMock, nodeMock );
      assertNull( versionId );
    } catch ( Exception e ) {
      fail( "No exception shold be here but it is: " + e );
    }
  }

  @Test
  public void testVersionId_Success() throws Exception {
    Version version = mock( Version.class );
    when( version.getName() ).thenReturn( VERSION_NAME_TEST );
    when( vmanagerMock.getBaseVersion( anyString() ) ).thenReturn( version );
    try {
      String versionId = JcrRepositoryFileUtils.getVersionId( sessionMock, pJcrConstMock, nodeMock );
      assertTrue( VERSION_NAME_TEST.equals( versionId ) );
    } catch ( Exception e ) {
      fail( "No exception shold be here but it is: " + e );
    }
  }

}
