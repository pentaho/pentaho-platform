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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.fileio;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IStreamListener;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;
import static org.powermock.reflect.Whitebox.getInternalState;

public class RepositoryFileOutputStreamTest {

  @Test
  public void convertTest() throws Exception {
    RepositoryFileOutputStream spy = spy( new RepositoryFileOutputStream( "1.ktr", "UTF-8" ) );
    Converter converter = Mockito.mock( Converter.class );
    ByteArrayInputStream bis = Mockito.mock( ByteArrayInputStream.class );
    Mockito.doReturn( Mockito.mock( NodeRepositoryFileData.class ) ).when( converter ).convert( bis, "UTF-8", "" );
    IRepositoryFileData data = spy.convert( null, bis, "" );
    assertTrue( data instanceof SimpleRepositoryFileData );
    data = spy.convert( converter, bis, "" );
    assertTrue( data instanceof NodeRepositoryFileData );
  }

  @Test
  public void testCloseWithEmptyDataWithoutForceFlush() throws IOException {
    RepositoryFileOutputStream repositoryFileOutputStream = spy( new RepositoryFileOutputStream( "1.ktr" ) );
    IUnifiedRepository repository = mock( IUnifiedRepository.class );
    setInternalState( repositoryFileOutputStream, "repository", repository );
    repositoryFileOutputStream.forceFlush( false );
    repositoryFileOutputStream.close();
    assertTrue( repositoryFileOutputStream.flushed );
    assertFalse( repositoryFileOutputStream.forceFlush );
    verify( repositoryFileOutputStream, times( 1 ) ).flush();
    verify( repository, times( 0 ) ).createFile( any(), any(), any(), any() );
  }

  @Test
  public void testCloseWithEmptyData() throws IOException {
    IUnifiedRepository repository = mock( IUnifiedRepository.class );
    RepositoryFile repositoryFile = mock( RepositoryFile.class );

    RepositoryFileOutputStream repositoryFileOutputStream =
      spy( new RepositoryFileOutputStream( "1.ktr", true, true ) );

    setInternalState( repositoryFileOutputStream, "repository", repository );
    when( repository.getFile( any() ) ).thenReturn( repositoryFile );
    when(
      repository.createFile( any( Serializable.class ), any( RepositoryFile.class ), any( IRepositoryFileData.class ),
        any( String.class ) ) ).thenReturn( repositoryFile );
    repositoryFileOutputStream.close();
    assertTrue( repositoryFileOutputStream.flushed );
    assertTrue( repositoryFileOutputStream.forceFlush );
    verify( repositoryFileOutputStream, times( 1 ) ).flush();
    verify( repository, times( 1 ) )
      .createFile( any( Serializable.class ), any( RepositoryFile.class ), any( IRepositoryFileData.class ),
        any( String.class ) );
  }

  @Test
  public void testForceFlush() {
    RepositoryFileOutputStream repositoryFileOutputStream = new RepositoryFileOutputStream( "" );
    assertTrue( getInternalState( repositoryFileOutputStream, "forceFlush" ) );
    repositoryFileOutputStream.forceFlush( false );
    assertFalse( getInternalState( repositoryFileOutputStream, "forceFlush" ) );
    repositoryFileOutputStream.forceFlush( true );
    assertTrue( getInternalState( repositoryFileOutputStream, "forceFlush" ) );
  }

  @Test
  public void testStreamCompleteListenerCallback() throws IOException {
    IUnifiedRepository repository = mock( IUnifiedRepository.class );
    RepositoryFile repositoryFile = mock( RepositoryFile.class );
    IStreamListener streamListener = mock( IStreamListener.class );

    RepositoryFileOutputStream repositoryFileOutputStream =
      spy( new RepositoryFileOutputStream( "1.ktr", true, true ) );

    repositoryFileOutputStream.forceFlush( false );

    repositoryFileOutputStream.addListener( streamListener );

    setInternalState( repositoryFileOutputStream, "repository", repository );
    when( repository.getFile( any() ) ).thenReturn( repositoryFile );
    when(
      repository.createFile( any( Serializable.class ), any( RepositoryFile.class ), any( IRepositoryFileData.class ),
        any( String.class ) ) ).thenReturn( repositoryFile );
    repositoryFileOutputStream.close();

    verify( streamListener, times( 1 ) ).streamComplete();
    verify( streamListener, times( 0 ) ).fileCreated( anyObject() );
  }
}
