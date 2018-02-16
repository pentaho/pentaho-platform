/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository.solution.filebased;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.MondrianSchemaAnnotator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MondrianFileObjectTest {
  @Test
  public void testAppliesAllAnnotators() throws Exception {
    FileObject schemaFile = FileObjectTestHelper.mockFile( "schemaFile", true );
    FileObject annotationsFile = FileObjectTestHelper.mockFile( "annotationsFile", true );
    MondrianSchemaAnnotator mondrianSchemaAnnotator = new MondrianSchemaAnnotator() {
      @Override
      public InputStream getInputStream(
          final InputStream schemaInputStream, final InputStream annotationsInputStream ) {
        try {
          return new ByteArrayInputStream(
            ( IOUtils.toString( annotationsInputStream ) + " - " + IOUtils.toString( schemaInputStream ) ).getBytes() );
        } catch ( IOException e ) {
          fail( e.getMessage() );
          return null;
        }
      }
    };
    MondrianFileObject mondrianFileObject =
        new MondrianFileObject( schemaFile, annotationsFile, mondrianSchemaAnnotator );
    String actual = IOUtils.toString( mondrianFileObject.getContent().getInputStream() );
    assertEquals( "annotationsFile - schemaFile", actual );
  }
}
