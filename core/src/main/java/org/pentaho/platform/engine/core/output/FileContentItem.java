/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.engine.core.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileContentItem extends SimpleContentItem {

  private File file;

  public FileContentItem( File file ) throws FileNotFoundException {
    super( new FileOutputStream( file ) );
    this.file = file;
  }

  public File getFile() {
    return file;
  }

}
