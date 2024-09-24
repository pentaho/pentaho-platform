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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.datatransforms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.dom4j.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.actions.ResultSetCompareAction;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.util.LogUtil;

public class ResultSetCompareComponentTest {

  private static final String RESULT_SET_ELEMENT_VALUE = "TEST_VALUE";

  private static final String FIRST_ROW = "FIRST_ROW";

  private static final String SECOND_ROW = "SECOND_ROW";

  private final Level level = LogManager.getRootLogger().getLevel();

  @Before
  public void setUp() {
    LogUtil.setLevel(LogManager.getLogger(), Level.OFF);
  }

  @Test
  public void validation_fails_without_both_resultSets() {
    int actualValidateResult = callValidationWithResultSets( null, null );
    assertEquals( IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, actualValidateResult );
  }

  @Test
  public void validation_fails_without_first_resultSets() {
    IPentahoResultSet rs = Mockito.mock( IPentahoResultSet.class );
    int actualValidateResult = callValidationWithResultSets( null, rs );
    assertEquals( IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, actualValidateResult );
  }

  @Test
  public void validation_fails_without_second_resultSets() {
    IPentahoResultSet rs = Mockito.mock( IPentahoResultSet.class );
    int actualValidateResult = callValidationWithResultSets( rs, null );
    assertEquals( IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, actualValidateResult );
  }

  @Test
  public void validation_fails_without_compareColumnNumber() {
    ResultSetCompareComponent rscc = createResultSetCompareComponent();
    IPentahoResultSet rs = Mockito.mock( IPentahoResultSet.class );
    ResultSetCompareAction resultSetCompareAction = createResultSetCompareAction( rs, rs, null, false, true );
    rscc.setActionDefinition( resultSetCompareAction );
    int actualValidateResult = rscc.validate();
    assertEquals( IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, actualValidateResult );
  }

  @Test
  public void validationSuccessful() {
    ResultSetCompareComponent rscc = createResultSetCompareComponent();
    IPentahoResultSet rs = Mockito.mock( IPentahoResultSet.class );
    ResultSetCompareAction resultSetCompareAction = createResultSetCompareAction( rs, rs, 0, false, true );
    rscc.setActionDefinition( resultSetCompareAction );
    int actualValidateResult = rscc.validate();
    assertEquals( IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK, actualValidateResult );
  }

  @Test
  public void execute_fails_without_validation() {
    ResultSetCompareComponent rscc = createResultSetCompareComponent();
    IPentahoResultSet rs = Mockito.mock( IPentahoResultSet.class );
    ResultSetCompareAction resultSetCompareAction = createResultSetCompareAction( rs, rs, 0, false, true );
    rscc.setActionDefinition( resultSetCompareAction );
    int actualExecuteResult = rscc.execute();
    assertNotEquals( IRuntimeContext.RUNTIME_STATUS_SUCCESS, actualExecuteResult );
  }

  @Test
  public void execute_fails_when_resultSets_have_different_number_of_rows() {
    ResultSetCompareComponent rscc = createResultSetCompareComponent();
    IPentahoResultSet rs1 = createResultSet( 1, 1, RESULT_SET_ELEMENT_VALUE );
    IPentahoResultSet rs2 = createResultSet( 1, 2, RESULT_SET_ELEMENT_VALUE );
    ResultSetCompareAction resultSetCompareAction = createResultSetCompareAction( rs1, rs2, 0, false, true );
    rscc.setActionDefinition( resultSetCompareAction );
    rscc.validate();
    int actualExecuteResult = rscc.execute();
    assertEquals( IRuntimeContext.RUNTIME_STATUS_FAILURE, actualExecuteResult );
  }

  @Test
  public void execute_fails_when_resultSets_have_different_number_of_columns() {
    ResultSetCompareComponent rscc = createResultSetCompareComponent();
    IPentahoResultSet rs1 = createResultSet( 1, 1, RESULT_SET_ELEMENT_VALUE );
    IPentahoResultSet rs2 = createResultSet( 2, 1, RESULT_SET_ELEMENT_VALUE );
    ResultSetCompareAction resultSetCompareAction = createResultSetCompareAction( rs1, rs2, 0, false, true );
    rscc.setActionDefinition( resultSetCompareAction );
    rscc.validate();
    int actualExecuteResult = rscc.execute();
    assertEquals( IRuntimeContext.RUNTIME_STATUS_FAILURE, actualExecuteResult );
  }

  @Test
  public void execute_fails_when_resultSets_have_different_values_in_compareColumn() {
    ResultSetCompareComponent rscc = createResultSetCompareComponent();
    IPentahoResultSet rs1 = createResultSet( new String[][] { { FIRST_ROW }, { SECOND_ROW } } );
    IPentahoResultSet rs2 = createResultSet( new String[][] { { FIRST_ROW }, { "SECOND" } } );
    ResultSetCompareAction resultSetCompareAction = createResultSetCompareAction( rs1, rs2, 0, false, true );
    rscc.setActionDefinition( resultSetCompareAction );
    rscc.validate();
    int actualExecuteResult = rscc.execute();
    assertEquals( IRuntimeContext.RUNTIME_STATUS_FAILURE, actualExecuteResult );
  }

  @Test
  public void executeSuccessful() {
    ResultSetCompareComponent rscc = createResultSetCompareComponent();
    IPentahoResultSet rs1 = createResultSet( new String[][] { { FIRST_ROW }, { SECOND_ROW } } );
    IPentahoResultSet rs2 = createResultSet( new String[][] { { FIRST_ROW }, { SECOND_ROW } } );
    ResultSetCompareAction resultSetCompareAction = createResultSetCompareAction( rs1, rs2, 0, false, true );
    rscc.setActionDefinition( resultSetCompareAction );
    rscc.validate();
    int actualExecuteResult = rscc.execute();
    assertEquals( IRuntimeContext.RUNTIME_STATUS_SUCCESS, actualExecuteResult );
  }

  @After
  public void tearDown() {
    LogUtil.setLevel(LogManager.getLogger(), Level.OFF);
  }

  private static int callValidationWithResultSets( IPentahoResultSet resultSet1, IPentahoResultSet resultSet2 ) {
    ResultSetCompareComponent rscc = createResultSetCompareComponent();
    ResultSetCompareAction resultSetCompareAction =
        createResultSetCompareAction( resultSet1, resultSet2, 0, false, true );
    rscc.setActionDefinition( resultSetCompareAction );
    return rscc.validate();
  }

  private static IPentahoResultSet createResultSet( int columnCount, int rowCount, Object resultSetElement ) {
    IPentahoResultSet rs = Mockito.mock( IPentahoResultSet.class );
    when( rs.getColumnCount() ).thenReturn( columnCount );
    when( rs.getRowCount() ).thenReturn( rowCount );
    when( rs.getValueAt( anyInt(), anyInt() ) ).thenReturn( resultSetElement );
    return rs;
  }

  private static IPentahoResultSet createResultSet( final Object[][] resultSet ) {
    IPentahoResultSet rs = Mockito.mock( IPentahoResultSet.class );
    when( rs.getRowCount() ).thenReturn( resultSet.length );
    when( rs.getColumnCount() ).thenAnswer( new Answer<Integer>() {

      @Override
      public Integer answer( InvocationOnMock invocation ) throws Throwable {
        return resultSet.length == 0 ? 0 : resultSet[0].length;
      }
    } );
    when( rs.getValueAt( anyInt(), anyInt() ) ).thenAnswer( new Answer<Object>() {

      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        int rowNumber = (Integer) args[0];
        int colNumber = (Integer) args[1];
        return resultSet[rowNumber][colNumber];
      }
    } );
    return rs;
  }

  private static ResultSetCompareComponent createResultSetCompareComponent() {
    ResultSetCompareComponent rscc = new ResultSetCompareComponent();
    IRuntimeContext context = mock( IRuntimeContext.class );
    rscc.setRuntimeContext( context );
    rscc.setSession( mock( IPentahoSession.class ) );
    rscc.setComponentDefinition( mock( Node.class ) );
    rscc.setInstanceId( "TEST_ID" );
    rscc.setProcessId( "TEST_PROCESS_ID" );
    rscc.setActionName( "TEST_ACTION_NAME" );
    return rscc;
  }

  private static ResultSetCompareAction createResultSetCompareAction( IPentahoResultSet resultSet1,
      IPentahoResultSet resultSet2, Integer compareColumnNum, boolean outputMismatches, boolean stopOnError ) {
    ResultSetCompareAction resultSetCompareAction = mock( ResultSetCompareAction.class );

    IActionInput resultSetInput1 = ActionInputConstant.NULL_INPUT;
    if ( resultSet1 != null ) {
      resultSetInput1 = mock( IActionInput.class );
      when( resultSetInput1.getValue() ).thenReturn( resultSet1 );
    }
    when( resultSetCompareAction.getResultSet1() ).thenReturn( resultSetInput1 );

    IActionInput resultSetInput2 = ActionInputConstant.NULL_INPUT;
    if ( resultSet2 != null ) {
      resultSetInput2 = mock( IActionInput.class );
      when( resultSetInput2.getValue() ).thenReturn( resultSet2 );
    }
    when( resultSetCompareAction.getResultSet2() ).thenReturn( resultSetInput2 );

    IActionInput compareColumnNumInput = ActionInputConstant.NULL_INPUT;
    if ( compareColumnNum != null ) {
      compareColumnNumInput = mock( IActionInput.class );
      when( compareColumnNumInput.getStringValue() ).thenReturn( String.valueOf( compareColumnNum ) );
    }
    when( resultSetCompareAction.getCompareColumnNum() ).thenReturn( compareColumnNumInput );

    IActionInput outputMismatchesInput = mock( IActionInput.class );
    when( outputMismatchesInput.getStringValue() ).thenReturn( String.valueOf( outputMismatches ) );
    when( resultSetCompareAction.getOutputMismatches() ).thenReturn( outputMismatchesInput );

    IActionInput stopOnErrorInput = mock( IActionInput.class );
    when( stopOnErrorInput.getStringValue() ).thenReturn( String.valueOf( stopOnError ) );
    when( resultSetCompareAction.getStopOnError() ).thenReturn( stopOnErrorInput );

    return resultSetCompareAction;
  }

}
