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
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.MondrianSchemaAnnotator;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MondrianVfsTest {

  public static final String schemaContent = "<schema>contents</schema>";
  public static final String annotationsContent = "<annotations>contents</annotations>";
  public static final String appliedContent = "<schema>annotationsapplied</schema>";

  @Test
  public void testGetsFileFromEtcMondrian() throws Exception {
    assertEquals( schemaContent, runCatalogTest( "/sample", null, false ) );
    assertEquals( schemaContent, runCatalogTest( "/foodmart", null, false ) );
  }

  @Test
  public void testAnnotationsAreApplied() throws Exception {
    MondrianSchemaAnnotator mondrianSchemaAnnotator = new MondrianSchemaAnnotator() {
      @Override
      public InputStream getInputStream( final InputStream inputStream, final InputStream annotationsInputStream ) {
        return IOUtils.toInputStream( appliedContent );
      }
    };
    assertEquals( appliedContent, runCatalogTest( "/annotated", mondrianSchemaAnnotator, true ) );
  }

  private String runCatalogTest( final String expectedCatalog, final MondrianSchemaAnnotator annotator,
                                 final boolean annotationsExist )
    throws IOException {
    MondrianVfs vfs = new MondrianVfs() {
      @Override FileObject getCatalogFileObject( final String catalog, final String fileName ) {
        assertEquals( expectedCatalog, catalog );
        try {
          if ( fileName.equals( "schema.xml" ) ) {
            return FileObjectTestHelper.mockFile( schemaContent, true );
          }
          if ( fileName.equals( "annotations.xml" ) ) {
            return FileObjectTestHelper.mockFile( annotationsContent, annotationsExist );
          }
          fail( "unrecognized File" );
          return null;
        } catch ( FileSystemException e ) {
          fail( e.getMessage() );
          return null;
        }
      }

      @Override MondrianSchemaAnnotator getAnnotator() {
        return annotator;
      }
    };
    FileObject file = vfs.findFile( null, "mondrian:" + expectedCatalog, null );
    return IOUtils.toString( file.getContent().getInputStream() );
  }
}
