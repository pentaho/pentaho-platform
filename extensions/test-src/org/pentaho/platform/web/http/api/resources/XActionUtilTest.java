/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileContentItem;
import org.pentaho.platform.api.repository.IContentItem;

/**
 * check that executing delete file from repository
 *
 */
public class XActionUtilTest {

  @Mock  private RepositoryFile xactionFile;

  @Mock  private RepositoryFile generatedFile;

  @Mock  private HttpServletRequest httpServletRequest;

  @Mock  private HttpServletResponse httpServletResponse;

  @Mock  private IPentahoSession userSession;

  @Mock  private IMimeTypeListener mimeTypeListener;

  @Mock  private IUnifiedRepository repository;

  @Mock private ISolutionEngine engine;

  @Mock private IMessageFormatter formatter;

  private IPentahoObjectFactory pentahoObjectFactoryUnified;

  @Before
  @SuppressWarnings( "unchecked" )
  public void setUp() throws ObjectFactoryException {
    MockitoAnnotations.initMocks( this );
    Map<String, String[]> map = Mockito.mock( Map.class );
    Mockito.when( httpServletRequest.getParameterMap() ).thenReturn( map );
    Mockito.when( httpServletRequest.getParameter( Mockito.anyString() ) ).thenReturn( null );

    Mockito.when( repository.getFile( Mockito.anyString() ) ).thenReturn( generatedFile );

    List<IContentItem> items = Arrays.asList( (IContentItem) Mockito.mock( RepositoryFileContentItem.class ) );
    IRuntimeContext context = Mockito.mock( IRuntimeContext.class );
    Mockito.when( context.getOutputContentItems() ).thenReturn( items );

    Mockito.when( engine.execute( Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap(),
        Mockito.any( IOutputHandler.class ), Mockito.any( IActionCompleteListener.class ), Mockito.any( IPentahoUrlFactory.class ),
        Mockito.anyList() ) ).thenReturn( context );
    pentahoObjectFactoryUnified = Mockito.mock( IPentahoObjectFactory.class );
    Mockito.when( pentahoObjectFactoryUnified.objectDefined( Mockito.anyString() ) ).thenReturn( true );
    Mockito.when( pentahoObjectFactoryUnified.get( this.anyClass(), Mockito.anyString(), Mockito.any( IPentahoSession.class ) ) ).thenAnswer( new Answer<Object>() {
        @Override
        public Object answer( InvocationOnMock invocation ) throws Throwable {
          if ( IUnifiedRepository.class.toString().equals( invocation.getArguments()[0].toString() ) ) {
            return repository;
          } else if (  ISolutionEngine.class.toString().equals( invocation.getArguments()[0].toString() ) ) {
            return engine;
          } else if ( IMessageFormatter.class.toString().equals( invocation.getArguments()[0].toString() ) ) {
            return formatter;
          } else {
            return null;
          }
        }
      } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactoryUnified );

    ISystemSettings systemSettingsService = Mockito.mock( ISystemSettings.class );
    Mockito.when( systemSettingsService.getSystemSetting( Mockito.anyString(), Mockito.anyString() ) ).thenAnswer( new Answer<String>() {
      @Override
      public String answer( InvocationOnMock invocation ) throws Throwable {
        return invocation.getArguments()[0].toString();
      }
    } );
    PentahoSystem.setSystemSettingsService( systemSettingsService );

    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL );
    PentahoSessionHolder.setSession( userSession );
  }

  private File createTempFile() throws IOException {
    return File.createTempFile( "XActionUtilTest", "tmp" );
  }

  @Test
  public void executeXActionSequence() throws Exception {
    XactionUtil.execute( MediaType.TEXT_HTML, xactionFile, httpServletRequest, httpServletResponse, userSession, mimeTypeListener );
    Mockito.verify( repository, Mockito.times( 1 ) ).deleteFile( Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString() );
  }

  @Test
  public void testDeleteContentItem_null() throws Exception {
    XactionUtil.deleteContentItem( null, null ); // No exception
    XactionUtil.deleteContentItem( null, Mockito.mock( IUnifiedRepository.class ) ); // No exception
    Mockito.verify( repository, Mockito.times( 0 ) ).deleteFile( Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString() );
  }

  @Test
  public void testDeleteContentItem_simple() throws Exception {
    final IContentItem contentItem = Mockito.mock( SimpleContentItem.class );
    Mockito.doReturn( Mockito.mock( OutputStream.class ) ).when( contentItem ).getOutputStream( Mockito.anyString() );
    try {
      XactionUtil.deleteContentItem( contentItem, null );
    } finally {
      contentItem.getOutputStream( null ).close();
    }
    Mockito.verify( repository, Mockito.times( 0 ) ).deleteFile( Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString() );
  }

  @Test
  public void testDeleteContentItem_repo() throws Exception {
    IContentItem item = Mockito.mock( RepositoryFileContentItem.class );
    Mockito.doReturn( Mockito.mock( OutputStream.class ) ).when( item ).getOutputStream( Mockito.anyString() );
    XactionUtil.deleteContentItem( item, repository );
    Mockito.verify( repository, Mockito.times( 1 ) ).deleteFile( Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString() );
  }

  @Test
  public void testDeleteContentItem_repoNoFile() throws Exception {
    Mockito.doReturn( null ).when( repository ).getFile( Mockito.anyString() );
    IContentItem item = Mockito.mock( RepositoryFileContentItem.class );
    Mockito.doReturn( Mockito.mock( OutputStream.class ) ).when( item ).getOutputStream( Mockito.anyString() );
    XactionUtil.deleteContentItem( item, repository );
    Mockito.verify( repository, Mockito.times( 0 ) ).deleteFile( Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString() );
  }

  @After
  public void tearDown() {
    PentahoSessionHolder.removeSession();
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactoryUnified );
    PentahoSystem.shutdown();
  }

  private Class<?> anyClass() {
    return Mockito.argThat( new AnyClassMatcher() );
  }

  private class AnyClassMatcher extends ArgumentMatcher<Class<?>> {
    @Override
    public boolean matches( final Object arg ) {
      return true;
    }
  }

}
