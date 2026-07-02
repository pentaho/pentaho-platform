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


package org.pentaho.platform.plugin.action.builtin;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class ActionSequenceActionTest {

  private List<IContentItem> expectedContentItem = Arrays.asList( mock( IContentItem.class ) );

  private IPentahoObjectFactory pentahoObjectFactory;

  @Before
  public void setUp() throws ObjectFactoryException {
    IRuntimeContext context = mock( IRuntimeContext.class );
    when( context.getOutputContentItems() ).thenReturn( expectedContentItem );
    when( context.getStatus() ).thenReturn( IRuntimeContext.RUNTIME_STATUS_SUCCESS );

    final ISolutionEngine engine = mock( ISolutionEngine.class );
    when( engine.execute( nullable( String.class ), nullable( String.class ), anyBoolean(), anyBoolean(), nullable( String.class ), anyBoolean(),
        anyMap(), any( IOutputHandler.class ), nullable( IActionCompleteListener.class ),
        nullable( IPentahoUrlFactory.class ), anyList() ) ).thenReturn( context );

    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( nullable( String.class ) ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), nullable( String.class ), nullable( IPentahoSession.class ) ) ).thenAnswer( new Answer<Object>() {
      @Override
      public ISolutionEngine answer( InvocationOnMock invocation ) throws Throwable {
        return engine;
      }
    } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );
  }

  @Test
  public void testGetActionOutputContents() throws Exception {
    OutputStream outputStream = mock( OutputStream.class );

    ActionSequenceAction action = new ActionSequenceAction();
    action.setOutputStream( outputStream );
    action.execute();
    List<IContentItem> outputContents = action.getActionOutputContents();
    assertNotNull( outputContents );
    assertEquals( expectedContentItem, outputContents );
  }

  @After
  public void tearDown() {
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
    PentahoSystem.shutdown();
  }

  private Class<?> anyClass() {
    return argThat( new AnyClassMatcher() );
  }

  private class AnyClassMatcher implements ArgumentMatcher<Class<?>> {
    @Override
    public boolean matches( Class<?> arg ) {
      // We return true, because we want to acknowledge all class types
      return true;
    }
  }
}
