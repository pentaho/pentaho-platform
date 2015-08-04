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
