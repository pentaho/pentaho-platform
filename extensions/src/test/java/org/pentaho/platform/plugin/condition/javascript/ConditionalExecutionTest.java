/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.condition.javascript;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.junit.Test;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;

public class ConditionalExecutionTest {

  private static final Log logger = mock( Log.class );

  @Test
  public void testShouldExecute_returns_true_for_true_string_result() throws Exception {
    ConditionalExecution conditionalExecution = new ConditionalExecution();
    conditionalExecution.setScript( "var result = 'true'; result;" );
    boolean actualResult = conditionalExecution.shouldExecute( Collections.EMPTY_MAP, logger );
    assertTrue( actualResult );
  }

  @Test
  public void testShouldExecute_returns_true_for_yes_string_result() throws Exception {
    ConditionalExecution conditionalExecution = new ConditionalExecution();
    conditionalExecution.setScript( "var result = 'yes'; result;" );
    boolean actualResult = conditionalExecution.shouldExecute( Collections.EMPTY_MAP, logger );
    assertTrue( actualResult );
  }

  @Test
  public void testShouldExecute_returns_false_properly() throws Exception {
    ConditionalExecution conditionalExecution = new ConditionalExecution();
    conditionalExecution.setScript( "var result = 'no'; result;" );
    boolean actualResult = conditionalExecution.shouldExecute( Collections.EMPTY_MAP, logger );
    assertFalse( actualResult );
  }

  @Test
  public void testShouldExecute_returns_true_for_true_result() throws Exception {
    ConditionalExecution conditionalExecution = new ConditionalExecution();
    conditionalExecution.setScript( "var result = true; result;" );
    boolean actualResult = conditionalExecution.shouldExecute( Collections.EMPTY_MAP, logger );
    assertTrue( actualResult );
  }

  @Test
  public void testShouldExecute_returns_true_for_number_result_greater_than_0() throws Exception {
    ConditionalExecution conditionalExecution = new ConditionalExecution();
    conditionalExecution.setScript( "var result = 1; result;" );
    boolean actualResult = conditionalExecution.shouldExecute( Collections.EMPTY_MAP, logger );
    assertTrue( actualResult );
  }

  @Test
  public void testShouldExecute_returns_false_for_number_result_less_than_0() throws Exception {
    ConditionalExecution conditionalExecution = new ConditionalExecution();
    conditionalExecution.setScript( "var result = -1; result;" );
    boolean actualResult = conditionalExecution.shouldExecute( Collections.EMPTY_MAP, logger );
    assertFalse( actualResult );
  }

  @Test
  public void testShouldExecute_returns_true_for_result_with_rows() throws Exception {
    ConditionalExecution conditionalExecution = new ConditionalExecution();
    conditionalExecution.setScript( "resultElement;" );
    IActionParameter parameter = createParameterWithResult( 1 );
    boolean actualResult =
        conditionalExecution.shouldExecute( Collections.singletonMap( "resultElement", parameter ), logger );
    assertTrue( actualResult );
  }

  @Test
  public void testShouldExecute_returns_false_for_result_without_rows() throws Exception {
    ConditionalExecution conditionalExecution = new ConditionalExecution();
    conditionalExecution.setScript( "resultElement;" );
    IActionParameter parameter = createParameterWithResult( 0 );
    boolean actualResult =
        conditionalExecution.shouldExecute( Collections.singletonMap( "resultElement", parameter ), logger );
    assertFalse( actualResult );
  }

  private static IActionParameter createParameterWithResult( int rowsCount ) {
    IActionParameter parameter = mock( IActionParameter.class );
    IPentahoResultSet resultSet = mock( IPentahoResultSet.class );
    when( resultSet.getRowCount() ).thenReturn( rowsCount );
    when( parameter.getValue() ).thenReturn( resultSet );
    return parameter;
  }

}
