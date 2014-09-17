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

package org.pentaho.platform.web.http.api.resources;

import org.junit.Test;
import org.pentaho.jmeter.util.EndpointUtil;

import javax.ws.rs.Path;
import java.io.File;

import static org.junit.Assert.*;

public class CommentEndpointTest {

  /**
   * This is an ANT runnable test only. This will fail running from within IDE
   */
  @Test
  public void testCommentEndpoints() throws Exception {
    File rootProjectFolder = new File( "." );

    File srcFolder = null;
    File[] children = rootProjectFolder.listFiles();
    if ( children != null ) {
      for ( File child : rootProjectFolder.listFiles() ) {
        if ( child != null && child.isDirectory() && ( child.getName().equals( "src" ) || child.getName()
          .equals( "source" ) ) ) {
          srcFolder = child;
        }
      }
    }
    String scanDirPath = srcFolder.getAbsolutePath().replace( "./", "" );

    assertTrue( EndpointUtil.compareCommentsAndAnnotations( scanDirPath, "pentaho/api", Path.class ) );
  }
}
