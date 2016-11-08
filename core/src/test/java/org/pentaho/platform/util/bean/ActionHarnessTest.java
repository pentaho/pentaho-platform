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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.util.bean;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IVarArgsAction;
import org.pentaho.platform.util.beans.ActionHarness;
import org.pentaho.platform.util.beans.BeanUtil.EagerFailingCallback;
import org.pentaho.platform.util.beans.PropertyNameFormatter;

import java.util.Map;

@SuppressWarnings( "nls" )
public class ActionHarnessTest {

  @Before
  public void init() {

  }

  @Test
  public void testSetValue() throws Exception {
    IAction action = new TestVarArgsAction();
    ActionHarness harness = new ActionHarness( action );
    harness.setValue( "message", "test message" );

    Assert.assertEquals( "test message", harness.getValue( "message" ) );
  }

  @Test
  public void testSetValue2() throws Exception {
    TestAction action1 = new TestAction();
    ActionHarness harness1 = new ActionHarness( action1 );
    harness1.setValue( "message", "test message action1" );

    TestAction action2 = new TestAction();
    ActionHarness harness2 = new ActionHarness( action2 );
    harness2.setValue( "message", "test message action2" );

    Assert.assertEquals( "test message action1", harness1.getValue( "message" ) );
    Assert.assertEquals( "test message action2", harness2.getValue( "message" ) );
  }

  @Test
  public void testVarArgsActionSetValue() throws Exception {

    TestVarArgsAction action = new TestVarArgsAction();
    ActionHarness harness = new ActionHarness( action );
    harness.setValue( "message", "test message" );
    harness.setValue( "undeclaredParam1", "undeclaredParam1 value" );
    harness.setValue( "undeclaredParam2", "undeclaredParam2 value" );

    Assert.assertEquals( "test message", harness.getValue( "message" ) );
    Assert.assertTrue( action.getVarArgs().containsKey( "undeclaredParam1" ) );
    Assert.assertTrue( action.getVarArgs().containsKey( "undeclaredParam2" ) );
    Assert.assertEquals( "undeclaredParam1 value", action.getVarArgs().get( "undeclaredParam1" ) );
    Assert.assertEquals( "undeclaredParam2 value", action.getVarArgs().get( "undeclaredParam2" ) );
  }

  @Test( expected = IllegalAccessException.class )
  public void testSetValueNonExistentProperty() throws Exception {

    TestAction action1 = new TestAction();
    ActionHarness harness1 = new ActionHarness( action1 );
    harness1.setValue( "THISPROPERTYDOESNOTEXIST", "", new EagerFailingCallback() );

    Assert.assertEquals( "test message action1", harness1.getValue( "message" ) );
  }

  @Test
  public void testSetNullIsSkippedWithNotError() throws Exception {

    TestAction action1 = new TestAction();
    ActionHarness harness1 = new ActionHarness( action1 );
    // commons bBeanutils will throw exception if you try to type convert a null value. Pentaho BeanUtil
    // will skip the set operation altogether if you try to set a null value on a bean. This test ensures
    // that no exception is thrown in this case. The correct behavior is a message will be logged indicating
    // that the null value could not be set. I don't consider this a "real" type conversion problem like
    // a text string failing conversion to a Long type.. this type of conversion will still fail.
    harness1.setValue( "count", null, new EagerFailingCallback() );
    // If no exception thrown, then the test is essentially passed. We'll double check that
    // count was not set as well.
    Assert.assertNull( "count property should remain null since null set value ops should be skipped", action1
        .getCount() );
  }

  @Test( expected = Exception.class )
  public void testSetValueFailedConvertWitNonNullValue() throws Exception {

    TestAction action1 = new TestAction();
    ActionHarness harness1 = new ActionHarness( action1 );
    harness1.setValue( "count", new CustomParamType(), new EagerFailingCallback() );
  }

  @Test
  public void testSetValueWithFormatter() throws Exception {

    TestAction action1 = new TestAction();
    ActionHarness harness1 = new ActionHarness( action1 );

    PropertyNameFormatter f = new PropertyNameFormatter() {

      public String format( String name ) {
        return "message";
      }
    };

    harness1.setValue( "THISWILLGETCLOBBERED", "test message action1", new EagerFailingCallback(), f );

    Assert.assertEquals( "test message action1", harness1.getValue( "message" ) );
  }

  @Test( expected = IllegalAccessException.class )
  public void testSetValueWithFormatterNonExistentProperty() throws Exception {

    TestAction action1 = new TestAction();
    ActionHarness harness1 = new ActionHarness( action1 );

    PropertyNameFormatter f = new PropertyNameFormatter() {

      public String format( String name ) {
        return "THISPROPERTYDOESNOTEXIST";
      }
    };

    harness1.setValue( "THISWILLGETCLOBBERED", "test message action1", new EagerFailingCallback(), f );
  }

  public class TestVarArgsAction implements IVarArgsAction {

    private String message;
    private boolean executeWasCalled = false;
    private Map<String, Object> varArgs;

    public String getMessage() {
      return message;
    }

    public void setMessage( String message ) {
      this.message = message;
    }

    public boolean isExecuteWasCalled() {
      return executeWasCalled;
    }

    public void execute() throws Exception {
      executeWasCalled = true;
    }

    public void setVarArgs( Map<String, Object> args ) {
      this.varArgs = args;
    }

    public Map<String, Object> getVarArgs() {
      return this.varArgs;
    }
  }

  public static class CustomParamType {

  }

}
