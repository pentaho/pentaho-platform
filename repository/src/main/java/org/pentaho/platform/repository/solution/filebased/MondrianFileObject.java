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
