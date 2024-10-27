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


package org.pentaho.platform.web.http.api.resources;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
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

  @Mock private IAuthorizationPolicy authPolicy;

  private IPentahoObjectFactory pentahoObjectFactoryUnified;

  @Before
  @SuppressWarnings( "unchecked" )
  public void setUp() throws ObjectFactoryException {
    MockitoAnnotations.initMocks( this );
    Map<String, String[]> map = mock( Map.class );
    when( httpServletRequest.getParameterMap() ).thenReturn( map );
    when( httpServletRequest.getParameter( nullable( String.class ) ) ).thenReturn( null );

    when( repository.getFile( nullable( String.class ) ) ).thenReturn( generatedFile );

    List<IContentItem> items = Arrays.asList( mock( RepositoryFileContentItem.class ) );
    IRuntimeContext context = mock( IRuntimeContext.class );
    when( context.getOutputContentItems() ).thenReturn( items );

    when( engine.execute( nullable( String.class ), nullable( String.class ), anyBoolean(), anyBoolean(), nullable( String.class ), anyBoolean(), anyMap(),
        nullable( IOutputHandler.class ), nullable( IActionCompleteListener.class ), nullable( IPentahoUrlFactory.class ),
        anyList() ) ).thenReturn( context );
    pentahoObjectFactoryUnified = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactoryUnified.objectDefined( nullable( String.class ) ) ).thenReturn( true );
    when( pentahoObjectFactoryUnified.get( this.anyClass(), nullable( String.class ), nullable( IPentahoSession.class ) ) ).thenAnswer( new Answer<Object>() {
        @Override
        public Object answer( InvocationOnMock invocation ) throws Throwable {
          if ( IUnifiedRepository.class.toString().equals( invocation.getArguments()[0].toString() ) ) {
            return repository;
          } else if (  ISolutionEngine.class.toString().equals( invocation.getArguments()[0].toString() ) ) {
            return engine;
          } else if ( IMessageFormatter.class.toString().equals( invocation.getArguments()[0].toString() ) ) {
            return formatter;
          } else if ( IAuthorizationPolicy.class.toString().equals( invocation.getArguments()[0].toString() ) ) {
            return authPolicy;
          } else {
            return null;
          }
        }
      } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactoryUnified );

    ISystemSettings systemSettingsService = mock( ISystemSettings.class );
    when( systemSettingsService.getSystemSetting( nullable( String.class ), nullable( String.class ) ) ).thenAnswer( new Answer<String>() {
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
    verify( repository ).deleteFile( nullable( String.class ), anyBoolean(), nullable( String.class ) );
  }

  @Test
  public void testDeleteContentItem_null() throws Exception {
    XactionUtil.deleteContentItem( null, null ); // No exception
    XactionUtil.deleteContentItem( null, mock( IUnifiedRepository.class ) ); // No exception
    verify( repository, times( 0 ) ).deleteFile( nullable( String.class ), anyBoolean(), nullable( String.class ) );
  }

  @Test
  public void testDeleteContentItem_simple() throws Exception {
    final IContentItem contentItem = mock( SimpleContentItem.class );
    doReturn( mock( OutputStream.class ) ).when( contentItem ).getOutputStream( any() );
    try {
      XactionUtil.deleteContentItem( contentItem, null );
    } finally {
      contentItem.getOutputStream( null ).close();
    }
    verify( repository, times( 0 ) ).deleteFile( nullable( String.class ), anyBoolean(), nullable( String.class ) );
  }

  @Test
  public void testDeleteContentItem_repo() throws Exception {
    IContentItem item = mock( RepositoryFileContentItem.class );
    doReturn( mock( OutputStream.class ) ).when( item ).getOutputStream( any() );
    XactionUtil.deleteContentItem( item, repository );
    verify( repository, times( 1 ) ).deleteFile( nullable( String.class ), anyBoolean(), nullable( String.class ) );
  }

  @Test
  public void testDeleteContentItem_repoNoFile() throws Exception {
    doReturn( null ).when( repository ).getFile( nullable( String.class ) );
    IContentItem item = mock( RepositoryFileContentItem.class );
    doReturn( mock( OutputStream.class ) ).when( item ).getOutputStream( any() );
    XactionUtil.deleteContentItem( item, repository );
    verify( repository, times( 0 ) ).deleteFile( nullable( String.class ), anyBoolean(), nullable( String.class ) );
  }

  @After
  public void tearDown() {
    PentahoSessionHolder.removeSession();
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactoryUnified );
    PentahoSystem.shutdown();
  }

  private Class<?> anyClass() {
    return argThat( new AnyClassMatcher() );
  }

  private class AnyClassMatcher implements ArgumentMatcher<Class<?>> {
    @Override
    public boolean matches( final Class<?> arg ) {
      return true;
    }
  }

}
