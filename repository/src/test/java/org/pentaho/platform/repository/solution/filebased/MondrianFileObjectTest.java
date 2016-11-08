/*
 * Copyright 2015 Pentaho Corporation.  All rights reserved.
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
