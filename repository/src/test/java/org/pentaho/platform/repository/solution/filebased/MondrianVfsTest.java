/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
