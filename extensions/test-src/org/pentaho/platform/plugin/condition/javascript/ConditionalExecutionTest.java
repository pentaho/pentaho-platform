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
