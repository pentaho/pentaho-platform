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
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileObjectTestHelper {
  public static FileObject mockFile( final String contents, final boolean exists ) throws FileSystemException {
    FileObject fileObject = mock( FileObject.class );
    when( fileObject.exists() ).thenReturn( exists );
    FileContent fileContent = mock( FileContent.class );
    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getInputStream() ).thenReturn( IOUtils.toInputStream( contents ) );
    final FileObject parent = mock( FileObject.class );
    when( fileObject.getParent() ).thenReturn( parent );
    final FileName fileName = mock( FileName.class );
    when( parent.getName() ).thenReturn( fileName );
    when( fileName.getURI() ).thenReturn( "mondrian:/catalog" );
    return fileObject;
  }
}
