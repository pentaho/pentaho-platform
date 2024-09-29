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
