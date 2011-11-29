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
package org.pentaho.platform.repository2.unified;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

/**
 * Class Description
 * User: dkincade
 */
@SuppressWarnings("nls")
public class RepositoryUtilsTest {

  @BeforeClass
  public static void setUpClass() {
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("joe", null, new GrantedAuthority[0]));
  }
  
  @AfterClass
  public static void tearDownClass() {
    SecurityContextHolder.clearContext();
  }
  
  @Test
  public void testCreationError() throws Exception {
    try {
      new RepositoryUtils(null);
      fail("Should not allow creating with no repository");
    } catch (Exception success) {
    }
  }

  @Test
  public void testGetFolder() throws Exception {
    final MockUnifiedRepository repository = new MockUnifiedRepository();
    final RepositoryUtils repositoryUtils = new RepositoryUtils(repository);

    RepositoryFile test = repositoryUtils.getFolder("/public/one/two/three", true, true, null);
    assertNotNull(test);
    assertEquals("The folder name is invalid", "three", test.getName());
    assertEquals("The path is invalid", "/public/one/two/three", test.getPath());
    assertTrue("The folder should be defined as a folder", test.isFolder());

    // Make sure it created the parents
    RepositoryFile one = repositoryUtils.getFolder("/public/one", false, false, null);
    assertNotNull(one);
    RepositoryFile two = repositoryUtils.getFolder("/public/one/two", false, false, null);
    assertNotNull(two);
  }

  @Test
  public void testGetFile() throws Exception {
    final MockUnifiedRepository repository = new MockUnifiedRepository();
    final RepositoryUtils repositoryUtils = new RepositoryUtils(repository);

    final SimpleRepositoryFileData data = new SimpleRepositoryFileData(
        new ByteArrayInputStream("Test".getBytes()), "UTF-8", "text/plain");
    RepositoryFile test = repositoryUtils.getFile("/public/one/two/three.prpt", data, true, true, null);
    assertNotNull(test);
    assertEquals("The filename is invalid", "three.prpt", test.getName());
    assertEquals("The path is invalid", "/public/one/two/three.prpt", test.getPath());
    assertFalse("The file should not be defined as a folder", test.isFolder());

    // Make sure it created the parents
    RepositoryFile one = repositoryUtils.getFolder("/public/one", false, false, null);
    assertNotNull(one);
    RepositoryFile two = repositoryUtils.getFolder("/public/one/two", false, false, null);
    assertNotNull(two);
  }

  @Test
  public void testGetParentPath() throws Exception {
    assertEquals("/public/one/two/three", RepositoryUtils.getParentPath("/public/one/two/three/four"));
    assertEquals("/publicone/two/three", RepositoryUtils.getParentPath("/publicone/two/three/four"));
    assertEquals("/public/one/two/three", RepositoryUtils.getParentPath("/public/one/two/three/"));
    assertEquals("/public/one", RepositoryUtils.getParentPath("/public/one/two"));
    assertEquals("/public", RepositoryUtils.getParentPath("/public/one"));
    assertEquals("", RepositoryUtils.getParentPath("one"));
    assertEquals(null, RepositoryUtils.getParentPath("/"));
  }
}
