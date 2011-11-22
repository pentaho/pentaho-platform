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

import junit.framework.TestCase;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;

import java.io.ByteArrayInputStream;

/**
 * Class Description
 * User: dkincade
 */
public class RepositoryUtilsTest extends TestCase {

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

    assertNull(repositoryUtils.getFolder("/one/two/three", false, false, null));
    assertNull(repositoryUtils.getFolder("/one/two/three", true, false, null));
    assertNull(repositoryUtils.getFolder("/one/two/three", false, true, null));
    assertNull(repositoryUtils.getFolder("/one/two/three", true, true, null)); // shouldn't create the root

    repository.createFolder("/");
    RepositoryFile test = repositoryUtils.getFolder("/one/two/three", true, true, null);
    assertNotNull(test);
    assertEquals("The folder name is invalid", "three", test.getName());
    assertEquals("The path is invalid", "/one/two/three", test.getPath());
    assertTrue("The folder should be defined as a folder", test.isFolder());

    // Make sure it created the parents
    RepositoryFile one = repositoryUtils.getFolder("/one", false, false, null);
    assertNotNull(one);
    RepositoryFile two = repositoryUtils.getFolder("/one/two", false, false, null);
    assertNotNull(two);
  }

  @Test
  public void testGetFile() throws Exception {
    final MockUnifiedRepository repository = new MockUnifiedRepository();
    final RepositoryUtils repositoryUtils = new RepositoryUtils(repository);

    assertNull(repositoryUtils.getFile("/one/two/three.prpt", null, false, false, null));
    assertNull(repositoryUtils.getFile("/one/two/three.prpt", null, true, false, null));
    assertNull(repositoryUtils.getFile("/one/two/three.prpt", null, false, true, null));
    assertNull(repositoryUtils.getFile("/one/two/three.prpt", null, true, true, null)); // shouldn't create the root

    repository.createFolder("/");
    final SimpleRepositoryFileData data = new SimpleRepositoryFileData(
        new ByteArrayInputStream("Test".getBytes()), "UTF-8", "text/plain");
    RepositoryFile test = repositoryUtils.getFile("/one/two/three.prpt", data, true, true, null);
    assertNotNull(test);
    assertEquals("The filename is invalid", "three.prpt", test.getName());
    assertEquals("The path is invalid", "/one/two/three.prpt", test.getPath());
    assertFalse("The file should not be defined as a folder", test.isFolder());

    // Make sure it created the parents
    RepositoryFile one = repositoryUtils.getFolder("/one", false, false, null);
    assertNotNull(one);
    RepositoryFile two = repositoryUtils.getFolder("/one/two", false, false, null);
    assertNotNull(two);
  }

  @Test
  public void testGetParentPath() throws Exception {
    assertEquals("/one/two/three", RepositoryUtils.getParentPath("/one/two/three/four"));
    assertEquals("one/two/three", RepositoryUtils.getParentPath("one/two/three/four"));
    assertEquals("/one/two/three", RepositoryUtils.getParentPath("/one/two/three/"));
    assertEquals("/one", RepositoryUtils.getParentPath("/one/two"));
    assertEquals("/", RepositoryUtils.getParentPath("/one"));
    assertEquals("", RepositoryUtils.getParentPath("one"));
    assertEquals(null, RepositoryUtils.getParentPath("/"));
  }
}
