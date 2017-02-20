/*
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
 * Copyright 2017 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.engine.services.runtime;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.util.Assert;
import org.pentaho.platform.api.engine.ICreateFeedbackParameterCallback;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.api.repository.RepositoryException;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.RuntimeObjectFactory;
import org.pentaho.platform.engine.security.SecurityParameterProvider;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.util.JVMParameterProvider;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuntimeContextTest {
  private final IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" );
  private IRuntimeContext runtimeCtx;
  private IPentahoSession session;
  private RuntimeRepository repository;

  @BeforeClass
  public static void setup() {
    final String solutionPath = ".";
    final String applicationPath = "";
    PentahoSystem.setApplicationContext( new StandaloneApplicationContext( solutionPath, applicationPath ) );

    final IPentahoRegistrableObjectFactory factory = new RuntimeObjectFactory();
    factory.registerObject( new SolutionEngine() );
    factory.registerObject( new RuntimeRepository() );
    PentahoSystem.registerPrimaryObjectFactory( factory );
  }

  @Before
  public void before() {
    final String instanceId = null;
    final IOutputHandler providerHandler = null;
    final ICreateFeedbackParameterCallback feedbackParameterCallback = null;
    final List<String> messages = new ArrayList<>();
    final String processId = "empty action sequence";
    final String solutionName = "solution";

    session = new StandaloneSession( "system" );
    repository = (RuntimeRepository) PentahoSystem.get( IRuntimeRepository.class, session );
    repository.reset();

    runtimeCtx = new RuntimeContext( instanceId, makeSolutionEngine( session ), solutionName,
      makeRuntimeData( session ), session, providerHandler, processId, urlFactory,
      makeParameterProviders( session ), messages, feedbackParameterCallback );
  }

  /**
   * Sanity test the basics - createNewInstance() works passing all combinations of arguments.
   *
   * @throws Exception
   */
  @Test
  public void testCreateNewInstanceBasicSanity() throws Exception {
    final boolean persisted = true;
    final boolean notPersisted = false;
    final boolean forceImmediateWrite = true;
    final boolean dontForceImmediateWrite = false;
    final Map<String, String> noParams = new HashMap<>(  );

    Assert.assertNotNull( runtimeCtx.createNewInstance( persisted ) );
    Assert.assertNotNull( runtimeCtx.createNewInstance( notPersisted ) );
    Assert.assertNotNull( runtimeCtx.createNewInstance( persisted, noParams ) );
    Assert.assertNotNull( runtimeCtx.createNewInstance( notPersisted, noParams ) );
    Assert.assertNotNull( runtimeCtx.createNewInstance( persisted, noParams, forceImmediateWrite ) );
    Assert.assertNotNull( runtimeCtx.createNewInstance( notPersisted, noParams, dontForceImmediateWrite ) );

    final int expectedSize = 6;
    final int expectedPersisted = 3;
    Assert.assertTrue( repository.getSize() == expectedSize );
    Assert.assertTrue( repository.getPersisted() == expectedPersisted );
  }

  /**
   * Tests that params are passed and saved correctly to the underlying runtime element.
   *
   * @throws Exception
   */
  @Test
  public void testCreateNewInstanceWithParams() throws Exception {
    final boolean persisted = true;
    final boolean forceImmediateWrite = true;
    final List<Integer> list = new ArrayList<>();
    final Map<String, Object> params = new HashMap<>();
    final Date now = new Date();
    final BigDecimal three = new BigDecimal( 3.0d );

    params.put( "One", "One" );
    params.put( "Two", new Long( 2 ) );
    params.put( "Three", three );
    params.put( "Date", now );

    list.add( 1 );
    list.add( 2 );
    list.add( 3 );
    params.put( "List", list );

    final Collection readAttrs = null;
    final String instanceId = runtimeCtx.createNewInstance( persisted, params, forceImmediateWrite );
    final IRuntimeElement elem = repository.loadElementById( instanceId, readAttrs );

    Assert.assertNotNull( elem );
    Assert.assertTrue( elem.getStringProperty( "One" ).equals( "One" ) );
    Assert.assertTrue( elem.getLongProperty( "Two", 0 ) == 2 );
    Assert.assertTrue( elem.getBigDecimalProperty( "Three" ).equals( three ) );
    Assert.assertTrue( elem.getDateProperty( "Date" ).equals( now ) );
    Assert.assertNotNullOrEmpty( elem.getListProperty( "List" ) );
  }

  /**
   * It seems that status and error level are one and alike by design.
   * Enforce a test on this. In addition achieve code coverage on these.
   *
   * @throws Exception
   */
  @Test
  public void testErrorLevel() throws Exception {
    final RuntimeContext rctx = (RuntimeContext) runtimeCtx;
    Assert.assertTrue( rctx.getErrorLevel() == rctx.getStatus() );
  }

  @Test
  public void testPromptNow() throws Exception {
    runtimeCtx.setPromptStatus( IRuntimeContext.PROMPT_NO );
    Assert.assertFalse( runtimeCtx.isPromptPending() );

    runtimeCtx.promptNow();
    Assert.assertTrue( runtimeCtx.isPromptPending() );
  }

  @Test
  public void testPromptNeeded() throws Exception {
    runtimeCtx.setPromptStatus( IRuntimeContext.PROMPT_NO );
    Assert.assertFalse( runtimeCtx.isPromptPending() );

    runtimeCtx.promptNeeded();
    Assert.assertTrue( runtimeCtx.isPromptPending() );
  }

  @Test
  public void testGetUrlFactory() throws Exception {
    Assert.assertNotNull( runtimeCtx.getUrlFactory() );
    Assert.assertTrue( runtimeCtx.getUrlFactory() == urlFactory );
  }

  private Map<String, IParameterProvider> makeParameterProviders( final IPentahoSession session ) {
    final Map<String, IParameterProvider> res = new HashMap<>();

    res.put( "jvm", new JVMParameterProvider() );
    res.put( SecurityParameterProvider.SCOPE_SECURITY, new SecurityParameterProvider( session ) );

    return res;
  }

  private ISolutionEngine makeSolutionEngine( final IPentahoSession session ) {
    final ISolutionEngine res = PentahoSystem.get( ISolutionEngine.class, session );
    res.setLoggingLevel( ILogger.ERROR );
    res.init( session );
    return res;
  }

  private IRuntimeElement makeRuntimeData( final IPentahoSession session ) {
    return new SimpleRuntimeElement( UUIDUtil.getUUIDAsString(), session.getId( ), IParameterProvider.SCOPE_SESSION );
  }
  /**
   * Test oriented repository implementation, used for confirming expected results.
   * TODO: consider replacing this with a mock object.
   */
  private static class RuntimeRepository extends PentahoBase implements IRuntimeRepository {
    private static final ThreadLocal session = new ThreadLocal();
    private Map<String, IRuntimeElement> data = new HashMap<>(  );
    private int persisted = 0;

    public static IPentahoSession getUserSession() {
      IPentahoSession userSession = (IPentahoSession) session.get();
      return userSession;
    }

    public RuntimeRepository() {
    }

    public List getMessages() {
      return null;
    }

    public void setSession( final IPentahoSession sess ) {
      session.set( sess );
      genLogIdFromSession( getUserSession() );
    }

    public IRuntimeElement loadElementById( final String instanceId, final Collection allowableReadAttributeNames )
      throws RepositoryException {

      return data.get( instanceId );
    }

    public IRuntimeElement newRuntimeElement( final String parId, final String parType, final boolean transientOnly ) {
      final String instanceId = UUIDUtil.getUUIDAsString();
      final IRuntimeElement res = new SimpleRuntimeElement( instanceId, parId, parType );
      data.put( instanceId, res );
      if ( !transientOnly ) {
        persisted++;
      }

      return res;
    }

    public IRuntimeElement newRuntimeElement( final String parId, final String parType, final String solnId,
                                              final boolean transientOnly ) {
      final String instanceId = UUIDUtil.getUUIDAsString();
      final IRuntimeElement res = new SimpleRuntimeElement( instanceId, parId, parType, solnId );
      data.put( instanceId, res );
      return res;
    }

    @Override
    public Log getLogger() {
      return null;
    }

    public boolean usesHibernate() {
      return false;
    }

    public int getSize() {
      return data.size();
    }

    public int getPersisted() {
      return persisted;
    }

    public void reset() {
      data.clear();
      persisted = 0;
    }
  }
}
