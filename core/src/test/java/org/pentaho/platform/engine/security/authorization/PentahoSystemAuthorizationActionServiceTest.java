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

package org.pentaho.platform.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginManagerListener;
import org.pentaho.platform.engine.core.system.objfac.spring.Const;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;

public class PentahoSystemAuthorizationActionServiceTest {

  private PentahoSystemAuthorizationActionService service;
  private IPluginManager pluginManager;

  @NonNull
  private IPentahoObjectReference<IAuthorizationAction> createActionObjectReference(
    @NonNull IAuthorizationAction action,
    @NonNull Map<String, Object> attrs ) {

    IPentahoObjectReference<IAuthorizationAction> actionReference = mock( IPentahoObjectReference.class );
    when( actionReference.getObject() ).thenReturn( action );
    when( actionReference.getRanking() ).thenReturn( 0 );
    when( actionReference.getAttributes() ).thenReturn( attrs );

    return actionReference;
  }

  @NonNull
  private IPentahoObjectReference<IAuthorizationAction> createActionObjectReference(
    @NonNull IAuthorizationAction action ) {
    return createActionObjectReference( action, Collections.emptyMap() );
  }

  @Before
  public void setUp() {
    pluginManager = mock( IPluginManager.class );

    service = new PentahoSystemAuthorizationActionService(
      pluginManager,
      this::createSampleActionsStream );
  }

  @NonNull
  private Stream<IPentahoObjectReference<IAuthorizationAction>> createSampleActionsStream() {
    var systemAction1 = createTestAction( "systemAction1" );
    var systemAction2 = createTestAction( "systemAction2" );
    var systemAction2Dup = createTestAction( "systemAction2" );
    // An action with an empty name. Should be excluded from the results.
    var systemAction3 = createTestAction( "" );
    var pluginAction1 = createTestAction( "pluginAction1" );
    var pluginAction2 = createTestAction( "pluginAction2" );
    var pluginAction2Dup = createTestAction( "pluginAction2" );
    var pluginAction3 = createTestAction( "pluginAction3" );

    Map<String, Object> plugin1Attrs = new HashMap<>();
    plugin1Attrs.put( Const.PUBLISHER_PLUGIN_ID_ATTRIBUTE, "plugin1" );

    Map<String, Object> plugin2Attrs = new HashMap<>();
    plugin2Attrs.put( Const.PUBLISHER_PLUGIN_ID_ATTRIBUTE, "plugin2" );

    var systemActionRef1 = createActionObjectReference( systemAction1 );
    var systemActionRef2 = createActionObjectReference( systemAction2 );
    var systemActionRef2Dup = createActionObjectReference( systemAction2Dup );
    var systemActionRef3 = createActionObjectReference( systemAction3 );

    var pluginActionRef1 = createActionObjectReference( pluginAction1, plugin1Attrs );
    var pluginActionRef2 = createActionObjectReference( pluginAction2, plugin2Attrs );
    var pluginActionRef2Dup = createActionObjectReference( pluginAction2Dup, plugin2Attrs );
    var pluginActionRef3 = createActionObjectReference( pluginAction3, plugin2Attrs );

    // Compose a supplier with all refs, including duplicates by name.
    // First entries in the stream will the ones with the highest ranking
    // (ensured by PentahoSystem.getObjectReferences()).
    return Stream.of(
      systemActionRef1, systemActionRef2, systemActionRef2Dup, systemActionRef3,
      pluginActionRef1, pluginActionRef2, pluginActionRef2Dup, pluginActionRef3 );
  }

  @NonNull
  private List<String> getNames( @NonNull Stream<IAuthorizationAction> stream ) {
    List<String> names = new ArrayList<>();
    stream.forEach( a -> names.add( a.getName() ) );
    return names;
  }

  @Test
  public void testGetActionsReturnsAllDistinctActions() {
    List<String> names = getNames( service.getActions() );
    assertTrue( names.contains( "systemAction1" ) );
    assertTrue( names.contains( "systemAction2" ) );
    assertTrue( names.contains( "pluginAction1" ) );
    assertTrue( names.contains( "pluginAction2" ) );
    assertTrue( names.contains( "pluginAction3" ) );
    assertEquals( 5, names.size() );
  }

  @Test
  public void testGetSystemActionsReturnsOnlySystemActions() {
    List<String> names = getNames( service.getSystemActions() );
    assertTrue( names.contains( "systemAction1" ) );
    assertTrue( names.contains( "systemAction2" ) );
    assertEquals( 2, names.size() );
  }

  @Test
  public void testGetPluginActionsReturnsOnlyPluginActions() {
    List<String> names = getNames( service.getPluginActions() );
    assertTrue( names.contains( "pluginAction1" ) );
    assertTrue( names.contains( "pluginAction2" ) );
    assertTrue( names.contains( "pluginAction3" ) );
    assertEquals( 3, names.size() );
  }

  @Test
  public void testGetPluginActionsWithPluginId() {
    List<String> names = getNames( service.getPluginActions( "plugin1" ) );
    assertEquals( 1, names.size() );
    assertEquals( "pluginAction1", names.get( 0 ) );

    names = getNames( service.getPluginActions( "plugin2" ) );
    assertEquals( 2, names.size() );
    assertEquals( "pluginAction2", names.get( 0 ) );
    assertEquals( "pluginAction3", names.get( 1 ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testGetPluginActionsWithNullPluginIdThrows() {
    service.getPluginActions( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testGetPluginActionsWithEmptyPluginIdThrows() {
    service.getPluginActions( "" );
  }

  @Test
  public void testPluginManagerRefreshesActionsOnPluginManagerReload() {
    // Capture the plugin manager listener registered in the service constructor.
    var listenerAtomicReference = new java.util.concurrent.atomic.AtomicReference<IPluginManagerListener>();
    doAnswer( invocation -> {
      listenerAtomicReference.set( invocation.getArgument( 0 ) );
      return null;
    } ).when( pluginManager ).addPluginManagerListener( any() );

    // Create a new service, which should register a listener with the mock plugin manager.
    service = new PentahoSystemAuthorizationActionService(
      pluginManager,
      this::createSampleActionsStream );

    // Verify that a listener was registered.
    IPluginManagerListener listener = listenerAtomicReference.get();
    assertNotNull( listener );

    // Capture actions and their names before listener is called
    List<IAuthorizationAction> actionsBefore = service.getActions().toList();
    List<String> namesBefore = actionsBefore.stream().map( IAuthorizationAction::getName ).toList();

    listener.onReload();

    // Capture actions and their names after listener is called
    List<IAuthorizationAction> actionsAfter = service.getActions().toList();
    List<String> namesAfter = actionsAfter.stream().map( IAuthorizationAction::getName ).toList();

    // Assert that the names are the same
    assertEquals( namesBefore, namesAfter );

    // Assert that the instances are different for each name (by index)
    for ( int i = 0; i < actionsBefore.size(); i++ ) {
      assertNotSame( actionsBefore.get( i ), actionsAfter.get( i ) );
    }
  }
}
