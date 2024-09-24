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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services.runtime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.util.Assert;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.AggregateObjectFactory;
import org.pentaho.platform.engine.security.SecurityParameterProvider;
import org.pentaho.platform.util.JVMParameterProvider;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class RuntimeContextTest {
  IRuntimeRepository mockedRuntimeRepository;
  ISolutionEngine mockedSolutionEngine;
  StandaloneApplicationContext applicationContext;
  AggregateObjectFactory aggregateObjectFactory;

  private final IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" );
  private final IPentahoSession session = new StandaloneSession( "system" );

  private IRuntimeContext runtimeCtx;

  @Before
  public void before() {
    mockedRuntimeRepository = mock( IRuntimeRepository.class );
    mockedSolutionEngine = mock( ISolutionEngine.class );
    applicationContext = new StandaloneApplicationContext( ".", "" );
    aggregateObjectFactory = new AggregateObjectFactory();
  }

  /**
   * Sanity test the basics - createNewInstance() works passing all combinations of arguments.
   */
  @Test
  public void testCreateNewInstanceBasicSanity() {

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( eq( IRuntimeRepository.class ), any() ) ).thenReturn( mockedRuntimeRepository );
      pentahoSystem.when( () -> PentahoSystem.get( eq( ISolutionEngine.class ), any() ) ).thenReturn( mockedSolutionEngine );
      pentahoSystem.when( PentahoSystem::getApplicationContext ).thenReturn( applicationContext );
      pentahoSystem.when( PentahoSystem::getObjectFactory ).thenReturn( aggregateObjectFactory );

      runtimeCtx = new RuntimeContext( "id", mockedSolutionEngine, "solutionName",
        makeRuntimeData( session ), session, null, "processId", urlFactory,
        makeParameterProviders( session ), new ArrayList<String>(), null );

      when( mockedRuntimeRepository.newRuntimeElement( anyString(), anyString(), anyBoolean() ) ).thenAnswer(
        (Answer<IRuntimeElement>) invocation -> {
          Object[] args = invocation.getArguments();
          final String parentId = (String) args[0];
          final String parentType = (String) args[1];
          return new SimpleRuntimeElement( UUIDUtil.getUUIDAsString(), parentId, parentType, "sol1" );
        }
      );

      final boolean persisted = true;
      final boolean notPersisted = false;
      final boolean forceImmediateWrite = true;
      final boolean dontForceImmediateWrite = false;
      final Map<String, String> noParams = new HashMap<>();

      Assert.assertNotNull( runtimeCtx.createNewInstance( persisted ) );
      Assert.assertNotNull( runtimeCtx.createNewInstance( notPersisted ) );
      Assert.assertNotNull( runtimeCtx.createNewInstance( persisted, noParams ) );
      Assert.assertNotNull( runtimeCtx.createNewInstance( notPersisted, noParams ) );
      Assert.assertNotNull( runtimeCtx.createNewInstance( persisted, noParams, forceImmediateWrite ) );
      Assert.assertNotNull( runtimeCtx.createNewInstance( notPersisted, noParams, dontForceImmediateWrite ) );

      verify( mockedRuntimeRepository, times( 6 ) ).newRuntimeElement( anyString(), anyString(), anyBoolean() );
      verify( mockedRuntimeRepository, times( 3 ) ).newRuntimeElement( anyString(), anyString(), eq( true ) );
      verify( mockedRuntimeRepository, times( 3 ) ).newRuntimeElement( anyString(), anyString(), eq( false ) );
    }
  }

  /**
   * Tests that params are passed and saved correctly to the underlying runtime element.
   */
  @Test
  public void testCreateNewInstanceWithParams() {

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( eq( IRuntimeRepository.class ), any() ) ).thenReturn( mockedRuntimeRepository );
      pentahoSystem.when( () -> PentahoSystem.get( eq( ISolutionEngine.class ), any() ) ).thenReturn( mockedSolutionEngine );
      pentahoSystem.when( PentahoSystem::getApplicationContext ).thenReturn( applicationContext );
      pentahoSystem.when( PentahoSystem::getObjectFactory ).thenReturn( aggregateObjectFactory );

      runtimeCtx = new RuntimeContext( "id", mockedSolutionEngine, "solutionName",
        makeRuntimeData( session ), session, null, "processId", urlFactory,
        makeParameterProviders( session ), new ArrayList<String>(), null );

      final IRuntimeElement elem = new SimpleRuntimeElement( UUIDUtil.getUUIDAsString(), "parentId", "parentType", "sol1" );

      when( mockedRuntimeRepository.newRuntimeElement( anyString(), anyString(), anyBoolean() ) ).thenAnswer(
        (Answer<IRuntimeElement>) invocation -> elem
      );

      final boolean persisted = true;
      final boolean forceImmediateWrite = true;
      final List<Integer> list = new ArrayList<>();
      final Map<String, Object> params = new HashMap<>();
      final Date now = new Date();
      final BigDecimal three = new BigDecimal( "3.0" );

      params.put( "One", "One" );
      params.put( "Two", 2L );
      params.put( "Three", three );
      params.put( "Date", now );

      list.add( 1 );
      list.add( 2 );
      list.add( 3 );
      params.put( "List", list );

      runtimeCtx.createNewInstance( persisted, params, forceImmediateWrite );

      Assert.assertTrue( elem.getStringProperty( "One" ).equals( "One" ) );
      Assert.assertTrue( elem.getLongProperty( "Two", 0 ) == 2 );
      Assert.assertTrue( elem.getBigDecimalProperty( "Three" ).equals( three ) );
      Assert.assertTrue( elem.getDateProperty( "Date" ).equals( now ) );
      Assert.assertNotNullOrEmpty( elem.getListProperty( "List" ) );
    }
  }

  /**
   * It seems that status and error level are one and alike by design.
   * Enforce a test on this. In addition, achieve code coverage on these.
   */
  @Test
  public void testErrorLevel() {

    runtimeCtx = new RuntimeContext( "id", mockedSolutionEngine, "solutionName",
      makeRuntimeData( session ), session, null, "processId", urlFactory,
      makeParameterProviders( session ), new ArrayList<String>(), null );

    final RuntimeContext rctx = (RuntimeContext) runtimeCtx;
    Assert.assertTrue( rctx.getErrorLevel() == rctx.getStatus() );
  }

  @Test
  public void testPromptNow() {

    runtimeCtx = new RuntimeContext( "id", mockedSolutionEngine, "solutionName",
      makeRuntimeData( session ), session, null, "processId", urlFactory,
      makeParameterProviders( session ), new ArrayList<String>(), null );

    runtimeCtx.setPromptStatus( IRuntimeContext.PROMPT_NO );
    Assert.assertFalse( runtimeCtx.isPromptPending() );

    runtimeCtx.promptNow();
    Assert.assertTrue( runtimeCtx.isPromptPending() );
  }

  @Test
  public void testPromptNeeded() {
    runtimeCtx = new RuntimeContext( "id", mockedSolutionEngine, "solutionName",
      makeRuntimeData( session ), session, null, "processId", urlFactory,
      makeParameterProviders( session ), new ArrayList<String>(), null );

    runtimeCtx.setPromptStatus( IRuntimeContext.PROMPT_NO );
    Assert.assertFalse( runtimeCtx.isPromptPending() );

    runtimeCtx.promptNeeded();
    Assert.assertTrue( runtimeCtx.isPromptPending() );
  }

  @Test
  public void testGetUrlFactory() {

    runtimeCtx = new RuntimeContext( "id", mockedSolutionEngine, "solutionName",
      makeRuntimeData( session ), session, null, "processId", urlFactory,
      makeParameterProviders( session ), new ArrayList<String>(), null );

    Assert.assertNotNull( runtimeCtx.getUrlFactory() );
    Assert.assertTrue( runtimeCtx.getUrlFactory() == urlFactory );
  }

  private Map<String, IParameterProvider> makeParameterProviders( final IPentahoSession session ) {
    final Map<String, IParameterProvider> res = new HashMap<>();

    res.put( "jvm", new JVMParameterProvider() );
    res.put( SecurityParameterProvider.SCOPE_SECURITY, new SecurityParameterProvider( session ) );

    return res;
  }

  private IRuntimeElement makeRuntimeData( final IPentahoSession session ) {
    return new SimpleRuntimeElement( UUIDUtil.getUUIDAsString(), session.getId(), IParameterProvider.SCOPE_SESSION );
  }
}
