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


package org.pentaho.platform.api.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pentaho.actionsequence.dom.IActionControlStatement;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.actionsequence.dom.IActionIfStatement;
import org.pentaho.actionsequence.dom.IActionLoop;
import org.pentaho.actionsequence.dom.IActionSequenceExecutableStatement;

/**
 * Created by bgroves on 10/28/15.
 */
public class ActionSequenceExceptionTest {

  private static final String MSG = "msg";
  private static final Throwable THROWABLE = mock( Throwable.class );
  private static final String SESSION_NAME = "sessionName";
  private static final String INSTANCE_ID = "instanceId";
  private static final String ACTION_SEQUENCE_NAME = "actionSeqName";
  private static final String STEP_DESCRIPTION = "stepDescription";
  private static final Integer STEP_NUMBER = new Integer( 2 );
  private static final String ACTION_CLASS = "actionClass";
  private static final Integer LOOP_INDEX = new Integer( 3 );
  private static final IActionDefinition ACTION_DEF = mock( IActionDefinition.class );

  @Test
  public void testGettersSetters() {
    ActionSequenceException gettersAction = new ActionSequenceException();
    gettersAction.setStepDescription( STEP_DESCRIPTION );
    gettersAction.setActionSequenceName( ACTION_SEQUENCE_NAME );
    gettersAction.setStepNumber( STEP_NUMBER );
    gettersAction.setInstanceId( INSTANCE_ID );
    gettersAction.setSessionId( SESSION_NAME );
    gettersAction.setActionClass( ACTION_CLASS );
    gettersAction.setLoopIndex( LOOP_INDEX );

    assertEquals( STEP_DESCRIPTION, gettersAction.getStepDescription() );
    assertEquals( ACTION_SEQUENCE_NAME, gettersAction.getActionSequenceName() );
    assertEquals( STEP_NUMBER, gettersAction.getStepNumber() );
    assertEquals( INSTANCE_ID, gettersAction.getInstanceId() );
    assertEquals( SESSION_NAME, gettersAction.getSessionId() );
    assertEquals( ACTION_CLASS, gettersAction.getActionClass() );
    assertEquals( LOOP_INDEX, gettersAction.getLoopIndex() );
    assertNotNull( gettersAction.getDate() );

    gettersAction.setActionDefinition( null );
    assertNull( gettersAction.getActionDefinition() );
  }

  @Test
  public void testConstructors() {
    ActionSequenceException constructorsAction = new ActionSequenceException( MSG );
    assertEquals( MSG, constructorsAction.getMessage() );
    assertNotNull( constructorsAction.getDate() );
    assertNull( constructorsAction.getStepNumber() );
    assertNull( constructorsAction.getStepDescription() );
    assertNull( constructorsAction.getActionSequenceName() );
    assertNull( constructorsAction.getInstanceId() );
    assertNull( constructorsAction.getSessionId() );
    assertNull( constructorsAction.getActionClass() );
    assertNull( constructorsAction.getCause() );

    constructorsAction = new ActionSequenceException( MSG, THROWABLE );
    assertEquals( MSG, constructorsAction.getMessage() );
    assertEquals( THROWABLE, constructorsAction.getCause() );
    assertNotNull( constructorsAction.getDate() );
    assertNull( constructorsAction.getStepNumber() );
    assertNull( constructorsAction.getStepDescription() );
    assertNull( constructorsAction.getActionSequenceName() );
    assertNull( constructorsAction.getInstanceId() );
    assertNull( constructorsAction.getSessionId() );
    assertNull( constructorsAction.getActionClass() );

    constructorsAction = new ActionSequenceException( THROWABLE );
    assertEquals( THROWABLE, constructorsAction.getCause() );
    assertNotNull( constructorsAction.getDate() );
    assertNotNull( constructorsAction.getMessage() );
    assertNull( constructorsAction.getStepNumber() );
    assertNull( constructorsAction.getStepDescription() );
    assertNull( constructorsAction.getActionSequenceName() );
    assertNull( constructorsAction.getInstanceId() );
    assertNull( constructorsAction.getSessionId() );
    assertNull( constructorsAction.getActionClass() );

    constructorsAction = new ActionSequenceException( MSG, THROWABLE, SESSION_NAME, INSTANCE_ID, ACTION_SEQUENCE_NAME,
      STEP_DESCRIPTION, ACTION_CLASS );
    assertEquals( MSG, constructorsAction.getMessage() );
    assertEquals( THROWABLE, constructorsAction.getCause() );
    assertEquals( STEP_DESCRIPTION, constructorsAction.getStepDescription() );
    assertEquals( ACTION_SEQUENCE_NAME, constructorsAction.getActionSequenceName() );
    assertEquals( INSTANCE_ID, constructorsAction.getInstanceId() );
    assertEquals( SESSION_NAME, constructorsAction.getSessionId() );
    assertEquals( ACTION_CLASS, constructorsAction.getActionClass() );
    assertNotNull( constructorsAction.getDate() );
    assertNull( constructorsAction.getActionDefinition() );

    constructorsAction = new ActionSequenceException( MSG, THROWABLE, SESSION_NAME, INSTANCE_ID, ACTION_SEQUENCE_NAME,
      ACTION_DEF );
    assertEquals( MSG, constructorsAction.getMessage() );
    assertEquals( THROWABLE, constructorsAction.getCause() );
    assertEquals( ACTION_SEQUENCE_NAME, constructorsAction.getActionSequenceName() );
    assertEquals( INSTANCE_ID, constructorsAction.getInstanceId() );
    assertEquals( SESSION_NAME, constructorsAction.getSessionId() );
    assertEquals( ACTION_DEF, constructorsAction.getActionDefinition() );
    assertNotNull( constructorsAction.getDate() );
    assertNull( constructorsAction.getStepDescription() );
    assertNull( constructorsAction.getActionClass() );

    constructorsAction = new ActionSequenceException( MSG, SESSION_NAME, INSTANCE_ID, ACTION_SEQUENCE_NAME,
      ACTION_DEF );
    assertEquals( MSG, constructorsAction.getMessage() );
    assertNull( constructorsAction.getCause() );
    assertEquals( ACTION_SEQUENCE_NAME, constructorsAction.getActionSequenceName() );
    assertEquals( INSTANCE_ID, constructorsAction.getInstanceId() );
    assertEquals( SESSION_NAME, constructorsAction.getSessionId() );
    assertEquals( ACTION_DEF, constructorsAction.getActionDefinition() );
    assertNotNull( constructorsAction.getDate() );
    assertNull( constructorsAction.getStepDescription() );
    assertNull( constructorsAction.getActionClass() );
    assertNull( constructorsAction.getCause() );
  }

  @Test
  public void testPrint() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter( stream, true );

    ActionSequenceException exception = new ActionSequenceException( MSG );
    exception.setActionDefinition( null );
    exception.printActionExecutionStack( writer );
    assertEquals( "", stream.toString() );

    IActionDefinition actionDef = mock( IActionDefinition.class, withSettings().extraInterfaces( IActionIfStatement.class ) );
    exception.setActionDefinition( actionDef );
    verify( actionDef ).getDescription();
    verify( actionDef ).getComponentName();
    exception.printActionExecutionStack( writer );
    verify( (IActionIfStatement) actionDef ).getCondition();
    assertNotEquals( "", stream.toString() );

    actionDef = mock( IActionDefinition.class, withSettings().extraInterfaces( IActionLoop.class ) );
    exception.setActionDefinition( actionDef );
    writer = new PrintWriter( stream, true );
    exception.printActionExecutionStack( writer );
    verify( (IActionLoop) actionDef ).getLoopOn();
    assertNotEquals( "", stream.toString() );

    actionDef = mock( IActionDefinition.class );
    exception.setActionDefinition( actionDef );
    writer = new PrintWriter( stream, true );
    exception.printActionExecutionStack( writer );
    verify( actionDef, times( 2 ) ).getDescription();
    verify( actionDef, times( 2 ) ).getComponentName();
    assertNotEquals( "", stream.toString() );
  }

  @Test
  public void testPrintStack() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter( stream, true );
    ActionSequenceException exception = new ActionSequenceException( MSG );

    IActionSequenceExecutableStatement statement = mock( IActionControlStatement.class );
    exception._printStack( statement, writer, "" );
    assertNotEquals( "", stream.toString() );

    statement = mock( IActionSequenceExecutableStatement.class );
    exception._printStack( statement, writer, "" );
    assertNotEquals( "", stream.toString() );

  }

  @Test
  public void testPrintStackRecursion() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter( stream, true );
    ActionSequenceException exception = new ActionSequenceException( MSG );

    IActionSequenceExecutableStatement statement = mock( IActionSequenceExecutableStatement.class );
    IActionControlStatement parent = mock( IActionControlStatement.class );
    Mockito.when( statement.getParent() ).thenReturn( parent );
    exception._printStack( statement, writer, "" );
    assertNotEquals( "", stream.toString() );
  }
}
