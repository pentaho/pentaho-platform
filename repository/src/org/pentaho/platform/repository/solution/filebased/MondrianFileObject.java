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
