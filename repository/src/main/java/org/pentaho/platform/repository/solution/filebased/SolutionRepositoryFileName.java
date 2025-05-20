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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;

public class SolutionRepositoryFileName extends AbstractFileName {

  public SolutionRepositoryFileName( final String absPath, final FileType type ) {
    super( "solution", absPath, type );
  }

  @Override protected void appendRootUri( StringBuilder stringBuilder, boolean b ) {

  }
  
  @Override
  public FileName createName( final String absPath, final FileType fileType ) {

    FileName name = new SolutionRepositoryFileName( absPath, fileType );
    return name;
  }

}
