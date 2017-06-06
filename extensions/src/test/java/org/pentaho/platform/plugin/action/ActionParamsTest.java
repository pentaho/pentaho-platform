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
package org.pentaho.platform.plugin.action;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.util.ActionUtil;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ActionParamsTest {
  private Map<String, Serializable> buildSample() {
    final Map<String, Serializable> res = new HashMap<>();
    for ( int i = 0; i < 10; i++ ) {
      final String keyPostFix = String.valueOf( i );

      switch( i ) {
        case 0:
        case 2:
          res.put( "int-" + keyPostFix, i );
          break;

        case 1:
        case 3:
          res.put( "string-" + keyPostFix, String.valueOf( i ) );
          break;

        case 4:
        case 6:
          final HashMap<String, Serializable> sub = new HashMap<>();
          res.put( "map-" + keyPostFix, sub );

          for ( int j = 1000; sub.size() < new Random().nextInt( 10 ); j += j ) {
            sub.put( "sub-map-" + keyPostFix, new Random().nextLong() );
          }
          break;

        case 5:
        case 7:
          final Date date = new Date( new Random().nextLong() );
          final DateFormat dateFormat = DateFormat.getDateInstance();
          final String formatted = dateFormat.format( date );
          res.put( "date-" + keyPostFix, date );
          res.put( "date-formatted-" + keyPostFix, formatted );
          break;

        default:
          res.put( "default" + keyPostFix, "Have a nice day!" );
      }
    }

    return res;
  }

  private class ActionParamsTestAction implements IAction {

    @Override public void execute() throws Exception {
      // dummy impl does nothing
    }
  }

  @Test
  public void testEquals() throws Exception {
    final Map<String, Serializable> params1 = buildSample();
    final Map<String, Serializable> params2 = buildSample();
    final IAction dummyAction = new ActionParamsTestAction();

    Assert
      .assertEquals( ActionParams.serialize( dummyAction, params1 ), ActionParams.serialize( dummyAction, params1 ) );
    Assert.assertNotEquals( ActionParams.serialize( dummyAction, params1 ),
      ActionParams.serialize( dummyAction, params2 ) );
  }

  @Test
  public void testSerialization() throws Exception {
    final Map<String, Serializable> expected = buildSample();
    final IAction dummyAction = new ActionParamsTestAction();

    final ActionParams serializedParams = ActionParams.serialize( dummyAction, expected );
    final Map<String, Serializable> actual = ActionParams.deserialize( dummyAction, serializedParams );

    Assert.assertTrue( expected.equals( actual ) );
  }

  @Test
  public void testJsonification() throws Exception {
    final Map<String, Serializable> expected = buildSample();
    final IAction dummyAction = new ActionParamsTestAction();

    final ActionParams serializedParams = ActionParams.serialize( dummyAction, expected );
    final String jsonSerializedParams = ActionParams.toJson( serializedParams );

    final ActionParams jsonDeserializedParams = ActionParams.fromJson( jsonSerializedParams );

    Assert.assertTrue( serializedParams.equals( jsonDeserializedParams ) );
  }

  @Test
  public void testFilteredParams() throws Exception {
    final Map<String, Serializable> expected = buildSample();
    final IAction dummyAction = new ActionParamsTestAction();

    expected.put( ActionUtil.INVOKER_STREAMPROVIDER, new String() );
    expected.put( "::session", new String() );
    final ActionParams actionParams = ActionParams.serialize( dummyAction, expected );
    final ActionParams jsonManipulatedParams = ActionParams.fromJson( ActionParams.toJson( actionParams ) );
    final Map<String, Serializable> actual = ActionParams.deserialize( dummyAction, jsonManipulatedParams );

    Assert.assertTrue( actionParams.getParamsToRecreate().contains( ActionUtil.INVOKER_STREAMPROVIDER ) );
    Assert.assertTrue( jsonManipulatedParams.getParamsToRecreate().contains( ActionUtil.INVOKER_STREAMPROVIDER ) );
    Assert.assertFalse(
      actual.containsKey( ActionUtil.INVOKER_STREAMPROVIDER ) ); // at this point we don't recreate this param
    Assert.assertFalse( actual.containsKey( ActionUtil.INVOKER_SESSION ) );
  }
}
