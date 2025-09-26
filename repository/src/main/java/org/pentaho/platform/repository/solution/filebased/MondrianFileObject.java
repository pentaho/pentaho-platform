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

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DecoratedFileObject;
import org.pentaho.platform.api.repository2.unified.MondrianSchemaAnnotator;

import java.io.InputStream;

/**
 * FileObject that applies Mondrian schema annotations to the InputStream
 */
class MondrianFileObject extends DecoratedFileObject {

  private final FileObject annotationsFile;
  private final MondrianSchemaAnnotator annotator;

  public MondrianFileObject(
      final FileObject schemaFile, final FileObject annotationsFile, final MondrianSchemaAnnotator annotator ) {
    super( schemaFile );
    this.annotationsFile = annotationsFile;
    this.annotator = annotator;
  }

  @Override
  public FileContent getContent() throws FileSystemException {
    return new DecoratedFileContent( super.getContent() ) {
      @Override public InputStream getInputStream() throws FileSystemException {
        return annotator.getInputStream( super.getInputStream(), annotationsFile.getContent().getInputStream() );
      }
    };
  }
}
