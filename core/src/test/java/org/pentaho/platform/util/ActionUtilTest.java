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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.workitem.IWorkItemLifecycleEventPublisher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.workitem.DummyPublisher;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@RunWith( MockitoJUnitRunner.class )
public class ActionUtilTest {

  @Test( expected = ActionInvocationException.class )
  public void resolveClassIllegalArgumentExceptionWithEmptyStrings()
    throws PluginBeanException, ActionInvocationException {
    ActionUtil.resolveActionClass( "", "" );
  }

  @Test( expected = ActionInvocationException.class )
  public void resolveClassIllegalArgumentExceptionWithNulls() throws PluginBeanException, ActionInvocationException {
    ActionUtil.resolveActionClass( null, null );
  }

  @Test( expected = ActionInvocationException.class )
  public void createActionBeanIllegalArgumentExceptionWithEmptyStrings()
    throws ActionInvocationException {
    ActionUtil.createActionBean( "", "" );
  }

  @Test( expected = ActionInvocationException.class )
  public void createActionBeanIllegalArgumentExceptionWithNulls()
    throws ActionInvocationException {
    ActionUtil.createActionBean( null, null );
  }

  @Test
  public void resolveClassTestHappyPathNoBeanID() throws Exception {
    Class<?> aClass = ActionUtil.resolveActionClass( MyTestAction.class.getName(), "" );
    assertEquals( MyTestAction.class, aClass );
  }

  @Test
  public void resolveClassTestHappyPath() throws Exception {
    // TODO: rewrite this test to read bean from spring rather than mocking it
    String beanId = "ktr.backgroundAction";
    Class<?> clazz = MyTestAction.class;

    IPluginManager pluginManager = mock( IPluginManager.class );
    try ( MockedStatic<PentahoSystem> pentahoSystem = mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( eq( IPluginManager.class ) ) ).thenReturn( pluginManager );
      Mockito.doReturn( clazz ).when( pluginManager ).loadClass( anyString() );
      Class<?> aClass = ActionUtil.resolveActionClass( MyTestAction.class.getName(), beanId );
      assertEquals( MyTestAction.class, aClass );
    }
  }

  @Test
  public void createActionBeanHappyPath() throws ActionInvocationException {
    IAction iaction = ActionUtil.createActionBean( MyTestAction.class.getName(), null );
    assertEquals( iaction.getClass(), MyTestAction.class );
  }


  @Test
  public void removeFromMapHappyPathTest() {
    Map<String, String> testMap = new HashMap<>();
    testMap.put( "one", "one" );
    testMap.put( "two", "two" );
    testMap.remove( "one" );
    assertNull( testMap.get( "one" ) );
    assertEquals( testMap.get( "two" ), "two" );
  }

  @Test
  public void removeFromMapSecondHappyPathTest() {
    Map<String, String> testMap = new HashMap<>();
    testMap.put( "one", "one" );
    testMap.put( "two", "two" );
    testMap.put( "actionClass", "actionClass" );
    testMap.remove( "actionClass" );
    assertNull( testMap.get( "actionClass" ) );
    assertEquals( testMap.get( "two" ), "two" );
  }

  @Test
  public void removeFromMapHappyPathMappedKeyTest() {
    Map<String, String> testMap = new HashMap<>();
    testMap.put( ActionUtil.QUARTZ_ACTIONCLASS, "one" );
    testMap.put( "two", "two" );
    testMap.remove( ActionUtil.QUARTZ_ACTIONCLASS );
    assertNull( testMap.get( ActionUtil.QUARTZ_ACTIONCLASS ) );
    assertEquals( testMap.get( "two" ), "two" );
  }

  @Test
  public void prepareMapNullTest() {
    Map<String, Serializable> testMap = null;
    ActionUtil.prepareMap( testMap );
    assertNull( testMap );
  }

  @Test
  public void prepareMapTest() {
    Map<String, Serializable> testMap = new HashMap<>();
    testMap.put( ActionUtil.QUARTZ_ACTIONCLASS, "one" );
    testMap.put( ActionUtil.QUARTZ_ACTIONUSER, "two" );
    ActionUtil.prepareMap( testMap );
    assertNull( testMap.get( ActionUtil.QUARTZ_ACTIONCLASS ) );
    assertNull( testMap.get( ActionUtil.QUARTZ_ACTIONUSER ) );
  }

  @Test
  public void testExtractName() {
    try ( MockedStatic<PentahoSystem> pentahoSystem = mockStatic( PentahoSystem.class ) ) {
      // fake the publisher, so that a call to extractName returns a value and puts it in the params map
      pentahoSystem.when( () -> PentahoSystem.get( eq( IWorkItemLifecycleEventPublisher.class ) ) ).thenReturn( new DummyPublisher() );
      final Map<String, Object> params = new HashMap<>();

      assertNotNull( ActionUtil.extractName( params ) );
      // the map should now contain a uid
      assertTrue( params.containsKey( ActionUtil.WORK_ITEM_NAME ) );
      final String uid = (String) params.get( ActionUtil.WORK_ITEM_NAME );
      assertEquals( uid, ActionUtil.extractName( params ) );
      assertEquals( 1, params.size() );
    }
  }

  @Test
  public void testExtractNameWithoutPublisher() {
    // by default, there is no IWorkItemLifecycleEventPublisher bean, call to extractName should return null
    final Map<String, Object> params = new HashMap<>();

    assertNull( ActionUtil.extractName( params ) );
    assertFalse( params.containsKey( ActionUtil.WORK_ITEM_UID ) );
  }

  @Test
  public void testGenerateWorkItemNameWithMap() {

    Map<String, Object> map = null;
    String result;

    // null map
    result = ActionUtil.generateWorkItemName( map );
    assertEquals( "[]", result );

    // empty map
    map = new HashMap<>();
    result = ActionUtil.generateWorkItemName( map );
    assertEquals( "[]", result );

    // quartz username only
    map = new HashMap<>();
    map.put( ActionUtil.QUARTZ_ACTIONUSER, "quartzAdmin" );
    result = ActionUtil.generateWorkItemName( map );
    assertEquals( "[quartzAdmin]", result );

    // action username only
    map = new HashMap<>();
    map.put( ActionUtil.INVOKER_ACTIONUSER, "actionAdmin" );
    result = ActionUtil.generateWorkItemName( map );
    assertEquals( "[actionAdmin]", result );

    // quartz and action username
    map = new HashMap<>();
    map.put( ActionUtil.QUARTZ_ACTIONUSER, "quartzAdmin" );
    map.put( ActionUtil.INVOKER_ACTIONUSER, "actionAdmin" );
    result = ActionUtil.generateWorkItemName( map );
    assertEquals( "[actionAdmin]", result );

    // quartz username only
    map = new HashMap<>();
    map.put( ActionUtil.QUARTZ_STREAMPROVIDER_INPUT_FILE, "quartzInputFile" );
    result = ActionUtil.generateWorkItemName( map );
    assertEquals( "quartzInputFile[]", result );

    // action username only
    map = new HashMap<>();
    map.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, "adminInputFile" );
    result = ActionUtil.generateWorkItemName( map );
    assertEquals( "adminInputFile[]", result );

    // quartz and action username
    map = new HashMap<>();
    map.put( ActionUtil.QUARTZ_STREAMPROVIDER_INPUT_FILE, "quartzInputFile" );
    map.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, "adminInputFile" );
    result = ActionUtil.generateWorkItemName( map );
    assertEquals( "adminInputFile[]", result );

    // all values present
    map = new HashMap<>();
    map.put( ActionUtil.QUARTZ_STREAMPROVIDER_INPUT_FILE, "quartzInputFile" );
    map.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, "adminInputFile" );
    map.put( ActionUtil.QUARTZ_ACTIONUSER, "quartzAdmin" );
    map.put( ActionUtil.INVOKER_ACTIONUSER, "actionAdmin" );
    result = ActionUtil.generateWorkItemName( map );
    assertEquals( "adminInputFile[actionAdmin]", result );

  }

  @Test
  public void testGenerateWorkItemName() {
    String result;

    // simple case
    result = ActionUtil.generateWorkItemName( "Test.prpt", "admin" );
    assertEquals( "Test.prpt[admin]", result );

    // bad characters
    result = ActionUtil.generateWorkItemName( "!@#$%^&*.prpt", "adm&*&in" );
    assertEquals( "!@#$%^&*.prpt[adm&*&in]", result );

    // all bad characters
    result = ActionUtil.generateWorkItemName( "!@#$%^&*.prpt", "&*&(*&" );
    assertEquals( "!@#$%^&*.prpt[&*&(*&]", result );

    // file path and spaces
    result = ActionUtil.generateWorkItemName( "folder/Test File.prpt", "adm&*&in" );
    assertEquals( "folder/Test File.prpt[adm&*&in]", result );

    // missing user and file
    result = ActionUtil.generateWorkItemName( "", "" );
    assertEquals( "[]", result );
    result = ActionUtil.generateWorkItemName( null, null );
    assertEquals( "[]", result );
  }

  @Test
  public void testRemoveKeyFromMap() {
    // null map - verify no exception is thrown
    try {
      ActionUtil.removeKeyFromMap( null, null );
      ActionUtil.removeKeyFromMap( null, "" );
    } catch ( final Exception e ) {
      Assert.fail();
    }

    final Map<String, Serializable> map = new HashMap<>();
    map.put( ActionUtil.QUARTZ_ACTIONCLASS, "actionClass" );
    map.put( ActionUtil.QUARTZ_ACTIONUSER, "user" );
    map.put( ActionUtil.INVOKER_ACTIONUSER, "user" );
    map.put( ActionUtil.QUARTZ_ACTIONID, "actionId" );
    map.put( ActionUtil.INVOKER_ACTIONID, "actionId" );
    map.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, "inputFile" );
    // clone the original map
    final Map<String, Serializable> clone = new HashMap<>( map );

    // bad keys, verify map isn't changed
    ActionUtil.removeKeyFromMap( map, null );
    Assert.assertEquals( map, clone );
    ActionUtil.removeKeyFromMap( map, "" );
    Assert.assertEquals( map, clone );
    ActionUtil.removeKeyFromMap( map, "some key" );
    Assert.assertEquals( map, clone );

    // verify that when KEY_MAP key is provided, both values are removed
    ActionUtil.removeKeyFromMap( map, ActionUtil.QUARTZ_ACTIONUSER );
    Assert.assertEquals( 4, map.size() );
    // verify that the expected keys are no longer present in the map
    Assert.assertNull( map.get( ActionUtil.QUARTZ_ACTIONUSER ) );
    Assert.assertNull( map.get( ActionUtil.INVOKER_ACTIONUSER ) );

    // verify that when KEY_MAP value is provided, both values are removed
    ActionUtil.removeKeyFromMap( map, ActionUtil.INVOKER_ACTIONID );
    Assert.assertEquals( 2, map.size() );
    // verify that the expected keys are no longer present in the map
    Assert.assertNull( map.get( ActionUtil.INVOKER_ACTIONID ) );
    Assert.assertNull( map.get( ActionUtil.QUARTZ_ACTIONID ) );
  }
}
