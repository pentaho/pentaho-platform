/*
 * Copyright 2002 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
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
