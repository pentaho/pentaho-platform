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


package org.pentaho.platform.engine.core.audit;

/**
 * @author James Dixon
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
public class MessageTypes {

  public static String PROCESS_START = "process_start"; //$NON-NLS-1$

  public static String PROCESS_END = "process_end"; //$NON-NLS-1$

  public static final String INSTANCE_START = "instance_start"; //$NON-NLS-1$

  public static final String INSTANCE_END = "instance_end"; //$NON-NLS-1$

  public static final String INSTANCE_FAILED = "instance_failed"; //$NON-NLS-1$

  public static final String RECREATED_INSTANCE_START = "recreated_instance_start"; //$NON-NLS-1$

  public static final String RECREATED_INSTANCE_END = "recreated_instance_end"; //$NON-NLS-1$

  public static final String RECREATED_INSTANCE_FAILED = "recreated_instance_failed"; //$NON-NLS-1$

  public static final String INSTANCE_ATTRIBUTE = "instance_attribute"; //$NON-NLS-1$

  public static final String SESSION_START = "session_start"; //$NON-NLS-1$

  public static final String SESSION_END = "session_end"; //$NON-NLS-1$

  public static final String ACTION_SEQUENCE_START = "action_sequence_start"; //$NON-NLS-1$

  public static final String ACTION_SEQUENCE_END = "action_sequence_end"; //$NON-NLS-1$

  public static final String ACTION_SEQUENCE_FAILED = "action_sequence_failed"; //$NON-NLS-1$

  public static final String COMPONENT_EXECUTE_START = "component_execution_start"; //$NON-NLS-1$

  public static final String COMPONENT_EXECUTE_END = "component_execution_end"; //$NON-NLS-1$

  public static final String COMPONENT_EXECUTE_FAILED = "component_execution_failed"; //$NON-NLS-1$

  public static final String DEPRECATION_WARNING = "deprecated"; //$NON-NLS-1$

  public static String PROCESS_ID_SESSION = "session"; //$NON-NLS-1$

  public static String PROCESS_ID_PORTLET = "portlet"; //$NON-NLS-1$

  public static String PROCESS_ID_HTTP = "http"; //$NON-NLS-1$

  public static final String START = "start"; //$NON-NLS-1$

  public static final String FAILED = "failed"; //$NON-NLS-1$

  public static String NOT_EXECUTED = "not_executed"; //$NON-NLS-1$

  public static final String END = "end"; //$NON-NLS-1$

  public static final String VALIDATION = "validation"; //$NON-NLS-1$

  public static final String EXECUTION = "execution"; //$NON-NLS-1$

  public static String UNKNOWN_ENTRY = "unkown"; //$NON-NLS-1$

  public static String ACTION_SEQUENCE_EXECUTE_CONDITIONAL = "condition"; //$NON-NLS-1$

  public static final String CANCELLED = "cancelled"; //$NON-NLS-1$
}
