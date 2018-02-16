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

package org.pentaho.platform.repository2.unified.fileio;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;


import java.io.ByteArrayInputStream;

public class RepositoryFileOutputStreamTest {

  @Test
  public void convertTest() throws Exception{
    RepositoryFileOutputStream spy = Mockito.spy( new RepositoryFileOutputStream( "1.ktr", "UTF-8"  ) );
    Converter converter = Mockito.mock( Converter.class);
    ByteArrayInputStream bis = Mockito.mock( ByteArrayInputStream.class);
    Mockito.doReturn( Mockito.mock( NodeRepositoryFileData.class ) ).when( converter ).convert( bis , "UTF-8", "");
    IRepositoryFileData data = spy.convert( null , bis , "");
    Assert.assertTrue( data instanceof SimpleRepositoryFileData );
    data = spy.convert( converter , bis , "");
    Assert.assertTrue( data instanceof NodeRepositoryFileData );
  }
}
