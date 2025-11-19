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
